package com.app.pixelcraft

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.domain.editor.GroupOperations
import com.app.pixelcraft.domain.editor.GroupOperations.duplicateGroup
import com.app.pixelcraft.domain.editor.GroupOperations.moveBlock
import com.app.pixelcraft.presentation.AlignmentAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GroupOperationsTest {

    private fun shape(id: String, x: Float, y: Float, z: Int = 0, group: String? = "g") = Layer(
        id = id, name = id, type = LayerType.SHAPE, groupId = group,
        positionX = x, positionY = y, width = 100f, height = 100f, zIndex = z
    )

    @Test
    fun scale_doublesSizeAndDistanceFromPivot() {
        val out = GroupOperations.scale(listOf(shape("a", -100f, 0f), shape("b", 100f, 0f)), 2f, 0f, 0f)
        assertEquals(-200f, out[0].positionX, 0.001f)
        assertEquals(200f, out[1].positionX, 0.001f)
        assertEquals(200f, out[0].width, 0.001f)
    }

    @Test
    fun rotate_90MovesMembersClockwiseAndAddsRotation() {
        val out = GroupOperations.rotate(listOf(shape("a", 100f, 0f)), 90f, 0f, 0f)
        assertEquals(0f, out[0].positionX, 0.001f)
        assertEquals(100f, out[0].positionY, 0.001f)
        assertEquals(90f, out[0].rotation, 0.001f)
    }

    @Test
    fun flipHorizontal_mirrorsAcrossGroupCentre() {
        val out = GroupOperations.flip(listOf(shape("a", 0f, 0f), shape("b", 200f, 0f)), horizontal = true)
        assertEquals(200f, out[0].positionX, 0.001f)
        assertEquals(0f, out[1].positionX, 0.001f)
        assertTrue(out.all { it.flipHorizontal })
    }

    @Test
    fun alignLeft_movesBoundsToCanvasEdgeKeepingSpacing() {
        val out = GroupOperations.align(
            listOf(shape("a", 0f, 0f), shape("b", 200f, 0f)), 1000f, 1000f, AlignmentAction.ALIGN_LEFT
        )
        assertEquals(-450f, out[0].positionX, 0.001f)
        assertEquals(-250f, out[1].positionX, 0.001f)
    }

    @Test
    fun moveBlockFront_keepsGroupOrderAboveOthers() {
        val layers = listOf(shape("a", 0f, 0f, 0), shape("x", 0f, 0f, 1, null), shape("b", 0f, 0f, 2))
        val out = layers.moveBlock(setOf("a", "b"), front = true).sortedBy { it.zIndex }.map { it.id }
        assertEquals(listOf("x", "a", "b"), out)
    }

    @Test
    fun duplicateGroup_createsNewGroupOnTop() {
        val layers = listOf(shape("a", 0f, 0f, 0), shape("b", 0f, 0f, 1))
        val (all, copies) = layers.duplicateGroup(setOf("a", "b"), newGroupId = "g2")
        assertEquals(4, all.size)
        assertTrue(copies.all { it.groupId == "g2" && it.zIndex >= 2 })
    }
}
