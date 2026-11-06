package com.app.pixelcraft

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
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

    @Test
    fun testCanvasEdgeSnappingAndInterLayerSmartGuides() {
        // Bug 2.10 & 2.11: A 200x100 layer whose left edge is at -532 (8px from left canvas edge -540)
        // has center at -432 -> snaps its left edge cleanly to -540 (center = -440)!
        val edgeSnap = SnappingEngine.calculateSnap(
            rawPosition = PointF(-432f, 120f),
            canvasWidth = 1080f,
            canvasHeight = 1080f,
            thresholdPx = 12f,
            layerWidth = 200f,
            layerHeight = 100f
        )
        assertEquals(-440f, edgeSnap.snappedPosition.x, 0.01f)
        assertEquals(-540f, edgeSnap.verticalGuideX!!, 0.01f)

        // Smart Guides against another visible layer at x = 210, width = 160 (left = 130, right = 290)
        val referenceLayer = Layer(
            id = "ref",
            name = "Reference",
            type = LayerType.SHAPE,
            positionX = 210f,
            positionY = -180f,
            width = 160f,
            height = 80f
        )
        val smartSnap = SnappingEngine.calculateSnap(
            rawPosition = PointF(204f, -175f),
            canvasWidth = 1080f,
            canvasHeight = 1080f,
            thresholdPx = 10f,
            layerWidth = 160f,
            layerHeight = 80f,
            otherLayers = listOf(referenceLayer)
        )
        assertEquals(210f, smartSnap.snappedPosition.x, 0.01f)
        assertEquals(-180f, smartSnap.snappedPosition.y, 0.01f)
        assertEquals(210f, smartSnap.verticalGuideX!!, 0.01f)
        assertEquals(-180f, smartSnap.horizontalGuideY!!, 0.01f)
    }
}
