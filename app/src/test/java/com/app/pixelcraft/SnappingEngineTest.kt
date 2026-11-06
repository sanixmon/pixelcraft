package com.app.pixelcraft

import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.domain.SnappingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnappingEngineTest {

    @Test
    fun testCenterSnapping() {
        val canvasCenter = PointF(540f, 540f)

        // Position within threshold of 0 center offset
        val result = SnappingEngine.calculateSnap(
            rawPosition = PointF(8f, -10f),
            canvasCenter = canvasCenter,
            gridSize = 100f,
            isSnapToGrid = false
        )

        assertEquals(0f, result.snappedPosition.x, 0.001f)
        assertEquals(0f, result.snappedPosition.y, 0.001f)
        assertTrue(result.showVerticalGuide)
        assertTrue(result.showHorizontalGuide)
    }

    @Test
    fun testGridSnapping() {
        val canvasCenter = PointF(540f, 540f)

        // Raw position 95 is close to grid step 100
        val result = SnappingEngine.calculateSnap(
            rawPosition = PointF(95f, 204f),
            canvasCenter = canvasCenter,
            gridSize = 100f,
            isSnapToGrid = true
        )

        assertEquals(100f, result.snappedPosition.x, 0.001f)
        assertEquals(200f, result.snappedPosition.y, 0.001f)
    }

    @Test
    fun testNoSnappingOutsideThreshold() {
        val canvasCenter = PointF(540f, 540f)

        val result = SnappingEngine.calculateSnap(
            rawPosition = PointF(150f, 250f),
            canvasCenter = canvasCenter,
            gridSize = 100f,
            isSnapToGrid = false
        )

        assertEquals(150f, result.snappedPosition.x, 0.001f)
        assertEquals(250f, result.snappedPosition.y, 0.001f)
        assertFalse(result.showVerticalGuide)
        assertFalse(result.showHorizontalGuide)
    }
}
