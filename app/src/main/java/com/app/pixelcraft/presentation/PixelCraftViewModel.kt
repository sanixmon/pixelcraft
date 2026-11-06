package com.app.pixelcraft.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.GallerySaver
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.ImageFit
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.SnappingEngine
import com.app.pixelcraft.domain.TextSizing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PixelCraftViewModel internal constructor(
    application: Application,
    private val projectStorage: ProjectStorage
) : AndroidViewModel(application) {

    constructor(application: Application) : this(application, ProjectStorage(application))

    companion object {
        const val MIN_CANVAS_DIMENSION = 64
        const val MAX_CANVAS_DIMENSION = 4096
    }

    private val imageExporter = ImageExporter(application)
    private val imageStore = ImageStore(application)
    private val gallerySaver = GallerySaver(application)

    private val _uiState = MutableStateFlow(PixelCraftUiState())
    val uiState: StateFlow<PixelCraftUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<EditorSnapshot>()
    private val redoStack = mutableListOf<EditorSnapshot>()
    private var isContinuousEditing = false

    // Tracks true un-snapped finger trajectory during active drag so Grid snapping never sticks
    private var rawDragPosition: PointF? = null
    private var rawDragLayerId: String? = null

    init {
        val defaultPreset = DefaultPresets.presets.first()
        applyPreset(defaultPreset, recordHistory = false)
        refreshSavedProjects()
    }

    private fun resetDragTracking() {
        rawDragPosition = null
        rawDragLayerId = null
    }

    private fun normalizeZIndices(layers: List<Layer>): List<Layer> {
        return layers.sortedBy { it.zIndex }.mapIndexed { index, layer ->
            layer.copy(zIndex = index)
        }
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

    private fun beginHistoryStep(continuous: Boolean) {
        if (continuous) {
            if (!isContinuousEditing) {
                isContinuousEditing = true
                recordHistory()
            }
        } else {
            isContinuousEditing = false
            recordHistory()
        }
    }

    fun finishContinuousEdit() {
        isContinuousEditing = false
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
        isContinuousEditing = false
        resetDragTracking()
        if (undoStack.isEmpty()) return
        val currentState = takeSnapshot()
        redoStack.add(currentState)
        val previousState = undoStack.removeAt(undoStack.lastIndex)

        _uiState.update {
            it.copy(
                layers = previousState.layers,
                canvasConfig = previousState.canvasConfig,
                selectedLayerId = previousState.selectedLayerId,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        updateHistoryFlags()
    }

    fun redo() {
        isContinuousEditing = false
        resetDragTracking()
        if (redoStack.isEmpty()) return
        val currentState = takeSnapshot()
        undoStack.add(currentState)
        val nextState = redoStack.removeAt(redoStack.lastIndex)

        _uiState.update {
            it.copy(
                layers = nextState.layers,
                canvasConfig = nextState.canvasConfig,
                selectedLayerId = nextState.selectedLayerId,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        updateHistoryFlags()
    }

    // --- Tab and Dialog Management ---

    fun setActiveTab(tab: BottomNavTab) {
        isContinuousEditing = false
        _uiState.update { it.copy(activeTab = tab, activeSubTool = ActiveSubTool.NONE) }
    }

    fun setActiveSubTool(subTool: ActiveSubTool) {
        isContinuousEditing = false
        _uiState.update { it.copy(activeSubTool = subTool) }
    }

    fun openDialog(dialog: ActiveDialog) {
        isContinuousEditing = false
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

    // --- Grid ---

    fun toggleGrid() {
        resetDragTracking()
        _uiState.update { state ->
            val nextGrid = !state.isGridEnabled
            state.copy(
                isGridEnabled = nextGrid,
                activeVerticalGuideX = if (nextGrid) state.activeVerticalGuideX else null,
                activeHorizontalGuideY = if (nextGrid) state.activeHorizontalGuideY else null
            )
        }
    }

    // --- Layer Selection & Navigation ---

    fun selectLayer(layerId: String?) {
        isContinuousEditing = false
        resetDragTracking()
        _uiState.update { state ->
            val layer = state.layers.find { it.id == layerId }
            val newTab = when (layer?.type) {
                LayerType.TEXT -> BottomNavTab.TEXT
                LayerType.SHAPE, LayerType.IMAGE -> BottomNavTab.OBJECTS
                null -> state.activeTab
            }
            val nextSubTool = if (newTab != state.activeTab || layer?.type != state.selectedLayer?.type) {
                ActiveSubTool.NONE
            } else {
                state.activeSubTool
            }
            state.copy(
                selectedLayerId = layerId,
                activeTab = newTab,
                activeSubTool = nextSubTool,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
    }

    // --- Layer Operations ---

    fun addTextLayer(initialText: String = "New Text") {
        beginHistoryStep(continuous = false)
        resetDragTracking()
        val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
        val labelIndex = _uiState.value.layers.count { it.type == LayerType.TEXT } + 1
        val newLayer = TextSizing.withEstimatedBounds(
            Layer(
                id = UUID.randomUUID().toString(),
                name = "Text $labelIndex",
                type = LayerType.TEXT,
                text = initialText,
                fontKey = "poppins",
                fontSize = 44f,
                textColor = 0xFFFFFFFF,
                positionX = 0f,
                positionY = 0f,
                zIndex = nextZ
            )
        )
        _uiState.update { state ->
            state.copy(
                layers = normalizeZIndices(state.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTab = BottomNavTab.TEXT,
                activeSubTool = ActiveSubTool.NONE
            )
        }
    }

    fun addCurrentDateLayer() {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        val dateString = dateFormat.format(Date())
        addTextLayer(dateString)
    }

    fun addShapeLayer(shapeType: ShapeType) {
        beginHistoryStep(continuous = false)
        resetDragTracking()
        val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
        val labelIndex = _uiState.value.layers.count { it.type == LayerType.SHAPE } + 1
        val isWordBalloon = shapeType.category == ShapeCategory.BALLOONS
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "${shapeType.label} $labelIndex",
            type = LayerType.SHAPE,
            shapeType = shapeType,
            width = shapeType.defaultWidth,
            height = shapeType.defaultHeight,
            shapeFillColor = if (isWordBalloon) 0xFFFFFFFF else 0xFF00B0FF,
            shapeStrokeColor = if (isWordBalloon) 0xFF111111 else 0xFFFFFFFF,
            shapeStrokeWidth = if (isWordBalloon) 5f else 0f,
            strokePosition = StrokePosition.OUTSIDE,
            positionX = 0f,
            positionY = 0f,
            zIndex = nextZ
        )
        _uiState.update { state ->
            state.copy(
                layers = normalizeZIndices(state.layers + newLayer),
                selectedLayerId = newLayer.id,
                activeTab = BottomNavTab.OBJECTS,
                activeSubTool = ActiveSubTool.NONE
            )
        }
    }

    /** Adds a picked photo as a movable layer, sized to fit within 60% of the canvas. */
    fun addImageLayer(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { image ->
                beginHistoryStep(continuous = false)
                resetDragTracking()
                val config = _uiState.value.canvasConfig
                val fit = ImageFit.fitWithin(image.width, image.height, config.width * 0.6f, config.height * 0.6f)
                val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
                val labelIndex = _uiState.value.layers.count { it.type == LayerType.IMAGE } + 1
                val newLayer = Layer(
                    id = UUID.randomUUID().toString(),
                    name = "Photo $labelIndex",
                    type = LayerType.IMAGE,
                    imageLocalPath = image.path,
                    width = fit.width,
                    height = fit.height,
                    zIndex = nextZ
                )
                _uiState.update { state ->
                    state.copy(
                        layers = normalizeZIndices(state.layers + newLayer),
                        selectedLayerId = newLayer.id,
                        activeTab = BottomNavTab.OBJECTS,
                        activeSubTool = ActiveSubTool.NONE
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = "Couldn't open that picture: ${err.message}") }
            }
        }
    }

    fun updateSelectedLayer(continuous: Boolean = false, transform: (Layer) -> Layer) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return

        beginHistoryStep(continuous = continuous)
        val selectedId = currentLayer.id
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id == selectedId) {
                        TextSizing.withEstimatedBounds(transform(layer))
                    } else {
                        layer
                    }
                }
            )
        }
    }

    fun updateLayerPositionWithSnapping(deltaX: Float, deltaY: Float) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return

        beginHistoryStep(continuous = true)
        val selectedId = currentLayer.id
        val baseRaw = if (rawDragLayerId == selectedId && rawDragPosition != null) {
            rawDragPosition!!
        } else {
            PointF(currentLayer.positionX, currentLayer.positionY)
        }
        val nextRaw = PointF(baseRaw.x + deltaX, baseRaw.y + deltaY)
        rawDragLayerId = selectedId
        rawDragPosition = nextRaw

        val stateSnapshot = _uiState.value
        if (!stateSnapshot.isGridEnabled) {
            // Freemove default: no magnetic anchor snapping when Grid is disabled
            _uiState.update { s ->
                s.copy(
                    layers = s.layers.map {
                        if (it.id == selectedId) {
                            it.copy(
                                positionX = nextRaw.x,
                                positionY = nextRaw.y
                            )
                        } else it
                    },
                    activeVerticalGuideX = null,
                    activeHorizontalGuideY = null
                )
            }
        } else {
            val snapResult = SnappingEngine.calculateSnap(
                rawPosition = nextRaw,
                canvasWidth = stateSnapshot.canvasConfig.width.toFloat(),
                canvasHeight = stateSnapshot.canvasConfig.height.toFloat(),
                gridColumns = stateSnapshot.gridColumns,
                gridRows = stateSnapshot.gridRows
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
                    activeVerticalGuideX = snapResult.verticalGuideX,
                    activeHorizontalGuideY = snapResult.horizontalGuideY
                )
            }
        }
    }

    fun clearSnappingGuides() {
        isContinuousEditing = false
        resetDragTracking()
        _uiState.update {
            it.copy(activeVerticalGuideX = null, activeHorizontalGuideY = null)
        }
    }

    fun nudgeSelectedLayer(dx: Float, dy: Float) {
        resetDragTracking()
        updateSelectedLayer(continuous = false) {
            it.copy(positionX = it.positionX + dx, positionY = it.positionY + dy)
        }
    }

    fun alignSelectedLayer(action: AlignmentAction) {
        resetDragTracking()
        val state = _uiState.value
        val canvasW = state.canvasConfig.width.toFloat()
        val canvasH = state.canvasConfig.height.toFloat()

        updateSelectedLayer(continuous = false) { layer ->
            val shapeOuterStroke = if (layer.type == LayerType.SHAPE && layer.shapeStrokeWidth > 0f) {
                when (layer.strokePosition) {
                    StrokePosition.OUTSIDE -> layer.shapeStrokeWidth
                    StrokePosition.CENTER -> layer.shapeStrokeWidth / 2f
                    StrokePosition.INSIDE -> 0f
                }
            } else {
                0f
            }
            val halfW = (layer.width / 2f) + shapeOuterStroke
            val halfH = (layer.height / 2f) + shapeOuterStroke

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

    fun deleteLayer(layerId: String) {
        val target = _uiState.value.layers.find { it.id == layerId } ?: return
        if (target.isLocked) return

        beginHistoryStep(continuous = false)
        resetDragTracking()
        _uiState.update { state ->
            val remaining = normalizeZIndices(state.layers.filter { it.id != layerId })
            val nextSelected = if (state.selectedLayerId == layerId) {
                remaining.maxByOrNull { it.zIndex }?.id
            } else {
                state.selectedLayerId
            }
            state.copy(
                layers = remaining,
                selectedLayerId = nextSelected,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
    }

    fun deleteSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        deleteLayer(selectedId)
    }

    fun duplicateSelectedLayer() {
        val layer = _uiState.value.selectedLayer ?: return
        beginHistoryStep(continuous = false)
        resetDragTracking()

        val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
        val copy = layer.copy(
            id = UUID.randomUUID().toString(),
            name = "${layer.name} Copy",
            isLocked = false,
            positionX = layer.positionX + 25f,
            positionY = layer.positionY + 25f,
            zIndex = nextZ
        )

        _uiState.update { state ->
            state.copy(
                layers = normalizeZIndices(state.layers + copy),
                selectedLayerId = copy.id
            )
        }
    }

    fun bringLayerToFront() {
        val current = _uiState.value.selectedLayer ?: return
        if (current.isLocked) return
        val maxZ = _uiState.value.layers.maxOfOrNull { it.zIndex } ?: 0
        if (current.zIndex == maxZ && _uiState.value.layers.count { it.zIndex == maxZ } == 1) return

        beginHistoryStep(continuous = false)
        val selectedId = current.id
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == selectedId) it.copy(zIndex = maxZ + 1) else it
            }
            state.copy(layers = normalizeZIndices(updated))
        }
    }

    fun sendLayerToBack() {
        val current = _uiState.value.selectedLayer ?: return
        if (current.isLocked) return
        val minZ = _uiState.value.layers.minOfOrNull { it.zIndex } ?: 0
        if (current.zIndex == minZ && _uiState.value.layers.count { it.zIndex == minZ } == 1) return

        beginHistoryStep(continuous = false)
        val selectedId = current.id
        _uiState.update { state ->
            val updated = state.layers.map {
                if (it.id == selectedId) it.copy(zIndex = minZ - 1) else it
            }
            state.copy(layers = normalizeZIndices(updated))
        }
    }

    /** Moves [layerId] one step forward (higher `zIndex`) in visual order. */
    fun moveLayerUp(layerId: String) {
        val sortedAsc = normalizeZIndices(_uiState.value.layers).toMutableList()
        val idx = sortedAsc.indexOfFirst { it.id == layerId }
        if (idx < 0 || idx >= sortedAsc.lastIndex) return

        beginHistoryStep(continuous = false)
        val tmp = sortedAsc[idx]
        sortedAsc[idx] = sortedAsc[idx + 1]
        sortedAsc[idx + 1] = tmp

        val reindexed = sortedAsc.mapIndexed { i, layer -> layer.copy(zIndex = i) }
        _uiState.update { it.copy(layers = reindexed) }
    }

    /** Moves [layerId] one step backward (lower `zIndex`) in visual order. */
    fun moveLayerDown(layerId: String) {
        val sortedAsc = normalizeZIndices(_uiState.value.layers).toMutableList()
        val idx = sortedAsc.indexOfFirst { it.id == layerId }
        if (idx <= 0) return

        beginHistoryStep(continuous = false)
        val tmp = sortedAsc[idx]
        sortedAsc[idx] = sortedAsc[idx - 1]
        sortedAsc[idx - 1] = tmp

        val reindexed = sortedAsc.mapIndexed { i, layer -> layer.copy(zIndex = i) }
        _uiState.update { it.copy(layers = reindexed) }
    }

    fun toggleLayerVisibility(layerId: String) {
        if (_uiState.value.layers.none { it.id == layerId }) return
        beginHistoryStep(continuous = false)
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                }
            )
        }
    }

    fun toggleLayerLock(layerId: String) {
        isContinuousEditing = false
        resetDragTracking()
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
                }
            )
        }
    }

    // --- Canvas & Background Settings ---

    fun setAspectRatioPreset(preset: AspectRatioPreset, customW: Int? = null, customH: Int? = null) {
        beginHistoryStep(continuous = false)
        val w = (customW ?: preset.defaultWidth).coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
        val h = (customH ?: preset.defaultHeight).coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
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
        beginHistoryStep(continuous = false)
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
        beginHistoryStep(continuous = false)
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
                beginHistoryStep(continuous = false)
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
        beginHistoryStep(continuous = false)
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.TRANSPARENT
                )
            )
        }
    }

    fun updateCanvasFilters(continuous: Boolean = false, transform: (CanvasFilters) -> CanvasFilters) {
        beginHistoryStep(continuous = continuous)
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
        isContinuousEditing = false
        resetDragTracking()
        if (recordHistory) recordHistory()
        val normalizedLayers = normalizeZIndices(
            preset.initialLayers.map { TextSizing.withEstimatedBounds(it) }
        )
        val topLayer = normalizedLayers.maxByOrNull { it.zIndex }
        _uiState.update { state ->
            state.copy(
                canvasConfig = preset.canvasConfig,
                layers = normalizedLayers,
                selectedLayerId = topLayer?.id,
                currentProjectId = null,
                currentProjectName = preset.title,
                activeSubTool = ActiveSubTool.NONE,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
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
        val cleanName = name.trim().ifBlank { "Untitled Project" }
        val reuseExistingId = state.currentProjectId != null && cleanName == state.currentProjectName
        val id = if (reuseExistingId) state.currentProjectId!! else UUID.randomUUID().toString()
        val project = ProjectData(
            id = id,
            name = cleanName,
            timestamp = System.currentTimeMillis(),
            snapshot = takeSnapshot()
        )
        viewModelScope.launch {
            projectStorage.saveProject(project).onSuccess {
                _uiState.update {
                    it.copy(
                        currentProjectId = id,
                        currentProjectName = cleanName,
                        activeDialog = null,
                        userMessage = "Saved project \"$cleanName\""
                    )
                }
                refreshSavedProjects()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(userMessage = "Couldn't save project: ${err.message}")
                }
            }
        }
    }

    fun loadProject(id: String) {
        isContinuousEditing = false
        resetDragTracking()
        viewModelScope.launch {
            projectStorage.loadProject(id).onSuccess { project ->
                recordHistory()
                val normalized = normalizeZIndices(
                    project.snapshot.layers.map { TextSizing.withEstimatedBounds(it) }
                )
                val validSelectedId = project.snapshot.selectedLayerId
                    ?.takeIf { sel -> normalized.any { it.id == sel } }
                    ?: normalized.maxByOrNull { it.zIndex }?.id
                _uiState.update {
                    it.copy(
                        currentProjectId = project.id,
                        currentProjectName = project.name,
                        canvasConfig = project.snapshot.canvasConfig.copy(
                            width = project.snapshot.canvasConfig.width.coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION),
                            height = project.snapshot.canvasConfig.height.coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
                        ),
                        layers = normalized,
                        selectedLayerId = validSelectedId,
                        activeDialog = null,
                        activeSubTool = ActiveSubTool.NONE,
                        activeVerticalGuideX = null,
                        activeHorizontalGuideY = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(userMessage = "Couldn't open project: ${err.message}")
                }
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            projectStorage.deleteProject(id).onSuccess {
                _uiState.update { state ->
                    if (state.currentProjectId == id) state.copy(currentProjectId = null) else state
                }
                refreshSavedProjects()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(userMessage = "Couldn't delete project: ${err.message}")
                }
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
                try {
                    val sanitized = state.currentProjectName
                        .replace("[^A-Za-z0-9_-]+".toRegex(), "_")
                        .trim('_')
                        .ifBlank { "PixelCraft" }
                    val baseName = "${sanitized}_${System.currentTimeMillis()}"
                    gallerySaver.save(bitmap, state.exportOptions, baseName).getOrThrow()
                } finally {
                    bitmap.recycle()
                }
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
