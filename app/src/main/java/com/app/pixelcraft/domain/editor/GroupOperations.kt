package com.app.pixelcraft.domain.editor

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.presentation.AlignmentAction
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Whole-group transforms: a group is scaled, rotated, flipped and aligned as one object
 * around the centre of its bounding box, the same way a single object is. Pure functions
 * over layer lists so they can be unit-tested without the ViewModel.
 *
 * Coordinates follow [Layer]: `positionX/Y` is the layer centre relative to the canvas
 * centre, y grows downwards, and positive rotation is clockwise.
 */
object GroupOperations {

    data class Bounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
        val centerX: Float get() = (left + right) / 2f
        val centerY: Float get() = (top + bottom) / 2f
    }

    /** Axis-aligned bounds of the members, accounting for each member's rotation. */
    fun bounds(members: List<Layer>): Bounds {
        var left = Float.POSITIVE_INFINITY
        var top = Float.POSITIVE_INFINITY
        var right = Float.NEGATIVE_INFINITY
        var bottom = Float.NEGATIVE_INFINITY
        for (layer in members) {
            val rad = Math.toRadians(layer.rotation.toDouble())
            val c = abs(cos(rad)).toFloat()
            val s = abs(sin(rad)).toFloat()
            val halfW = (layer.width * c + layer.height * s) / 2f
            val halfH = (layer.width * s + layer.height * c) / 2f
            left = minOf(left, layer.positionX - halfW)
            right = maxOf(right, layer.positionX + halfW)
            top = minOf(top, layer.positionY - halfH)
            bottom = maxOf(bottom, layer.positionY + halfH)
        }
        if (members.isEmpty()) return Bounds(0f, 0f, 0f, 0f)
        return Bounds(left, top, right, bottom)
    }

    fun translate(members: List<Layer>, dx: Float, dy: Float): List<Layer> =
        members.map { if (it.isLocked) it else it.copy(positionX = it.positionX + dx, positionY = it.positionY + dy) }

    /** Scales every member's size and its distance from ([pivotX], [pivotY]) by [factor]. */
    fun scale(members: List<Layer>, factor: Float, pivotX: Float, pivotY: Float): List<Layer> =
        members.map { layer ->
            if (layer.isLocked) return@map layer
            val moved = layer.copy(
                positionX = pivotX + (layer.positionX - pivotX) * factor,
                positionY = pivotY + (layer.positionY - pivotY) * factor
            )
            if (layer.type == LayerType.TEXT) {
                TextSizing.withEstimatedBounds(moved.copy(fontSize = (layer.fontSize * factor).coerceIn(8f, 520f)))
            } else {
                moved.copy(
                    width = (layer.width * factor).coerceIn(24f, 4096f),
                    height = (layer.height * factor).coerceIn(if (layer.shapeType == com.app.pixelcraft.data.model.ShapeType.LINE) 4f else 24f, 4096f)
                )
            }
        }

    /** Rotates the members as a rigid body by [degrees] (clockwise) around the pivot. */
    fun rotate(members: List<Layer>, degrees: Float, pivotX: Float, pivotY: Float): List<Layer> {
        if (degrees == 0f) return members
        val rad = Math.toRadians(degrees.toDouble())
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        return members.map { layer ->
            if (layer.isLocked) return@map layer
            val dx = layer.positionX - pivotX
            val dy = layer.positionY - pivotY
            layer.copy(
                positionX = pivotX + dx * c - dy * s,
                positionY = pivotY + dx * s + dy * c,
                rotation = normalizeDegrees(layer.rotation + degrees)
            )
        }
    }

    /** Mirrors the whole group across its vertical (horizontal = true) or horizontal axis. */
    fun flip(members: List<Layer>, horizontal: Boolean): List<Layer> {
        val b = bounds(members.filter { !it.isLocked })
        return members.map { layer ->
            if (layer.isLocked) return@map layer
            if (horizontal) {
                layer.copy(
                    positionX = 2f * b.centerX - layer.positionX,
                    flipHorizontal = !layer.flipHorizontal,
                    rotation = normalizeDegrees(-layer.rotation)
                )
            } else {
                layer.copy(
                    positionY = 2f * b.centerY - layer.positionY,
                    flipVertical = !layer.flipVertical,
                    rotation = normalizeDegrees(-layer.rotation)
                )
            }
        }
    }

    /** Aligns the group's bounding box to the canvas, moving all members together. */
    fun align(members: List<Layer>, canvasWidth: Float, canvasHeight: Float, action: AlignmentAction): List<Layer> {
        val b = bounds(members)
        val (dx, dy) = when (action) {
            AlignmentAction.CENTER_HORIZONTAL -> -b.centerX to 0f
            AlignmentAction.CENTER_VERTICAL -> 0f to -b.centerY
            AlignmentAction.ALIGN_LEFT -> (-canvasWidth / 2f - b.left) to 0f
            AlignmentAction.ALIGN_RIGHT -> (canvasWidth / 2f - b.right) to 0f
            AlignmentAction.ALIGN_TOP -> 0f to (-canvasHeight / 2f - b.top)
            AlignmentAction.ALIGN_BOTTOM -> 0f to (canvasHeight / 2f - b.bottom)
        }
        return translate(members, dx, dy)
    }

    /**
     * Spreads [members] so the gaps between their bounding boxes are equal, keeping the
     * first and last (by position) where they are. Needs at least three layers.
     */
    fun distribute(members: List<Layer>, horizontal: Boolean): List<Layer> {
        val movable = members.filter { !it.isLocked }
        if (movable.size < 3) return members
        fun box(l: Layer) = bounds(listOf(l))
        val sorted = movable.sortedBy { if (horizontal) it.positionX else it.positionY }
        val first = box(sorted.first())
        val last = box(sorted.last())
        val span = if (horizontal) last.right - first.left else last.bottom - first.top
        val sizes = sorted.map { val b = box(it); if (horizontal) b.right - b.left else b.bottom - b.top }
        val gap = (span - sizes.sum()) / (sorted.size - 1)
        var cursor = if (horizontal) first.left else first.top
        val moved = HashMap<String, Layer>()
        sorted.forEachIndexed { i, layer ->
            val b = box(layer)
            val start = if (horizontal) b.left else b.top
            val delta = cursor - start
            moved[layer.id] = if (horizontal) layer.copy(positionX = layer.positionX + delta) else layer.copy(positionY = layer.positionY + delta)
            cursor += sizes[i] + gap
        }
        return members.map { moved[it.id] ?: it }
    }

    /** Moves all [memberIds] above (front = true) or below every other layer, keeping their own order. */
    fun List<Layer>.moveBlock(memberIds: Set<String>, front: Boolean): List<Layer> {
        val sorted = sortedBy { it.zIndex }
        val members = sorted.filter { it.id in memberIds }
        val others = sorted.filter { it.id !in memberIds }
        val ordered = if (front) others + members else members + others
        return ordered.mapIndexed { i, layer -> if (layer.zIndex == i) layer else layer.copy(zIndex = i) }
    }

    /** Copies all [memberIds] into a new group placed on top, offset by [offsetPx]. */
    fun List<Layer>.duplicateGroup(
        memberIds: Set<String>,
        newGroupId: String = UUID.randomUUID().toString(),
        offsetPx: Float = 25f
    ): Pair<List<Layer>, List<Layer>> {
        val sources = filter { it.id in memberIds }.sortedBy { it.zIndex }
        var nextZ = (maxOfOrNull { it.zIndex } ?: -1) + 1
        val copies = sources.map { src ->
            src.copy(
                id = UUID.randomUUID().toString(),
                name = "${src.name} Copy",
                isLocked = false,
                // Grouped layers stay grouped (as a new group); loose ones stay loose.
                groupId = if (src.groupId != null) newGroupId else null,
                positionX = src.positionX + offsetPx,
                positionY = src.positionY + offsetPx,
                zIndex = nextZ++
            )
        }
        return (this + copies) to copies
    }

    private fun normalizeDegrees(deg: Float): Float {
        var d = deg % 360f
        if (d > 180f) d -= 360f
        if (d <= -180f) d += 360f
        return d
    }
}
