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
import androidx.compose.material3.Slider
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
 * Ivy Draw-inspired vector path stroke inspector shared by both Text (`TEXT_STROKE`)
 * and Shape (`SHAPE_STROKE`) tools.
 */
@Composable
fun IvyStrokeInspector(
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
    onToggleEnable: (Boolean, Float) -> Unit,
    onWidthChange: (Float) -> Unit,
    onColorChange: (Long) -> Unit,
    onPositionChange: (StrokePosition) -> Unit,
    onPatternChange: (StrokePattern) -> Unit,
    onDashLengthChange: (Float) -> Unit,
    onDashGapChange: (Float) -> Unit,
    onDashPhaseChange: (Float) -> Unit,
    onJoinChange: (StrokeJoinStyle) -> Unit,
    onMiterLimitChange: (Float) -> Unit,
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
            Text("Width: ${strokeWidth.roundToInt()} px", color = Color.White, fontSize = 12.sp)
            Slider(
                value = strokeWidth.coerceIn(1f, maxStrokeWidth),
                onValueChange = onWidthChange,
                onValueChangeFinished = onFinishContinuousEdit,
                valueRange = 1f..maxStrokeWidth
            )

            Text("Stroke Color", color = Color(0xFFCCCCCC), fontSize = 11.sp)
            ColorPaletteRow(
                colors = palette,
                selectedColor = strokeColor,
                onSelectColor = onColorChange,
                allowAlpha = true,
                pickerTitle = "Stroke Color"
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Position (Alignment)", color = Color(0xFFCCCCCC), fontSize = 11.sp)
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
                        label = { Text(pos.label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Dash Pattern", color = Color(0xFFCCCCCC), fontSize = 11.sp)
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
                        label = { Text(pat.label, fontSize = 11.sp) }
                    )
                }
            }

            if (strokePattern != StrokePattern.SOLID) {
                if (strokePattern == StrokePattern.DASHED || strokePattern == StrokePattern.DASH_DOT) {
                    Text("Dash Length: ${dashLength.roundToInt()} px", color = Color.White, fontSize = 11.sp)
                    Slider(
                        value = dashLength.coerceIn(4f, 60f),
                        onValueChange = onDashLengthChange,
                        onValueChangeFinished = onFinishContinuousEdit,
                        valueRange = 4f..60f
                    )
                }
                Text("Gap: ${dashGap.roundToInt()} px", color = Color.White, fontSize = 11.sp)
                Slider(
                    value = dashGap.coerceIn(2f, 50f),
                    onValueChange = onDashGapChange,
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 2f..50f
                )
                Text("Phase Offset: ${dashPhase.roundToInt()} px", color = Color.White, fontSize = 11.sp)
                Slider(
                    value = dashPhase.coerceIn(0f, 60f),
                    onValueChange = onDashPhaseChange,
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 0f..60f
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Corner Join", color = Color(0xFFCCCCCC), fontSize = 11.sp)
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
                        label = { Text(join.label, fontSize = 11.sp) }
                    )
                }
            }
            if (strokeJoin == StrokeJoinStyle.MITER) {
                Text("Miter Limit: ${"%.1f".format(miterLimit)}", color = Color.White, fontSize = 11.sp)
                Slider(
                    value = miterLimit.coerceIn(1f, 16f),
                    onValueChange = onMiterLimitChange,
                    onValueChangeFinished = onFinishContinuousEdit,
                    valueRange = 1f..16f
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("Line Cap", color = Color(0xFFCCCCCC), fontSize = 11.sp)
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
                        label = { Text(cap.label, fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}
