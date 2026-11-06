package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.data.presets.StickerLibrary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared `android.graphics.Canvas` renderer used by both `PixelCraftCanvas` (live editor preview)
 * and `ImageExporter` (high-resolution bitmap export), operating in virtual canvas coordinates
 * (`0..config.width` x `0..config.height`) so preview and export match pixel-for-pixel.
 */
object CanvasRenderer {

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
        bitmapLookup: (String) -> Bitmap?
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
                LayerType.TEXT -> renderTextLayer(canvas, layer)
                LayerType.SHAPE -> renderShapeLayer(canvas, layer)
                LayerType.STICKER -> renderStickerLayer(canvas, layer)
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

    private fun renderTextLayer(canvas: Canvas, layer: Layer) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize
            color = ColorMath.scaleAlpha(layer.textColor, layer.opacity)
            textAlign = when (layer.textAlign) {
                TextAlignment.LEFT -> Paint.Align.LEFT
                TextAlignment.CENTER -> Paint.Align.CENTER
                TextAlignment.RIGHT -> Paint.Align.RIGHT
            }
            isUnderlineText = layer.isUnderline
            typeface = when {
                layer.isBold && layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                layer.isBold -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.DEFAULT
            }
        }

        val lines = layer.text.split("\n")
        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * layer.lineHeightMultiplier
        val totalTextHeight = lines.size * lineHeight
        val maxLineWidth = lines.maxOfOrNull { paint.measureText(it) } ?: 0f

        val textX = when (layer.textAlign) {
            TextAlignment.LEFT -> -maxLineWidth / 2f
            TextAlignment.CENTER -> 0f
            TextAlignment.RIGHT -> maxLineWidth / 2f
        }
        val startY = -totalTextHeight / 2f + lineHeight / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f

        // Optional Text Background pill sized to actual measured text width
        if (layer.textBackgroundColor != null) {
            val bgArgb = ColorMath.scaleAlpha(layer.textBackgroundColor, layer.opacity)
            if ((bgArgb ushr 24) > 0) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = bgArgb
                }
                val halfW = (maxLineWidth / 2f) + layer.textBackgroundPadding
                val halfH = (totalTextHeight / 2f) + layer.textBackgroundPadding
                val bgRect = RectF(-halfW, -halfH, halfW, halfH)
                canvas.drawRoundRect(bgRect, layer.textBackgroundRadius, layer.textBackgroundRadius, bgPaint)
            }
        }

        // Mirror Reflection
        if (layer.reflectionEnabled) {
            canvas.save()
            canvas.scale(1f, -1f)
            canvas.translate(0f, -(totalTextHeight + layer.reflectionDistance * 4f))
            val reflectPaint = Paint(paint).apply {
                color = ColorMath.scaleAlpha(layer.textColor, layer.opacity * layer.reflectionOpacity)
                clearShadowLayer()
            }
            drawTextLines(canvas, lines, reflectPaint, textX, startY, lineHeight)
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
            val extrudePaint = Paint(paint).apply {
                color = ColorMath.darkenArgb(layer.textColor, layer.darken3D, layer.opacity)
                clearShadowLayer()
            }
            val steps = layer.depth3D.toInt().coerceIn(1, 35)
            val angleRad = (layer.angle3D * PI / 180f).toFloat()
            val stepDx = cos(angleRad)
            val stepDy = sin(angleRad)
            for (i in steps downTo 1) {
                canvas.save()
                canvas.translate(i * stepDx, i * stepDy)
                drawTextLines(canvas, lines, extrudePaint, textX, startY, lineHeight)
                canvas.restore()
            }
        }

        // Drop Shadow
        if (layer.shadowEnabled) {
            paint.setShadowLayer(
                layer.shadowRadius.coerceAtLeast(0.5f),
                layer.shadowDx,
                layer.shadowDy,
                ColorMath.scaleAlpha(layer.shadowColor, layer.opacity)
            )
        }

        // Main Text Fill
        drawTextLines(canvas, lines, paint, textX, startY, lineHeight)

        // Stroke Outline
        if (layer.strokeWidth > 0f) {
            val strokePaint = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                color = ColorMath.scaleAlpha(layer.strokeColor, layer.opacity)
                clearShadowLayer()
            }
            drawTextLines(canvas, lines, strokePaint, textX, startY, lineHeight)
        }
    }

    private fun drawTextLines(
        canvas: Canvas,
        lines: List<String>,
        paint: Paint,
        x: Float,
        startY: Float,
        lineHeight: Float
    ) {
        for ((idx, line) in lines.withIndex()) {
            canvas.drawText(line, x, startY + (idx * lineHeight), paint)
        }
    }

    private fun renderShapeLayer(canvas: Canvas, layer: Layer) {
        val shapePath = createShapePath(layer)
        val fillArgb = ColorMath.scaleAlpha(layer.shapeFillColor, layer.opacity)
        val hasFill = (fillArgb ushr 24) > 0
        val shadowArgb = if (layer.shadowEnabled) ColorMath.scaleAlpha(layer.shadowColor, layer.opacity) else 0

        if (hasFill) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillArgb
                if (layer.shadowEnabled) {
                    setShadowLayer(layer.shadowRadius.coerceAtLeast(0.5f), layer.shadowDx, layer.shadowDy, shadowArgb)
                }
            }
            canvas.drawPath(shapePath, fillPaint)
        }

        if (layer.shapeStrokeWidth > 0f) {
            val strokeArgb = ColorMath.scaleAlpha(layer.shapeStrokeColor, layer.opacity)
            if ((strokeArgb ushr 24) > 0) {
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = layer.shapeStrokeWidth
                    strokeJoin = Paint.Join.ROUND
                    strokeCap = Paint.Cap.ROUND
                    color = strokeArgb
                    // Apply shadow directly to stroke when fill is transparent (e.g., Neon Glow border)
                    if (layer.shadowEnabled && !hasFill) {
                        setShadowLayer(layer.shadowRadius.coerceAtLeast(0.5f), layer.shadowDx, layer.shadowDy, shadowArgb)
                    }
                }
                canvas.drawPath(shapePath, strokePaint)
            }
        }
    }

    private fun renderStickerLayer(canvas: Canvas, layer: Layer) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize.coerceAtLeast(36f)
            textAlign = Paint.Align.CENTER
            color = ColorMath.scaleAlpha(0xFFFFFFFF, layer.opacity)
            if (layer.shadowEnabled) {
                setShadowLayer(
                    layer.shadowRadius.coerceAtLeast(0.5f),
                    layer.shadowDx,
                    layer.shadowDy,
                    ColorMath.scaleAlpha(layer.shadowColor, layer.opacity)
                )
            }
        }
        val fm = paint.fontMetrics
        val baselineY = -(fm.ascent + fm.descent) / 2f
        val stickerIcon = StickerLibrary.stickers.find { it.id == layer.stickerKey }?.emojiOrIcon ?: "⭐"
        canvas.drawText(stickerIcon, 0f, baselineY, paint)
    }

    private fun createShapePath(layer: Layer): Path {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f
        val rect = RectF(-halfW, -halfH, halfW, halfH)

        return when (layer.shapeType) {
            ShapeType.RECTANGLE -> Path().apply {
                addRect(rect, Path.Direction.CW)
            }
            ShapeType.ROUNDED_RECTANGLE -> Path().apply {
                addRoundRect(rect, layer.shapeCornerRadius, layer.shapeCornerRadius, Path.Direction.CW)
            }
            ShapeType.CIRCLE -> Path().apply {
                addOval(rect, Path.Direction.CW)
            }
            ShapeType.TRIANGLE -> Path().apply {
                moveTo(0f, -halfH)
                lineTo(halfW, halfH)
                lineTo(-halfW, halfH)
                close()
            }
            ShapeType.STAR -> createStarPath(halfW, halfH, 5)
            ShapeType.HEART -> createHeartPath(halfW, halfH)
            ShapeType.HEXAGON -> createPolygonPath(halfW, halfH, 6)
            ShapeType.SPEECH_BUBBLE -> {
                val maxTailH = (layer.height * 0.35f).coerceAtLeast(6f)
                val tailH = (layer.height * 0.16f).coerceIn(6f, maxTailH).coerceAtMost(36f)
                val bubbleRect = RectF(-halfW, -halfH, halfW, halfH - tailH)
                val body = Path().apply {
                    addRoundRect(bubbleRect, layer.shapeCornerRadius, layer.shapeCornerRadius, Path.Direction.CW)
                }
                val tail = Path().apply {
                    moveTo(-halfW * 0.25f, halfH - tailH - 2f)
                    lineTo(-halfW * 0.45f, halfH)
                    lineTo(halfW * 0.05f, halfH - tailH - 2f)
                    close()
                }
                body.op(tail, Path.Op.UNION)
                body
            }
            ShapeType.ARROW -> Path().apply {
                moveTo(-halfW, -halfH * 0.4f)
                lineTo(0f, -halfH * 0.4f)
                lineTo(0f, -halfH)
                lineTo(halfW, 0f)
                lineTo(0f, halfH)
                lineTo(0f, halfH * 0.4f)
                lineTo(-halfW, halfH * 0.4f)
                close()
            }
        }
    }

    private fun createStarPath(halfW: Float, halfH: Float, points: Int): Path {
        val path = Path()
        val outerRadius = halfW.coerceAtMost(halfH)
        val innerRadius = outerRadius * 0.45f
        val step = PI / points

        for (i in 0 until 2 * points) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * step - (PI / 2.0)
            val x = (r * cos(angle)).toFloat()
            val y = (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun createHeartPath(halfW: Float, halfH: Float): Path {
        return Path().apply {
            moveTo(0f, halfH * 0.7f)
            cubicTo(-halfW * 1.2f, -halfH * 0.2f, -halfW * 0.8f, -halfH, 0f, -halfH * 0.35f)
            cubicTo(halfW * 0.8f, -halfH, halfW * 1.2f, -halfH * 0.2f, 0f, halfH * 0.7f)
            close()
        }
    }

    private fun createPolygonPath(halfW: Float, halfH: Float, sides: Int): Path {
        val path = Path()
        val radius = halfW.coerceAtMost(halfH)
        val step = (2.0 * PI / sides)
        for (i in 0 until sides) {
            val angle = i * step - (PI / 2.0)
            val x = (radius * cos(angle)).toFloat()
            val y = (radius * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }
}
