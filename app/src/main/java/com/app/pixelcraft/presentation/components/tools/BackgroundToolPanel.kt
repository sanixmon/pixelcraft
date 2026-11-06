package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.CanvasConfig

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
        0xFF2A2E39, 0xFF121212, 0xFF000000, 0xFFFFFFFF,
        0xFF1E3A8A, 0xFF065F46, 0xFF7C2D12, 0xFF831843, 0xFF4C1D95
    )

    val gradients = listOf(
        listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
        listOf(0xFFFF416C, 0xFFFF4B2B),
        listOf(0xFF141E30, 0xFF243B55),
        listOf(0xFF8A2387, 0xFFE94057, 0xFFF27121),
        listOf(0xFF11998E, 0xFF38EF7D)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF232730))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Canvas Background", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPickImage,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Photo", fontSize = 11.sp, color = Color.Black)
                }

                Button(
                    onClick = { onOpenDialog(ActiveDialog.IMAGE_SIZE) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Image Size", fontSize = 11.sp)
                }

                Button(
                    onClick = onSetTransparent,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Texture, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Transparent", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("Solid Color", color = Color(0xFFCCCCCC), fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (c in solidColors) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(c))
                        .clickable { onSetColor(c) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text("Gradients", color = Color(0xFFCCCCCC), fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (grad in gradients) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                grad.map { Color(it) }
                            )
                        )
                        .clickable { onSetGradient(grad, 45f) }
                )
            }
        }
    }
}
