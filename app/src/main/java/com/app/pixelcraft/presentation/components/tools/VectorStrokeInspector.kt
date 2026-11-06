package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.StrokeCapStyle
import com.app.pixelcraft.data.model.StrokeJoinStyle
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

/**
 * Vector path contour stroke inspector shared by both Text (`TEXT_STROKE`) and Shape (`SHAPE_STROKE`) tools.
 */
@Composable
fun VectorStrokeInspector(
    strokeWidth: Float,
    maxStrokeWidth: Float,
    defaultEnableWidth: Float = 5f,
    strokeColor: Long,
    strokePosition: StrokePosition,
    strokePattern: StrokePattern,
    dashLength: Float,
    dashGap: Float,
    dashPhase: Float,
    strokeJoin: StrokeJoinStyle,
    miterLimit: Float,
    strokeCap: StrokeCapStyle,
    palette: List<Long>,
    recentColors: List<Long> = emptyList(),
    onPickFromCanvas: (() -> Unit)? = null,
    onToggleEnable: (Boolean, Float) -> Unit,
    onWidthChange: (Float, Boolean) -> Unit,
    onColorChange: (Long) -> Unit,
    onPositionChange: (StrokePosition) -> Unit,
    onPatternChange: (StrokePattern) -> Unit,
    onDashLengthChange: (Float, Boolean) -> Unit,
    onDashGapChange: (Float, Boolean) -> Unit,
    onDashPhaseChange: (Float, Boolean) -> Unit,
    onJoinChange: (StrokeJoinStyle) -> Unit,
    onMiterLimitChange: (Float, Boolean) -> Unit,
    onCapChange: (StrokeCapStyle) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Vector Stroke", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = strokeWidth > 0f,
                onCheckedChange = { enable ->
                    onToggleEnable(enable, if (enable) defaultEnableWidth else 0f)
                }
            )
        }

        if (strokeWidth > 0f) {
            PrecisionSlider(
                label = "Stroke Width",
                value = strokeWidth,
                valueRange = 1f..maxStrokeWidth,
                defaultValue = defaultEnableWidth,
                fineStep = 1f,
                unitSuffix = "px",
                valueFormatter = { "${it.roundToInt()}" },
                onValueChange = onWidthChange,
                onValueChangeFinished = onFinishContinuousEdit
            )

            Text("Stroke Color", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            ColorPaletteRow(
                colors = palette,
                recentColors = recentColors,
                selectedColor = strokeColor,
                onSelectColor = onColorChange,
                onPickFromCanvas = onPickFromCanvas,
                allowAlpha = true,
                pickerTitle = "Stroke Color"
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Stroke Alignment", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (pos in StrokePosition.entries) {
                    FilterChip(
                        selected = strokePosition == pos,
                        onClick = { onPositionChange(pos) },
                        label = { Text(pos.label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Dash Pattern", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (pat in StrokePattern.entries) {
                    FilterChip(
                        selected = strokePattern == pat,
                        onClick = { onPatternChange(pat) },
                        label = { Text(pat.label, fontSize = 12.sp) }
                    )
                }
            }

            if (strokePattern != StrokePattern.SOLID) {
                if (strokePattern == StrokePattern.DASHED || strokePattern == StrokePattern.DASH_DOT) {
                    PrecisionSlider(
                        label = "Dash Length",
                        value = dashLength,
                        valueRange = 4f..60f,
                        defaultValue = 16f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = onDashLengthChange,
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
                PrecisionSlider(
                    label = "Dash Gap",
                    value = dashGap,
                    valueRange = 2f..50f,
                    defaultValue = 10f,
                    fineStep = 1f,
                    unitSuffix = "px",
                    onValueChange = onDashGapChange,
                    onValueChangeFinished = onFinishContinuousEdit
                )
                PrecisionSlider(
                    label = "Phase Offset",
                    value = dashPhase,
                    valueRange = 0f..60f,
                    defaultValue = 0f,
                    fineStep = 1f,
                    unitSuffix = "px",
                    onValueChange = onDashPhaseChange,
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Corner Join", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (join in StrokeJoinStyle.entries) {
                    FilterChip(
                        selected = strokeJoin == join,
                        onClick = { onJoinChange(join) },
                        label = { Text(join.label, fontSize = 12.sp) }
                    )
                }
            }
            if (strokeJoin == StrokeJoinStyle.MITER) {
                PrecisionSlider(
                    label = "Miter Limit",
                    value = miterLimit,
                    valueRange = 1f..16f,
                    defaultValue = 4f,
                    fineStep = 0.5f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChange = onMiterLimitChange,
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Line Cap", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (cap in StrokeCapStyle.entries) {
                    FilterChip(
                        selected = strokeCap == cap,
                        onClick = { onCapChange(cap) },
                        label = { Text(cap.label, fontSize = 12.sp) }
                    )
                }
            }
        }
    }
}
