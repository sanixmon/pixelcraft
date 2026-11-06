package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType

@Composable
fun PixelCraftLayersSheet(
    layers: List<Layer>,
    selectedLayerId: String?,
    selectedLayerIds: Set<String> = emptySet(),
    onSelectLayer: (String) -> Unit,
    onToggleMultiSelect: (String) -> Unit = {},
    onGroupSelected: () -> Unit = {},
    onUngroupSelected: () -> Unit = {},
    onRequestRename: (Layer) -> Unit = {},
    onToggleLock: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedLayers = layers.sortedBy { it.zIndex }.asReversed()
    val hasGroupInSelection = layers.any { it.id in selectedLayerIds && it.groupId != null }

    Card(
        modifier = modifier
            .width(316.dp)
            .fillMaxHeight(0.74f)
            .padding(8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1E29)),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Layers (${layers.size})",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    fontSize = 15.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Layers", tint = Color.White)
                }
            }

            if (selectedLayerIds.size >= 2 || hasGroupInSelection) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedLayerIds.size >= 2) {
                        TextButton(onClick = onGroupSelected) {
                            Icon(Icons.Default.GroupWork, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Group (${selectedLayerIds.size})", color = Color(0xFF00E5FF), fontSize = 12.sp)
                        }
                    }
                    if (hasGroupInSelection) {
                        TextButton(onClick = onUngroupSelected) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ungroup", color = Color(0xFFFBBF24), fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (layers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No layers yet — tap + in the top bar to add text, shapes, or photos.",
                        color = Color(0xFF949CB0),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedLayers, key = { _, layer -> layer.id }) { index, layer ->
                        val isPrimary = layer.id == selectedLayerId
                        val isMultiSelected = layer.id in selectedLayerIds
                        val canMoveUp = index > 0 && !layer.isLocked
                        val canMoveDown = index < sortedLayers.lastIndex && !layer.isLocked

                        val (typeIcon, typeLabel) = when (layer.type) {
                            LayerType.TEXT -> Icons.Default.TextFields to "Text"
                            LayerType.SHAPE -> Icons.Default.Category to layer.shapeType.label
                            LayerType.IMAGE -> Icons.Default.Image to "Photo"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPrimary) Color(0xFF263042) else Color(0xFF13161F))
                                .border(
                                    width = if (isPrimary) 1.5.dp else 1.dp,
                                    color = when {
                                        isPrimary -> Color(0xFF00E5FF)
                                        isMultiSelected -> Color(0xFF7C4DFF)
                                        else -> Color(0xFF2B3140)
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectLayer(layer.id) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isMultiSelected || isPrimary,
                                    onCheckedChange = { onToggleMultiSelect(layer.id) },
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = typeLabel,
                                    tint = if (isPrimary) Color(0xFF00E5FF) else Color(0xFFA0A8B8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (layer.type == LayerType.TEXT) {
                                            layer.text.replace("\n", " ").ifBlank { layer.name }
                                        } else {
                                            layer.name
                                        },
                                        color = Color.White,
                                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append(typeLabel)
                                            if (layer.groupId != null) append(" • Grouped")
                                        },
                                        color = Color(0xFF8E96A8),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onRequestRename(layer) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename ${layer.name}",
                                        tint = Color(0xFFA0A8B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleVisibility(layer.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (layer.isVisible) "Hide Layer" else "Show Layer",
                                        tint = if (layer.isVisible) Color(0xFF00E5FF) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleLock(layer.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = if (layer.isLocked) "Unlock Layer" else "Lock Layer",
                                        tint = if (layer.isLocked) Color(0xFFFFB300) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onMoveUp(layer.id) },
                                    enabled = canMoveUp,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Move Forward",
                                        tint = if (canMoveUp) Color.White else Color(0xFF444A58),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onMoveDown(layer.id) },
                                    enabled = canMoveDown,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Move Backward",
                                        tint = if (canMoveDown) Color.White else Color(0xFF444A58),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(layer.id) },
                                    enabled = !layer.isLocked,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Layer",
                                        tint = if (!layer.isLocked) Color(0xFFFF5252) else Color(0xFF553333),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
