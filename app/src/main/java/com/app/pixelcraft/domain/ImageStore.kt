package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ImportedImage(val path: String, val width: Int, val height: Int)

/**
 * Copies picked pictures into app storage (picker URIs are temporary, projects must keep working)
 * and serves decoded bitmaps from a shared memory cache to both the editor canvas and the exporter.
 */
class ImageStore(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }

    suspend fun import(uri: Uri): Result<ImportedImage> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decode(uri)
            val ext = if (bitmap.hasAlpha()) "png" else "jpg"
            val file = File(imagesDir, "${UUID.randomUUID()}.$ext")
            FileOutputStream(file).use { out ->
                val format = if (bitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                check(bitmap.compress(format, 92, out)) { "Could not store the picture" }
            }
            cache.put(file.absolutePath, bitmap)
            ImportedImage(file.absolutePath, bitmap.width, bitmap.height)
        }
    }

    private fun decode(uri: Uri): Bitmap {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies EXIF orientation for us.
            val source = ImageDecoder.createSource(resolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val sample = ImageFit.sampleSize(info.size.width, info.size.height, MAX_DIMENSION)
                decoder.setTargetSampleSize(sample)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        check(bounds.outWidth > 0 && bounds.outHeight > 0) { "Not a readable picture" }
        val opts = BitmapFactory.Options().apply {
            inSampleSize = ImageFit.sampleSize(bounds.outWidth, bounds.outHeight, MAX_DIMENSION)
        }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("Not a readable picture")
    }

    companion object {
        private const val MAX_DIMENSION = 2048

        private val cache = object : LruCache<String, Bitmap>(
            (Runtime.getRuntime().maxMemory() / 8).toInt()
        ) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
        }

        /** Decodes (or returns cached) bitmap for a stored picture. Blocking: call off the main thread. */
        fun loadBitmap(path: String): Bitmap? {
            cache.get(path)?.let { return it }
            val bitmap = BitmapFactory.decodeFile(path) ?: return null
            cache.put(path, bitmap)
            return bitmap
        }
    }
}
