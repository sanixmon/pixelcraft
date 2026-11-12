package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.presentation.i18n.labelRes
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Looks
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

@Composable
internal fun ColumnScope.TextColorInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = !layer.textGradientEnabled,
            onClick = { onUpdate(false) { it.copy(textGradientEnabled = false) } },
            label = { Text(stringResource(R.string.ui_solid_fill), fontSize = 12.sp) }
        )
        FilterChip(
            selected = layer.textGradientEnabled && !layer.textGradientRadial,
            onClick = {
                onUpdate(false) { it.copy(textGradientEnabled = true, textGradientRadial = false) }
            },
            label = { Text(stringResource(R.string.ui_linear_gradient), fontSize = 12.sp) }
        )
        FilterChip(
            selected = layer.textGradientEnabled && layer.textGradientRadial,
            onClick = {
                onUpdate(false) { it.copy(textGradientEnabled = true, textGradientRadial = true) }
            },
            label = { Text(stringResource(R.string.ui_radial_gradient), fontSize = 12.sp) }
        )
    }

    Text(
        text = if (layer.textGradientEnabled) stringResource(R.string.ui_start_color) else stringResource(R.string.ui_text_color),
        color = PixelCraftColors.TextSecondary,
        fontSize = 12.sp
    )
    ColorPaletteRow(
        colors = TextColorPalette,
        recentColors = recentColors,
        selectedColor = layer.textColor,
        onSelectColor = { colorValue -> onUpdate(false) { it.copy(textColor = colorValue) } },
        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_COLOR) },
        allowAlpha = true,
        pickerTitle = stringResource(R.string.ui_text_color)
    )

    if (layer.textGradientEnabled) {
        Text(stringResource(R.string.ui_end_color), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
        ColorPaletteRow(
            colors = TextColorPalette,
            recentColors = recentColors,
            selectedColor = layer.textGradientEndColor,
            onSelectColor = { endCol -> onUpdate(false) { it.copy(textGradientEndColor = endCol) } },
            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_GRADIENT_END) },
            allowAlpha = true,
            pickerTitle = stringResource(R.string.ui_gradient_end_color)
        )
        if (!layer.textGradientRadial) {
            PrecisionSlider(
                label = stringResource(R.string.ui_gradient_angle),
                value = layer.textGradientAngle,
                valueRange = 0f..360f,
                defaultValue = 0f,
                fineStep = 5f,
                unitSuffix = "°",
                onValueChange = { ang, cont -> onUpdate(cont) { it.copy(textGradientAngle = ang) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }
    }
}

@Composable
internal fun ColumnScope.TextBackgroundInspector(
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
        Text(stringResource(R.string.ui_enable_text_background_pill), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
        Switch(
            checked = layer.textBackgroundColor != null,
            onCheckedChange = { enable ->
                onUpdate(false) {
                    it.copy(textBackgroundColor = if (enable) 0xCC000000L else null)
                }
            }
        )
    }
    if (layer.textBackgroundColor != null) {
        val bgPalette = listOf(
            0xCC000000L, 0xCCFFFFFFL, 0xCCFF0055L, 0xCC00E5FFL,
            0xCC651FFFL, 0xCCFFEB3BL, 0xCC00C853L
        )
        ColorPaletteRow(
            colors = bgPalette,
            recentColors = recentColors,
            selectedColor = layer.textBackgroundColor,
            onSelectColor = { c -> onUpdate(false) { it.copy(textBackgroundColor = c) } },
            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_BACKGROUND) },
            allowAlpha = true,
            pickerTitle = stringResource(R.string.ui_text_background_color)
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_pill_padding),
            value = layer.textBackgroundPadding,
            valueRange = 0f..60f,
            defaultValue = 16f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { p, cont -> onUpdate(cont) { it.copy(textBackgroundPadding = p) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_corner_radius),
            value = layer.textBackgroundRadius,
            valueRange = 0f..48f,
            defaultValue = 8f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { r, cont -> onUpdate(cont) { it.copy(textBackgroundRadius = r) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
}

@Composable
internal fun ColumnScope.TextStrokeInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
    VectorStrokeInspector(
        strokeWidth = layer.strokeWidth,
        maxStrokeWidth = 36f,
        defaultEnableWidth = 4f,
        strokeColor = layer.strokeColor,
        strokePosition = layer.strokePosition,
        strokePattern = layer.strokePattern,
        dashLength = layer.strokeDashLength,
        dashGap = layer.strokeDashGap,
        dashPhase = layer.strokePhase,
        strokeJoin = layer.strokeJoin,
        miterLimit = layer.strokeMiterLimit,
        strokeCap = layer.strokeCap,
        palette = TextColorPalette,
        recentColors = recentColors,
        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.STROKE_COLOR) },
        onToggleEnable = { _, width ->
            onUpdate(false) { it.copy(strokeWidth = width) }
        },
        onWidthChange = { w, cont -> onUpdate(cont) { it.copy(strokeWidth = w) } },
        onColorChange = { c -> onUpdate(false) { it.copy(strokeColor = c) } },
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

@Composable
internal fun ColumnScope.TextGlowInspector(
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
        Text(stringResource(R.string.ui_enable_outer_neon_glow), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
        Switch(
            checked = layer.glowEnabled,
            onCheckedChange = { enable -> onUpdate(false) { it.copy(glowEnabled = enable) } }
        )
    }
    if (layer.glowEnabled) {
        ColorPaletteRow(
            colors = listOf(
                0xFF00E5FFL, 0xFFFF007FL, 0xFF76FF03L, 0xFFFFEA00L,
                0xFFD500F9L, 0xFFFF6D00L, 0xFFFFFFFFL
            ),
            recentColors = recentColors,
            selectedColor = layer.glowColor,
            onSelectColor = { c -> onUpdate(false) { it.copy(glowColor = c) } },
            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.GLOW_COLOR) },
            allowAlpha = true,
            pickerTitle = stringResource(R.string.ui_outer_glow_color)
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_glow_blur_radius),
            value = layer.glowRadius,
            valueRange = 2f..60f,
            defaultValue = 18f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { rad, cont -> onUpdate(cont) { it.copy(glowRadius = rad) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
}

@Composable
internal fun ColumnScope.TextShadowInspector(
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
            onCheckedChange = { enable -> onUpdate(false) { it.copy(shadowEnabled = enable) } }
        )
    }
    if (layer.shadowEnabled) {
        ColorPaletteRow(
            colors = listOf(
                0xAA000000L, 0xFF000000L, 0xCCFFFFFFL, 0xFFFF0055L,
                0xFF00E5FFL, 0xFFFFEB3BL, 0xFF651FFFL
            ),
            recentColors = recentColors,
            selectedColor = layer.shadowColor,
            onSelectColor = { c -> onUpdate(false) { it.copy(shadowColor = c) } },
            onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHADOW_COLOR) },
            allowAlpha = true,
            pickerTitle = stringResource(R.string.ui_text_shadow_color)
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

@Composable
internal fun ColumnScope.Text3dInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.ui_enable_3d_extrusion), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
        Switch(
            checked = layer.is3D,
            onCheckedChange = { enable -> onUpdate(false) { it.copy(is3D = enable) } }
        )
    }
    if (layer.is3D) {
        PrecisionSlider(
            label = "3D Depth",
            value = layer.depth3D,
            valueRange = 1f..36f,
            defaultValue = 16f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { d, cont -> onUpdate(cont) { it.copy(depth3D = d) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_side_darken),
            value = layer.darken3D * 100f,
            valueRange = 0f..85f,
            defaultValue = 40f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { dk, cont -> onUpdate(cont) { it.copy(darken3D = dk / 100f) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_extrusion_angle),
            value = layer.angle3D,
            valueRange = 0f..360f,
            defaultValue = 30f,
            fineStep = 5f,
            unitSuffix = "°",
            onValueChange = { a, cont -> onUpdate(cont) { it.copy(angle3D = a) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.ui_floor_shadow), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
        Switch(
            checked = layer.shadow3DEnabled,
            onCheckedChange = { en -> onUpdate(false) { it.copy(shadow3DEnabled = en) } }
        )
    }
    if (layer.shadow3DEnabled) {
        ColorPaletteRow(
            colors = listOf(0x66000000L, 0xAA000000L, 0x6600E5FFL, 0x66FF007FL),
            recentColors = recentColors,
            selectedColor = layer.shadow3DColor,
            onSelectColor = { c -> onUpdate(false) { it.copy(shadow3DColor = c) } },
            allowAlpha = true,
            pickerTitle = "3D Floor Shadow Color"
        )
    }
}

@Composable
internal fun ColumnScope.TextReflectionInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.ui_enable_mirror_reflection), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
        Switch(
            checked = layer.reflectionEnabled,
            onCheckedChange = { enable -> onUpdate(false) { it.copy(reflectionEnabled = enable) } }
        )
    }
    if (layer.reflectionEnabled) {
        PrecisionSlider(
            label = stringResource(R.string.ui_gap_distance),
            value = layer.reflectionDistance,
            valueRange = 0f..48f,
            defaultValue = 4f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { d, cont -> onUpdate(cont) { it.copy(reflectionDistance = d) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_reflection_opacity),
            value = layer.reflectionOpacity * 100f,
            valueRange = 5f..85f,
            defaultValue = 35f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { op, cont -> onUpdate(cont) { it.copy(reflectionOpacity = op / 100f) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
}
