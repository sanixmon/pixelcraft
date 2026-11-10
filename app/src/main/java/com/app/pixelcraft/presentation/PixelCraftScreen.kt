package com.app.pixelcraft.presentation

import com.app.pixelcraft.presentation.i18n.asString
import com.app.pixelcraft.presentation.i18n.UiText
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
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
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.presentation.components.PixelCraftBottomNavigation
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
import com.app.pixelcraft.presentation.components.tools.ObjectToolPanel
import com.app.pixelcraft.presentation.components.tools.PresetsToolPanel
import com.app.pixelcraft.presentation.components.tools.TextToolPanel

/** Constant bottom reserve in portrait so the canvas slot stays steady across tabs & subtool panels. */
private val PortraitCanvasBottomReserve = 104.dp

@Composable
fun PixelCraftScreen(
    viewModel: PixelCraftViewModel,
    onNavigateHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var layerPendingRename by remember { mutableStateOf<Layer?>(null) }
    var exitAfterSave by remember { mutableStateOf(false) }

    // Ordered BackHandler preventing accidental loss of user work (Fixes Bug 2.1)
    fun handleNavigateBackStep() {
        when {
            state.isEyedropperActive -> viewModel.cancelEyedropper()
            state.activeSubTool != ActiveSubTool.NONE -> viewModel.setActiveSubTool(ActiveSubTool.NONE)
            state.isLayersDrawerOpen -> viewModel.setLayersDrawerOpen(false)
            state.selectedLayerId != null -> viewModel.selectLayer(null)
            state.isDirty -> viewModel.openDialog(ActiveDialog.UNSAVED_CHANGES)
            else -> onNavigateHome()
        }
    }

    BackHandler(enabled = state.activeDialog == null) {
        handleNavigateBackStep()
    }

    val pickLayerImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.addImageLayer(it) }
    }
    val pickBackgroundImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setBackgroundImage(it) }
    }
    val pickCustomFont = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importCustomFont(it) }
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
                withDismissAction = exportedUri != null
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

    @Composable
    fun ActiveToolSheetContent() {
        when (state.activeTab) {
            BottomNavTab.PRESETS -> {
                PresetsToolPanel(
                    savedProjects = state.savedProjects,
                    onApplyPreset = { viewModel.applyPreset(it) },
                    onLoadProject = { viewModel.loadProject(it) },
                    onDeleteProject = { viewModel.deleteProject(it) },
                    onOpenDialog = { viewModel.openDialog(it) }
                )
            }

            BottomNavTab.TEXT -> {
                TextToolPanel(
                    selectedLayer = state.selectedLayer,
                    activeSubTool = state.activeSubTool,
                    recentColors = state.recentColors,
                    favoriteFontKeys = state.favoriteFontKeys,
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
                    onAddText = { viewModel.addTextLayer() }
                )
            }

            BottomNavTab.OBJECTS -> {
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
                    onAddImage = { pickLayerImage.launch(imagesOnly) }
                )
            }

            BottomNavTab.BACKGROUND -> {
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

            BottomNavTab.FILTERS -> {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PixelCraftTopBar(
                state = state,
                onNavigateBack = {
                    if (state.isDirty) viewModel.openDialog(ActiveDialog.UNSAVED_CHANGES)
                    else onNavigateHome()
                },
                onAddText = { viewModel.addTextLayer() },
                onAddDate = { viewModel.openDialog(ActiveDialog.DATE_FORMAT) },
                onAddShape = { viewModel.openDialog(ActiveDialog.ADD_SHAPE) },
                onAddImage = { pickLayerImage.launch(imagesOnly) },
                onDirectShare = { viewModel.shareCanvasDirectly() },
                onOpenDialog = { viewModel.openDialog(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onToggleGrid = { viewModel.toggleGrid() },
                onToggleSafeZone = { viewModel.toggleSafeZone() },
                onToggleLayers = { viewModel.toggleLayersDrawer() }
            )
        },
        bottomBar = {
            PixelCraftBottomNavigation(
                activeTab = state.activeTab,
                onTabSelected = { viewModel.setActiveTab(it) }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF12151E))
        ) {
            val isLandscapeWide = maxWidth > 640.dp && maxWidth > maxHeight

            if (isLandscapeWide) {
                // Responsive Landscape / Tablet side-by-side workspace (§ 5.3)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
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
                            onCopyStyle = { viewModel.copySelectedStyle() },
                            onPasteStyle = { viewModel.pasteStyleToSelected() },
                            onBringToFront = { viewModel.bringLayerToFront() },
                            onSendToBack = { viewModel.sendLayerToBack() },
                            onToggleLock = { viewModel.toggleLayerLock(it) },
                            onEyedropperColorPicked = { viewModel.applyEyedropperColor(it) },
                            onCancelEyedropper = { viewModel.cancelEyedropper() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF1A1E29))
                            .shadow(10.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        ActiveToolSheetContent()
                    }
                }
            } else {
                // Portrait layout with stable canvas slot + anchored bottom tool panel
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
                    onCopyStyle = { viewModel.copySelectedStyle() },
                    onPasteStyle = { viewModel.pasteStyleToSelected() },
                    onBringToFront = { viewModel.bringLayerToFront() },
                    onSendToBack = { viewModel.sendLayerToBack() },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onEyedropperColorPicked = { viewModel.applyEyedropperColor(it) },
                    onCancelEyedropper = { viewModel.cancelEyedropper() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = PortraitCanvasBottomReserve)
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .shadow(10.dp)
                ) {
                    ActiveToolSheetContent()
                }
            }

            // First-run Coach Marks card (§ 5.4)
            if (!state.onboardingDismissed) {
                CoachMarksOverlay(
                    onDismiss = { viewModel.dismissOnboarding() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
            }

            // Floating Layers Drawer
            if (state.isLayersDrawerOpen) {
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
                    onDelete = { viewModel.deleteLayer(it) },
                    onClose = { viewModel.setLayersDrawerOpen(false) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }

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
                onCancelExport = { viewModel.cancelExport() },
                onDismiss = { viewModel.closeDialog() }
            )
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

@Composable
private fun CoachMarksOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xEE181D2A))
            .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = null,
            tint = Color(0xFF00E5FF),
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ui_quick_gestures),
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = stringResource(R.string.ui_double_tap_text_to_edit_pinch_twist_with),
                color = Color(0xFFD5DAE5),
                fontSize = 11.sp
            )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.ui_dismiss_tips), tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}
