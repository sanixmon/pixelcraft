package com.app.pixelcraft

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.domain.ProjectStorage
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * ViewModel-level coordination tests: behaviour that only exists where the
 * ViewModel wires state, selection, clipboard, grouping and history together.
 * Pure domain pieces (EditorHistory, LayerOperations, GroupOperations) are
 * covered in EditorArchitectureTest; end-to-end bug regressions live in
 * EditorBugRegressionTest.
 *
 * Setup notes — do not "simplify" these away:
 * - Robolectric (@RunWith + @Config sdk 35) is REQUIRED: `Application()` in
 *   a plain JVM unit test throws RuntimeException("Stub!") because android.jar
 *   is stubbed; ApplicationProvider gives a real Application.
 * - Dispatchers.setMain(UnconfinedTestDispatcher()) makes viewModelScope
 *   updates apply synchronously so assertions can read uiState immediately.
 * - draftStore = null: DraftController treats a null store as a no-op, so
 *   init's draft check and autosave do no disk I/O.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ViewModelCoordinationTest {

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

    private fun createViewModel(): PixelCraftViewModel {
        val dir = tempFolder.newFolder()
        val app = ApplicationProvider.getApplicationContext<Application>()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PixelCraftViewModel(
                    application = app,
                    projectStorage = ProjectStorage { dir },
                    draftStore = null
                ) as T
            }
        }
        return ViewModelProvider(viewModelStore, factory)["vm_${vmCounter++}", PixelCraftViewModel::class.java]
    }

    private fun addThreeCircles(vm: PixelCraftViewModel): List<String> {
        vm.addShapeLayer(ShapeType.CIRCLE)
        val first = vm.uiState.value.selectedLayerId!!
        vm.addShapeLayer(ShapeType.CIRCLE)
        val second = vm.uiState.value.selectedLayerId!!
        vm.addShapeLayer(ShapeType.CIRCLE)
        val third = vm.uiState.value.selectedLayerId!!
        return listOf(first, second, third)
    }

    @Test
    fun layerClipboardCopyPasteOffsetsCopiesAndAdvancesClipboard() {
        val vm = createViewModel()
        val firstId = vm.uiState.value.layers.single().id
        vm.addShapeLayer(ShapeType.CIRCLE)
        val circleId = vm.uiState.value.selectedLayerId!!

        // Lock the circle: pasted copies must come back unlocked.
        vm.toggleLayerLock(circleId)
        assertTrue(vm.uiState.value.layers.first { it.id == circleId }.isLocked)

        vm.addTextLayer("Second")
        val secondId = vm.uiState.value.selectedLayerId!!

        vm.toggleLayerMultiSelection(circleId)
        assertEquals(setOf(secondId, circleId), vm.uiState.value.selectedLayerIds)

        vm.copySelectedLayers()
        assertEquals(2, vm.uiState.value.layerClipboardCount)

        vm.pasteLayers()
        val state = vm.uiState.value
        assertEquals(5, state.layers.size)
        assertEquals(2, state.layerClipboardCount)

        val originalIds = setOf(firstId, circleId, secondId)
        val pasted = state.layers.filter { it.id !in originalIds }
        assertEquals(2, pasted.size)
        assertTrue("Pasted copies must be unlocked", pasted.none { it.isLocked })
        pasted.forEach {
            assertEquals(30f, it.positionX, 0.01f)
            assertEquals(30f, it.positionY, 0.01f)
        }
        assertEquals(pasted.map { it.id }.toSet(), state.selectedLayerIds)

        // Paste again: the clipboard itself advanced, so copies land a further step away.
        vm.pasteLayers()
        val again = vm.uiState.value
        assertEquals(7, again.layers.size)
        val pastedIds = pasted.map { it.id }.toSet()
        val secondPaste = again.layers.filter { it.id !in originalIds && it.id !in pastedIds }
        assertEquals(2, secondPaste.size)
        secondPaste.forEach {
            assertEquals(60f, it.positionX, 0.01f)
            assertEquals(60f, it.positionY, 0.01f)
        }

        // Each paste is a single undo step.
        vm.undo()
        assertEquals(5, vm.uiState.value.layers.size)
        vm.undo()
        assertEquals(3, vm.uiState.value.layers.size)
    }

    @Test
    fun pasteWithEmptyClipboardIsNoop() {
        val vm = createViewModel()
        assertEquals(0, vm.uiState.value.layerClipboardCount)
        vm.pasteLayers()
        assertEquals(1, vm.uiState.value.layers.size)
        assertFalse(vm.uiState.value.canUndo)
        assertEquals(0, vm.uiState.value.layerClipboardCount)
    }

    @Test
    fun distributeSelectedSpacesThreeLayersEvenlyAndIsUndoable() {
        val vm = createViewModel()
        val ids = addThreeCircles(vm)

        // Uneven gaps (-300, 0, 100): the ends stay, the middle lands at -100.
        vm.selectLayer(ids[0])
        vm.updateSelectedLayer { it.copy(positionX = -300f) }
        vm.selectLayer(ids[1])
        vm.updateSelectedLayer { it.copy(positionX = 0f) }
        vm.selectLayer(ids[2])
        vm.updateSelectedLayer { it.copy(positionX = 100f) }

        vm.toggleLayerMultiSelection(ids[0])
        vm.toggleLayerMultiSelection(ids[1])
        assertEquals(ids.toSet(), vm.uiState.value.selectedLayerIds)

        vm.distributeSelected(horizontal = true)
        val xs = ids.map { id -> vm.uiState.value.layers.first { it.id == id }.positionX }
        assertEquals(-300f, xs[0], 0.01f)
        assertEquals(-100f, xs[1], 0.01f)
        assertEquals(100f, xs[2], 0.01f)

        vm.undo()
        val restored = ids.map { id -> vm.uiState.value.layers.first { it.id == id }.positionX }
        assertEquals(-300f, restored[0], 0.01f)
        assertEquals(0f, restored[1], 0.01f)
        assertEquals(100f, restored[2], 0.01f)
    }

    @Test
    fun jumpToHistoryIndexRestoresOldestStateThenRedoWalksForward() {
        val vm = createViewModel()
        val firstId = vm.uiState.value.layers.single().id
        vm.addShapeLayer(ShapeType.CIRCLE)
        vm.addShapeLayer(ShapeType.SPEECH_BUBBLE)
        assertEquals(3, vm.uiState.value.layers.size)
        assertEquals(listOf("Add Shape", "Add Shape"), vm.uiState.value.undoHistoryLabels)

        vm.jumpToHistoryIndex(0)
        assertEquals(1, vm.uiState.value.layers.size)
        assertEquals(firstId, vm.uiState.value.selectedLayerId)
        assertFalse(vm.uiState.value.canUndo)
        assertTrue(vm.uiState.value.canRedo)

        vm.redo()
        assertEquals(2, vm.uiState.value.layers.size)
        vm.redo()
        assertEquals(3, vm.uiState.value.layers.size)
        assertFalse(vm.uiState.value.canRedo)
    }

    @Test
    fun duplicateSelectedLayerCreatesCopyOnTopAndIsUndoable() {
        val vm = createViewModel()
        val original = vm.uiState.value.selectedLayer!!
        vm.duplicateSelectedLayer()

        val state = vm.uiState.value
        assertEquals(2, state.layers.size)
        val copy = state.layers.first { it.id != original.id }
        assertEquals("${original.name} Copy", copy.name)
        assertTrue(copy.zIndex > original.zIndex)
        assertEquals(copy.id, state.selectedLayerId)
        assertEquals(listOf("Duplicate Layer"), state.undoHistoryLabels)

        vm.undo()
        assertEquals(1, vm.uiState.value.layers.size)
        assertEquals(original.id, vm.uiState.value.selectedLayerId)
    }

    @Test
    fun renameLayerTrimsNameRejectsBlankAndIsUndoable() {
        val vm = createViewModel()
        val layer = vm.uiState.value.selectedLayer!!
        val originalName = layer.name

        vm.renameLayer(layer.id, "  Poster Title  ")
        assertEquals("Poster Title", vm.uiState.value.selectedLayer!!.name)
        assertEquals(listOf("Rename Layer"), vm.uiState.value.undoHistoryLabels)

        vm.undo()
        assertEquals(originalName, vm.uiState.value.selectedLayer!!.name)

        vm.renameLayer(layer.id, "   ")
        assertEquals(originalName, vm.uiState.value.selectedLayer!!.name)
        assertEquals(emptyList<String>(), vm.uiState.value.undoHistoryLabels)

        vm.renameLayer("missing-id", "Nope")
        assertEquals(1, vm.uiState.value.layers.size)
        assertEquals(originalName, vm.uiState.value.selectedLayer!!.name)
    }

    @Test
    fun focusSelectedGroupMemberNarrowsWholeGroupToOneLayer() {
        val vm = createViewModel()
        val firstId = vm.uiState.value.layers.single().id
        vm.addTextLayer("Second")
        val secondId = vm.uiState.value.selectedLayerId!!

        vm.toggleLayerMultiSelection(firstId)
        vm.groupSelectedLayers()
        val groupId = vm.uiState.value.layers.first { it.id == firstId }.groupId
        assertNotNull(groupId)

        // Tapping one member selects the whole group...
        vm.selectLayer(firstId)
        assertEquals(setOf(firstId, secondId), vm.uiState.value.selectedLayerIds)

        // ...until focused, which narrows it to just that member without ungrouping.
        vm.focusSelectedGroupMember()
        assertEquals(setOf(firstId), vm.uiState.value.selectedLayerIds)
        assertEquals(firstId, vm.uiState.value.selectedLayerId)
        assertEquals(groupId, vm.uiState.value.layers.first { it.id == firstId }.groupId)
    }
}
