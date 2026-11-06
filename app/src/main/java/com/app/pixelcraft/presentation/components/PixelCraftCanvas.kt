package com.app.pixelcraft.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.CanvasRenderer
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.TransformHelper
import com.app.pixelcraft.presentation.PixelCraftUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PixelCraftCanvas(
    state: PixelCraftUiState,
    onSelectLayer: (String?) -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val canvasConfig = state.canvasConfig
    val safeWidth = canvasConfig.width.coerceAtLeast(1)
    val safeHeight = canvasConfig.height.coerceAtLeast(1)
    val aspect = safeWidth.toFloat() / safeHeight.toFloat()

    val imagePaths = remember(state.layers, canvasConfig.customImageUri) {
        buildSet {
            canvasConfig.customImageUri?.let { add(it) }
            state.layers.mapNotNullTo(this) { it.imageLocalPath }
        }
    }
    val bitmaps by produceState(emptyMap<String, Bitmap>(), imagePaths) {
        value = withContext(Dispatchers.IO) {
            imagePaths.mapNotNull { path -> ImageStore.loadBitmap(path)?.let { path to it } }.toMap()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181A20))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val maxHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

        var displayWidth = maxWidthPx
        val displayHeight = maxWidthPx / aspect
        if (displayHeight > maxHeightPx) {
            displayWidth = maxHeightPx * aspect
        }

        val canvasScale = (displayWidth / safeWidth.toFloat()).coerceAtLeast(0.01f)

        val currentState by rememberUpdatedState(state)
        val currentScale by rememberUpdatedState(canvasScale)
        val currentOnSelectLayer by rememberUpdatedState(onSelectLayer)
        val currentOnDragDelta by rememberUpdatedState(onDragDelta)
        val currentOnDragEnd by rememberUpdatedState(onDragEnd)

        fun hitTestTopInteractiveLayerId(offset: Offset): String? {
            val st = currentState
            val scaleVal = currentScale
            val virtualTouch = PointF(offset.x / scaleVal, offset.y / scaleVal)
            val canvasCenter = PointF(
                st.canvasConfig.width.coerceAtLeast(1) / 2f,
                st.canvasConfig.height.coerceAtLeast(1) / 2f
            )
            // Reverse ascending zIndex so ties and distinct zIndices match render order identically.
            val topToBottom = st.layers
                .filter { it.isVisible && !it.isLocked }
                .sortedBy { it.zIndex }
                .asReversed()
            return topToBottom.firstOrNull { layer ->
                val bounds = TransformHelper.computeLayerBounds(layer, canvasCenter)
                TransformHelper.hitTestBody(virtualTouch, bounds)
            }?.id
        }

        Box(
            modifier = Modifier
                .aspectRatio(aspect)
                .shadow(12.dp, RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val hitId = hitTestTopInteractiveLayerId(tapOffset)
                        currentOnSelectLayer(hitId)
                    }
                }
                .pointerInput(Unit) {
                    var draggingActiveLayer = false
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val hitId = hitTestTopInteractiveLayerId(startOffset)
                            if (hitId != null) {
                                if (currentState.selectedLayerId != hitId) {
                                    currentOnSelectLayer(hitId)
                                }
                                draggingActiveLayer = true
                            } else {
                                draggingActiveLayer = false
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (draggingActiveLayer) {
                                change.consume()
                                val scaleVal = currentScale
                                currentOnDragDelta(dragAmount.x / scaleVal, dragAmount.y / scaleVal)
                            }
                        },
                        onDragEnd = {
                            if (draggingActiveLayer) {
                                draggingActiveLayer = false
                                currentOnDragEnd()
                            }
                        },
                        onDragCancel = {
                            if (draggingActiveLayer) {
                                draggingActiveLayer = false
                                currentOnDragEnd()
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                scale(scale = canvasScale, pivot = Offset.Zero) {
                    val native = drawContext.canvas.nativeCanvas
                    val virtualCenter = Offset(safeWidth / 2f, safeHeight / 2f)

                    // 1. Background, Layers, and Post-Processing Filters (shared with ImageExporter)
                    CanvasRenderer.renderBackground(
                        canvas = native,
                        config = canvasConfig,
                        bitmapLookup = { bitmaps[it] },
                        drawCheckerboardWhenTransparent = true,
                        checkerCellPx = 24f / canvasScale
                    )
                    CanvasRenderer.renderLayers(
                        canvas = native,
                        config = canvasConfig,
                        layers = state.layers,
                        bitmapLookup = { bitmaps[it] },
                        typefaceResolver = { fontKey, isBold, isItalic ->
                            FontCatalog.resolveTypeface(context, fontKey, isBold, isItalic)
                        }
                    )
                    CanvasRenderer.renderFilters(
                        canvas = native,
                        config = canvasConfig
                    )

                    // 2. Selected Layer Outline (editor-only)
                    val active = state.selectedLayer
                    if (active != null && active.isVisible) {
                        val activeCenter = Offset(
                            virtualCenter.x + active.positionX,
                            virtualCenter.y + active.positionY
                        )
                        val outlineColor = if (active.isLocked) Color(0xFFFFB300) else Color(0xFF00E5FF)
                        val shapeOuterPad = if (active.type == LayerType.SHAPE && active.shapeStrokeWidth > 0f) {
                            when (active.strokePosition) {
                                StrokePosition.OUTSIDE -> active.shapeStrokeWidth
                                StrokePosition.CENTER -> active.shapeStrokeWidth / 2f
                                StrokePosition.INSIDE -> 0f
                            }
                        } else {
                            0f
                        }
                        translate(left = activeCenter.x, top = activeCenter.y) {
                            rotate(degrees = active.rotation, pivot = Offset.Zero) {
                                val halfW = (active.width / 2f) + shapeOuterPad
                                val halfH = (active.height / 2f) + shapeOuterPad
                                drawRect(
                                    color = outlineColor,
                                    topLeft = Offset(-halfW, -halfH),
                                    size = Size(halfW * 2f, halfH * 2f),
                                    style = Stroke(
                                        width = 2.5f / canvasScale,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(12f / canvasScale, 8f / canvasScale)
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // 3. Magnetic Anchor Guides (active ONLY when Grid is enabled)
                    if (state.isGridEnabled) {
                        state.activeVerticalGuideX?.let { gx ->
                            val canvasX = virtualCenter.x + gx
                            drawLine(
                                color = Color(0xFFFFEB3B),
                                start = Offset(canvasX, 0f),
                                end = Offset(canvasX, safeHeight.toFloat()),
                                strokeWidth = 2.5f / canvasScale
                            )
                        }
                        state.activeHorizontalGuideY?.let { gy ->
                            val canvasY = virtualCenter.y + gy
                            drawLine(
                                color = Color(0xFFFFEB3B),
                                start = Offset(0f, canvasY),
                                end = Offset(safeWidth.toFloat(), canvasY),
                                strokeWidth = 2.5f / canvasScale
                            )
                        }
                    }
                }

                // 4. Grid Overlay (editor-only)
                if (state.isGridEnabled) {
                    drawGridOverlay(state, size)
                }
            }
        }
    }
}

private fun DrawScope.drawGridOverlay(state: PixelCraftUiState, size: Size) {
    val cols = state.gridColumns + 1
    val rows = state.gridRows + 1
    val colStep = size.width / cols
    val rowStep = size.height / rows

    val gridColor = Color(0x6600E5FF)

    for (c in 1 until cols) {
        val x = c * colStep
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.5f)
    }
    for (r in 1 until rows) {
        val y = r * rowStep
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
    }
}
