package com.app.pixelcraft.presentation

import android.app.Activity
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.saveable.rememberSaveable
import com.app.pixelcraft.presentation.components.EditorHalfSheet
import com.app.pixelcraft.presentation.components.EditorTool
import com.app.pixelcraft.presentation.components.EditorToolLevel
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.presentation.components.EditorToolRow
import com.app.pixelcraft.presentation.components.EditorUndoStrip
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.app.pixelcraft.ui.theme.PixelCraftEditorTheme
import com.app.pixelcraft.ui.theme.PixelCraftTheme

import com.app.pixelcraft.presentation.i18n.asString
import com.app.pixelcraft.presentation.i18n.UiText
import com.app.pixelcraft.ui.theme.EditorColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.presentation.components.PixelCraftCanvas
import com.app.pixelcraft.presentation.components.PixelCraftLayersSheet
import com.app.pixelcraft.presentation.components.PixelCraftTopBar
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftAboutDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftDateFormatDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftExportDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftHistoryDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftImageSizeDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftRenameDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftSaveProjectDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftSavedStylesDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftShapeDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftTextEditDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftUnsavedChangesDialog
import com.app.pixelcraft.presentation.components.tools.BackgroundToolPanel
import com.app.pixelcraft.presentation.components.tools.FilterToolPanel
import com.app.pixelcraft.presentation.components.tools.GroupToolPanel
import com.app.pixelcraft.presentation.components.tools.ObjectToolPanel
import com.app.pixelcraft.presentation.components.tools.TextToolPanel


@Composable
fun PixelCraftScreen(
    viewModel: PixelCraftViewModel,
    onNavigateHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Dark editor chrome needs light system-bar icons; restore the home screen's dark icons on exit.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }
    PixelCraftEditorTheme {
        PixelCraftEditorContent(
            viewModel = viewModel,
            onNavigateHome = onNavigateHome,
            modifier = modifier
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun PixelCraftEditorContent(
    viewModel: PixelCraftViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var layerPendingRename by remember { mutableStateOf<Layer?>(null) }
    var exitAfterSave by remember { mutableStateOf(false) }
    var openTool by rememberSaveable { mutableStateOf<EditorTool?>(null) }

    // Selecting a layer jumps straight to its sub-tools; deselecting returns to the top-level row.
    LaunchedEffect(state.selectedLayerId) {
        if (state.selectedLayerId != null) openTool = null
    }

    // Ordered BackHandler preventing accidental loss of user work (Fixes Bug 2.1)
    fun handleNavigateBackStep() {
        when {
            state.isEyedropperActive -> viewModel.cancelEyedropper()
            state.activeSubTool != ActiveSubTool.NONE -> viewModel.setActiveSubTool(ActiveSubTool.NONE)
            state.isLayersDrawerOpen -> viewModel.setLayersDrawerOpen(false)
            state.selectedLayerId != null -> viewModel.selectLayer(null)
            openTool != null -> openTool = null
            state.isDirty -> viewModel.openDialog(ActiveDialog.UNSAVED_CHANGES)
            else -> onNavigateHome()
        }
    }

    BackHandler(enabled = state.activeDialog == null) {
        handleNavigateBackStep()
    }

    // Pick up to 10 photos at once; each becomes its own layer, cascaded so none hide the others.
    val pickLayerImage = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        if (uris.isNotEmpty()) viewModel.addImageLayers(uris)
    }
    val pickBackgroundImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setBackgroundImage(it) }
    }
    val pickCustomFont = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importCustomFont(it) }
    }
    // Optional: the user can grant a fonts folder (opens at /sdcard/Fonts/) from the font picker's
    // folder button; once granted it's re-scanned on every launch. Never prompted automatically.
    val pickDeviceFontsFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { viewModel.setDeviceFontsFolder(it) }
    }
    val launchDeviceFontsPicker = {
        runCatching { pickDeviceFontsFolder.launch(FontCatalog.deviceFontsInitialUri) }
        Unit
    }
    val imagesOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    // Android 9 (API 28) needs WRITE_EXTERNAL_STORAGE to insert into shared Pictures
    val requestStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.exportCanvas()
        else viewModel.reportMessage(UiText.Res(R.string.msg_storage_permission_needed))
    }
    val exportWithPermission = {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else viewModel.exportCanvas()
    }

    // Launch system share sheet directly from FileProvider cache URI without polluting Gallery (Fixes Bug 2.16)
    LaunchedEffect(state.pendingShareUri) {
        val shareUri = state.pendingShareUri ?: return@LaunchedEffect
        try {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = state.pendingShareMimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, context.getString(R.string.ui_share_design)))
        } finally {
            viewModel.consumePendingShareUri()
        }
    }

    val shareActionLabel = stringResource(R.string.action_share)
    LaunchedEffect(state.userMessage) {
        val msg = state.userMessage?.asString(context) ?: return@LaunchedEffect
        val exportedUri = state.lastExportedUri
        try {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (exportedUri != null) shareActionLabel else null,
                withDismissAction = exportedUri != null,
                duration = if (exportedUri != null) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && exportedUri != null) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(exportedUri) ?: "image/*"
                    putExtra(Intent.EXTRA_STREAM, exportedUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, context.getString(R.string.ui_share_image)))
            }
        } finally {
            viewModel.clearUserMessage()
        }
    }

    // CapCut-style tool area: top-level row -> a tool's sub-tool row -> its compact inspector.
    @Composable
    fun ToolArea() {
        val selected = state.selectedLayer
        val inspectorOpen = state.activeSubTool != ActiveSubTool.NONE
        val groupMembers = selected?.groupId?.let { gid -> state.layers.count { it.groupId == gid } } ?: 0
        when {
            // A whole group is selected: manipulate it as one object.
            selected != null && selected.groupId != null && groupMembers > 1 && state.selectedLayerIds.size > 1 -> {
                EditorToolLevel(onBack = { viewModel.selectLayer(null) }) {
                    GroupToolPanel(
                        groupId = selected.groupId,
                        memberCount = groupMembers,
                        primaryLayer = selected,
                        onScale = { p, cont -> viewModel.scaleSelectedGroup(p, cont) },
                        onRotate = { d, cont -> viewModel.rotateSelectedGroup(d, cont) },
                        onUpdateMembers = { cont, tf -> viewModel.updateSelectedGroupMembers(cont, transform = tf) },
                        onFinishContinuousEdit = { viewModel.finishContinuousEdit() },
                        onEndRelativeSession = { viewModel.resetGroupEditBase() },
                        onFlip = { viewModel.flipSelectedGroup(it) },
                        onAlign = { viewModel.alignSelectedLayer(it) },
                        onBringToFront = { viewModel.bringLayerToFront() },
                        onSendToBack = { viewModel.sendLayerToBack() },
                        onDuplicate = { viewModel.duplicateSelectedLayer() },
                        onDelete = { viewModel.deleteSelectedLayer() },
                        onUngroup = { viewModel.ungroupSelectedLayers() },
                        onEditItem = { viewModel.focusSelectedGroupMember() },
                        onMerge = { viewModel.mergeSelectedGroup() },
                        onDistribute = { horizontal -> viewModel.distributeSelected(horizontal) },
                        recentColors = state.recentColors
                    )
                }
            }

            selected != null && selected.type == LayerType.TEXT -> {
                EditorToolLevel(onBack = { viewModel.selectLayer(null) }, showBack = !inspectorOpen) {
                    TextToolPanel(
                        selectedLayer = state.selectedLayer,
                        activeSubTool = state.activeSubTool,
                        recentColors = state.recentColors,
                        favoriteFontKeys = state.favoriteFontKeys,
                        recentFontKeys = state.recentFontKeys,
                        availableFontsCount = state.availableFontsCount,
                        hasCopiedStyle = state.copiedStyleLayer != null,
                        onSubToolSelected = { viewModel.setActiveSubTool(it) },
                        onUpdateLayer = { continuous, transform ->
                            viewModel.updateSelectedLayer(continuous = continuous, transform = transform)
                        },
                        onFinishContinuousEdit = { viewModel.finishContinuousEdit() },
                        onApplyTextCase = { mode -> viewModel.applyTextCase(mode) },
                        onToggleFavoriteFont = { key -> viewModel.toggleFavoriteFont(key) },
                        onImportFont = {
                            pickCustomFont.launch(
                                arrayOf(
                                    "font/*",
                                    "application/x-font-ttf",
                                    "application/x-font-otf",
                                    "application/octet-stream"
                                )
                            )
                        },
                        onPickDeviceFontsFolder = launchDeviceFontsPicker,
                        onStartEyedropper = { target -> viewModel.startEyedropper(target) },
                        onCopyStyle = { viewModel.copySelectedStyle() },
                        onPasteStyle = { viewModel.pasteStyleToSelected() },
                        onOpenSavedStyles = { viewModel.openDialog(ActiveDialog.SAVED_STYLES) },
                        onDeleteLayer = { viewModel.deleteSelectedLayer() },
                        onDuplicateLayer = { viewModel.duplicateSelectedLayer() },
                        onBringToFront = { viewModel.bringLayerToFront() },
                        onSendToBack = { viewModel.sendLayerToBack() },
                        onNudge = { dx, dy -> viewModel.nudgeSelectedLayer(dx, dy) },
                        onAlign = { action -> viewModel.alignSelectedLayer(action) },
                        onOpenEditDialog = { viewModel.openDialog(ActiveDialog.TEXT_EDIT) },
                        onAddText = { viewModel.addTextLayer() },
                        canvasWidth = state.canvasConfig.width,
                        canvasHeight = state.canvasConfig.height
                    )
                }
            }

            selected != null -> {
                EditorToolLevel(onBack = { viewModel.selectLayer(null) }, showBack = !inspectorOpen) {
                    ObjectToolPanel(
                        selectedLayer = state.selectedLayer,
                        activeSubTool = state.activeSubTool,
                        recentColors = state.recentColors,
                        hasCopiedStyle = state.copiedStyleLayer != null,
                        onSubToolSelected = { viewModel.setActiveSubTool(it) },
                        onUpdateLayer = { continuous, transform ->
                            viewModel.updateSelectedLayer(continuous = continuous, transform = transform)
                        },
                        onFinishContinuousEdit = { viewModel.finishContinuousEdit() },
                        onStartEyedropper = { target -> viewModel.startEyedropper(target) },
                        onCopyStyle = { viewModel.copySelectedStyle() },
                        onPasteStyle = { viewModel.pasteStyleToSelected() },
                        onOpenSavedStyles = { viewModel.openDialog(ActiveDialog.SAVED_STYLES) },
                        onDeleteLayer = { viewModel.deleteSelectedLayer() },
                        onDuplicateLayer = { viewModel.duplicateSelectedLayer() },
                        onBringToFront = { viewModel.bringLayerToFront() },
                        onSendToBack = { viewModel.sendLayerToBack() },
                        onOpenDialog = { viewModel.openDialog(it) },
                        onAddImage = { pickLayerImage.launch(imagesOnly) },
                        canvasWidth = state.canvasConfig.width,
                        canvasHeight = state.canvasConfig.height,
                        onNudge = { dx, dy -> viewModel.nudgeSelectedLayer(dx, dy) },
                        onAlign = { action -> viewModel.alignSelectedLayer(action) }
                    )
                }
            }

            openTool == EditorTool.CANVAS -> {
                EditorToolLevel(onBack = { openTool = null }) {
                    BackgroundToolPanel(
                        canvasConfig = state.canvasConfig,
                        recentColors = state.recentColors,
                        onSetColor = { viewModel.setBackgroundColor(it) },
                        onSetGradient = { colors, angle, gradType, continuous ->
                            viewModel.setBackgroundGradient(colors, angle, gradType, continuous)
                        },
                        onUpdatePhotoEffects = { blur, dim, continuous ->
                            viewModel.updateBackgroundPhotoEffects(blur, dim, continuous)
                        },
                        onFinishContinuousEdit = { viewModel.finishContinuousEdit() },
                        onSetTransparent = { viewModel.setBackgroundTransparent() },
                        onPickImage = { pickBackgroundImage.launch(imagesOnly) },
                        onPickEyedropper = { viewModel.startEyedropper(EyedropperTarget.CANVAS_BACKGROUND) },
                        onOpenDialog = { viewModel.openDialog(it) }
                    )
                }
            }

            openTool == EditorTool.EFFECTS -> {
                EditorToolLevel(onBack = { openTool = null }) {
                    FilterToolPanel(
                        filters = state.canvasConfig.filters,
                        recentColors = state.recentColors,
                        onUpdateFilters = { continuous, transform ->
                            viewModel.updateCanvasFilters(continuous = continuous, transform = transform)
                        },
                        onFinishContinuousEdit = { viewModel.finishContinuousEdit() }
                    )
                }
            }

            else -> {
                EditorToolRow(
                    onToolSelected = { tool ->
                        when (tool) {
                            EditorTool.TEXT -> viewModel.addTextLayer()
                            EditorTool.DATE -> viewModel.openDialog(ActiveDialog.DATE_FORMAT)
                            EditorTool.SHAPES -> viewModel.openDialog(ActiveDialog.ADD_SHAPE)
                            EditorTool.PHOTO -> pickLayerImage.launch(imagesOnly)
                            EditorTool.CANVAS, EditorTool.EFFECTS -> openTool = tool
                            EditorTool.LAYERS -> viewModel.setLayersDrawerOpen(true)
                        }
                    }
                )
            }
        }
    }

    @Composable
    fun EditorCanvas(canvasModifier: Modifier) {
        PixelCraftCanvas(
            state = state,
            onSelectLayer = { viewModel.selectLayer(it) },
            onDragDelta = { dx, dy, snapThreshold ->
                viewModel.updateLayerPositionWithSnapping(dx, dy, snapThreshold)
            },
            onTransformGesture = { panDx, panDy, zoom, rot ->
                viewModel.transformSelectedLayerGesture(panDx, panDy, zoom, rot)
            },
            onUpdateSelectedLayer = { cont, tf ->
                viewModel.updateSelectedLayer(continuous = cont, transform = tf)
            },
            onDragEnd = { viewModel.clearSnappingGuides() },
            onOpenTextEdit = { viewModel.openDialog(ActiveDialog.TEXT_EDIT) },
            onDeleteSelected = { viewModel.deleteSelectedLayer() },
            onDuplicateSelected = { viewModel.duplicateSelectedLayer() },
            onCopyLayers = { viewModel.copySelectedLayers() },
            onPasteLayers = { viewModel.pasteLayers() },
            onCopyStyle = { viewModel.copySelectedStyle() },
            onPasteStyle = { viewModel.pasteStyleToSelected() },
            onBringToFront = { viewModel.bringLayerToFront() },
            onSendToBack = { viewModel.sendLayerToBack() },
            onToggleLock = { viewModel.toggleLayerLock(it) },
            onEyedropperColorPicked = { viewModel.applyEyedropperColor(it) },
            onCancelEyedropper = { viewModel.cancelEyedropper() },
            modifier = canvasModifier
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown || state.activeDialog != null) {
                    return@onPreviewKeyEvent false
                }
                val step = if (keyEvent.isShiftPressed) 10f else 1f
                when {
                    keyEvent.isCtrlPressed && keyEvent.isShiftPressed && keyEvent.key == Key.Z -> {
                        viewModel.redo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.Z -> {
                        viewModel.undo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.Y -> {
                        viewModel.redo()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.C -> {
                        viewModel.copySelectedLayers()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.V -> {
                        viewModel.pasteLayers()
                        true
                    }
                    keyEvent.isCtrlPressed && keyEvent.key == Key.D -> {
                        viewModel.duplicateSelectedLayer()
                        true
                    }
                    keyEvent.key == Key.Delete || keyEvent.key == Key.Backspace -> {
                        viewModel.deleteSelectedLayer()
                        true
                    }
                    keyEvent.key == Key.DirectionLeft -> {
                        viewModel.nudgeSelectedLayer(-step, 0f)
                        true
                    }
                    keyEvent.key == Key.DirectionRight -> {
                        viewModel.nudgeSelectedLayer(step, 0f)
                        true
                    }
                    keyEvent.key == Key.DirectionUp -> {
                        viewModel.nudgeSelectedLayer(0f, -step)
                        true
                    }
                    keyEvent.key == Key.DirectionDown -> {
                        viewModel.nudgeSelectedLayer(0f, step)
                        true
                    }
                    else -> false
                }
            },
        containerColor = EditorColors.Background,
        topBar = {
            PixelCraftTopBar(
                state = state,
                onNavigateBack = {
                    if (state.isDirty) viewModel.openDialog(ActiveDialog.UNSAVED_CHANGES)
                    else onNavigateHome()
                },
                onOpenDialog = { viewModel.openDialog(it) },
                onToggleGrid = { viewModel.toggleGrid() },
                onToggleSafeZone = { viewModel.toggleSafeZone() },
                onToggleLayers = { viewModel.toggleLayersDrawer() },
                onPasteLayers = { viewModel.pasteLayers() }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EditorColors.CanvasStage)
        ) {
            val isLandscapeWide = maxWidth > 640.dp && maxWidth > maxHeight
            // Compact settings panel has a fixed height (~30% of the editor) so the canvas
            // above it keeps the same size and position no matter which tool is open.
            val toolAreaHeight = maxOf(maxHeight * 0.3f, 120.dp)

            if (isLandscapeWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        EditorCanvas(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        EditorUndoStrip(
                            state = state,
                            onUndo = { viewModel.undo() },
                            onRedo = { viewModel.redo() }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                            .background(EditorColors.Panel)
                            .border(1.dp, EditorColors.Border),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ToolArea()
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    EditorCanvas(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    EditorUndoStrip(
                        state = state,
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(toolAreaHeight)
                            .background(EditorColors.Panel),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ToolArea()
                    }
                }
            }

            // Toasts ("Copied", "Saved"...) float at the top of the canvas so they never
            // cover the tool panel at the bottom.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            ) { data -> EditorToast(data) }

            // First-run Coach Marks card (§ 5.4)
            if (!state.onboardingDismissed) {
                CoachMarksOverlay(
                    onDismiss = { viewModel.dismissOnboarding() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
            }

            if (state.isLayersDrawerOpen) {
                EditorHalfSheet(
                    title = null,
                    onDismiss = { viewModel.setLayersDrawerOpen(false) }
                ) {
                    PixelCraftLayersSheet(
                        layers = state.layers,
                        selectedLayerId = state.selectedLayerId,
                        selectedLayerIds = state.selectedLayerIds,
                        onSelectLayer = { viewModel.selectLayer(it) },
                        onToggleMultiSelect = { viewModel.toggleLayerMultiSelection(it) },
                        onGroupSelected = { viewModel.groupSelectedLayers() },
                        onUngroupSelected = { viewModel.ungroupSelectedLayers() },
                        onRequestRename = { layer ->
                            layerPendingRename = layer
                            viewModel.openDialog(ActiveDialog.RENAME_LAYER)
                        },
                        onToggleLock = { viewModel.toggleLayerLock(it) },
                        onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                        onMoveUp = { id -> viewModel.moveLayerUp(id) },
                        onMoveDown = { id -> viewModel.moveLayerDown(id) },
                        onReorder = { ids -> viewModel.reorderLayers(ids) },
                        onDistribute = { horizontal -> viewModel.distributeSelected(horizontal) },
                        onDelete = { viewModel.deleteLayer(it) },
                        onClose = { viewModel.setLayersDrawerOpen(false) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Legacy dialogs keep the light theme until they get their own pass.
    PixelCraftTheme {
    when (state.activeDialog) {
        ActiveDialog.TEXT_EDIT -> {
            val selected = state.selectedLayer
            if (selected != null && selected.type == LayerType.TEXT) {
                PixelCraftTextEditDialog(
                    initialText = selected.text,
                    onConfirm = { newText ->
                        viewModel.updateSelectedLayer(label = "Edit Text") { it.copy(text = newText) }
                        viewModel.closeDialog()
                    },
                    onDismiss = { viewModel.closeDialog() }
                )
            } else {
                // Fixes Bug 2.9: never mutate ViewModel state synchronously during composition!
                LaunchedEffect(Unit) {
                    viewModel.closeDialog()
                }
            }
        }

        ActiveDialog.IMAGE_SIZE -> {
            PixelCraftImageSizeDialog(
                currentPreset = state.canvasConfig.preset,
                currentWidth = state.canvasConfig.width,
                currentHeight = state.canvasConfig.height,
                onApply = { preset, w, h ->
                    viewModel.setAspectRatioPreset(preset, w, h)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.EXPORT -> {
            // The export sheet belongs to the dark editor chrome.
            PixelCraftEditorTheme {
                PixelCraftExportDialog(
                    canvasConfig = state.canvasConfig,
                    initialOptions = state.exportOptions,
                    hasSelectedLayer = state.selectedLayer != null,
                    isExporting = state.isExporting,
                    onExport = { opts ->
                        viewModel.setExportOptions(opts)
                        exportWithPermission()
                    },
                    onShareDirectly = { opts ->
                        viewModel.setExportOptions(opts)
                        viewModel.closeDialog()
                        viewModel.shareCanvasDirectly()
                    },
                    onSaveAsProject = { viewModel.openDialog(ActiveDialog.SAVE_PROJECT) },
                    onCancelExport = { viewModel.cancelExport() },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
        }

        ActiveDialog.ADD_SHAPE -> {
            PixelCraftShapeDialog(
                onSelectShape = { shape ->
                    viewModel.addShapeLayer(shape)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.SAVE_PROJECT -> {
            PixelCraftSaveProjectDialog(
                initialName = state.currentProjectName,
                onSave = { name ->
                    val shouldExit = exitAfterSave
                    exitAfterSave = false
                    viewModel.saveProject(name) {
                        if (shouldExit) onNavigateHome()
                    }
                },
                onDismiss = {
                    exitAfterSave = false
                    viewModel.closeDialog()
                }
            )
        }

        ActiveDialog.UNSAVED_CHANGES -> {
            PixelCraftUnsavedChangesDialog(
                projectName = state.currentProjectName,
                onSaveAndExit = {
                    exitAfterSave = true
                    viewModel.openDialog(ActiveDialog.SAVE_PROJECT)
                },
                onDiscardAndExit = {
                    viewModel.markCleanAndDiscardDraft()
                    onNavigateHome()
                },
                onCancel = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.RENAME_LAYER -> {
            val target = layerPendingRename ?: state.selectedLayer
            if (target != null) {
                PixelCraftRenameDialog(
                    title = stringResource(R.string.ui_rename_layer),
                    initialName = target.name,
                    onConfirm = { newName ->
                        viewModel.renameLayer(target.id, newName)
                        layerPendingRename = null
                    },
                    onDismiss = {
                        layerPendingRename = null
                        viewModel.closeDialog()
                    }
                )
            } else {
                LaunchedEffect(Unit) { viewModel.closeDialog() }
            }
        }

        ActiveDialog.HISTORY_SHEET -> {
            PixelCraftHistoryDialog(
                undoLabels = state.undoHistoryLabels,
                onJumpToIndex = { idx -> viewModel.jumpToHistoryIndex(idx) },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.DATE_FORMAT -> {
            PixelCraftDateFormatDialog(
                onSelectPattern = { pattern ->
                    viewModel.closeDialog()
                    viewModel.addCurrentDateLayer(pattern)
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.SAVED_STYLES -> {
            PixelCraftSavedStylesDialog(
                hasSelectedLayer = state.selectedLayer != null,
                savedStyles = state.savedStyles,
                onSaveCurrent = { styleName -> viewModel.saveCurrentStyle(styleName) },
                onApplyStyle = { saved ->
                    viewModel.applySavedStyle(saved)
                    viewModel.closeDialog()
                },
                onDeleteStyle = { id -> viewModel.deleteSavedStyle(id) },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.HELP -> {
            PixelCraftAboutDialog(onDismiss = { viewModel.closeDialog() })
        }

        null -> {}
    }
    }
}

@Composable
private fun CoachMarksOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(EditorColors.Surface)
            .border(1.5.dp, EditorColors.Accent, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = EditorColors.Accent,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ui_quick_gestures),
                color = EditorColors.Accent,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.ui_double_tap_text_to_edit_pinch_twist_with),
                color = EditorColors.TextSecondary,
                fontSize = 11.sp
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_dismiss_tips), tint = EditorColors.TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

/** Compact pill toast; shows the action (e.g. Share) only when there is one. */
@Composable
private fun EditorToast(data: SnackbarData) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(EditorColors.SurfaceElevated.copy(alpha = 0.96f))
            .border(1.dp, EditorColors.Border, RoundedCornerShape(20.dp))
            .padding(start = 16.dp, end = if (data.visuals.actionLabel != null) 6.dp else 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = data.visuals.message,
            color = EditorColors.TextPrimary,
            fontSize = 13.sp,
            maxLines = 2
        )
        data.visuals.actionLabel?.let { label ->
            TextButton(onClick = { data.performAction() }) {
                Text(label, color = EditorColors.Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
