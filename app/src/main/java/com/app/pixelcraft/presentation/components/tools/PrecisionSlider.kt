package com.app.pixelcraft.presentation.components.tools

import com.app.pixelcraft.R
import com.app.pixelcraft.ui.theme.PixelCraftColors
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Accessible precision slider (§ 5.2f & § 5.5) with:
 * - Tappable numeric value pill that opens an exact number input dialog
 * - One-tap Reset button when value differs from [defaultValue]
 * - Fine `-` and `+` step buttons
 * - TalkBack semantics description
 */
@Composable
fun PrecisionSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float, Boolean) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    defaultValue: Float? = null,
    fineStep: Float = ((valueRange.endInclusive - valueRange.start) / 50f).coerceAtLeast(0.5f),
    valueFormatter: (Float) -> String = { "${it.toInt()}" },
    unitSuffix: String = ""
) {
    var showNumberDialog by remember { mutableStateOf(false) }
    val clamped = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val displayValue = valueFormatter(clamped)
    val showReset = defaultValue != null && abs(clamped - defaultValue) > 0.01f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = PixelCraftColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showReset && defaultValue != null) {
                    IconButton(
                        onClick = { onValueChange(defaultValue, false) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.ui_reset_named, label),
                            tint = PixelCraftColors.Accent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PixelCraftColors.SurfaceElevated)
                        .border(1.dp, PixelCraftColors.Border, RoundedCornerShape(6.dp))
                        .clickable { showNumberDialog = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (unitSuffix.isNotEmpty()) "$displayValue $unitSuffix" else displayValue,
                        color = PixelCraftColors.Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val next = (clamped - fineStep).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(next, false)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.ui_decrease_named, label),
                    tint = PixelCraftColors.TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Slider(
                value = clamped,
                onValueChange = { onValueChange(it, true) },
                onValueChangeFinished = onValueChangeFinished,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = PixelCraftColors.Accent,
                    activeTrackColor = PixelCraftColors.Accent,
                    inactiveTrackColor = PixelCraftColors.Border
                ),
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "$label: $displayValue $unitSuffix"
                    }
            )

            IconButton(
                onClick = {
                    val next = (clamped + fineStep).coerceIn(valueRange.start, valueRange.endInclusive)
                    onValueChange(next, false)
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.ui_increase_named, label),
                    tint = PixelCraftColors.TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showNumberDialog) {
        var textInput by remember(clamped) {
            mutableStateOf(
                if (fineStep < 1f) "%.2f".format(clamped) else clamped.toInt().toString()
            )
        }
        AlertDialog(
            onDismissRequest = { showNumberDialog = false },
            containerColor = PixelCraftColors.DialogSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = label,
                    color = PixelCraftColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.ui_range, valueFormatter(valueRange.start), valueFormatter(valueRange.endInclusive), unitSuffix),
                        color = PixelCraftColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = textInput.trim().replace(',', '.').toFloatOrNull()
                        if (parsed != null) {
                            onValueChange(parsed.coerceIn(valueRange.start, valueRange.endInclusive), false)
                        }
                        showNumberDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelCraftColors.Accent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.ui_apply), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNumberDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = PixelCraftColors.TextSecondary)
                }
            }
        )
    }
}
