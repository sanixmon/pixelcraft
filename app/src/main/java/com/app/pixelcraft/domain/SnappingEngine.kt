package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.PointF
import kotlin.math.abs
import kotlin.math.round

data class SnapResult(
    val snappedPosition: PointF,
    val showVerticalGuide: Boolean = false,
    val guideX: Float = 0f,
    val showHorizontalGuide: Boolean = false,
    val guideY: Float = 0f
)

object SnappingEngine {
    private const val SNAP_THRESHOLD_PX = 18f

    fun calculateSnap(
        rawPosition: PointF,
        canvasCenter: PointF,
        gridSize: Float,
        isSnapToGrid: Boolean
    ): SnapResult {
        var snappedX = rawPosition.x
        var snappedY = rawPosition.y
        var showVert = false
        var showHoriz = false
        var guideX = 0f
        var guideY = 0f

        // Center line snapping (when position is close to 0 center offset)
        if (abs(rawPosition.x) <= SNAP_THRESHOLD_PX) {
            snappedX = 0f
            showVert = true
            guideX = canvasCenter.x
        }

        if (abs(rawPosition.y) <= SNAP_THRESHOLD_PX) {
            snappedY = 0f
            showHoriz = true
            guideY = canvasCenter.y
        }

        // Grid snapping
        if (isSnapToGrid && gridSize > 0f) {
            val gridStepX = round(rawPosition.x / gridSize) * gridSize
            if (abs(rawPosition.x - gridStepX) <= SNAP_THRESHOLD_PX) {
                snappedX = gridStepX
            }

            val gridStepY = round(rawPosition.y / gridSize) * gridSize
            if (abs(rawPosition.y - gridStepY) <= SNAP_THRESHOLD_PX) {
                snappedY = gridStepY
            }
        }

        return SnapResult(
            snappedPosition = PointF(snappedX, snappedY),
            showVerticalGuide = showVert,
            guideX = guideX,
            showHorizontalGuide = showHoriz,
            guideY = guideY
        )
    }
}
