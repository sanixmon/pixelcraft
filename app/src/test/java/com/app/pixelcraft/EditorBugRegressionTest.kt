package com.app.pixelcraft

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.domain.ColorMath
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.PixelCraftViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class EditorBugRegressionTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var viewModelStore: ViewModelStore
    private var vmCounter = 0

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModelStore = ViewModelStore()
        vmCounter = 0
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel(draftFile: File? = null): PixelCraftViewModel {
        val dir = tempFolder.newFolder()
        val ds = draftFile?.let { DraftStore { it } }
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PixelCraftViewModel(
                    application = Application(),
                    projectStorage = ProjectStorage { dir },
                    draftStore = ds
                ) as T
            }
        }
        return ViewModelProvider(viewModelStore, factory)["vm_${vmCounter++}", PixelCraftViewModel::class.java]
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
        assertEquals("Should provide 20 curated starter templates", 20, DefaultPresets.presets.size)
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
        val l0 = vm.uiState.value.layers.single().id
        vm.addShapeLayer(ShapeType.CIRCLE)
        val l1 = vm.uiState.value.selectedLayerId!!
        vm.addShapeLayer(ShapeType.SPEECH_BUBBLE)
        val l2 = vm.uiState.value.selectedLayerId!!

        fun visualBottomToTop(): List<String> =
            vm.uiState.value.layers.sortedBy { it.zIndex }.map { it.id }

        assertEquals(listOf(l0, l1, l2), visualBottomToTop())

        vm.moveLayerDown(l2)
        assertEquals(listOf(l0, l2, l1), visualBottomToTop())

        vm.moveLayerUp(l0)
        assertEquals(listOf(l2, l0, l1), visualBottomToTop())

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

        vm.updateLayerPositionWithSnapping(5f, 0f)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertNotNull(vm.uiState.value.activeVerticalGuideX)

        // Frame 4 (+5f -> raw = 20f > 18f threshold -> smoothly breaks free to 20f!)
        vm.updateLayerPositionWithSnapping(5f, 0f)
        assertEquals(20f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertNull(vm.uiState.value.activeVerticalGuideX)

        vm.updateLayerPositionWithSnapping(-12f, 0f)
        assertNotNull(vm.uiState.value.activeVerticalGuideX)
        vm.toggleGrid()
        assertFalse(vm.uiState.value.isGridEnabled)
        assertNull(vm.uiState.value.activeVerticalGuideX)
    }

    @Test
    fun draggingLayerRecordsSingleUndoStepAndRestoresOrigin() {
        val vm = createViewModel()
        assertFalse(vm.uiState.value.canUndo)

        vm.updateLayerPositionWithSnapping(40f, 30f)
        vm.updateLayerPositionWithSnapping(30f, 20f)
        vm.updateLayerPositionWithSnapping(30f, 10f)
        vm.clearSnappingGuides()

        assertEquals(100f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(60f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
        assertTrue("Canvas drag should be undoable", vm.uiState.value.canUndo)

        vm.undo()
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionX, 0.01f)
        assertEquals(0f, vm.uiState.value.selectedLayer!!.positionY, 0.01f)
        assertFalse("Multi-frame drag must coalesce into a single undo entry", vm.uiState.value.canUndo)

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
    fun lockedLayerBlocksMutationsAndLockToggleIsUndoable() {
        val vm = createViewModel()
        val layerId = vm.uiState.value.selectedLayerId!!
        val originalText = vm.uiState.value.selectedLayer!!.text

        // Bug 2.18: toggleLayerLock itself records exactly 1 undo step ("Toggle Lock")
        vm.toggleLayerLock(layerId)
        assertTrue(vm.uiState.value.selectedLayer!!.isLocked)
        assertEquals(listOf("Toggle Lock"), vm.uiState.value.undoHistoryLabels)

        // All blocked operations on a locked layer must be no-ops and NOT add extra undo entries
        vm.nudgeSelectedLayer(50f, 50f)
        vm.alignSelectedLayer(AlignmentAction.ALIGN_LEFT)
        vm.updateSelectedLayer { it.copy(text = "Mutated") }
        vm.updateLayerPositionWithSnapping(80f, 80f)
        vm.deleteSelectedLayer()
        vm.deleteLayer(layerId)

        val after = vm.uiState.value.layers.find { it.id == layerId }
        assertNotNull("Locked layer must not be deleted", after)
        assertEquals(0f, after!!.positionX, 0.01f)
        assertEquals(0f, after.positionY, 0.01f)
        assertEquals(originalText, after.text)
        assertEquals("Blocked operations must not pollute undo stack", listOf("Toggle Lock"), vm.uiState.value.undoHistoryLabels)

        // Undoing unlocks the layer cleanly
        vm.undo()
        assertFalse(vm.uiState.value.selectedLayer!!.isLocked)
        assertFalse(vm.uiState.value.canUndo)
    }

    @Test
    fun pinchRotateGestureSnapsToCardinalAnglesAndTriggersSingleHapticPerSnap() {
        val vm = createViewModel()
        val initialFontSize = vm.uiState.value.selectedLayer!!.fontSize

        // Rotate to 43° -> within ±4° of 45° -> snaps to 45° and returns true for haptic
        val snappedFirst = vm.transformSelectedLayerGesture(
            panDeltaX = 10f,
            panDeltaY = -5f,
            zoomFactor = 1.5f,
            rotationDeltaDeg = 43f
        )
        assertTrue("Entering 45° snap zone should trigger haptic once", snappedFirst)
        assertEquals(45f, vm.uiState.value.selectedLayer!!.rotation, 0.01f)
        assertEquals(initialFontSize * 1.5f, vm.uiState.value.selectedLayer!!.fontSize, 0.05f)

        // Micro-movement +1° (still 44° -> snapped to 45°) should NOT re-trigger haptic spam
        val snappedAgain = vm.transformSelectedLayerGesture(0f, 0f, 1.0f, 1f)
        assertFalse("Staying inside same 45° snap zone must not spam haptics", snappedAgain)
        assertEquals(45f, vm.uiState.value.selectedLayer!!.rotation, 0.01f)

        // Rotate +8° further (raw = 52° > 49°) -> breaks out of 45° snap cleanly
        vm.transformSelectedLayerGesture(0f, 0f, 1.0f, 8f)
        assertEquals(52f, vm.uiState.value.selectedLayer!!.rotation, 0.01f)
        vm.finishContinuousEdit()

        // Single undo restores initial state
        vm.undo()
        assertEquals(0f, vm.uiState.value.selectedLayer!!.rotation, 0.01f)
        assertEquals(initialFontSize, vm.uiState.value.selectedLayer!!.fontSize, 0.05f)
    }

    @Test
    fun styleCopyPasteGroupDragAndTextCaseTransformWorkEndToEnd() {
        val vm = createViewModel()
        vm.updateSelectedLayer(label = "Style Source") {
            it.copy(
                text = "hello pixel world",
                textColor = 0xFF00E5FFL,
                strokeWidth = 8f,
                glowEnabled = true,
                blendMode = LayerBlendMode.SCREEN
            )
        }
        vm.applyTextCase(TextCaseMode.TITLE_CASE)
        assertEquals("Hello Pixel World", vm.uiState.value.selectedLayer!!.text)

        vm.copySelectedStyle()
        val firstId = vm.uiState.value.selectedLayerId!!

        vm.addTextLayer("Second")
        val secondId = vm.uiState.value.selectedLayerId!!
        vm.pasteStyleToSelected()
        val pasted = vm.uiState.value.selectedLayer!!
        assertEquals(0xFF00E5FFL, pasted.textColor)
        assertEquals(8f, pasted.strokeWidth, 0.01f)
        assertTrue(pasted.glowEnabled)
        assertEquals(LayerBlendMode.SCREEN, pasted.blendMode)

        // Multi-select & group both layers -> dragging one moves both together!
        vm.toggleLayerMultiSelection(firstId)
        assertEquals(setOf(firstId, secondId), vm.uiState.value.selectedLayerIds)
        vm.groupSelectedLayers()
        assertNotNull(vm.uiState.value.selectedLayer!!.groupId)

        vm.selectLayer(firstId)
        vm.updateLayerPositionWithSnapping(50f, -30f)
        vm.clearSnappingGuides()

        val movedFirst = vm.uiState.value.layers.first { it.id == firstId }
        val movedSecond = vm.uiState.value.layers.first { it.id == secondId }
        assertEquals(50f, movedFirst.positionX, 0.01f)
        assertEquals(-30f, movedFirst.positionY, 0.01f)
        assertEquals(50f, movedSecond.positionX, 0.01f)
        assertEquals(-30f, movedSecond.positionY, 0.01f)
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
    fun projectSaveIsolationRenameDuplicateUndoDeleteAndDraftLifecycle() {
        val draftFile = File(tempFolder.newFolder(), "draft.json")
        val vm = createViewModel(draftFile)

        vm.saveProject("Poster One")
        awaitCondition { vm.uiState.value.currentProjectId != null && vm.uiState.value.savedProjects.size == 1 }
        val posterOneId = vm.uiState.value.currentProjectId!!
        assertFalse("Saving project clears dirty flag", vm.uiState.value.isDirty)

        vm.applyPreset(DefaultPresets.presets[1])
        assertNull(vm.uiState.value.currentProjectId)
        assertTrue("Applying template in editor marks state dirty", vm.uiState.value.isDirty)

        vm.saveProject("Poster Two")
        awaitCondition {
            vm.uiState.value.currentProjectId != null &&
                vm.uiState.value.currentProjectId != posterOneId &&
                vm.uiState.value.savedProjects.size == 2
        }
        val posterTwoId = vm.uiState.value.currentProjectId!!

        vm.renameSavedProject(posterOneId, "Poster Prime")
        awaitCondition { vm.uiState.value.projectSummaries.any { it.name == "Poster Prime" } }

        vm.duplicateSavedProject(posterTwoId)
        awaitCondition { vm.uiState.value.savedProjects.size == 3 }

        vm.deleteProject(posterTwoId)
        awaitCondition { vm.uiState.value.savedProjects.size == 2 }

        vm.undoDeleteProject()
        awaitCondition { vm.uiState.value.savedProjects.size == 3 }
    }

    @Test
    fun colorMathHsvHexAndComposeUnsignedArgbRoundTripCleanly() {
        assertEquals(0x00000000, ColorMath.scaleAlpha(0x00000000L, 1.0f))

        val scaled = ColorMath.scaleAlpha(0x80FF0055L, 0.5f)
        assertEquals(0x40, (scaled ushr 24) and 0xFF)
        assertEquals(0xFF0055, scaled and 0x00FFFFFF)

        val samples = listOf(
            0xFFFF0000L, 0xFF00FF00L, 0xFF0000FFL, 0xFF00E5FFL,
            0xFFFF007FL, 0xFFFFFFFFL, 0xFF000000L, 0x80651FFFL
        )
        for (sample in samples) {
            val hsv = ColorMath.argbToHsv(sample)
            val alpha = ColorMath.extractAlpha(sample)
            val rebuilt = ColorMath.hsvToArgb(hsv[0], hsv[1], hsv[2], alpha)
            assertEquals("Upper 32 bits must be 0 for Compose Color(Long)", 0L, rebuilt ushr 32)
            val composeColor = Color(rebuilt)
            assertNotNull(composeColor)
            assertEquals((sample ushr 24) and 0xFFL, (rebuilt ushr 24) and 0xFFL)
            assertTrue(kotlin.math.abs(((sample ushr 16) and 0xFFL) - ((rebuilt ushr 16) and 0xFFL)) <= 1L)
            assertTrue(kotlin.math.abs(((sample ushr 8) and 0xFFL) - ((rebuilt ushr 8) and 0xFFL)) <= 1L)
            assertTrue(kotlin.math.abs((sample and 0xFFL) - (rebuilt and 0xFFL)) <= 1L)
        }

        assertEquals("#00E5FF", ColorMath.formatHex(0xFF00E5FFL, includeAlpha = false))
        assertEquals("#8000E5FF", ColorMath.formatHex(0x8000E5FFL, includeAlpha = true))
        assertEquals(0xFF00E5FFL, ColorMath.parseHexOrNull("#00e5ff"))
        assertEquals(0xCCFF0055L, ColorMath.parseHexOrNull("CCFF0055"))
        assertEquals(0x80112233L, ColorMath.parseHexOrNull("#112233", defaultAlpha = 0x80))
        assertNull(ColorMath.parseHexOrNull("#12345"))
        assertNull(ColorMath.parseHexOrNull("#GGGGGG"))
    }

    @Test
    fun textSizingScalesWithBundledFontsLetterSpacingAndStrokePosition() {
        val condensed = TextSizing.withEstimatedBounds(
            Layer(id = "1", name = "Condensed", type = LayerType.TEXT, text = "HEADLINE", fontKey = "bebas_neue", fontSize = 48f)
        )
        val pixelWide = TextSizing.withEstimatedBounds(
            Layer(id = "2", name = "Pixel", type = LayerType.TEXT, text = "HEADLINE", fontKey = "press_start_2p", fontSize = 48f)
        )
        assertTrue("Press Start 2P must estimate wider than Bebas Neue", pixelWide.width > condensed.width)

        val spaced = TextSizing.withEstimatedBounds(
            condensed.copy(letterSpacing = 0.4f)
        )
        assertTrue("Positive letterSpacing must widen text bounds", spaced.width > condensed.width)

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
