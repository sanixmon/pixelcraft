package com.app.pixelcraft

import com.app.pixelcraft.domain.editor.LayerGestureTracker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LayerGestureTrackerTest {

    @Test
    fun dragAccumulatesFromRawPositionNotSnappedLayerPosition() {
        val t = LayerGestureTracker()
        val first = t.advanceDrag("a", currentX = 0f, currentY = 0f, dx = 3f, dy = 0f)
        assertEquals(3f, first.x, 0.001f)
        // Layer was snapped back to 0 by a guide; the raw trajectory must keep going.
        val second = t.advanceDrag("a", currentX = 0f, currentY = 0f, dx = 3f, dy = 1f)
        assertEquals(6f, second.x, 0.001f)
        assertEquals(1f, second.y, 0.001f)
    }

    @Test
    fun dragOnAnotherLayerStartsFromThatLayersPosition() {
        val t = LayerGestureTracker()
        t.advanceDrag("a", 0f, 0f, 10f, 10f)
        val b = t.advanceDrag("b", 100f, 50f, 1f, 1f)
        assertEquals(101f, b.x, 0.001f)
        assertEquals(51f, b.y, 0.001f)
    }

    @Test
    fun rotationSnapsWithinThresholdAndReportsHapticOnlyOnce() {
        val t = LayerGestureTracker()
        val r1 = t.advanceRotation(currentRotation = 85f, deltaDeg = 2f, snap = true)
        assertEquals(90f, r1.degrees, 0.001f)
        assertTrue(r1.snappedJustNow)
        val r2 = t.advanceRotation(currentRotation = 90f, deltaDeg = 1f, snap = true)
        assertEquals(90f, r2.degrees, 0.001f)
        assertFalse("haptic must fire once per snap", r2.snappedJustNow)
    }

    @Test
    fun rotationEscapesSnapUsingRawAngle() {
        val t = LayerGestureTracker()
        t.advanceRotation(85f, 2f, snap = true) // raw 87 → snapped 90
        val escaped = t.advanceRotation(90f, 10f, snap = true) // raw 97, outside threshold
        assertEquals(97f, escaped.degrees, 0.001f)
        assertFalse(escaped.snappedJustNow)
    }

    @Test
    fun rotationWrapsIntoMinus180To180() {
        val t = LayerGestureTracker()
        assertEquals(-170f, t.advanceRotation(170f, 20f, snap = false).degrees, 0.001f)
    }

    @Test
    fun endRotationRestartsFromLayerRotation() {
        val t = LayerGestureTracker()
        t.advanceRotation(0f, 30f, snap = false)
        t.endRotation()
        assertEquals(15f, t.advanceRotation(10f, 5f, snap = false).degrees, 0.001f)
    }
}
