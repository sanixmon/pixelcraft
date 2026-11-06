package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow

private val ObjectPalette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFF00B0FF, 0xFFFF0055,
    0xFFFFEB3B, 0xFF00E676, 0xFF651FFF, 0xFFFF9100
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
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onOpenDialog(ActiveDialog.ADD_SHAPE) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(Icons.Default.FormatShapes, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shapes & Balloons", color = Color.Black, fontWeight = FontWeight.Bold)
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
            if (selectedLayer.type == LayerType.SHAPE) {
                ObjectToolItem(
                    icon = Icons.Default.Category,
                    label = "Shape",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_TYPE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_TYPE) }
                )
                ObjectToolItem(
                    icon = Icons.Default.FormatColorFill,
                    label = "Fill",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_COLOR,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_COLOR) }
                )
                ObjectToolItem(
                    icon = Icons.Default.BorderStyle,
                    label = "Stroke",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_STROKE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_STROKE) }
                )
                ObjectToolItem(
                    icon = Icons.Default.Lightbulb,
                    label = "Shadow",
                    isActive = activeSubTool == ActiveSubTool.SHAPE_SHADOW,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_SHADOW) }
                )
                ObjectToolItem(
                    icon = Icons.Default.Flip,
                    label = "Flip H",
                    isActive = selectedLayer.flipHorizontal,
                    onClick = {
                        onUpdateLayer(false) { it.copy(flipHorizontal = !it.flipHorizontal) }
                    }
                )
                ObjectToolItem(
                    icon = Icons.Default.Flip,
                    label = "Flip V",
                    isActive = selectedLayer.flipVertical,
                    iconRotation = 90f,
                    onClick = {
                        onUpdateLayer(false) { it.copy(flipVertical = !it.flipVertical) }
                    }
                )
            }

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
    iconRotation: Float = 0f,
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
            modifier = Modifier
                .size(22.dp)
                .rotate(iconRotation)
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
            .heightIn(max = 225.dp)
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
                                value = layer.shapeCornerRadius.coerceIn(0f, 140f),
                                onValueChange = { r -> onUpdate(true) { it.copy(shapeCornerRadius = r) } },
                                onValueChangeFinished = onFinishContinuousEdit,
                                valueRange = 0f..140f
                            )
                        }
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
                    var activeCat by remember(layer.shapeType) { mutableStateOf(layer.shapeType.category) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (cat in ShapeCategory.entries) {
                            FilterChip(
                                selected = activeCat == cat,
                                onClick = { activeCat = cat },
                                label = { Text(cat.label, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (shape in ShapeType.entries.filter { it.category == activeCat }) {
                            FilterChip(
                                selected = layer.shapeType == shape,
                                onClick = { onUpdate(false) { it.copy(shapeType = shape) } },
                                label = { Text(shape.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_COLOR -> {
                if (layer.type == LayerType.SHAPE) {
                    val isTransparent = !layer.shapeGradientEnabled && (layer.shapeFillColor ushr 24) == 0L
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !layer.shapeGradientEnabled && !isTransparent,
                            onClick = {
                                onUpdate(false) {
                                    val fallback = if ((it.shapeFillColor ushr 24) == 0L) 0xFF00B0FF else it.shapeFillColor
                                    it.copy(shapeGradientEnabled = false, shapeFillColor = fallback)
                                }
                            },
                            label = { Text("Solid", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = layer.shapeGradientEnabled,
                            onClick = {
                                onUpdate(false) {
                                    val start = if ((it.shapeFillColor ushr 24) == 0L) 0xFF00B0FF else it.shapeFillColor
                                    it.copy(shapeGradientEnabled = true, shapeFillColor = start)
                                }
                            },
                            label = { Text("Gradient", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = isTransparent,
                            onClick = {
                                onUpdate(false) {
                                    it.copy(shapeGradientEnabled = false, shapeFillColor = 0x00000000L)
                                }
                            },
                            label = { Text("None (Hollow)", fontSize = 11.sp) }
                        )
                    }

                    if (!isTransparent) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (layer.shapeGradientEnabled) "Start Color" else "Fill Color",
                            color = Color(0xFFCCCCCC),
                            fontSize = 11.sp
                        )
                        ColorPaletteRow(
                            colors = ObjectPalette,
                            selectedColor = layer.shapeFillColor,
                            onSelectColor = { c -> onUpdate(false) { it.copy(shapeFillColor = c) } },
                            allowAlpha = true,
                            pickerTitle = if (layer.shapeGradientEnabled) "Gradient Start Color" else "Shape Fill Color"
                        )

                        if (layer.shapeGradientEnabled) {
                            Text("End Color", color = Color(0xFFCCCCCC), fontSize = 11.sp)
                            ColorPaletteRow(
                                colors = ObjectPalette,
                                selectedColor = layer.shapeGradientEndColor,
                                onSelectColor = { c -> onUpdate(false) { it.copy(shapeGradientEndColor = c) } },
                                allowAlpha = true,
                                pickerTitle = "Gradient End Color"
                            )
                            Text("Gradient Angle: ${layer.shapeGradientAngle.toInt()}°", color = Color.White, fontSize = 11.sp)
                            Slider(
                                value = layer.shapeGradientAngle.coerceIn(0f, 360f),
                                onValueChange = { ang -> onUpdate(true) { it.copy(shapeGradientAngle = ang) } },
                                onValueChangeFinished = onFinishContinuousEdit,
                                valueRange = 0f..360f
                            )
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_STROKE -> {
                if (layer.type == LayerType.SHAPE) {
                    IvyStrokeInspector(
                        strokeWidth = layer.shapeStrokeWidth,
                        maxStrokeWidth = 48f,
                        defaultEnableWidth = 5f,
                        strokeColor = layer.shapeStrokeColor,
                        strokePosition = layer.strokePosition,
                        strokePattern = layer.strokePattern,
                        dashLength = layer.strokeDashLength,
                        dashGap = layer.strokeDashGap,
                        dashPhase = layer.strokePhase,
                        strokeJoin = layer.strokeJoin,
                        miterLimit = layer.strokeMiterLimit,
                        strokeCap = layer.strokeCap,
                        palette = ObjectPalette,
                        onToggleEnable = { _, width ->
                            onUpdate(false) { it.copy(shapeStrokeWidth = width) }
                        },
                        onWidthChange = { w -> onUpdate(true) { it.copy(shapeStrokeWidth = w) } },
                        onColorChange = { c -> onUpdate(false) { it.copy(shapeStrokeColor = c) } },
                        onPositionChange = { pos -> onUpdate(false) { it.copy(strokePosition = pos) } },
                        onPatternChange = { pat -> onUpdate(false) { it.copy(strokePattern = pat) } },
                        onDashLengthChange = { d -> onUpdate(true) { it.copy(strokeDashLength = d) } },
                        onDashGapChange = { g -> onUpdate(true) { it.copy(strokeDashGap = g) } },
                        onDashPhaseChange = { p -> onUpdate(true) { it.copy(strokePhase = p) } },
                        onJoinChange = { j -> onUpdate(false) { it.copy(strokeJoin = j) } },
                        onMiterLimitChange = { m -> onUpdate(true) { it.copy(strokeMiterLimit = m) } },
                        onCapChange = { c -> onUpdate(false) { it.copy(strokeCap = c) } },
                        onFinishContinuousEdit = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.SHAPE_SHADOW -> {
                if (layer.type == LayerType.SHAPE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Shape Shadow", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = layer.shadowEnabled,
                            onCheckedChange = { en -> onUpdate(false) { it.copy(shadowEnabled = en) } }
                        )
                    }
                    if (layer.shadowEnabled) {
                        Text("Shadow Color", color = Color(0xFFCCCCCC), fontSize = 11.sp)
                        ColorPaletteRow(
                            colors = listOf(
                                0xAA000000, 0xFF000000, 0xCCFFFFFF, 0xFFFF007F,
                                0xFF00E5FF, 0xFFFFEB3B, 0xFF651FFF
                            ),
                            selectedColor = layer.shadowColor,
                            onSelectColor = { c -> onUpdate(false) { it.copy(shadowColor = c) } },
                            allowAlpha = true,
                            pickerTitle = "Shape Shadow Color"
                        )
                        Text("Blur Radius: ${layer.shadowRadius.toInt()} px", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = layer.shadowRadius.coerceIn(1f, 40f),
                            onValueChange = { r -> onUpdate(true) { it.copy(shadowRadius = r) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = 1f..40f
                        )
                        Text("Offset X: ${layer.shadowDx.toInt()} px", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = layer.shadowDx.coerceIn(-30f, 30f),
                            onValueChange = { dx -> onUpdate(true) { it.copy(shadowDx = dx) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = -30f..30f
                        )
                        Text("Offset Y: ${layer.shadowDy.toInt()} px", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = layer.shadowDy.coerceIn(-30f, 30f),
                            onValueChange = { dy -> onUpdate(true) { it.copy(shadowDy = dy) } },
                            onValueChangeFinished = onFinishContinuousEdit,
                            valueRange = -30f..30f
                        )
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
