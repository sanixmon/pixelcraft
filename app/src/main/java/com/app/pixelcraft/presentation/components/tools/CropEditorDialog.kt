package com.app.pixelcraft.presentation.components.tools

import androidx.compose.material3.minimumInteractiveComponentSize
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.ui.theme.EditorColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt

/** Crop rectangle as fractions (0..1) of the source image. */
internal data class CropRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width get() = right - left
    val height get() = bottom - top
}

private enum class CropAspect(val label: String, val ratio: Float?) {
    FREE("Free", null),
    ORIGINAL("Original", -1f),
    SQUARE("1:1", 1f),
    PORTRAIT_4_5("4:5", 4f / 5f),
    PORTRAIT_3_4("3:4", 3f / 4f),
    STORY_9_16("9:16", 9f / 16f),
    LANDSCAPE_4_3("4:3", 4f / 3f),
    WIDE_16_9("16:9", 16f / 9f)
}

private enum class Handle { MOVE, TL, TR, BL, BR, L, T, R, B }

private const val MIN_FRACTION = 0.06f

/**
 * Full-screen crop editor: the whole photo with a draggable crop box (corners, edges, or
 * drag inside to move), rule-of-thirds guides and aspect presets. Apply hands back the
 * crop plus the layer size that shows it undistorted at about the same area on canvas.
 */
@Composable
internal fun CropEditorDialog(
    layer: Layer,
    onApply: (crop: CropRect, width: Float, height: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(layer.imageLocalPath) {
        bitmap = layer.imageLocalPath?.let { path ->
            withContext(Dispatchers.IO) { ImageStore.loadBitmap(path, forExport = false) }
        }
    }
    var crop by remember {
        mutableStateOf(CropRect(layer.cropLeft, layer.cropTop, layer.cropRight, layer.cropBottom))
    }
    var aspect by remember { mutableStateOf(CropAspect.FREE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.ui_cancel), color = EditorColors.TextSecondary)
                }
                Text(stringResource(R.string.ui_crop), color = EditorColors.TextPrimary, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    val bmp = bitmap ?: return@TextButton
                    val pxRatio = (crop.width * bmp.width) / (crop.height * bmp.height)
                    val area = layer.width * layer.height
                    onApply(crop, sqrt(area * pxRatio), sqrt(area / pxRatio))
                }) {
                    Text(stringResource(R.string.ui_apply), color = EditorColors.Accent, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val bmp = bitmap
                if (bmp == null) {
                    CircularProgressIndicator(color = EditorColors.Accent)
                } else {
                    CropSurface(
                        bitmap = bmp,
                        crop = crop,
                        ratio = aspect.ratio?.let { if (it < 0f) bmp.width.toFloat() / bmp.height else it },
                        onCropChange = { crop = it }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AspectChip(stringResource(R.string.ui_full_reset), selected = false) {
                    aspect = CropAspect.FREE
                    crop = CropRect(0f, 0f, 1f, 1f)
                }
                for (option in CropAspect.entries) {
                    val label = when (option) {
                        CropAspect.FREE -> stringResource(R.string.ui_crop_free)
                        CropAspect.ORIGINAL -> stringResource(R.string.ui_crop_original)
                        else -> option.label
                    }
                    AspectChip(label, selected = aspect == option) {
                        aspect = option
                        val bmp = bitmap ?: return@AspectChip
                        val r = option.ratio?.let { if (it < 0f) bmp.width.toFloat() / bmp.height else it }
                        if (r != null) crop = fitAspect(crop, r, bmp.width.toFloat() / bmp.height)
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) EditorColors.TextPrimary else EditorColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) EditorColors.Background else EditorColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CropSurface(
    bitmap: Bitmap,
    crop: CropRect,
    ratio: Float?,
    onCropChange: (CropRect) -> Unit
) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    val imageAspect = bitmap.width.toFloat() / bitmap.height
    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val boxAspect = maxWidth / maxHeight
        val (w, h) = if (imageAspect > boxAspect) maxWidth to maxWidth / imageAspect else maxHeight * imageAspect to maxHeight
        val density = LocalDensity.current
        val wPx = with(density) { w.toPx() }
        val hPx = with(density) { h.toPx() }
        val touchSlop = with(density) { 28.dp.toPx() }

        Box(modifier = Modifier.size(w, h)) {
            Image(bitmap = image, contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            var handle by remember { mutableStateOf(Handle.MOVE) }
            val current by rememberUpdatedState(crop)
            val onChange by rememberUpdatedState(onCropChange)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(wPx, hPx, ratio) {
                        detectDragGestures(
                            onDragStart = { p -> handle = pickHandle(p, current, wPx, hPx, touchSlop, ratio != null) },
                            onDrag = { change, delta ->
                                change.consume()
                                onChange(dragCrop(current, handle, delta.x / wPx, delta.y / hPx, ratio, imageAspect))
                            }
                        )
                    }
            ) {
                val r = Rect(crop.left * size.width, crop.top * size.height, crop.right * size.width, crop.bottom * size.height)
                // Dim everything outside the crop box.
                clipRect(r.left, r.top, r.right, r.bottom, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.6f))
                }
                // Rule-of-thirds guides.
                for (i in 1..2) {
                    val x = r.left + r.width * i / 3f
                    val y = r.top + r.height * i / 3f
                    drawLine(Color.White.copy(alpha = 0.45f), Offset(x, r.top), Offset(x, r.bottom), 1.dp.toPx())
                    drawLine(Color.White.copy(alpha = 0.45f), Offset(r.left, y), Offset(r.right, y), 1.dp.toPx())
                }
                drawRect(Color.White, r.topLeft, r.size, style = Stroke(2.dp.toPx()))
                // Corner brackets.
                val len = 22.dp.toPx()
                val t = 4.dp.toPx()
                for ((cx, cy, sx, sy) in listOf(
                    Quad(r.left, r.top, 1f, 1f), Quad(r.right, r.top, -1f, 1f),
                    Quad(r.left, r.bottom, 1f, -1f), Quad(r.right, r.bottom, -1f, -1f)
                )) {
                    drawRect(Color.White, Offset(if (sx > 0) cx else cx - len, if (sy > 0) cy else cy - t), Size(len, t))
                    drawRect(Color.White, Offset(if (sx > 0) cx else cx - t, if (sy > 0) cy else cy - len), Size(t, len))
                }
            }
        }
    }
}

private data class Quad(val a: Float, val b: Float, val c: Float, val d: Float)

private fun pickHandle(p: Offset, c: CropRect, w: Float, h: Float, slop: Float, locked: Boolean): Handle {
    val l = c.left * w
    val t = c.top * h
    val r = c.right * w
    val b = c.bottom * h
    fun near(x: Float, y: Float) = abs(p.x - x) < slop && abs(p.y - y) < slop
    return when {
        near(l, t) -> Handle.TL
        near(r, t) -> Handle.TR
        near(l, b) -> Handle.BL
        near(r, b) -> Handle.BR
        // With a locked aspect, edges would break the ratio, so only corners and move.
        !locked && abs(p.x - l) < slop && p.y in t..b -> Handle.L
        !locked && abs(p.x - r) < slop && p.y in t..b -> Handle.R
        !locked && abs(p.y - t) < slop && p.x in l..r -> Handle.T
        !locked && abs(p.y - b) < slop && p.x in l..r -> Handle.B
        else -> Handle.MOVE
    }
}

/**
 * Applies a drag of ([dx], [dy]) (fractions of the image) to [c]. With [ratio] set (a
 * width:height ratio in pixels) corner drags keep that ratio, anchored at the opposite corner.
 */
private fun dragCrop(c: CropRect, handle: Handle, dx: Float, dy: Float, ratio: Float?, imageAspect: Float): CropRect {
    if (handle == Handle.MOVE) {
        val nx = dx.coerceIn(-c.left, 1f - c.right)
        val ny = dy.coerceIn(-c.top, 1f - c.bottom)
        return CropRect(c.left + nx, c.top + ny, c.right + nx, c.bottom + ny)
    }
    var l = c.left
    var t = c.top
    var r = c.right
    var b = c.bottom
    when (handle) {
        Handle.TL -> { l += dx; t += dy }
        Handle.TR -> { r += dx; t += dy }
        Handle.BL -> { l += dx; b += dy }
        Handle.BR -> { r += dx; b += dy }
        Handle.L -> l += dx
        Handle.R -> r += dx
        Handle.T -> t += dy
        Handle.B -> b += dy
        Handle.MOVE -> Unit
    }
    l = l.coerceIn(0f, c.right - MIN_FRACTION)
    r = r.coerceIn(c.left + MIN_FRACTION, 1f)
    t = t.coerceIn(0f, c.bottom - MIN_FRACTION)
    b = b.coerceIn(c.top + MIN_FRACTION, 1f)
    if (ratio == null) return CropRect(l, t, r, b)

    // Height (in image fractions) that matches the width for this pixel ratio.
    val anchorRight = handle == Handle.TL || handle == Handle.BL
    val anchorBottom = handle == Handle.TL || handle == Handle.TR
    var width = r - l
    var height = width * imageAspect / ratio
    val maxH = if (anchorBottom) c.bottom else 1f - c.top
    if (height > maxH) {
        height = maxH
        width = height * ratio / imageAspect
    }
    return if (anchorRight) {
        val nl = c.right - width
        if (anchorBottom) CropRect(nl, c.bottom - height, c.right, c.bottom) else CropRect(nl, c.top, c.right, c.top + height)
    } else {
        if (anchorBottom) CropRect(c.left, c.bottom - height, c.left + width, c.bottom) else CropRect(c.left, c.top, c.left + width, c.top + height)
    }
}

/** Largest box with pixel [ratio] centred on [c] that fits inside the image. */
private fun fitAspect(c: CropRect, ratio: Float, imageAspect: Float): CropRect {
    // In fraction units, a box with pixel ratio `ratio` has width/height = ratio / imageAspect.
    val fracRatio = ratio / imageAspect
    var w = 1f
    var h = w / fracRatio
    if (h > 1f) {
        h = 1f
        w = h * fracRatio
    }
    val cx = ((c.left + c.right) / 2f).coerceIn(w / 2f, 1f - w / 2f)
    val cy = ((c.top + c.bottom) / 2f).coerceIn(h / 2f, 1f - h / 2f)
    return CropRect(cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f)
}
