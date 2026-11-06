package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.util.AtomicFile
import com.app.pixelcraft.data.model.CURRENT_PROJECT_SCHEMA_VERSION
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ProjectSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Atomic, corruption-resilient project repository supporting:
 * - `AtomicFile` writes with compact JSON (`prettyPrint = false`)
 * - Fast `index.json` metadata index (`List<ProjectSummary>`)
 * - 256px WebP thumbnails (`thumbnails/<id>.webp`)
 * - Portable relative image filenames (`<uuid>.jpg`) with automatic v1 -> v2 migration
 * - Rename, Duplicate, and `.pixelcraft` ZIP archive export/import
 */
class ProjectStorage(private val projectsDirProvider: () -> File) {

    constructor(context: Context) : this({ File(context.filesDir, "pixelcraft_projects") })

    private val indexMutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    val projectsDir: File
        get() = projectsDirProvider().apply {
            if (!exists()) mkdirs()
        }

    val thumbnailsDir: File
        get() = File(projectsDir, "thumbnails").apply {
            if (!exists()) mkdirs()
        }

    private val indexFile: File
        get() = File(projectsDir, INDEX_FILE_NAME)

    fun resolveThumbnailFile(projectId: String): File {
        return File(thumbnailsDir, "$projectId.webp")
    }

    suspend fun saveProject(
        project: ProjectData,
        thumbnailBitmap: Bitmap? = null
    ): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            var thumbName = project.thumbnailFileName
            if (thumbnailBitmap != null) {
                val thumbFile = resolveThumbnailFile(project.id)
                val out = ByteArrayOutputStream()
                @Suppress("DEPRECATION")
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    Bitmap.CompressFormat.WEBP
                }
                if (thumbnailBitmap.compress(format, 82, out)) {
                    DraftStore.writeAtomically(thumbFile, out.toByteArray())
                    thumbName = thumbFile.name
                }
            } else if (thumbName == null && resolveThumbnailFile(project.id).exists()) {
                thumbName = resolveThumbnailFile(project.id).name
            }

            val migrated = migrateAndNormalize(
                project.copy(
                    schemaVersion = CURRENT_PROJECT_SCHEMA_VERSION,
                    thumbnailFileName = thumbName
                )
            )
            val projectFile = File(projectsDir, "${migrated.id}.json")
            val contentBytes = json.encodeToString(migrated).toByteArray(Charsets.UTF_8)
            DraftStore.writeAtomically(projectFile, contentBytes)

            indexMutex.withLock {
                val existing = readOrRebuildIndexUnlocked().filterNot { it.id == migrated.id }
                val updated = (listOf(migrated.toSummary()) + existing).sortedByDescending { it.timestamp }
                writeIndexUnlocked(updated)
            }
            migrated
        }
    }

    suspend fun loadProject(id: String): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(projectsDir, "$id.json")
            if (!file.exists()) error("Project not found")
            val rawBytes = AtomicFile(file).readFully()
            val decoded = json.decodeFromString<ProjectData>(rawBytes.toString(Charsets.UTF_8))
            migrateAndNormalize(decoded)
        }
    }

    /** Fast summary listing from `index.json` (automatically rebuilt if missing). */
    suspend fun listProjectSummaries(): Result<List<ProjectSummary>> = withContext(Dispatchers.IO) {
        runCatching {
            indexMutex.withLock {
                readOrRebuildIndexUnlocked()
            }
        }
    }

    /** Loads full [ProjectData] records sorted newest-first. */
    suspend fun listProjects(): Result<List<ProjectData>> = withContext(Dispatchers.IO) {
        runCatching {
            val files = projectsDir.listFiles { _, name ->
                name.endsWith(".json") && name != INDEX_FILE_NAME
            } ?: emptyArray()
            files.mapNotNull { file ->
                runCatching {
                    val bytes = AtomicFile(file).readFully()
                    migrateAndNormalize(json.decodeFromString<ProjectData>(bytes.toString(Charsets.UTF_8)))
                }.getOrNull()
            }.sortedByDescending { it.timestamp }
        }
    }

    suspend fun renameProject(id: String, newName: String): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            val clean = newName.trim().ifBlank { "Untitled Project" }
            val loaded = loadProject(id).getOrThrow()
            val renamed = loaded.copy(name = clean, timestamp = System.currentTimeMillis())
            saveProject(renamed).getOrThrow()
        }
    }

    suspend fun duplicateProject(
        id: String,
        newId: String = UUID.randomUUID().toString()
    ): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            val source = loadProject(id).getOrThrow()
            val sourceThumb = resolveThumbnailFile(id)
            var duplicatedThumbName: String? = null
            if (sourceThumb.exists()) {
                val destThumb = resolveThumbnailFile(newId)
                DraftStore.writeAtomically(destThumb, sourceThumb.readBytes())
                duplicatedThumbName = destThumb.name
            }
            val copy = source.copy(
                id = newId,
                name = "${source.name} Copy",
                timestamp = System.currentTimeMillis(),
                thumbnailFileName = duplicatedThumbName
            )
            saveProject(copy).getOrThrow()
        }
    }

    suspend fun deleteProject(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(projectsDir, "$id.json")
            AtomicFile(file).delete()
            if (file.exists()) file.delete()

            val thumb = resolveThumbnailFile(id)
            AtomicFile(thumb).delete()
            if (thumb.exists()) thumb.delete()

            indexMutex.withLock {
                val updated = readOrRebuildIndexUnlocked().filterNot { it.id == id }
                writeIndexUnlocked(updated)
            }
        }
    }

    /** Collects all relative image file names referenced across all saved projects for orphan GC. */
    suspend fun collectAllReferencedImageNames(): Set<String> = withContext(Dispatchers.IO) {
        val projects = listProjects().getOrDefault(emptyList())
        val refs = HashSet<String>()
        for (proj in projects) {
            proj.snapshot.canvasConfig.customImageUri?.takeIf { it.isNotBlank() }?.let {
                refs.add(ImageStore.toRelativeFileName(it))
            }
            for (layer in proj.snapshot.layers) {
                layer.imageLocalPath?.takeIf { it.isNotBlank() }?.let {
                    refs.add(ImageStore.toRelativeFileName(it))
                }
            }
        }
        refs
    }

    /**
     * Exports a portable `.pixelcraft` ZIP archive containing `project.json`, optional `thumb.webp`,
     * and every image asset in `assets/` referenced by the project.
     */
    suspend fun exportProjectArchive(
        projectId: String,
        imagesDir: File,
        outputStream: OutputStream
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val project = loadProject(projectId).getOrThrow()
            ZipOutputStream(outputStream.buffered()).use { zip ->
                val projectBytes = json.encodeToString(project).toByteArray(Charsets.UTF_8)
                zip.putNextEntry(ZipEntry("project.json"))
                zip.write(projectBytes)
                zip.closeEntry()

                val thumb = resolveThumbnailFile(projectId)
                if (thumb.exists()) {
                    zip.putNextEntry(ZipEntry("thumb.webp"))
                    thumb.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }

                val assetNames = buildSet {
                    project.snapshot.canvasConfig.customImageUri?.takeIf { it.isNotBlank() }?.let {
                        add(ImageStore.toRelativeFileName(it))
                    }
                    project.snapshot.layers.forEach { layer ->
                        layer.imageLocalPath?.takeIf { it.isNotBlank() }?.let {
                            add(ImageStore.toRelativeFileName(it))
                        }
                    }
                }

                for (assetName in assetNames) {
                    val safeName = File(assetName).name
                    val assetFile = File(imagesDir, safeName)
                    if (assetFile.exists() && assetFile.isFile) {
                        zip.putNextEntry(ZipEntry("assets/$safeName"))
                        assetFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
        }
    }

    /**
     * Imports a `.pixelcraft` ZIP archive, extracting bundled image assets with Zip-Slip protection
     * and registering the imported project under a fresh ID to avoid overwriting existing projects.
     */
    suspend fun importProjectArchive(
        inputStream: InputStream,
        imagesDir: File
    ): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            if (!imagesDir.exists()) imagesDir.mkdirs()
            val canonicalImagesDir = imagesDir.canonicalPath + File.separator

            var rawProjectJson: String? = null
            var thumbBytes: ByteArray? = null
            val remappedAssets = HashMap<String, String>()

            ZipInputStream(inputStream.buffered()).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        when {
                            entry.name == "project.json" -> {
                                rawProjectJson = zip.readBytes().toString(Charsets.UTF_8)
                            }
                            entry.name == "thumb.webp" -> {
                                thumbBytes = zip.readBytes()
                            }
                            entry.name.startsWith("assets/") -> {
                                val originalName = File(entry.name).name
                                val ext = originalName.substringAfterLast('.', "png")
                                val targetName = if (File(imagesDir, originalName).exists()) {
                                    "${UUID.randomUUID()}.$ext"
                                } else {
                                    originalName
                                }
                                val dest = File(imagesDir, targetName)
                                check(dest.canonicalPath.startsWith(canonicalImagesDir)) {
                                    "Blocked unsafe archive entry: ${entry.name}"
                                }
                                DraftStore.writeAtomically(dest, zip.readBytes())
                                remappedAssets[originalName] = targetName
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val jsonText = requireNotNull(rawProjectJson) { "Invalid .pixelcraft file: missing project.json" }
            val parsed = migrateAndNormalize(json.decodeFromString<ProjectData>(jsonText))
            val importedId = UUID.randomUUID().toString()

            var importedThumbName: String? = null
            if (thumbBytes != null) {
                val thumbFile = resolveThumbnailFile(importedId)
                DraftStore.writeAtomically(thumbFile, thumbBytes!!)
                importedThumbName = thumbFile.name
            }

            val remappedConfig = parsed.snapshot.canvasConfig.let { cfg ->
                val bg = cfg.customImageUri?.let { remappedAssets[ImageStore.toRelativeFileName(it)] ?: it }
                cfg.copy(customImageUri = bg)
            }
            val remappedLayers = parsed.snapshot.layers.map { layer ->
                val img = layer.imageLocalPath?.let { remappedAssets[ImageStore.toRelativeFileName(it)] ?: it }
                layer.copy(imageLocalPath = img)
            }

            val importedProject = parsed.copy(
                id = importedId,
                timestamp = System.currentTimeMillis(),
                thumbnailFileName = importedThumbName,
                snapshot = parsed.snapshot.copy(
                    canvasConfig = remappedConfig,
                    layers = remappedLayers
                )
            )
            saveProject(importedProject).getOrThrow()
        }
    }

    private fun readOrRebuildIndexUnlocked(): List<ProjectSummary> {
        val currentProjectFiles = projectsDir.listFiles { _, name ->
            name.endsWith(".json") && name != INDEX_FILE_NAME
        } ?: emptyArray()

        if (indexFile.exists()) {
            val cached = runCatching {
                val bytes = AtomicFile(indexFile).readFully()
                json.decodeFromString<List<ProjectSummary>>(bytes.toString(Charsets.UTF_8))
            }.getOrNull()
            if (cached != null && cached.size == currentProjectFiles.size) {
                return cached.sortedByDescending { it.timestamp }
            }
        }

        val rebuilt = currentProjectFiles.mapNotNull { file ->
            runCatching {
                val bytes = AtomicFile(file).readFully()
                migrateAndNormalize(json.decodeFromString<ProjectData>(bytes.toString(Charsets.UTF_8))).toSummary()
            }.getOrNull()
        }.sortedByDescending { it.timestamp }

        writeIndexUnlocked(rebuilt)
        return rebuilt
    }

    private fun writeIndexUnlocked(summaries: List<ProjectSummary>) {
        val bytes = json.encodeToString(summaries).toByteArray(Charsets.UTF_8)
        DraftStore.writeAtomically(indexFile, bytes)
    }

    companion object {
        private const val INDEX_FILE_NAME = "index.json"

        fun migrateAndNormalize(project: ProjectData): ProjectData {
            return project.copy(
                schemaVersion = CURRENT_PROJECT_SCHEMA_VERSION,
                snapshot = normalizeSnapshotImagePaths(project.snapshot)
            )
        }

        fun normalizeSnapshotImagePaths(snapshot: EditorSnapshot): EditorSnapshot {
            val cleanBgUri = snapshot.canvasConfig.customImageUri?.takeIf { it.isNotBlank() }?.let {
                ImageStore.toRelativeFileName(it)
            }
            val cleanLayers = snapshot.layers.map { layer ->
                val cleanImg = layer.imageLocalPath?.takeIf { it.isNotBlank() }?.let {
                    ImageStore.toRelativeFileName(it)
                }
                if (cleanImg == layer.imageLocalPath) layer else layer.copy(imageLocalPath = cleanImg)
            }
            return snapshot.copy(
                canvasConfig = snapshot.canvasConfig.copy(customImageUri = cleanBgUri),
                layers = cleanLayers
            )
        }
    }
}
