package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
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

@Composable
internal fun ColumnScope.ShapeTypeInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit
) {
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
                    label = { Text(cat.localizedLabel(), fontSize = 12.sp) }
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
                    label = { Text(shape.localizedLabel(), fontSize = 12.sp) }
                )
            }
        }
    }
}

@Composable
internal fun ColumnScope.ShapeColorInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
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
                label = { Text(stringResource(R.string.ui_solid), fontSize = 12.sp) }
            )
            FilterChip(
                selected = layer.shapeGradientEnabled,
                onClick = {
                    onUpdate(false) {
                        val start = if ((it.shapeFillColor ushr 24) == 0L) 0xFF00B0FF else it.shapeFillColor
                        it.copy(shapeGradientEnabled = true, shapeFillColor = start)
                    }
                },
                label = { Text(stringResource(R.string.ui_gradient), fontSize = 12.sp) }
            )
            FilterChip(
                selected = isTransparent,
                onClick = {
                    onUpdate(false) {
                        it.copy(shapeGradientEnabled = false, shapeFillColor = 0x00000000L)
                    }
                },
                label = { Text(stringResource(R.string.ui_none_hollow), fontSize = 12.sp) }
            )
        }

        if (!isTransparent) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (layer.shapeGradientEnabled) stringResource(R.string.ui_start_color) else stringResource(R.string.ui_fill_color),
                color = PixelCraftColors.TextSecondary,
                fontSize = 12.sp
            )
            ColorPaletteRow(
                colors = ObjectPalette,
                recentColors = recentColors,
                selectedColor = layer.shapeFillColor,
                onSelectColor = { c -> onUpdate(false) { it.copy(shapeFillColor = c) } },
                onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHAPE_FILL) },
                allowAlpha = true,
                pickerTitle = if (layer.shapeGradientEnabled) stringResource(R.string.ui_gradient_start_color) else stringResource(R.string.ui_shape_fill_color)
            )

            if (layer.shapeGradientEnabled) {
                Text(stringResource(R.string.ui_end_color), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
                ColorPaletteRow(
                    colors = ObjectPalette,
                    recentColors = recentColors,
                    selectedColor = layer.shapeGradientEndColor,
                    onSelectColor = { c -> onUpdate(false) { it.copy(shapeGradientEndColor = c) } },
                    onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHAPE_GRADIENT_END) },
                    allowAlpha = true,
                    pickerTitle = stringResource(R.string.ui_gradient_end_color)
                )
                PrecisionSlider(
                    label = stringResource(R.string.ui_gradient_angle),
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

@Composable
internal fun ColumnScope.ShapeStrokeInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
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

@Composable
internal fun ColumnScope.ShapeShadowInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.ui_enable_drop_shadow), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
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
            pickerTitle = stringResource(R.string.ui_drop_shadow_color)
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_blur_radius),
            value = layer.shadowRadius,
            valueRange = 0f..48f,
            defaultValue = 10f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { r, cont -> onUpdate(cont) { it.copy(shadowRadius = r) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_offset_x),
            value = layer.shadowDx,
            valueRange = -40f..40f,
            defaultValue = 4f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { dx, cont -> onUpdate(cont) { it.copy(shadowDx = dx) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_offset_y),
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
