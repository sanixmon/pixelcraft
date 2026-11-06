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
        val result = SnappingEngine.calculateSnap(PointF(8f, -10f))

        assertEquals(0f, result.snappedPosition.x, 0.001f)
        assertEquals(0f, result.snappedPosition.y, 0.001f)
        assertTrue(result.showVerticalGuide)
        assertTrue(result.showHorizontalGuide)
    }

    @Test
    fun testSingleAxisSnapping() {
        val result = SnappingEngine.calculateSnap(PointF(12f, 120f))

        assertEquals(0f, result.snappedPosition.x, 0.001f)
        assertEquals(120f, result.snappedPosition.y, 0.001f)
        assertTrue(result.showVerticalGuide)
        assertFalse(result.showHorizontalGuide)
    }

    @Test
    fun testNoSnappingOutsideThreshold() {
        val result = SnappingEngine.calculateSnap(PointF(150f, 250f))

        assertEquals(150f, result.snappedPosition.x, 0.001f)
        assertEquals(250f, result.snappedPosition.y, 0.001f)
        assertFalse(result.showVerticalGuide)
        assertFalse(result.showHorizontalGuide)
    }
}
