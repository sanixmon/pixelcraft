package com.app.pixelcraft.domain.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.CanvasRenderer
import com.app.pixelcraft.domain.ImageStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Flattens a group into one transparent picture so it can be edited as a single object
 * (crop, adjust, mask, outline, shadow...). Rendering uses the export renderer, so the
 * result matches what the canvas shows.
 */
object GroupMerger {

    /** The flattened picture and where it sits on the canvas (centre offset and size, canvas px). */
    class Merged(val bitmap: Bitmap, val centerX: Float, val centerY: Float, val width: Float, val height: Float)

    private const val MAX_SIDE = 4096f
    private const val OVERSAMPLE = 2f

    /**
     * Renders [members] onto a transparent picture just big enough to hold them.
     * [maxSide] caps the output size; thumbnails pass a small one and [fullQuality] = false
     * to draw photos from their cached previews.
     */
    suspend fun render(
        context: Context,
        members: List<Layer>,
        maxSide: Float = MAX_SIDE,
        fullQuality: Boolean = true
    ): Merged = withContext(Dispatchers.Default) {
        val visible = members.filter { it.isVisible }
        require(visible.isNotEmpty()) { "Nothing visible to merge" }

        // Room for effects that draw outside a layer's box (strokes, glow, shadows, 3D depth).
        val pad = visible.maxOf { effectPadding(it) }
        val b = GroupOperations.bounds(visible)
        val w = (b.right - b.left) + pad * 2f
        val h = (b.bottom - b.top) + pad * 2f
        val scale = min(OVERSAMPLE, maxSide / max(w, h))
        val outW = ceil(w * scale).toInt().coerceAtLeast(1)
        val outH = ceil(h * scale).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(outW / w, outH / h)

        // Re-centre the members on the merged picture's own little canvas.
        val config = CanvasConfig(
            width = ceil(w).toInt(),
            height = ceil(h).toInt(),
            backgroundType = BackgroundType.TRANSPARENT
        )
        val dx = (config.width - w) / 2f
        val dy = (config.height - h) / 2f
        val local = visible.map {
            it.copy(positionX = it.positionX - b.centerX - dx, positionY = it.positionY - b.centerY - dy, groupId = null)
        }
        CanvasRenderer.renderLayers(
            canvas = canvas,
            config = config,
            layers = local,
            bitmapLookup = { path -> ImageStore.loadBitmap(path, forExport = fullQuality) },
            typefaceResolver = { key, bold, italic -> FontCatalog.resolveTypeface(context, key, bold, italic) }
        )
        Merged(bitmap, b.centerX, b.centerY, w, h)
    }

    private fun effectPadding(layer: Layer): Float {
        var pad = 8f
        if (layer.type == LayerType.TEXT) {
            pad = max(pad, layer.strokeWidth * 2f)
            if (layer.glowEnabled) pad = max(pad, layer.glowRadius * 2f)
            if (layer.is3D) pad = max(pad, layer.depth3D * 1.5f)
            if (layer.reflectionEnabled) pad = max(pad, layer.height * (1f + layer.reflectionDistance / 100f))
        } else if (layer.type == LayerType.SHAPE) {
            pad = max(pad, layer.shapeStrokeWidth)
        } else {
            pad = max(pad, layer.imageBorderWidth * 2f)
        }
        if (layer.shadowEnabled) pad = max(pad, layer.shadowRadius * 2f + max(kotlin.math.abs(layer.shadowDx), kotlin.math.abs(layer.shadowDy)))
        return pad
    }
}
