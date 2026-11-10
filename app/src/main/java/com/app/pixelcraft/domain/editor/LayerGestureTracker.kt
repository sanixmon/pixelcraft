package com.app.pixelcraft.domain.editor

import kotlin.math.abs

/**
 * Remembers the *un-snapped* finger trajectory during a drag or twist gesture.
 *
 * Snapping moves the layer to a guide or cardinal angle, but the next gesture delta must be
 * applied to where the finger really is, otherwise a snapped layer "sticks" and never escapes.
 */
class LayerGestureTracker(
    private val snapAngles: FloatArray = DEFAULT_SNAP_ANGLES,
    private val snapThresholdDeg: Float = DEFAULT_SNAP_THRESHOLD_DEG
) {
    data class Position(val x: Float, val y: Float)

    data class Rotation(val degrees: Float, val snappedJustNow: Boolean)

    private var dragLayerId: String? = null
    private var dragPosition: Position? = null
    private var rawRotation: Float? = null
    private var lastSnappedAngle: Float? = null

    /** Returns the raw (un-snapped) position after applying [dx]/[dy] to layer [layerId]. */
    fun advanceDrag(layerId: String, currentX: Float, currentY: Float, dx: Float, dy: Float): Position {
        val base = dragPosition?.takeIf { dragLayerId == layerId } ?: Position(currentX, currentY)
        val next = Position(base.x + dx, base.y + dy)
        dragLayerId = layerId
        dragPosition = next
        return next
    }

    /**
     * Applies [deltaDeg] to the raw rotation and, when [snap] is on, snaps to the nearest angle within
     * the threshold. [Rotation.snappedJustNow] is true only on the first frame of a new snap (for haptics).
     */
    fun advanceRotation(currentRotation: Float, deltaDeg: Float, snap: Boolean): Rotation {
        val raw = normalize((rawRotation ?: currentRotation) + deltaDeg)
        rawRotation = raw
        if (!snap) return Rotation(raw, snappedJustNow = false)

        val snapped = snapAngles.firstOrNull { abs(raw - it) <= snapThresholdDeg }
        if (snapped == null) {
            lastSnappedAngle = null
            return Rotation(raw, snappedJustNow = false)
        }
        val isNew = lastSnappedAngle != snapped
        lastSnappedAngle = snapped
        return Rotation(snapped, snappedJustNow = isNew)
    }

    /** Ends a continuous rotate gesture but keeps drag tracking (matches finishContinuousEdit). */
    fun endRotation() {
        rawRotation = null
        lastSnappedAngle = null
    }

    fun reset() {
        dragLayerId = null
        dragPosition = null
        endRotation()
    }

    companion object {
        val DEFAULT_SNAP_ANGLES = floatArrayOf(-180f, -135f, -90f, -45f, 0f, 45f, 90f, 135f, 180f)
        const val DEFAULT_SNAP_THRESHOLD_DEG = 4f

        fun normalize(degrees: Float): Float {
            var a = degrees % 360f
            if (a > 180f) a -= 360f
            if (a < -180f) a += 360f
            return a
        }
    }
}
