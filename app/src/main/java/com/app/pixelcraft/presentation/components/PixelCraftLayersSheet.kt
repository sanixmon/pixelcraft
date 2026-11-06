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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
    onSelectLayer: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Reverse ascending zIndex so top-to-bottom matches render order even if ties exist
    val sortedLayers = layers.sortedBy { it.zIndex }.asReversed()

    Card(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight(0.7f)
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
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
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (layers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No layers yet", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(sortedLayers, key = { _, layer -> layer.id }) { index, layer ->
                        val isSelected = layer.id == selectedLayerId
                        val canMoveUp = index > 0 && !layer.isLocked
                        val canMoveDown = index < sortedLayers.lastIndex && !layer.isLocked

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF2C3240) else Color(0xFF181A20))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF333333),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectLayer(layer.id) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (layer.type == LayerType.TEXT) {
                                        layer.text.replace("\n", " ").take(18)
                                    } else {
                                        layer.name
                                    },
                                    color = Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = layer.type.name,
                                    color = Color(0xFF888888),
                                    fontSize = 9.sp
                                )
                            }

                            IconButton(
                                onClick = { onToggleVisibility(layer.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibility",
                                    tint = if (layer.isVisible) Color(0xFF00E5FF) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onToggleLock(layer.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock",
                                    tint = if (layer.isLocked) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onMoveUp(layer.id) },
                                enabled = canMoveUp,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (canMoveUp) Color.White else Color(0xFF444444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onMoveDown(layer.id) },
                                enabled = canMoveDown,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (canMoveDown) Color.White else Color(0xFF444444),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onDelete(layer.id) },
                                enabled = !layer.isLocked,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = if (!layer.isLocked) Color(0xFFFF5252) else Color(0xFF553333),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
