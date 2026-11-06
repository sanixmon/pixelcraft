package com.app.pixelcraft

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.domain.ColorMath
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.PixelCraftViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class EditorBugRegressionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): PixelCraftViewModel {
        val dir = tempFolder.newFolder()
        return PixelCraftViewModel(Application(), ProjectStorage { dir })
    }

    private fun awaitCondition(timeoutMs: Long = 3000L, predicate: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!predicate() && System.currentTimeMillis() < deadline) {
            Thread.sleep(15)
        }
        assertTrue("Timed out waiting for async condition", predicate())
    }

    @Test
    fun multiLayerPresetsHaveDistinctAscendingZIndicesAndFittedBounds() {
        for (preset in DefaultPresets.presets) {
            val zIndices = preset.initialLayers.map { it.zIndex }
            assertEquals(
                "Preset '${preset.id}' has duplicate zIndex values: $zIndices",
                zIndices.size,
                zIndices.toSet().size
            )
            val topRendered = preset.initialLayers.sortedBy { it.zIndex }.last()
            val topHitTested = preset.initialLayers.sortedBy { it.zIndex }.asReversed().first()
            assertEquals(
                "Preset '${preset.id}' top hit-tested layer must match top rendered layer",
                topRendered.id,
                topHitTested.id
            )
        }

        val ytPreset = DefaultPresets.presets.first { it.id == "preset_youtube" }
        val subtitle = ytPreset.initialLayers.first { it.id == "layer_yt_sub" }
        assertTrue(
            "Long subtitle should have estimated width > 350f, got ${subtitle.width}",
            subtitle.width > 350f
        )

        val keepCalm = DefaultPresets.presets.first { it.id == "preset_keep_calm" }
        val crown = keepCalm.initialLayers.first { it.id == "layer_crown" }
        assertEquals(LayerType.SHAPE, crown.type)
        assertEquals(ShapeType.CROWN, crown.shapeType)
    }

    @Test
    fun moveLayerUpAndDownSwapVisualZOrderRegardlessOfListOrder() {
        val vm = createViewModel()
        // Initial layer (L0), add Circle Shape (L1), add Speech Balloon Shape (L2)
        val l0 = vm.uiState.value.layers.single().id
        vm.addShapeLayer(ShapeType.CIRCLE)
        val l1 = vm.uiState.value.selectedLayerId!!
        vm.addShapeLayer(ShapeType.SPEECH_BUBBLE)
        val l2 = vm.uiState.value.selectedLayerId!!

        fun visualBottomToTop(): List<String> =
            vm.uiState.value.layers.sortedBy { it.zIndex }.map { it.id }

        assertEquals(listOf(l0, l1, l2), visualBottomToTop())

        // Move top layer L2 down one step -> [L0, L2, L1]
        vm.moveLayerDown(l2)
        assertEquals(listOf(l0, l2, l1), visualBottomToTop())

        // Move bottom layer L0 up one step -> [L2, L0, L1]
        vm.moveLayerUp(l0)
        assertEquals(listOf(l2, l0, l1), visualBottomToTop())

        // Undo restores [L0, L2, L1]
        vm.undo()
        assertEquals(listOf(l0, l2, l1), visualBottomToTop())
    }

    @Test
    fun freemoveDefaultHasZeroAnchorPullWhileGridModeEnablesSmoothNonStickyAnchors() {
        val vm = createViewModel()
        assertFalse("Grid must be OFF by default", vm.uiState.value.isGridEnabled)

        // 1. Freemove (Grid OFF): small drag (+6f, -8f) near origin MUST NOT snap to (0, 0)
        vm.updateLayerPositionWithSnapping(6f, -8f)
        assertEquals(6f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(-8f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
        assertNull(vm.uiState.value.activeVerticalGuideX)
        assertNull(vm.uiState.value.activeHorizontalGuideY)
        vm.clearSnappingGuides()

        // Reset to origin
        vm.alignSelectedLayer(AlignmentAction.CENTER_HORIZONTAL)
        vm.alignSelectedLayer(AlignmentAction.CENTER_VERTICAL)
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)

        // 2. Enable Grid -> magnetic anchor active, AND slow 5px-per-frame drag must NOT stick forever at 0
        vm.toggleGrid()
        assertTrue(vm.uiState.value.isGridEnabled)

        // Frame 1..3 (+5f each -> raw = 5, 10, 15 <= 18f threshold -> snaps to 0f with active vertical guide)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertNotNull(vm.uiState.value.activeVerticalGuideX)

        // Frame 4 (+5f -> raw = 20f > 18f threshold -> smoothly breaks free to 20f!)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        assertEquals(20f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertNull(vm.uiState.value.activeVerticalGuideX)

        // Toggling Grid OFF clears any active guides
        vm.updateLayerPositionWithSnapping(-12f, 0f) // raw = 8f -> snapped to 0f while Grid ON
        assertNotNull(vm.uiState.value.activeVerticalGuideX)
        vm.toggleGrid()
        assertFalse(vm.uiState.value.isGridEnabled)
        assertNull(vm.uiState.value.activeVerticalGuideX)
    }

    @Test
    fun draggingLayerRecordsSingleUndoStepAndRestoresOrigin() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.canUndo)

        // Drag across several frames
        vm.updateLayerPositionWithSnapping(40f, 30f)
        vm.updateLayerPositionWithSnapping(30f, 20f)
        vm.updateLayerPositionWithSnapping(30f, 10f)
        vm.clearSnappingGuides()

        assertEquals(100f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(60f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
        assertTrue("Canvas drag should be undoable", vm.uiState.value.canUndo)

        // Single undo restores pre-drag origin and exhausts undo stack
        vm.undo()
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
        assertFalse("Multi-frame drag must coalesce into a single undo entry", vm.uiState.value.canUndo)

        // Redo restores final dragged coordinates
        vm.redo()
        assertEquals(100f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(60f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
    }

    @Test
    fun continuousSliderEditsCoalesceIntoSingleUndoStep() {
        val vm = createViewModel()
        val initialSize = vm.uiState.value.selectedLayer!!.fontSize

        for (step in 1..10) {
            vm.updateSelectedLayer(continuous = true) { it.copy(fontSize = initialSize + step * 3f) }
        }
        vm.finishContinuousEdit()

        assertEquals(initialSize + 30f, vm.uiState.value.selectedLayer!!.fontSize, 0.01f)
        vm.undo()
        assertEquals(initialSize, vm.uiState.value.selectedLayer!!.fontSize, 0.01f)
        assertFalse("Slider drag must coalesce into one undo step", vm.uiState.value.canUndo)

        // Also verify filter slider coalescing
        for (step in 1..8) {
            vm.updateCanvasFilters(continuous = true) {
                it.copy(vignetteEnabled = true, vignetteIntensity = 0.1f + step * 0.05f)
            }
        }
        vm.finishContinuousEdit()
        vm.undo()
        assertFalse(vm.uiState.value.canvasConfig.filters.vignetteEnabled)
        assertFalse(vm.uiState.value.canUndo)
    }

    @Test
    fun lockedLayerBlocksNudgeAlignUpdateDragReorderAndDelete() {
        val vm = createViewModel()
        val layerId = vm.uiState.value.selectedLayerId!!
        val originalText = vm.uiState.value.selectedLayer!!.text

        vm.toggleLayerLock(layerId)
        assertTrue(vm.uiState.value.selectedLayer!!.isLocked)

        vm.nudgeSelectedLayer(50f, 50f)
        vm.alignSelectedLayer(AlignmentAction.ALIGN_LEFT)
        vm.updateSelectedLayer { it.copy(text = "Mutated") }
        vm.updateLayerPositionWithSnapping(80f, 80f)
        vm.deleteSelectedLayer()
        vm.deleteLayer(layerId)

        val after = vm.uiState.value.layers.find { it.id == layerId }
        assertTrue("Locked layer must not be deleted", after != null)
        assertEquals(0f, after!!.positionX, 0.01f)
        assertEquals(0f, after.positionY, 0.01f)
        assertEquals(originalText, after.text)
        assertFalse("Blocked operations on locked layer should not pollute undo stack", vm.uiState.value.canUndo)
    }

    @Test
    fun invalidCustomCanvasDimensionsAreClampedToSafePositiveBounds() {
        val vm = createViewModel()
        vm.setAspectRatioPreset(AspectRatioPreset.CUSTOM, 0, -500)

        val config = vm.uiState.value.canvasConfig
        assertEquals(PixelCraftViewModel.MIN_CANVAS_DIMENSION, config.width)
        assertEquals(PixelCraftViewModel.MIN_CANVAS_DIMENSION, config.height)

        vm.setAspectRatioPreset(AspectRatioPreset.CUSTOM, 999_999, 999_999)
        val clamped = vm.uiState.value.canvasConfig
        assertEquals(PixelCraftViewModel.MAX_CANVAS_DIMENSION, clamped.width)
        assertEquals(PixelCraftViewModel.MAX_CANVAS_DIMENSION, clamped.height)
    }

    @Test
    fun projectSaveIsolationAcrossPresetsRenameAndDelete() = runTest {
        val vm = createViewModel()

        vm.saveProject("Poster One")
        awaitCondition { vm.uiState.value.currentProjectId != null && vm.uiState.value.savedProjects.size == 1 }
        val posterOneId = vm.uiState.value.currentProjectId!!

        vm.applyPreset(DefaultPresets.presets[1])
        assertNull(vm.uiState.value.currentProjectId)

        vm.saveProject("Poster Two")
        awaitCondition {
            vm.uiState.value.currentProjectId != null &&
                vm.uiState.value.currentProjectId != posterOneId &&
                vm.uiState.value.savedProjects.size == 2
        }
        val posterTwoId = vm.uiState.value.currentProjectId!!

        vm.addTextLayer("Updated")
        vm.saveProject("Poster Two")
        awaitCondition { vm.uiState.value.userMessage == "Saved project \"Poster Two\"" }
        assertEquals(posterTwoId, vm.uiState.value.currentProjectId)
        assertEquals(2, vm.uiState.value.savedProjects.size)

        vm.saveProject("Poster Three")
        awaitCondition { vm.uiState.value.savedProjects.size == 3 }
        assertEquals(
            setOf("Poster One", "Poster Two", "Poster Three"),
            vm.uiState.value.savedProjects.map { it.name }.toSet()
        )

        val posterThreeId = vm.uiState.value.currentProjectId!!
        vm.deleteProject(posterThreeId)
        awaitCondition { vm.uiState.value.savedProjects.size == 2 && vm.uiState.value.currentProjectId == null }
    }

    @Test
    fun colorMathHsvHexAndComposeUnsignedArgbRoundTripCleanly() {
        // Transparent shapeFillColor (0x00000000) must stay alpha 0 even when layer opacity is 1.0f
        assertEquals(0x00000000, ColorMath.scaleAlpha(0x00000000L, 1.0f))

        // Semi-transparent 0x80FF0055 at 50% layer opacity -> alpha 0x40 (64), RGB unchanged
        val scaled = ColorMath.scaleAlpha(0x80FF0055L, 0.5f)
        assertEquals(0x40, (scaled ushr 24) and 0xFF)
        assertEquals(0xFF0055, scaled and 0x00FFFFFF)

        // HSV <-> ARGB round-trip + Compose Color(Long) unsigned 32-bit compatibility
        val samples = listOf(
            0xFFFF0000L, 0xFF00FF00L, 0xFF0000FFL, 0xFF00E5FFL,
            0xFFFF007FL, 0xFFFFFFFFL, 0xFF000000L, 0x80651FFFL
        )
        for (sample in samples) {
            val hsv = ColorMath.argbToHsv(sample)
            val alpha = ColorMath.extractAlpha(sample)
            val rebuilt = ColorMath.hsvToArgb(hsv[0], hsv[1], hsv[2], alpha)
            assertEquals("Upper 32 bits must be 0 for Compose Color(Long)", 0L, rebuilt ushr 32)
            // Must construct Compose Color without IllegalArgumentException
            val composeColor = Color(rebuilt)
            assertNotNull(composeColor)
            assertEquals((sample ushr 24) and 0xFFL, (rebuilt ushr 24) and 0xFFL)
            assertTrue(kotlin.math.abs(((sample ushr 16) and 0xFFL) - ((rebuilt ushr 16) and 0xFFL)) <= 1L)
            assertTrue(kotlin.math.abs(((sample ushr 8) and 0xFFL) - ((rebuilt ushr 8) and 0xFFL)) <= 1L)
            assertTrue(kotlin.math.abs((sample and 0xFFL) - (rebuilt and 0xFFL)) <= 1L)
        }

        // Hex formatting & parsing
        assertEquals("#00E5FF", ColorMath.formatHex(0xFF00E5FFL, includeAlpha = false))
        assertEquals("#8000E5FF", ColorMath.formatHex(0x8000E5FFL, includeAlpha = true))
        assertEquals(0xFF00E5FFL, ColorMath.parseHexOrNull("#00e5ff"))
        assertEquals(0xCCFF0055L, ColorMath.parseHexOrNull("CCFF0055"))
        assertEquals(0x80112233L, ColorMath.parseHexOrNull("#112233", defaultAlpha = 0x80))
        assertNull(ColorMath.parseHexOrNull("#12345"))
        assertNull(ColorMath.parseHexOrNull("#GGGGGG"))
    }

    @Test
    fun textSizingScalesWithBundledFontsAndStrokePosition() {
        val condensed = TextSizing.withEstimatedBounds(
            Layer(id = "1", name = "Condensed", type = LayerType.TEXT, text = "HEADLINE", fontKey = "bebas_neue", fontSize = 48f)
        )
        val pixelWide = TextSizing.withEstimatedBounds(
            Layer(id = "2", name = "Pixel", type = LayerType.TEXT, text = "HEADLINE", fontKey = "press_start_2p", fontSize = 48f)
        )
        assertTrue("Press Start 2P must estimate wider than Bebas Neue", pixelWide.width > condensed.width)

        val insideStroke = TextSizing.withEstimatedBounds(
            Layer(
                id = "3",
                name = "Inside",
                type = LayerType.TEXT,
                text = "STROKE",
                fontSize = 48f,
                strokeWidth = 12f,
                strokePosition = StrokePosition.INSIDE
            )
        )
        val outsideStroke = TextSizing.withEstimatedBounds(
            Layer(
                id = "4",
                name = "Outside",
                type = LayerType.TEXT,
                text = "STROKE",
                fontSize = 48f,
                strokeWidth = 12f,
                strokePosition = StrokePosition.OUTSIDE
            )
        )
        assertTrue("Outside stroke must expand estimated bounds over Inside stroke", outsideStroke.width > insideStroke.width)
    }
}
