package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

private val ObjectPalette = listOf(
    0xFF00B0FF, 0xFFFF0055, 0xFFFFEB3B, 0xFF00E676,
    0xFF651FFF, 0xFFFF9100, 0xFFFFFFFF, 0xFF000000
)

@Composable
fun ObjectToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (
        selectedLayer == null || (
            selectedLayer.type != LayerType.SHAPE &&
                selectedLayer.type != LayerType.STICKER &&
                selectedLayer.type != LayerType.IMAGE
            )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF232730))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
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
        if (activeSubTool != ActiveSubTool.NONE) {
            ObjectSubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                onUpdate = onUpdateLayer,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onClose = { onSubToolSelected(ActiveSubTool.NONE) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ObjectToolItem(
                icon = Icons.Default.AspectRatio,
                label = "Size",
                isActive = activeSubTool == ActiveSubTool.OBJECT_SIZE,
                onClick = { onSubToolSelected(ActiveSubTool.OBJECT_SIZE) }
            )
            ObjectToolItem(
                icon = Icons.Default.Rotate90DegreesCcw,
                label = "Rotate",
                isActive = activeSubTool == ActiveSubTool.OBJECT_ROTATE,
                onClick = { onSubToolSelected(ActiveSubTool.OBJECT_ROTATE) }
            )
            ObjectToolItem(
                icon = Icons.Default.Opacity,
                label = "Opacity",
                isActive = activeSubTool == ActiveSubTool.SHAPE_OPACITY,
                onClick = { onSubToolSelected(ActiveSubTool.SHAPE_OPACITY) }
            )

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
            }

            ObjectToolItem(
                icon = Icons.Default.Layers,
                label = "To Front",
                isActive = false,
                onClick = onBringToFront
            )
            ObjectToolItem(
                icon = Icons.Default.Layers,
                label = "To Back",
                isActive = false,
                onClick = onSendToBack
            )
            ObjectToolItem(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                isActive = false,
                onClick = onDuplicateLayer
            )
            ObjectToolItem(
                icon = Icons.Default.Delete,
                label = "Delete",
                isActive = false,
                onClick = onDeleteLayer
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
private fun ObjectSubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
            .background(Color(0xFF1B1D24))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subTool.name
                    .removePrefix("SHAPE_")
                    .removePrefix("OBJECT_")
                    .replace("_", " "),
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
            ActiveSubTool.OBJECT_SIZE -> {
                when (layer.type) {
                    LayerType.SHAPE -> {
                        Text("Width: ${layer.width.toInt()} px", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = layer.width.coerceIn(40f, 1600f),
                            onValueChange = { w -> onUpdate(true) { it.copy(width = w) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = 40f..1600f
                        )
                        Text("Height: ${layer.height.toInt()} px", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = layer.height.coerceIn(40f, 1600f),
                            onValueChange = { h -> onUpdate(true) { it.copy(height = h) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = 40f..1600f
                        )
                        if (layer.shapeType == ShapeType.ROUNDED_RECTANGLE || layer.shapeType == ShapeType.SPEECH_BUBBLE) {
                            Text("Corner Radius: ${layer.shapeCornerRadius.toInt()} px", color = Color.White, fontSize = 12.sp)
                            Slider(
                                value = layer.shapeCornerRadius.coerceIn(0f, 120f),
                                onValueChange = { r -> onUpdate(true) { it.copy(shapeCornerRadius = r) } },
                                onValueChangeFinished = onFinishContinuousEdit,
                                valueRange = 0f..120f
                            )
                        }
                    }

                    LayerType.STICKER -> {
                        Text("Sticker Size: ${layer.fontSize.toInt()}", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = layer.fontSize.coerceIn(36f, 220f),
                            onValueChange = { s -> onUpdate(true) { it.copy(fontSize = s) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = 36f..220f
                        )
                    }

                    LayerType.IMAGE -> {
                        Text("Width: ${layer.width.toInt()} px (aspect locked)", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = layer.width.coerceIn(80f, 2200f),
                            onValueChange = { newW ->
                                onUpdate(true) { current ->
                                    val ratio = current.height.coerceAtLeast(1f) / current.width.coerceAtLeast(1f)
                                    current.copy(
                                        width = newW,
                                        height = (newW * ratio).coerceIn(40f, 2400f)
                                    )
                                }
                            },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = 80f..2200f
                        )
                    }

                    else -> {}
                }
            }

            ActiveSubTool.OBJECT_ROTATE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Angle: ${layer.rotation.toInt()}°", color = Color.White, fontSize = 12.sp)
                    if (layer.rotation != 0f) {
                        TextButton(onClick = { onUpdate(false) { it.copy(rotation = 0f) } }) {
                            Text("Reset 0°", color = Color(0xFF00E5FF), fontSize = 11.sp)
                        }
                    }
                }
                Slider(
                    value = layer.rotation.coerceIn(-180f, 180f),
                    onValueChange = { r -> onUpdate(true) { it.copy(rotation = r) } },
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = -180f..180f
                )
            }

            ActiveSubTool.SHAPE_TYPE -> {
                if (layer.type == LayerType.SHAPE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (shape in ShapeType.entries) {
                            FilterChip(
                                selected = layer.shapeType == shape,
                                onClick = { onUpdate(false) { it.copy(shapeType = shape) } },
                                label = { Text(shape.name.replace("_", " ")) }
                            )
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_COLOR -> {
                if (layer.type == LayerType.SHAPE) {
                    Text("Fill Color", color = Color.White, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = (layer.shapeFillColor ushr 24) == 0L,
                            onClick = { onUpdate(false) { it.copy(shapeFillColor = 0x00000000) } },
                            label = { Text("None") }
                        )
                        for (c in ObjectPalette) {
                            val isSelected = layer.shapeFillColor == c
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(c))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF555555),
                                        shape = CircleShape
                                    )
                                    .clickable { onUpdate(false) { it.copy(shapeFillColor = c) } }
                            )
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_STROKE -> {
                if (layer.type == LayerType.SHAPE) {
                    Text("Stroke Width: ${layer.shapeStrokeWidth.toInt()} px", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.shapeStrokeWidth.coerceIn(0f, 40f),
                        onValueChange = { w -> onUpdate(true) { it.copy(shapeStrokeWidth = w) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..40f
                    )
                    if (layer.shapeStrokeWidth > 0f) {
                        Text("Stroke Color", color = Color.White, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (c in ObjectPalette) {
                                val isSelected = layer.shapeStrokeColor == c
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(c))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF555555),
                                            shape = CircleShape
                                        )
                                        .clickable { onUpdate(false) { it.copy(shapeStrokeColor = c) } }
                                )
                            }
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_OPACITY -> {
                Text("Opacity: ${(layer.opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = layer.opacity.coerceIn(0f, 1f),
                    onValueChange = { op -> onUpdate(true) { it.copy(opacity = op) } },
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 0f..1f
                )
            }

            else -> {}
        }
    }
}
