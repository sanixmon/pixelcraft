package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
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
internal fun ColumnScope.ObjectSizeInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    var lockAspect by remember { mutableStateOf(layer.type == LayerType.IMAGE) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.ui_lock_aspect_ratio), color = Color(0xFFD5DAE5), fontSize = 12.sp)
        Switch(checked = lockAspect, onCheckedChange = { lockAspect = it })
    }

    PrecisionSlider(
        label = stringResource(R.string.ui_width),
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
            label = stringResource(R.string.ui_height),
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
            label = stringResource(R.string.ui_corner_radius),
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

@Composable
internal fun ColumnScope.ObjectRotateInspector(
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
        onValueChange = { r, cont -> onUpdate(cont) { it.copy(rotation = r) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

@Composable
internal fun ColumnScope.ObjectOpacityInspector(
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
        onValueChange = { op, cont ->
            onUpdate(cont) { it.copy(opacity = (op / 100f).coerceIn(0f, 1f)) }
        },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

@Composable
internal fun ColumnScope.ObjectBlendInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit
) {
    Text(stringResource(R.string.ui_composite_blend_mode), color = Color(0xFFCCCCCC), fontSize = 12.sp)
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
