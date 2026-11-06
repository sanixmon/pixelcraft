package com.app.pixelcraft

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.domain.TransformHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class TransformHelperTest {

    @Test
    fun testRotatePoint_90Degrees() {
        val center = PointF(0f, 0f)
        val point = PointF(10f, 0f)
        val rotated = TransformHelper.rotatePoint(point, center, 90f)

        assertTrue(abs(rotated.x) < 0.001f)
        assertEquals(10f, rotated.y, 0.001f)
    }

    @Test
    fun testRotatePoint_180Degrees() {
        val center = PointF(100f, 100f)
        val point = PointF(150f, 100f)
        val rotated = TransformHelper.rotatePoint(point, center, 180f)

        assertEquals(50f, rotated.x, 0.01f)
        assertEquals(100f, rotated.y, 0.01f)
    }

    @Test
    fun testComputeLayerBounds() {
        val layer = Layer(
            id = "layer1",
            name = "Test Layer",
            type = LayerType.TEXT,
            positionX = 0f,
            positionY = 0f,
            width = 200f,
            height = 100f,
            rotation = 0f
        )
        val center = PointF(500f, 500f)
        val bounds = TransformHelper.computeLayerBounds(layer, center)

        assertEquals(500f, bounds.centerX, 0.01f)
        assertEquals(500f, bounds.centerY, 0.01f)
        assertEquals(200f, bounds.width, 0.01f)
        assertEquals(100f, bounds.height, 0.01f)
        assertEquals(400f, bounds.topLeft.x, 0.01f)
        assertEquals(450f, bounds.topLeft.y, 0.01f)
    }

    @Test
    fun testHitTestBody() {
        val layer = Layer(
            id = "layer1",
            name = "Test Layer",
            type = LayerType.TEXT,
            positionX = 0f,
            positionY = 0f,
            width = 200f,
            height = 100f,
            rotation = 0f
        )
        val center = PointF(500f, 500f)
        val bounds = TransformHelper.computeLayerBounds(layer, center)

        assertTrue(TransformHelper.hitTestBody(PointF(500f, 500f), bounds))
        assertFalse(TransformHelper.hitTestBody(PointF(500f, 405f), bounds))
        assertFalse(TransformHelper.hitTestBody(PointF(100f, 100f), bounds))
    }
}
