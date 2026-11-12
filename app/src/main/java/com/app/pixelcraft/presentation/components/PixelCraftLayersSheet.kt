package com.app.pixelcraft.presentation.components

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
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
import com.app.pixelcraft.ui.theme.PixelCraftColors

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PixelCraftColors.Surface),
        border = BorderStroke(1.dp, PixelCraftColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ui_layers_count, layers.size),
                    fontWeight = FontWeight.Bold,
                    color = PixelCraftColors.TextPrimary,
                    fontSize = 15.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.size(38.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.ui_close_layers), tint = PixelCraftColors.TextSecondary)
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
                            Icon(Icons.Default.GroupWork, contentDescription = null, tint = PixelCraftColors.Accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ui_group_count, selectedLayerIds.size), color = PixelCraftColors.Accent, fontSize = 12.sp)
                        }
                    }
                    if (hasGroupInSelection) {
                        TextButton(onClick = onUngroupSelected) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, tint = PixelCraftColors.AccentHover, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.ui_ungroup), color = PixelCraftColors.AccentHover, fontSize = 12.sp)
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
                        text = stringResource(R.string.ui_no_layers_yet_tap_in_the_top_bar_to_add),
                        color = PixelCraftColors.TextSecondary,
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
                            LayerType.TEXT -> Icons.Default.TextFields to stringResource(R.string.nav_text)
                            LayerType.SHAPE -> Icons.Default.Category to layer.shapeType.localizedLabel()
                            LayerType.IMAGE -> Icons.Default.Image to stringResource(R.string.ui_photo)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPrimary) PixelCraftColors.SurfaceElevated else PixelCraftColors.Background)
                                .border(
                                    width = if (isPrimary) 1.5.dp else 1.dp,
                                    color = when {
                                        isPrimary -> PixelCraftColors.Accent
                                        isMultiSelected -> PixelCraftColors.AccentHover
                                        else -> PixelCraftColors.Border
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
                                    tint = if (isPrimary) PixelCraftColors.Accent else PixelCraftColors.TextSecondary,
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
                                        color = PixelCraftColors.TextPrimary,
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
                                        color = PixelCraftColors.TextSecondary,
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
                                        contentDescription = stringResource(R.string.ui_rename_named, layer.name),
                                        tint = PixelCraftColors.TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleVisibility(layer.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (layer.isVisible) stringResource(R.string.ui_hide_layer) else stringResource(R.string.ui_show_layer),
                                        tint = if (layer.isVisible) PixelCraftColors.Accent else PixelCraftColors.TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onToggleLock(layer.id) },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = if (layer.isLocked) stringResource(R.string.ui_unlock_layer) else stringResource(R.string.ui_lock_layer),
                                        tint = if (layer.isLocked) PixelCraftColors.Warning else PixelCraftColors.TextMuted,
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
                                        contentDescription = stringResource(R.string.ui_move_forward),
                                        tint = if (canMoveUp) PixelCraftColors.TextPrimary else PixelCraftColors.TextMuted.copy(alpha = 0.4f),
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
                                        contentDescription = stringResource(R.string.ui_move_backward),
                                        tint = if (canMoveDown) PixelCraftColors.TextPrimary else PixelCraftColors.TextMuted.copy(alpha = 0.4f),
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
                                        contentDescription = stringResource(R.string.ui_delete_layer),
                                        tint = if (!layer.isLocked) PixelCraftColors.Danger else PixelCraftColors.TextMuted.copy(alpha = 0.4f),
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
