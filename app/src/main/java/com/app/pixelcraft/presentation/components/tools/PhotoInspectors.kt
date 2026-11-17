package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.EditorColors
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
    if (layer.type != LayerType.IMAGE) return
    // The full-screen crop editor opens straight away; this row lets you reopen or reset it.
    var showEditor by remember(layer.id) { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { showEditor = true },
            colors = ButtonDefaults.buttonColors(containerColor = EditorColors.Accent, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(stringResource(R.string.ui_crop_adjust), fontSize = 13.sp, color = Color.White)
        }
        OutlinedButton(
            onClick = {
                onUpdate(false) { l ->
                    // Restore the full photo at its natural aspect, keeping the on-canvas area.
                    val full = l.copy(cropLeft = 0f, cropTop = 0f, cropRight = 1f, cropBottom = 1f)
                    val cw = (l.cropRight - l.cropLeft).coerceAtLeast(0.01f)
                    val ch = (l.cropBottom - l.cropTop).coerceAtLeast(0.01f)
                    val pxRatioNow = l.width / l.height
                    val naturalRatio = pxRatioNow * ch / cw
                    val area = l.width * l.height
                    full.copy(
                        width = kotlin.math.sqrt(area * naturalRatio),
                        height = kotlin.math.sqrt(area / naturalRatio)
                    )
                }
            },
            border = BorderStroke(1.dp, EditorColors.Border),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(stringResource(R.string.ui_full_reset), color = EditorColors.TextPrimary, fontSize = 13.sp)
        }
    }
    if (showEditor) {
        CropEditorDialog(
            layer = layer,
            onApply = { crop, w, h ->
                onUpdate(false) {
                    it.copy(
                        cropLeft = crop.left, cropTop = crop.top, cropRight = crop.right, cropBottom = crop.bottom,
                        width = w, height = h
                    )
                }
                showEditor = false
            },
            onDismiss = { showEditor = false }
        )
    }
}

/** Photo filter looks (B&W, Vintage, Noir...), shown as their own tool next to Adjust. */
@Composable
internal fun ColumnScope.PhotoFilterInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit
) {
    if (layer.type != LayerType.IMAGE) return
    ToolDropdown(
        label = stringResource(R.string.ui_filter),
        options = PhotoFilterPreset.entries,
        selected = layer.imageAdjustments.filterPreset,
        optionLabel = { it.localizedLabel() },
        onSelect = { preset ->
            onUpdate(false) { it.copy(imageAdjustments = it.imageAdjustments.copy(filterPreset = preset)) }
        }
    )
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
            Text(stringResource(R.string.ui_filter_presets), color = EditorColors.TextSecondary, fontSize = 12.sp)
            if (!adj.isDefault || adj.hasBlur) {
                OutlinedButton(
                    onClick = {
                        onUpdate(false) { it.copy(imageAdjustments = ImageAdjustments()) }
                    },
                    border = BorderStroke(1.dp, EditorColors.Border)
                ) {
                    Text(stringResource(R.string.ui_reset_all), color = EditorColors.Accent, fontSize = 11.sp)
                }
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
        PrecisionSlider(
            label = stringResource(R.string.ui_hue),
            value = adj.hue,
            valueRange = -180f..180f,
            defaultValue = 0f,
            fineStep = 2f,
            unitSuffix = "°",
            onValueChange = { h, cont -> onUpdate(cont) { it.copy(imageAdjustments = it.imageAdjustments.copy(hue = h)) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_blur),
            value = adj.blur,
            valueRange = 0f..25f,
            defaultValue = 0f,
            fineStep = 1f,
            unitSuffix = "px",
            onValueChange = { b, cont -> onUpdate(cont) { it.copy(imageAdjustments = it.imageAdjustments.copy(blur = b)) } },
            onValueChangeFinished = onFinishContinuousEdit
        )
        PrecisionSlider(
            label = stringResource(R.string.ui_tint),
            value = adj.tintStrength * 100f,
            valueRange = 0f..100f,
            defaultValue = 0f,
            fineStep = 5f,
            unitSuffix = "%",
            onValueChange = { t, cont ->
                onUpdate(cont) { it.copy(imageAdjustments = it.imageAdjustments.copy(tintStrength = t / 100f)) }
            },
            onValueChangeFinished = onFinishContinuousEdit
        )
        ColorPaletteRow(
            colors = ObjectPalette,
            selectedColor = adj.tintColor,
            // Choosing a tint colour with no tint yet also turns the tint on.
            onSelectColor = { c ->
                onUpdate(false) {
                    val a = it.imageAdjustments
                    it.copy(imageAdjustments = a.copy(tintColor = c, tintStrength = if (a.tintStrength <= 0f) 0.35f else a.tintStrength))
                }
            },
            allowAlpha = false,
            pickerTitle = stringResource(R.string.ui_tint)
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
        val featuredMasks = listOf(
            null,
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
        ToolDropdown(
            label = stringResource(R.string.ui_clip_photo_into_shape),
            options = featuredMasks,
            selected = layer.imageMaskShape,
            optionLabel = { it?.localizedLabel() ?: stringResource(R.string.ui_none) },
            onSelect = { mask -> onUpdate(false) { it.copy(imageMaskShape = mask) } }
        )

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
    }
}

/**
 * Stroke for photos and merged pictures. On a merged picture (a cut-out) it outlines the
 * shape itself, sticker style; on a photo it frames the photo or its mask shape.
 */
@Composable
internal fun ColumnScope.PhotoStrokeInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit
) {
    if (layer.type != LayerType.IMAGE) return
    PrecisionSlider(
        label = stringResource(R.string.ui_stroke_width),
        value = layer.imageBorderWidth,
        valueRange = 0f..40f,
        defaultValue = 0f,
        fineStep = 1f,
        unitSuffix = "px",
        onValueChange = { bw, cont -> onUpdate(cont) { it.copy(imageBorderWidth = bw) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    ColorPaletteRow(
        colors = ObjectPalette,
        recentColors = recentColors,
        selectedColor = layer.imageBorderColor,
        // Picking a colour with no stroke yet also turns the stroke on.
        onSelectColor = { c ->
            onUpdate(false) {
                it.copy(imageBorderColor = c, imageBorderWidth = if (it.imageBorderWidth < 1f) 8f else it.imageBorderWidth)
            }
        },
        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.IMAGE_BORDER) },
        allowAlpha = true,
        pickerTitle = stringResource(R.string.ui_stroke_color)
    )
}
