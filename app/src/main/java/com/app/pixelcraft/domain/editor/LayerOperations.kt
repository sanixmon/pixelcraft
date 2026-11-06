package com.app.pixelcraft.domain.editor

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.presentation.AlignmentAction
import java.util.UUID

/**
 * Pure, side-effect-free operations over immutable [Layer] lists so z-order, duplication,
 * alignment, and grouping can be unit-tested independently of Android / ViewModel state.
 */
object LayerOperations {

    fun List<Layer>.normalizeZIndices(): List<Layer> {
        return sortedBy { it.zIndex }.mapIndexed { index, layer ->
            if (layer.zIndex == index) layer else layer.copy(zIndex = index)
        }
    }

    fun List<Layer>.moveUp(layerId: String): List<Layer> {
        val sortedAsc = normalizeZIndices().toMutableList()
        val idx = sortedAsc.indexOfFirst { it.id == layerId }
        if (idx < 0 || idx >= sortedAsc.lastIndex) return this
        if (sortedAsc[idx].isLocked) return this

        val tmp = sortedAsc[idx]
        sortedAsc[idx] = sortedAsc[idx + 1]
        sortedAsc[idx + 1] = tmp
        return sortedAsc.mapIndexed { i, layer -> layer.copy(zIndex = i) }
    }

    fun List<Layer>.moveDown(layerId: String): List<Layer> {
        val sortedAsc = normalizeZIndices().toMutableList()
        val idx = sortedAsc.indexOfFirst { it.id == layerId }
        if (idx <= 0) return this
        if (sortedAsc[idx].isLocked) return this

        val tmp = sortedAsc[idx]
        sortedAsc[idx] = sortedAsc[idx - 1]
        sortedAsc[idx - 1] = tmp
        return sortedAsc.mapIndexed { i, layer -> layer.copy(zIndex = i) }
    }

    fun List<Layer>.bringToFront(layerId: String): List<Layer> {
        val current = find { it.id == layerId } ?: return this
        if (current.isLocked) return this
        val maxZ = maxOfOrNull { it.zIndex } ?: 0
        if (current.zIndex == maxZ && count { it.zIndex == maxZ } == 1) return this

        return map {
            if (it.id == layerId) it.copy(zIndex = maxZ + 1) else it
        }.normalizeZIndices()
    }

    fun List<Layer>.sendToBack(layerId: String): List<Layer> {
        val current = find { it.id == layerId } ?: return this
        if (current.isLocked) return this
        val minZ = minOfOrNull { it.zIndex } ?: 0
        if (current.zIndex == minZ && count { it.zIndex == minZ } == 1) return this

        return map {
            if (it.id == layerId) it.copy(zIndex = minZ - 1) else it
        }.normalizeZIndices()
    }

    fun List<Layer>.deleteUnlocked(layerId: String): List<Layer> {
        val target = find { it.id == layerId } ?: return this
        if (target.isLocked) return this
        return filter { it.id != layerId }.normalizeZIndices()
    }

    fun List<Layer>.duplicate(
        layerId: String,
        newId: String = UUID.randomUUID().toString(),
        offsetPx: Float = 25f
    ): Pair<List<Layer>, Layer>? {
        val source = find { it.id == layerId } ?: return null
        val nextZ = (maxOfOrNull { it.zIndex } ?: -1) + 1
        val copy = source.copy(
            id = newId,
            name = "${source.name} Copy",
            isLocked = false,
            positionX = source.positionX + offsetPx,
            positionY = source.positionY + offsetPx,
            zIndex = nextZ
        )
        return (this + copy).normalizeZIndices() to copy
    }

    fun alignSingleLayer(
        layer: Layer,
        canvasWidth: Float,
        canvasHeight: Float,
        action: AlignmentAction
    ): Layer {
        if (layer.isLocked) return layer
        val shapeOuterStroke = if (layer.type == LayerType.SHAPE && layer.shapeStrokeWidth > 0f) {
            when (layer.strokePosition) {
                StrokePosition.OUTSIDE -> layer.shapeStrokeWidth
                StrokePosition.CENTER -> layer.shapeStrokeWidth / 2f
                StrokePosition.INSIDE -> 0f
            }
        } else {
            0f
        }
        val halfW = (layer.width / 2f) + shapeOuterStroke
        val halfH = (layer.height / 2f) + shapeOuterStroke

        return when (action) {
            AlignmentAction.CENTER_HORIZONTAL -> layer.copy(positionX = 0f)
            AlignmentAction.CENTER_VERTICAL -> layer.copy(positionY = 0f)
            AlignmentAction.ALIGN_LEFT -> layer.copy(positionX = -canvasWidth / 2f + halfW)
            AlignmentAction.ALIGN_RIGHT -> layer.copy(positionX = canvasWidth / 2f - halfW)
            AlignmentAction.ALIGN_TOP -> layer.copy(positionY = -canvasHeight / 2f + halfH)
            AlignmentAction.ALIGN_BOTTOM -> layer.copy(positionY = canvasHeight / 2f - halfH)
        }
    }

    fun List<Layer>.groupLayers(layerIds: Set<String>, groupId: String = UUID.randomUUID().toString()): List<Layer> {
        if (layerIds.size < 2) return this
        return map { layer ->
            if (layer.id in layerIds && !layer.isLocked) layer.copy(groupId = groupId) else layer
        }
    }

    fun List<Layer>.ungroupLayers(layerIds: Set<String>): List<Layer> {
        if (layerIds.isEmpty()) return this
        val targetGroups = filter { it.id in layerIds }.mapNotNull { it.groupId }.toSet()
        return map { layer ->
            if (layer.id in layerIds || (layer.groupId != null && layer.groupId in targetGroups)) {
                layer.copy(groupId = null)
            } else {
                layer
            }
        }
    }
}
