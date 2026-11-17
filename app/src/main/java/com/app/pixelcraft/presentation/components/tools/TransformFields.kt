package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.ui.theme.EditorColors
import kotlin.math.roundToInt

/**
 * Exact numbers for the selection: X / Y (layer centre, from the canvas's top-left corner,
 * in canvas pixels), W / H and rotation. Text has no W / H here: its size follows the font.
 */
@Composable
internal fun TransformFields(
    layer: Layer,
    canvasWidth: Int,
    canvasHeight: Int,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    modifier: Modifier = Modifier
) {
    val showSize = layer.type != LayerType.TEXT
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NumberField("X", stringResource(R.string.ui_position_x), layer.positionX + canvasWidth / 2f, Modifier.weight(1f)) { v ->
            onUpdate(false) { it.copy(positionX = v - canvasWidth / 2f) }
        }
        NumberField("Y", stringResource(R.string.ui_position_y), layer.positionY + canvasHeight / 2f, Modifier.weight(1f)) { v ->
            onUpdate(false) { it.copy(positionY = v - canvasHeight / 2f) }
        }
        if (showSize) {
            NumberField("W", stringResource(R.string.ui_width), layer.width, Modifier.weight(1f)) { v ->
                onUpdate(false) { it.copy(width = v.coerceIn(8f, 8192f)) }
            }
            NumberField("H", stringResource(R.string.ui_height), layer.height, Modifier.weight(1f)) { v ->
                onUpdate(false) { it.copy(height = v.coerceIn(8f, 8192f)) }
            }
        }
        NumberField("°", stringResource(R.string.ui_rotation_angle), layer.rotation, Modifier.weight(1f)) { v ->
            var d = v % 360f
            if (d > 180f) d -= 360f
            if (d <= -180f) d += 360f
            onUpdate(false) { it.copy(rotation = d) }
        }
    }
}

@Composable
private fun NumberField(label: String, description: String, value: Float, modifier: Modifier, onCommit: (Float) -> Unit) {
    val shown = value.roundToInt().toString()
    var text by remember { mutableStateOf(shown) }
    var focused by remember { mutableStateOf(false) }
    // Follow outside changes (drags, undo) unless the user is typing.
    LaunchedEffect(shown) { if (!focused) text = shown }
    val focusManager = LocalFocusManager.current

    fun commit() {
        val parsed = text.replace(',', '.').toFloatOrNull()
        if (parsed != null && parsed.roundToInt().toString() != shown) onCommit(parsed) else text = shown
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = EditorColors.TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EditorColors.SurfaceElevated)
                .border(1.dp, if (focused) EditorColors.Accent else EditorColors.Border, RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = text,
                onValueChange = { new -> text = new.filter { it.isDigit() || it == '-' || it == '.' || it == ',' }.take(7) },
                singleLine = true,
                textStyle = TextStyle(
                    color = EditorColors.TextPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(EditorColors.Accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    commit()
                    focusManager.clearFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = description }
                    .onFocusChanged { state ->
                        if (focused && !state.isFocused) commit()
                        focused = state.isFocused
                    }
            )
        }
    }
}
