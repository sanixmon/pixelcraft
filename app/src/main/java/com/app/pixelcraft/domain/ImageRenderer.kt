package com.app.pixelcraft.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.app.pixelcraft.data.model.Layer

/** Picture drawing shared by the editor canvas and the exporter, so the export matches the preview. */
object ImageRenderer {

    /** Fills a [w] x [h] area with the bitmap, centre-cropped to keep its proportions. */
    fun drawCover(canvas: Canvas, bitmap: Bitmap, w: Float, h: Float) {
        val crop = ImageFit.coverCrop(bitmap.width, bitmap.height, w, h)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(
            bitmap,
            Rect(crop.left, crop.top, crop.right, crop.bottom),
            RectF(0f, 0f, w, h),
            paint
        )
    }

    /** Draws an image layer centred on the current origin, sized to the layer's box. */
    fun drawImageLayer(canvas: Canvas, bitmap: Bitmap, layer: Layer) {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
            if (layer.shadowEnabled) {
                setShadowLayer(layer.shadowRadius, layer.shadowDx, layer.shadowDy, layer.shadowColor.toInt())
            }
        }
        canvas.drawBitmap(bitmap, null, RectF(-halfW, -halfH, halfW, halfH), paint)
    }
}
