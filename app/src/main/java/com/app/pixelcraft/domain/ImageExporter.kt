package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.presets.FontCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

enum class ExportQuality(val multiplier: Float, val label: String) {
    DEFAULT(1.0f, "Default"),
    LOW(0.5f, "Low (Fast)"),
    MEDIUM(1.0f, "Medium (Standard)"),
    HIGH(1.5f, "High"),
    VERY_HIGH(2.0f, "Very High (HD)"),
    ULTRA(3.0f, "Ultra (4K)")
}

enum class ExportFormat(val extension: String, val compressFormat: Bitmap.CompressFormat, val mimeType: String) {
    PNG("png", Bitmap.CompressFormat.PNG, "image/png"),
    JPEG("jpg", Bitmap.CompressFormat.JPEG, "image/jpeg")
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.PNG,
    val qualityPreset: ExportQuality = ExportQuality.MEDIUM,
    val jpegQuality: Int = 95
)

class ImageExporter(private val context: Context? = null) {

    companion object {
        const val MAX_EXPORT_DIMENSION = 8192

        fun computeTargetDimensions(baseWidth: Int, baseHeight: Int, multiplier: Float): Pair<Int, Int> {
            val safeW = baseWidth.coerceAtLeast(64)
            val safeH = baseHeight.coerceAtLeast(64)
            var outW = (safeW * multiplier).roundToInt().coerceAtLeast(64)
            var outH = (safeH * multiplier).roundToInt().coerceAtLeast(64)

            val longest = max(outW, outH)
            if (longest > MAX_EXPORT_DIMENSION) {
                val capRatio = MAX_EXPORT_DIMENSION.toFloat() / longest.toFloat()
                outW = (outW * capRatio).roundToInt().coerceAtLeast(64)
                outH = (outH * capRatio).roundToInt().coerceAtLeast(64)
            }
            return outW to outH
        }
    }

    suspend fun exportCanvasToBitmap(
        canvasConfig: CanvasConfig,
        layers: List<Layer>,
        options: ExportOptions
    ): Bitmap = withContext(Dispatchers.Default) {
        val (outWidth, outHeight) = computeTargetDimensions(
            canvasConfig.width,
            canvasConfig.height,
            options.qualityPreset.multiplier
        )

        val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val scaleRatioX = outWidth.toFloat() / canvasConfig.width.coerceAtLeast(1).toFloat()
        val scaleRatioY = outHeight.toFloat() / canvasConfig.height.coerceAtLeast(1).toFloat()
        canvas.scale(scaleRatioX, scaleRatioY)

        CanvasRenderer.renderBackground(
            canvas = canvas,
            config = canvasConfig,
            bitmapLookup = ImageStore::loadBitmap,
            drawCheckerboardWhenTransparent = false
        )
        if (context != null) {
            CanvasRenderer.renderLayers(
                canvas = canvas,
                config = canvasConfig,
                layers = layers,
                bitmapLookup = ImageStore::loadBitmap,
                typefaceResolver = { fontKey, isBold, isItalic ->
                    FontCatalog.resolveTypeface(context, fontKey, isBold, isItalic)
                }
            )
        } else {
            CanvasRenderer.renderLayers(
                canvas = canvas,
                config = canvasConfig,
                layers = layers,
                bitmapLookup = ImageStore::loadBitmap
            )
        }
        CanvasRenderer.renderFilters(
            canvas = canvas,
            config = canvasConfig
        )

        bitmap
    }
}
