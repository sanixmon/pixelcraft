package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import com.app.pixelcraft.presentation.components.dialogs.PixelCraftColorPickerDialog

@Composable
fun BackgroundToolPanel(
    canvasConfig: CanvasConfig,
    onSetColor: (Long) -> Unit,
    onSetGradient: (List<Long>, Float) -> Unit,
    onSetTransparent: () -> Unit,
    onPickImage: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    val solidColors = listOf(
        0xFF2A2E39L, 0xFF121212L, 0xFF000000L, 0xFFFFFFFFL,
        0xFF1E3A8AL, 0xFF065F46L, 0xFF7C2D12L, 0xFF831843L, 0xFF4C1D95L
    )

    val gradients = listOf(
        listOf(0xFF0F2027L, 0xFF203A43L, 0xFF2C5364L),
        listOf(0xFFFF416CL, 0xFFFF4B2BL),
        listOf(0xFF141E30L, 0xFF243B55L),
        listOf(0xFF8A2387L, 0xFFE94057L, 0xFFF27121L),
        listOf(0xFF11998EL, 0xFF38EF7DL)
    )

    var editingGradientStopIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 230.dp)
            .background(Color(0xFF232730))
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = "Canvas Background",
            color = Color(0xFF00E5FF),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPickImage,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Photo", fontSize = 11.sp, color = Color.Black)
            }

            Button(
                onClick = { onOpenDialog(ActiveDialog.IMAGE_SIZE) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Image Size", fontSize = 11.sp)
            }

            Button(
                onClick = onSetTransparent,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Texture, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Transparent", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Solid Color (Palette / Wheel / Hex)", color = Color(0xFFCCCCCC), fontSize = 11.sp)
        ColorPaletteRow(
            colors = solidColors,
            selectedColor = if (canvasConfig.backgroundType == BackgroundType.SOLID_COLOR) {
                canvasConfig.backgroundColor
            } else {
                0x00000000L
            },
            onSelectColor = { onSetColor(it) },
            allowAlpha = false,
            pickerTitle = "Canvas Background Color"
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text("Gradients (Presets & Custom 2-Stop)", color = Color(0xFFCCCCCC), fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Custom 2-stop gradient stop pickers
            val activeStops = if (canvasConfig.gradientColors.size >= 2) {
                canvasConfig.gradientColors
            } else {
                listOf(0xFF0F2027L, 0xFF2C5364L)
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(activeStops.first() and 0xFFFFFFFFL))
                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                    .clickable { editingGradientStopIndex = 0 },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Gradient Start", tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(activeStops.last() and 0xFFFFFFFFL))
                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                    .clickable { editingGradientStopIndex = 1 },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Gradient End", tint = Color.White, modifier = Modifier.size(14.dp))
            }

            for (grad in gradients) {
                val isSelected = canvasConfig.backgroundType == BackgroundType.GRADIENT &&
                    canvasConfig.gradientColors == grad
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(grad.map { Color(it and 0xFFFFFFFFL) }))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF555555),
                            shape = CircleShape
                        )
                        .clickable { onSetGradient(grad, canvasConfig.gradientAngle) }
                )
            }
        }

        if (canvasConfig.backgroundType == BackgroundType.GRADIENT) {
            Text(
                text = "Gradient Angle: ${canvasConfig.gradientAngle.toInt()}°",
                color = Color(0xFFCCCCCC),
                fontSize = 11.sp
            )
            Slider(
                value = canvasConfig.gradientAngle.coerceIn(0f, 360f),
                onValueChange = { ang -> onSetGradient(canvasConfig.gradientColors, ang) },
                valueRange = 0f..360f
            )
        }
    }

    val stopIdx = editingGradientStopIndex
    if (stopIdx != null) {
        val currentStops = if (canvasConfig.gradientColors.size >= 2) {
            canvasConfig.gradientColors
        } else {
            listOf(0xFF0F2027L, 0xFF2C5364L)
        }
        val seed = if (stopIdx == 0) currentStops.first() else currentStops.last()
        PixelCraftColorPickerDialog(
            title = if (stopIdx == 0) "Gradient Start Color" else "Gradient End Color",
            initialColor = seed,
            allowAlpha = false,
            onConfirm = { picked ->
                val newStops = if (stopIdx == 0) {
                    listOf(picked, currentStops.last())
                } else {
                    listOf(currentStops.first(), picked)
                }
                editingGradientStopIndex = null
                onSetGradient(newStops, canvasConfig.gradientAngle)
            },
            onDismiss = { editingGradientStopIndex = null }
        )
    }
}
