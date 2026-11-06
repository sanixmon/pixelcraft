package com.app.pixelcraft.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.ImageExporter

@Composable
fun PixelCraftExportDialog(
    canvasConfig: CanvasConfig,
    initialOptions: ExportOptions,
    isExporting: Boolean,
    onExport: (ExportOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var format by remember(initialOptions.format) { mutableStateOf(initialOptions.format) }
    var quality by remember(initialOptions.qualityPreset) { mutableStateOf(initialOptions.qualityPreset) }

    val (targetWidth, targetHeight) = ImageExporter.computeTargetDimensions(
        canvasConfig.width,
        canvasConfig.height,
        quality.multiplier
    )

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = "Export Image",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Image Format:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = format == ExportFormat.PNG,
                        onClick = { format = ExportFormat.PNG },
                        label = { Text("PNG (Transparent)") }
                    )
                    FilterChip(
                        selected = format == ExportFormat.JPEG,
                        onClick = { format = ExportFormat.JPEG },
                        label = { Text("JPEG") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Dimensions Quality:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (q in ExportQuality.entries) {
                        val isSelected = quality == q
                        val (w, h) = ImageExporter.computeTargetDimensions(
                            canvasConfig.width,
                            canvasConfig.height,
                            q.multiplier
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF2C3240) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00E5FF) else Color(0xFF333333),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { quality = q }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q.label,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${w}x${h}",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Resolution: $targetWidth x $targetHeight px",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExport(
                        initialOptions.copy(
                            format = format,
                            qualityPreset = quality
                        )
                    )
                },
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Save to Gallery", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isExporting) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
