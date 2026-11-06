package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
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
            putCached(file.absolutePath, bitmap)
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
        private val maxCacheBytes = (Runtime.getRuntime().maxMemory() / 8).coerceAtLeast(4 * 1024 * 1024)
        private var currentCacheBytes = 0L
        private val cache = LinkedHashMap<String, Bitmap>(16, 0.75f, true)

        @Synchronized
        private fun getCached(path: String): Bitmap? = cache[path]

        @Synchronized
        private fun putCached(path: String, bitmap: Bitmap) {
            cache.remove(path)?.let { currentCacheBytes -= it.byteCount }
            cache[path] = bitmap
            currentCacheBytes += bitmap.byteCount
            val iter = cache.entries.iterator()
            while (currentCacheBytes > maxCacheBytes && iter.hasNext()) {
                val eldest = iter.next()
                currentCacheBytes -= eldest.value.byteCount
                iter.remove()
            }
        }

        /** Decodes (or returns cached) bitmap for a stored picture. Blocking: call off the main thread. */
        fun loadBitmap(path: String): Bitmap? {
            getCached(path)?.let { return it }
            val bitmap = BitmapFactory.decodeFile(path) ?: return null
            putCached(path, bitmap)
            return bitmap
        }
    }
}
