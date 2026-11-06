package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.PointF
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class HandleType {
    NONE,
    BODY,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    ROTATION,
    DELETE,
    DUPLICATE
}

data class LayerBounds(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val topLeft: PointF,
    val topRight: PointF,
    val bottomLeft: PointF,
    val bottomRight: PointF,
    val rotationHandle: PointF,
    val deleteHandle: PointF,
    val duplicateHandle: PointF
)

object TransformHelper {

    fun rotatePoint(point: PointF, center: PointF, angleDegrees: Float): PointF {
        val rad = (angleDegrees * PI / 180f).toFloat()
        val cosA = cos(rad)
        val sinA = sin(rad)
        val dx = point.x - center.x
        val dy = point.y - center.y
        return PointF(
            x = center.x + (dx * cosA - dy * sinA),
            y = center.y + (dx * sinA + dy * cosA)
        )
    }

    fun computeLayerBounds(layer: Layer, canvasCenter: PointF): LayerBounds {
        val cx = canvasCenter.x + layer.positionX
        val cy = canvasCenter.y + layer.positionY
        val center = PointF(cx, cy)

        val halfW = (layer.width * layer.scaleX) / 2f
        val halfH = (layer.height * layer.scaleY) / 2f

        // Unrotated corners
        val rawTL = PointF(cx - halfW, cy - halfH)
        val rawTR = PointF(cx + halfW, cy - halfH)
        val rawBL = PointF(cx - halfW, cy + halfH)
        val rawBR = PointF(cx + halfW, cy + halfH)
        val rawRot = PointF(cx, cy - halfH - 45f)
        val rawDel = PointF(cx - halfW, cy - halfH)
        val rawDup = PointF(cx + halfW, cy + halfH)

        return LayerBounds(
            centerX = cx,
            centerY = cy,
            width = halfW * 2f,
            height = halfH * 2f,
            rotation = layer.rotation,
            topLeft = rotatePoint(rawTL, center, layer.rotation),
            topRight = rotatePoint(rawTR, center, layer.rotation),
            bottomLeft = rotatePoint(rawBL, center, layer.rotation),
            bottomRight = rotatePoint(rawBR, center, layer.rotation),
            rotationHandle = rotatePoint(rawRot, center, layer.rotation),
            deleteHandle = rotatePoint(rawDel, center, layer.rotation),
            duplicateHandle = rotatePoint(rawDup, center, layer.rotation)
        )
    }

    fun hitTestHandle(
        touch: PointF,
        bounds: LayerBounds,
        handleHitRadius: Float = 36f
    ): HandleType {
        fun dist(p1: PointF, p2: PointF): Float {
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            return sqrt(dx * dx + dy * dy)
        }

        if (dist(touch, bounds.rotationHandle) <= handleHitRadius) return HandleType.ROTATION
        if (dist(touch, bounds.bottomRight) <= handleHitRadius) return HandleType.BOTTOM_RIGHT
        if (dist(touch, bounds.topLeft) <= handleHitRadius) return HandleType.TOP_LEFT
        if (dist(touch, bounds.topRight) <= handleHitRadius) return HandleType.TOP_RIGHT
        if (dist(touch, bounds.bottomLeft) <= handleHitRadius) return HandleType.BOTTOM_LEFT

        // Unrotate touch relative to center to test inside body
        val unrotatedTouch = rotatePoint(touch, PointF(bounds.centerX, bounds.centerY), -bounds.rotation)
        val halfW = bounds.width / 2f
        val halfH = bounds.height / 2f

        val inside = unrotatedTouch.x >= bounds.centerX - halfW &&
                unrotatedTouch.x <= bounds.centerX + halfW &&
                unrotatedTouch.y >= bounds.centerY - halfH &&
                unrotatedTouch.y <= bounds.centerY + halfH

        return if (inside) HandleType.BODY else HandleType.NONE
    }

    fun calculateAngle(center: PointF, point: PointF): Float {
        val dx = point.x - center.x
        val dy = point.y - center.y
        val radians = atan2(dy.toDouble(), dx.toDouble())
        var degrees = (radians * 180.0 / PI).toFloat() + 90f // 0 at top
        if (degrees < -180f) degrees += 360f
        if (degrees > 180f) degrees -= 360f
        return degrees
    }
}
