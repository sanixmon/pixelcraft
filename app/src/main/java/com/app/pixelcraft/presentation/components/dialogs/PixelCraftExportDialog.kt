package com.app.pixelcraft.presentation.components.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.EditorColors
import kotlin.math.roundToInt

/** CapCut-style export sheet: format/quality/resolution chips, then Save and Share. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelCraftExportDialog(
    canvasConfig: CanvasConfig,
    initialOptions: ExportOptions,
    hasSelectedLayer: Boolean,
    isExporting: Boolean,
    onExport: (ExportOptions) -> Unit,
    onShareDirectly: (ExportOptions) -> Unit,
    onSaveAsProject: () -> Unit,
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
    // Custom output width (px); null = use the scale presets.
    var customWidthText by remember(initialOptions.customWidth) { mutableStateOf(initialOptions.customWidth?.toString()) }
    var jpgBackground by remember(initialOptions.backgroundColor) { mutableStateOf(initialOptions.backgroundColor) }
    val customWidth = customWidthText?.toIntOrNull()?.coerceIn(64, 8192)
    val outWidth = customWidth ?: (canvasConfig.width * qualityPreset.multiplier).roundToInt()
    val outHeight = (outWidth.toFloat() * canvasConfig.height / canvasConfig.width.coerceAtLeast(1)).roundToInt()

    fun currentOptions(): ExportOptions = ExportOptions(
        format = format,
        qualityPreset = qualityPreset,
        jpegQuality = compressionQuality.roundToInt().coerceIn(10, 100),
        selectedLayerOnly = selectedLayerOnly && hasSelectedLayer,
        trimTransparent = trimTransparent && format.supportsTransparency,
        customWidth = customWidth,
        backgroundColor = jpgBackground
    )

    val exporting by rememberUpdatedState(isExporting)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !exporting }
    )
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = EditorColors.SurfaceElevated,
        labelColor = EditorColors.TextSecondary,
        selectedContainerColor = EditorColors.Accent,
        selectedLabelColor = Color.Black
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isExporting) onDismiss() },
        sheetState = sheetState,
        containerColor = EditorColors.Surface,
        contentColor = EditorColors.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            SectionLabel(stringResource(R.string.ui_format))
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
                        label = { Text(fmt.label, fontSize = 13.sp) },
                        colors = chipColors,
                        border = null
                    )
                }
            }

            if (format.usesQualitySlider) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.ui_compression_quality), color = EditorColors.TextSecondary, fontSize = 12.sp)
                    Text("${compressionQuality.roundToInt()}%", color = EditorColors.Accent, fontSize = 12.sp)
                }
                Slider(
                    value = compressionQuality,
                    onValueChange = { compressionQuality = it },
                    valueRange = 20f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = EditorColors.Accent,
                        activeTrackColor = EditorColors.Accent,
                        inactiveTrackColor = EditorColors.Border
                    )
                )
            }

            SectionLabel(stringResource(R.string.ui_resolution_scale))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (q in ExportQuality.entries) {
                    FilterChip(
                        selected = customWidthText == null && qualityPreset == q,
                        onClick = {
                            qualityPreset = q
                            customWidthText = null
                        },
                        label = { Text(q.localizedLabel(), fontSize = 13.sp) },
                        colors = chipColors,
                        border = null
                    )
                }
                FilterChip(
                    selected = customWidthText != null,
                    onClick = { if (customWidthText == null) customWidthText = canvasConfig.width.toString() },
                    label = { Text(stringResource(R.string.ui_custom), fontSize = 13.sp) },
                    colors = chipColors,
                    border = null
                )
            }
            if (customWidthText != null) {
                OutlinedTextField(
                    value = customWidthText.orEmpty(),
                    onValueChange = { v -> customWidthText = v.filter { it.isDigit() }.take(5) },
                    label = { Text(stringResource(R.string.ui_width_px)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            Text(
                text = stringResource(R.string.ui_output_size, outWidth, outHeight),
                color = EditorColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (!format.supportsTransparency &&
                (selectedLayerOnly || canvasConfig.backgroundType == BackgroundType.TRANSPARENT)
            ) {
                SectionLabel(stringResource(R.string.ui_jpg_background))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in listOf(0xFFFFFFFFL, 0xFF000000L, 0xFFF6F1E9L, 0xFF111827L)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    width = if (jpgBackground == c) 3.dp else 1.dp,
                                    color = if (jpgBackground == c) EditorColors.Accent else EditorColors.Border,
                                    shape = CircleShape
                                )
                                .clickable { jpgBackground = c }
                        )
                    }
                }
            }

            if (format.supportsTransparency) {
                if (hasSelectedLayer) {
                    CheckRow(
                        checked = selectedLayerOnly,
                        label = stringResource(R.string.ui_export_selected_layer_only_sticker),
                        onToggle = { selectedLayerOnly = !selectedLayerOnly }
                    )
                }
                if (selectedLayerOnly || canvasConfig.backgroundType == BackgroundType.TRANSPARENT) {
                    CheckRow(
                        checked = trimTransparent,
                        label = stringResource(R.string.ui_trim_empty_transparent_margins),
                        onToggle = { trimTransparent = !trimTransparent }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onExport(currentOptions()) },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditorColors.Accent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ui_rendering), fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ui_save_to_gallery), fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onShareDirectly(currentOptions()) },
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, EditorColors.Border)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = EditorColors.TextPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.action_share), color = EditorColors.TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isExporting) {
                    TextButton(onClick = onCancelExport) {
                        Text(stringResource(R.string.ui_stop), color = EditorColors.Danger, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    TextButton(onClick = onSaveAsProject) {
                        Text(stringResource(R.string.ui_save_as_project), color = EditorColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = EditorColors.TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun CheckRow(
    checked: Boolean,
    label: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Text(text = label, color = EditorColors.TextPrimary, fontSize = 13.sp)
    }
}
