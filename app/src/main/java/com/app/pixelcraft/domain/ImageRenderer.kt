package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlin.math.sin
import kotlin.math.cos
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
    fun drawImageLayer(canvas: Canvas, original: Bitmap, layer: Layer) {
        if (original.width <= 0 || original.height <= 0) return
        val bitmap = if (layer.imageAdjustments.hasBlur) blurredLayerBitmap(original, layer.imageAdjustments.blur) else original
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
                if (layer.isCutout && clipMaskPath == null) {
                    // Shadow cast by the cut-out's shape, not its bounding box.
                    canvas.drawBitmap(alphaMask(bitmap), cropSrcRect(layer, bitmap), dstRect, shadowPaint)
                } else if (clipMaskPath != null) {
                    canvas.drawPath(clipMaskPath, shadowPaint)
                } else {
                    canvas.drawRect(dstRect, shadowPaint)
                }
                canvas.restore()
            }
        }

        // Cut-out outline: the shape's silhouette stamped in a ring behind the image.
        val cutoutOutline = layer.isCutout && clipMaskPath == null && layer.imageBorderWidth > 0.5f
        if (cutoutOutline) {
            val borderArgb = ColorMath.scaleAlpha(layer.imageBorderColor, layer.opacity)
            if ((borderArgb ushr 24) > 0) {
                canvas.save()
                if (layer.flipHorizontal || layer.flipVertical) {
                    canvas.scale(if (layer.flipHorizontal) -1f else 1f, if (layer.flipVertical) -1f else 1f)
                }
                val mask = alphaMask(bitmap)
                val src = cropSrcRect(layer, bitmap)
                val outlinePaint = borderPaintLocal.get()!!.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG
                    color = borderArgb
                }
                val radius = layer.imageBorderWidth
                val rings = if (radius > 6f) 3 else 1
                val steps = 24
                val shifted = RectF()
                for (ring in 1..rings) {
                    val r = radius * ring / rings
                    for (i in 0 until steps) {
                        val a = (2.0 * Math.PI * i / steps)
                        val dx = (cos(a) * r).toFloat()
                        val dy = (sin(a) * r).toFloat()
                        shifted.set(dstRect.left + dx, dstRect.top + dy, dstRect.right + dx, dstRect.bottom + dy)
                        canvas.drawBitmap(mask, src, shifted, outlinePaint)
                    }
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

        // Optional photo border frame (cut-outs got their silhouette outline above)
        if (layer.imageBorderWidth > 0.5f && !cutoutOutline) {
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

    /** Standard luminance-preserving hue rotation matrix. */
    private fun hueMatrix(degrees: Float): ColorMatrix {
        val rad = Math.toRadians(degrees.toDouble())
        val c = cos(rad).toFloat()
        val sn = sin(rad).toFloat()
        val lr = 0.213f
        val lg = 0.715f
        val lb = 0.072f
        return ColorMatrix(
            floatArrayOf(
                lr + c * (1 - lr) + sn * (-lr), lg + c * (-lg) + sn * (-lg), lb + c * (-lb) + sn * (1 - lb), 0f, 0f,
                lr + c * (-lr) + sn * 0.143f, lg + c * (1 - lg) + sn * 0.140f, lb + c * (-lb) + sn * (-0.283f), 0f, 0f,
                lr + c * (-lr) + sn * (-(1 - lr)), lg + c * (-lg) + sn * lg, lb + c * (1 - lb) + sn * lb, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    private data class LayerBlurKey(val bitmapId: Int, val width: Int, val height: Int, val radiusTenths: Int)
    private val layerBlurCache = object : LruCache<LayerBlurKey, Bitmap>(6) {}

    /**
     * Soft blur of a whole picture: shrink by a factor that grows with [radius], then scale back
     * up in two smoothing passes. Cached per picture and radius so dragging stays smooth.
     */
    private fun blurredLayerBitmap(bitmap: Bitmap, radius: Float): Bitmap {
        val r = radius.coerceIn(0.5f, 25f)
        val key = LayerBlurKey(System.identityHashCode(bitmap), bitmap.width, bitmap.height, (r * 10f).roundToInt())
        synchronized(layerBlurCache) { layerBlurCache.get(key)?.takeIf { !it.isRecycled }?.let { return it } }
        val factor = (1f + r * 0.6f).coerceIn(1.5f, 16f)
        val smallW = (bitmap.width / factor).roundToInt().coerceAtLeast(4)
        val smallH = (bitmap.height / factor).roundToInt().coerceAtLeast(4)
        val small = Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)
        val smaller = Bitmap.createScaledBitmap(small, (smallW / 2).coerceAtLeast(2), (smallH / 2).coerceAtLeast(2), true)
        val back = Bitmap.createScaledBitmap(smaller, smallW, smallH, true)
        val result = Bitmap.createScaledBitmap(back, bitmap.width.coerceAtMost(2048), bitmap.height.coerceAtMost(2048), true)
        synchronized(layerBlurCache) { layerBlurCache.put(key, result) }
        return result
    }

    private val alphaMasks = java.util.WeakHashMap<Bitmap, Bitmap>()

    /** Alpha-only copy of [bitmap], cached per bitmap; drawn with a paint colour it gives the silhouette. */
    private fun alphaMask(bitmap: Bitmap): Bitmap = synchronized(alphaMasks) {
        alphaMasks.getOrPut(bitmap) { bitmap.extractAlpha() }
    }

    private fun cropSrcRect(layer: Layer, bitmap: Bitmap): Rect {
        val cLeft = layer.cropLeft.coerceIn(0f, 0.95f)
        val cTop = layer.cropTop.coerceIn(0f, 0.95f)
        val cRight = layer.cropRight.coerceIn(cLeft + 0.05f, 1f)
        val cBottom = layer.cropBottom.coerceIn(cTop + 0.05f, 1f)
        return Rect(
            (cLeft * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1),
            (cTop * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1),
            (cRight * bitmap.width).roundToInt().coerceIn(1, bitmap.width),
            (cBottom * bitmap.height).roundToInt().coerceIn(1, bitmap.height)
        )
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

        // 2b. Hue rotation
        if (adj.hue != 0f) combined.postConcat(hueMatrix(adj.hue.coerceIn(-180f, 180f)))

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

        // 3b. Tint: blend towards the tint colour, keeping the picture's light and shade.
        val ts = adj.tintStrength.coerceIn(0f, 1f)
        if (ts > 0f) {
            val tr = ((adj.tintColor ushr 16) and 0xFF).toFloat() / 255f
            val tg = ((adj.tintColor ushr 8) and 0xFF).toFloat() / 255f
            val tb = (adj.tintColor and 0xFF).toFloat() / 255f
            val k = 1f - ts
            combined.postConcat(
                ColorMatrix(
                    floatArrayOf(
                        k + ts * tr * 0.299f, ts * tr * 0.587f, ts * tr * 0.114f, 0f, 0f,
                        ts * tg * 0.299f, k + ts * tg * 0.587f, ts * tg * 0.114f, 0f, 0f,
                        ts * tb * 0.299f, ts * tb * 0.587f, k + ts * tb * 0.114f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            )
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
