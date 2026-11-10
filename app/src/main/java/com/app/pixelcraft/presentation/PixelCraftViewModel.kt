package com.app.pixelcraft.presentation

import com.app.pixelcraft.R
import com.app.pixelcraft.presentation.i18n.UiText
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.pixelcraft.PixelCraftApp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.DraftPayload
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ProjectSummary
import com.app.pixelcraft.data.model.SavedLayerStyle
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.CanvasRenderer
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.GallerySaver
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.ImageFit
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.SnappingEngine
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.domain.UserPreferencesStore
import com.app.pixelcraft.domain.editor.EditorHistory
import com.app.pixelcraft.domain.editor.LayerGestureTracker
import com.app.pixelcraft.presentation.editor.DraftController
import com.app.pixelcraft.presentation.editor.EditorHost
import com.app.pixelcraft.presentation.editor.ExportController
import com.app.pixelcraft.presentation.editor.ProjectsController
import com.app.pixelcraft.domain.editor.LayerOperations
import com.app.pixelcraft.domain.editor.LayerOperations.bringToFront
import com.app.pixelcraft.domain.editor.LayerOperations.deleteUnlocked
import com.app.pixelcraft.domain.editor.LayerOperations.duplicate
import com.app.pixelcraft.domain.editor.LayerOperations.groupLayers
import com.app.pixelcraft.domain.editor.LayerOperations.moveDown
import com.app.pixelcraft.domain.editor.LayerOperations.moveUp
import com.app.pixelcraft.domain.editor.LayerOperations.normalizeZIndices
import com.app.pixelcraft.domain.editor.LayerOperations.sendToBack
import com.app.pixelcraft.domain.editor.LayerOperations.ungroupLayers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt

class PixelCraftViewModel internal constructor(
    application: Application,
    private val projectStorage: ProjectStorage,
    private val draftStore: DraftStore? = null,
    private val imageStore: ImageStore = ImageStore(
        imagesDirProvider = {
            val base = runCatching { application.filesDir }.getOrNull()
                ?: File(System.getProperty("java.io.tmpdir"), "pixelcraft_test_images")
            File(base, "images")
        },
        context = runCatching { application.applicationContext }.getOrNull()
    ),
    private val imageExporter: ImageExporter = ImageExporter(
        runCatching { application.applicationContext }.getOrNull()
    ),
    private val gallerySaver: GallerySaver? = runCatching {
        application.applicationContext?.let { GallerySaver(it) }
    }.getOrNull(),
    private val preferencesStore: UserPreferencesStore = UserPreferencesStore(
        runCatching { application.applicationContext }.getOrNull()
    )
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        projectStorage = (application as? PixelCraftApp)?.container?.projectStorage
            ?: ProjectStorage(application),
        draftStore = (application as? PixelCraftApp)?.container?.draftStore
            ?: DraftStore(application),
        imageStore = (application as? PixelCraftApp)?.container?.imageStore
            ?: ImageStore(application),
        imageExporter = (application as? PixelCraftApp)?.container?.imageExporter
            ?: ImageExporter(application),
        gallerySaver = (application as? PixelCraftApp)?.container?.gallerySaver
            ?: GallerySaver(application),
        preferencesStore = (application as? PixelCraftApp)?.container?.preferencesStore
            ?: UserPreferencesStore(application)
    )

    companion object {
        const val MIN_CANVAS_DIMENSION = 64
        const val MAX_CANVAS_DIMENSION = 4096
    }

    private val _uiState = MutableStateFlow(PixelCraftUiState())

    /**
     * Resolves a string for content the editor creates (default layer names, placeholder text).
     * Falls back to English when resources are unavailable (plain JVM unit tests).
     */
    private fun text(@androidx.annotation.StringRes id: Int, fallback: String, vararg args: Any): String =
        runCatching { getApplication<Application>().getString(id, *args) as String? }.getOrNull() ?: fallback
    val uiState: StateFlow<PixelCraftUiState> = _uiState.asStateFlow()

    private val history = EditorHistory<EditorSnapshot>(limit = 50)

    // Tracks the true un-snapped finger trajectory & rotation during a gesture so snapping never sticks
    private val gestureTracker = LayerGestureTracker()

    private val host = object : EditorHost {
        override val state get() = _uiState
        override val scope get() = viewModelScope
        override fun takeSnapshot() = this@PixelCraftViewModel.takeSnapshot()
        override fun recordHistoryStep(label: String) = history.beginStep(takeSnapshot(), label = label)
        override fun endEditingSession() {
            history.finishContinuousEdit()
            resetDragTracking()
        }
        override fun clearHistory() = history.clear()
        override fun syncHistoryFlags(markDirty: Boolean?) = this@PixelCraftViewModel.syncHistoryFlags(markDirty)
        override fun string(id: Int, fallback: String, vararg args: Any) = text(id, fallback, *args)
    }

    private val drafts = DraftController(host, draftStore)
    private val projects = ProjectsController(
        host = host,
        projectStorage = projectStorage,
        imageStore = imageStore,
        draftStore = draftStore,
        drafts = drafts,
        appContext = { runCatching { getApplication<Application>().applicationContext }.getOrNull() }
    )
    private val exporter = ExportController(host, imageExporter, gallerySaver, preferencesStore)

    val thumbnailsDirectory: File
        get() = projectStorage.thumbnailsDir

    init {
        val defaultPreset = DefaultPresets.presets.first()
        applyPreset(defaultPreset, recordHistory = false, markDirty = false)
        _uiState.update { it.copy(isReady = true) }
        refreshSavedProjects()
        observeUserPreferences()
        drafts.checkRecoverableDraft()
    }

    private fun observeUserPreferences() {
        val flow = preferencesStore.preferencesFlow ?: return
        viewModelScope.launch {
            flow.collect { snap ->
                val resolvedFormat = ExportFormat.entries.find { it.name == snap.lastExportFormat }
                    ?: ExportFormat.PNG
                val resolvedQuality = ExportQuality.entries.find { it.name == snap.lastExportQuality }
                    ?: ExportQuality.MEDIUM
                _uiState.update { state ->
                    state.copy(
                        exportOptions = state.exportOptions.copy(
                            format = resolvedFormat,
                            qualityPreset = resolvedQuality
                        ),
                        recentColors = snap.recentColors,
                        favoriteFontKeys = snap.favoriteFontKeys,
                        savedStyles = snap.savedStyles,
                        onboardingDismissed = snap.onboardingDismissed,
                        availableFontsCount = FontCatalog.allFonts().size
                    )
                }
            }
        }
    }

    private fun scheduleAutosave() = drafts.scheduleAutosave()

    fun resumeSavedDraft() = drafts.resumeSavedDraft()

    fun discardRecoverableDraft() = drafts.discardRecoverableDraft()

    fun markCleanAndDiscardDraft() = drafts.markCleanAndDiscardDraft()

    private fun resetDragTracking() = gestureTracker.reset()

    private fun takeSnapshot(): EditorSnapshot {
        val state = _uiState.value
        return EditorSnapshot(
            layers = state.layers,
            canvasConfig = state.canvasConfig,
            selectedLayerId = state.selectedLayerId
        )
    }

    private fun beginHistoryStep(
        continuous: Boolean,
        label: String = "Edit",
        coalesceTag: String? = null
    ) {
        history.beginStep(
            currentSnapshot = takeSnapshot(),
            label = label,
            continuous = continuous,
            coalesceTag = coalesceTag
        )
        syncHistoryFlags(markDirty = true)
        scheduleAutosave()
    }

    fun finishContinuousEdit() {
        history.finishContinuousEdit()
        gestureTracker.endRotation()
    }

    private fun syncHistoryFlags(markDirty: Boolean? = null) {
        _uiState.update { state ->
            state.copy(
                canUndo = history.canUndo,
                canRedo = history.canRedo,
                lastUndoLabel = history.lastUndoLabel,
                lastRedoLabel = history.lastRedoLabel,
                undoHistoryLabels = history.undoEntries().map { it.label },
                isDirty = markDirty ?: state.isDirty
            )
        }
    }

    fun undo() {
        resetDragTracking()
        val previousState = history.undo(takeSnapshot()) ?: return
        _uiState.update {
            it.copy(
                layers = previousState.layers,
                canvasConfig = previousState.canvasConfig,
                selectedLayerId = previousState.selectedLayerId,
                selectedLayerIds = setOfNotNull(previousState.selectedLayerId),
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        syncHistoryFlags(markDirty = history.canUndo)
        if (history.canUndo) scheduleAutosave()
    }

    fun redo() {
        resetDragTracking()
        val nextState = history.redo(takeSnapshot()) ?: return
        _uiState.update {
            it.copy(
                layers = nextState.layers,
                canvasConfig = nextState.canvasConfig,
                selectedLayerId = nextState.selectedLayerId,
                selectedLayerIds = setOfNotNull(nextState.selectedLayerId),
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        syncHistoryFlags(markDirty = true)
        scheduleAutosave()
    }

    fun jumpToHistoryIndex(index: Int) {
        resetDragTracking()
        val restored = history.jumpToUndoIndex(index, takeSnapshot()) ?: return
        _uiState.update {
            it.copy(
                layers = restored.layers,
                canvasConfig = restored.canvasConfig,
                selectedLayerId = restored.selectedLayerId,
                selectedLayerIds = setOfNotNull(restored.selectedLayerId),
                activeDialog = null,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        syncHistoryFlags(markDirty = history.canUndo)
    }

    // --- Tab, Dialog, Coach Mark & Eyedropper Management ---

    fun setActiveTab(tab: BottomNavTab) {
        history.finishContinuousEdit()
        _uiState.update { it.copy(activeTab = tab, activeSubTool = ActiveSubTool.NONE) }
    }

    fun setActiveSubTool(subTool: ActiveSubTool) {
        history.finishContinuousEdit()
        _uiState.update { it.copy(activeSubTool = subTool) }
    }

    fun openDialog(dialog: ActiveDialog) {
        history.finishContinuousEdit()
        if (dialog == ActiveDialog.TEXT_EDIT && _uiState.value.selectedLayer?.type != LayerType.TEXT) {
            return
        }
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

    fun dismissOnboarding() {
        _uiState.update { it.copy(onboardingDismissed = true) }
        viewModelScope.launch {
            preferencesStore.dismissOnboarding()
        }
    }

    fun startEyedropper(target: EyedropperTarget) {
        _uiState.update { it.copy(activeEyedropperTarget = target) }
    }

    fun cancelEyedropper() {
        _uiState.update { it.copy(activeEyedropperTarget = null) }
    }

    fun applyEyedropperColor(colorArgb: Long) {
        val target = _uiState.value.activeEyedropperTarget ?: return
        _uiState.update { it.copy(activeEyedropperTarget = null) }
        recordRecentColor(colorArgb)
        when (target) {
            EyedropperTarget.TEXT_COLOR -> updateSelectedLayer(label = "Eyedropper Text Color") {
                it.copy(textColor = colorArgb)
            }
            EyedropperTarget.TEXT_GRADIENT_END -> updateSelectedLayer(label = "Eyedropper Gradient") {
                it.copy(textGradientEnabled = true, textGradientEndColor = colorArgb)
            }
            EyedropperTarget.TEXT_BACKGROUND -> updateSelectedLayer(label = "Eyedropper Text Pill") {
                it.copy(textBackgroundColor = colorArgb)
            }
            EyedropperTarget.STROKE_COLOR -> updateSelectedLayer(label = "Eyedropper Stroke") {
                if (it.type == LayerType.SHAPE) it.copy(shapeStrokeColor = colorArgb)
                else it.copy(strokeColor = colorArgb)
            }
            EyedropperTarget.GLOW_COLOR -> updateSelectedLayer(label = "Eyedropper Glow") {
                it.copy(glowEnabled = true, glowColor = colorArgb)
            }
            EyedropperTarget.SHADOW_COLOR -> updateSelectedLayer(label = "Eyedropper Shadow") {
                it.copy(shadowEnabled = true, shadowColor = colorArgb)
            }
            EyedropperTarget.SHAPE_FILL -> updateSelectedLayer(label = "Eyedropper Shape Fill") {
                it.copy(shapeFillColor = colorArgb)
            }
            EyedropperTarget.SHAPE_GRADIENT_END -> updateSelectedLayer(label = "Eyedropper Shape Gradient") {
                it.copy(shapeGradientEnabled = true, shapeGradientEndColor = colorArgb)
            }
            EyedropperTarget.SHAPE_STROKE -> updateSelectedLayer(label = "Eyedropper Shape Stroke") {
                it.copy(shapeStrokeColor = colorArgb)
            }
            EyedropperTarget.IMAGE_BORDER -> updateSelectedLayer(label = "Eyedropper Photo Border") {
                it.copy(imageBorderColor = colorArgb, imageBorderWidth = it.imageBorderWidth.coerceAtLeast(4f))
            }
            EyedropperTarget.CANVAS_BACKGROUND -> setBackgroundColor(colorArgb)
        }
    }

    fun recordRecentColor(colorArgb: Long) {
        viewModelScope.launch {
            preferencesStore.recordRecentColor(colorArgb)
        }
    }

    fun toggleFavoriteFont(fontKey: String) {
        viewModelScope.launch {
            preferencesStore.toggleFavoriteFont(fontKey)
        }
    }

    fun importCustomFont(uri: Uri) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            FontCatalog.importCustomFont(ctx, uri).onSuccess { imported ->
                _uiState.update {
                    it.copy(
                        availableFontsCount = FontCatalog.allFonts().size,
                        userMessage = UiText.Res(R.string.msg_font_imported, imported.displayName)
                    )
                }
                if (_uiState.value.selectedLayer?.type == LayerType.TEXT) {
                    updateSelectedLayer(label = "Apply Custom Font") { layer ->
                        layer.copy(fontKey = imported.key)
                    }
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = UiText.Res(R.string.msg_font_import_failed, err.message.orEmpty())) }
            }
        }
    }

    // --- Grid & Safe Zone ---

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

    fun toggleSafeZone() {
        _uiState.update { it.copy(showSafeZone = !it.showSafeZone) }
    }

    // --- Layer Selection, Multi-Select & Grouping ---

    fun selectLayer(layerId: String?) {
        history.finishContinuousEdit()
        resetDragTracking()
        _uiState.update { state ->
            val layer = state.layers.find { it.id == layerId }
            val groupMembers = if (layer?.groupId != null) {
                state.layers.filter { it.groupId == layer.groupId }.map { it.id }.toSet()
            } else {
                setOfNotNull(layerId)
            }
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
                selectedLayerIds = groupMembers,
                activeTab = newTab,
                activeSubTool = nextSubTool,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
    }

    fun toggleLayerMultiSelection(layerId: String) {
        _uiState.update { state ->
            if (state.layers.none { it.id == layerId }) return@update state
            val nextSet = state.selectedLayerIds.toMutableSet()
            if (!nextSet.add(layerId)) {
                nextSet.remove(layerId)
            }
            val primary = if (state.selectedLayerId in nextSet) {
                state.selectedLayerId
            } else {
                nextSet.lastOrNull()
            }
            state.copy(
                selectedLayerId = primary,
                selectedLayerIds = nextSet
            )
        }
    }

    fun groupSelectedLayers() {
        val ids = _uiState.value.selectedLayerIds
        if (ids.size < 2) return
        beginHistoryStep(continuous = false, label = "Group Layers")
        _uiState.update { state ->
            state.copy(
                layers = state.layers.groupLayers(ids),
                userMessage = UiText.Res(R.string.msg_grouped_layers, ids.size)
            )
        }
    }

    fun ungroupSelectedLayers() {
        val ids = _uiState.value.selectedLayerIds.ifEmpty {
            setOfNotNull(_uiState.value.selectedLayerId)
        }
        if (ids.isEmpty()) return
        beginHistoryStep(continuous = false, label = "Ungroup Layers")
        _uiState.update { state ->
            state.copy(
                layers = state.layers.ungroupLayers(ids),
                userMessage = UiText.Res(R.string.msg_ungrouped_layers)
            )
        }
    }

    // --- Style Copy / Paste & Saved Styles ---

    fun copySelectedStyle() {
        val layer = _uiState.value.selectedLayer ?: return
        _uiState.update {
            it.copy(
                copiedStyleLayer = layer,
                userMessage = UiText.Res(R.string.msg_style_copied, layer.name)
            )
        }
    }

    fun pasteStyleToSelected() {
        val copied = _uiState.value.copiedStyleLayer ?: return
        val target = _uiState.value.selectedLayer ?: return
        if (target.isLocked) return
        updateSelectedLayer(continuous = false, label = "Paste Style") {
            it.copyVisualStyleFrom(copied)
        }
    }

    fun saveCurrentStyle(styleName: String) {
        val layer = _uiState.value.selectedLayer ?: return
        val clean = styleName.trim().ifBlank { text(R.string.default_style_name, "${layer.name} Style", layer.name) }
        val entry = SavedLayerStyle(
            id = UUID.randomUUID().toString(),
            name = clean,
            styleLayer = layer
        )
        viewModelScope.launch {
            preferencesStore.saveLayerStyle(entry)
            _uiState.update { it.copy(userMessage = UiText.Res(R.string.msg_style_saved, clean)) }
        }
    }

    fun applySavedStyle(style: SavedLayerStyle) {
        val target = _uiState.value.selectedLayer ?: return
        if (target.isLocked) return
        updateSelectedLayer(continuous = false, label = "Apply Style") {
            it.copyVisualStyleFrom(style.styleLayer)
        }
    }

    fun deleteSavedStyle(styleId: String) {
        viewModelScope.launch {
            preferencesStore.deleteLayerStyle(styleId)
        }
    }

    // --- Home Screen /Blank Canvas / External Intent Flows ---

    fun createNewBlankCanvas(
        preset: AspectRatioPreset = AspectRatioPreset.SQUARE,
        customW: Int? = null,
        customH: Int? = null,
        backgroundColor: Long = 0xFF1F2430L
    ) {
        drafts.cancelAutosave()
        history.clear()
        resetDragTracking()
        val w = (customW ?: preset.defaultWidth).coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
        val h = (customH ?: preset.defaultHeight).coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
        val starterText = TextSizing.withEstimatedBounds(
            Layer(
                id = UUID.randomUUID().toString(),
                name = text(R.string.default_headline_name, "Headline"),
                type = LayerType.TEXT,
                text = text(R.string.default_tap_to_edit, "Tap to Edit"),
                fontKey = "poppins",
                fontSize = (minOf(w, h) * 0.055f).coerceIn(28f, 96f),
                textColor = 0xFFFFFFFF,
                zIndex = 0
            )
        )
        _uiState.update {
            it.copy(
                canvasConfig = CanvasConfig(
                    width = w,
                    height = h,
                    preset = preset,
                    backgroundType = BackgroundType.SOLID_COLOR,
                    backgroundColor = backgroundColor
                ),
                layers = listOf(starterText),
                selectedLayerId = starterText.id,
                selectedLayerIds = setOf(starterText.id),
                currentProjectId = null,
                currentProjectName = text(R.string.ui_untitled, "Untitled"),
                isDirty = false,
                activeTab = BottomNavTab.TEXT,
                activeSubTool = ActiveSubTool.NONE,
                activeDialog = null,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        syncHistoryFlags(markDirty = false)
    }

    /** Starts a fresh canvas sized proportionally to an imported photo (Home "From Photo" & ACTION_SEND/EDIT). */
    fun startCanvasFromPhoto(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { imported ->
                drafts.cancelAutosave()
                history.clear()
                resetDragTracking()
                val fit = ImageFit.fitWithin(
                    imported.width,
                    imported.height,
                    MAX_CANVAS_DIMENSION.toFloat().coerceAtMost(2160f),
                    MAX_CANVAS_DIMENSION.toFloat().coerceAtMost(2160f)
                )
                val canvasW = fit.width.roundToInt().coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
                val canvasH = fit.height.roundToInt().coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
                _uiState.update {
                    it.copy(
                        canvasConfig = CanvasConfig(
                            width = canvasW,
                            height = canvasH,
                            preset = AspectRatioPreset.CUSTOM,
                            backgroundType = BackgroundType.CUSTOM_IMAGE,
                            customImageUri = imported.path
                        ),
                        layers = emptyList(),
                        selectedLayerId = null,
                        selectedLayerIds = emptySet(),
                        currentProjectId = null,
                        currentProjectName = text(R.string.default_photo_design, "Photo Design"),
                        isDirty = true,
                        activeTab = BottomNavTab.TEXT,
                        activeSubTool = ActiveSubTool.NONE,
                        activeDialog = null
                    )
                }
                syncHistoryFlags(markDirty = true)
                scheduleAutosave()
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = UiText.Res(R.string.msg_open_picture_failed, err.message.orEmpty())) }
            }
        }
    }

    // --- Layer Operations ---

    fun addTextLayer(initialText: String = text(R.string.default_new_text, "New Text")) {
        beginHistoryStep(continuous = false, label = "Add Text")
        resetDragTracking()
        val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
        val labelIndex = _uiState.value.layers.count { it.type == LayerType.TEXT } + 1
        val newLayer = TextSizing.withEstimatedBounds(
            Layer(
                id = UUID.randomUUID().toString(),
                name = text(R.string.default_text_layer_name, "Text $labelIndex", labelIndex),
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
                layers = (state.layers + newLayer).normalizeZIndices(),
                selectedLayerId = newLayer.id,
                selectedLayerIds = setOf(newLayer.id),
                activeTab = BottomNavTab.TEXT,
                activeSubTool = ActiveSubTool.NONE
            )
        }
    }

    fun addCurrentDateLayer(pattern: String = "EEEE, d MMMM yyyy") {
        val formatted = runCatching {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date())
        }.getOrElse {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }
        addTextLayer(formatted)
    }

    fun applyTextCase(mode: TextCaseMode) {
        val current = _uiState.value.selectedLayer ?: return
        if (current.type != LayerType.TEXT || current.isLocked) return
        updateSelectedLayer(continuous = false, label = "Change Text Case") {
            it.copy(text = TextSizing.transformCase(it.text, mode))
        }
    }

    fun renameLayer(layerId: String, newName: String) {
        val clean = newName.trim().ifBlank { return }
        if (_uiState.value.layers.none { it.id == layerId }) return
        beginHistoryStep(continuous = false, label = "Rename Layer")
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(name = clean) else it
                },
                activeDialog = null
            )
        }
    }

    fun addShapeLayer(shapeType: ShapeType) {
        beginHistoryStep(continuous = false, label = "Add Shape")
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
                layers = (state.layers + newLayer).normalizeZIndices(),
                selectedLayerId = newLayer.id,
                selectedLayerIds = setOf(newLayer.id),
                activeTab = BottomNavTab.OBJECTS,
                activeSubTool = ActiveSubTool.NONE
            )
        }
    }

    /** Adds a picked photo as a movable layer, sized to fit within 60% of the canvas. */
    fun addImageLayer(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { image ->
                beginHistoryStep(continuous = false, label = "Add Photo")
                resetDragTracking()
                val config = _uiState.value.canvasConfig
                val fit = ImageFit.fitWithin(image.width, image.height, config.width * 0.6f, config.height * 0.6f)
                val nextZ = (_uiState.value.layers.maxOfOrNull { it.zIndex } ?: -1) + 1
                val labelIndex = _uiState.value.layers.count { it.type == LayerType.IMAGE } + 1
                val newLayer = Layer(
                    id = UUID.randomUUID().toString(),
                    name = text(R.string.default_photo_layer_name, "Photo $labelIndex", labelIndex),
                    type = LayerType.IMAGE,
                    imageLocalPath = image.path,
                    width = fit.width,
                    height = fit.height,
                    zIndex = nextZ
                )
                _uiState.update { state ->
                    state.copy(
                        layers = (state.layers + newLayer).normalizeZIndices(),
                        selectedLayerId = newLayer.id,
                        selectedLayerIds = setOf(newLayer.id),
                        activeTab = BottomNavTab.OBJECTS,
                        activeSubTool = ActiveSubTool.NONE
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = UiText.Res(R.string.msg_open_picture_failed, err.message.orEmpty())) }
            }
        }
    }

    fun updateSelectedLayer(
        continuous: Boolean = false,
        label: String = "Edit Layer",
        transform: (Layer) -> Layer
    ) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return

        beginHistoryStep(continuous = continuous, label = label)
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

    fun updateLayerPositionWithSnapping(
        deltaX: Float,
        deltaY: Float,
        snapThresholdPx: Float = SnappingEngine.SNAP_THRESHOLD_PX
    ) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return

        beginHistoryStep(continuous = true, label = "Move Layer")
        val selectedId = currentLayer.id
        val raw = gestureTracker.advanceDrag(selectedId, currentLayer.positionX, currentLayer.positionY, deltaX, deltaY)
        val nextRaw = PointF(raw.x, raw.y)

        val stateSnapshot = _uiState.value
        val activeGroupId = currentLayer.groupId
        val targetX: Float
        val targetY: Float
        val vGuide: Float?
        val hGuide: Float?

        if (!stateSnapshot.isGridEnabled) {
            targetX = nextRaw.x
            targetY = nextRaw.y
            vGuide = null
            hGuide = null
        } else {
            val others = stateSnapshot.layers.filter { layer ->
                layer.id != selectedId && (activeGroupId == null || layer.groupId != activeGroupId)
            }
            val snapResult = SnappingEngine.calculateSnap(
                rawPosition = nextRaw,
                canvasWidth = stateSnapshot.canvasConfig.width.toFloat(),
                canvasHeight = stateSnapshot.canvasConfig.height.toFloat(),
                gridColumns = stateSnapshot.gridColumns,
                gridRows = stateSnapshot.gridRows,
                thresholdPx = snapThresholdPx,
                layerWidth = currentLayer.width,
                layerHeight = currentLayer.height,
                otherLayers = others
            )
            targetX = snapResult.snappedPosition.x
            targetY = snapResult.snappedPosition.y
            vGuide = snapResult.verticalGuideX
            hGuide = snapResult.horizontalGuideY
        }

        val appliedDx = targetX - currentLayer.positionX
        val appliedDy = targetY - currentLayer.positionY

        _uiState.update { s ->
            s.copy(
                layers = s.layers.map { layer ->
                    when {
                        layer.id == selectedId -> layer.copy(positionX = targetX, positionY = targetY)
                        activeGroupId != null && layer.groupId == activeGroupId && !layer.isLocked -> {
                            layer.copy(
                                positionX = layer.positionX + appliedDx,
                                positionY = layer.positionY + appliedDy
                            )
                        }
                        else -> layer
                    }
                },
                activeVerticalGuideX = vGuide,
                activeHorizontalGuideY = hGuide
            )
        }
    }

    /**
     * Applies 2-finger pinch-scale, twist-rotate (with `±4°` cardinal/diagonal snap), and pan to
     * the selected layer (§ 5.1). Returns `true` when rotation freshly snaps to a cardinal/diagonal angle
     * so the canvas UI can trigger haptic feedback!
     */
    fun transformSelectedLayerGesture(
        panDeltaX: Float,
        panDeltaY: Float,
        zoomFactor: Float,
        rotationDeltaDeg: Float,
        snapRotation: Boolean = true
    ): Boolean {
        val currentLayer = _uiState.value.selectedLayer ?: return false
        if (currentLayer.isLocked) return false

        beginHistoryStep(continuous = true, label = "Transform Layer")
        val selectedId = currentLayer.id

        val rotation = gestureTracker.advanceRotation(currentLayer.rotation, rotationDeltaDeg, snapRotation)
        val finalRotation = rotation.degrees

        val safeZoom = zoomFactor.coerceIn(0.5f, 2.0f)
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id != selectedId) {
                        layer
                    } else {
                        val moved = layer.copy(
                            positionX = layer.positionX + panDeltaX,
                            positionY = layer.positionY + panDeltaY,
                            rotation = finalRotation
                        )
                        if (layer.type == LayerType.TEXT) {
                            TextSizing.withEstimatedBounds(
                                moved.copy(fontSize = (layer.fontSize * safeZoom).coerceIn(8f, 520f))
                            )
                        } else {
                            moved.copy(
                                width = (layer.width * safeZoom).coerceIn(24f, 4096f),
                                height = (layer.height * safeZoom).coerceIn(24f, 4096f)
                            )
                        }
                    }
                }
            )
        }
        return rotation.snappedJustNow
    }

    fun clearSnappingGuides() {
        history.finishContinuousEdit()
        resetDragTracking()
        _uiState.update {
            it.copy(activeVerticalGuideX = null, activeHorizontalGuideY = null)
        }
    }

    fun nudgeSelectedLayer(dx: Float, dy: Float) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return
        resetDragTracking()
        beginHistoryStep(continuous = false, label = "Nudge Layer", coalesceTag = "nudge_${currentLayer.id}")
        val selectedId = currentLayer.id
        val groupId = currentLayer.groupId
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map { layer ->
                    if (layer.id == selectedId || (groupId != null && layer.groupId == groupId && !layer.isLocked)) {
                        layer.copy(positionX = layer.positionX + dx, positionY = layer.positionY + dy)
                    } else {
                        layer
                    }
                }
            )
        }
    }

    fun alignSelectedLayer(action: AlignmentAction) {
        val currentLayer = _uiState.value.selectedLayer ?: return
        if (currentLayer.isLocked) return
        resetDragTracking()
        val state = _uiState.value
        val canvasW = state.canvasConfig.width.toFloat()
        val canvasH = state.canvasConfig.height.toFloat()

        updateSelectedLayer(continuous = false, label = "Align Layer") { layer ->
            LayerOperations.alignSingleLayer(layer, canvasW, canvasH, action)
        }
    }

    fun deleteLayer(layerId: String) {
        val target = _uiState.value.layers.find { it.id == layerId } ?: return
        if (target.isLocked) return

        beginHistoryStep(continuous = false, label = "Delete Layer")
        resetDragTracking()
        _uiState.update { state ->
            val remaining = state.layers.deleteUnlocked(layerId)
            val nextSelected = if (state.selectedLayerId == layerId) {
                remaining.maxByOrNull { it.zIndex }?.id
            } else {
                state.selectedLayerId
            }
            state.copy(
                layers = remaining,
                selectedLayerId = nextSelected,
                selectedLayerIds = setOfNotNull(nextSelected),
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
        val selectedId = _uiState.value.selectedLayerId ?: return
        val duplicated = _uiState.value.layers.duplicate(selectedId) ?: return
        beginHistoryStep(continuous = false, label = "Duplicate Layer")
        resetDragTracking()
        _uiState.update { state ->
            state.copy(
                layers = duplicated.first,
                selectedLayerId = duplicated.second.id,
                selectedLayerIds = setOf(duplicated.second.id)
            )
        }
    }

    fun bringLayerToFront() {
        val current = _uiState.value.selectedLayer ?: return
        if (current.isLocked) return
        val updated = _uiState.value.layers.bringToFront(current.id)
        if (updated == _uiState.value.layers) return
        beginHistoryStep(continuous = false, label = "Bring to Front")
        _uiState.update { it.copy(layers = updated) }
    }

    fun sendLayerToBack() {
        val current = _uiState.value.selectedLayer ?: return
        if (current.isLocked) return
        val updated = _uiState.value.layers.sendToBack(current.id)
        if (updated == _uiState.value.layers) return
        beginHistoryStep(continuous = false, label = "Send to Back")
        _uiState.update { it.copy(layers = updated) }
    }

    /** Moves [layerId] one step forward (higher `zIndex`) in visual order. */
    fun moveLayerUp(layerId: String) {
        val updated = _uiState.value.layers.moveUp(layerId)
        if (updated == _uiState.value.layers) return
        beginHistoryStep(continuous = false, label = "Move Layer Up")
        _uiState.update { it.copy(layers = updated) }
    }

    /** Moves [layerId] one step backward (lower `zIndex`) in visual order. */
    fun moveLayerDown(layerId: String) {
        val updated = _uiState.value.layers.moveDown(layerId)
        if (updated == _uiState.value.layers) return
        beginHistoryStep(continuous = false, label = "Move Layer Down")
        _uiState.update { it.copy(layers = updated) }
    }

    fun toggleLayerVisibility(layerId: String) {
        if (_uiState.value.layers.none { it.id == layerId }) return
        beginHistoryStep(continuous = false, label = "Toggle Visibility")
        _uiState.update { state ->
            state.copy(
                layers = state.layers.map {
                    if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
                }
            )
        }
    }

    /** Fixes Bug 2.18: Locking and unlocking a layer records an undo step consistently with visibility! */
    fun toggleLayerLock(layerId: String) {
        if (_uiState.value.layers.none { it.id == layerId }) return
        resetDragTracking()
        beginHistoryStep(continuous = false, label = "Toggle Lock")
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
        beginHistoryStep(continuous = false, label = "Resize Canvas")
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
        beginHistoryStep(continuous = false, label = "Background Color")
        recordRecentColor(color)
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.SOLID_COLOR,
                    backgroundColor = color
                )
            )
        }
    }

    fun setBackgroundGradient(
        colors: List<Long>,
        angle: Float = _uiState.value.canvasConfig.gradientAngle,
        type: GradientType = _uiState.value.canvasConfig.gradientType,
        continuous: Boolean = false
    ) {
        beginHistoryStep(continuous = continuous, label = "Background Gradient")
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.GRADIENT,
                    gradientColors = colors,
                    gradientAngle = angle,
                    gradientType = type
                )
            )
        }
    }

    /** Uses a picked photo as the canvas background (centre-cropped to fill it). */
    fun setBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            imageStore.import(uri).onSuccess { image ->
                beginHistoryStep(continuous = false, label = "Background Photo")
                _uiState.update { state ->
                    state.copy(
                        canvasConfig = state.canvasConfig.copy(
                            backgroundType = BackgroundType.CUSTOM_IMAGE,
                            customImageUri = image.path
                        )
                    )
                }
            }.onFailure { err ->
                _uiState.update { it.copy(userMessage = UiText.Res(R.string.msg_open_picture_failed, err.message.orEmpty())) }
            }
        }
    }

    fun updateBackgroundPhotoEffects(
        blurRadius: Float = _uiState.value.canvasConfig.backgroundBlur,
        dimAmount: Float = _uiState.value.canvasConfig.backgroundDim,
        continuous: Boolean = true
    ) {
        beginHistoryStep(continuous = continuous, label = "Background Photo Adjustments")
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundBlur = blurRadius.coerceIn(0f, 25f),
                    backgroundDim = dimAmount.coerceIn(0f, 0.85f)
                )
            )
        }
    }

    fun setBackgroundTransparent() {
        beginHistoryStep(continuous = false, label = "Transparent Background")
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    backgroundType = BackgroundType.TRANSPARENT
                )
            )
        }
    }

    fun updateCanvasFilters(continuous: Boolean = false, transform: (CanvasFilters) -> CanvasFilters) {
        beginHistoryStep(continuous = continuous, label = "Canvas Filters")
        _uiState.update { state ->
            state.copy(
                canvasConfig = state.canvasConfig.copy(
                    filters = transform(state.canvasConfig.filters)
                )
            )
        }
    }

    // --- Presets ---

    fun applyPreset(preset: StylePreset, recordHistory: Boolean = true, markDirty: Boolean = recordHistory) {
        history.finishContinuousEdit()
        resetDragTracking()
        if (recordHistory) {
            history.beginStep(takeSnapshot(), label = "Apply Template")
        }
        val normalizedLayers = preset.initialLayers
            .map { TextSizing.withEstimatedBounds(it) }
            .normalizeZIndices()
        val topLayer = normalizedLayers.maxByOrNull { it.zIndex }
        _uiState.update { state ->
            state.copy(
                canvasConfig = preset.canvasConfig,
                layers = normalizedLayers,
                selectedLayerId = topLayer?.id,
                selectedLayerIds = setOfNotNull(topLayer?.id),
                currentProjectId = null,
                currentProjectName = preset.title,
                activeSubTool = ActiveSubTool.NONE,
                activeVerticalGuideX = null,
                activeHorizontalGuideY = null
            )
        }
        syncHistoryFlags(markDirty = markDirty)
        if (markDirty) scheduleAutosave()
    }

    // --- Projects Save / Load / Rename / Duplicate / Archive ---

    fun refreshSavedProjects() = projects.refreshSavedProjects()

    fun saveProject(name: String, onSaved: (() -> Unit)? = null) = projects.saveProject(name, onSaved)

    fun loadProject(id: String) = projects.loadProject(id)

    fun renameSavedProject(id: String, newName: String) = projects.renameSavedProject(id, newName)

    fun duplicateSavedProject(id: String) = projects.duplicateSavedProject(id)

    fun deleteProject(id: String) = projects.deleteProject(id)

    fun undoDeleteProject() = projects.undoDeleteProject()

    fun exportProjectArchive(projectId: String, destinationUri: Uri) =
        projects.exportProjectArchive(projectId, destinationUri)

    fun importProjectArchive(sourceUri: Uri, onImported: ((String) -> Unit)? = null) =
        projects.importProjectArchive(sourceUri, onImported)

    // --- Export & Direct Share ---

    fun setExportOptions(options: ExportOptions) = exporter.setExportOptions(options)

    fun cancelExport() = exporter.cancelExport()

    fun exportCanvas() = exporter.exportCanvas()

    fun shareCanvasDirectly() = exporter.shareCanvasDirectly()

    fun consumePendingShareUri() {
        _uiState.update { it.copy(pendingShareUri = null) }
    }

    fun reportMessage(message: UiText) {
        _uiState.update { it.copy(userMessage = message) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null, lastExportedUri = null) }
    }
}
