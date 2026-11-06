package com.app.pixelcraft

import android.app.Application
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.Quote
import com.app.pixelcraft.data.model.ShapeType
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
            "Long subtitle should have estimated width > 300f, got ${subtitle.width}",
            subtitle.width > 350f
        )
    }

    @Test
    fun moveLayerUpAndDownSwapVisualZOrderRegardlessOfListOrder() {
        val vm = createViewModel()
        // Initial layer (L0), add Shape (L1), add Sticker (L2)
        val l0 = vm.uiState.value.layers.single().id
        vm.addShapeLayer(ShapeType.CIRCLE)
        val l1 = vm.uiState.value.selectedLayerId!!
        vm.addStickerLayer("emoji_fire")
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

        // 1. Save "Poster One" and wait until IO completes so currentProjectId is populated
        vm.saveProject("Poster One")
        awaitCondition { vm.uiState.value.currentProjectId != null && vm.uiState.value.savedProjects.size == 1 }
        val posterOneId = vm.uiState.value.currentProjectId!!

        // 2. Apply preset -> must clear currentProjectId so saving "Poster Two" does not overwrite "Poster One"
        vm.applyPreset(DefaultPresets.presets[1])
        assertNull(vm.uiState.value.currentProjectId)

        vm.saveProject("Poster Two")
        awaitCondition {
            vm.uiState.value.currentProjectId != null &&
                vm.uiState.value.currentProjectId != posterOneId &&
                vm.uiState.value.savedProjects.size == 2
        }
        val posterTwoId = vm.uiState.value.currentProjectId!!

        // 3. Re-saving with the same name updates in place
        vm.addTextLayer("Updated")
        vm.saveProject("Poster Two")
        awaitCondition { vm.uiState.value.userMessage == "Saved project \"Poster Two\"" }
        assertEquals(posterTwoId, vm.uiState.value.currentProjectId)
        assertEquals(2, vm.uiState.value.savedProjects.size)

        // 4. Saving under a new name ("Save As") creates a distinct project without overwriting "Poster Two"
        vm.saveProject("Poster Three")
        awaitCondition { vm.uiState.value.savedProjects.size == 3 }
        assertEquals(
            setOf("Poster One", "Poster Two", "Poster Three"),
            vm.uiState.value.savedProjects.map { it.name }.toSet()
        )

        // 5. Deleting the active project clears currentProjectId
        val posterThreeId = vm.uiState.value.currentProjectId!!
        vm.deleteProject(posterThreeId)
        awaitCondition { vm.uiState.value.savedProjects.size == 2 && vm.uiState.value.currentProjectId == null }
    }

    @Test
    fun colorMathPreservesTransparentFillAndScalesExistingAlpha() {
        // Transparent shapeFillColor (0x00000000) must stay alpha 0 even when layer opacity is 1.0f
        assertEquals(0x00000000, ColorMath.scaleAlpha(0x00000000L, 1.0f))

        // Semi-transparent 0x80FF0055 at 50% layer opacity -> alpha 0x40 (64), RGB unchanged
        val scaled = ColorMath.scaleAlpha(0x80FF0055L, 0.5f)
        assertEquals(0x40, (scaled ushr 24) and 0xFF)
        assertEquals(0xFF0055, scaled and 0x00FFFFFF)

        // Darken 0xFFFF8000 by 50% at 50% opacity
        val darkened = ColorMath.darkenArgb(0xFFFF8000L, 0.5f, 0.5f)
        assertEquals(128, (darkened ushr 24) and 0xFF)
        assertEquals(128, (darkened ushr 16) and 0xFF)
        assertEquals(64, (darkened ushr 8) and 0xFF)
        assertEquals(0, darkened and 0xFF)
    }

    @Test
    fun textSizingGrowsWithContentAndWrapsQuotes() {
        val short = TextSizing.withEstimatedBounds(
            Layer(id = "1", name = "Short", type = LayerType.TEXT, text = "Hi", fontSize = 32f)
        )
        val wide = TextSizing.withEstimatedBounds(
            Layer(id = "2", name = "Wide", type = LayerType.TEXT, text = "SUPER WIDE HEADLINE BANNER", fontSize = 64f)
        )
        val multiline = TextSizing.withEstimatedBounds(
            Layer(id = "3", name = "Multi", type = LayerType.TEXT, text = "Line 1\nLine 2\nLine 3\nLine 4", fontSize = 48f)
        )
        assertTrue(wide.width > short.width * 3f)
        assertTrue(multiline.height > short.height * 2.5f)

        val quote = Quote(
            text = "The only way to do great work is to love what you do every single day.",
            author = "Steve Jobs"
        )
        val wrapped = TextSizing.wrapQuote(quote, maxCharsPerLine = 26)
        val lines = wrapped.lines()
        assertTrue("Long quote should wrap into multiple lines", lines.size >= 4)
        assertEquals("- Steve Jobs", lines.last())
    }
}
