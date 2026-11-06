package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.PointF
import kotlin.math.abs

data class SnapResult(
    val snappedPosition: PointF,
    val verticalGuideX: Float? = null,
    val horizontalGuideY: Float? = null
) {
    val showVerticalGuide: Boolean get() = verticalGuideX != null
    val showHorizontalGuide: Boolean get() = horizontalGuideY != null
}

/**
 * Magnetic anchor snapping engine (invoked ONLY when the editor Grid feature is enabled).
 * Snaps centered virtual coordinates against both `(0, 0)` center axes and active grid divisions.
 */
object SnappingEngine {
    const val SNAP_THRESHOLD_PX = 18f

    fun calculateSnap(
        rawPosition: PointF,
        canvasWidth: Float = 1080f,
        canvasHeight: Float = 1080f,
        gridColumns: Int = 0,
        gridRows: Int = 0
    ): SnapResult {
        val xTargets = buildList {
            add(0f)
            if (gridColumns > 0 && canvasWidth > 0f) {
                val stepX = canvasWidth / (gridColumns + 1)
                for (c in 1..gridColumns) {
                    add(-canvasWidth / 2f + c * stepX)
                }
            }
        }
        val yTargets = buildList {
            add(0f)
            if (gridRows > 0 && canvasHeight > 0f) {
                val stepY = canvasHeight / (gridRows + 1)
                for (r in 1..gridRows) {
                    add(-canvasHeight / 2f + r * stepY)
                }
            }
        }

        val nearestX = xTargets.minByOrNull { abs(rawPosition.x - it) }
            ?.takeIf { abs(rawPosition.x - it) <= SNAP_THRESHOLD_PX }
        val nearestY = yTargets.minByOrNull { abs(rawPosition.y - it) }
            ?.takeIf { abs(rawPosition.y - it) <= SNAP_THRESHOLD_PX }

        return SnapResult(
            snappedPosition = PointF(
                x = nearestX ?: rawPosition.x,
                y = nearestY ?: rawPosition.y
            ),
            verticalGuideX = nearestX,
            horizontalGuideY = nearestY
        )
    }
}
