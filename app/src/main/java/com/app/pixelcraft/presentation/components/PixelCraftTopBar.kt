package com.app.pixelcraft.presentation.components

import com.app.pixelcraft.presentation.i18n.historyLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.presentation.PixelCraftUiState

@Composable
fun PixelCraftTopBar(
    state: PixelCraftUiState,
    onNavigateBack: () -> Unit,
    onAddText: () -> Unit,
    onAddDate: () -> Unit,
    onAddShape: () -> Unit,
    onAddImage: () -> Unit,
    onDirectShare: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleSafeZone: () -> Unit,
    onToggleLayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PixelCraftColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(54.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Group: Home/Back + Project Title & Dirty/Autosave Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.ui_back_to_home),
                        tint = PixelCraftColors.TextPrimary
                    )
                }

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onOpenDialog(ActiveDialog.SAVE_PROJECT) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .widthIn(max = 128.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.currentProjectName,
                            color = PixelCraftColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (state.isDirty) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PixelCraftColors.Accent)
                            )
                        }
                    }
                    Text(
                        text = when {
                            state.isDirty && state.lastAutosavedAt != null -> stringResource(R.string.ui_draft_autosaved)
                            state.isDirty -> stringResource(R.string.ui_unsaved_edits)
                            state.currentProjectId != null -> stringResource(R.string.ui_saved)
                            else -> "${state.canvasConfig.width}×${state.canvasConfig.height}"
                        },
                        color = PixelCraftColors.TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Right Group: Primary Editor Actions (all visible without horizontal scroll clipping)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1. Add Menu
                Box {
                    IconButton(
                        onClick = { addMenuExpanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.ui_add_layer),
                            tint = PixelCraftColors.Accent
                        )
                    }
                    DropdownMenu(
                        expanded = addMenuExpanded,
                        onDismissRequest = { addMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_text_layer)) },
                            onClick = {
                                addMenuExpanded = false
                                onAddText()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_date_time_stamp)) },
                            onClick = {
                                addMenuExpanded = false
                                onAddDate()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.FormatShapes, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_shapes_word_balloons)) },
                            onClick = {
                                addMenuExpanded = false
                                onAddShape()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_photo_layer)) },
                            onClick = {
                                addMenuExpanded = false
                                onAddImage()
                            }
                        )
                    }
                }

                // 2. Undo & Redo
                IconButton(
                    onClick = onUndo,
                    enabled = state.canUndo,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = state.lastUndoLabel?.let { stringResource(R.string.ui_undo_named, historyLabel(it)) } ?: stringResource(R.string.ui_undo),
                        tint = if (state.canUndo) PixelCraftColors.TextPrimary else PixelCraftColors.TextMuted.copy(alpha = 0.45f)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = state.canRedo,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = state.lastRedoLabel?.let { stringResource(R.string.ui_redo_named, historyLabel(it)) } ?: stringResource(R.string.ui_redo),
                        tint = if (state.canRedo) PixelCraftColors.TextPrimary else PixelCraftColors.TextMuted.copy(alpha = 0.45f)
                    )
                }

                // 3. Layers Drawer Toggle
                IconButton(
                    onClick = onToggleLayers,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = stringResource(R.string.ui_layers_count, state.layers.size),
                        tint = if (state.isLayersDrawerOpen) PixelCraftColors.Accent else PixelCraftColors.TextPrimary
                    )
                }

                // 4. Direct Share (Bug 2.16: shares via FileProvider cache without polluting Gallery!)
                IconButton(
                    onClick = onDirectShare,
                    enabled = !state.isExporting,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.ui_quick_share),
                        tint = PixelCraftColors.TextPrimary
                    )
                }

                // 5. Export to Gallery
                IconButton(
                    onClick = { onOpenDialog(ActiveDialog.EXPORT) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = stringResource(R.string.ui_export_image),
                        tint = PixelCraftColors.Accent
                    )
                }

                // 6. Secondary More Menu
                Box {
                    IconButton(
                        onClick = { moreMenuExpanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.ui_more_options),
                            tint = PixelCraftColors.TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_save_project)) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenDialog(ActiveDialog.SAVE_PROJECT)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = null,
                                    tint = if (state.isGridEnabled) PixelCraftColors.Accent else PixelCraftColors.TextSecondary
                                )
                            },
                            text = {
                                Text(if (state.isGridEnabled) stringResource(R.string.ui_grid_snap_on) else stringResource(R.string.ui_grid_snap_off))
                            },
                            onClick = {
                                moreMenuExpanded = false
                                onToggleGrid()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CenterFocusStrong,
                                    contentDescription = null,
                                    tint = if (state.showSafeZone) PixelCraftColors.Accent else PixelCraftColors.TextSecondary
                                )
                            },
                            text = {
                                Text(if (state.showSafeZone) stringResource(R.string.ui_safe_zone_guide_on) else stringResource(R.string.ui_safe_zone_guide_off))
                            },
                            onClick = {
                                moreMenuExpanded = false
                                onToggleSafeZone()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_edit_history_count, state.undoHistoryLabels.size)) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenDialog(ActiveDialog.HISTORY_SHEET)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_my_saved_styles)) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenDialog(ActiveDialog.SAVED_STYLES)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, tint = PixelCraftColors.Accent) },
                            text = { Text(stringResource(R.string.ui_canvas_size_dims, state.canvasConfig.width, state.canvasConfig.height)) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenDialog(ActiveDialog.IMAGE_SIZE)
                            }
                        )
                        HorizontalDivider(color = PixelCraftColors.Border)
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = PixelCraftColors.TextSecondary) },
                            text = { Text(stringResource(R.string.ui_about_pixelcraft)) },
                            onClick = {
                                moreMenuExpanded = false
                                onOpenDialog(ActiveDialog.HELP)
                            }
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = PixelCraftColors.Border, thickness = 1.dp)
    }
}
