package com.app.pixelcraft.presentation.components.dialogs

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
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = "Image Size",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Preset Aspect Ratio:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                for (preset in AspectRatioPreset.entries) {
                    val isSelected = selectedPreset == preset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF2C3240) else Color.Transparent)
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
                            text = preset.title,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${preset.defaultWidth}x${preset.defaultHeight}",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Dimensions ($minDim–$maxDim px):",
                    color = Color.White,
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
                        label = { Text("Width") },
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
                        label = { Text("Height") },
                        isError = parsedH == null || parsedH !in minDim..maxDim,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter width and height between $minDim and $maxDim px.",
                        color = Color(0xFFFF5252),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
