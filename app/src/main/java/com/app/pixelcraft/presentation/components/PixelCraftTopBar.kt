package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.presentation.PixelCraftUiState
import com.app.pixelcraft.ui.theme.EditorColors

/**
 * CapCut-style minimal editor top bar: back on the left, Layers + overflow + Export pill on the right.
 * Undo/redo live in a strip under the canvas; save and share live in the Export sheet.
 */
@Composable
fun PixelCraftTopBar(
    state: PixelCraftUiState,
    onNavigateBack: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleSafeZone: () -> Unit,
    onToggleLayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorColors.Background)
            .statusBarsPadding()
            .height(52.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ui_back_to_home),
                tint = EditorColors.TextPrimary
            )
        }

        // Unsaved-changes indicator: a dot instead of a status line.
        if (state.isDirty) {
            val statusDescription = if (state.lastAutosavedAt != null) {
                stringResource(R.string.ui_draft_autosaved)
            } else {
                stringResource(R.string.ui_unsaved_edits)
            }
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(EditorColors.Accent)
                    .semantics { contentDescription = statusDescription }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onToggleLayers, modifier = Modifier.size(44.dp)) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = stringResource(R.string.ui_layers_count, state.layers.size),
                tint = if (state.isLayersDrawerOpen) EditorColors.Accent else EditorColors.TextPrimary
            )
        }

        Box {
            IconButton(onClick = { moreMenuExpanded = true }, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.ui_more_options),
                    tint = EditorColors.TextPrimary
                )
            }
            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            tint = if (state.isGridEnabled) EditorColors.Accent else EditorColors.TextSecondary
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
                            tint = if (state.showSafeZone) EditorColors.Accent else EditorColors.TextSecondary
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
                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = EditorColors.TextSecondary) },
                    text = { Text(stringResource(R.string.ui_edit_history_count, state.undoHistoryLabels.size)) },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.HISTORY_SHEET)
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Bookmarks, contentDescription = null, tint = EditorColors.TextSecondary) },
                    text = { Text(stringResource(R.string.ui_my_saved_styles)) },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.SAVED_STYLES)
                    }
                )
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.AspectRatio, contentDescription = null, tint = EditorColors.TextSecondary) },
                    text = { Text(stringResource(R.string.ui_canvas_size_dims, state.canvasConfig.width, state.canvasConfig.height)) },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.IMAGE_SIZE)
                    }
                )
                HorizontalDivider(color = EditorColors.Border)
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = EditorColors.TextSecondary) },
                    text = { Text(stringResource(R.string.ui_about_pixelcraft)) },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.HELP)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(EditorColors.Accent)
                .clickable { onOpenDialog(ActiveDialog.EXPORT) }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.action_export),
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}
