package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.StrokeCapStyle
import com.app.pixelcraft.data.model.StrokeJoinStyle
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Shared `android.graphics.Canvas` renderer used by `PixelCraftCanvas` (live editor preview),
 * `ProjectStorage` thumbnails, and `ImageExporter` (high-resolution export), operating in virtual
 * canvas coordinates (`0..config.width` x `0..config.height`) so preview and export match 1:1.
 *
 * Performance architecture (§ 4.1–4.5):
 * - Reuses `ThreadLocal<RenderPaints>` across frames (zero `Paint()` churn in draw loop).
 * - Fetches pre-built glyph contours & exact bounds from [TextGeometryCache].
 * - Batches 3D extrusion steps into a single `Path` drawn once.
 */
object CanvasRenderer {

    private class RenderPaints {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val lightCheckerPaint = Paint().apply { color = 0xFF333333.toInt() }
        val darkCheckerPaint = Paint().apply { color = 0xFF222222.toInt() }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val shadowCasterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val auxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val layerCompositePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val extrudePath = Path()
        val shaderMatrix = Matrix()
    }

    private val paintsLocal = ThreadLocal.withInitial { RenderPaints() }

    @Volatile
    private var cachedNoiseTile: Bitmap? = null

    private fun defaultTypefaceResolver(fontKey: String, isBold: Boolean, isItalic: Boolean): Typeface {
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        return Typeface.create(Typeface.DEFAULT, style)
    }

    fun renderBackground(
        canvas: Canvas,
        config: CanvasConfig,
        bitmapLookup: (String) -> Bitmap?,
        drawCheckerboardWhenTransparent: Boolean,
        checkerCellPx: Float = 24f
    ) {
        val w = config.width.toFloat()
        val h = config.height.toFloat()
        val paints = paintsLocal.get()!!
        val paint = paints.bgPaint.apply {
            reset()
            flags = Paint.ANTI_ALIAS_FLAG
        }

        when (config.backgroundType) {
            BackgroundType.SOLID_COLOR -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.GRADIENT -> {
                val colors = config.gradientColors.map { it.toInt() }.toIntArray()
                if (colors.size >= 2) {
                    val cx = w / 2f
                    val cy = h / 2f
                    paint.shader = when (config.gradientType) {
                        GradientType.LINEAR -> {
                            val angleRad = (config.gradientAngle * PI / 180f).toFloat()
                            val x1 = cx - (cos(angleRad) * cx)
                            val y1 = cy - (sin(angleRad) * cy)
                            val x2 = cx + (cos(angleRad) * cx)
                            val y2 = cy + (sin(angleRad) * cy)
                            LinearGradient(x1, y1, x2, y2, colors, null, Shader.TileMode.CLAMP)
                        }
                        GradientType.RADIAL -> {
                            val radius = hypot(cx, cy).coerceAtLeast(16f)
                            RadialGradient(cx, cy, radius, colors, null, Shader.TileMode.CLAMP)
                        }
                        GradientType.SWEEP -> {
                            val sweepColors = if (colors.first() != colors.last()) {
                                colors + colors.first()
                            } else {
                                colors
                            }
                            SweepGradient(cx, cy, sweepColors, null).also { sweep ->
                                paints.shaderMatrix.reset()
                                paints.shaderMatrix.postRotate(config.gradientAngle, cx, cy)
                                sweep.setLocalMatrix(paints.shaderMatrix)
                            }
                        }
                    }
                } else {
                    paint.color = colors.firstOrNull() ?: config.backgroundColor.toInt()
                }
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.TRANSPARENT -> {
                if (drawCheckerboardWhenTransparent) {
                    val cell = checkerCellPx.coerceAtLeast(4f)
                    val cols = (w / cell).toInt() + 1
                    val rows = (h / cell).toInt() + 1
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val left = c * cell
                            val top = r * cell
                            canvas.drawRect(
                                left,
                                top,
                                (left + cell).coerceAtMost(w),
                                (top + cell).coerceAtMost(h),
                                if ((r + c) % 2 == 0) paints.lightCheckerPaint else paints.darkCheckerPaint
                            )
                        }
                    }
                }
            }
            BackgroundType.CUSTOM_IMAGE -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
                config.customImageUri
                    ?.let(bitmapLookup)
                    ?.let {
                        ImageRenderer.drawCover(
                            canvas = canvas,
                            bitmap = it,
                            w = w,
                            h = h,
                            blurRadius = config.backgroundBlur,
                            dimAmount = config.backgroundDim
                        )
                    }
            }
        }
    }

    fun renderLayers(
        canvas: Canvas,
        config: CanvasConfig,
        layers: List<Layer>,
        bitmapLookup: (String) -> Bitmap?,
        typefaceResolver: (String, Boolean, Boolean) -> Typeface = ::defaultTypefaceResolver
    ) {
        val canvasCenterX = config.width / 2f
        val canvasCenterY = config.height / 2f
        val paints = paintsLocal.get()!!

        val sortedLayers = layers.filter { it.isVisible }.sortedBy { it.zIndex }
        for (layer in sortedLayers) {
            if (layer.opacity <= 0f) continue

            val needsBlendLayer = layer.blendMode != LayerBlendMode.NORMAL && layer.type != LayerType.IMAGE
            val layerSaveCount = if (needsBlendLayer) {
                paints.layerCompositePaint.reset()
                paints.layerCompositePaint.flags = Paint.ANTI_ALIAS_FLAG
                paints.layerCompositePaint.xfermode = ImageRenderer.resolveXfermode(layer.blendMode)
                canvas.saveLayer(null, paints.layerCompositePaint)
            } else {
                canvas.save()
            }

            canvas.translate(canvasCenterX + layer.positionX, canvasCenterY + layer.positionY)
            canvas.rotate(layer.rotation)

            when (layer.type) {
                LayerType.TEXT -> {
                    if (layer.flipHorizontal || layer.flipVertical) {
                        canvas.scale(
                            if (layer.flipHorizontal) -1f else 1f,
                            if (layer.flipVertical) -1f else 1f
                        )
                    }
                    renderTextLayer(canvas, layer, typefaceResolver, paints)
                }
                LayerType.SHAPE -> renderShapeLayer(canvas, layer, paints)
                LayerType.IMAGE -> layer.imageLocalPath
                    ?.let(bitmapLookup)
                    ?.let { ImageRenderer.drawImageLayer(canvas, it, layer) }
            }

            canvas.restoreToCount(layerSaveCount)
        }
    }

    fun renderFilters(canvas: Canvas, config: CanvasConfig) {
        val w = config.width.toFloat()
        val h = config.height.toFloat()
        val filters = config.filters
        val paints = paintsLocal.get()!!

        if (filters.vignetteEnabled) {
            val cx = w / 2f
            val cy = h / 2f
            val radius = (cx.coerceAtLeast(cy)) * 1.25f
            val colors = intArrayOf(Color.TRANSPARENT, filters.vignetteColor.toInt())
            val stops = floatArrayOf(1f - filters.vignetteIntensity.coerceIn(0.1f, 0.9f), 1f)
            val paint = paints.auxPaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                shader = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, w, h, paint)
        }

        if (filters.stripesEnabled) {
            val spacing = filters.stripesSpacing.coerceAtLeast(2f)
            val stripePaint = paints.auxPaint.apply {
                reset()
                color = Color.argb(45, 0, 0, 0)
                strokeWidth = 2f
            }
            var y = 0f
            while (y < h) {
                canvas.drawLine(0f, y, w, y, stripePaint)
                y += spacing
            }
        }

        if (filters.grainEnabled && filters.grainIntensity > 0.01f) {
            val tile = getOrBuildNoiseTile()
            val alpha = (filters.grainIntensity.coerceIn(0f, 1f) * 90f).roundToInt().coerceIn(0, 140)
            if (alpha > 0) {
                val grainPaint = paints.auxPaint.apply {
                    reset()
                    flags = Paint.FILTER_BITMAP_FLAG
                    this.alpha = alpha
                    shader = android.graphics.BitmapShader(tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                }
                canvas.drawRect(0f, 0f, w, h, grainPaint)
            }
        }
    }

    /** Renders a downscaled thumbnail bitmap (e.g. 256px max side) for `ProjectStorage` or Eyedropper sampling. */
    fun renderThumbnail(
        config: CanvasConfig,
        layers: List<Layer>,
        bitmapLookup: (String) -> Bitmap?,
        typefaceResolver: (String, Boolean, Boolean) -> Typeface = ::defaultTypefaceResolver,
        maxSide: Int = 256
    ): Bitmap {
        val safeW = config.width.coerceAtLeast(64)
        val safeH = config.height.coerceAtLeast(64)
        val longest = max(safeW, safeH)
        val ratio = (maxSide.toFloat() / longest.toFloat()).coerceAtMost(1f)
        val outW = (safeW * ratio).roundToInt().coerceAtLeast(32)
        val outH = (safeH * ratio).roundToInt().coerceAtLeast(32)

        val bmp = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.scale(outW.toFloat() / safeW.toFloat(), outH.toFloat() / safeH.toFloat())
        renderBackground(canvas, config, bitmapLookup, drawCheckerboardWhenTransparent = true)
        renderLayers(canvas, config, layers, bitmapLookup, typefaceResolver)
        renderFilters(canvas, config)
        return bmp
    }

    private fun getOrBuildNoiseTile(): Bitmap {
        cachedNoiseTile?.takeIf { !it.isRecycled }?.let { return it }
        val size = 128
        val pixels = IntArray(size * size)
        val rng = Random(0x50495845L) // Deterministic seed so preview and export match
        for (i in pixels.indices) {
            val luma = rng.nextInt(0, 256)
            val alpha = rng.nextInt(90, 220)
            pixels[i] = Color.argb(alpha, luma, luma, luma)
        }
        val bmp = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        cachedNoiseTile = bmp
        return bmp
    }

    private fun renderTextLayer(
        canvas: Canvas,
        layer: Layer,
        typefaceResolver: (String, Boolean, Boolean) -> Typeface,
        paints: RenderPaints
    ) {
        val resolvedTypeface = typefaceResolver(layer.fontKey, layer.isBold, layer.isItalic)
        val geometry = TextGeometryCache.getOrBuild(layer, resolvedTypeface)
        val textPath = geometry.path
        val maxLineWidth = geometry.maxLineWidth
        val totalTextHeight = geometry.totalHeight
        val halfSpanW = (maxLineWidth / 2f).coerceAtLeast(4f)
        val halfSpanH = (totalTextHeight / 2f).coerceAtLeast(4f)

        // Optional Text Background pill sized to exact measured text bounds
        if (layer.textBackgroundColor != null) {
            val bgArgb = ColorMath.scaleAlpha(layer.textBackgroundColor, layer.opacity)
            if ((bgArgb ushr 24) > 0) {
                val bgPaint = paints.auxPaint.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG
                    style = Paint.Style.FILL
                    color = bgArgb
                }
                val outerStroke = when {
                    layer.strokeWidth <= 0f -> 0f
                    layer.strokePosition == StrokePosition.OUTSIDE -> layer.strokeWidth
                    layer.strokePosition == StrokePosition.CENTER -> layer.strokeWidth / 2f
                    else -> 0f
                }
                val halfW = halfSpanW + outerStroke + layer.textBackgroundPadding
                val halfH = halfSpanH + outerStroke + layer.textBackgroundPadding
                val bgRect = RectF(-halfW, -halfH, halfW, halfH)
                canvas.drawRoundRect(bgRect, layer.textBackgroundRadius, layer.textBackgroundRadius, bgPaint)
            }
        }

        // Mirror Reflection
        if (layer.reflectionEnabled) {
            canvas.save()
            canvas.scale(1f, -1f)
            canvas.translate(0f, -(totalTextHeight + layer.reflectionDistance * 4f))
            val reflectPaint = paints.auxPaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = Paint.Style.FILL
                color = ColorMath.scaleAlpha(layer.textColor, layer.opacity * layer.reflectionOpacity)
            }
            canvas.drawPath(textPath, reflectPaint)
            canvas.restore()
        }

        // 3D Floor Shadow
        if (layer.shadow3DEnabled) {
            val shadow3DPaint = paints.auxPaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = Paint.Style.FILL
                color = ColorMath.scaleAlpha(layer.shadow3DColor, layer.shadow3DOpacity * layer.opacity)
            }
            val floorHalfW = (maxLineWidth.coerceAtLeast(60f) / 2f) * layer.shadow3DScale
            val floorY = totalTextHeight / 2f + 16f
            val shadowRect = RectF(-floorHalfW, floorY, floorHalfW, floorY + 14f)
            canvas.drawOval(shadowRect, shadow3DPaint)
        }

        // Outer Glow (Neon halo behind glyph & stroke)
        if (layer.glowEnabled && layer.glowRadius > 0.5f) {
            val glowArgb = ColorMath.scaleAlpha(layer.glowColor, layer.opacity)
            if ((glowArgb ushr 24) > 0) {
                val glowExpand = when {
                    layer.strokeWidth <= 0f -> 2f
                    layer.strokePosition == StrokePosition.OUTSIDE -> layer.strokeWidth * 2f
                    layer.strokePosition == StrokePosition.CENTER -> layer.strokeWidth
                    else -> 2f
                }
                val glowPaint = paints.glowPaint.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG
                    style = Paint.Style.FILL_AND_STROKE
                    strokeWidth = glowExpand
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    color = glowArgb
                    maskFilter = BlurMaskFilter(layer.glowRadius.coerceIn(1f, 80f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawPath(textPath, glowPaint)
            }
        }

        // Batched single-Path 3D Extrusion (§ 4.5 — 1 drawPath instead of up to 35 save/translate/drawPath)
        if (layer.is3D) {
            val outerExpand = when {
                layer.strokeWidth <= 0f -> 0f
                layer.strokePosition == StrokePosition.OUTSIDE -> layer.strokeWidth * 2f
                layer.strokePosition == StrokePosition.CENTER -> layer.strokeWidth
                else -> 0f
            }
            val extrudeBaseColor = if (layer.strokeWidth > 0f && layer.strokePosition != StrokePosition.INSIDE) {
                layer.strokeColor
            } else {
                layer.textColor
            }
            val extrudePaint = paints.auxPaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = if (outerExpand > 0f) Paint.Style.FILL_AND_STROKE else Paint.Style.FILL
                strokeWidth = outerExpand
                strokeJoin = resolveJoin(layer.strokeJoin)
                strokeCap = resolveCap(layer.strokeCap)
                strokeMiter = layer.strokeMiterLimit.coerceIn(1f, 20f)
                color = ColorMath.darkenArgb(extrudeBaseColor, layer.darken3D, layer.opacity)
            }
            val steps = layer.depth3D.toInt().coerceIn(1, 35)
            val angleRad = (layer.angle3D * PI / 180f).toFloat()
            val stepDx = cos(angleRad)
            val stepDy = sin(angleRad)
            val batchedExtrude = paints.extrudePath.apply { reset() }
            for (i in steps downTo 1) {
                batchedExtrude.addPath(textPath, i * stepDx, i * stepDy)
            }
            canvas.drawPath(batchedExtrude, extrudePaint)
        }

        val startArgb = ColorMath.scaleAlpha(layer.textColor, layer.opacity)
        val endArgb = ColorMath.scaleAlpha(layer.textGradientEndColor, layer.opacity)
        val hasFill: Boolean
        val isFillFullyOpaque: Boolean
        val fillPaint = paints.fillPaint.apply {
            reset()
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
        }

        if (layer.textGradientEnabled) {
            hasFill = ((startArgb ushr 24) > 0) || ((endArgb ushr 24) > 0)
            isFillFullyOpaque = ((startArgb ushr 24) == 255) && ((endArgb ushr 24) == 255)
            fillPaint.shader = if (layer.textGradientRadial) {
                val r = hypot(halfSpanW, halfSpanH).coerceAtLeast(8f)
                RadialGradient(0f, 0f, r, startArgb, endArgb, Shader.TileMode.CLAMP)
            } else {
                val angleRad = (layer.textGradientAngle * PI / 180f).toFloat()
                val x1 = -cos(angleRad) * halfSpanW
                val y1 = -sin(angleRad) * halfSpanH
                val x2 = cos(angleRad) * halfSpanW
                val y2 = sin(angleRad) * halfSpanH
                LinearGradient(x1, y1, x2, y2, startArgb, endArgb, Shader.TileMode.CLAMP)
            }
        } else {
            hasFill = (startArgb ushr 24) > 0
            isFillFullyOpaque = (startArgb ushr 24) == 255
            fillPaint.color = startArgb
        }

        drawVectorPathWithStroke(
            canvas = canvas,
            path = textPath,
            paints = paints,
            fillPaint = fillPaint,
            hasFill = hasFill,
            isFillFullyOpaque = isFillFullyOpaque,
            strokeWidthPx = layer.strokeWidth,
            strokeArgb = ColorMath.scaleAlpha(layer.strokeColor, layer.opacity),
            strokePosition = layer.strokePosition,
            strokeJoin = layer.strokeJoin,
            strokeCap = layer.strokeCap,
            strokePattern = layer.strokePattern,
            dashLength = layer.strokeDashLength,
            dashGap = layer.strokeDashGap,
            dashPhase = layer.strokePhase,
            miterLimit = layer.strokeMiterLimit,
            shadowEnabled = layer.shadowEnabled,
            shadowArgb = if (layer.shadowEnabled) ColorMath.scaleAlpha(layer.shadowColor, layer.opacity) else 0,
            shadowRadius = layer.shadowRadius,
            shadowDx = layer.shadowDx,
            shadowDy = layer.shadowDy
        )
    }

    private fun renderShapeLayer(canvas: Canvas, layer: Layer, paints: RenderPaints) {
        val shapePath = ShapePathBuilder.buildPath(layer)
        val halfW = (layer.width / 2f).coerceAtLeast(1f)
        val halfH = (layer.height / 2f).coerceAtLeast(1f)

        if (layer.glowEnabled && layer.glowRadius > 0.5f) {
            val glowArgb = ColorMath.scaleAlpha(layer.glowColor, layer.opacity)
            if ((glowArgb ushr 24) > 0) {
                val glowPaint = paints.glowPaint.apply {
                    reset()
                    flags = Paint.ANTI_ALIAS_FLAG
                    style = Paint.Style.FILL_AND_STROKE
                    strokeWidth = (layer.shapeStrokeWidth * 2f).coerceAtLeast(2f)
                    color = glowArgb
                    maskFilter = BlurMaskFilter(layer.glowRadius.coerceIn(1f, 80f), BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawPath(shapePath, glowPaint)
            }
        }

        val startArgb = ColorMath.scaleAlpha(layer.shapeFillColor, layer.opacity)
        val endArgb = ColorMath.scaleAlpha(layer.shapeGradientEndColor, layer.opacity)
        val hasFill: Boolean
        val isFillFullyOpaque: Boolean
        val fillPaint = paints.fillPaint.apply {
            reset()
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
        }

        if (layer.shapeGradientEnabled) {
            hasFill = ((startArgb ushr 24) > 0) || ((endArgb ushr 24) > 0)
            isFillFullyOpaque = ((startArgb ushr 24) == 255) && ((endArgb ushr 24) == 255)
            val angleRad = (layer.shapeGradientAngle * PI / 180f).toFloat()
            val x1 = -cos(angleRad) * halfW
            val y1 = -sin(angleRad) * halfH
            val x2 = cos(angleRad) * halfW
            val y2 = sin(angleRad) * halfH
            fillPaint.shader = LinearGradient(x1, y1, x2, y2, startArgb, endArgb, Shader.TileMode.CLAMP)
        } else {
            hasFill = (startArgb ushr 24) > 0
            isFillFullyOpaque = (startArgb ushr 24) == 255
            fillPaint.color = startArgb
        }

        drawVectorPathWithStroke(
            canvas = canvas,
            path = shapePath,
            paints = paints,
            fillPaint = fillPaint,
            hasFill = hasFill,
            isFillFullyOpaque = isFillFullyOpaque,
            strokeWidthPx = layer.shapeStrokeWidth,
            strokeArgb = ColorMath.scaleAlpha(layer.shapeStrokeColor, layer.opacity),
            strokePosition = layer.strokePosition,
            strokeJoin = layer.strokeJoin,
            strokeCap = layer.strokeCap,
            strokePattern = layer.strokePattern,
            dashLength = layer.strokeDashLength,
            dashGap = layer.strokeDashGap,
            dashPhase = layer.strokePhase,
            miterLimit = layer.strokeMiterLimit,
            shadowEnabled = layer.shadowEnabled,
            shadowArgb = if (layer.shadowEnabled) ColorMath.scaleAlpha(layer.shadowColor, layer.opacity) else 0,
            shadowRadius = layer.shadowRadius,
            shadowDx = layer.shadowDx,
            shadowDy = layer.shadowDy
        )
    }

    /**
     * Unified vector contour renderer supporting:
     * - Position: `OUTSIDE` (`clipOutPath` + 2x width), `CENTER` (1x width), `INSIDE` (`clipPath` + 2x width)
     * - Dash Patterns: `SOLID`, `DASHED`, `DOTTED`, `DASH_DOT` with customizable length, gap, and phase
     * - Line Join (`ROUND`, `MITER` + miter limit, `BEVEL`) & Line Cap (`ROUND`, `BUTT`, `SQUARE`)
     * - Clean Drop Shadow integration across opaque, semi-transparent, and hollow (`None`) fills
     */
    private fun drawVectorPathWithStroke(
        canvas: Canvas,
        path: Path,
        paints: RenderPaints,
        fillPaint: Paint,
        hasFill: Boolean,
        isFillFullyOpaque: Boolean,
        strokeWidthPx: Float,
        strokeArgb: Int,
        strokePosition: StrokePosition,
        strokeJoin: StrokeJoinStyle,
        strokeCap: StrokeCapStyle,
        strokePattern: StrokePattern,
        dashLength: Float,
        dashGap: Float,
        dashPhase: Float,
        miterLimit: Float,
        shadowEnabled: Boolean,
        shadowArgb: Int,
        shadowRadius: Float,
        shadowDx: Float,
        shadowDy: Float
    ) {
        val hasStroke = strokeWidthPx > 0f && (strokeArgb ushr 24) > 0
        val hasShadow = shadowEnabled && (shadowArgb ushr 24) > 0
        val blur = shadowRadius.coerceAtLeast(0.5f)
        val isStrokeOpaque = (strokeArgb ushr 24) == 255

        // Unified outer silhouette shadow when both fill and outer/center solid stroke are opaque
        val useSilhouetteShadowCaster = hasShadow &&
            hasFill &&
            isFillFullyOpaque &&
            hasStroke &&
            isStrokeOpaque &&
            strokePattern == StrokePattern.SOLID &&
            strokePosition != StrokePosition.INSIDE

        if (useSilhouetteShadowCaster) {
            val expandWidth = if (strokePosition == StrokePosition.OUTSIDE) strokeWidthPx * 2f else strokeWidthPx
            val shadowCaster = paints.shadowCasterPaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = Paint.Style.FILL_AND_STROKE
                strokeWidth = expandWidth
                this.strokeJoin = resolveJoin(strokeJoin)
                this.strokeCap = resolveCap(strokeCap)
                strokeMiter = miterLimit.coerceIn(1f, 20f)
                color = strokeArgb
                setShadowLayer(blur, shadowDx, shadowDy, shadowArgb)
            }
            canvas.drawPath(path, shadowCaster)
        }

        // 1. Fill Pass
        if (hasFill) {
            if (hasShadow && !useSilhouetteShadowCaster) {
                fillPaint.setShadowLayer(blur, shadowDx, shadowDy, shadowArgb)
            }
            canvas.drawPath(path, fillPaint)
        }

        // 2. Vector Stroke Pass
        if (hasStroke) {
            val paintStrokeWidth = when (strokePosition) {
                StrokePosition.OUTSIDE, StrokePosition.INSIDE -> strokeWidthPx * 2f
                StrokePosition.CENTER -> strokeWidthPx
            }
            val strokePaint = paints.strokePaint.apply {
                reset()
                flags = Paint.ANTI_ALIAS_FLAG
                style = Paint.Style.STROKE
                strokeWidth = paintStrokeWidth
                this.strokeJoin = resolveJoin(strokeJoin)
                this.strokeCap = resolveCap(strokeCap)
                strokeMiter = miterLimit.coerceIn(1f, 20f)
                color = strokeArgb
                pathEffect = resolveDashEffect(
                    pattern = strokePattern,
                    visualStrokeWidth = strokeWidthPx,
                    cap = strokeCap,
                    dashLength = dashLength,
                    dashGap = dashGap,
                    dashPhase = dashPhase
                )
                if (hasShadow && !hasFill) {
                    setShadowLayer(blur, shadowDx, shadowDy, shadowArgb)
                }
            }

            when (strokePosition) {
                StrokePosition.OUTSIDE -> {
                    canvas.save()
                    canvas.clipOutPath(path)
                    canvas.drawPath(path, strokePaint)
                    canvas.restore()
                }
                StrokePosition.INSIDE -> {
                    canvas.save()
                    canvas.clipPath(path)
                    canvas.drawPath(path, strokePaint)
                    canvas.restore()
                }
                StrokePosition.CENTER -> {
                    canvas.drawPath(path, strokePaint)
                }
            }
        }
    }

    private fun resolveJoin(join: StrokeJoinStyle): Paint.Join = when (join) {
        StrokeJoinStyle.ROUND -> Paint.Join.ROUND
        StrokeJoinStyle.MITER -> Paint.Join.MITER
        StrokeJoinStyle.BEVEL -> Paint.Join.BEVEL
    }

    private fun resolveCap(cap: StrokeCapStyle): Paint.Cap = when (cap) {
        StrokeCapStyle.ROUND -> Paint.Cap.ROUND
        StrokeCapStyle.BUTT -> Paint.Cap.BUTT
        StrokeCapStyle.SQUARE -> Paint.Cap.SQUARE
    }

    private fun resolveDashEffect(
        pattern: StrokePattern,
        visualStrokeWidth: Float,
        cap: StrokeCapStyle,
        dashLength: Float,
        dashGap: Float,
        dashPhase: Float
    ): DashPathEffect? {
        val cleanDash = dashLength.coerceAtLeast(2f)
        val cleanGap = dashGap.coerceAtLeast(2f)
        val dotSpan = if (cap == StrokeCapStyle.ROUND) 1f else visualStrokeWidth.coerceAtLeast(2f)
        val roundCapPad = if (cap != StrokeCapStyle.BUTT) visualStrokeWidth else 0f

        return when (pattern) {
            StrokePattern.SOLID -> null
            StrokePattern.DASHED -> DashPathEffect(
                floatArrayOf(cleanDash, (cleanGap + roundCapPad * 0.5f).coerceAtLeast(2f)),
                dashPhase
            )
            StrokePattern.DOTTED -> DashPathEffect(
                floatArrayOf(dotSpan, (cleanGap + roundCapPad).coerceAtLeast(4f)),
                dashPhase
            )
            StrokePattern.DASH_DOT -> DashPathEffect(
                floatArrayOf(
                    cleanDash,
                    (cleanGap + roundCapPad * 0.6f).coerceAtLeast(3f),
                    dotSpan,
                    (cleanGap + roundCapPad * 0.6f).coerceAtLeast(3f)
                ),
                dashPhase
            )
        }
    }
}
