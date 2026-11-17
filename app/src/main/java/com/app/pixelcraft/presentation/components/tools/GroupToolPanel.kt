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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.GroupWork
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
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.EditorColors

private enum class GroupSubTool { NONE, SIZE, ROTATE, OPACITY, BLEND, ALIGN }

/**
 * Tool row for a selected group: the same object manipulations (size, rotate, opacity,
 * blend, flip, align, order, duplicate, delete) applied to the whole group as one object.
 */
@Composable
fun GroupToolPanel(
    groupId: String,
    memberCount: Int,
    primaryLayer: Layer,
    onScale: (percent: Float, continuous: Boolean) -> Unit,
    onRotate: (degrees: Float, continuous: Boolean) -> Unit,
    onUpdateMembers: (continuous: Boolean, transform: (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onEndRelativeSession: () -> Unit,
    onFlip: (horizontal: Boolean) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onUngroup: () -> Unit,
    onEditItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subTool by remember(groupId) { mutableStateOf(GroupSubTool.NONE) }
    val toolRowScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorColors.Surface)
    ) {
        if (subTool != GroupSubTool.NONE) {
            GroupSubToolInspector(
                subTool = subTool,
                primaryLayer = primaryLayer,
                onScale = onScale,
                onRotate = onRotate,
                onUpdateMembers = onUpdateMembers,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onAlign = onAlign,
                onClose = {
                    onEndRelativeSession()
                    subTool = GroupSubTool.NONE
                }
            )
            return@Column
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .horizontalScroll(toolRowScroll)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ObjectToolItem(Icons.Default.GroupWork, stringResource(R.string.ui_ungroup), false, onClick = onUngroup)
            ObjectToolItem(Icons.Default.Edit, stringResource(R.string.ui_edit_item), false, onClick = onEditItem)
            ObjectToolItem(Icons.Default.AspectRatio, stringResource(R.string.ui_size), false) { subTool = GroupSubTool.SIZE }
            ObjectToolItem(Icons.Default.Rotate90DegreesCcw, stringResource(R.string.ui_rotate), false) { subTool = GroupSubTool.ROTATE }
            ObjectToolItem(Icons.Default.Opacity, stringResource(R.string.ui_opacity), false) { subTool = GroupSubTool.OPACITY }
            ObjectToolItem(Icons.Default.BlurOn, stringResource(R.string.ui_blend), false) { subTool = GroupSubTool.BLEND }
            ObjectToolItem(Icons.Default.AlignHorizontalCenter, stringResource(R.string.ui_align), false) { subTool = GroupSubTool.ALIGN }
            ObjectToolItem(Icons.Default.Flip, stringResource(R.string.ui_flip_h), false) { onFlip(true) }
            ObjectToolItem(Icons.Default.Flip, stringResource(R.string.ui_flip_v), false, iconRotation = 90f) { onFlip(false) }
            ObjectToolItem(Icons.Default.Layers, stringResource(R.string.ui_to_front), false, onClick = onBringToFront)
            ObjectToolItem(Icons.Default.Layers, stringResource(R.string.ui_to_back), false, onClick = onSendToBack)
            ObjectToolItem(Icons.Default.ContentCopy, stringResource(R.string.action_duplicate), false, onClick = onDuplicate)
            ObjectToolItem(Icons.Default.Delete, stringResource(R.string.action_delete), false, onClick = onDelete)
        }
        Text(
            text = stringResource(R.string.ui_group_count, memberCount),
            color = EditorColors.TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
        )
    }
}

@Composable
private fun GroupSubToolInspector(
    subTool: GroupSubTool,
    primaryLayer: Layer,
    onScale: (Float, Boolean) -> Unit,
    onRotate: (Float, Boolean) -> Unit,
    onUpdateMembers: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onClose: () -> Unit
) {
    // Size and rotation are relative to the group as it was when this inspector opened.
    var scalePercent by remember(subTool) { mutableFloatStateOf(100f) }
    var rotationDeg by remember(subTool) { mutableFloatStateOf(0f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(EditorColors.PanelElevated),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(start = 6.dp)
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(EditorColors.Surface)
        ) {
            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ui_done), tint = EditorColors.TextPrimary)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 8.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
        ) {
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
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(EditorColors.SurfaceElevated)
                                    .clickable {
                                        rotationDeg += step
                                        onRotate(rotationDeg, false)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

                GroupSubTool.ALIGN -> TextAlignInspector(onAlign = onAlign)

                GroupSubTool.NONE -> Unit
            }
        }
    }
}
