package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.domain.TransformHelper
import kotlin.math.roundToInt

@Composable
fun PixelCraftTransformHandles(
    layer: Layer,
    canvasCenter: PointF,
    canvasScale: Float,
    onDragDelta: (Float, Float) -> Unit,
    onResizeDelta: (Float, Float) -> Unit,
    onRotateDelta: (Float) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bounds = TransformHelper.computeLayerBounds(layer, canvasCenter)

    val boxWidthDp = (bounds.width * canvasScale).dp
    val boxHeightDp = (bounds.height * canvasScale).dp
    val offsetX = ((bounds.centerX - bounds.width / 2f) * canvasScale).roundToInt()
    val offsetY = ((bounds.centerY - bounds.height / 2f) * canvasScale).roundToInt()

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX, offsetY) }
            .size(boxWidthDp, boxHeightDp)
            .rotate(layer.rotation)
            .border(1.5.dp, Color(0xFF00E5FF))
    ) {
        // Rotation handle on top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-28).dp)
                .size(24.dp)
                .background(Color(0xFF00E5FF), CircleShape)
                .pointerInput(layer.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onRotateDelta(dragAmount.x * 0.5f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rotate",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }

        // Delete button top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-12).dp, y = (-12).dp)
                .size(24.dp)
                .background(Color(0xFFFF1744), CircleShape)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Duplicate button bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-12).dp, y = 12.dp)
                .size(24.dp)
                .background(Color(0xFF00E676), CircleShape)
                .clickable { onDuplicate() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Duplicate",
                tint = Color.Black,
                modifier = Modifier.size(14.dp)
            )
        }

        // Resize handle bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 12.dp, y = 12.dp)
                .size(24.dp)
                .background(Color(0xFF00E5FF), CircleShape)
                .pointerInput(layer.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onResizeDelta(dragAmount.x / canvasScale, dragAmount.y / canvasScale)
                    }
                }
        )
    }
}
