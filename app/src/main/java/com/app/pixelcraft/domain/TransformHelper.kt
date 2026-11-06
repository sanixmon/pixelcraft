package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.PointF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class LayerBounds(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float,
    val rotation: Float,
    val topLeft: PointF,
    val topRight: PointF,
    val bottomLeft: PointF,
    val bottomRight: PointF
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

        val halfW = layer.width / 2f
        val halfH = layer.height / 2f

        // Unrotated corners
        val rawTL = PointF(cx - halfW, cy - halfH)
        val rawTR = PointF(cx + halfW, cy - halfH)
        val rawBL = PointF(cx - halfW, cy + halfH)
        val rawBR = PointF(cx + halfW, cy + halfH)

        return LayerBounds(
            centerX = cx,
            centerY = cy,
            width = layer.width,
            height = layer.height,
            rotation = layer.rotation,
            topLeft = rotatePoint(rawTL, center, layer.rotation),
            topRight = rotatePoint(rawTR, center, layer.rotation),
            bottomLeft = rotatePoint(rawBL, center, layer.rotation),
            bottomRight = rotatePoint(rawBR, center, layer.rotation)
        )
    }

    fun hitTestBody(touch: PointF, bounds: LayerBounds): Boolean {
        // Unrotate touch relative to center to test inside body
        val unrotatedTouch = rotatePoint(touch, PointF(bounds.centerX, bounds.centerY), -bounds.rotation)
        val halfW = bounds.width / 2f
        val halfH = bounds.height / 2f

        return unrotatedTouch.x >= bounds.centerX - halfW &&
                unrotatedTouch.x <= bounds.centerX + halfW &&
                unrotatedTouch.y >= bounds.centerY - halfH &&
                unrotatedTouch.y <= bounds.centerY + halfH
    }
}
