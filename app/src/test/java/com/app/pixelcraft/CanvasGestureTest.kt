package com.app.pixelcraft

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.PixelCraftUiState
import com.app.pixelcraft.presentation.components.PixelCraftCanvas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

/**
 * Drives real pointer input through [PixelCraftCanvas] and checks the callbacks the
 * ViewModel relies on. ViewModel-level gesture maths is covered in EditorBugRegressionTest;
 * this covers the touch → callback translation that previously had no tests.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CanvasGestureTest {

    @get:Rule
    val compose = createComposeRule()

    private val bigBox = Layer(
        id = "box",
        name = "Box",
        type = LayerType.SHAPE,
        shapeType = ShapeType.RECTANGLE,
        width = 600f,
        height = 600f
    )

    private class Recorder {
        val selections = mutableListOf<String?>()
        var dragDx = 0f
        var dragDy = 0f
        var dragCalls = 0
        var dragEnds = 0
        var zoomProduct = 1f
        var transformCalls = 0
    }

    private fun setCanvas(selectedId: String?, layers: List<Layer> = listOf(bigBox)): Recorder {
        val rec = Recorder()
        val state = PixelCraftUiState(
            canvasConfig = CanvasConfig(width = 1080, height = 1080),
            layers = layers,
            selectedLayerId = selectedId,
            onboardingDismissed = true
        )
        compose.setContent {
            Box(Modifier.size(400.dp).testTag("canvas")) {
                PixelCraftCanvas(
                    state = state,
                    onSelectLayer = { rec.selections += it },
                    onDragDelta = { dx, dy, _ ->
                        rec.dragDx += dx
                        rec.dragDy += dy
                        rec.dragCalls++
                    },
                    onTransformGesture = { _, _, zoom, _ ->
                        rec.zoomProduct *= zoom
                        rec.transformCalls++
                        false
                    },
                    onUpdateSelectedLayer = { _, _ -> },
                    onDragEnd = { rec.dragEnds++ }
                )
            }
        }
        compose.waitForIdle()
        return rec
    }

    @Test
    fun draggingSelectedLayerReportsHorizontalMovementAndEnds() {
        val rec = setCanvas(selectedId = "box")
        compose.onNodeWithTag("canvas").performTouchInput {
            swipe(start = center, end = center + Offset(120f, 0f), durationMillis = 300)
        }
        compose.waitForIdle()

        assertTrue("onDragDelta was never called", rec.dragCalls > 0)
        assertTrue("expected rightward drag, got dx=${rec.dragDx}", rec.dragDx > 0f)
        assertTrue("expected ~no vertical drag, got dy=${rec.dragDy}", abs(rec.dragDy) < abs(rec.dragDx) * 0.2f)
        assertEquals(1, rec.dragEnds)
    }

    @Test
    fun draggingUnselectedLayerSelectsItFirst() {
        val rec = setCanvas(selectedId = null)
        compose.onNodeWithTag("canvas").performTouchInput {
            swipe(start = center, end = center + Offset(0f, 120f), durationMillis = 300)
        }
        compose.waitForIdle()

        assertTrue("expected selection of 'box', got ${rec.selections}", "box" in rec.selections)
        assertTrue("expected downward drag, got dy=${rec.dragDy}", rec.dragDy > 0f)
    }

    @Test
    fun twoFingerPinchOutOnSelectedLayerScalesItUp() {
        val rec = setCanvas(selectedId = "box")
        compose.onNodeWithTag("canvas").performTouchInput {
            pinch(
                start0 = center - Offset(30f, 0f),
                end0 = center - Offset(120f, 0f),
                start1 = center + Offset(30f, 0f),
                end1 = center + Offset(120f, 0f),
                durationMillis = 400
            )
        }
        compose.waitForIdle()

        assertTrue("onTransformGesture was never called", rec.transformCalls > 0)
        assertTrue("expected zoom > 1, got ${rec.zoomProduct}", rec.zoomProduct > 1.2f)
        assertEquals("pinch must not be reported as a single-finger drag", 0, rec.dragCalls)
    }

    @Test
    fun twoFingerPinchInOnSelectedLayerScalesItDown() {
        val rec = setCanvas(selectedId = "box")
        compose.onNodeWithTag("canvas").performTouchInput {
            pinch(
                start0 = center - Offset(120f, 0f),
                end0 = center - Offset(40f, 0f),
                start1 = center + Offset(120f, 0f),
                end1 = center + Offset(40f, 0f),
                durationMillis = 400
            )
        }
        compose.waitForIdle()

        assertTrue("expected zoom < 1, got ${rec.zoomProduct}", rec.zoomProduct < 0.8f)
    }

    @Test
    fun lockedLayerIsNeitherDraggedNorTransformed() {
        val locked = bigBox.copy(isLocked = true)
        val rec = setCanvas(selectedId = "box", layers = listOf(locked))
        compose.onNodeWithTag("canvas").performTouchInput {
            swipe(start = center, end = center + Offset(120f, 0f), durationMillis = 300)
        }
        compose.onNodeWithTag("canvas").performTouchInput {
            pinch(
                start0 = center - Offset(30f, 0f),
                end0 = center - Offset(120f, 0f),
                start1 = center + Offset(30f, 0f),
                end1 = center + Offset(120f, 0f),
                durationMillis = 400
            )
        }
        compose.waitForIdle()

        assertEquals(0, rec.dragCalls)
        assertEquals(0, rec.transformCalls)
    }
}
