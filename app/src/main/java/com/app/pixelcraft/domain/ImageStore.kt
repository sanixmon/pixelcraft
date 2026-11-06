package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * [fileName] is always stored as a portable relative file name (e.g. `<uuid>.jpg`) so projects
 * survive backup/restore across users and devices. Legacy absolute paths are resolved seamlessly.
 */
data class ImportedImage(val path: String, val width: Int, val height: Int)

class ImageStore(private val imagesDirProvider: () -> File, private val context: Context? = null) {

    constructor(context: Context) : this({ File(context.filesDir, "images") }, context.applicationContext)

    val imagesDir: File
        get() = imagesDirProvider().apply { if (!exists()) mkdirs() }

    private val maxCacheBytes: Int =
        (Runtime.getRuntime().maxMemory() / 8L)
            .coerceIn(4L * 1024L * 1024L, 64L * 1024L * 1024L)
            .toInt()

    private val previewCache = object : LruCache<String, Bitmap>(maxCacheBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.allocationByteCount.coerceAtLeast(1)
        }
    }

    init {
        defaultInstance = this
    }

    /** Converts either a legacy `/data/user/0/.../<uuid>.jpg` path or `<uuid>.jpg` into a portable relative filename. */
    fun toRelativeFileName(pathOrName: String): String = Companion.toRelativeFileName(pathOrName)

    /** Resolves a relative filename (or existing absolute path) into a readable [File]. */
    fun resolveFile(pathOrName: String): File {
        val candidate = File(pathOrName)
        if (candidate.isAbsolute && candidate.exists()) return candidate
        return File(imagesDir, candidate.name)
    }

    suspend fun import(uri: Uri): Result<ImportedImage> = withContext(Dispatchers.IO) {
        runCatching {
            val ctx = requireNotNull(context) { "Android Context required to import URI" }
            val resolver = ctx.contentResolver
            val mime = resolver.getType(uri)?.lowercase().orEmpty()

            // Probe original bounds first without allocating pixel memory
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

            val rawWidth = bounds.outWidth
            val rawHeight = bounds.outHeight
            val fitsMasterDirectly = rawWidth in 1..MASTER_MAX_DIMENSION &&
                rawHeight in 1..MASTER_MAX_DIMENSION &&
                (mime == "image/png" || mime == "image/webp")

            if (fitsMasterDirectly) {
                // Fast zero-loss stream copy when PNG/WebP has no EXIF orientation & fits master bounds
                val ext = if (mime == "image/webp") "webp" else "png"
                val fileName = "${UUID.randomUUID()}.$ext"
                val dest = File(imagesDir, fileName)
                val temp = File(imagesDir, "$fileName.tmp")
                resolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(temp).use { out -> input.copyTo(out) }
                } ?: error("Not a readable picture")
                check(temp.renameTo(dest)) { "Could not finalize imported picture" }

                val proxy = decodeProxyBitmap(dest)
                if (proxy != null) {
                    previewCache.put(fileName, proxy)
                }
                return@runCatching ImportedImage(fileName, rawWidth, rawHeight)
            }

            // Decode with ImageDecoder (API 28+ applies EXIF orientation automatically) up to 4096px master
            val source = ImageDecoder.createSource(resolver, uri)
            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val sample = ImageFit.sampleSize(info.size.width, info.size.height, MASTER_MAX_DIMENSION)
                decoder.setTargetSampleSize(sample)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }

            val hasAlpha = decoded.hasAlpha()
            val ext = if (hasAlpha) "png" else "jpg"
            val fileName = "${UUID.randomUUID()}.$ext"
            val file = File(imagesDir, fileName)
            val bytesStream = java.io.ByteArrayOutputStream()
            val format = if (hasAlpha) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
            check(decoded.compress(format, 94, bytesStream)) { "Could not encode picture" }
            DraftStore.writeAtomically(file, bytesStream.toByteArray())

            val proxy = if (decoded.width > PROXY_MAX_DIMENSION || decoded.height > PROXY_MAX_DIMENSION) {
                val fit = ImageFit.fitWithin(
                    decoded.width,
                    decoded.height,
                    PROXY_MAX_DIMENSION.toFloat(),
                    PROXY_MAX_DIMENSION.toFloat()
                )
                Bitmap.createScaledBitmap(
                    decoded,
                    fit.width.toInt().coerceAtLeast(1),
                    fit.height.toInt().coerceAtLeast(1),
                    true
                )
            } else {
                decoded
            }
            previewCache.put(fileName, proxy)
            ImportedImage(fileName, decoded.width, decoded.height)
        }
    }

    /** Loads downsampled preview proxy (~1536px) cached in [LruCache] for smooth 60fps canvas rendering. */
    fun loadPreviewBitmap(pathOrName: String): Bitmap? {
        val key = toRelativeFileName(pathOrName)
        previewCache.get(key)?.takeIf { !it.isRecycled }?.let { return it }

        val file = resolveFile(pathOrName)
        if (!file.exists()) return null
        val decoded = decodeProxyBitmap(file) ?: return null
        previewCache.put(key, decoded)
        return decoded
    }

    /** Loads full-resolution master bitmap (up to 4096px) for high-resolution export. */
    fun loadMasterBitmap(pathOrName: String, maxDimension: Int = MASTER_MAX_DIMENSION): Bitmap? {
        val file = resolveFile(pathOrName)
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageFit.sampleSize(bounds.outWidth, bounds.outHeight, maxDimension)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, opts)
    }

    private fun decodeProxyBitmap(file: File): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageFit.sampleSize(bounds.outWidth, bounds.outHeight, PROXY_MAX_DIMENSION)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, opts)
    }

    fun trimMemory(level: Int) {
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            previewCache.evictAll()
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            previewCache.trimToSize(maxCacheBytes / 2)
        }
    }

    /**
     * Garbage-collects orphan image files not referenced by any saved project, active draft,
     * or live editor session, respecting [gracePeriodMs] (default 24 hours) so recent undo states remain safe.
     */
    suspend fun collectOrphanImages(
        referencedPaths: Set<String>,
        gracePeriodMs: Long = DEFAULT_GC_GRACE_MS,
        nowMs: Long = System.currentTimeMillis()
    ): Int = withContext(Dispatchers.IO) {
        val referencedNames = referencedPaths
            .filter { it.isNotBlank() }
            .mapTo(HashSet()) { toRelativeFileName(it) }

        val files = imagesDir.listFiles() ?: return@withContext 0
        var deletedCount = 0
        for (file in files) {
            if (!file.isFile) continue
            if (file.name in referencedNames) continue
            val ageMs = nowMs - file.lastModified()
            if (ageMs >= gracePeriodMs) {
                previewCache.remove(file.name)
                if (file.delete()) deletedCount++
            }
        }
        deletedCount
    }

    companion object {
        const val MASTER_MAX_DIMENSION = 4096
        const val PROXY_MAX_DIMENSION = 1536
        const val DEFAULT_GC_GRACE_MS = 24L * 60L * 60L * 1000L

        @Volatile
        private var defaultInstance: ImageStore? = null

        fun toRelativeFileName(pathOrName: String): String {
            if (pathOrName.isBlank()) return pathOrName
            val normalized = pathOrName.replace('\\', '/')
            return normalized.substringAfterLast('/')
        }

        /** Backward-compatible static helper delegating to active [ImageStore] instance or direct file decode. */
        fun loadBitmap(pathOrName: String, forExport: Boolean = false): Bitmap? {
            val instance = defaultInstance
            if (instance != null) {
                return if (forExport) {
                    instance.loadMasterBitmap(pathOrName)
                } else {
                    instance.loadPreviewBitmap(pathOrName)
                }
            }
            val file = File(pathOrName)
            return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        }
    }
}
