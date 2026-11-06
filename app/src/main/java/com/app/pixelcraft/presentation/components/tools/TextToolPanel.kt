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
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.AlignmentAction

private val TextColorPalette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFF0055, 0xFFFF5722, 0xFFFFEB3B,
    0xFF00E676, 0xFF00E5FF, 0xFF2979FF, 0xFF651FFF, 0xFFFF80AB
)

@Composable
fun TextToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onOpenEditDialog: () -> Unit,
    onAddText: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedLayer == null || selectedLayer.type != LayerType.TEXT) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF232730)),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onAddText,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ New Text", color = Color.Black, fontWeight = FontWeight.Bold)
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
            SubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                onUpdate = onUpdateLayer,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onNudge = onNudge,
                onAlign = onAlign,
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
            ToolButton(
                icon = Icons.Default.Edit,
                label = "Edit",
                isActive = false,
                onClick = { onOpenEditDialog() }
            )
            ToolButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                isActive = false,
                onClick = { onDeleteLayer() }
            )
            ToolButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                isActive = false,
                onClick = { onDuplicateLayer() }
            )
            ToolButton(
                icon = Icons.Default.Layers,
                label = "To Front",
                isActive = false,
                onClick = { onBringToFront() }
            )
            ToolButton(
                icon = Icons.Default.Layers,
                label = "To Back",
                isActive = false,
                onClick = { onSendToBack() }
            )
            ToolButton(
                icon = Icons.Default.ArrowUpward,
                label = "Position",
                isActive = activeSubTool == ActiveSubTool.TEXT_POSITION,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_POSITION) }
            )
            ToolButton(
                icon = Icons.Default.AlignHorizontalCenter,
                label = "Relative Pos",
                isActive = activeSubTool == ActiveSubTool.TEXT_RELATIVE_POSITION,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_RELATIVE_POSITION) }
            )
            ToolButton(
                icon = Icons.Default.FormatSize,
                label = "Size",
                isActive = activeSubTool == ActiveSubTool.TEXT_SIZE,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_SIZE) }
            )
            ToolButton(
                icon = Icons.Default.ColorLens,
                label = "Color",
                isActive = activeSubTool == ActiveSubTool.TEXT_COLOR,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_COLOR) }
            )
            ToolButton(
                icon = Icons.Default.Opacity,
                label = "Opacity",
                isActive = activeSubTool == ActiveSubTool.TEXT_OPACITY,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_OPACITY) }
            )
            ToolButton(
                icon = Icons.Default.Rotate90DegreesCcw,
                label = "Rotate",
                isActive = activeSubTool == ActiveSubTool.TEXT_ROTATE,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_ROTATE) }
            )
            ToolButton(
                icon = Icons.Default.FormatBold,
                label = "Style",
                isActive = activeSubTool == ActiveSubTool.TEXT_STYLE,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_STYLE) }
            )
            ToolButton(
                icon = Icons.Default.FormatColorFill,
                label = "Background",
                isActive = activeSubTool == ActiveSubTool.TEXT_BACKGROUND,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_BACKGROUND) }
            )
            ToolButton(
                icon = Icons.Default.ColorLens,
                label = "Stroke",
                isActive = activeSubTool == ActiveSubTool.TEXT_STROKE,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_STROKE) }
            )
            ToolButton(
                icon = Icons.Default.Lightbulb,
                label = "Shadow",
                isActive = activeSubTool == ActiveSubTool.TEXT_SHADOW,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_SHADOW) }
            )
            ToolButton(
                icon = Icons.Default.ViewInAr,
                label = "3D Text",
                isActive = activeSubTool == ActiveSubTool.TEXT_3D,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_3D) }
            )
            ToolButton(
                icon = Icons.Default.SettingsBackupRestore,
                label = "Reflection",
                isActive = activeSubTool == ActiveSubTool.TEXT_REFLECTION,
                onClick = { onSubToolSelected(ActiveSubTool.TEXT_REFLECTION) }
            )
        }
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    val inactiveColor = Color.White

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
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
private fun SubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
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
                text = subTool.name.replace("TEXT_", "").replace("_", " "),
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
            ActiveSubTool.TEXT_POSITION -> {
                var stepPx by remember { mutableFloatStateOf(10f) }
                Text("Step: ${stepPx.toInt()} px", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = stepPx,
                    onValueChange = { stepPx = it },
                    valueRange = 1f..50f
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNudge(-stepPx, 0f) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Left", tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { onNudge(0f, -stepPx) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White)
                        }
                        IconButton(onClick = { onNudge(0f, stepPx) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { onNudge(stepPx, 0f) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Right", tint = Color.White)
                    }
                }
            }

            ActiveSubTool.TEXT_RELATIVE_POSITION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_LEFT) }) {
                        Icon(Icons.AutoMirrored.Filled.AlignHorizontalLeft, contentDescription = "Left", tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.CENTER_HORIZONTAL) }) {
                        Icon(Icons.Default.AlignHorizontalCenter, contentDescription = "Center H", tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_RIGHT) }) {
                        Icon(Icons.AutoMirrored.Filled.AlignHorizontalRight, contentDescription = "Right", tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_TOP) }) {
                        Icon(Icons.Default.AlignVerticalTop, contentDescription = "Top", tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.CENTER_VERTICAL) }) {
                        Icon(Icons.Default.AlignVerticalCenter, contentDescription = "Center V", tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_BOTTOM) }) {
                        Icon(Icons.Default.AlignVerticalBottom, contentDescription = "Bottom", tint = Color.White)
                    }
                }
            }

            ActiveSubTool.TEXT_SIZE -> {
                Text("Size: ${layer.fontSize.toInt()} sp", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = layer.fontSize.coerceIn(12f, 160f),
                    onValueChange = { newSize -> onUpdate(true) { it.copy(fontSize = newSize) } },
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 12f..160f
                )
            }

            ActiveSubTool.TEXT_COLOR -> {
                Text("Color Palette", color = Color.White, fontSize = 12.sp)
                ColorSwatchRow(
                    colors = TextColorPalette,
                    selectedColor = layer.textColor,
                    onSelect = { colorValue -> onUpdate(false) { it.copy(textColor = colorValue) } }
                )
            }

            ActiveSubTool.TEXT_OPACITY -> {
                Text("Opacity: ${(layer.opacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = layer.opacity.coerceIn(0f, 1f),
                    onValueChange = { newOpacity -> onUpdate(true) { it.copy(opacity = newOpacity) } },
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 0f..1f
                )
            }

            ActiveSubTool.TEXT_ROTATE -> {
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
                    onValueChange = { newRot -> onUpdate(true) { it.copy(rotation = newRot) } },
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = -180f..180f
                )
            }

            ActiveSubTool.TEXT_STYLE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = layer.isBold,
                        onClick = { onUpdate(false) { it.copy(isBold = !it.isBold) } },
                        label = { Text("Bold") }
                    )
                    FilterChip(
                        selected = layer.isItalic,
                        onClick = { onUpdate(false) { it.copy(isItalic = !it.isItalic) } },
                        label = { Text("Italic") }
                    )
                    FilterChip(
                        selected = layer.isUnderline,
                        onClick = { onUpdate(false) { it.copy(isUnderline = !it.isUnderline) } },
                        label = { Text("Underline") }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (align in TextAlignment.entries) {
                        FilterChip(
                            selected = layer.textAlign == align,
                            onClick = { onUpdate(false) { it.copy(textAlign = align) } },
                            label = { Text("Align ${align.name.lowercase().replaceFirstChar { it.uppercase() }}") }
                        )
                    }
                }
            }

            ActiveSubTool.TEXT_BACKGROUND -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Text Background", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.textBackgroundColor != null,
                        onCheckedChange = { enable ->
                            onUpdate(false) {
                                it.copy(textBackgroundColor = if (enable) 0xCC000000 else null)
                            }
                        }
                    )
                }
                if (layer.textBackgroundColor != null) {
                    val bgPalette = listOf(
                        0xCC000000, 0xCCFFFFFF, 0xCCFF0055, 0xCC00E5FF,
                        0xCC651FFF, 0xCCFFEB3B, 0xCC00C853
                    )
                    ColorSwatchRow(
                        colors = bgPalette,
                        selectedColor = layer.textBackgroundColor,
                        onSelect = { c -> onUpdate(false) { it.copy(textBackgroundColor = c) } }
                    )
                    Text("Padding: ${layer.textBackgroundPadding.toInt()} px", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.textBackgroundPadding.coerceIn(0f, 60f),
                        onValueChange = { p -> onUpdate(true) { it.copy(textBackgroundPadding = p) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..60f
                    )
                    Text("Radius: ${layer.textBackgroundRadius.toInt()} px", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.textBackgroundRadius.coerceIn(0f, 40f),
                        onValueChange = { r -> onUpdate(true) { it.copy(textBackgroundRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..40f
                    )
                }
            }

            ActiveSubTool.TEXT_STROKE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Stroke", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.strokeWidth > 0f,
                        onCheckedChange = { enable ->
                            onUpdate(false) { it.copy(strokeWidth = if (enable) 4f else 0f) }
                        }
                    )
                }
                if (layer.strokeWidth > 0f) {
                    ColorSwatchRow(
                        colors = TextColorPalette,
                        selectedColor = layer.strokeColor,
                        onSelect = { c -> onUpdate(false) { it.copy(strokeColor = c) } }
                    )
                    Text("Width: ${layer.strokeWidth.toInt()} px", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.strokeWidth.coerceIn(1f, 30f),
                        onValueChange = { w -> onUpdate(true) { it.copy(strokeWidth = w) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 1f..30f
                    )
                }
            }

            ActiveSubTool.TEXT_SHADOW -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Shadow", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.shadowEnabled,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(shadowEnabled = enable) } }
                    )
                }
                if (layer.shadowEnabled) {
                    Text("Blur Radius: ${layer.shadowRadius.toInt()}", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.shadowRadius.coerceIn(1f, 40f),
                        onValueChange = { r -> onUpdate(true) { it.copy(shadowRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 1f..40f
                    )
                    Text("Offset X: ${layer.shadowDx.toInt()}", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.shadowDx.coerceIn(-30f, 30f),
                        onValueChange = { dx -> onUpdate(true) { it.copy(shadowDx = dx) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = -30f..30f
                    )
                    Text("Offset Y: ${layer.shadowDy.toInt()}", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.shadowDy.coerceIn(-30f, 30f),
                        onValueChange = { dy -> onUpdate(true) { it.copy(shadowDy = dy) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = -30f..30f
                    )
                }
            }

            ActiveSubTool.TEXT_3D -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable 3D Text", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.is3D,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(is3D = enable) } }
                    )
                }
                if (layer.is3D) {
                    Text("Depth: ${layer.depth3D.toInt()}", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.depth3D.coerceIn(1f, 35f),
                        onValueChange = { d -> onUpdate(true) { it.copy(depth3D = d) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 1f..35f
                    )
                    Text("Darken: ${(layer.darken3D * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.darken3D.coerceIn(0f, 0.8f),
                        onValueChange = { dk -> onUpdate(true) { it.copy(darken3D = dk) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..0.8f
                    )
                    Text("Angle: ${layer.angle3D.toInt()}°", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.angle3D.coerceIn(0f, 360f),
                        onValueChange = { a -> onUpdate(true) { it.copy(angle3D = a) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..360f
                    )
                }
            }

            ActiveSubTool.TEXT_REFLECTION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Reflection", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.reflectionEnabled,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(reflectionEnabled = enable) } }
                    )
                }
                if (layer.reflectionEnabled) {
                    Text("Distance: ${layer.reflectionDistance.toInt()} px", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.reflectionDistance.coerceIn(0f, 40f),
                        onValueChange = { d -> onUpdate(true) { it.copy(reflectionDistance = d) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0f..40f
                    )
                    Text("Opacity: ${(layer.reflectionOpacity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = layer.reflectionOpacity.coerceIn(0.05f, 0.8f),
                        onValueChange = { op -> onUpdate(true) { it.copy(reflectionOpacity = op) } },
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 0.05f..0.8f
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun ColorSwatchRow(
    colors: List<Long>,
    selectedColor: Long,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (colorValue in colors) {
            val isSelected = selectedColor == colorValue
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(colorValue))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF555555),
                        shape = CircleShape
                    )
                    .clickable { onSelect(colorValue) }
            )
        }
    }
}
