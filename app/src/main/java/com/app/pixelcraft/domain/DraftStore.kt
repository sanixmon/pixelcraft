package com.app.pixelcraft.domain

import android.content.Context
import androidx.core.util.AtomicFile
import com.app.pixelcraft.data.model.DraftPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Atomic crash-safe persistence for the active unsaved editor draft (`files/draft.json`).
 */
class DraftStore(private val draftFileProvider: () -> File) {

    constructor(context: Context) : this({ File(context.filesDir, "draft.json") })

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val draftFile: File
        get() = draftFileProvider().apply { parentFile?.mkdirs() }

    suspend fun save(payload: DraftPayload): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val normalized = payload.copy(
                snapshot = ProjectStorage.normalizeSnapshotImagePaths(payload.snapshot)
            )
            val bytes = json.encodeToString(normalized).toByteArray(Charsets.UTF_8)
            writeAtomically(draftFile, bytes)
        }
    }

    suspend fun load(): DraftPayload? = withContext(Dispatchers.IO) {
        runCatching {
            val file = draftFile
            if (!file.exists()) return@withContext null
            val bytes = AtomicFile(file).readFully()
            val decoded = json.decodeFromString<DraftPayload>(bytes.toString(Charsets.UTF_8))
            decoded.copy(snapshot = ProjectStorage.normalizeSnapshotImagePaths(decoded.snapshot))
        }.getOrNull()
    }

    fun hasDraftSync(): Boolean {
        return runCatching { draftFile.exists() && draftFile.length() > 0L }.getOrDefault(false)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        runCatching {
            val atomic = AtomicFile(draftFile)
            atomic.delete()
            if (draftFile.exists()) draftFile.delete()
        }
    }

    companion object {
        internal fun writeAtomically(target: File, bytes: ByteArray) {
            target.parentFile?.mkdirs()
            val atomic = AtomicFile(target)
            var stream: java.io.FileOutputStream? = null
            try {
                stream = atomic.startWrite()
                stream.write(bytes)
                atomic.finishWrite(stream)
            } catch (t: Throwable) {
                if (stream != null) {
                    atomic.failWrite(stream)
                }
                throw t
            }
        }
    }
}
