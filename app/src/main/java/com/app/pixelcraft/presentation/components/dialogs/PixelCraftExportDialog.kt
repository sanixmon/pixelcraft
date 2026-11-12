package com.app.pixelcraft.presentation.components.dialogs

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.ImageExporter
import kotlin.math.roundToInt

@Composable
fun PixelCraftExportDialog(
    canvasConfig: CanvasConfig,
    initialOptions: ExportOptions,
    hasSelectedLayer: Boolean,
    isExporting: Boolean,
    onExport: (ExportOptions) -> Unit,
    onShareDirectly: (ExportOptions) -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit
) {
    var format by remember(initialOptions.format) { mutableStateOf(initialOptions.format) }
    var qualityPreset by remember(initialOptions.qualityPreset) { mutableStateOf(initialOptions.qualityPreset) }
    var compressionQuality by remember(initialOptions.jpegQuality) {
        mutableFloatStateOf(initialOptions.jpegQuality.toFloat())
    }
    var selectedLayerOnly by remember(initialOptions.selectedLayerOnly) {
        mutableStateOf(initialOptions.selectedLayerOnly && hasSelectedLayer)
    }
    var trimTransparent by remember(initialOptions.trimTransparent) {
        mutableStateOf(initialOptions.trimTransparent)
    }

    val activeEstimate = remember(canvasConfig.width, canvasConfig.height, qualityPreset) {
        ImageExporter.estimateDimensions(canvasConfig.width, canvasConfig.height, qualityPreset)
    }

    fun currentOptions(): ExportOptions = ExportOptions(
        format = format,
        qualityPreset = qualityPreset,
        jpegQuality = compressionQuality.roundToInt().coerceIn(10, 100),
        selectedLayerOnly = selectedLayerOnly && hasSelectedLayer,
        trimTransparent = trimTransparent && format.supportsTransparency
    )

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        containerColor = PixelCraftColors.DialogSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = stringResource(R.string.ui_export_share_image),
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
                Text(stringResource(R.string.ui_format), color = PixelCraftColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (fmt in ExportFormat.entries) {
                        FilterChip(
                            selected = format == fmt,
                            onClick = { format = fmt },
                            label = { Text(fmt.label, fontSize = 12.sp) }
                        )
                    }
                }

                if (format.usesQualitySlider) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ui_compression_quality), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
                        Text("${compressionQuality.roundToInt()}%", color = PixelCraftColors.Accent, fontSize = 12.sp)
                    }
                    Slider(
                        value = compressionQuality,
                        onValueChange = { compressionQuality = it },
                        valueRange = 20f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = PixelCraftColors.Accent,
                            activeTrackColor = PixelCraftColors.Accent,
                            inactiveTrackColor = PixelCraftColors.Border
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.ui_resolution_scale), color = PixelCraftColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (q in ExportQuality.entries) {
                        val isSelected = qualityPreset == q
                        val est = ImageExporter.estimateDimensions(canvasConfig.width, canvasConfig.height, q)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PixelCraftColors.Accent.copy(alpha = 0.12f) else PixelCraftColors.Background)
                                .border(
                                    1.dp,
                                    if (isSelected) PixelCraftColors.Accent else PixelCraftColors.Border,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { qualityPreset = q }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = q.localizedLabel(),
                                    color = if (isSelected) PixelCraftColors.Accent else PixelCraftColors.TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (est.cappedByMemory) {
                                    Text(
                                        text = stringResource(R.string.ui_optimized_for_device_ram),
                                        color = PixelCraftColors.Warning,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "${est.safeWidth}×${est.safeHeight} (~${"%.1f".format(est.estimatedBitmapMb)} MB)",
                                color = PixelCraftColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (format.supportsTransparency) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (hasSelectedLayer) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedLayerOnly = !selectedLayerOnly },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedLayerOnly,
                                onCheckedChange = { selectedLayerOnly = it }
                            )
                            Text(
                                text = stringResource(R.string.ui_export_selected_layer_only_sticker),
                                color = PixelCraftColors.TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (selectedLayerOnly || canvasConfig.backgroundType == BackgroundType.TRANSPARENT) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { trimTransparent = !trimTransparent },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = trimTransparent,
                                onCheckedChange = { trimTransparent = it }
                            )
                            Text(
                                text = stringResource(R.string.ui_trim_empty_transparent_margins),
                                color = PixelCraftColors.TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.ui_export_output, activeEstimate.safeWidth, activeEstimate.safeHeight, format.label),
                    color = PixelCraftColors.Accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onShareDirectly(currentOptions()) },
                    enabled = !isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PixelCraftColors.Border)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = PixelCraftColors.Accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ui_share_without_saving_to_gallery), color = PixelCraftColors.TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onExport(currentOptions()) },
                enabled = !isExporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Accent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ui_rendering), color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ui_save_to_gallery), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (isExporting) {
                TextButton(onClick = onCancelExport) {
                    Text(stringResource(R.string.ui_stop), color = PixelCraftColors.Danger, fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel), color = PixelCraftColors.TextSecondary)
                }
            }
        }
    )
}
