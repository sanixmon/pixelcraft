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
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.app.pixelcraft.data.model.ImageAdjustments
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PhotoFilterPreset
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

private val ObjectPalette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFF00B0FF, 0xFFFF0055,
    0xFFFFEB3B, 0xFF00E676, 0xFF651FFF, 0xFFFF9100
)

@Composable
fun ObjectToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    recentColors: List<Long> = emptyList(),
    hasCopiedStyle: Boolean = false,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit = {},
    onCopyStyle: () -> Unit = {},
    onPasteStyle: () -> Unit = {},
    onOpenSavedStyles: () -> Unit = {},
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
                .background(Color(0xFF1E222D))
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
                Text("Photo Layer", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E222D))
    ) {
        if (activeSubTool != ActiveSubTool.NONE) {
            ObjectSubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                recentColors = recentColors,
                onUpdate = onUpdateLayer,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onStartEyedropper = onStartEyedropper,
                onClose = { onSubToolSelected(ActiveSubTool.NONE) }
            )
        }

        // Quick Add + Style Clipboard bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B24))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniActionPill(
                icon = Icons.Default.FormatShapes,
                label = "+ Shape",
                onClick = { onOpenDialog(ActiveDialog.ADD_SHAPE) }
            )
            MiniActionPill(
                icon = Icons.Default.AddPhotoAlternate,
                label = "+ Photo",
                onClick = onAddImage
            )
            MiniActionPill(
                icon = Icons.Default.ContentCopy,
                label = "Copy Style",
                onClick = onCopyStyle
            )
            if (hasCopiedStyle) {
                MiniActionPill(
                    icon = Icons.Default.ContentPaste,
                    label = "Paste Style",
                    accent = true,
                    onClick = onPasteStyle
                )
            }
            MiniActionPill(
                icon = Icons.Default.Bookmarks,
                label = "Styles",
                onClick = onOpenSavedStyles
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
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
            }

            if (selectedLayer.type == LayerType.IMAGE) {
                ObjectToolItem(
                    icon = Icons.Default.Crop,
                    label = "Crop",
                    isActive = activeSubTool == ActiveSubTool.IMAGE_CROP,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_CROP) }
                )
                ObjectToolItem(
                    icon = Icons.Default.Tune,
                    label = "Adjust",
                    isActive = activeSubTool == ActiveSubTool.IMAGE_ADJUST,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_ADJUST) }
                )
                ObjectToolItem(
                    icon = Icons.Default.FilterVintage,
                    label = "Mask",
                    isActive = activeSubTool == ActiveSubTool.IMAGE_MASK,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_MASK) }
                )
            }

            // Shared across BOTH Shape AND Image layers (Fixes Bug 2.17!)
            ObjectToolItem(
                icon = Icons.Default.Lightbulb,
                label = "Shadow",
                isActive = activeSubTool == ActiveSubTool.SHAPE_SHADOW,
                onClick = { onSubToolSelected(ActiveSubTool.SHAPE_SHADOW) }
            )
            ObjectToolItem(
                icon = Icons.Default.BlurOn,
                label = "Blend",
                isActive = activeSubTool == ActiveSubTool.LAYER_BLEND,
                onClick = { onSubToolSelected(ActiveSubTool.LAYER_BLEND) }
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
                label = "Duplicate",
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
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else Color.Transparent)
            .padding(horizontal = 9.dp, vertical = 6.dp),
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
            fontSize = 11.sp,
            color = if (isActive) activeColor else Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun ObjectSubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(Color(0xFF161922))
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
                    .removePrefix("IMAGE_")
                    .removePrefix("LAYER_")
                    .replace("_", " "),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 13.sp
            )
            IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (subTool) {
            ActiveSubTool.OBJECT_SIZE -> {
                var lockAspect by remember { mutableStateOf(layer.type == LayerType.IMAGE) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lock Aspect Ratio", color = Color(0xFFD5DAE5), fontSize = 12.sp)
                    Switch(checked = lockAspect, onCheckedChange = { lockAspect = it })
                }

                PrecisionSlider(
                    label = "Width",
                    value = layer.width,
                    valueRange = 40f..2400f,
                    defaultValue = 320f,
                    fineStep = 4f,
                    unitSuffix = "px",
                    onValueChange = { newW, cont ->
                        onUpdate(cont) { current ->
                            if (lockAspect) {
                                val ratio = current.height.coerceAtLeast(1f) / current.width.coerceAtLeast(1f)
                                current.copy(width = newW, height = (newW * ratio).coerceIn(40f, 2400f))
                            } else {
                                current.copy(width = newW)
                            }
                        }
                    },
                    onValueChangeFinished = onFinishContinuousEdit
                )

                if (!lockAspect) {
                    PrecisionSlider(
                        label = "Height",
                        value = layer.height,
                        valueRange = 40f..2400f,
                        defaultValue = 320f,
                        fineStep = 4f,
                        unitSuffix = "px",
                        onValueChange = { newH, cont -> onUpdate(cont) { it.copy(height = newH) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }

                if (
                    layer.type == LayerType.SHAPE &&
                    (layer.shapeType == ShapeType.ROUNDED_RECTANGLE || layer.shapeType == ShapeType.SPEECH_BUBBLE)
                ) {
                    PrecisionSlider(
                        label = "Corner Radius",
                        value = layer.shapeCornerRadius,
                        valueRange = 0f..180f,
                        defaultValue = 24f,
                        fineStep = 2f,
                        unitSuffix = "px",
                        onValueChange = { r, cont -> onUpdate(cont) { it.copy(shapeCornerRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.OBJECT_ROTATE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-90f, -45f, 0f, 45f, 90f).forEach { deg ->
                        FilterChip(
                            selected = layer.rotation.roundToInt() == deg.roundToInt(),
                            onClick = { onUpdate(false) { it.copy(rotation = deg) } },
                            label = { Text("${deg.toInt()}°", fontSize = 11.sp) }
                        )
                    }
                }
                PrecisionSlider(
                    label = "Rotation Angle",
                    value = layer.rotation,
                    valueRange = -180f..180f,
                    defaultValue = 0f,
                    fineStep = 1f,
                    unitSuffix = "°",
                    onValueChange = { r, cont -> onUpdate(cont) { it.copy(rotation = r) } },
                    onValueChangeFinished = onFinishContinuousEdit
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
                                label = { Text(cat.label, fontSize = 12.sp) }
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
                                label = { Text(shape.label, fontSize = 12.sp) }
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
                            label = { Text("Solid", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = layer.shapeGradientEnabled,
                            onClick = {
                                onUpdate(false) {
                                    val start = if ((it.shapeFillColor ushr 24) == 0L) 0xFF00B0FF else it.shapeFillColor
                                    it.copy(shapeGradientEnabled = true, shapeFillColor = start)
                                }
                            },
                            label = { Text("Gradient", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = isTransparent,
                            onClick = {
                                onUpdate(false) {
                                    it.copy(shapeGradientEnabled = false, shapeFillColor = 0x00000000L)
                                }
                            },
                            label = { Text("None (Hollow)", fontSize = 12.sp) }
                        )
                    }

                    if (!isTransparent) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (layer.shapeGradientEnabled) "Start Color" else "Fill Color",
                            color = Color(0xFFCCCCCC),
                            fontSize = 12.sp
                        )
                        ColorPaletteRow(
                            colors = ObjectPalette,
                            recentColors = recentColors,
                            selectedColor = layer.shapeFillColor,
                            onSelectColor = { c -> onUpdate(false) { it.copy(shapeFillColor = c) } },
                            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHAPE_FILL) },
                            allowAlpha = true,
                            pickerTitle = if (layer.shapeGradientEnabled) "Gradient Start Color" else "Shape Fill Color"
                        )

                        if (layer.shapeGradientEnabled) {
                            Text("End Color", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                            ColorPaletteRow(
                                colors = ObjectPalette,
                                recentColors = recentColors,
                                selectedColor = layer.shapeGradientEndColor,
                                onSelectColor = { c -> onUpdate(false) { it.copy(shapeGradientEndColor = c) } },
                                onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHAPE_GRADIENT_END) },
                                allowAlpha = true,
                                pickerTitle = "Gradient End Color"
                            )
                            PrecisionSlider(
                                label = "Gradient Angle",
                                value = layer.shapeGradientAngle,
                                valueRange = 0f..360f,
                                defaultValue = 90f,
                                fineStep = 5f,
                                unitSuffix = "°",
                                onValueChange = { ang, cont -> onUpdate(cont) { it.copy(shapeGradientAngle = ang) } },
                                onValueChangeFinished = onFinishContinuousEdit
                            )
                        }
                    }
                }
            }

            ActiveSubTool.SHAPE_STROKE -> {
                if (layer.type == LayerType.SHAPE) {
                    VectorStrokeInspector(
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
                        recentColors = recentColors,
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHAPE_STROKE) },
                        onToggleEnable = { _, width ->
                            onUpdate(false) { it.copy(shapeStrokeWidth = width) }
                        },
                        onWidthChange = { w, cont -> onUpdate(cont) { it.copy(shapeStrokeWidth = w) } },
                        onColorChange = { c -> onUpdate(false) { it.copy(shapeStrokeColor = c) } },
                        onPositionChange = { pos -> onUpdate(false) { it.copy(strokePosition = pos) } },
                        onPatternChange = { pat -> onUpdate(false) { it.copy(strokePattern = pat) } },
                        onDashLengthChange = { d, cont -> onUpdate(cont) { it.copy(strokeDashLength = d) } },
                        onDashGapChange = { g, cont -> onUpdate(cont) { it.copy(strokeDashGap = g) } },
                        onDashPhaseChange = { p, cont -> onUpdate(cont) { it.copy(strokePhase = p) } },
                        onJoinChange = { j -> onUpdate(false) { it.copy(strokeJoin = j) } },
                        onMiterLimitChange = { m, cont -> onUpdate(cont) { it.copy(strokeMiterLimit = m) } },
                        onCapChange = { c -> onUpdate(false) { it.copy(strokeCap = c) } },
                        onFinishContinuousEdit = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.SHAPE_SHADOW -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Drop Shadow", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.shadowEnabled,
                        onCheckedChange = { en -> onUpdate(false) { it.copy(shadowEnabled = en) } }
                    )
                }
                if (layer.shadowEnabled) {
                    ColorPaletteRow(
                        colors = listOf(
                            0xAA000000L, 0xFF000000L, 0xCCFFFFFFL, 0xFFFF007FL,
                            0xFF00E5FFL, 0xFFFFEB3BL, 0xFF651FFFL
                        ),
                        recentColors = recentColors,
                        selectedColor = layer.shadowColor,
                        onSelectColor = { c -> onUpdate(false) { it.copy(shadowColor = c) } },
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHADOW_COLOR) },
                        allowAlpha = true,
                        pickerTitle = "Drop Shadow Color"
                    )
                    PrecisionSlider(
                        label = "Blur Radius",
                        value = layer.shadowRadius,
                        valueRange = 0f..48f,
                        defaultValue = 10f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { r, cont -> onUpdate(cont) { it.copy(shadowRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Offset X",
                        value = layer.shadowDx,
                        valueRange = -40f..40f,
                        defaultValue = 4f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { dx, cont -> onUpdate(cont) { it.copy(shadowDx = dx) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Offset Y",
                        value = layer.shadowDy,
                        valueRange = -40f..40f,
                        defaultValue = 6f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { dy, cont -> onUpdate(cont) { it.copy(shadowDy = dy) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.SHAPE_OPACITY -> {
                PrecisionSlider(
                    label = "Opacity",
                    value = layer.opacity * 100f,
                    valueRange = 0f..100f,
                    defaultValue = 100f,
                    fineStep = 2f,
                    unitSuffix = "%",
                    onValueChange = { op, cont ->
                        onUpdate(cont) { it.copy(opacity = (op / 100f).coerceIn(0f, 1f)) }
                    },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.LAYER_BLEND -> {
                Text("Composite Blend Mode", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (mode in LayerBlendMode.entries) {
                        FilterChip(
                            selected = layer.blendMode == mode,
                            onClick = { onUpdate(false) { it.copy(blendMode = mode) } },
                            label = { Text(mode.label, fontSize = 12.sp) }
                        )
                    }
                }
            }

            ActiveSubTool.IMAGE_CROP -> {
                if (layer.type == LayerType.IMAGE) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = {
                            onUpdate(false) {
                                it.copy(cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f)
                            }
                        }) {
                            Text("Full Reset", color = Color(0xFF00E5FF), fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {
                            onUpdate(false) {
                                val minSide = minOf(it.width, it.height)
                                it.copy(
                                    cropLeft = 0.08f,
                                    cropTop = 0.08f,
                                    cropRight = 0.92f,
                                    cropBottom = 0.92f,
                                    width = minSide,
                                    height = minSide
                                )
                            }
                        }) {
                            Text("1:1 Square", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {
                            onUpdate(false) {
                                val base = minOf(it.width, it.height)
                                it.copy(width = base, height = base * 1.25f)
                            }
                        }) {
                            Text("4:5 Portrait", color = Color.White, fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {
                            onUpdate(false) {
                                val base = maxOf(it.width, it.height)
                                it.copy(width = base, height = base * (9f / 16f))
                            }
                        }) {
                            Text("16:9 Wide", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    PrecisionSlider(
                        label = "Crop Left",
                        value = layer.cropLeft * 100f,
                        valueRange = 0f..80f,
                        defaultValue = 0f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { pct, cont ->
                            onUpdate(cont) {
                                val cl = (pct / 100f).coerceAtMost(it.cropRight - 0.08f).coerceAtLeast(0f)
                                it.copy(cropLeft = cl)
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Crop Right",
                        value = layer.cropRight * 100f,
                        valueRange = 20f..100f,
                        defaultValue = 100f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { pct, cont ->
                            onUpdate(cont) {
                                val cr = (pct / 100f).coerceAtLeast(it.cropLeft + 0.08f).coerceAtMost(1f)
                                it.copy(cropRight = cr)
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Crop Top",
                        value = layer.cropTop * 100f,
                        valueRange = 0f..80f,
                        defaultValue = 0f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { pct, cont ->
                            onUpdate(cont) {
                                val ct = (pct / 100f).coerceAtMost(it.cropBottom - 0.08f).coerceAtLeast(0f)
                                it.copy(cropTop = ct)
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Crop Bottom",
                        value = layer.cropBottom * 100f,
                        valueRange = 20f..100f,
                        defaultValue = 100f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { pct, cont ->
                            onUpdate(cont) {
                                val cb = (pct / 100f).coerceAtLeast(it.cropTop + 0.08f).coerceAtMost(1f)
                                it.copy(cropBottom = cb)
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.IMAGE_ADJUST -> {
                if (layer.type == LayerType.IMAGE) {
                    val adj = layer.imageAdjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Presets", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                        if (!adj.isDefault) {
                            OutlinedButton(onClick = {
                                onUpdate(false) { it.copy(imageAdjustments = ImageAdjustments()) }
                            }) {
                                Text("Reset All", color = Color(0xFF00E5FF), fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (preset in PhotoFilterPreset.entries) {
                            FilterChip(
                                selected = adj.filterPreset == preset,
                                onClick = {
                                    onUpdate(false) {
                                        it.copy(imageAdjustments = it.imageAdjustments.copy(filterPreset = preset))
                                    }
                                },
                                label = { Text(preset.label, fontSize = 12.sp) }
                            )
                        }
                    }

                    PrecisionSlider(
                        label = "Brightness",
                        value = adj.brightness * 100f,
                        valueRange = -50f..50f,
                        defaultValue = 0f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { b, cont ->
                            onUpdate(cont) {
                                it.copy(imageAdjustments = it.imageAdjustments.copy(brightness = b / 100f))
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Contrast",
                        value = adj.contrast * 100f,
                        valueRange = 50f..180f,
                        defaultValue = 100f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { c, cont ->
                            onUpdate(cont) {
                                it.copy(imageAdjustments = it.imageAdjustments.copy(contrast = c / 100f))
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Saturation",
                        value = adj.saturation * 100f,
                        valueRange = 0f..200f,
                        defaultValue = 100f,
                        fineStep = 4f,
                        unitSuffix = "%",
                        onValueChange = { s, cont ->
                            onUpdate(cont) {
                                it.copy(imageAdjustments = it.imageAdjustments.copy(saturation = s / 100f))
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = "Warmth",
                        value = adj.warmth * 100f,
                        valueRange = -40f..40f,
                        defaultValue = 0f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { w, cont ->
                            onUpdate(cont) {
                                it.copy(imageAdjustments = it.imageAdjustments.copy(warmth = w / 100f))
                            }
                        },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.IMAGE_MASK -> {
                if (layer.type == LayerType.IMAGE) {
                    Text("Clip Photo into Shape", color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = layer.imageMaskShape == null,
                            onClick = { onUpdate(false) { it.copy(imageMaskShape = null) } },
                            label = { Text("None", fontSize = 12.sp) }
                        )
                        val featuredMasks = listOf(
                            ShapeType.CIRCLE,
                            ShapeType.ROUNDED_RECTANGLE,
                            ShapeType.HEART,
                            ShapeType.STAR,
                            ShapeType.HEXAGON,
                            ShapeType.DIAMOND,
                            ShapeType.OCTAGON,
                            ShapeType.SHIELD,
                            ShapeType.STARBURST
                        )
                        for (mask in featuredMasks) {
                            FilterChip(
                                selected = layer.imageMaskShape == mask,
                                onClick = { onUpdate(false) { it.copy(imageMaskShape = mask) } },
                                label = { Text(mask.label, fontSize = 12.sp) }
                            )
                        }
                    }

                    if (layer.imageMaskShape == null) {
                        PrecisionSlider(
                            label = "Photo Corner Radius",
                            value = layer.imageCornerRadius,
                            valueRange = 0f..220f,
                            defaultValue = 0f,
                            fineStep = 4f,
                            unitSuffix = "px",
                            onValueChange = { r, cont -> onUpdate(cont) { it.copy(imageCornerRadius = r) } },
                            onValueChangeFinished = onFinishContinuousEdit
                        )
                    }

                    PrecisionSlider(
                        label = "Photo Border Width",
                        value = layer.imageBorderWidth,
                        valueRange = 0f..40f,
                        defaultValue = 0f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { bw, cont -> onUpdate(cont) { it.copy(imageBorderWidth = bw) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    if (layer.imageBorderWidth > 0f) {
                        ColorPaletteRow(
                            colors = ObjectPalette,
                            recentColors = recentColors,
                            selectedColor = layer.imageBorderColor,
                            onSelectColor = { c -> onUpdate(false) { it.copy(imageBorderColor = c) } },
                            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.IMAGE_BORDER) },
                            allowAlpha = true,
                            pickerTitle = "Photo Border Color"
                        )
                    }
                }
            }

            else -> {}
        }
    }
}
