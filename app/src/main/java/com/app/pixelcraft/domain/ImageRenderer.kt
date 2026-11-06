package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.LruCache
import com.app.pixelcraft.data.model.ImageAdjustments
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.PhotoFilterPreset
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Shared bitmap & photo layer renderer used by both the live editor canvas and `ImageExporter`
 * so crops, flips, shape masks, color adjustments, borders, shadows, and blend modes match 1:1.
 */
object ImageRenderer {

    private data class BlurKey(
        val bitmapIdentity: Int,
        val blurTenths: Int,
        val targetW: Int,
        val targetH: Int
    )

    private val blurCache = object : LruCache<BlurKey, Bitmap>(4) {}

    private val paintLocal = ThreadLocal.withInitial {
        Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    }

    private val borderPaintLocal = ThreadLocal.withInitial {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    }

    /** Fills a [w] x [h] area with [bitmap], centre-cropped, with optional soft blur and dim overlay. */
    fun drawCover(
        canvas: Canvas,
        bitmap: Bitmap,
        w: Float,
        h: Float,
        blurRadius: Float = 0f,
        dimAmount: Float = 0f
    ) {
        if (bitmap.width <= 0 || bitmap.height <= 0 || w <= 0f || h <= 0f) return
        val paint = paintLocal.get()!!.apply {
            reset()
            flags = Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
        }

        if (blurRadius >= 0.5f) {
            val blurred = getOrBuildBlurredCover(bitmap, w, h, blurRadius)
            canvas.drawBitmap(
                blurred,
                null,
                RectF(0f, 0f, w, h),
                paint
            )
        } else {
            val crop = ImageFit.coverCrop(bitmap.width, bitmap.height, w, h)
            canvas.drawBitmap(
                bitmap,
                Rect(crop.left, crop.top, crop.right, crop.bottom),
                RectF(0f, 0f, w, h),
                paint
            )
        }

        val clampedDim = dimAmount.coerceIn(0f, 0.90f)
        if (clampedDim > 0.005f) {
            val dimPaint = borderPaintLocal.get()!!.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = Paint.Style.FILL
                color = Color.argb((clampedDim * 255f).roundToInt().coerceIn(0, 255), 0, 0, 0)
            }
            canvas.drawRect(0f, 0f, w, h, dimPaint)
        }
    }

    private fun getOrBuildBlurredCover(bitmap: Bitmap, w: Float, h: Float, blurRadius: Float): Bitmap {
        val blurTenths = (blurRadius.coerceIn(0f, 30f) * 10f).roundToInt()
        val downscaleFactor = (2f + blurRadius * 0.75f).coerceIn(2f, 24f)
        val smallW = (w / downscaleFactor).roundToInt().coerceIn(16, 360)
        val smallH = (h / downscaleFactor).roundToInt().coerceIn(16, 360)
        val key = BlurKey(System.identityHashCode(bitmap), blurTenths, smallW, smallH)

        synchronized(blurCache) {
            blurCache.get(key)?.takeIf { !it.isRecycled }?.let { return it }
        }

        val crop = ImageFit.coverCrop(bitmap.width, bitmap.height, w, h)
        val small = Bitmap.createBitmap(smallW, smallH, Bitmap.Config.ARGB_8888)
        val smallCanvas = Canvas(small)
        val p = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        smallCanvas.drawBitmap(
            bitmap,
            Rect(crop.left, crop.top, crop.right, crop.bottom),
            RectF(0f, 0f, smallW.toFloat(), smallH.toFloat()),
            p
        )
        // Second pass at half size and back smooths blocky bilinear artifacts into a clean soft blur
        val halfW = (smallW / 2).coerceAtLeast(8)
        val halfH = (smallH / 2).coerceAtLeast(8)
        val pass1 = Bitmap.createScaledBitmap(small, halfW, halfH, true)
        val pass2 = Bitmap.createScaledBitmap(pass1, smallW, smallH, true)

        synchronized(blurCache) {
            blurCache.put(key, pass2)
        }
        return pass2
    }

    /** Draws an image layer centred on `(0, 0)` with flip, crop, mask, adjustments, shadow, and border. */
    fun drawImageLayer(canvas: Canvas, bitmap: Bitmap, layer: Layer) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        val halfW = (layer.width / 2f).coerceAtLeast(2f)
        val halfH = (layer.height / 2f).coerceAtLeast(2f)
        val dstRect = RectF(-halfW, -halfH, halfW, halfH)

        val clipMaskPath: Path? = when {
            layer.imageMaskShape != null -> ShapePathBuilder.buildPath(
                shapeType = layer.imageMaskShape,
                width = layer.width,
                height = layer.height,
                cornerRadius = layer.imageCornerRadius.takeIf { it > 0f } ?: 24f,
                flipHorizontal = false,
                flipVertical = false
            )
            layer.imageCornerRadius > 0.5f -> Path().apply {
                val r = layer.imageCornerRadius.coerceIn(0f, min(halfW, halfH))
                addRoundRect(dstRect, r, r, Path.Direction.CW)
            }
            else -> null
        }

        val alphaInt = (layer.opacity * 255f).roundToInt().coerceIn(0, 255)

        // Drop shadow pass (drawn behind clipped photo so clipPath doesn't clip away the outer shadow!)
        if (layer.shadowEnabled && alphaInt > 0) {
            val shadowColor = ColorMath.scaleAlpha(layer.shadowColor, layer.opacity)
            if ((shadowColor ushr 24) > 0) {
                val shadowPaint = borderPaintLocal.get()!!.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG
                    style = Paint.Style.FILL
                    color = Color.BLACK
                    setShadowLayer(
                        layer.shadowRadius.coerceAtLeast(0.5f),
                        layer.shadowDx,
                        layer.shadowDy,
                        shadowColor
                    )
                }
                canvas.save()
                if (layer.flipHorizontal || layer.flipVertical) {
                    canvas.scale(
                        if (layer.flipHorizontal) -1f else 1f,
                        if (layer.flipVertical) -1f else 1f
                    )
                }
                if (clipMaskPath != null) {
                    canvas.drawPath(clipMaskPath, shadowPaint)
                } else {
                    canvas.drawRect(dstRect, shadowPaint)
                }
                canvas.restore()
            }
        }

        canvas.save()
        if (layer.flipHorizontal || layer.flipVertical) {
            canvas.scale(
                if (layer.flipHorizontal) -1f else 1f,
                if (layer.flipVertical) -1f else 1f
            )
        }
        if (clipMaskPath != null) {
            canvas.clipPath(clipMaskPath)
        }

        // Source crop rectangle (normalized 0..1)
        val cLeft = layer.cropLeft.coerceIn(0f, 0.95f)
        val cTop = layer.cropTop.coerceIn(0f, 0.95f)
        val cRight = layer.cropRight.coerceIn(cLeft + 0.05f, 1f)
        val cBottom = layer.cropBottom.coerceIn(cTop + 0.05f, 1f)
        val srcRect = Rect(
            (cLeft * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1),
            (cTop * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1),
            (cRight * bitmap.width).roundToInt().coerceIn(1, bitmap.width),
            (cBottom * bitmap.height).roundToInt().coerceIn(1, bitmap.height)
        )

        val paint = paintLocal.get()!!.apply {
            reset()
            flags = Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG
            alpha = alphaInt
            colorFilter = buildColorFilter(layer.imageAdjustments)
            xfermode = resolveXfermode(layer.blendMode)
        }

        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        canvas.restore()

        // Optional photo border frame
        if (layer.imageBorderWidth > 0.5f) {
            val borderArgb = ColorMath.scaleAlpha(layer.imageBorderColor, layer.opacity)
            if ((borderArgb ushr 24) > 0) {
                canvas.save()
                if (layer.flipHorizontal || layer.flipVertical) {
                    canvas.scale(
                        if (layer.flipHorizontal) -1f else 1f,
                        if (layer.flipVertical) -1f else 1f
                    )
                }
                val bPaint = borderPaintLocal.get()!!.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG
                    style = Paint.Style.STROKE
                    strokeWidth = layer.imageBorderWidth
                    color = borderArgb
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                }
                if (clipMaskPath != null) {
                    canvas.drawPath(clipMaskPath, bPaint)
                } else {
                    canvas.drawRect(dstRect, bPaint)
                }
                canvas.restore()
            }
        }
    }

    fun resolveXfermode(blendMode: LayerBlendMode): PorterDuffXfermode? = when (blendMode) {
        LayerBlendMode.NORMAL -> null
        LayerBlendMode.MULTIPLY -> PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
        LayerBlendMode.SCREEN -> PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        LayerBlendMode.OVERLAY -> PorterDuffXfermode(PorterDuff.Mode.OVERLAY)
        LayerBlendMode.DARKEN -> PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        LayerBlendMode.LIGHTEN -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
        LayerBlendMode.PLUS -> PorterDuffXfermode(PorterDuff.Mode.ADD)
    }

    fun buildColorFilter(adj: ImageAdjustments): ColorMatrixColorFilter? {
        if (adj.isDefault) return null

        val combined = ColorMatrix()

        // 1. Saturation
        if (adj.saturation != 1f) {
            val satMatrix = ColorMatrix().apply {
                setSaturation(adj.saturation.coerceIn(0f, 2.5f))
            }
            combined.postConcat(satMatrix)
        }

        // 2. Contrast & Brightness
        val c = adj.contrast.coerceIn(0.3f, 2.2f)
        val b = adj.brightness.coerceIn(-0.6f, 0.6f) * 255f
        val translate = (1f - c) * 127.5f + b
        if (c != 1f || b != 0f) {
            val cbMatrix = ColorMatrix(
                floatArrayOf(
                    c, 0f, 0f, 0f, translate,
                    0f, c, 0f, 0f, translate,
                    0f, 0f, c, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            combined.postConcat(cbMatrix)
        }

        // 3. Warmth
        val w = adj.warmth.coerceIn(-0.5f, 0.5f)
        if (w != 0f) {
            val rScale = 1f + w * 0.25f
            val bScale = 1f - w * 0.25f
            val warmMatrix = ColorMatrix(
                floatArrayOf(
                    rScale, 0f, 0f, 0f, w * 14f,
                    0f, 1f, 0f, 0f, w * 4f,
                    0f, 0f, bScale, 0f, -w * 14f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            combined.postConcat(warmMatrix)
        }

        // 4. Filter Preset
        val presetMatrix: ColorMatrix? = when (adj.filterPreset) {
            PhotoFilterPreset.NONE -> null
            PhotoFilterPreset.BW -> ColorMatrix().apply {
                setSaturation(0f)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.12f, 0f, 0f, 0f, -12f,
                            0f, 1.12f, 0f, 0f, -12f,
                            0f, 0f, 1.12f, 0f, -12f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            PhotoFilterPreset.VINTAGE -> ColorMatrix(
                floatArrayOf(
                    0.85f, 0.22f, 0.08f, 0f, 14f,
                    0.08f, 0.86f, 0.08f, 0f, 8f,
                    0.05f, 0.14f, 0.68f, 0f, 4f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.COOL -> ColorMatrix(
                floatArrayOf(
                    0.92f, 0f, 0f, 0f, -6f,
                    0f, 1.02f, 0f, 0f, 2f,
                    0f, 0f, 1.16f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.WARM -> ColorMatrix(
                floatArrayOf(
                    1.14f, 0f, 0f, 0f, 10f,
                    0f, 1.03f, 0f, 0f, 4f,
                    0f, 0f, 0.86f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            PhotoFilterPreset.VIVID -> ColorMatrix().apply {
                setSaturation(1.38f)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.10f, 0f, 0f, 0f, -10f,
                            0f, 1.10f, 0f, 0f, -10f,
                            0f, 0f, 1.10f, 0f, -10f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            PhotoFilterPreset.NOIR -> ColorMatrix().apply {
                setSaturation(0f)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            1.36f, 0f, 0f, 0f, -34f,
                            0f, 1.36f, 0f, 0f, -34f,
                            0f, 0f, 1.36f, 0f, -34f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
            PhotoFilterPreset.FADE -> ColorMatrix().apply {
                setSaturation(0.84f)
                postConcat(
                    ColorMatrix(
                        floatArrayOf(
                            0.86f, 0f, 0f, 0f, 24f,
                            0f, 0.86f, 0f, 0f, 24f,
                            0f, 0f, 0.88f, 0f, 26f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                )
            }
        }
        if (presetMatrix != null) {
            combined.postConcat(presetMatrix)
        }

        return ColorMatrixColorFilter(combined)
    }
}
