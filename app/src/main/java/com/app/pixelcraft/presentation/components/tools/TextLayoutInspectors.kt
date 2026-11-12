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
internal fun ColumnScope.TextPositionInspector(
    onNudge: (Float, Float) -> Unit
) {
    var stepPx by remember { mutableFloatStateOf(10f) }
    PrecisionSlider(
        label = stringResource(R.string.ui_nudge_step),
        value = stepPx,
        valueRange = 1f..50f,
        defaultValue = 10f,
        fineStep = 1f,
        unitSuffix = "px",
        onValueChange = { next, _ -> stepPx = next },
        onValueChangeFinished = {}
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onNudge(-stepPx, 0f) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_nudge_left), tint = PixelCraftColors.TextPrimary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onNudge(0f, -stepPx) }) {
                Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.ui_nudge_up), tint = PixelCraftColors.TextPrimary)
            }
            IconButton(onClick = { onNudge(0f, stepPx) }) {
                Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.ui_nudge_down), tint = PixelCraftColors.TextPrimary)
            }
        }
        IconButton(onClick = { onNudge(stepPx, 0f) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.ui_nudge_right), tint = PixelCraftColors.TextPrimary)
        }
    }
}

@Composable
internal fun ColumnScope.TextAlignInspector(
    onAlign: (AlignmentAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        IconButton(onClick = { onAlign(AlignmentAction.ALIGN_LEFT) }) {
            Icon(Icons.AutoMirrored.Filled.AlignHorizontalLeft, contentDescription = stringResource(R.string.ui_align_left), tint = PixelCraftColors.TextPrimary)
        }
        IconButton(onClick = { onAlign(AlignmentAction.CENTER_HORIZONTAL) }) {
            Icon(Icons.Default.AlignHorizontalCenter, contentDescription = stringResource(R.string.ui_center_horizontally), tint = PixelCraftColors.Accent)
        }
        IconButton(onClick = { onAlign(AlignmentAction.ALIGN_RIGHT) }) {
            Icon(Icons.AutoMirrored.Filled.AlignHorizontalRight, contentDescription = stringResource(R.string.ui_align_right), tint = PixelCraftColors.TextPrimary)
        }
        IconButton(onClick = { onAlign(AlignmentAction.ALIGN_TOP) }) {
            Icon(Icons.Default.AlignVerticalTop, contentDescription = stringResource(R.string.ui_align_top), tint = PixelCraftColors.TextPrimary)
        }
        IconButton(onClick = { onAlign(AlignmentAction.CENTER_VERTICAL) }) {
            Icon(Icons.Default.AlignVerticalCenter, contentDescription = stringResource(R.string.ui_center_vertically), tint = PixelCraftColors.Accent)
        }
        IconButton(onClick = { onAlign(AlignmentAction.ALIGN_BOTTOM) }) {
            Icon(Icons.Default.AlignVerticalBottom, contentDescription = stringResource(R.string.ui_align_bottom), tint = PixelCraftColors.TextPrimary)
        }
    }
}

@Composable
internal fun ColumnScope.TextOpacityInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    PrecisionSlider(
        label = stringResource(R.string.ui_opacity),
        value = layer.opacity * 100f,
        valueRange = 0f..100f,
        defaultValue = 100f,
        fineStep = 2f,
        unitSuffix = "%",
        onValueChange = { pct, cont ->
            onUpdate(cont) { it.copy(opacity = (pct / 100f).coerceIn(0f, 1f)) }
        },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

@Composable
internal fun ColumnScope.TextRotateInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
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
        label = stringResource(R.string.ui_rotation_angle),
        value = layer.rotation,
        valueRange = -180f..180f,
        defaultValue = 0f,
        fineStep = 1f,
        unitSuffix = "°",
        onValueChange = { newRot, cont -> onUpdate(cont) { it.copy(rotation = newRot) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

@Composable
internal fun ColumnScope.TextBlendInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit
) {
    Text(stringResource(R.string.ui_composite_blend_mode), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
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
                label = { Text(mode.localizedLabel(), fontSize = 12.sp) }
            )
        }
    }
}
