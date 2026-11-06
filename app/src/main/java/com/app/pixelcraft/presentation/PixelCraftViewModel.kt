package com.app.pixelcraft.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.DrawPath
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.Quote
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.GallerySaver
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.ImageFit
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.SnappingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PixelCraftViewModel(application: Application) : AndroidViewModel(application) {

    private val projectStorage = ProjectStorage(application)
    private val imageExporter = ImageExporter()
    private val imageStore = ImageStore(application)
    private val gallerySaver = GallerySaver(application)

    private val _uiState = MutableStateFlow(PixelCraftUiState())
    val uiState: StateFlow<PixelCraftUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()

    init {
        // Apply default preset on launch
        val defaultPreset = DefaultPresets.presets.first()
        applyPreset(defaultPreset, recordHistory = false)
        refreshSavedProjects()
    }

    private fun takeSnapshot(): EditorSnapshot {
        val state = _uiState.value
        return EditorSnapshot(
            layers = state.layers,
            canvasConfig = state.canvasConfig,
            selectedLayerId = state.selectedLayerId
        )
    }

    private fun recordHistory() {
        undoStack.add(takeSnapshot())
        if (undoStack.size > 50) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val currentState = takeSnapshot()
        redoStack.add(currentState)
        val previousState = undoStack.removeAt(undoStack.lastIndex)

        _uiState.update {
            it.copy(
                layers = previousState.layers,
                canvasConfig = previousState.canvasConfig,
                selectedLayerId = previousState.selectedLayerId
            )
        }
        updateHistoryFlags()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val currentState = takeSnapshot()
        undoStack.add(currentState)
        val nextState = redoStack.removeAt(redoStack.lastIndex)

        _uiState.update {
            it.copy(
                layers = nextState.layers,
                canvasConfig = nextState.canvasConfig,
                selectedLayerId = nextState.selectedLayerId
            )
        }
        updateHistoryFlags()
    }

    // --- Tab and Dialog Management ---

    fun setActiveTab(tab: com.app.pixelcraft.data.model.BottomNavTab) {
        _uiState.update { it.copy(activeTab = tab, activeSubTool = ActiveSubTool.NONE) }
    }

    fun setActiveSubTool(subTool: ActiveSubTool) {
        _uiState.update { it.copy(activeSubTool = subTool) }
    }

    fun openDialog(dialog: ActiveDialog) {
        _uiState.update { it.copy(activeDialog = dialog) }
    }

    fun closeDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun toggleLayersDrawer() {
        _uiState.update { it.copy(isLayersDrawerOpen = !it.isLayersDrawerOpen) }
    }

    fun setLayersDrawerOpen(open: Boolean) {
        _uiState.update { it.copy(isLayersDrawerOpen = open) }
    }

    // --- Grid & Zoom ---

    fun toggleGrid() {
        _uiState.update { it.copy(isGridEnabled = !it.isGridEnabled) }
    }

    fun toggleSnapToGrid() {
        _uiState.update { it.copy(isSnapToGrid = !it.isSnapToGrid) }
    }

    fun toggleZoomMode() {
        _uiState.update { it.copy(isZoomMode = !it.isZoomMode) }
    }

    fun resetZoom() {
        _uiState.update { it.copy(zoomScale = 1.0f, panOffset = PointF(0f, 0f)) }
    }

    fun updateZoomAndPan(scaleMultiplier: Float, panDelta: PointF) {
        _uiState.update { current ->
            val newScale = (current.zoomScale * scaleMultiplier).coerceIn(0.5f, 5.0f)
            val newPanX = current.panOffset.x + panDelta.x
            val newPanY = current.panOffset.y + panDelta.y
            current.copy(zoomScale = newScale, panOffset = PointF(newPanX, newPanY))
        }
    }

    // --- Layer Selection & Navigation ---

    fun selectLayer(layerId: String?) {
        _uiState.update { state ->
            val layer = state.layers.find { it.id == layerId }
            val newTab = when (layer?.type) {
                LayerType.TEXT -> com.app.pixelcraft.data.model.BottomNavTab.TEXT
                LayerType.SHAPE, LayerType.STICKER, LayerType.DRAWING, LayerType.IMAGE -> com.app.pixelcraft.data.model.BottomNavTab.OBJECTS
                null -> state.activeTab
            }
            state.copy(selectedLayerId = layerId, activeTab = newTab)
        }
    }

    // --- Layer Operations ---

    fun addTextLayer(initialText: String = "New Text") {
        recordHistory()
        val newZIndex = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Text $newZIndex",
            type = LayerType.TEXT,
            text = initialText,
            fontSize = 44f,
            textColor = 0xFFFFFFFF,
            positionX = 0f,
            positionY = 0f,
            zIndex = newZIndex
        )
        _uiState.update { state ->
            state.copy(
                layers = state.layers + newLayer,
                selectedLayerId = newLayer.id,
                activeTab = com.app.pixelcraft.data.model.BottomNavTab.TEXT
            )
        }
    }

    fun addCurrentDateLayer() {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        addTextLayer(dateString)
    }

    fun addQuoteLayer(quote: Quote) {
        val text = "\"${quote.text}\"\n- ${quote.author.ifBlank { "Unknown" }}"
        addTextLayer(text)
    }

    fun addShapeLayer(shapeType: ShapeType) {
        recordHistory()
        val newZIndex = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Shape $newZIndex",
            type = LayerType.SHAPE,
            shapeType = shapeType,
            width = 320f,
            height = 320f,
            shapeFillColor = 0xFF00B0FF,
            positionX = 0f,
            positionY = 0f,
            zIndex = newZIndex
        )
        _uiState.update { state ->
            state.copy(
                layers = state.layers + newLayer,
                selectedLayerId = newLayer.id,
                activeTab = com.app.pixelcraft.data.model.BottomNavTab.OBJECTS
            )
        }
    }

    fun addStickerLayer(stickerKey: String) {
        recordHistory()
        val newZIndex = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Sticker $newZIndex",
            type = LayerType.STICKER,
            stickerKey = stickerKey,
            fontSize = 64f,
            width = 160f,
            height = 160f,
            positionX = 0f,
            positionY = 0f,
            zIndex = newZIndex
        )
        _uiState.update { state ->
            state.copy(
                layers = state.layers + newLayer,
                selectedLayerId = newLayer.id,
                activeTab = com.app.pixelcraft.data.model.BottomNavTab.OBJECTS
            )
        }
    }

    /** Adds a picked photo as a movable layer, sized to fit within 60% of the canvas. */
    fun addImageLayer(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { image ->
                recordHistory()
                val config = _uiState.value.canvasConfig
                val fit = ImageFit.fitWithin(image.width, image.height, config.width * 0.6f, config.height * 0.6f)
                val newZIndex = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1
                val newLayer = Layer(
                    id = UUID.randomUUID().toString(),
                    name = "Photo $newZIndex",
                    type = LayerType.IMAGE,
                    imageLocalPath = image.path,
                    width = fit.width,
                    height = fit.height,
                    zIndex = newZIndex
                )
                _uiState.update { state ->
                    state.copy(
                        layers = state.layers + newLayer,
                        selectedLayerId = newLayer.id,
                        activeTab = com.app.pixelcraft.data.model.BottomNavTab.OBJECTS
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = "Couldn't open that picture: ${err.message}") }
            }
        }
    }

    fun updateSelectedLayer(transform: (Layer) -> Layer) {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordHistory()
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id == selectedId) transform(layer) else layer
                }
            )
        }
    }

    fun updateLayerPositionWithSnapping(deltaX: Float, deltaY: Float) {
        val selectedId = _uiState.value.selectedLayerId ?: return
        val currentLayer = _uiState.value.layers.find { it.id == selectedId } ?: return
        if (currentLayer.isLocked) return

        val state = _uiState.value
        val rawX = currentLayer.positionX + deltaX
        val rawY = currentLayer.positionY + deltaY

        val canvasCenter = PointF(state.canvasConfig.width / 2f, state.canvasConfig.height / 2f)
        val gridSize = state.canvasConfig.width / (state.gridColumns + 1f)

        val snapResult = SnappingEngine.calculateSnap(
            rawPosition = PointF(rawX, rawY),
            canvasCenter = canvasCenter,
            gridSize = gridSize,
            isSnapToGrid = state.isSnapToGrid
        )

        _uiState.update { s ->
            s.copy(
                layers = s.layers.map {
                    if (it.id == selectedId) {
                        it.copy(
                            positionX = snapResult.snappedPosition.x,
                            positionY = snapResult.snappedPosition.y
                        )
                    } else it
                },
                showVerticalGuide = snapResult.showVerticalGuide,
                verticalGuideX = snapResult.guideX,
                showHorizontalGuide = snapResult.showHorizontalGuide,
                horizontalGuideY = snapResult.guideY
            )
        }
    }

    fun clearSnappingGuides() {
        _uiState.update {
            it.copy(showVerticalGuide = false, showHorizontalGuide = false)
        }
    }

    fun nudgeSelectedLayer(dx: Float, dy: Float) {
        updateSelectedLayer {
            it.copy(positionX = it.positionX + dx, positionY = it.positionY + dy)
        }
    }

    fun alignSelectedLayer(action: AlignmentAction) {
        val state = _uiState.value
        val canvasW = state.canvasConfig.width.toFloat()
        val canvasH = state.canvasConfig.height.toFloat()

        updateSelectedLayer { layer ->
            val halfW = (layer.width * layer.scaleX) / 2f
            val halfH = (layer.height * layer.scaleY) / 2f

            when (action) {
                AlignmentAction.CENTER_HORIZONTAL -> layer.copy(positionX = 0f)
                AlignmentAction.CENTER_VERTICAL -> layer.copy(positionY = 0f)
                AlignmentAction.ALIGN_LEFT -> layer.copy(positionX = -canvasW / 2f + halfW)
                AlignmentAction.ALIGN_RIGHT -> layer.copy(positionX = canvasW / 2f - halfW)
                AlignmentAction.ALIGN_TOP -> layer.copy(positionY = -canvasH / 2f + halfH)
                AlignmentAction.ALIGN_BOTTOM -> layer.copy(positionY = canvasH / 2f - halfH)
            }
        }
    }

    fun deleteSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordHistory()
        _uiState.update { state ->
            val remaining = state.layers.filter { it.id != selectedId }
            state.copy(
                layers = remaining,
                selectedLayerId = remaining.lastOrNull()?.id
            )
        }
    }

    fun duplicateSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        val layer = _uiState.value.layers.find { it.id == selectedId } ?: return
        recordHistory()

        val newZIndex = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0) + 1
        val copy = layer.copy(
            id = UUID.randomUUID().toString(),
            name = "${layer.name} Copy",
            positionX = layer.positionX + 25f,
            positionY = layer.positionY + 25f,
            zIndex = newZIndex
        )

        _uiState.update { state ->
            state.copy(
                layers = state.layers + copy,
                selectedLayerId = copy.id
            )
        }
    }

    fun bringLayerToFront() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordHistory()
        _uiState.update { state ->
            val maxZ = state.layers.maxOfOrNull { it.zIndex } ?: 0
            state.copy(
                layers = state.layers.map {
                    if (it.id == selectedId) it.copy(zIndex = maxZ + 1) else it
                }
            )
        }
    }

    fun sendLayerToBack() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        recordHistory()
        _uiState.update { state ->
            val minZ = state.layers.minOfOrNull { it.zIndex } ?: 0
            state.copy(
                layers = state.layers.map {
                    if (it.id == selectedId) it.copy(zIndex = minZ - 1) else it
                }
            )
        }
    }

    fun toggleLayerVisibility(layerId: String) {
        recordHistory()
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                }
            )
        }
    }

    fun toggleLayerLock(layerId: String) {
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
                }
            )
        }
    }

    fun reorderLayers(fromIndex: Int, toIndex: Int) {
        recordHistory()
        _uiState.update { state ->
            val list = state.layers.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                val updatedWithZ = list.mapIndexed { index, layer ->
                    layer.copy(zIndex = index)
                }
                state.copy(layers = updatedWithZ)
            } else {
                state
            }
        }
    }

    // --- Canvas & Background Settings ---

    fun setAspectRatioPreset(preset: AspectRatioPreset, customW: Int? = null, customH: Int? = null) {
        recordHistory()
        val w = customW ?: preset.defaultWidth
        val h = customH ?: preset.defaultHeight
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    preset = preset,
                    width = w,
                    height = h
                )
            )
        }
    }

    fun setBackgroundColor(color: Long) {
        recordHistory()
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.SOLID_COLOR,
                    backgroundColor = color
                )
            )
        }
    }

    fun setBackgroundGradient(colors: List<Long>, angle: Float = 45f) {
        recordHistory()
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.GRADIENT,
                    gradientColors = colors,
                    gradientAngle = angle
                )
            )
        }
    }

    /** Uses a picked photo as the canvas background (centre-cropped to fill it). */
    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { image ->
                recordHistory()
                _uiState.update { state ->
                    state.copy(
                        canvasConfig = state.canvasConfig.copy(
                            backgroundType = BackgroundType.CUSTOM_IMAGE,
                            customImageUri = image.path
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = "Couldn't open that picture: ${err.message}") }
            }
        }
    }

    fun setBackgroundTransparent() {
        recordHistory()
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.TRANSPARENT
                )
            )
        }
    }

    fun updateCanvasFilters(transform: (CanvasFilters) -> CanvasFilters) {
        recordHistory()
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    filters = transform(state.canvasConfig.filters)
                )
            )
        }
    }

    // --- Presets ---

    fun applyPreset(preset: StylePreset, recordHistory: Boolean = true) {
        if (recordHistory) recordHistory()
        _uiState.update { state ->
            state.copy(
                canvasConfig = preset.canvasConfig,
                layers = preset.initialLayers,
                selectedLayerId = preset.initialLayers.firstOrNull()?.id,
                currentProjectName = preset.title
            )
        }
    }

    // --- Projects Save / Load ---

    fun refreshSavedProjects() {
        viewModelScope.launch {
            projectStorage.listProjects().onSuccess { projects ->
                _uiState.update { it.copy(savedProjects = projects) }
            }
        }
    }

    fun saveProject(name: String) {
        val state = _uiState.value
        val id = state.currentProjectId ?: UUID.randomUUID().toString()
        val project = ProjectData(
            id = id,
            name = name,
            timestamp = System.currentTimeMillis(),
            snapshot = takeSnapshot()
        )
        viewModelScope.launch {
            projectStorage.saveProject(project).onSuccess {
                _uiState.update {
                    it.copy(
                        currentProjectId = id,
                        currentProjectName = name,
                        activeDialog = null
                    )
                }
                refreshSavedProjects()
            }
        }
    }

    fun loadProject(id: String) {
        recordHistory()
        viewModelScope.launch {
            projectStorage.loadProject(id).onSuccess { project ->
                _uiState.update {
                    it.copy(
                        currentProjectId = project.id,
                        currentProjectName = project.name,
                        canvasConfig = project.snapshot.canvasConfig,
                        layers = project.snapshot.layers,
                        selectedLayerId = project.snapshot.selectedLayerId,
                        activeDialog = null
                    )
                }
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectStorage.deleteProject(id).onSuccess {
                refreshSavedProjects()
            }
        }
    }

    // --- Export ---

    fun setExportOptions(options: ExportOptions) {
        _uiState.update { it.copy(exportOptions = options) }
    }

    fun exportCanvas() {
        val state = _uiState.value
        _uiState.update { it.copy(isExporting = true) }

        viewModelScope.launch {
            runCatching {
                val bitmap = imageExporter.exportCanvasToBitmap(
                    canvasConfig = state.canvasConfig,
                    layers = state.layers,
                    options = state.exportOptions
                )
                val baseName = state.currentProjectName.replace("[^A-Za-z0-9_-]+".toRegex(), "_") +
                    "_" + System.currentTimeMillis()
                gallerySaver.save(bitmap, state.exportOptions, baseName).getOrThrow()
            }.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportedUri = uri,
                        userMessage = "Saved to Pictures/${GallerySaver.ALBUM}",
                        activeDialog = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportedUri = null,
                        userMessage = "Export failed: ${err.message}"
                    )
                }
            }
        }
    }

    fun reportMessage(message: String) {
        _uiState.update { it.copy(userMessage = message) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, lastExportedUri = null) }
    }
}
