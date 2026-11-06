package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.Layer
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
 * Magnetic anchor & Smart Guides snapping engine (invoked when the editor Grid/Snap feature is enabled).
 *
 * Fixes Bugs 2.10 & 2.11:
 * - Accepts screen-space [thresholdPx] (`12.dp.toPx() / effectiveCanvasScale`) so magnetic pull feels
 *   identical on both 512px and 4096px canvases.
 * - Snaps the dragged layer's center AND left/right/top/bottom edges (`layerWidth`, `layerHeight`)
 *   against canvas center `(0, 0)`, canvas edges, active grid divisions, AND other visible layers'
 *   centers and edges (Smart Guides).
 */
object SnappingEngine {
    const val SNAP_THRESHOLD_PX = 18f

    private data class AxisCandidate(
        val snappedCenter: Float,
        val guideLine: Float,
        val distance: Float
    )

    fun calculateSnap(
        rawPosition: PointF,
        canvasWidth: Float = 1080f,
        canvasHeight: Float = 1080f,
        gridColumns: Int = 0,
        gridRows: Int = 0,
        thresholdPx: Float = SNAP_THRESHOLD_PX,
        layerWidth: Float = 0f,
        layerHeight: Float = 0f,
        otherLayers: List<Layer> = emptyList()
    ): SnapResult {
        val safeThreshold = thresholdPx.coerceIn(2f, 120f)
        val halfCanvasW = canvasWidth / 2f
        val halfCanvasH = canvasHeight / 2f

        val xTargets = buildList {
            add(0f)
            if (layerWidth > 0f && canvasWidth > 0f) {
                add(-halfCanvasW)
                add(halfCanvasW)
            }
            if (gridColumns > 0 && canvasWidth > 0f) {
                val stepX = canvasWidth / (gridColumns + 1)
                for (c in 1..gridColumns) {
                    add(-halfCanvasW + c * stepX)
                }
            }
            for (other in otherLayers) {
                if (!other.isVisible) continue
                add(other.positionX)
                if (other.width > 0f) {
                    add(other.positionX - other.width / 2f)
                    add(other.positionX + other.width / 2f)
                }
            }
        }

        val yTargets = buildList {
            add(0f)
            if (layerHeight > 0f && canvasHeight > 0f) {
                add(-halfCanvasH)
                add(halfCanvasH)
            }
            if (gridRows > 0 && canvasHeight > 0f) {
                val stepY = canvasHeight / (gridRows + 1)
                for (r in 1..gridRows) {
                    add(-halfCanvasH + r * stepY)
                }
            }
            for (other in otherLayers) {
                if (!other.isVisible) continue
                add(other.positionY)
                if (other.height > 0f) {
                    add(other.positionY - other.height / 2f)
                    add(other.positionY + other.height / 2f)
                }
            }
        }

        val xOffsets = if (layerWidth > 0f) {
            floatArrayOf(0f, -layerWidth / 2f, layerWidth / 2f)
        } else {
            floatArrayOf(0f)
        }
        val yOffsets = if (layerHeight > 0f) {
            floatArrayOf(0f, -layerHeight / 2f, layerHeight / 2f)
        } else {
            floatArrayOf(0f)
        }

        val bestX = findBestAxisSnap(rawPosition.x, xOffsets, xTargets, safeThreshold)
        val bestY = findBestAxisSnap(rawPosition.y, yOffsets, yTargets, safeThreshold)

        return SnapResult(
            snappedPosition = PointF(
                x = bestX?.snappedCenter ?: rawPosition.x,
                y = bestY?.snappedCenter ?: rawPosition.y
            ),
            verticalGuideX = bestX?.guideLine,
            horizontalGuideY = bestY?.guideLine
        )
    }

    private fun findBestAxisSnap(
        rawCenter: Float,
        anchorOffsets: FloatArray,
        targets: List<Float>,
        threshold: Float
    ): AxisCandidate? {
        var best: AxisCandidate? = null
        for (offset in anchorOffsets) {
            val anchorPos = rawCenter + offset
            for (target in targets) {
                val dist = abs(anchorPos - target)
                if (dist <= threshold) {
                    // Slight preference (+0.01f) for center-anchor ties over edge-anchor ties
                    val weightedDist = if (offset == 0f) dist else dist + 0.01f
                    if (best == null || weightedDist < best.distance) {
                        best = AxisCandidate(
                            snappedCenter = target - offset,
                            guideLine = target,
                            distance = weightedDist
                        )
                    }
                }
            }
        }
        return best
    }
}
