package com.app.pixelcraft.domain

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.ShapeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Builds pure vector `android.graphics.Path` contours centered at `(0, 0)` for all 24 PicSay Pro
 * inspired `ShapeType` entries. Shared by `PixelCraftShapeDialog` previews, `CanvasRenderer`,
 * and `ImageExporter` so shapes and Ivy Draw vector strokes match pixel-for-pixel at any resolution.
 */
object ShapePathBuilder {

    fun buildPath(layer: Layer): Path {
        return buildPath(
            shapeType = layer.shapeType,
            width = layer.width,
            height = layer.height,
            cornerRadius = layer.shapeCornerRadius,
            flipHorizontal = layer.flipHorizontal,
            flipVertical = layer.flipVertical
        )
    }

    fun buildPath(
        shapeType: ShapeType,
        width: Float,
        height: Float,
        cornerRadius: Float = 24f,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false
    ): Path {
        val halfW = (width / 2f).coerceAtLeast(4f)
        val halfH = (height / 2f).coerceAtLeast(4f)
        val rect = RectF(-halfW, -halfH, halfW, halfH)
        val clampedRadius = cornerRadius.coerceIn(0f, min(halfW, halfH))

        val path = when (shapeType) {
            ShapeType.RECTANGLE -> Path().apply {
                addRect(rect, Path.Direction.CW)
            }

            ShapeType.ROUNDED_RECTANGLE -> Path().apply {
                addRoundRect(rect, clampedRadius, clampedRadius, Path.Direction.CW)
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

            ShapeType.DIAMOND -> Path().apply {
                moveTo(0f, -halfH)
                lineTo(halfW, 0f)
                lineTo(0f, halfH)
                lineTo(-halfW, 0f)
                close()
            }

            ShapeType.PENTAGON -> createPolygonPath(halfW, halfH, sides = 5, angleOffsetRad = -PI / 2.0)

            ShapeType.HEXAGON -> createPolygonPath(halfW, halfH, sides = 6, angleOffsetRad = -PI / 2.0)

            ShapeType.OCTAGON -> createPolygonPath(halfW, halfH, sides = 8, angleOffsetRad = -PI / 2.0 + PI / 8.0)

            ShapeType.SPEECH_ROUND -> {
                val tailH = (height * 0.20f).coerceIn(8f, halfH * 0.55f)
                val bodyBottom = halfH - tailH
                val body = Path().apply {
                    addOval(RectF(-halfW, -halfH, halfW, bodyBottom), Path.Direction.CW)
                }
                val overlap = (height * 0.08f).coerceAtLeast(4f)
                val tail = Path().apply {
                    moveTo(-halfW * 0.32f, bodyBottom - overlap)
                    lineTo(-halfW * 0.58f, halfH)
                    lineTo(halfW * 0.06f, bodyBottom - overlap)
                    close()
                }
                body.op(tail, Path.Op.UNION)
                body
            }

            ShapeType.SPEECH_BUBBLE -> {
                val tailH = (height * 0.18f).coerceIn(8f, halfH * 0.50f)
                val bodyBottom = halfH - tailH
                val bodyRect = RectF(-halfW, -halfH, halfW, bodyBottom)
                val bubbleRadius = clampedRadius.coerceAtMost(min(halfW, (bodyBottom + halfH) / 2f))
                val body = Path().apply {
                    addRoundRect(bodyRect, bubbleRadius, bubbleRadius, Path.Direction.CW)
                }
                val tail = Path().apply {
                    moveTo(-halfW * 0.30f, bodyBottom - 3f)
                    lineTo(-halfW * 0.52f, halfH)
                    lineTo(halfW * 0.04f, bodyBottom - 3f)
                    close()
                }
                body.op(tail, Path.Op.UNION)
                body
            }

            ShapeType.THOUGHT_CLOUD -> createThoughtCloudPath(halfW, halfH)

            ShapeType.SHOUT_BURST -> createShoutBurstPath(halfW, halfH)

            ShapeType.ARROW -> Path().apply {
                val shaftHalfH = halfH * 0.38f
                val headStartX = halfW * 0.12f
                moveTo(-halfW, -shaftHalfH)
                lineTo(headStartX, -shaftHalfH)
                lineTo(headStartX, -halfH)
                lineTo(halfW, 0f)
                lineTo(headStartX, halfH)
                lineTo(headStartX, shaftHalfH)
                lineTo(-halfW, shaftHalfH)
                close()
            }

            ShapeType.ARROW_DOUBLE -> Path().apply {
                val shaftHalfH = halfH * 0.36f
                val headSpanX = halfW * 0.42f
                moveTo(-halfW, 0f)
                lineTo(-headSpanX, -halfH)
                lineTo(-headSpanX, -shaftHalfH)
                lineTo(headSpanX, -shaftHalfH)
                lineTo(headSpanX, -halfH)
                lineTo(halfW, 0f)
                lineTo(headSpanX, halfH)
                lineTo(headSpanX, shaftHalfH)
                lineTo(-headSpanX, shaftHalfH)
                lineTo(-headSpanX, halfH)
                close()
            }

            ShapeType.CHEVRON -> Path().apply {
                moveTo(-halfW, -halfH)
                lineTo(halfW * 0.38f, -halfH)
                lineTo(halfW, 0f)
                lineTo(halfW * 0.38f, halfH)
                lineTo(-halfW, halfH)
                lineTo(-halfW * 0.38f, 0f)
                close()
            }

            ShapeType.LOCATION_PIN -> Path().apply {
                moveTo(0f, halfH)
                cubicTo(-halfW * 0.25f, halfH * 0.52f, -halfW, halfH * 0.05f, -halfW, -halfH * 0.28f)
                cubicTo(-halfW, -halfH * 0.72f, -halfW * 0.55f, -halfH, 0f, -halfH)
                cubicTo(halfW * 0.55f, -halfH, halfW, -halfH * 0.72f, halfW, -halfH * 0.28f)
                cubicTo(halfW, halfH * 0.05f, halfW * 0.25f, halfH * 0.52f, 0f, halfH)
                close()
            }

            ShapeType.STAR -> createStarPath(halfW, halfH, points = 5, innerRatio = 0.42f)

            ShapeType.STARBURST -> createStarPath(halfW, halfH, points = 12, innerRatio = 0.74f)

            ShapeType.HEART -> Path().apply {
                moveTo(0f, halfH * 0.85f)
                cubicTo(-halfW * 1.15f, halfH * 0.05f, -halfW * 0.95f, -halfH * 0.95f, 0f, -halfH * 0.35f)
                cubicTo(halfW * 0.95f, -halfH * 0.95f, halfW * 1.15f, halfH * 0.05f, 0f, halfH * 0.85f)
                close()
            }

            ShapeType.SHIELD -> Path().apply {
                moveTo(0f, -halfH)
                lineTo(halfW, -halfH * 0.78f)
                lineTo(halfW, -halfH * 0.05f)
                cubicTo(halfW, halfH * 0.50f, halfW * 0.45f, halfH * 0.82f, 0f, halfH)
                cubicTo(-halfW * 0.45f, halfH * 0.82f, -halfW, halfH * 0.50f, -halfW, -halfH * 0.05f)
                lineTo(-halfW, -halfH * 0.78f)
                close()
            }

            ShapeType.RIBBON_BANNER -> Path().apply {
                val notchX = halfW * 0.78f
                moveTo(-halfW, -halfH)
                lineTo(halfW, -halfH)
                lineTo(notchX, 0f)
                lineTo(halfW, halfH)
                lineTo(-halfW, halfH)
                lineTo(-notchX, 0f)
                close()
            }

            ShapeType.CROWN -> Path().apply {
                moveTo(-halfW * 0.82f, halfH)
                lineTo(-halfW, -halfH * 0.48f)
                lineTo(-halfW * 0.52f, halfH * 0.08f)
                lineTo(0f, -halfH)
                lineTo(halfW * 0.52f, halfH * 0.08f)
                lineTo(halfW, -halfH * 0.48f)
                lineTo(halfW * 0.82f, halfH)
                close()
            }

            ShapeType.LIGHTNING -> Path().apply {
                moveTo(halfW * 0.22f, -halfH)
                lineTo(-halfW * 0.88f, halfH * 0.08f)
                lineTo(-halfW * 0.08f, halfH * 0.08f)
                lineTo(-halfW * 0.28f, halfH)
                lineTo(halfW * 0.88f, -halfH * 0.08f)
                lineTo(halfW * 0.08f, -halfH * 0.08f)
                close()
            }

            ShapeType.CROSS -> Path().apply {
                val armW = halfW * 0.36f
                val armH = halfH * 0.36f
                moveTo(-armW, -halfH)
                lineTo(armW, -halfH)
                lineTo(armW, -armH)
                lineTo(halfW, -armH)
                lineTo(halfW, armH)
                lineTo(armW, armH)
                lineTo(armW, halfH)
                lineTo(-armW, halfH)
                lineTo(-armW, armH)
                lineTo(-halfW, armH)
                lineTo(-halfW, -armH)
                lineTo(-armW, -armH)
                close()
            }
        }

        if (flipHorizontal || flipVertical) {
            val matrix = Matrix().apply {
                setScale(
                    if (flipHorizontal) -1f else 1f,
                    if (flipVertical) -1f else 1f
                )
            }
            path.transform(matrix)
        }
        return path
    }

    private fun createThoughtCloudPath(halfW: Float, halfH: Float): Path {
        val cloudBottom = halfH * 0.52f
        val cloudCy = (-halfH + cloudBottom) / 2f
        val cloudHalfH = (cloudBottom + halfH) / 2f

        val cloud = Path().apply {
            addOval(
                RectF(-halfW * 0.68f, cloudCy - cloudHalfH * 0.65f, halfW * 0.68f, cloudCy + cloudHalfH * 0.68f),
                Path.Direction.CW
            )
        }

        val puffs = listOf(
            // (cxFactor, cyFactorInCloud, rxFactor, ryFactorInCloud)
            floatArrayOf(-0.56f, -0.05f, 0.42f, 0.55f),
            floatArrayOf(-0.28f, -0.45f, 0.42f, 0.52f),
            floatArrayOf(0.18f, -0.48f, 0.45f, 0.52f),
            floatArrayOf(0.56f, -0.04f, 0.42f, 0.54f),
            floatArrayOf(0.30f, 0.42f, 0.44f, 0.48f),
            floatArrayOf(-0.24f, 0.42f, 0.44f, 0.48f)
        )
        for (p in puffs) {
            val cx = halfW * p[0]
            val cy = cloudCy + cloudHalfH * p[1]
            val rx = halfW * p[2]
            val ry = cloudHalfH * p[3]
            val puff = Path().apply {
                addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
            }
            cloud.op(puff, Path.Op.UNION)
        }

        // Two trailing PicSay Pro thought bubbles leading toward bottom-left
        val bubble1 = Path().apply {
            val cx = -halfW * 0.36f
            val cy = halfH * 0.70f
            val rx = halfW * 0.13f
            val ry = halfH * 0.11f
            addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
        }
        val bubble2 = Path().apply {
            val cx = -halfW * 0.56f
            val cy = halfH * 0.90f
            val rx = halfW * 0.08f
            val ry = halfH * 0.07f
            addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW)
        }
        cloud.op(bubble1, Path.Op.UNION)
        cloud.op(bubble2, Path.Op.UNION)
        return cloud
    }

    private fun createShoutBurstPath(halfW: Float, halfH: Float): Path {
        val spikes = 16
        val path = Path()
        val step = PI / spikes
        for (i in 0 until 2 * spikes) {
            val ratio = when {
                i % 2 == 1 -> 0.68f
                i % 4 == 0 -> 1.0f
                else -> 0.88f
            }
            val angle = i * step - PI / 2.0
            val x = (halfW * ratio * cos(angle)).toFloat()
            val y = (halfH * ratio * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun createStarPath(halfW: Float, halfH: Float, points: Int, innerRatio: Float): Path {
        val path = Path()
        val step = PI / points
        for (i in 0 until 2 * points) {
            val rX = if (i % 2 == 0) halfW else halfW * innerRatio
            val rY = if (i % 2 == 0) halfH else halfH * innerRatio
            val angle = i * step - (PI / 2.0)
            val x = (rX * cos(angle)).toFloat()
            val y = (rY * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    private fun createPolygonPath(halfW: Float, halfH: Float, sides: Int, angleOffsetRad: Double): Path {
        val path = Path()
        val step = (2.0 * PI / sides)
        for (i in 0 until sides) {
            val angle = i * step + angleOffsetRad
            val x = (halfW * cos(angle)).toFloat()
            val y = (halfH * sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }
}
