package com.app.pixelcraft.presentation.components
import com.app.pixelcraft.R
import com.app.pixelcraft.ui.theme.PixelCraftColors
import androidx.compose.ui.res.stringResource
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.FontCatalog
import androidx.core.graphics.createBitmap
import com.app.pixelcraft.domain.CanvasRenderer
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.LayerBounds
import com.app.pixelcraft.domain.TextGeometryCache
import com.app.pixelcraft.domain.TransformHelper
import com.app.pixelcraft.presentation.PixelCraftUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.BoxScope

/** Floating zoom-level / pan-mode toggle with a "Fit" reset shown once the viewport has moved. */
@Composable
internal fun BoxScope.ViewportControlsPill(
    zoom: Float,
    isPanMode: Boolean,
    canFit: Boolean,
    onTogglePanMode: () -> Unit,
    onFit: () -> Unit
) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PixelCraftColors.Surface.copy(alpha = 0.94f))
            .border(1.dp, PixelCraftColors.Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isPanMode) PixelCraftColors.Accent.copy(alpha = 0.14f) else Color.Transparent)
                .clickable(onClick = onTogglePanMode)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPanMode) Icons.Default.PanTool else Icons.Default.ZoomIn,
                contentDescription = stringResource(R.string.ui_toggle_canvas_pan_mode),
                tint = if (isPanMode) PixelCraftColors.Accent else PixelCraftColors.TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${(zoom * 100).roundToInt()}%",
                color = if (isPanMode) PixelCraftColors.Accent else PixelCraftColors.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (canFit) {
            Text(
                text = stringResource(R.string.ui_fit),
                color = PixelCraftColors.Accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onFit)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

/** Live angle readout while a layer is being twisted. */
@Composable
internal fun BoxScope.RotationSnapPill(degrees: Float) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PixelCraftColors.Surface.copy(alpha = 0.95f))
            .border(1.dp, PixelCraftColors.Accent, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "${degrees.roundToInt()}°",
            color = PixelCraftColors.Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
internal fun BoxScope.EyedropperBanner(onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PixelCraftColors.Surface.copy(alpha = 0.96f))
            .border(1.5.dp, PixelCraftColors.Accent, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Colorize,
            contentDescription = null,
            tint = PixelCraftColors.Accent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.ui_tap_canvas_to_pick_color),
            color = PixelCraftColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.ui_cancel_eyedropper),
            tint = PixelCraftColors.Danger,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onCancel)
        )
    }
}

/** Long-press quick actions for the selected layer. */
@Composable
internal fun LayerContextMenu(
    expanded: Boolean,
    target: Layer?,
    offset: DpOffset,
    canPasteStyle: Boolean,
    onDismiss: () -> Unit,
    onEditText: () -> Unit,
    onDuplicate: () -> Unit,
    onCopyStyle: () -> Unit,
    onPasteStyle: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onToggleLock: (String) -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded && target != null,
        onDismissRequest = onDismiss,
        offset = offset,
        modifier = Modifier.background(PixelCraftColors.Surface)
    ) {
        if (target != null) {
            if (target.type == LayerType.TEXT && !target.isLocked) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_edit_text), color = PixelCraftColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PixelCraftColors.Accent) },
                    onClick = {
                        onDismiss()
                        onEditText()
                    }
                )
            }
            if (!target.isLocked) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_duplicate), color = PixelCraftColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PixelCraftColors.TextSecondary) },
                    onClick = {
                        onDismiss()
                        onDuplicate()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ui_copy_style), color = PixelCraftColors.TextPrimary) },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PixelCraftColors.Accent) },
                onClick = {
                    onDismiss()
                    onCopyStyle()
                }
            )
            if (canPasteStyle && !target.isLocked) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_paste_style), color = PixelCraftColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null, tint = PixelCraftColors.Accent) },
                    onClick = {
                        onDismiss()
                        onPasteStyle()
                    }
                )
            }
            HorizontalDivider(color = PixelCraftColors.Border)
            if (!target.isLocked) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_bring_to_front), color = PixelCraftColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.VerticalAlignTop, contentDescription = null, tint = PixelCraftColors.TextSecondary) },
                    onClick = {
                        onDismiss()
                        onBringToFront()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ui_send_to_back), color = PixelCraftColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, tint = PixelCraftColors.TextSecondary) },
                    onClick = {
                        onDismiss()
                        onSendToBack()
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(if (target.isLocked) stringResource(R.string.ui_unlock_layer) else stringResource(R.string.ui_lock_layer), color = PixelCraftColors.TextPrimary) },
                leadingIcon = {
                    Icon(
                        imageVector = if (target.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = PixelCraftColors.Warning
                    )
                },
                onClick = {
                    onDismiss()
                    onToggleLock(target.id)
                }
            )
            if (!target.isLocked) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete), color = PixelCraftColors.Danger) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PixelCraftColors.Danger) },
                    onClick = {
                        onDismiss()
                        onDelete()
                    }
                )
            }
        }
    }
}

internal fun DrawScope.drawVirtualGrid(
    state: PixelCraftUiState,
    canvasW: Float,
    canvasH: Float,
    effectiveScale: Float
) {
    val cols = state.gridColumns + 1
    val rows = state.gridRows + 1
    val colStep = canvasW / cols
    val rowStep = canvasH / rows
    val gridColor = PixelCraftColors.GridLine
    val strokeW = 1.2f / effectiveScale

    for (c in 1 until cols) {
        val x = c * colStep
        drawLine(gridColor, Offset(x, 0f), Offset(x, canvasH), strokeWidth = strokeW)
    }
    for (r in 1 until rows) {
        val y = r * rowStep
        drawLine(gridColor, Offset(0f, y), Offset(canvasW, y), strokeWidth = strokeW)
    }
}

internal fun DrawScope.drawSafeZoneOverlay(
    canvasW: Float,
    canvasH: Float,
    effectiveScale: Float
) {
    val isVerticalSocial = canvasH > canvasW * 1.35f
    val leftInset = canvasW * 0.07f
    val rightInset = if (isVerticalSocial) canvasW * 0.14f else canvasW * 0.07f
    val topInset = if (isVerticalSocial) canvasH * 0.13f else canvasH * 0.08f
    val bottomInset = if (isVerticalSocial) canvasH * 0.20f else canvasH * 0.10f

    val safeW = (canvasW - leftInset - rightInset).coerceAtLeast(1f)
    val safeH = (canvasH - topInset - bottomInset).coerceAtLeast(1f)
    val dash = PathEffect.dashPathEffect(floatArrayOf(14f / effectiveScale, 10f / effectiveScale))

    drawRect(
        color = PixelCraftColors.Terracotta.copy(alpha = 0.75f),
        topLeft = Offset(leftInset, topInset),
        size = Size(safeW, safeH),
        style = Stroke(width = 2f / effectiveScale, pathEffect = dash)
    )
}
