package com.app.pixelcraft.presentation.components.dialogs

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.presentation.PixelCraftViewModel

@Composable
fun PixelCraftImageSizeDialog(
    currentPreset: AspectRatioPreset,
    currentWidth: Int,
    currentHeight: Int,
    onApply: (AspectRatioPreset, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember(currentPreset) { mutableStateOf(currentPreset) }
    var widthText by remember(currentWidth) { mutableStateOf(currentWidth.toString()) }
    var heightText by remember(currentHeight) { mutableStateOf(currentHeight.toString()) }

    val minDim = PixelCraftViewModel.MIN_CANVAS_DIMENSION
    val maxDim = PixelCraftViewModel.MAX_CANVAS_DIMENSION
    val parsedW = widthText.trim().toIntOrNull()
    val parsedH = heightText.trim().toIntOrNull()
    val isValid = parsedW != null && parsedH != null &&
        parsedW in minDim..maxDim && parsedH in minDim..maxDim

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelCraftColors.DialogSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.ui_image_size),
                fontWeight = FontWeight.Bold,
                color = PixelCraftColors.TextPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.ui_preset_aspect_ratio), color = PixelCraftColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                for (preset in AspectRatioPreset.entries) {
                    val isSelected = selectedPreset == preset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) PixelCraftColors.Accent.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable {
                                selectedPreset = preset
                                if (preset != AspectRatioPreset.CUSTOM) {
                                    widthText = preset.defaultWidth.toString()
                                    heightText = preset.defaultHeight.toString()
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = preset.localizedLabel(),
                            color = if (isSelected) PixelCraftColors.Accent else PixelCraftColors.TextPrimary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${preset.defaultWidth}x${preset.defaultHeight}",
                            color = PixelCraftColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.ui_dimensions_range, minDim, maxDim),
                    color = PixelCraftColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = widthText,
                        onValueChange = { next ->
                            widthText = next.filter { it.isDigit() }.take(4)
                            if (selectedPreset != AspectRatioPreset.CUSTOM &&
                                widthText.toIntOrNull() != selectedPreset.defaultWidth
                            ) {
                                selectedPreset = AspectRatioPreset.CUSTOM
                            }
                        },
                        label = { Text(stringResource(R.string.ui_width)) },
                        isError = parsedW == null || parsedW !in minDim..maxDim,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { next ->
                            heightText = next.filter { it.isDigit() }.take(4)
                            if (selectedPreset != AspectRatioPreset.CUSTOM &&
                                heightText.toIntOrNull() != selectedPreset.defaultHeight
                            ) {
                                selectedPreset = AspectRatioPreset.CUSTOM
                            }
                        },
                        label = { Text(stringResource(R.string.ui_height)) },
                        isError = parsedH == null || parsedH !in minDim..maxDim,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.ui_dimensions_hint, minDim, maxDim),
                        color = PixelCraftColors.Danger,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = (parsedW ?: currentWidth).coerceIn(minDim, maxDim)
                    val h = (parsedH ?: currentHeight).coerceIn(minDim, maxDim)
                    onApply(selectedPreset, w, h)
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.ui_ok), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = PixelCraftColors.TextSecondary)
            }
        }
    )
}
