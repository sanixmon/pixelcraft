package com.app.pixelcraft.presentation.components.dialogs

import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.domain.ColorMath
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

private val RainbowSweepColors = listOf(
    Color(0xFFFF0000),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF00FFFF),
    Color(0xFF0000FF),
    Color(0xFFFF00FF),
    Color(0xFFFF0000)
)

/**
 * Reusable horizontal color palette row with:
 * - Optional Canvas Eyedropper launcher (`onPickFromCanvas`)
 * - Leading Rainbow HSV/Hex Custom Color Picker swatch
 * - Deduplicated Recent Colors + preset swatches
 */
@Composable
fun ColorPaletteRow(
    colors: List<Long>,
    selectedColor: Long,
    onSelectColor: (Long) -> Unit,
    modifier: Modifier = Modifier,
    recentColors: List<Long> = emptyList(),
    onPickFromCanvas: (() -> Unit)? = null,
    allowAlpha: Boolean = true,
    pickerTitle: String = stringResource(R.string.ui_custom_color),
    leadingContent: (@Composable () -> Unit)? = null
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val normalizedSelected = selectedColor and 0xFFFFFFFFL
    val mergedSwatches = remember(recentColors, colors) {
        val seen = HashSet<Long>()
        buildList {
            for (c in recentColors) {
                val n = c and 0xFFFFFFFFL
                if (seen.add(n)) add(n)
            }
            for (c in colors) {
                val n = c and 0xFFFFFFFFL
                if (seen.add(n)) add(n)
            }
        }
    }
    val isCustomSelected = (normalizedSelected ushr 24) > 0L &&
        mergedSwatches.none { it == normalizedSelected }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingContent?.invoke()

        if (onPickFromCanvas != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF282D3A))
                    .border(1.5.dp, Color(0xFF00E5FF), CircleShape)
                    .clickable { onPickFromCanvas() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Colorize,
                    contentDescription = stringResource(R.string.ui_pick_color_from_canvas),
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Custom HSV Color Wheel + Hex Picker launcher swatch
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.sweepGradient(RainbowSweepColors))
                .border(
                    width = if (isCustomSelected) 2.5.dp else 1.5.dp,
                    color = if (isCustomSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.75f),
                    shape = CircleShape
                )
                .clickable { showCustomDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isCustomSelected) Color(normalizedSelected) else Color(0xCC181A20)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.ui_custom_color_wheel_hex),
                    tint = if (isCustomSelected && ColorMath.argbToHsv(normalizedSelected)[2] > 0.65f) {
                        Color.Black
                    } else {
                        Color.White
                    },
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        for (normalizedSwatch in mergedSwatches) {
            val isSelected = normalizedSelected == normalizedSwatch
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(normalizedSwatch))
                    .border(
                        width = if (isSelected) 2.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF555555),
                        shape = CircleShape
                    )
                    .clickable { onSelectColor(normalizedSwatch) }
            )
        }
    }

    if (showCustomDialog) {
        val seedColor = if ((normalizedSelected ushr 24) == 0L) 0xFFFFFFFFL else normalizedSelected
        PixelCraftColorPickerDialog(
            title = pickerTitle,
            initialColor = seedColor,
            allowAlpha = allowAlpha,
            onConfirm = { chosen ->
                showCustomDialog = false
                onSelectColor(chosen)
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

/**
 * Interactive 2D HSV Color Wheel + Brightness/Alpha sliders + `#RRGGBB` / `#AARRGGBB` Hex dialog.
 */
@Composable
fun PixelCraftColorPickerDialog(
    title: String = stringResource(R.string.ui_custom_color),
    initialColor: Long,
    allowAlpha: Boolean = true,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val cleanInitial = initialColor and 0xFFFFFFFFL
    val initialHsv = remember(cleanInitial) { ColorMath.argbToHsv(cleanInitial) }
    val initialAlpha = remember(cleanInitial, allowAlpha) {
        if (allowAlpha) ColorMath.extractAlpha(cleanInitial).let { if (it <= 0f) 1f else it } else 1f
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2].coerceAtLeast(0.05f)) }
    var alpha by remember { mutableFloatStateOf(initialAlpha) }

    val currentArgb = ColorMath.hsvToArgb(hue, saturation, value, if (allowAlpha) alpha else 1f)
    var hexText by remember {
        mutableStateOf(
            ColorMath.formatHex(
                currentArgb,
                includeAlpha = allowAlpha && initialAlpha < 0.999f
            )
        )
    }
    var hexHasError by remember { mutableStateOf(false) }

    fun syncHexFromHsv(h: Float, s: Float, v: Float, a: Float) {
        val argb = ColorMath.hsvToArgb(h, s, v, if (allowAlpha) a else 1f)
        hexText = ColorMath.formatHex(argb, includeAlpha = allowAlpha && a < 0.999f)
        hexHasError = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E222B),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HsvColorWheel(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onHueSatChanged = { newH, newS ->
                        hue = newH
                        saturation = newS
                        syncHexFromHsv(newH, newS, value, alpha)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.ui_brightness), color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    Text("${(value * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
                }
                Slider(
                    value = value,
                    onValueChange = { v ->
                        value = v
                        syncHexFromHsv(hue, saturation, v, alpha)
                    },
                    valueRange = 0f..1f
                )

                if (allowAlpha) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.ui_alpha), color = Color(0xFFCCCCCC), fontSize = 12.sp)
                        Text("${(alpha * 100).roundToInt()}%", color = Color.White, fontSize = 12.sp)
                    }
                    Slider(
                        value = alpha,
                        onValueChange = { a ->
                            alpha = a
                            syncHexFromHsv(hue, saturation, value, a)
                        },
                        valueRange = 0.02f..1f
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentArgb))
                            .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { typed ->
                            val filtered = typed.take(9)
                            hexText = filtered
                            val defaultAlphaByte = if (allowAlpha) (alpha * 255f).roundToInt() else 255
                            val parsed = ColorMath.parseHexOrNull(filtered, defaultAlpha = defaultAlphaByte)
                            if (parsed != null) {
                                val hsv = ColorMath.argbToHsv(parsed)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                                if (allowAlpha && filtered.trim().removePrefix("#").length == 8) {
                                    alpha = ColorMath.extractAlpha(parsed).coerceAtLeast(0.02f)
                                }
                                hexHasError = false
                            } else {
                                hexHasError = true
                            }
                        },
                        label = {
                            Text(
                                text = if (allowAlpha) stringResource(R.string.ui_hex_rrggbb_aarrggbb) else stringResource(R.string.ui_hex_rrggbb),
                                fontSize = 11.sp
                            )
                        },
                        isError = hexHasError,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentArgb) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(stringResource(R.string.ui_apply), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = Color.White)
            }
        }
    )
}

@Composable
private fun HsvColorWheel(
    hue: Float,
    saturation: Float,
    value: Float,
    onHueSatChanged: (Float, Float) -> Unit
) {
    fun updateFromOffset(offset: Offset, widthPx: Float, heightPx: Float) {
        val cx = widthPx / 2f
        val cy = heightPx / 2f
        val radius = (widthPx.coerceAtMost(heightPx) / 2f).coerceAtLeast(1f)
        val dx = offset.x - cx
        val dy = offset.y - cy
        val angleDeg = (((atan2(dy.toDouble(), dx.toDouble()) * 180.0 / PI).toFloat()) + 360f) % 360f
        val sat = (hypot(dx, dy) / radius).coerceIn(0f, 1f)
        onHueSatChanged(angleDeg, sat)
    }

    Box(
        modifier = Modifier
            .size(184.dp)
            .pointerInput(Unit) {
                detectTapGestures { tap ->
                    updateFromOffset(tap, size.width.toFloat(), size.height.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { start ->
                        updateFromOffset(start, size.width.toFloat(), size.height.toFloat())
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        updateFromOffset(change.position, size.width.toFloat(), size.height.toFloat())
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(184.dp)) {
            val radius = size.minDimension / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.sweepGradient(RainbowSweepColors, center = centerOffset),
                radius = radius,
                center = centerOffset
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = centerOffset,
                    radius = radius
                ),
                radius = radius,
                center = centerOffset
            )
            val darkenAlpha = (1f - value.coerceIn(0f, 1f))
            if (darkenAlpha > 0f) {
                drawCircle(
                    color = Color.Black.copy(alpha = darkenAlpha),
                    radius = radius,
                    center = centerOffset
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = 2f)
            )

            val rad = (hue * PI / 180.0)
            val thumbDist = saturation.coerceIn(0f, 1f) * radius
            val thumbCenter = Offset(
                x = centerOffset.x + (cos(rad) * thumbDist).toFloat(),
                y = centerOffset.y + (sin(rad) * thumbDist).toFloat()
            )
            drawCircle(
                color = Color.Black,
                radius = 10.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
