package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.ImageAdjustments
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PhotoFilterPreset
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

@Composable
internal fun ColumnScope.PhotoCropInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    if (layer.type == LayerType.IMAGE) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onUpdate(false) {
                        it.copy(cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f)
                    }
                },
                border = BorderStroke(1.dp, PixelCraftColors.Border)
            ) {
                Text(stringResource(R.string.ui_full_reset), color = PixelCraftColors.Accent, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    onUpdate(false) {
                        val minSide = minOf(it.width, it.height)
                        it.copy(
                            cropLeft = 0.08f,
                            cropTop = 0.08f,
                            cropRight = 0.92f,
                            cropBottom = 0.92f,
                            width = minSide,
                            height = minSide
                        )
                    }
                },
                border = BorderStroke(1.dp, PixelCraftColors.Border)
            ) {
                Text("1:1 Square", color = PixelCraftColors.TextPrimary, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    onUpdate(false) {
                        val base = minOf(it.width, it.height)
                        it.copy(width = base, height = base * 1.25f)
                    }
                },
                border = BorderStroke(1.dp, PixelCraftColors.Border)
            ) {
                Text("4:5 Portrait", color = PixelCraftColors.TextPrimary, fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = {
                    onUpdate(false) {
                        val base = maxOf(it.width, it.height)
                        it.copy(width = base, height = base * (9f / 16f))
                    }
                },
                border = BorderStroke(1.dp, PixelCraftColors.Border)
            ) {
                Text("16:9 Wide", color = PixelCraftColors.TextPrimary, fontSize = 11.sp)
            }
        }

        PrecisionSlider(
            label = stringResource(R.string.ui_crop_left),
            value = layer.cropLeft * 100f,
            valueRange = 0f..80f,
            defaultValue = 0f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { pct, cont ->
                onUpdate(cont) {
                    val cl = (pct / 100f).coerceAtMost(it.cropRight - 0.08f).coerceAtLeast(0f)
                    it.copy(cropLeft = cl)
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_crop_right),
            value = layer.cropRight * 100f,
            valueRange = 20f..100f,
            defaultValue = 100f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { pct, cont ->
                onUpdate(cont) {
                    val cr = (pct / 100f).coerceAtLeast(it.cropLeft + 0.08f).coerceAtMost(1f)
                    it.copy(cropRight = cr)
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_crop_top),
            value = layer.cropTop * 100f,
            valueRange = 0f..80f,
            defaultValue = 0f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { pct, cont ->
                onUpdate(cont) {
                    val ct = (pct / 100f).coerceAtMost(it.cropBottom - 0.08f).coerceAtLeast(0f)
                    it.copy(cropTop = ct)
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_crop_bottom),
            value = layer.cropBottom * 100f,
            valueRange = 20f..100f,
            defaultValue = 100f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { pct, cont ->
                onUpdate(cont) {
                    val cb = (pct / 100f).coerceAtLeast(it.cropTop + 0.08f).coerceAtMost(1f)
                    it.copy(cropBottom = cb)
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
}

@Composable
internal fun ColumnScope.PhotoAdjustInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    if (layer.type == LayerType.IMAGE) {
        val adj = layer.imageAdjustments
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.ui_filter_presets), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
            if (!adj.isDefault) {
                OutlinedButton(
                    onClick = {
                        onUpdate(false) { it.copy(imageAdjustments = ImageAdjustments()) }
                    },
                    border = BorderStroke(1.dp, PixelCraftColors.Border)
                ) {
                    Text(stringResource(R.string.ui_reset_all), color = PixelCraftColors.Accent, fontSize = 11.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (preset in PhotoFilterPreset.entries) {
                FilterChip(
                    selected = adj.filterPreset == preset,
                    onClick = {
                        onUpdate(false) {
                            it.copy(imageAdjustments = it.imageAdjustments.copy(filterPreset = preset))
                        }
                    },
                    label = { Text(preset.localizedLabel(), fontSize = 12.sp) }
                )
            }
        }

        PrecisionSlider(
            label = stringResource(R.string.ui_brightness),
            value = adj.brightness * 100f,
            valueRange = -50f..50f,
            defaultValue = 0f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { b, cont ->
                onUpdate(cont) {
                    it.copy(imageAdjustments = it.imageAdjustments.copy(brightness = b / 100f))
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_contrast),
            value = adj.contrast * 100f,
            valueRange = 50f..180f,
            defaultValue = 100f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { c, cont ->
                onUpdate(cont) {
                    it.copy(imageAdjustments = it.imageAdjustments.copy(contrast = c / 100f))
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_saturation),
            value = adj.saturation * 100f,
            valueRange = 0f..200f,
            defaultValue = 100f,
            fineStep = 4f,
            unitSuffix = "%",
            onValueChange = { s, cont ->
                onUpdate(cont) {
                    it.copy(imageAdjustments = it.imageAdjustments.copy(saturation = s / 100f))
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_warmth),
            value = adj.warmth * 100f,
            valueRange = -40f..40f,
            defaultValue = 0f,
            fineStep = 2f,
            unitSuffix = "%",
            onValueChange = { w, cont ->
                onUpdate(cont) {
                    it.copy(imageAdjustments = it.imageAdjustments.copy(warmth = w / 100f))
                }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
    }
}

@Composable
internal fun ColumnScope.PhotoMaskInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
    if (layer.type == LayerType.IMAGE) {
        Text(stringResource(R.string.ui_clip_photo_into_shape), color = PixelCraftColors.TextSecondary, fontSize = 12.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = layer.imageMaskShape == null,
                onClick = { onUpdate(false) { it.copy(imageMaskShape = null) } },
                label = { Text(stringResource(R.string.ui_none), fontSize = 12.sp) }
            )
            val featuredMasks = listOf(
                ShapeType.CIRCLE,
                ShapeType.ROUNDED_RECTANGLE,
                ShapeType.HEART,
                ShapeType.STAR,
                ShapeType.HEXAGON,
                ShapeType.DIAMOND,
                ShapeType.OCTAGON,
                ShapeType.SHIELD,
                ShapeType.STARBURST
            )
            for (mask in featuredMasks) {
                FilterChip(
                    selected = layer.imageMaskShape == mask,
                    onClick = { onUpdate(false) { it.copy(imageMaskShape = mask) } },
                    label = { Text(mask.localizedLabel(), fontSize = 12.sp) }
                )
            }
        }

        if (layer.imageMaskShape == null) {
            PrecisionSlider(
                label = stringResource(R.string.ui_photo_corner_radius),
                value = layer.imageCornerRadius,
                valueRange = 0f..220f,
                defaultValue = 0f,
                fineStep = 4f,
                unitSuffix = "px",
                onValueChange = { r, cont -> onUpdate(cont) { it.copy(imageCornerRadius = r) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }

        PrecisionSlider(
            label = stringResource(R.string.ui_photo_border_width),
            value = layer.imageBorderWidth,
            valueRange = 0f..40f,
            defaultValue = 0f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { bw, cont -> onUpdate(cont) { it.copy(imageBorderWidth = bw) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        if (layer.imageBorderWidth > 0f) {
            ColorPaletteRow(
                colors = ObjectPalette,
                recentColors = recentColors,
                selectedColor = layer.imageBorderColor,
                onSelectColor = { c -> onUpdate(false) { it.copy(imageBorderColor = c) } },
                onPickFromCanvas = { onStartEyedropper(EyedropperTarget.IMAGE_BORDER) },
                allowAlpha = true,
                pickerTitle = stringResource(R.string.ui_photo_border_color)
            )
        }
    }
}
