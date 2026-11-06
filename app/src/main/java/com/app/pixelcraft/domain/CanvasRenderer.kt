package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.StrokeCapStyle
import com.app.pixelcraft.data.model.StrokeJoinStyle
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.TextAlignment
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared `android.graphics.Canvas` renderer used by both `PixelCraftCanvas` (live editor preview)
 * and `ImageExporter` (high-resolution bitmap export), operating in virtual canvas coordinates
 * (`0..config.width` x `0..config.height`) so preview and export match pixel-for-pixel.
 */
object CanvasRenderer {

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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (config.backgroundType) {
            BackgroundType.SOLID_COLOR -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.GRADIENT -> {
                val colors = config.gradientColors.map { it.toInt() }.toIntArray()
                if (colors.size >= 2) {
                    val angleRad = (config.gradientAngle * PI / 180f).toFloat()
                    val x1 = w / 2f - (cos(angleRad) * w / 2f)
                    val y1 = h / 2f - (sin(angleRad) * h / 2f)
                    val x2 = w / 2f + (cos(angleRad) * w / 2f)
                    val y2 = h / 2f + (sin(angleRad) * h / 2f)
                    paint.shader = LinearGradient(x1, y1, x2, y2, colors, null, Shader.TileMode.CLAMP)
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
                    val lightPaint = Paint().apply { color = 0xFF333333.toInt() }
                    val darkPaint = Paint().apply { color = 0xFF222222.toInt() }
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val left = c * cell
                            val top = r * cell
                            canvas.drawRect(
                                left,
                                top,
                                (left + cell).coerceAtMost(w),
                                (top + cell).coerceAtMost(h),
                                if ((r + c) % 2 == 0) lightPaint else darkPaint
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
                    ?.let { ImageRenderer.drawCover(canvas, it, w, h) }
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

        val sortedLayers = layers.filter { it.isVisible }.sortedBy { it.zIndex }
        for (layer in sortedLayers) {
            if (layer.opacity <= 0f) continue

            canvas.save()
            canvas.translate(canvasCenterX + layer.positionX, canvasCenterY + layer.positionY)
            canvas.rotate(layer.rotation)

            when (layer.type) {
                LayerType.TEXT -> renderTextLayer(canvas, layer, typefaceResolver)
                LayerType.SHAPE -> renderShapeLayer(canvas, layer)
                LayerType.IMAGE -> layer.imageLocalPath
                    ?.let(bitmapLookup)
                    ?.let { ImageRenderer.drawImageLayer(canvas, it, layer) }
            }

            canvas.restore()
        }
    }

    fun renderFilters(canvas: Canvas, config: CanvasConfig) {
        val w = config.width.toFloat()
        val h = config.height.toFloat()
        val filters = config.filters

        if (filters.vignetteEnabled) {
            val cx = w / 2f
            val cy = h / 2f
            val radius = (cx.coerceAtLeast(cy)) * 1.25f
            val colors = intArrayOf(Color.TRANSPARENT, filters.vignetteColor.toInt())
            val stops = floatArrayOf(1f - filters.vignetteIntensity.coerceIn(0.1f, 0.9f), 1f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, w, h, paint)
        }

        if (filters.stripesEnabled) {
            val spacing = filters.stripesSpacing.coerceAtLeast(2f)
            val stripePaint = Paint().apply {
                color = Color.argb(45, 0, 0, 0)
                strokeWidth = 2f
            }
            var y = 0f
            while (y < h) {
                canvas.drawLine(0f, y, w, y, stripePaint)
                y += spacing
            }
        }
    }

    private fun renderTextLayer(
        canvas: Canvas,
        layer: Layer,
        typefaceResolver: (String, Boolean, Boolean) -> Typeface
    ) {
        val resolvedTypeface = typefaceResolver(layer.fontKey, layer.isBold, layer.isItalic)
        val metricsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize
            textAlign = Paint.Align.LEFT
            typeface = resolvedTypeface
            isFakeBoldText = layer.isBold && !resolvedTypeface.isBold
            textSkewX = if (layer.isItalic && !resolvedTypeface.isItalic) -0.20f else 0f
        }

        val lines = layer.text.split("\n")
        val fontMetrics = metricsPaint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * layer.lineHeightMultiplier
        val totalTextHeight = lines.size * lineHeight
        val lineWidths = lines.map { metricsPaint.measureText(it) }
        val maxLineWidth = lineWidths.maxOrNull() ?: 0f
        val startY = -totalTextHeight / 2f + lineHeight / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f

        val textPath = buildTextContourPath(
            lines = lines,
            lineWidths = lineWidths,
            maxLineWidth = maxLineWidth,
            startY = startY,
            lineHeight = lineHeight,
            align = layer.textAlign,
            isUnderline = layer.isUnderline,
            fontSize = layer.fontSize,
            paint = metricsPaint
        )

        // Optional Text Background pill sized to actual measured text width
        if (layer.textBackgroundColor != null) {
            val bgArgb = ColorMath.scaleAlpha(layer.textBackgroundColor, layer.opacity)
            if ((bgArgb ushr 24) > 0) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgArgb
                }
                val outerStroke = when {
                    layer.strokeWidth <= 0f -> 0f
                    layer.strokePosition == StrokePosition.OUTSIDE -> layer.strokeWidth
                    layer.strokePosition == StrokePosition.CENTER -> layer.strokeWidth / 2f
                    else -> 0f
                }
                val halfW = (maxLineWidth / 2f) + outerStroke + layer.textBackgroundPadding
                val halfH = (totalTextHeight / 2f) + outerStroke + layer.textBackgroundPadding
                val bgRect = RectF(-halfW, -halfH, halfW, halfH)
                canvas.drawRoundRect(bgRect, layer.textBackgroundRadius, layer.textBackgroundRadius, bgPaint)
            }
        }

        // Mirror Reflection
        if (layer.reflectionEnabled) {
            canvas.save()
            canvas.scale(1f, -1f)
            canvas.translate(0f, -(totalTextHeight + layer.reflectionDistance * 4f))
            val reflectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = ColorMath.scaleAlpha(layer.textColor, layer.opacity * layer.reflectionOpacity)
            }
            canvas.drawPath(textPath, reflectPaint)
            canvas.restore()
        }

        // 3D Floor Shadow
        if (layer.shadow3DEnabled) {
            val shadow3DPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ColorMath.scaleAlpha(layer.shadow3DColor, layer.shadow3DOpacity * layer.opacity)
            }
            val floorHalfW = (maxLineWidth.coerceAtLeast(60f) / 2f) * layer.shadow3DScale
            val floorY = totalTextHeight / 2f + 16f
            val shadowRect = RectF(-floorHalfW, floorY, floorHalfW, floorY + 14f)
            canvas.drawOval(shadowRect, shadow3DPaint)
        }

        // 3D Extrusion
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
            val extrudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
            for (i in steps downTo 1) {
                canvas.save()
                canvas.translate(i * stepDx, i * stepDy)
                canvas.drawPath(textPath, extrudePaint)
                canvas.restore()
            }
        }

        val fillArgb = ColorMath.scaleAlpha(layer.textColor, layer.opacity)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillArgb
        }

        drawVectorPathWithIvyStroke(
            canvas = canvas,
            path = textPath,
            fillPaint = fillPaint,
            hasFill = (fillArgb ushr 24) > 0,
            isFillFullyOpaque = (fillArgb ushr 24) == 255,
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

    private fun buildTextContourPath(
        lines: List<String>,
        lineWidths: List<Float>,
        maxLineWidth: Float,
        startY: Float,
        lineHeight: Float,
        align: TextAlignment,
        isUnderline: Boolean,
        fontSize: Float,
        paint: Paint
    ): Path {
        val combined = Path()
        val linePath = Path()
        for ((idx, line) in lines.withIndex()) {
            if (line.isEmpty()) continue
            val lw = lineWidths[idx]
            val lineLeftX = when (align) {
                TextAlignment.LEFT -> -maxLineWidth / 2f
                TextAlignment.CENTER -> -lw / 2f
                TextAlignment.RIGHT -> maxLineWidth / 2f - lw
            }
            val baselineY = startY + (idx * lineHeight)
            linePath.reset()
            paint.getTextPath(line, 0, line.length, lineLeftX, baselineY, linePath)
            combined.addPath(linePath)

            if (isUnderline && lw > 0f) {
                val underlineOffset = fontSize * 0.08f
                val underlineThickness = (fontSize * 0.065f).coerceAtLeast(2f)
                val underlinePath = Path().apply {
                    addRect(
                        RectF(
                            lineLeftX,
                            baselineY + underlineOffset,
                            lineLeftX + lw,
                            baselineY + underlineOffset + underlineThickness
                        ),
                        Path.Direction.CW
                    )
                }
                combined.op(underlinePath, Path.Op.UNION)
            }
        }
        return combined
    }

    private fun renderShapeLayer(canvas: Canvas, layer: Layer) {
        val shapePath = ShapePathBuilder.buildPath(layer)
        val halfW = (layer.width / 2f).coerceAtLeast(1f)
        val halfH = (layer.height / 2f).coerceAtLeast(1f)

        val startArgb = ColorMath.scaleAlpha(layer.shapeFillColor, layer.opacity)
        val endArgb = ColorMath.scaleAlpha(layer.shapeGradientEndColor, layer.opacity)
        val hasFill: Boolean
        val isFillFullyOpaque: Boolean
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

        drawVectorPathWithIvyStroke(
            canvas = canvas,
            path = shapePath,
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
     * Unified Ivy Draw-style vector path renderer supporting:
     * - Position: `OUTSIDE` (`clipOutPath` + 2x width), `CENTER` (1x width), `INSIDE` (`clipPath` + 2x width)
     * - Dash Patterns: `SOLID`, `DASHED`, `DOTTED`, `DASH_DOT` with customizable length, gap, and phase
     * - Line Join (`ROUND`, `MITER` + miter limit, `BEVEL`) & Line Cap (`ROUND`, `BUTT`, `SQUARE`)
     * - Clean Drop Shadow integration across opaque, semi-transparent, and hollow (`None`) fills
     */
    private fun drawVectorPathWithIvyStroke(
        canvas: Canvas,
        path: Path,
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
            val shadowCaster = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

        // 2. Ivy Draw Vector Stroke Pass
        if (hasStroke) {
            val paintStrokeWidth = when (strokePosition) {
                StrokePosition.OUTSIDE, StrokePosition.INSIDE -> strokeWidthPx * 2f
                StrokePosition.CENTER -> strokeWidthPx
            }
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
