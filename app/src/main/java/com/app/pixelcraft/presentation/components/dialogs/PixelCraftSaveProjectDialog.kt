package com.app.pixelcraft.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.BuildConfig
import com.app.pixelcraft.data.model.SavedLayerStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PixelCraftSaveProjectDialog(
    initialName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = "Save Project",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Project Name", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.ifBlank { "Untitled Project" }) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelCraftUnsavedChangesDialog(
    projectName: String,
    onSaveAndExit: () -> Unit,
    onDiscardAndExit: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = "Save Changes?",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "You have unsaved edits in \"$projectName\". Save as a project before leaving?",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onDiscardAndExit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Discard Changes", color = Color(0xFFFF5252), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveAndExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save & Exit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Keep Editing", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelCraftRenameDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), fontSize = 18.sp)
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text.trim().ifBlank { initialName }) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("Rename", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelCraftHistoryDialog(
    undoLabels: List<String>,
    onJumpToIndex: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF00E5FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit History (${undoLabels.size})", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            if (undoLabels.isEmpty()) {
                Text("No edits recorded in this session yet.", color = Color(0xFFA0A8B8), fontSize = 13.sp)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val reversed = undoLabels.mapIndexed { idx, label -> idx to label }.asReversed()
                    itemsIndexed(reversed) { _, (originalIndex, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF151821))
                                .clickable { onJumpToIndex(originalIndex) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${originalIndex + 1}. $label",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Restore",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelCraftDateFormatDialog(
    onSelectPattern: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val patterns = listOf(
        "EEEE, d MMMM yyyy",
        "d MMMM yyyy",
        "dd/MM/yyyy",
        "yyyy-MM-dd",
        "EEE, MMM d • HH:mm",
        "HH:mm"
    )
    val now = remember { Date() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text("Insert Date / Time Stamp", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (pat in patterns) {
                    val preview = remember(pat) {
                        runCatching { SimpleDateFormat(pat, Locale.getDefault()).format(now) }.getOrDefault(pat)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF151821))
                            .clickable { onSelectPattern(pat) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(preview, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

@Composable
fun PixelCraftSavedStylesDialog(
    hasSelectedLayer: Boolean,
    savedStyles: List<SavedLayerStyle>,
    onSaveCurrent: (String) -> Unit,
    onApplyStyle: (SavedLayerStyle) -> Unit,
    onDeleteStyle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var styleName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text("My Saved Styles", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (hasSelectedLayer) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = styleName,
                            onValueChange = { styleName = it },
                            placeholder = { Text("New style name...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveCurrent(styleName)
                                styleName = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (savedStyles.isEmpty()) {
                    Text(
                        text = "Save your favorite text & shape styles here to reuse across any project.",
                        color = Color(0xFFA0A8B8),
                        fontSize = 13.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(savedStyles, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF151821))
                                    .clickable(enabled = hasSelectedLayer) { onApplyStyle(item) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "Font: ${item.styleLayer.fontKey} • Stroke: ${item.styleLayer.strokeWidth.toInt()}px",
                                        color = Color(0xFF8E96A8),
                                        fontSize = 11.sp
                                    )
                                }
                                IconButton(onClick = { onDeleteStyle(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Style", tint = Color(0xFFFF5252))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = Color(0xFF00E5FF))
            }
        }
    )
}

@Composable
fun PixelCraftAboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = "PixelCraft v${BuildConfig.VERSION_NAME}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Offline-first typography, vector graphics & photo composition studio built with Jetpack Compose & Material 3.",
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Highlights:\n" +
                        "• 15 bundled open-source fonts + custom .ttf/.otf import\n" +
                        "• Curved arc text, letter spacing, gradient fill, neon glow & 3D extrusion\n" +
                        "• Vector path strokes (Outside / Center / Inside, Dash patterns, Join & Cap)\n" +
                        "• 24 vector shapes & word balloons + photo crop, filters & shape masks\n" +
                        "• 2-finger pinch/rotate gestures, Smart Guides, Canvas Eyedropper & autosave\n" +
                        "• PNG, JPEG & WebP (Lossy/Lossless) export + portable .pixelcraft archives",
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
