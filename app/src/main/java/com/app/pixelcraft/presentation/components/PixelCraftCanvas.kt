package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.app.pixelcraft.domain.ImageRenderer
import com.app.pixelcraft.domain.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.data.presets.FontLibrary
import com.app.pixelcraft.domain.TransformHelper
import com.app.pixelcraft.presentation.PixelCraftUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PixelCraftCanvas(
    state: PixelCraftUiState,
    onSelectLayer: (String?) -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    onResizeLayer: (Float, Float) -> Unit,
    onRotateLayer: (Float) -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onZoomAndPan: (Float, PointF) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val canvasConfig = state.canvasConfig
    val imagePaths = remember(state.layers, canvasConfig.customImageUri) {
        buildSet {
            canvasConfig.customImageUri?.let { add(it) }
            state.layers.mapNotNullTo(this) { it.imageLocalPath }
        }
    }
    val bitmaps by produceState(emptyMap<String, Bitmap>(), imagePaths) {
        value = withContext(Dispatchers.IO) {
            imagePaths.mapNotNull { path -> ImageStore.loadBitmap(path)?.let { path to it } }.toMap()
        }
    }
    val aspect = canvasConfig.width.toFloat() / canvasConfig.height.toFloat()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181A20))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Calculate fitting scale
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        var displayWidth = maxWidthPx
        var displayHeight = maxWidthPx / aspect
        if (displayHeight > maxHeightPx) {
            displayHeight = maxHeightPx
            displayWidth = maxHeightPx * aspect
        }

        val canvasScale = displayWidth / canvasConfig.width.toFloat()
        val canvasCenter = PointF(displayWidth / 2f, displayHeight / 2f)

        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .shadow(12.dp, RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
                .clipToBounds()
                .pointerInput(state.isZoomMode) {
                    if (state.isZoomMode) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            onZoomAndPan(zoom, PointF(pan.x, pan.y))
                        }
                    } else {
                        detectTapGestures { tapOffset ->
                            // Hit test layers in reverse zIndex
                            val sorted = state.layers.filter { it.isVisible }.sortedByDescending { it.zIndex }
                            var hitId: String? = null
                            for (layer in sorted) {
                                val bounds = TransformHelper.computeLayerBounds(
                                    layer,
                                    PointF(canvasConfig.width / 2f, canvasConfig.height / 2f)
                                )
                                // Convert tap to virtual canvas coords
                                val virtualTouch = PointF(
                                    tapOffset.x / canvasScale,
                                    tapOffset.y / canvasScale
                                )
                                val hit = TransformHelper.hitTestHandle(virtualTouch, bounds)
                                if (hit != com.app.pixelcraft.domain.HandleType.NONE) {
                                    hitId = layer.id
                                    break
                                }
                            }
                            onSelectLayer(hitId)
                        }
                    }
                }
                .pointerInput(state.selectedLayerId, state.isZoomMode) {
                    if (!state.isZoomMode && state.selectedLayer != null) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta(dragAmount.x / canvasScale, dragAmount.y / canvasScale)
                            },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragEnd() }
                        )
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Canvas Background
                drawBackground(canvasConfig, size, canvasConfig.customImageUri?.let { bitmaps[it] })

                // 2. Scale coordinate space to virtual canvas dimensions
                scale(scale = canvasScale, pivot = Offset.Zero) {
                    val virtualCenter = Offset(canvasConfig.width / 2f, canvasConfig.height / 2f)

                    // 3. Render all layers by zIndex
                    val sortedLayers = state.layers.filter { it.isVisible }.sortedBy { it.zIndex }
                    for (layer in sortedLayers) {
                        val layerCenter = Offset(
                            virtualCenter.x + layer.positionX,
                            virtualCenter.y + layer.positionY
                        )

                        translate(left = layerCenter.x, top = layerCenter.y) {
                            rotate(degrees = layer.rotation, pivot = Offset.Zero) {
                                scale(scaleX = layer.scaleX, scaleY = layer.scaleY, pivot = Offset.Zero) {
                                    when (layer.type) {
                                        LayerType.TEXT -> drawComposeTextLayer(this, layer)
                                        LayerType.SHAPE -> drawComposeShapeLayer(this, layer)
                                        LayerType.STICKER -> drawComposeStickerLayer(this, layer)
                                        LayerType.DRAWING -> drawComposeDrawingLayer(this, layer)
                                        LayerType.IMAGE -> layer.imageLocalPath?.let { bitmaps[it] }?.let {
                                            ImageRenderer.drawImageLayer(drawContext.canvas.nativeCanvas, it, layer)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Draw Selected Layer Outline
                    val active = state.selectedLayer
                    if (active != null && !state.isZoomMode) {
                        val activeCenter = Offset(
                            virtualCenter.x + active.positionX,
                            virtualCenter.y + active.positionY
                        )
                        translate(left = activeCenter.x, top = activeCenter.y) {
                            rotate(degrees = active.rotation, pivot = Offset.Zero) {
                                val halfW = (active.width * active.scaleX) / 2f
                                val halfH = (active.height * active.scaleY) / 2f
                                drawRect(
                                    color = Color(0xFF00E5FF),
                                    topLeft = Offset(-halfW, -halfH),
                                    size = Size(halfW * 2f, halfH * 2f),
                                    style = Stroke(
                                        width = 3f / canvasScale,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                    )
                                )

                                // Corner resize handles
                                val pinRadius = 14f / canvasScale
                                drawCircle(Color(0xFF00E5FF), radius = pinRadius, center = Offset(-halfW, -halfH))
                                drawCircle(Color(0xFF00E5FF), radius = pinRadius, center = Offset(halfW, -halfH))
                                drawCircle(Color(0xFF00E5FF), radius = pinRadius, center = Offset(-halfW, halfH))
                                drawCircle(Color(0xFF00E5FF), radius = pinRadius, center = Offset(halfW, halfH))

                                // Top rotation pin stem
                                drawLine(
                                    color = Color(0xFF00E5FF),
                                    start = Offset(0f, -halfH),
                                    end = Offset(0f, -halfH - 40f / canvasScale),
                                    strokeWidth = 2f / canvasScale
                                )
                                drawCircle(Color(0xFF00E5FF), radius = pinRadius * 1.2f, center = Offset(0f, -halfH - 40f / canvasScale))
                            }
                        }
                    }

                    // 5. Draw Magnetic Snapping Guides
                    if (state.showVerticalGuide) {
                        drawLine(
                            color = Color(0xFFFFEB3B),
                            start = Offset(virtualCenter.x, 0f),
                            end = Offset(virtualCenter.x, canvasConfig.height.toFloat()),
                            strokeWidth = 2.5f / canvasScale
                        )
                    }
                    if (state.showHorizontalGuide) {
                        drawLine(
                            color = Color(0xFFFFEB3B),
                            start = Offset(0f, virtualCenter.y),
                            end = Offset(canvasConfig.width.toFloat(), virtualCenter.y),
                            strokeWidth = 2.5f / canvasScale
                        )
                    }
                }

                // 6. Draw Grid Overlay if enabled
                if (state.isGridEnabled) {
                    drawGridOverlay(state, size)
                }

                // 7. Post-Processing Filters Preview (Vignette, Scanlines)
                drawFilterPreviews(canvasConfig, size)
            }
        }
    }
}

private fun DrawScope.drawBackground(config: CanvasConfig, size: Size, image: Bitmap?) {
    when (config.backgroundType) {
        BackgroundType.SOLID_COLOR -> {
            drawRect(Color(config.backgroundColor))
        }
        BackgroundType.GRADIENT -> {
            val angleRad = (config.gradientAngle * PI / 180f).toFloat()
            val x1 = size.width / 2f - (cos(angleRad) * size.width / 2f)
            val y1 = size.height / 2f - (sin(angleRad) * size.height / 2f)
            val x2 = size.width / 2f + (cos(angleRad) * size.width / 2f)
            val y2 = size.height / 2f + (sin(angleRad) * size.height / 2f)
            val brush = Brush.linearGradient(
                colors = config.gradientColors.map { Color(it) },
                start = Offset(x1, y1),
                end = Offset(x2, y2)
            )
            drawRect(brush)
        }
        BackgroundType.TRANSPARENT -> {
            // Draw checkerboard pattern
            val checkSize = 24f
            val cols = (size.width / checkSize).toInt() + 1
            val rows = (size.height / checkSize).toInt() + 1
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val color = if ((r + c) % 2 == 0) Color(0xFF333333) else Color(0xFF222222)
                    drawRect(
                        color = color,
                        topLeft = Offset(c * checkSize, r * checkSize),
                        size = Size(checkSize, checkSize)
                    )
                }
            }
        }
        BackgroundType.PRESET_WALLPAPER -> {
            drawRect(Color(config.backgroundColor))
        }
        BackgroundType.CUSTOM_IMAGE -> {
            drawRect(Color(config.backgroundColor))
            image?.let { ImageRenderer.drawCover(drawContext.canvas.nativeCanvas, it, size.width, size.height) }
        }
    }
}

private fun DrawScope.drawGridOverlay(state: PixelCraftUiState, size: Size) {
    val cols = state.gridColumns + 1
    val rows = state.gridRows + 1
    val colStep = size.width / cols
    val rowStep = size.height / rows

    val gridColor = Color(0x6600E5FF)
    val stroke = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))

    for (c in 1 until cols) {
        val x = c * colStep
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
    }
    for (r in 1 until rows) {
        val y = r * rowStep
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
    }
}

private fun DrawScope.drawFilterPreviews(config: CanvasConfig, size: Size) {
    val filters = config.filters
    if (filters.vignetteEnabled) {
        val radius = (size.width.coerceAtLeast(size.height)) * 0.7f
        val brush = Brush.radialGradient(
            0.0f to Color.Transparent,
            1.0f - filters.vignetteIntensity.coerceIn(0.1f, 0.9f) to Color.Transparent,
            1.0f to Color(filters.vignetteColor),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = radius
        )
        drawRect(brush)
    }

    if (filters.stripesEnabled) {
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color(0x33000000),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )
            y += filters.stripesSpacing
        }
    }
}

private fun drawComposeTextLayer(drawScope: DrawScope, layer: Layer) {
    drawScope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize
            color = layer.textColor.toInt()
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
            textAlign = when (layer.textAlign) {
                TextAlignment.LEFT -> android.graphics.Paint.Align.LEFT
                TextAlignment.CENTER -> android.graphics.Paint.Align.CENTER
                TextAlignment.RIGHT -> android.graphics.Paint.Align.RIGHT
            }
            isUnderlineText = layer.isUnderline
            typeface = if (layer.isBold && layer.isItalic) {
                android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
            } else if (layer.isBold) {
                android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            } else if (layer.isItalic) {
                android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
            } else {
                android.graphics.Typeface.DEFAULT
            }
        }

        val lines = layer.text.split("\n")
        val fontMetrics = paint.fontMetrics
        val lineHeight = (fontMetrics.descent - fontMetrics.ascent) * layer.lineHeightMultiplier
        val totalTextHeight = lines.size * lineHeight

        // Text Background
        if (layer.textBackgroundColor != null) {
            val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.textBackgroundColor.toInt()
            }
            val halfW = (layer.width / 2f) + layer.textBackgroundPadding
            val halfH = (totalTextHeight / 2f) + layer.textBackgroundPadding
            val bgRect = android.graphics.RectF(-halfW, -halfH, halfW, halfH)
            drawRoundRect(bgRect, layer.textBackgroundRadius, layer.textBackgroundRadius, bgPaint)
        }

        // Reflection
        if (layer.reflectionEnabled) {
            save()
            scale(1f, -1f)
            translate(0f, -(totalTextHeight + layer.reflectionDistance * 4f))
            val reflectPaint = android.graphics.Paint(paint).apply {
                alpha = ((paint.alpha / 255f) * layer.reflectionOpacity * 255).toInt()
            }
            val startY = -totalTextHeight / 2f + lineHeight * 0.75f
            for ((idx, line) in lines.withIndex()) {
                val y = startY + (idx * lineHeight)
                drawText(line, 0f, y, reflectPaint)
            }
            restore()
        }

        // 3D Floor Shadow
        if (layer.shadow3DEnabled) {
            val shadow3DPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.shadow3DColor.toInt()
                alpha = (layer.shadow3DOpacity * 255).toInt()
            }
            val floorY = totalTextHeight / 2f + 16f
            val shadowRect = android.graphics.RectF(
                -layer.width / 2f * layer.shadow3DScale,
                floorY,
                layer.width / 2f * layer.shadow3DScale,
                floorY + 14f
            )
            drawOval(shadowRect, shadow3DPaint)
        }

        // 3D Extrusion
        if (layer.is3D) {
            val extrudePaint = android.graphics.Paint(paint).apply {
                val a = android.graphics.Color.alpha(layer.textColor.toInt())
                val r = (android.graphics.Color.red(layer.textColor.toInt()) * (1f - layer.darken3D)).toInt().coerceIn(0, 255)
                val g = (android.graphics.Color.green(layer.textColor.toInt()) * (1f - layer.darken3D)).toInt().coerceIn(0, 255)
                val b = (android.graphics.Color.blue(layer.textColor.toInt()) * (1f - layer.darken3D)).toInt().coerceIn(0, 255)
                color = android.graphics.Color.argb(a, r, g, b)
                clearShadowLayer()
            }
            val steps = layer.depth3D.toInt().coerceIn(1, 30)
            val angleRad = (layer.angle3D * PI / 180f).toFloat()
            val stepDx = cos(angleRad)
            val stepDy = sin(angleRad)
            val startY = -totalTextHeight / 2f + lineHeight * 0.75f
            for (i in steps downTo 1) {
                save()
                translate(i * stepDx, i * stepDy)
                for ((idx, line) in lines.withIndex()) {
                    val y = startY + (idx * lineHeight)
                    drawText(line, 0f, y, extrudePaint)
                }
                restore()
            }
        }

        // Drop Shadow
        if (layer.shadowEnabled) {
            paint.setShadowLayer(layer.shadowRadius, layer.shadowDx, layer.shadowDy, layer.shadowColor.toInt())
        }

        // Main Text Lines
        val startY = -totalTextHeight / 2f + lineHeight * 0.75f
        for ((idx, line) in lines.withIndex()) {
            val y = startY + (idx * lineHeight)
            drawText(line, 0f, y, paint)
        }

        // Stroke Outline
        if (layer.strokeWidth > 0f) {
            val strokePaint = android.graphics.Paint(paint).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                color = layer.strokeColor.toInt()
                clearShadowLayer()
            }
            for ((idx, line) in lines.withIndex()) {
                val y = startY + (idx * lineHeight)
                drawText(line, 0f, y, strokePaint)
            }
        }
    }
}

private fun drawComposeShapeLayer(drawScope: DrawScope, layer: Layer) {
    val halfW = layer.width / 2f
    val halfH = layer.height / 2f
    val rectOffset = Offset(-halfW, -halfH)
    val rectSize = Size(layer.width, layer.height)
    val fillColor = Color(layer.shapeFillColor).copy(alpha = layer.opacity)

    when (layer.shapeType) {
        ShapeType.RECTANGLE -> {
            drawScope.drawRect(color = fillColor, topLeft = rectOffset, size = rectSize)
        }
        ShapeType.ROUNDED_RECTANGLE -> {
            drawScope.drawRoundRect(
                color = fillColor,
                topLeft = rectOffset,
                size = rectSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.shapeCornerRadius)
            )
        }
        ShapeType.CIRCLE -> {
            drawScope.drawOval(color = fillColor, topLeft = rectOffset, size = rectSize)
        }
        ShapeType.TRIANGLE -> {
            val path = Path().apply {
                moveTo(0f, -halfH)
                lineTo(halfW, halfH)
                lineTo(-halfW, halfH)
                close()
            }
            drawScope.drawPath(path, color = fillColor)
        }
        ShapeType.STAR -> {
            val path = Path()
            val outerRadius = halfW.coerceAtMost(halfH)
            val innerRadius = outerRadius * 0.45f
            val step = PI / 5.0
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerRadius else innerRadius
                val angle = i * step - (PI / 2.0)
                val x = (r * cos(angle)).toFloat()
                val y = (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawScope.drawPath(path, color = fillColor)
        }
        ShapeType.HEART -> {
            val path = Path().apply {
                moveTo(0f, halfH * 0.7f)
                cubicTo(-halfW * 1.2f, -halfH * 0.2f, -halfW * 0.8f, -halfH, 0f, -halfH * 0.35f)
                cubicTo(halfW * 0.8f, -halfH, halfW * 1.2f, -halfH * 0.2f, 0f, halfH * 0.7f)
                close()
            }
            drawScope.drawPath(path, color = fillColor)
        }
        ShapeType.HEXAGON -> {
            val path = Path()
            val radius = halfW.coerceAtMost(halfH)
            val step = (2.0 * PI / 6.0)
            for (i in 0 until 6) {
                val angle = i * step - (PI / 2.0)
                val x = (radius * cos(angle)).toFloat()
                val y = (radius * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawScope.drawPath(path, color = fillColor)
        }
        ShapeType.SPEECH_BUBBLE -> {
            val path = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(
                            -halfW, -halfH, halfW, halfH - 24f
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.shapeCornerRadius)
                    )
                )
                moveTo(-16f, halfH - 24f)
                lineTo(-40f, halfH)
                lineTo(8f, halfH - 24f)
            }
            drawScope.drawPath(path, color = fillColor)
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
            drawScope.drawPath(path, color = fillColor)
        }
    }

    // Stroke Outline
    if (layer.shapeStrokeWidth > 0f) {
        val strokeColor = Color(layer.shapeStrokeColor)
        val strokeStyle = Stroke(width = layer.shapeStrokeWidth)
        if (layer.shapeType == ShapeType.ROUNDED_RECTANGLE) {
            drawScope.drawRoundRect(
                color = strokeColor,
                topLeft = rectOffset,
                size = rectSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(layer.shapeCornerRadius),
                style = strokeStyle
            )
        } else if (layer.shapeType == ShapeType.RECTANGLE) {
            drawScope.drawRect(color = strokeColor, topLeft = rectOffset, size = rectSize, style = strokeStyle)
        } else if (layer.shapeType == ShapeType.CIRCLE) {
            drawScope.drawOval(color = strokeColor, topLeft = rectOffset, size = rectSize, style = strokeStyle)
        }
    }
}

private fun drawComposeStickerLayer(drawScope: DrawScope, layer: Layer) {
    drawScope.drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textSize = layer.fontSize.coerceAtLeast(36f)
            textAlign = android.graphics.Paint.Align.CENTER
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
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
        drawText(stickerIcon, 0f, paint.textSize / 3f, paint)
    }
}

private fun drawComposeDrawingLayer(drawScope: DrawScope, layer: Layer) {
    for (drawPath in layer.drawPaths) {
        if (drawPath.points.size < 2) continue
        val path = Path().apply {
            moveTo(drawPath.points.first().x, drawPath.points.first().y)
            for (i in 1 until drawPath.points.size) {
                val p = drawPath.points[i]
                lineTo(p.x, p.y)
            }
        }
        drawScope.drawPath(
            path = path,
            color = Color(drawPath.color),
            style = Stroke(
                width = drawPath.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
