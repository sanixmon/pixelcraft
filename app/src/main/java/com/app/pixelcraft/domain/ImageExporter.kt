package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
    val customWidth: Int? = null,
    val customHeight: Int? = null,
    val jpegQuality: Int = 95
)

class ImageExporter {

    suspend fun exportCanvasToBitmap(
        canvasConfig: CanvasConfig,
        layers: List<Layer>,
        options: ExportOptions
    ): Bitmap = withContext(Dispatchers.Default) {
        val baseWidth = options.customWidth ?: canvasConfig.width
        val baseHeight = options.customHeight ?: canvasConfig.height
        val scale = options.qualityPreset.multiplier

        val outWidth = (baseWidth * scale).toInt().coerceAtLeast(100)
        val outHeight = (baseHeight * scale).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Scale canvas to match target export resolution
        val scaleRatioX = outWidth.toFloat() / canvasConfig.width.toFloat()
        val scaleRatioY = outHeight.toFloat() / canvasConfig.height.toFloat()
        canvas.scale(scaleRatioX, scaleRatioY)

        // 1. Draw Background
        renderBackground(canvas, canvasConfig)

        // 2. Draw Layers in order of zIndex
        val sortedLayers = layers.filter { it.isVisible }.sortedBy { it.zIndex }
        for (layer in sortedLayers) {
            canvas.save()

            val canvasCenterX = canvasConfig.width / 2f
            val canvasCenterY = canvasConfig.height / 2f
            val layerCenterX = canvasCenterX + layer.positionX
            val layerCenterY = canvasCenterY + layer.positionY

            // Transform matrix
            canvas.translate(layerCenterX, layerCenterY)
            canvas.rotate(layer.rotation)
            canvas.scale(layer.scaleX, layer.scaleY)

            when (layer.type) {
                LayerType.TEXT -> renderTextLayer(canvas, layer)
                LayerType.SHAPE -> renderShapeLayer(canvas, layer)
                LayerType.STICKER -> renderStickerLayer(canvas, layer)
                LayerType.DRAWING -> renderDrawingLayer(canvas, layer)
                LayerType.IMAGE -> layer.imageLocalPath
                    ?.let { ImageStore.loadBitmap(it) }
                    ?.let { ImageRenderer.drawImageLayer(canvas, it, layer) }
            }

            canvas.restore()
        }

        // 3. Post-processing filters (Vignette, Noise, Stripes)
        renderFilters(canvas, canvasConfig, outWidth, outHeight)

        bitmap
    }

    private fun renderBackground(canvas: Canvas, config: CanvasConfig) {
        val w = config.width.toFloat()
        val h = config.height.toFloat()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (config.backgroundType) {
            BackgroundType.SOLID_COLOR -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.GRADIENT -> {
                val angleRad = (config.gradientAngle * PI / 180f).toFloat()
                val x1 = w / 2f - (cos(angleRad) * w / 2f)
                val y1 = h / 2f - (sin(angleRad) * h / 2f)
                val x2 = w / 2f + (cos(angleRad) * w / 2f)
                val y2 = h / 2f + (sin(angleRad) * h / 2f)
                val colors = config.gradientColors.map { it.toInt() }.toIntArray()
                paint.shader = LinearGradient(x1, y1, x2, y2, colors, null, Shader.TileMode.CLAMP)
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.TRANSPARENT -> {
                // Keep transparent bitmap for PNG
            }
            BackgroundType.PRESET_WALLPAPER -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
            }
            BackgroundType.CUSTOM_IMAGE -> {
                paint.color = config.backgroundColor.toInt()
                canvas.drawRect(0f, 0f, w, h, paint)
                config.customImageUri
                    ?.let { ImageStore.loadBitmap(it) }
                    ?.let { ImageRenderer.drawCover(canvas, it, w, h) }
            }
        }
    }

    private fun renderTextLayer(canvas: Canvas, layer: Layer) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize
            color = layer.textColor.toInt()
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
            textAlign = when (layer.textAlign) {
                TextAlignment.LEFT -> Paint.Align.LEFT
                TextAlignment.CENTER -> Paint.Align.CENTER
                TextAlignment.RIGHT -> Paint.Align.RIGHT
            }
            isUnderlineText = layer.isUnderline
            typeface = if (layer.isBold && layer.isItalic) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            } else if (layer.isBold) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else if (layer.isItalic) {
                Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            } else {
                Typeface.DEFAULT
            }
        }

        val lines = layer.text.split("\n")
        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * layer.lineHeightMultiplier
        val totalTextHeight = lines.size * lineHeight

        // Optional Text Background
        if (layer.textBackgroundColor != null) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.textBackgroundColor.toInt()
            }
            val halfW = (layer.width / 2f) + layer.textBackgroundPadding
            val halfH = (totalTextHeight / 2f) + layer.textBackgroundPadding
            val bgRect = RectF(-halfW, -halfH, halfW, halfH)
            canvas.drawRoundRect(bgRect, layer.textBackgroundRadius, layer.textBackgroundRadius, bgPaint)
        }

        // Reflection Effect (Mirror below)
        if (layer.reflectionEnabled) {
            canvas.save()
            canvas.scale(1f, -1f)
            canvas.translate(0f, -(totalTextHeight + layer.reflectionDistance * 4f))
            val reflectPaint = Paint(paint).apply {
                alpha = ((paint.alpha / 255f) * layer.reflectionOpacity * 255).toInt()
            }
            drawTextLines(canvas, lines, reflectPaint, lineHeight, totalTextHeight)
            canvas.restore()
        }

        // 3D Floor Shadow
        if (layer.shadow3DEnabled) {
            val shadow3DPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.shadow3DColor.toInt()
                alpha = (layer.shadow3DOpacity * 255).toInt()
            }
            val floorY = totalTextHeight / 2f + 16f
            val shadowRect = RectF(
                -layer.width / 2f * layer.shadow3DScale,
                floorY,
                layer.width / 2f * layer.shadow3DScale,
                floorY + 14f
            )
            canvas.drawOval(shadowRect, shadow3DPaint)
        }

        // 3D Extrusion
        if (layer.is3D) {
            val extrudePaint = Paint(paint).apply {
                color = darkenColor(layer.textColor.toInt(), layer.darken3D)
                clearShadowLayer()
            }
            val steps = layer.depth3D.toInt().coerceIn(1, 40)
            val angleRad = (layer.angle3D * PI / 180f).toFloat()
            val stepDx = cos(angleRad)
            val stepDy = sin(angleRad)
            for (i in steps downTo 1) {
                canvas.save()
                canvas.translate(i * stepDx, i * stepDy)
                drawTextLines(canvas, lines, extrudePaint, lineHeight, totalTextHeight)
                canvas.restore()
            }
        }

        // Drop Shadow
        if (layer.shadowEnabled) {
            paint.setShadowLayer(layer.shadowRadius, layer.shadowDx, layer.shadowDy, layer.shadowColor.toInt())
        }

        // Main Text Fill
        drawTextLines(canvas, lines, paint, lineHeight, totalTextHeight)

        // Outline / Stroke
        if (layer.strokeWidth > 0f) {
            val strokePaint = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                color = layer.strokeColor.toInt()
                clearShadowLayer()
            }
            drawTextLines(canvas, lines, strokePaint, lineHeight, totalTextHeight)
        }
    }

    private fun drawTextLines(
        canvas: Canvas,
        lines: List<String>,
        paint: Paint,
        lineHeight: Float,
        totalHeight: Float
    ) {
        val startY = -totalHeight / 2f + lineHeight * 0.75f
        for ((idx, line) in lines.withIndex()) {
            val y = startY + (idx * lineHeight)
            canvas.drawText(line, 0f, y, paint)
        }
    }

    private fun renderShapeLayer(canvas: Canvas, layer: Layer) {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f
        val rect = RectF(-halfW, -halfH, halfW, halfH)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = layer.shapeFillColor.toInt()
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
            if (layer.shadowEnabled) {
                setShadowLayer(layer.shadowRadius, layer.shadowDx, layer.shadowDy, layer.shadowColor.toInt())
            }
        }

        when (layer.shapeType) {
            ShapeType.RECTANGLE -> canvas.drawRect(rect, fillPaint)
            ShapeType.ROUNDED_RECTANGLE -> canvas.drawRoundRect(rect, layer.shapeCornerRadius, layer.shapeCornerRadius, fillPaint)
            ShapeType.CIRCLE -> canvas.drawOval(rect, fillPaint)
            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(0f, -halfH)
                    lineTo(halfW, halfH)
                    lineTo(-halfW, halfH)
                    close()
                }
                canvas.drawPath(path, fillPaint)
            }
            ShapeType.STAR -> {
                val path = createStarPath(halfW, halfH, 5)
                canvas.drawPath(path, fillPaint)
            }
            ShapeType.HEART -> {
                val path = createHeartPath(halfW, halfH)
                canvas.drawPath(path, fillPaint)
            }
            ShapeType.HEXAGON -> {
                val path = createPolygonPath(halfW, halfH, 6)
                canvas.drawPath(path, fillPaint)
            }
            ShapeType.SPEECH_BUBBLE -> {
                val bubbleRect = RectF(-halfW, -halfH, halfW, halfH - 24f)
                val path = Path().apply {
                    addRoundRect(bubbleRect, layer.shapeCornerRadius, layer.shapeCornerRadius, Path.Direction.CW)
                    moveTo(-16f, halfH - 24f)
                    lineTo(-40f, halfH)
                    lineTo(8f, halfH - 24f)
                }
                canvas.drawPath(path, fillPaint)
            }
            ShapeType.ARROW -> {
                val path = Path().apply {
                    moveTo(-halfW, -halfH * 0.4f)
                    lineTo(0f, -halfH * 0.4f)
                    lineTo(0f, -halfH)
                    lineTo(halfW, 0f)
                    lineTo(0f, halfH)
                    lineTo(0f, halfH * 0.4f)
                    lineTo(-halfW, halfH * 0.4f)
                    close()
                }
                canvas.drawPath(path, fillPaint)
            }
        }

        // Shape Stroke
        if (layer.shapeStrokeWidth > 0f) {
            val strokePaint = Paint(fillPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.shapeStrokeWidth
                color = layer.shapeStrokeColor.toInt()
                clearShadowLayer()
            }
            if (layer.shapeType == ShapeType.ROUNDED_RECTANGLE) {
                canvas.drawRoundRect(rect, layer.shapeCornerRadius, layer.shapeCornerRadius, strokePaint)
            } else if (layer.shapeType == ShapeType.RECTANGLE) {
                canvas.drawRect(rect, strokePaint)
            } else if (layer.shapeType == ShapeType.CIRCLE) {
                canvas.drawOval(rect, strokePaint)
            }
        }
    }

    private fun renderStickerLayer(canvas: Canvas, layer: Layer) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize.coerceAtLeast(36f)
            textAlign = Paint.Align.CENTER
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
            if (layer.shadowEnabled) {
                setShadowLayer(layer.shadowRadius, layer.shadowDx, layer.shadowDy, layer.shadowColor.toInt())
            }
        }

        val stickerIcon = when (layer.stickerKey) {
            "emoji_fire" -> "🔥"
            "emoji_sunglasses" -> "😎"
            "emoji_heart_eyes" -> "😍"
            "emoji_sparkles" -> "✨"
            "emoji_100" -> "💯"
            "emoji_party" -> "🎉"
            "emoji_rocket" -> "🚀"
            "emoji_crown" -> "👑"
            "badge_star" -> "⭐"
            "badge_trophy" -> "🏆"
            "badge_medal" -> "🥇"
            "badge_check" -> "✅"
            "dec_heart" -> "❤️"
            "dec_lightning" -> "⚡"
            "dec_diamond" -> "💎"
            "dec_bulb" -> "💡"
            "sale_tag" -> "🏷️"
            "sale_gift" -> "🎁"
            else -> "⭐"
        }

        canvas.drawText(stickerIcon, 0f, paint.textSize / 3f, paint)
    }

    private fun renderDrawingLayer(canvas: Canvas, layer: Layer) {
        for (drawPath in layer.drawPaths) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = drawPath.color.toInt()
                strokeWidth = drawPath.strokeWidth
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                if (drawPath.isNeon) {
                    setShadowLayer(drawPath.strokeWidth * 1.5f, 0f, 0f, drawPath.color.toInt())
                }
            }
            if (drawPath.points.size < 2) continue
            val path = Path().apply {
                moveTo(drawPath.points.first().x, drawPath.points.first().y)
                for (i in 1 until drawPath.points.size) {
                    val p = drawPath.points[i]
                    lineTo(p.x, p.y)
                }
            }
            canvas.drawPath(path, paint)
        }
    }

    private fun renderFilters(canvas: Canvas, config: CanvasConfig, w: Int, h: Int) {
        val filters = config.filters

        // Vignette
        if (filters.vignetteEnabled) {
            val cx = w / 2f
            val cy = h / 2f
            val radius = (cx.coerceAtLeast(cy)) * 1.2f
            val colors = intArrayOf(Color.TRANSPARENT, filters.vignetteColor.toInt())
            val stops = floatArrayOf(1f - filters.vignetteIntensity.coerceIn(0.1f, 0.9f), 1f)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        }

        // Scanlines / Stripes
        if (filters.stripesEnabled) {
            val stripePaint = Paint().apply {
                color = Color.argb(40, 0, 0, 0)
                strokeWidth = 2f
            }
            var y = 0f
            while (y < h) {
                canvas.drawLine(0f, y, w.toFloat(), y, stripePaint)
                y += filters.stripesSpacing
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
        val path = Path()
        val w = halfW * 2f
        val h = halfH * 2f
        path.moveTo(0f, halfH * 0.7f)
        path.cubicTo(-halfW * 1.2f, -halfH * 0.2f, -halfW * 0.8f, -halfH, 0f, -halfH * 0.35f)
        path.cubicTo(halfW * 0.8f, -halfH, halfW * 1.2f, -halfH * 0.2f, 0f, halfH * 0.7f)
        path.close()
        return path
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

    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * (1f - factor)).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * (1f - factor)).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * (1f - factor)).toInt().coerceIn(0, 255)
        return Color.argb(a, r, g, b)
    }
}
