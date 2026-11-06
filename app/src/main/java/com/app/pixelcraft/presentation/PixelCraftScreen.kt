package com.app.pixelcraft.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.presentation.components.PixelCraftBottomNavigation
import com.app.pixelcraft.presentation.components.PixelCraftCanvas
import com.app.pixelcraft.presentation.components.PixelCraftLayersSheet
import com.app.pixelcraft.presentation.components.PixelCraftTopBar
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftAboutDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftExportDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftImageSizeDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftQuotesDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftSaveProjectDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftShapeDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftStickerDialog
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftTextEditDialog
import com.app.pixelcraft.presentation.components.tools.BackgroundToolPanel
import com.app.pixelcraft.presentation.components.tools.FilterToolPanel
import com.app.pixelcraft.presentation.components.tools.ObjectToolPanel
import com.app.pixelcraft.presentation.components.tools.PresetsToolPanel
import com.app.pixelcraft.presentation.components.tools.TextToolPanel

@Composable
fun PixelCraftScreen(
    viewModel: PixelCraftViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current

    val pickLayerImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.addImageLayer(it) }
    }
    val pickBackgroundImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.setBackgroundImage(it) }
    }
    val imagesOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    // Android 8–9 need storage permission to write into the shared Pictures folder.
    val requestStoragePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.exportCanvas()
        else viewModel.reportMessage("Storage permission is needed to save to the gallery")
    }
    val exportWithPermission = {
        val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else viewModel.exportCanvas()
    }

    LaunchedEffect(state.userMessage) {
        val msg = state.userMessage
        if (msg != null) {
            val exportedUri = state.lastExportedUri
            viewModel.clearUserMessage()
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (exportedUri != null) "Share" else null,
                withDismissAction = exportedUri != null
            )
            if (result == SnackbarResult.ActionPerformed && exportedUri != null) {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = context.contentResolver.getType(exportedUri) ?: "image/*"
                    putExtra(Intent.EXTRA_STREAM, exportedUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(send, "Share image"))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PixelCraftTopBar(
                state = state,
                onAddText = { viewModel.addTextLayer() },
                onAddDate = { viewModel.addCurrentDateLayer() },
                onAddSticker = { viewModel.openDialog(ActiveDialog.ADD_STICKER) },
                onAddShape = { viewModel.openDialog(ActiveDialog.ADD_SHAPE) },
                onOpenDialog = { viewModel.openDialog(it) },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onToggleZoom = { viewModel.toggleZoomMode() },
                onToggleGrid = { viewModel.toggleGrid() },
                onToggleLayers = { viewModel.toggleLayersDrawer() }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Active Tool Panel above the bottom navigation
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
                            onSubToolSelected = { viewModel.setActiveSubTool(it) },
                            onUpdateLayer = { viewModel.updateSelectedLayer(it) },
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
                            onSubToolSelected = { viewModel.setActiveSubTool(it) },
                            onUpdateLayer = { viewModel.updateSelectedLayer(it) },
                            onDeleteLayer = { viewModel.deleteSelectedLayer() },
                            onDuplicateLayer = { viewModel.duplicateSelectedLayer() },
                            onOpenDialog = { viewModel.openDialog(it) },
                            onAddImage = { pickLayerImage.launch(imagesOnly) }
                        )
                    }

                    BottomNavTab.BACKGROUND -> {
                        BackgroundToolPanel(
                            canvasConfig = state.canvasConfig,
                            onSetColor = { viewModel.setBackgroundColor(it) },
                            onSetGradient = { colors, angle -> viewModel.setBackgroundGradient(colors, angle) },
                            onSetTransparent = { viewModel.setBackgroundTransparent() },
                            onPickImage = { pickBackgroundImage.launch(imagesOnly) },
                            onOpenDialog = { viewModel.openDialog(it) }
                        )
                    }

                    BottomNavTab.FILTERS -> {
                        FilterToolPanel(
                            filters = state.canvasConfig.filters,
                            onUpdateFilters = { viewModel.updateCanvasFilters(it) }
                        )
                    }
                }

                // 5 Iconic Bottom Tabs
                PixelCraftBottomNavigation(
                    activeTab = state.activeTab,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF14161D))
        ) {
            // Main Interactive Canvas
            PixelCraftCanvas(
                state = state,
                onSelectLayer = { viewModel.selectLayer(it) },
                onDragDelta = { dx, dy -> viewModel.updateLayerPositionWithSnapping(dx, dy) },
                onDragEnd = { viewModel.clearSnappingGuides() },
                onResizeLayer = { dx, dy ->
                    viewModel.updateSelectedLayer {
                        it.copy(
                            width = (it.width + dx).coerceAtLeast(60f),
                            height = (it.height + dy).coerceAtLeast(40f)
                        )
                    }
                },
                onRotateLayer = { dRot ->
                    viewModel.updateSelectedLayer {
                        it.copy(rotation = (it.rotation + dRot).coerceIn(-180f, 180f))
                    }
                },
                onDeleteLayer = { viewModel.deleteSelectedLayer() },
                onDuplicateLayer = { viewModel.duplicateSelectedLayer() },
                onZoomAndPan = { zoom, pan -> viewModel.updateZoomAndPan(zoom, pan) }
            )

            // Floating Layers Sheet on Right Side
            if (state.isLayersDrawerOpen) {
                PixelCraftLayersSheet(
                    layers = state.layers,
                    selectedLayerId = state.selectedLayerId,
                    onSelectLayer = { viewModel.selectLayer(it) },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                    onMoveUp = { idx -> viewModel.reorderLayers(idx, idx - 1) },
                    onMoveDown = { idx -> viewModel.reorderLayers(idx, idx + 1) },
                    onDuplicate = { viewModel.duplicateSelectedLayer() },
                    onDelete = { viewModel.deleteSelectedLayer() },
                    onClose = { viewModel.setLayersDrawerOpen(false) },
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }

    // Dialogs
    when (state.activeDialog) {
        ActiveDialog.TEXT_EDIT -> {
            PixelCraftTextEditDialog(
                initialText = state.selectedLayer?.text ?: "New Text",
                onConfirm = { newText ->
                    viewModel.updateSelectedLayer { it.copy(text = newText) }
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.QUOTES -> {
            PixelCraftQuotesDialog(
                onQuoteSelected = { quote ->
                    viewModel.addQuoteLayer(quote)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
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
                isExporting = state.isExporting,
                onExport = { opts ->
                    viewModel.setExportOptions(opts)
                    exportWithPermission()
                },
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

        ActiveDialog.ADD_STICKER -> {
            PixelCraftStickerDialog(
                onSelectSticker = { stickerKey ->
                    viewModel.addStickerLayer(stickerKey)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.SAVE_PROJECT -> {
            PixelCraftSaveProjectDialog(
                initialName = state.currentProjectName,
                onSave = { name -> viewModel.saveProject(name) },
                onDismiss = { viewModel.closeDialog() }
            )
        }

        ActiveDialog.OPEN_PROJECT -> {
            // Already handled via PresetsToolPanel or open project dialog
            viewModel.closeDialog()
        }

        ActiveDialog.HELP -> {
            PixelCraftAboutDialog(onDismiss = { viewModel.closeDialog() })
        }

        else -> {}
    }
}
