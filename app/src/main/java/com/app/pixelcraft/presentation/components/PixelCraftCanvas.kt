package com.app.pixelcraft.presentation.components

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

private enum class ActiveHandleDrag {
    NONE,
    BOTTOM_RIGHT_SCALE_ROTATE,
    RIGHT_STRETCH,
    BOTTOM_STRETCH
}

@Composable
fun PixelCraftCanvas(
    state: PixelCraftUiState,
    onSelectLayer: (String?) -> Unit,
    onDragDelta: (Float, Float, Float) -> Unit,
    onTransformGesture: (Float, Float, Float, Float) -> Boolean,
    onUpdateSelectedLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onDragEnd: () -> Unit,
    onOpenTextEdit: () -> Unit = {},
    onDeleteSelected: () -> Unit = {},
    onDuplicateSelected: () -> Unit = {},
    onCopyStyle: () -> Unit = {},
    onPasteStyle: () -> Unit = {},
    onBringToFront: () -> Unit = {},
    onSendToBack: () -> Unit = {},
    onToggleLock: (String) -> Unit = {},
    onEyedropperColorPicked: (Long) -> Unit = {},
    onCancelEyedropper: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current

    val canvasConfig = state.canvasConfig
    val safeWidth = canvasConfig.width.coerceAtLeast(1)
    val safeHeight = canvasConfig.height.coerceAtLeast(1)
    val aspect = safeWidth.toFloat() / safeHeight.toFloat()

    // Incremental bitmap cache (§ 4.6): only decodes newly added image paths instead of reloading all photos
    val bitmaps = remember { mutableStateMapOf<String, Bitmap>() }
    val imagePaths = remember(state.layers, canvasConfig.customImageUri) {
        buildSet {
            canvasConfig.customImageUri?.takeIf { it.isNotBlank() }?.let { add(it) }
            state.layers.mapNotNullTo(this) { it.imageLocalPath?.takeIf { p -> p.isNotBlank() } }
        }
    }
    LaunchedEffect(imagePaths) {
        val stale = bitmaps.keys.filter { it !in imagePaths }
        stale.forEach { bitmaps.remove(it) }
        val missing = imagePaths.filter { it !in bitmaps }
        for (path in missing) {
            val loaded = withContext(Dispatchers.IO) {
                ImageStore.loadBitmap(path, forExport = false)
            }
            if (loaded != null) {
                bitmaps[path] = loaded
            }
        }
    }

    // Viewport zoom & pan state (§ 5.1: 0.5x .. 6.0x)
    var viewportZoom by remember { mutableFloatStateOf(1f) }
    var viewportPan by remember { mutableStateOf(Offset.Zero) }
    var forceViewportPanMode by remember { mutableStateOf(false) }

    // Live single-layer drag & offscreen static-layer cache (§ 4.3)
    var isDraggingSingleLayer by remember { mutableStateOf(false) }
    var liveGestureRotation by remember { mutableStateOf<Float?>(null) }
    val staticBelowCacheRef = remember { arrayOfNulls<Bitmap>(1) }
    var staticCacheForLayerId by remember { mutableStateOf<String?>(null) }

    fun recycleStaticCache() {
        staticBelowCacheRef[0]?.let { bmp ->
            if (!bmp.isRecycled) bmp.recycle()
        }
        staticBelowCacheRef[0] = null
        staticCacheForLayerId = null
    }

    DisposableEffect(Unit) {
        onDispose { recycleStaticCache() }
    }

    // Context menu on long-press (§ 5.1)
    var contextMenuExpanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }

    // Tap cycling memory for overlapping layers (§ 5.1)
    var lastTapScreenOffset by remember { mutableStateOf<Offset?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF14171F))
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        val padPx = with(density) { 16.dp.toPx() }
        val boxWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val boxHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)
        val availWidthPx = (boxWidthPx - padPx * 2f).coerceAtLeast(1f)
        val availHeightPx = (boxHeightPx - padPx * 2f).coerceAtLeast(1f)

        var fitDisplayWidth = availWidthPx
        val fitDisplayHeight = availWidthPx / aspect
        if (fitDisplayHeight > availHeightPx) {
            fitDisplayWidth = availHeightPx * aspect
        }

        val baseScale = (fitDisplayWidth / safeWidth.toFloat()).coerceAtLeast(0.01f)
        val effectiveScale = (baseScale * viewportZoom).coerceAtLeast(0.01f)
        val containerCenter = Offset(boxWidthPx / 2f, boxHeightPx / 2f)

        val handleHitRadiusPx = with(density) { 24.dp.toPx() } // 48dp touch target (§ 5.5)
        val tapCycleSlopPx = with(density) { 16.dp.toPx() }
        val snapThresholdVirtualPx = (with(density) { 12.dp.toPx() } / effectiveScale).coerceAtLeast(2f)

        fun screenToVirtual(screenPos: Offset): PointF {
            val vx = (screenPos.x - containerCenter.x - viewportPan.x) / effectiveScale + safeWidth / 2f
            val vy = (screenPos.y - containerCenter.y - viewportPan.y) / effectiveScale + safeHeight / 2f
            return PointF(vx, vy)
        }

        fun virtualToScreen(vx: Float, vy: Float): Offset {
            val sx = (vx - safeWidth / 2f) * effectiveScale + containerCenter.x + viewportPan.x
            val sy = (vy - safeHeight / 2f) * effectiveScale + containerCenter.y + viewportPan.y
            return Offset(sx, sy)
        }

        // Exact layer dimensions using TextGeometryCache for TEXT layers (fixes Bug 2.8)
        fun exactLayerWithBounds(layer: Layer): Layer {
            if (layer.type != LayerType.TEXT) {
                val pad = if (layer.type == LayerType.SHAPE && layer.shapeStrokeWidth > 0f) {
                    when (layer.strokePosition) {
                        StrokePosition.OUTSIDE -> layer.shapeStrokeWidth * 2f
                        StrokePosition.CENTER -> layer.shapeStrokeWidth
                        StrokePosition.INSIDE -> 0f
                    }
                } else {
                    0f
                }
                return layer.copy(
                    width = (layer.width + pad).coerceAtLeast(24f),
                    height = (layer.height + pad).coerceAtLeast(24f)
                )
            }
            val tf = FontCatalog.resolveTypeface(context, layer.fontKey, layer.isBold, layer.isItalic)
            val geom = TextGeometryCache.getOrBuild(layer, tf)
            val extraPad = when {
                layer.textBackgroundColor != null -> layer.textBackgroundPadding * 2f
                layer.strokeWidth > 0f -> layer.strokeWidth * 2f + 12f
                else -> 16f
            }
            val exactW = max(geom.bounds.width() + extraPad, 32f)
            val exactH = max(geom.bounds.height() + extraPad, layer.fontSize.coerceAtLeast(24f))
            return layer.copy(width = exactW, height = exactH)
        }

        fun computeExactBounds(layer: Layer): LayerBounds {
            val canvasCenter = PointF(safeWidth / 2f, safeHeight / 2f)
            return TransformHelper.computeLayerBounds(exactLayerWithBounds(layer), canvasCenter)
        }

        val currentState by rememberUpdatedState(state)
        val currentEffectiveScale by rememberUpdatedState(effectiveScale)
        val currentSnapThreshold by rememberUpdatedState(snapThresholdVirtualPx)
        val currentContainerCenter by rememberUpdatedState(containerCenter)
        val currentForcePan by rememberUpdatedState(forceViewportPanMode)
        val currentOnSelectLayer by rememberUpdatedState(onSelectLayer)
        val currentOnDragDelta by rememberUpdatedState(onDragDelta)
        val currentOnTransformGesture by rememberUpdatedState(onTransformGesture)
        val currentOnUpdateSelected by rememberUpdatedState(onUpdateSelectedLayer)
        val currentOnDragEnd by rememberUpdatedState(onDragEnd)
        val currentOnOpenTextEdit by rememberUpdatedState(onOpenTextEdit)
        val currentOnDeleteSelected by rememberUpdatedState(onDeleteSelected)
        val currentOnDuplicateSelected by rememberUpdatedState(onDuplicateSelected)
        val currentOnEyedropperPicked by rememberUpdatedState(onEyedropperColorPicked)

        fun screenToVirtualLatest(screenPos: Offset): PointF {
            val sc = currentEffectiveScale
            val ctr = currentContainerCenter
            val pan = viewportPan
            val st = currentState
            val w = st.canvasConfig.width.coerceAtLeast(1)
            val h = st.canvasConfig.height.coerceAtLeast(1)
            return PointF(
                x = (screenPos.x - ctr.x - pan.x) / sc + w / 2f,
                y = (screenPos.y - ctr.y - pan.y) / sc + h / 2f
            )
        }

        fun virtualToScreenLatest(pt: PointF): Offset {
            val sc = currentEffectiveScale
            val ctr = currentContainerCenter
            val pan = viewportPan
            val st = currentState
            val w = st.canvasConfig.width.coerceAtLeast(1)
            val h = st.canvasConfig.height.coerceAtLeast(1)
            return Offset(
                x = (pt.x - w / 2f) * sc + ctr.x + pan.x,
                y = (pt.y - h / 2f) * sc + ctr.y + pan.y
            )
        }

        fun hitTestInteractiveLayers(screenPos: Offset, includeLocked: Boolean = false): List<Layer> {
            val st = currentState
            val vTouch = screenToVirtualLatest(screenPos)
            val cCenter = PointF(
                st.canvasConfig.width.coerceAtLeast(1) / 2f,
                st.canvasConfig.height.coerceAtLeast(1) / 2f
            )
            val ordered = st.layers
                .filter { it.isVisible && (includeLocked || !it.isLocked) }
                .sortedBy { it.zIndex }
                .asReversed()
            return ordered.filter { layer ->
                val b = TransformHelper.computeLayerBounds(exactLayerWithBounds(layer), cCenter)
                TransformHelper.hitTestBody(vTouch, b)
            }
        }

        fun detectHandleAt(screenPos: Offset, activeLayer: Layer): ActiveHandleDrag {
            if (!activeLayer.isVisible || activeLayer.isLocked) return ActiveHandleDrag.NONE
            val bounds = computeExactBounds(activeLayer)
            val brScreen = virtualToScreenLatest(bounds.bottomRight)
            if ((screenPos - brScreen).getDistance() <= handleHitRadiusPx) {
                return ActiveHandleDrag.BOTTOM_RIGHT_SCALE_ROTATE
            }
            val rightMid = PointF(
                (bounds.topRight.x + bounds.bottomRight.x) / 2f,
                (bounds.topRight.y + bounds.bottomRight.y) / 2f
            )
            if ((screenPos - virtualToScreenLatest(rightMid)).getDistance() <= handleHitRadiusPx) {
                return ActiveHandleDrag.RIGHT_STRETCH
            }
            if (activeLayer.type != LayerType.TEXT) {
                val bottomMid = PointF(
                    (bounds.bottomLeft.x + bounds.bottomRight.x) / 2f,
                    (bounds.bottomLeft.y + bounds.bottomRight.y) / 2f
                )
                if ((screenPos - virtualToScreenLatest(bottomMid)).getDistance() <= handleHitRadiusPx) {
                    return ActiveHandleDrag.BOTTOM_STRETCH
                }
            }
            return ActiveHandleDrag.NONE
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapPos ->
                            val st = currentState
                            // 1. Canvas Eyedropper sampling (§ 7.4)
                            if (st.isEyedropperActive) {
                                val vPt = screenToVirtualLatest(tapPos)
                                val cw = st.canvasConfig.width.coerceAtLeast(1)
                                val ch = st.canvasConfig.height.coerceAtLeast(1)
                                if (vPt.x in 0f..cw.toFloat() && vPt.y in 0f..ch.toFloat()) {
                                    val sampleBmp = CanvasRenderer.renderThumbnail(
                                        config = st.canvasConfig,
                                        layers = st.layers,
                                        bitmapLookup = { bitmaps[it] },
                                        typefaceResolver = { k, b, i -> FontCatalog.resolveTypeface(context, k, b, i) },
                                        maxSide = 512
                                    )
                                    val sx = ((vPt.x / cw.toFloat()) * (sampleBmp.width - 1))
                                        .roundToInt()
                                        .coerceIn(0, sampleBmp.width - 1)
                                    val sy = ((vPt.y / ch.toFloat()) * (sampleBmp.height - 1))
                                        .roundToInt()
                                        .coerceIn(0, sampleBmp.height - 1)
                                    val argb = sampleBmp.getPixel(sx, sy).toLong() and 0xFFFFFFFFL
                                    sampleBmp.recycle()
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentOnEyedropperPicked(argb)
                                }
                                return@detectTapGestures
                            }

                            // 2. Corner action handles on selected unlocked layer (§ 5.1: TL = Delete, TR = Edit/Duplicate)
                            val active = st.selectedLayer
                            if (active != null && active.isVisible && !active.isLocked) {
                                val bounds = computeExactBounds(active)
                                val tlScreen = virtualToScreenLatest(bounds.topLeft)
                                if ((tapPos - tlScreen).getDistance() <= handleHitRadiusPx) {
                                    currentOnDeleteSelected()
                                    return@detectTapGestures
                                }
                                val trScreen = virtualToScreenLatest(bounds.topRight)
                                if ((tapPos - trScreen).getDistance() <= handleHitRadiusPx) {
                                    if (active.type == LayerType.TEXT) {
                                        currentOnOpenTextEdit()
                                    } else {
                                        currentOnDuplicateSelected()
                                    }
                                    return@detectTapGestures
                                }
                            }

                            // 3. Layer selection with overlap tap-to-cycle (§ 5.1)
                            val hits = hitTestInteractiveLayers(tapPos, includeLocked = false)
                            if (hits.isEmpty()) {
                                lastTapScreenOffset = null
                                currentOnSelectLayer(null)
                            } else {
                                val prevTap = lastTapScreenOffset
                                val sameSpot = prevTap != null && (tapPos - prevTap).getDistance() <= tapCycleSlopPx
                                val currentIdx = hits.indexOfFirst { it.id == st.selectedLayerId }
                                val chosen = if (sameSpot && currentIdx >= 0 && hits.size > 1) {
                                    hits[(currentIdx + 1) % hits.size]
                                } else {
                                    hits.first()
                                }
                                lastTapScreenOffset = tapPos
                                currentOnSelectLayer(chosen.id)
                            }
                        },
                        onDoubleTap = { doubleTapPos ->
                            val st = currentState
                            if (st.isEyedropperActive) return@detectTapGestures
                            val hits = hitTestInteractiveLayers(doubleTapPos, includeLocked = false)
                            val topHit = hits.firstOrNull()
                            if (topHit != null && topHit.type == LayerType.TEXT) {
                                if (st.selectedLayerId != topHit.id) {
                                    currentOnSelectLayer(topHit.id)
                                }
                                currentOnOpenTextEdit()
                            } else if (viewportZoom != 1f || viewportPan != Offset.Zero) {
                                viewportZoom = 1f
                                viewportPan = Offset.Zero
                            }
                        },
                        onLongPress = { longPos ->
                            val st = currentState
                            if (st.isEyedropperActive) return@detectTapGestures
                            val hits = hitTestInteractiveLayers(longPos, includeLocked = true)
                            val target = hits.firstOrNull() ?: return@detectTapGestures
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnSelectLayer(target.id)
                            contextMenuOffset = with(density) {
                                DpOffset(longPos.x.toDp(), longPos.y.toDp())
                            }
                            contextMenuExpanded = true
                        }
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        if (currentState.isEyedropperActive) return@awaitEachGesture

                        val startPos = firstDown.position
                        val initialSelected = currentState.selectedLayer
                        var handleMode = if (!currentForcePan && initialSelected != null) {
                            detectHandleAt(startPos, initialSelected)
                        } else {
                            ActiveHandleDrag.NONE
                        }

                        var dragStarted = false
                        var multiTouchMode: Boolean? = null // true = transform layer, false = zoom/pan viewport
                        var draggingLayerBody = false
                        var panningViewportOneFinger = false
                        var accumulatedPan = Offset.Zero

                        // Snapshot values for handle drag
                        var handleCenterScreen = Offset.Zero
                        var handleStartDist = 1f
                        var handleStartAngleDeg = 0f
                        var handleBaseLayer: Layer? = null

                        fun initHandleSnapshot(target: Layer) {
                            val b = computeExactBounds(target)
                            handleCenterScreen = virtualToScreenLatest(PointF(b.centerX, b.centerY))
                            val vec = startPos - handleCenterScreen
                            handleStartDist = hypot(vec.x, vec.y).coerceAtLeast(8f)
                            handleStartAngleDeg = (atan2(vec.y, vec.x) * 180f / PI.toFloat())
                            handleBaseLayer = target
                        }

                        if (handleMode != ActiveHandleDrag.NONE && initialSelected != null) {
                            initHandleSnapshot(initialSelected)
                        }

                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.isEmpty()) break

                            if (pressed.size >= 2) {
                                // Two-finger gesture: either layer pinch/rotate OR canvas zoom/pan
                                if (isDraggingSingleLayer) {
                                    isDraggingSingleLayer = false
                                    recycleStaticCache()
                                }
                                handleMode = ActiveHandleDrag.NONE
                                draggingLayerBody = false

                                if (multiTouchMode == null) {
                                    val centroid = event.calculateCentroid(useCurrent = true)
                                    val active = currentState.selectedLayer
                                    val targetsLayer = !currentForcePan &&
                                        active != null &&
                                        active.isVisible &&
                                        !active.isLocked &&
                                        run {
                                            val b = computeExactBounds(active)
                                            val centerScreen = virtualToScreenLatest(PointF(b.centerX, b.centerY))
                                            val radiusScreen = max(b.width, b.height) * currentEffectiveScale * 0.85f +
                                                handleHitRadiusPx * 2f
                                            (centroid - centerScreen).getDistance() <= radiusScreen
                                        }
                                    multiTouchMode = targetsLayer
                                }

                                val zoomChange = event.calculateZoom()
                                val rotChange = event.calculateRotation()
                                val panChange = event.calculatePan()

                                if (multiTouchMode == true) {
                                    val sc = currentEffectiveScale
                                    val snapped = currentOnTransformGesture(
                                        panChange.x / sc,
                                        panChange.y / sc,
                                        zoomChange,
                                        rotChange
                                    )
                                    if (snapped) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    liveGestureRotation = currentState.selectedLayer?.rotation
                                    dragStarted = true
                                } else {
                                    val centroid = event.calculateCentroid(useCurrent = true)
                                    val newZoom = (viewportZoom * zoomChange).coerceIn(0.5f, 6.0f)
                                    val appliedFactor = newZoom / viewportZoom
                                    val pivotDelta = centroid - currentContainerCenter - viewportPan
                                    viewportZoom = newZoom
                                    viewportPan = (viewportPan + panChange + pivotDelta * (1f - appliedFactor))
                                }

                                event.changes.forEach { ch ->
                                    if (ch.positionChanged()) ch.consume()
                                }
                            } else if (multiTouchMode == null) {
                                // Single-finger drag handling
                                val change = pressed.first()
                                val delta = change.position - change.previousPosition
                                accumulatedPan += delta

                                if (!dragStarted && accumulatedPan.getDistance() > viewConfig.touchSlop) {
                                    dragStarted = true
                                    if (currentForcePan) {
                                        panningViewportOneFinger = true
                                    } else if (handleMode != ActiveHandleDrag.NONE && handleBaseLayer != null) {
                                        // Handle drag initialized above
                                    } else {
                                        val hit = hitTestInteractiveLayers(startPos, includeLocked = false).firstOrNull()
                                        if (hit != null) {
                                            if (currentState.selectedLayerId != hit.id) {
                                                currentOnSelectLayer(hit.id)
                                            }
                                            draggingLayerBody = true
                                            // Prepare static below-layer bitmap cache when single unlocked non-grouped layer drags (§ 4.3)
                                            if (hit.groupId == null) {
                                                val st = currentState
                                                val cw = st.canvasConfig.width.coerceAtLeast(1)
                                                val ch = st.canvasConfig.height.coerceAtLeast(1)
                                                val cacheScale = (currentEffectiveScale).coerceAtMost(1400f / max(cw, ch).toFloat())
                                                val bmpW = (cw * cacheScale).roundToInt().coerceAtLeast(1)
                                                val bmpH = (ch * cacheScale).roundToInt().coerceAtLeast(1)
                                                recycleStaticCache()
                                                val offscreen = runCatching {
                                                    createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888).also { bmp ->
                                                        val c = android.graphics.Canvas(bmp)
                                                        c.scale(bmpW / cw.toFloat(), bmpH / ch.toFloat())
                                                        CanvasRenderer.renderBackground(
                                                            canvas = c,
                                                            config = st.canvasConfig,
                                                            bitmapLookup = { bitmaps[it] },
                                                            drawCheckerboardWhenTransparent = true,
                                                            checkerCellPx = 24f / currentEffectiveScale
                                                        )
                                                        val belowLayers = st.layers.filter {
                                                            it.id != hit.id && it.zIndex <= hit.zIndex
                                                        }
                                                        CanvasRenderer.renderLayers(
                                                            canvas = c,
                                                            config = st.canvasConfig,
                                                            layers = belowLayers,
                                                            bitmapLookup = { bitmaps[it] },
                                                            typefaceResolver = { k, b, i ->
                                                                FontCatalog.resolveTypeface(context, k, b, i)
                                                            }
                                                        )
                                                    }
                                                }.getOrNull()
                                                staticBelowCacheRef[0] = offscreen
                                                staticCacheForLayerId = hit.id
                                                isDraggingSingleLayer = offscreen != null
                                            }
                                        } else if (viewportZoom > 1.01f) {
                                            panningViewportOneFinger = true
                                        }
                                    }
                                }

                                if (dragStarted && (delta.x != 0f || delta.y != 0f)) {
                                    when {
                                        panningViewportOneFinger -> {
                                            viewportPan += delta
                                            change.consume()
                                        }

                                        handleMode == ActiveHandleDrag.BOTTOM_RIGHT_SCALE_ROTATE -> {
                                            val base = handleBaseLayer
                                            if (base != null) {
                                                val vecNow = change.position - handleCenterScreen
                                                val distNow = hypot(vecNow.x, vecNow.y).coerceAtLeast(8f)
                                                val scaleFactor = (distNow / handleStartDist).coerceIn(0.15f, 8f)
                                                val angleNow = atan2(vecNow.y, vecNow.x) * 180f / PI.toFloat()
                                                var nextRot = (base.rotation + (angleNow - handleStartAngleDeg)) % 360f
                                                if (nextRot > 180f) nextRot -= 360f
                                                if (nextRot < -180f) nextRot += 360f
                                                val cardinals = floatArrayOf(-180f, -135f, -90f, -45f, 0f, 45f, 90f, 135f, 180f)
                                                val snappedRot = cardinals.firstOrNull { kotlin.math.abs(nextRot - it) <= 4f }
                                                if (snappedRot != null && liveGestureRotation != snappedRot) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                }
                                                val appliedRot = snappedRot ?: nextRot
                                                liveGestureRotation = appliedRot

                                                currentOnUpdateSelected(true) { cur ->
                                                    if (cur.type == LayerType.TEXT) {
                                                        cur.copy(
                                                            fontSize = (base.fontSize * scaleFactor).coerceIn(8f, 520f),
                                                            rotation = appliedRot
                                                        )
                                                    } else {
                                                        cur.copy(
                                                            width = (base.width * scaleFactor).coerceIn(24f, 4096f),
                                                            height = (base.height * scaleFactor).coerceIn(24f, 4096f),
                                                            rotation = appliedRot
                                                        )
                                                    }
                                                }
                                                change.consume()
                                            }
                                        }

                                        handleMode == ActiveHandleDrag.RIGHT_STRETCH -> {
                                            val vNow = screenToVirtualLatest(change.position)
                                            val active = currentState.selectedLayer
                                            if (active != null) {
                                                val cCenter = PointF(
                                                    currentState.canvasConfig.width / 2f + active.positionX,
                                                    currentState.canvasConfig.height / 2f + active.positionY
                                                )
                                                val localPt = TransformHelper.rotatePoint(vNow, cCenter, -active.rotation)
                                                val newHalfW = kotlin.math.abs(localPt.x - cCenter.x).coerceAtLeast(16f)
                                                currentOnUpdateSelected(true) { cur ->
                                                    if (cur.type == LayerType.TEXT) {
                                                        val ratio = (newHalfW * 2f) / exactLayerWithBounds(cur).width.coerceAtLeast(1f)
                                                        cur.copy(fontSize = (cur.fontSize * ratio).coerceIn(8f, 520f))
                                                    } else {
                                                        cur.copy(width = (newHalfW * 2f).coerceIn(32f, 4096f))
                                                    }
                                                }
                                                change.consume()
                                            }
                                        }

                                        handleMode == ActiveHandleDrag.BOTTOM_STRETCH -> {
                                            val vNow = screenToVirtualLatest(change.position)
                                            val active = currentState.selectedLayer
                                            if (active != null && active.type != LayerType.TEXT) {
                                                val cCenter = PointF(
                                                    currentState.canvasConfig.width / 2f + active.positionX,
                                                    currentState.canvasConfig.height / 2f + active.positionY
                                                )
                                                val localPt = TransformHelper.rotatePoint(vNow, cCenter, -active.rotation)
                                                val newHalfH = kotlin.math.abs(localPt.y - cCenter.y).coerceAtLeast(16f)
                                                currentOnUpdateSelected(true) { cur ->
                                                    cur.copy(height = (newHalfH * 2f).coerceIn(32f, 4096f))
                                                }
                                                change.consume()
                                            }
                                        }

                                        draggingLayerBody -> {
                                            val sc = currentEffectiveScale
                                            currentOnDragDelta(delta.x / sc, delta.y / sc, currentSnapThreshold)
                                            change.consume()
                                        }
                                    }
                                }
                            }
                        } while (true)

                        liveGestureRotation = null
                        if (isDraggingSingleLayer) {
                            isDraggingSingleLayer = false
                            recycleStaticCache()
                        }
                        if (dragStarted && (draggingLayerBody || multiTouchMode == true || handleMode != ActiveHandleDrag.NONE)) {
                            currentOnDragEnd()
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val topLeftScreen = virtualToScreen(0f, 0f)
                val canvasScreenW = safeWidth * effectiveScale
                val canvasScreenH = safeHeight * effectiveScale

                // Subtle drop-shadow frame behind canvas
                drawRect(
                    color = Color(0x66000000),
                    topLeft = Offset(topLeftScreen.x + 4f, topLeftScreen.y + 8f),
                    size = Size(canvasScreenW, canvasScreenH)
                )

                // Render virtual canvas clipped cleanly to canvas bounds
                withTransform({
                    clipRect(
                        left = topLeftScreen.x,
                        top = topLeftScreen.y,
                        right = topLeftScreen.x + canvasScreenW,
                        bottom = topLeftScreen.y + canvasScreenH
                    )
                    translate(left = topLeftScreen.x, top = topLeftScreen.y)
                    scale(scaleX = effectiveScale, scaleY = effectiveScale, pivot = Offset.Zero)
                }) {
                    val native = drawContext.canvas.nativeCanvas
                    val cachedStatic = staticBelowCacheRef[0]
                    val activeDragLayer = state.selectedLayer

                    if (
                        isDraggingSingleLayer &&
                        cachedStatic != null &&
                        !cachedStatic.isRecycled &&
                        activeDragLayer != null &&
                        staticCacheForLayerId == activeDragLayer.id
                    ) {
                        // Fast path (§ 4.3): blit cached background + below-layers bitmap, then render dragged & above layers
                        native.drawBitmap(
                            cachedStatic,
                            null,
                            android.graphics.RectF(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat()),
                            null
                        )
                        val dynamicLayers = state.layers.filter {
                            it.id == activeDragLayer.id || it.zIndex > activeDragLayer.zIndex
                        }
                        CanvasRenderer.renderLayers(
                            canvas = native,
                            config = canvasConfig,
                            layers = dynamicLayers,
                            bitmapLookup = { bitmaps[it] },
                            typefaceResolver = { fontKey, isBold, isItalic ->
                                FontCatalog.resolveTypeface(context, fontKey, isBold, isItalic)
                            }
                        )
                    } else {
                        CanvasRenderer.renderBackground(
                            canvas = native,
                            config = canvasConfig,
                            bitmapLookup = { bitmaps[it] },
                            drawCheckerboardWhenTransparent = true,
                            checkerCellPx = 24f / effectiveScale
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
                    }

                    CanvasRenderer.renderFilters(
                        canvas = native,
                        config = canvasConfig
                    )

                    // Grid Overlay
                    if (state.isGridEnabled) {
                        drawVirtualGrid(state, safeWidth.toFloat(), safeHeight.toFloat(), effectiveScale)
                    }

                    // Social Safe Zone Overlay (§ 7.4)
                    if (state.showSafeZone) {
                        drawSafeZoneOverlay(safeWidth.toFloat(), safeHeight.toFloat(), effectiveScale)
                    }

                    // Magnetic Smart Guides (active when Grid / Snap is enabled)
                    if (state.isGridEnabled) {
                        val virtualCenter = Offset(safeWidth / 2f, safeHeight / 2f)
                        state.activeVerticalGuideX?.let { gx ->
                            val canvasX = virtualCenter.x + gx
                            drawLine(
                                color = Color(0xFFFFEB3B),
                                start = Offset(canvasX, 0f),
                                end = Offset(canvasX, safeHeight.toFloat()),
                                strokeWidth = 2f / effectiveScale
                            )
                        }
                        state.activeHorizontalGuideY?.let { gy ->
                            val canvasY = virtualCenter.y + gy
                            drawLine(
                                color = Color(0xFFFFEB3B),
                                start = Offset(0f, canvasY),
                                end = Offset(safeWidth.toFloat(), canvasY),
                                strokeWidth = 2f / effectiveScale
                            )
                        }
                    }
                }

                // Canvas border
                drawRect(
                    color = Color(0xFF2B3142),
                    topLeft = topLeftScreen,
                    size = Size(canvasScreenW, canvasScreenH),
                    style = Stroke(width = 1.5f)
                )

                // Secondary selected group/multi-select outlines
                if (state.selectedLayerIds.size > 1) {
                    for (member in state.layers) {
                        if (member.id in state.selectedLayerIds && member.id != state.selectedLayerId && member.isVisible) {
                            val mb = computeExactBounds(member)
                            val centerScr = virtualToScreen(mb.centerX, mb.centerY)
                            val wScr = mb.width * effectiveScale
                            val hScr = mb.height * effectiveScale
                            rotate(degrees = mb.rotation, pivot = centerScr) {
                                drawRect(
                                    color = Color(0x9900E5FF),
                                    topLeft = Offset(centerScr.x - wScr / 2f, centerScr.y - hScr / 2f),
                                    size = Size(wScr, hScr),
                                    style = Stroke(
                                        width = 1.8f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                                    )
                                )
                            }
                        }
                    }
                }

                // Primary Selected Layer Selection Box + Direct Manipulation Handles (§ 5.1 & § 2.8)
                val active = state.selectedLayer
                if (active != null && active.isVisible && !state.isEyedropperActive) {
                    val b = computeExactBounds(active)
                    val centerScr = virtualToScreen(b.centerX, b.centerY)
                    val halfWScr = (b.width * effectiveScale) / 2f
                    val halfHScr = (b.height * effectiveScale) / 2f
                    val outlineColor = if (active.isLocked) Color(0xFFFFB300) else Color(0xFF00E5FF)

                    rotate(degrees = b.rotation, pivot = centerScr) {
                        drawRect(
                            color = outlineColor,
                            topLeft = Offset(centerScr.x - halfWScr, centerScr.y - halfHScr),
                            size = Size(halfWScr * 2f, halfHScr * 2f),
                            style = Stroke(
                                width = 2.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        )

                        if (!active.isLocked) {
                            val pillLong = 18.dp.toPx()
                            val pillShort = 6.dp.toPx()
                            // Right stretch pill
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(centerScr.x + halfWScr - pillShort / 2f, centerScr.y - pillLong / 2f),
                                size = Size(pillShort, pillLong),
                                cornerRadius = CornerRadius(pillShort / 2f, pillShort / 2f)
                            )
                            // Bottom stretch pill (for Shape & Photo)
                            if (active.type != LayerType.TEXT) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(centerScr.x - pillLong / 2f, centerScr.y + halfHScr - pillShort / 2f),
                                    size = Size(pillLong, pillShort),
                                    cornerRadius = CornerRadius(pillShort / 2f, pillShort / 2f)
                                )
                            }
                        }
                    }

                    if (!active.isLocked) {
                        val knobRadius = 11.dp.toPx()
                        // Top-Left: Delete Handle (Red badge with ×)
                        val tlScr = virtualToScreen(b.topLeft.x, b.topLeft.y)
                        drawCircle(color = Color(0xFFFF5252), radius = knobRadius, center = tlScr)
                        drawCircle(color = Color.White, radius = knobRadius, center = tlScr, style = Stroke(width = 2f))
                        val cross = knobRadius * 0.38f
                        drawLine(Color.White, Offset(tlScr.x - cross, tlScr.y - cross), Offset(tlScr.x + cross, tlScr.y + cross), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(tlScr.x + cross, tlScr.y - cross), Offset(tlScr.x - cross, tlScr.y + cross), strokeWidth = 2.4f)

                        // Top-Right: Edit (Text) / Duplicate (Shape & Image) Handle
                        val trScr = virtualToScreen(b.topRight.x, b.topRight.y)
                        drawCircle(color = Color(0xFF1E2433), radius = knobRadius, center = trScr)
                        drawCircle(color = Color(0xFF00E5FF), radius = knobRadius, center = trScr, style = Stroke(width = 2f))
                        val iconHalf = knobRadius * 0.36f
                        drawRect(
                            color = Color(0xFF00E5FF),
                            topLeft = Offset(trScr.x - iconHalf, trScr.y - iconHalf),
                            size = Size(iconHalf * 2f, iconHalf * 2f),
                            style = Stroke(width = 2f)
                        )

                        // Bottom-Right: Scale + Rotate Handle (Cyan filled knob)
                        val brScr = virtualToScreen(b.bottomRight.x, b.bottomRight.y)
                        drawCircle(color = Color(0xFF00E5FF), radius = knobRadius * 1.08f, center = brScr)
                        drawCircle(color = Color(0xFF10131A), radius = knobRadius * 0.42f, center = brScr)
                    }
                }
            }

            // Top-Left Viewport Zoom & Pan Controls Pill (§ 5.1)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xCC181C26))
                    .border(1.dp, Color(0xFF2D3446), RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (forceViewportPanMode) Color(0x3300E5FF) else Color.Transparent)
                        .clickable { forceViewportPanMode = !forceViewportPanMode }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (forceViewportPanMode) Icons.Default.PanTool else Icons.Default.ZoomIn,
                        contentDescription = "Toggle Canvas Pan Mode",
                        tint = if (forceViewportPanMode) Color(0xFF00E5FF) else Color(0xFFB8C0D0),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(viewportZoom * 100).roundToInt()}%",
                        color = if (forceViewportPanMode) Color(0xFF00E5FF) else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (viewportZoom != 1f || viewportPan != Offset.Zero) {
                    Text(
                        text = "Fit",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                viewportZoom = 1f
                                viewportPan = Offset.Zero
                                forceViewportPanMode = false
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Live Rotation Snap Pill
            liveGestureRotation?.let { deg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xDD10131A))
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${deg.roundToInt()}°",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Canvas Eyedropper Banner (§ 7.4)
            if (state.isEyedropperActive) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xEE111827))
                        .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Colorize,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tap canvas to pick color",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Eyedropper",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onCancelEyedropper() }
                    )
                }
            }

            // Long-Press Quick Context Menu (§ 5.1)
            val menuTarget = state.selectedLayer
            DropdownMenu(
                expanded = contextMenuExpanded && menuTarget != null,
                onDismissRequest = { contextMenuExpanded = false },
                offset = contextMenuOffset,
                modifier = Modifier.background(Color(0xFF1D222E))
            ) {
                if (menuTarget != null) {
                    if (menuTarget.type == LayerType.TEXT && !menuTarget.isLocked) {
                        DropdownMenuItem(
                            text = { Text("Edit Text", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF00E5FF)) },
                            onClick = {
                                contextMenuExpanded = false
                                onOpenTextEdit()
                            }
                        )
                    }
                    if (!menuTarget.isLocked) {
                        DropdownMenuItem(
                            text = { Text("Duplicate", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White) },
                            onClick = {
                                contextMenuExpanded = false
                                onDuplicateSelected()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Copy Style", color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF00E5FF)) },
                        onClick = {
                            contextMenuExpanded = false
                            onCopyStyle()
                        }
                    )
                    if (state.copiedStyleLayer != null && !menuTarget.isLocked) {
                        DropdownMenuItem(
                            text = { Text("Paste Style", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF00E5FF)) },
                            onClick = {
                                contextMenuExpanded = false
                                onPasteStyle()
                            }
                        )
                    }
                    HorizontalDivider(color = Color(0xFF2E3546))
                    if (!menuTarget.isLocked) {
                        DropdownMenuItem(
                            text = { Text("Bring to Front", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.VerticalAlignTop, contentDescription = null, tint = Color.White) },
                            onClick = {
                                contextMenuExpanded = false
                                onBringToFront()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Send to Back", color = Color.White) },
                            leadingIcon = { Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, tint = Color.White) },
                            onClick = {
                                contextMenuExpanded = false
                                onSendToBack()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(if (menuTarget.isLocked) "Unlock Layer" else "Lock Layer", color = Color.White) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (menuTarget.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFFB300)
                            )
                        },
                        onClick = {
                            contextMenuExpanded = false
                            onToggleLock(menuTarget.id)
                        }
                    )
                    if (!menuTarget.isLocked) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) },
                            onClick = {
                                contextMenuExpanded = false
                                onDeleteSelected()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawVirtualGrid(
    state: PixelCraftUiState,
    canvasW: Float,
    canvasH: Float,
    effectiveScale: Float
) {
    val cols = state.gridColumns + 1
    val rows = state.gridRows + 1
    val colStep = canvasW / cols
    val rowStep = canvasH / rows
    val gridColor = Color(0x5500E5FF)
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

private fun DrawScope.drawSafeZoneOverlay(
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
        color = Color(0xAAFF4081),
        topLeft = Offset(leftInset, topInset),
        size = Size(safeW, safeH),
        style = Stroke(width = 2f / effectiveScale, pathEffect = dash)
    )
}
