package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.PointF
import kotlin.math.abs

data class SnapResult(
    val snappedPosition: PointF,
    val showVerticalGuide: Boolean = false,
    val showHorizontalGuide: Boolean = false
)

object SnappingEngine {
    private const val SNAP_THRESHOLD_PX = 18f

    fun calculateSnap(rawPosition: PointF): SnapResult {
        val snapX = abs(rawPosition.x) <= SNAP_THRESHOLD_PX
        val snapY = abs(rawPosition.y) <= SNAP_THRESHOLD_PX

        return SnapResult(
            snappedPosition = PointF(
                x = if (snapX) 0f else rawPosition.x,
                y = if (snapY) 0f else rawPosition.y
            ),
            showVerticalGuide = snapX,
            showHorizontalGuide = snapY
        )
    }
}
