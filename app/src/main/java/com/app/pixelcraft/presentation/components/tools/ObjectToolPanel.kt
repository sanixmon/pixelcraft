package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.ActiveSubTool

@Composable
fun ObjectToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: ((Layer) -> Layer) -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedLayer == null || (selectedLayer.type != LayerType.SHAPE && selectedLayer.type != LayerType.STICKER)) {
        // Quick add buttons for Objects
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF232730))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onOpenDialog(ActiveDialog.ADD_SHAPE) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(Icons.Default.FormatShapes, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shapes", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onOpenDialog(ActiveDialog.ADD_STICKER) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF651FFF))
            ) {
                Icon(Icons.Default.EmojiEmotions, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stickers", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAddImage,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Photo", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF232730))
    ) {
        // Subtool inspector for Shape
        if (activeSubTool != ActiveSubTool.NONE && selectedLayer.type == LayerType.SHAPE) {
            ShapeSubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                onUpdate = onUpdateLayer,
                onClose = { onSubToolSelected(ActiveSubTool.NONE) }
            )
        }

        // Horizontal tool strip for selected object
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selectedLayer.type == LayerType.SHAPE) {
                ObjectToolItem(
                    icon = Icons.Default.Category,
                    label = "Type",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_TYPE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_TYPE) }
                )
                ObjectToolItem(
                    icon = Icons.Default.FormatColorFill,
                    label = "Color",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_COLOR,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_COLOR) }
                )
                ObjectToolItem(
                    icon = Icons.Default.ColorLens,
                    label = "Stroke",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_STROKE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_STROKE) }
                )
                ObjectToolItem(
                    icon = Icons.Default.Opacity,
                    label = "Opacity",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_OPACITY,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_OPACITY) }
                )
            }

            ObjectToolItem(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                isActive = false,
                onClick = { onDuplicateLayer() }
            )
            ObjectToolItem(
                icon = Icons.Default.Delete,
                label = "Delete",
                isActive = false,
                onClick = { onDeleteLayer() }
            )
        }
    }
}

@Composable
private fun ObjectToolItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) activeColor else Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun ShapeSubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    onUpdate: ((Layer) -> Layer) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1D24))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subTool.name.replace("SHAPE_", ""),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 14.sp
            )
            IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (subTool) {
            ActiveSubTool.SHAPE_TYPE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (shape in ShapeType.entries) {
                        FilterChip(
                            selected = layer.shapeType == shape,
                            onClick = { onUpdate { it.copy(shapeType = shape) } },
                            label = { Text(shape.name.replace("_", " ")) }
                        )
                    }
                }
            }

            ActiveSubTool.SHAPE_COLOR -> {
                val palette = listOf(
                    0xFF00B0FF, 0xFFFF0055, 0xFFFFEB3B, 0xFF00E676,
                    0xFF651FFF, 0xFFFF9100, 0xFFFFFFFF, 0xFF000000
                )
                Text("Fill Color", color = Color.White, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (c in palette) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .clickable { onUpdate { it.copy(shapeFillColor = c) } }
                        )
                    }
                }
            }

            ActiveSubTool.SHAPE_STROKE -> {
                Text("Stroke Width: ${layer.shapeStrokeWidth.toInt()} px", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = layer.shapeStrokeWidth,
                    onValueChange = { w -> onUpdate { it.copy(shapeStrokeWidth = w) } },
                    valueRange = 0f..40f
                )
            }

            ActiveSubTool.SHAPE_OPACITY -> {
                Text("Opacity: ${(layer.opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = layer.opacity,
                    onValueChange = { op -> onUpdate { it.copy(opacity = op) } },
                    valueRange = 0f..1f
                )
            }

            else -> {}
        }
    }
}
