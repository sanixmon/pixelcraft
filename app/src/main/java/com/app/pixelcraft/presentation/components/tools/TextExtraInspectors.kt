package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import com.app.pixelcraft.ui.theme.EditorColors

// --- Stretch & wrap (shown in the Size panel) ------------------------------------------

@Composable
internal fun ColumnScope.TextStretchAndWrapControls(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    PrecisionSlider(
        label = stringResource(R.string.ui_stretch_width),
        value = layer.textScaleX * 100f,
        valueRange = 30f..300f,
        defaultValue = 100f,
        fineStep = 5f,
        unitSuffix = "%",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(textScaleX = v / 100f) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_stretch_height),
        value = layer.textScaleY * 100f,
        valueRange = 30f..300f,
        defaultValue = 100f,
        fineStep = 5f,
        unitSuffix = "%",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(textScaleY = v / 100f) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_wrap_width),
        value = layer.textBoxWidth,
        valueRange = 0f..2400f,
        defaultValue = 0f,
        fineStep = 10f,
        unitSuffix = "px",
        valueFormatter = { if (it < 1f) "Off" else "${it.toInt()}" },
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(textBoxWidth = if (v < 20f) 0f else v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

// --- Inner shadow ------------------------------------------------------------------------

@Composable
internal fun ColumnScope.TextInnerShadowInspector(
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    ToggleRow(stringResource(R.string.ui_inner_shadow), layer.innerShadowEnabled) { on ->
        onUpdate(false) { it.copy(innerShadowEnabled = on) }
    }
    if (!layer.innerShadowEnabled) return
    PrecisionSlider(
        label = stringResource(R.string.ui_blur),
        value = layer.innerShadowRadius,
        valueRange = 0f..40f,
        defaultValue = 6f,
        fineStep = 1f,
        unitSuffix = "px",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(innerShadowRadius = v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_offset_x),
        value = layer.innerShadowDx,
        valueRange = -30f..30f,
        defaultValue = 3f,
        fineStep = 1f,
        unitSuffix = "px",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(innerShadowDx = v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_offset_y),
        value = layer.innerShadowDy,
        valueRange = -30f..30f,
        defaultValue = 3f,
        fineStep = 1f,
        unitSuffix = "px",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(innerShadowDy = v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    ColorPaletteRow(
        colors = listOf(0x99000000, 0xCC000000, 0xAA3D0C4E, 0xAA003A5C, 0x99FFFFFF, 0xAAE63946),
        recentColors = recentColors,
        selectedColor = layer.innerShadowColor,
        onSelectColor = { c -> onUpdate(false) { it.copy(innerShadowColor = c) } },
        allowAlpha = true,
        pickerTitle = stringResource(R.string.ui_inner_shadow)
    )
}

// --- Emboss --------------------------------------------------------------------------

@Composable
internal fun ColumnScope.TextEmbossInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
    ToggleRow(stringResource(R.string.ui_emboss), layer.embossEnabled) { on ->
        onUpdate(false) { it.copy(embossEnabled = on) }
    }
    if (!layer.embossEnabled) return
    PrecisionSlider(
        label = stringResource(R.string.ui_depth),
        value = layer.embossDepth,
        valueRange = 1f..20f,
        defaultValue = 3f,
        fineStep = 0.5f,
        unitSuffix = "px",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(embossDepth = v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_light_angle),
        value = layer.embossAngle,
        valueRange = -180f..180f,
        defaultValue = -45f,
        fineStep = 5f,
        unitSuffix = "°",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(embossAngle = v) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
    PrecisionSlider(
        label = stringResource(R.string.ui_intensity),
        value = layer.embossIntensity * 100f,
        valueRange = 0f..100f,
        defaultValue = 50f,
        fineStep = 5f,
        unitSuffix = "%",
        onValueChange = { v, cont -> onUpdate(cont) { it.copy(embossIntensity = v / 100f) } },
        onValueChangeFinished = onFinishContinuousEdit
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = EditorColors.TextPrimary, fontSize = 13.sp)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

// --- One-tap text style presets ------------------------------------------------------

/** A ready-made look: keeps the text, size and position, replaces fill and effects. */
internal class TextStylePreset(
    val nameRes: Int,
    val previewBackground: Long,
    val apply: (Layer) -> Layer
)

/** Turns every fill/effect back to plain so presets don't stack on old styling. */
private fun Layer.plainStyle(): Layer = copy(
    textGradientEnabled = false,
    strokeWidth = 0f,
    strokePosition = StrokePosition.OUTSIDE,
    shadowEnabled = false,
    glowEnabled = false,
    is3D = false,
    shadow3DEnabled = false,
    reflectionEnabled = false,
    textBackgroundColor = null,
    innerShadowEnabled = false,
    embossEnabled = false,
    isItalic = false
)

internal val TextStylePresets: List<TextStylePreset> = listOf(
    TextStylePreset(R.string.preset_sticker, 0xFF3A6EA5) {
        it.plainStyle().copy(
            textColor = 0xFFFFFFFF, strokeWidth = 8f, strokeColor = 0xFF111111,
            shadowEnabled = true, shadowColor = 0x66000000, shadowRadius = 10f, shadowDx = 0f, shadowDy = 6f
        )
    },
    TextStylePreset(R.string.preset_neon, 0xFF0B0B14) {
        it.plainStyle().copy(textColor = 0xFFFFE6F7, glowEnabled = true, glowColor = 0xFFFF2E9A, glowRadius = 26f)
    },
    TextStylePreset(R.string.preset_gold_3d, 0xFF1B2735) {
        it.plainStyle().copy(
            textColor = 0xFFFFD700, textGradientEnabled = true, textGradientEndColor = 0xFFFF9800, textGradientAngle = 90f,
            strokeWidth = 2f, strokeColor = 0xFF7A4B00, is3D = true, depth3D = 14f, darken3D = 0.55f, angle3D = 45f
        )
    },
    TextStylePreset(R.string.preset_retro, 0xFF2B1055) {
        it.plainStyle().copy(
            textColor = 0xFFFF5F6D, textGradientEnabled = true, textGradientEndColor = 0xFFFFC371, textGradientAngle = 90f,
            strokeWidth = 5f, strokeColor = 0xFF2B1055,
            shadowEnabled = true, shadowColor = 0xFF00D1FF, shadowRadius = 0.5f, shadowDx = 6f, shadowDy = 6f
        )
    },
    TextStylePreset(R.string.preset_comic, 0xFF00A6FB) {
        it.plainStyle().copy(
            fontKey = "bangers", textColor = 0xFFFFE600, strokeWidth = 6f, strokeColor = 0xFF111111,
            shadowEnabled = true, shadowColor = 0xFF111111, shadowRadius = 0.5f, shadowDx = 7f, shadowDy = 7f
        )
    },
    TextStylePreset(R.string.preset_highlight, 0xFFF6F1E9) {
        it.plainStyle().copy(
            textColor = 0xFF111111, textBackgroundColor = 0xFFFFD233, textBackgroundPadding = 18f, textBackgroundRadius = 6f
        )
    },
    TextStylePreset(R.string.preset_metal, 0xFF2E3440) {
        it.plainStyle().copy(
            textColor = 0xFFC9CED6, strokeWidth = 1.5f, strokeColor = 0xFF5A6270,
            embossEnabled = true, embossDepth = 3f, embossAngle = -45f, embossIntensity = 0.65f
        )
    },
    TextStylePreset(R.string.preset_ice, 0xFF0F2027) {
        it.plainStyle().copy(
            textColor = 0xFF9BE7FF, innerShadowEnabled = true, innerShadowColor = 0xCC003A5C,
            innerShadowRadius = 8f, innerShadowDx = 0f, innerShadowDy = 4f,
            glowEnabled = true, glowColor = 0x99FFFFFF, glowRadius = 12f
        )
    },
    TextStylePreset(R.string.preset_soft_shadow, 0xFFE8ECF1) {
        it.plainStyle().copy(
            textColor = 0xFF111111, shadowEnabled = true, shadowColor = 0x55000000, shadowRadius = 18f, shadowDx = 0f, shadowDy = 10f
        )
    },
    TextStylePreset(R.string.preset_elegant, 0xFFF6F1E9) {
        it.plainStyle().copy(fontKey = "playfair_display", isItalic = true, textColor = 0xFF1C1C1C, letterSpacing = 0.02f)
    },
    TextStylePreset(R.string.preset_spaced_caps, 0xFF111111) {
        it.plainStyle().copy(fontKey = "inter", isBold = true, textColor = 0xFFFFFFFF, letterSpacing = 0.25f)
    },
    TextStylePreset(R.string.preset_outline, 0xFF8E2DE2) {
        it.plainStyle().copy(textColor = 0x00FFFFFF, strokeWidth = 3f, strokeColor = 0xFFFFFFFF, strokePosition = StrokePosition.CENTER)
    }
)

@Composable
internal fun ColumnScope.TextPresetsInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (preset in TextStylePresets) {
            val sample = remember(preset, layer.fontKey) { preset.apply(layer) }
            PresetTile(sample, preset, stringResource(preset.nameRes)) {
                onUpdate(false) { preset.apply(it) }
            }
        }
    }
}

/** A quick Compose approximation of the look (fill, gradient, outline, shadow, glow, box). */
@Composable
private fun PresetTile(sample: Layer, preset: TextStylePreset, name: String, onClick: () -> Unit) {
    val family = remember(sample.fontKey) { FontCatalog.resolveComposeFontFamily(sample.fontKey) }
    val fill = if (sample.textGradientEnabled) {
        Brush.verticalGradient(listOf(Color(sample.textColor), Color(sample.textGradientEndColor)))
    } else {
        null
    }
    val shadow = when {
        sample.glowEnabled -> Shadow(Color(sample.glowColor), blurRadius = 18f)
        sample.shadowEnabled -> Shadow(Color(sample.shadowColor), androidx.compose.ui.geometry.Offset(sample.shadowDx, sample.shadowDy), sample.shadowRadius)
        else -> null
    }
    val base = TextStyle(
        fontFamily = family,
        fontSize = 26.sp,
        fontWeight = if (sample.isBold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (sample.isItalic) FontStyle.Italic else FontStyle.Normal,
        letterSpacing = (sample.letterSpacing * 26f).sp
    )
    Column(
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, EditorColors.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(preset.previewBackground)),
            contentAlignment = Alignment.Center
        ) {
            val boxed = sample.textBackgroundColor
            Box(
                modifier = if (boxed != null) {
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(boxed))
                        .padding(horizontal = 6.dp)
                } else {
                    Modifier
                },
                contentAlignment = Alignment.Center
            ) {
                if (sample.strokeWidth > 0f) {
                    Text(
                        "Aa",
                        style = base.copy(
                            color = Color(sample.strokeColor),
                            drawStyle = Stroke(width = sample.strokeWidth.coerceAtMost(8f))
                        )
                    )
                }
                Text(
                    "Aa",
                    style = if (fill != null) {
                        base.copy(brush = fill, shadow = shadow)
                    } else {
                        base.copy(color = Color(sample.textColor), shadow = shadow)
                    }
                )
            }
        }
        Text(
            name,
            color = EditorColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}
