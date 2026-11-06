package com.app.pixelcraft

import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.domain.SnappingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
        assertEquals(0f, result.verticalGuideX!!, 0.001f)
        assertEquals(0f, result.horizontalGuideY!!, 0.001f)
    }

    @Test
    fun testGridDivisionSnapping() {
        // 1080x1080 with 3x3 internal grid lines -> step = 270 -> lines at -270, 0, +270
        val result = SnappingEngine.calculateSnap(
            rawPosition = PointF(-262f, 276f),
            canvasWidth = 1080f,
            canvasHeight = 1080f,
            gridColumns = 3,
            gridRows = 3
        )

        assertEquals(-270f, result.snappedPosition.x, 0.001f)
        assertEquals(270f, result.snappedPosition.y, 0.001f)
        assertNotNull(result.verticalGuideX)
        assertNotNull(result.horizontalGuideY)
        assertEquals(-270f, result.verticalGuideX!!, 0.001f)
        assertEquals(270f, result.horizontalGuideY!!, 0.001f)
    }

    @Test
    fun testSingleAxisSnapping() {
        val result = SnappingEngine.calculateSnap(PointF(12f, 120f))

        assertEquals(0f, result.snappedPosition.x, 0.001f)
        assertEquals(120f, result.snappedPosition.y, 0.001f)
        assertTrue(result.showVerticalGuide)
        assertFalse(result.showHorizontalGuide)
        assertNull(result.horizontalGuideY)
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
