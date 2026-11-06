package com.app.pixelcraft.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.presets.FontCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Distinct export resolution multipliers (fixes Bug 2.14 duplicate `1.0x` entry).
 */
enum class ExportQuality(val multiplier: Float, val label: String) {
    LOW(0.5f, "Draft (0.5x)"),
    MEDIUM(1.0f, "Standard (1x)"),
    HIGH(1.5f, "High (1.5x)"),
    VERY_HIGH(2.0f, "HD (2x)"),
    ULTRA(3.0f, "Ultra (3x)")
}

enum class ExportFormat(
    val label: String,
    val extension: String,
    val mimeType: String,
    val supportsTransparency: Boolean,
    val usesQualitySlider: Boolean
) {
    PNG("PNG", "png", "image/png", supportsTransparency = true, usesQualitySlider = false),
    JPEG("JPEG", "jpg", "image/jpeg", supportsTransparency = false, usesQualitySlider = true),
    WEBP_LOSSY("WebP", "webp", "image/webp", supportsTransparency = true, usesQualitySlider = true),
    WEBP_LOSSLESS("WebP Lossless", "webp", "image/webp", supportsTransparency = true, usesQualitySlider = false);

    fun resolveCompressFormat(): Bitmap.CompressFormat = when (this) {
        PNG -> Bitmap.CompressFormat.PNG
        JPEG -> Bitmap.CompressFormat.JPEG
        WEBP_LOSSY -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
        WEBP_LOSSLESS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSLESS
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }
    }
}

data class ExportOptions(
    val format: ExportFormat = ExportFormat.PNG,
    val qualityPreset: ExportQuality = ExportQuality.MEDIUM,
    val jpegQuality: Int = 95,
    val selectedLayerOnly: Boolean = false,
    val trimTransparent: Boolean = false
)

data class ExportDimensionEstimate(
    val requestedWidth: Int,
    val requestedHeight: Int,
    val safeWidth: Int,
    val safeHeight: Int,
    val estimatedBitmapMb: Float,
    val cappedByMemory: Boolean
)

class ImageExporter(private val context: Context? = null) {

    companion object {
        const val MAX_EXPORT_DIMENSION = 8192

        /**
         * Dynamic pixel budget based on JVM max heap (~25% of maxMemory divided by 4 bytes per ARGB_8888 pixel),
         * preventing OutOfMemoryError on mid-range phones while allowing 4K+ exports on high-RAM devices (Bug 2.6).
         */
        fun safeMaxPixelsForHeap(maxHeapBytes: Long): Long {
            val rawBudget = (maxHeapBytes / 4L) / 4L
            return rawBudget.coerceIn(1_048_576L, 67_108_864L)
        }

        fun computeTargetDimensions(
            baseWidth: Int,
            baseHeight: Int,
            multiplier: Float,
            maxPixels: Long = Long.MAX_VALUE
        ): Pair<Int, Int> {
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

            val totalPixels = outW.toLong() * outH.toLong()
            if (maxPixels > 0L && totalPixels > maxPixels) {
                val scale = sqrt(maxPixels.toDouble() / totalPixels.toDouble()).toFloat()
                outW = (outW * scale).roundToInt().coerceAtLeast(64)
                outH = (outH * scale).roundToInt().coerceAtLeast(64)
            }
            return outW to outH
        }

        fun estimateDimensions(
            baseWidth: Int,
            baseHeight: Int,
            quality: ExportQuality,
            maxHeapBytes: Long = Runtime.getRuntime().maxMemory()
        ): ExportDimensionEstimate {
            val (reqW, reqH) = computeTargetDimensions(baseWidth, baseHeight, quality.multiplier, Long.MAX_VALUE)
            val pixelBudget = safeMaxPixelsForHeap(maxHeapBytes)
            val (safeW, safeH) = computeTargetDimensions(baseWidth, baseHeight, quality.multiplier, pixelBudget)
            val mb = (safeW.toLong() * safeH.toLong() * 4L).toFloat() / (1024f * 1024f)
            return ExportDimensionEstimate(
                requestedWidth = reqW,
                requestedHeight = reqH,
                safeWidth = safeW,
                safeHeight = safeH,
                estimatedBitmapMb = mb,
                cappedByMemory = safeW < reqW || safeH < reqH
            )
        }

        /** Trims fully transparent outer rows/columns while keeping a small 4px safety margin for anti-aliased edges. */
        fun trimTransparentBounds(source: Bitmap): Bitmap {
            val w = source.width
            val h = source.height
            if (w <= 8 || h <= 8) return source

            val rowPixels = IntArray(w)
            var top = -1
            var bottom = -1
            var left = w
            var right = -1

            for (y in 0 until h) {
                source.getPixels(rowPixels, 0, w, 0, y, w, 1)
                var rowHasAlpha = false
                for (x in 0 until w) {
                    if (Color.alpha(rowPixels[x]) > 2) {
                        rowHasAlpha = true
                        if (x < left) left = x
                        if (x > right) right = x
                    }
                }
                if (rowHasAlpha) {
                    if (top == -1) top = y
                    bottom = y
                }
            }

            if (top == -1 || right < left) {
                return source
            }

            val pad = 4
            val cropL = (left - pad).coerceAtLeast(0)
            val cropT = (top - pad).coerceAtLeast(0)
            val cropR = (right + pad + 1).coerceAtMost(w)
            val cropB = (bottom + pad + 1).coerceAtMost(h)
            val cropW = (cropR - cropL).coerceAtLeast(1)
            val cropH = (cropB - cropT).coerceAtLeast(1)
            if (cropW == w && cropH == h) return source

            return Bitmap.createBitmap(source, cropL, cropT, cropW, cropH)
        }
    }

    suspend fun exportCanvasToBitmap(
        canvasConfig: CanvasConfig,
        layers: List<Layer>,
        options: ExportOptions,
        selectedLayerIds: Set<String> = emptySet()
    ): Bitmap = withContext(Dispatchers.Default) {
        val effectiveConfig = if (options.selectedLayerOnly) {
            canvasConfig.copy(
                backgroundType = BackgroundType.TRANSPARENT,
                filters = CanvasFilters()
            )
        } else {
            canvasConfig
        }
        val effectiveLayers = if (options.selectedLayerOnly && selectedLayerIds.isNotEmpty()) {
            layers.filter { it.id in selectedLayerIds }
        } else {
            layers
        }

        val pixelBudget = safeMaxPixelsForHeap(Runtime.getRuntime().maxMemory())
        val (baseOutW, baseOutH) = computeTargetDimensions(
            effectiveConfig.width,
            effectiveConfig.height,
            options.qualityPreset.multiplier,
            maxPixels = pixelBudget
        )

        val fallbackScales = floatArrayOf(1.0f, 0.75f, 0.5f)
        var lastOom: OutOfMemoryError? = null

        for (fallback in fallbackScales) {
            ensureActive()
            val outWidth = (baseOutW * fallback).roundToInt().coerceAtLeast(64)
            val outHeight = (baseOutH * fallback).roundToInt().coerceAtLeast(64)
            try {
                val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                if (!options.format.supportsTransparency && effectiveConfig.backgroundType == BackgroundType.TRANSPARENT) {
                    canvas.drawColor(Color.WHITE)
                }

                val scaleRatioX = outWidth.toFloat() / effectiveConfig.width.coerceAtLeast(1).toFloat()
                val scaleRatioY = outHeight.toFloat() / effectiveConfig.height.coerceAtLeast(1).toFloat()
                canvas.scale(scaleRatioX, scaleRatioY)

                val exportBitmapLookup: (String) -> Bitmap? = { path ->
                    ImageStore.loadBitmap(path, forExport = true)
                }

                CanvasRenderer.renderBackground(
                    canvas = canvas,
                    config = effectiveConfig,
                    bitmapLookup = exportBitmapLookup,
                    drawCheckerboardWhenTransparent = false
                )
                if (context != null) {
                    CanvasRenderer.renderLayers(
                        canvas = canvas,
                        config = effectiveConfig,
                        layers = effectiveLayers,
                        bitmapLookup = exportBitmapLookup,
                        typefaceResolver = { fontKey, isBold, isItalic ->
                            FontCatalog.resolveTypeface(context, fontKey, isBold, isItalic)
                        }
                    )
                } else {
                    CanvasRenderer.renderLayers(
                        canvas = canvas,
                        config = effectiveConfig,
                        layers = effectiveLayers,
                        bitmapLookup = exportBitmapLookup
                    )
                }
                CanvasRenderer.renderFilters(
                    canvas = canvas,
                    config = effectiveConfig
                )

                return@withContext if (
                    options.trimTransparent &&
                    options.format.supportsTransparency &&
                    effectiveConfig.backgroundType == BackgroundType.TRANSPARENT
                ) {
                    trimTransparentBounds(bitmap)
                } else {
                    bitmap
                }
            } catch (oom: OutOfMemoryError) {
                lastOom = oom
                TextGeometryCache.evictAll()
                System.gc()
            }
        }
        throw IllegalStateException("Not enough memory to export at requested resolution", lastOom)
    }
}
