package com.app.pixelcraft.domain

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Writes exported images into either:
 * 1. The public gallery under `Pictures/PixelCraft` (`save`), OR
 * 2. A temporary `cacheDir/share/` directory served via `FileProvider` (`saveToShareCache`) so direct sharing
 *    does not clutter the user's gallery (fixes Bug 2.16).
 */
class GallerySaver(private val context: Context) {

    suspend fun save(bitmap: Bitmap, options: ExportOptions, baseName: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val displayName = "$baseName.${options.format.extension}"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveScoped(bitmap, options, displayName)
                } else {
                    saveLegacy(bitmap, options, displayName)
                }
            }
        }

    suspend fun saveToShareCache(bitmap: Bitmap, options: ExportOptions, baseName: String): Result<Uri> =
        withContext(Dispatchers.IO) {
            runCatching {
                val shareDir = File(context.cacheDir, "share").apply { if (!exists()) mkdirs() }
                pruneOldShareFiles(shareDir)
                val displayName = "$baseName.${options.format.extension}"
                val file = File(shareDir, displayName)
                FileOutputStream(file).use { compress(bitmap, options, it) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            }
        }

    fun getFileProviderUri(file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun pruneOldShareFiles(dir: File) {
        val cutoff = System.currentTimeMillis() - 6L * 60L * 60L * 1000L
        dir.listFiles()?.forEach { f ->
            if (f.isFile && f.lastModified() < cutoff) {
                f.delete()
            }
        }
    }

    private fun compress(bitmap: Bitmap, options: ExportOptions, out: OutputStream) {
        val quality = if (options.format == ExportFormat.WEBP_LOSSLESS) {
            100
        } else {
            options.jpegQuality.coerceIn(10, 100)
        }
        check(bitmap.compress(options.format.resolveCompressFormat(), quality, out)) {
            "Could not encode the image"
        }
    }

    private fun saveScoped(bitmap: Bitmap, options: ExportOptions, displayName: String): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, options.format.mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: error("Could not create a gallery entry")
        try {
            val out = resolver.openOutputStream(uri) ?: error("Could not open the gallery entry")
            out.use { compress(bitmap, options, it) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
        return uri
    }

    /** Android 9 (API 28): requires WRITE_EXTERNAL_STORAGE, requested before gallery export. */
    @Suppress("DEPRECATION")
    private fun saveLegacy(bitmap: Bitmap, options: ExportOptions, displayName: String): Uri {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM)
        if (!dir.exists() && !dir.mkdirs()) error("Could not create ${dir.path}")
        val file = File(dir, displayName)
        FileOutputStream(file).use { compress(bitmap, options, it) }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATA, file.absolutePath)
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, options.format.mimeType)
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Saved ${file.name} but could not add it to the gallery")
    }

    companion object {
        const val ALBUM = "PixelCraft"
    }
}
