package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.R
import com.app.pixelcraft.presentation.PixelCraftUiState
import com.app.pixelcraft.presentation.i18n.historyLabel
import com.app.pixelcraft.ui.theme.EditorColors

/** Thin undo/redo strip that sits between the canvas and the tool row, CapCut-style. */
@Composable
fun EditorUndoStrip(
    state: PixelCraftUiState,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(EditorColors.Background)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onUndo, enabled = state.canUndo, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = state.lastUndoLabel?.let { stringResource(R.string.ui_undo_named, historyLabel(it)) }
                    ?: stringResource(R.string.ui_undo),
                tint = if (state.canUndo) EditorColors.TextPrimary else EditorColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onRedo, enabled = state.canRedo, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = state.lastRedoLabel?.let { stringResource(R.string.ui_redo_named, historyLabel(it)) }
                    ?: stringResource(R.string.ui_redo),
                tint = if (state.canRedo) EditorColors.TextPrimary else EditorColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
