package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.presentation.PixelCraftUiState

@Composable
fun PixelCraftTopBar(
    state: PixelCraftUiState,
    onAddText: () -> Unit,
    onAddDate: () -> Unit,
    onAddSticker: () -> Unit,
    onAddShape: () -> Unit,
    onAddImage: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleLayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    var addMenuExpanded by remember { mutableStateOf(false) }
    var saveMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF20232A))
            .statusBarsPadding()
            .height(52.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Plus Button (Add Menu)
        Box {
            IconButton(onClick = { addMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF00E5FF)
                )
            }
            DropdownMenu(
                expanded = addMenuExpanded,
                onDismissRequest = { addMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Text") },
                    onClick = {
                        addMenuExpanded = false
                        onAddText()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Current date") },
                    onClick = {
                        addMenuExpanded = false
                        onAddDate()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Photo") },
                    onClick = {
                        addMenuExpanded = false
                        onAddImage()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sticker") },
                    onClick = {
                        addMenuExpanded = false
                        onAddSticker()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Shapes") },
                    onClick = {
                        addMenuExpanded = false
                        onAddShape()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Quotes") },
                    onClick = {
                        addMenuExpanded = false
                        onOpenDialog(ActiveDialog.QUOTES)
                    }
                )
            }
        }

        // 2. Save Button
        Box {
            IconButton(onClick = { saveMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = saveMenuExpanded,
                onDismissRequest = { saveMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Save as project") },
                    onClick = {
                        saveMenuExpanded = false
                        onOpenDialog(ActiveDialog.SAVE_PROJECT)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Save as image") },
                    onClick = {
                        saveMenuExpanded = false
                        onOpenDialog(ActiveDialog.EXPORT)
                    }
                )
            }
        }

        // 3. Share Button
        IconButton(onClick = { onOpenDialog(ActiveDialog.EXPORT) }) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White
            )
        }

        // 4. Quotes Button
        IconButton(onClick = { onOpenDialog(ActiveDialog.QUOTES) }) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = "Quotes",
                tint = Color.White
            )
        }

        // 5. Undo & Redo
        IconButton(
            onClick = onUndo,
            enabled = state.canUndo
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "Undo",
                tint = if (state.canUndo) Color.White else Color(0xFF616161)
            )
        }

        IconButton(
            onClick = onRedo,
            enabled = state.canRedo
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Redo,
                contentDescription = "Redo",
                tint = if (state.canRedo) Color.White else Color(0xFF616161)
            )
        }

        // 6. Grid Overlay
        IconButton(onClick = onToggleGrid) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid",
                tint = if (state.isGridEnabled) Color(0xFF00E5FF) else Color.White
            )
        }

        // 7. Layers Panel Toggle
        IconButton(onClick = onToggleLayers) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "Layers",
                tint = if (state.isLayersDrawerOpen) Color(0xFF00E5FF) else Color.White
            )
        }

        // 8. More Menu
        Box {
            IconButton(onClick = { moreMenuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Image size") },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.IMAGE_SIZE)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export image") },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.EXPORT)
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("About PixelCraft") },
                    onClick = {
                        moreMenuExpanded = false
                        onOpenDialog(ActiveDialog.HELP)
                    }
                )
            }
        }
    }
}
