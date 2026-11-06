package com.app.pixelcraft.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class FitSize(val width: Float, val height: Float)

data class CropRect(val left: Int, val top: Int, val right: Int, val bottom: Int)

/** Pure sizing maths for imported pictures (no Android types, so it is unit-testable). */
object ImageFit {

    /** Largest size with the source's aspect ratio that fits inside [maxW] x [maxH]. */
    fun fitWithin(srcW: Int, srcH: Int, maxW: Float, maxH: Float): FitSize {
        require(srcW > 0 && srcH > 0) { "Image has no size" }
        val s = min(maxW / srcW, maxH / srcH)
        return FitSize(srcW * s, srcH * s)
    }

    /** Centred region of the source that fills a [dstW] x [dstH] frame without distortion ("cover"). */
    fun coverCrop(srcW: Int, srcH: Int, dstW: Float, dstH: Float): CropRect {
        val srcAspect = srcW.toFloat() / srcH
        val dstAspect = dstW / dstH
        return if (srcAspect > dstAspect) {
            val cropW = (srcH * dstAspect).roundToInt().coerceIn(1, srcW)
            val left = (srcW - cropW) / 2
            CropRect(left, 0, left + cropW, srcH)
        } else {
            val cropH = (srcW / dstAspect).roundToInt().coerceIn(1, srcH)
            val top = (srcH - cropH) / 2
            CropRect(0, top, srcW, top + cropH)
        }
    }

    /** Power-of-two decode sample size so the longest side ends up at most [maxDim]. */
    fun sampleSize(srcW: Int, srcH: Int, maxDim: Int): Int {
        var sample = 1
        while (max(srcW, srcH) / sample > maxDim) sample *= 2
        return sample
    }
}
