package com.app.pixelcraft.presentation.components.tools

import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.EditorColors

internal enum class GroupSubTool { NONE, SIZE, ROTATE, OPACITY, BLEND, ALIGN, STROKE }

@Composable
internal fun ColumnScope.GroupSubToolContent(
    subTool: GroupSubTool,
    primaryLayer: Layer,
    onScale: (Float, Boolean) -> Unit,
    onRotate: (Float, Boolean) -> Unit,
    onUpdateMembers: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    recentColors: List<Long>,
    memberCount: Int,
    onDistribute: (Boolean) -> Unit
) {
    // Size and rotation are relative to the group as it was when this inspector opened.
    var scalePercent by remember(subTool) { mutableFloatStateOf(100f) }
    var rotationDeg by remember(subTool) { mutableFloatStateOf(0f) }

    when (subTool) {
        GroupSubTool.SIZE -> PrecisionSlider(
            label = stringResource(R.string.ui_group_size),
            value = scalePercent,
            valueRange = 10f..400f,
            defaultValue = 100f,
            fineStep = 5f,
            unitSuffix = "%",
            onValueChange = { v, cont ->
                scalePercent = v
                onScale(v, cont)
            },
            onValueChangeFinished = onFinishContinuousEdit
        )

        GroupSubTool.ROTATE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(-90f, -45f, 45f, 90f).forEach { step ->
                    Text(
                        text = (if (step > 0) "+" else "") + "${step.toInt()}°",
                        color = EditorColors.TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(EditorColors.SurfaceElevated)
                            .clickable {
                                rotationDeg += step
                                onRotate(rotationDeg, false)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            PrecisionSlider(
                label = stringResource(R.string.ui_rotation_angle),
                value = rotationDeg,
                valueRange = -180f..180f,
                defaultValue = 0f,
                fineStep = 1f,
                unitSuffix = "°",
                onValueChange = { v, cont ->
                    rotationDeg = v
                    onRotate(v, cont)
                },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }

        GroupSubTool.OPACITY -> PrecisionSlider(
            label = stringResource(R.string.ui_opacity),
            value = primaryLayer.opacity * 100f,
            valueRange = 0f..100f,
            defaultValue = 100f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { op, cont ->
                onUpdateMembers(cont) { it.copy(opacity = (op / 100f).coerceIn(0f, 1f)) }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )

        GroupSubTool.BLEND -> ToolDropdown(
            label = stringResource(R.string.ui_composite_blend_mode),
            options = LayerBlendMode.entries,
            selected = primaryLayer.blendMode,
            optionLabel = { it.localizedLabel() },
            onSelect = { mode -> onUpdateMembers(false) { it.copy(blendMode = mode) } }
        )

        GroupSubTool.ALIGN -> {
            TextAlignInspector(onAlign = onAlign)
            if (memberCount >= 3) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    IconButton(onClick = { onDistribute(true) }) {
                        Icon(Icons.Default.HorizontalDistribute, contentDescription = stringResource(R.string.ui_distribute_h), tint = EditorColors.TextPrimary)
                    }
                    IconButton(onClick = { onDistribute(false) }) {
                        Icon(Icons.Default.VerticalDistribute, contentDescription = stringResource(R.string.ui_distribute_v), tint = EditorColors.TextPrimary)
                    }
                }
            }
        }

        GroupSubTool.STROKE -> {
            // One stroke for every member: text outline, shape stroke, photo border.
            val width = when (primaryLayer.type) {
                LayerType.TEXT -> primaryLayer.strokeWidth
                LayerType.SHAPE -> primaryLayer.shapeStrokeWidth
                LayerType.IMAGE -> primaryLayer.imageBorderWidth
            }
            val color = when (primaryLayer.type) {
                LayerType.TEXT -> primaryLayer.strokeColor
                LayerType.SHAPE -> primaryLayer.shapeStrokeColor
                LayerType.IMAGE -> primaryLayer.imageBorderColor
            }
            PrecisionSlider(
                label = stringResource(R.string.ui_stroke_width),
                value = width,
                valueRange = 0f..40f,
                defaultValue = 0f,
                fineStep = 1f,
                unitSuffix = "px",
                onValueChange = { w, cont -> onUpdateMembers(cont) { it.withGroupStroke(width = w) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
            ColorPaletteRow(
                colors = ObjectPalette,
                recentColors = recentColors,
                selectedColor = color,
                onSelectColor = { c ->
                    onUpdateMembers(false) { it.withGroupStroke(color = c, width = if (width < 1f) 6f else null) }
                },
                pickerTitle = stringResource(R.string.ui_stroke_color)
            )
        }

        GroupSubTool.NONE -> Unit
    }
}


/** Applies a stroke width and/or colour using whichever stroke this layer type has. */
private fun Layer.withGroupStroke(width: Float? = null, color: Long? = null): Layer = when (type) {
    LayerType.TEXT -> copy(strokeWidth = width ?: strokeWidth, strokeColor = color ?: strokeColor)
    LayerType.SHAPE -> copy(shapeStrokeWidth = width ?: shapeStrokeWidth, shapeStrokeColor = color ?: shapeStrokeColor)
    LayerType.IMAGE -> copy(imageBorderWidth = width ?: imageBorderWidth, imageBorderColor = color ?: imageBorderColor)
}
