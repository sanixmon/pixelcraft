package com.app.pixelcraft.domain

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.Quote
import kotlin.math.cos
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/** Pure ARGB bit operations (no Android framework calls, safe in JVM unit tests). */
object ColorMath {

    /** Scales the existing alpha channel of [argb] by [opacity] (0f..1f), preserving transparent colors. */
    fun scaleAlpha(argb: Long, opacity: Float): Int {
        val baseAlpha = ((argb ushr 24) and 0xFFL).toInt()
        val scaledAlpha = (baseAlpha * opacity.coerceIn(0f, 1f)).roundToInt().coerceIn(0, 255)
        val rgb = argb.toInt() and 0x00FFFFFF
        return (scaledAlpha shl 24) or rgb
    }

    /** Darkens RGB channels by [factor] (0f..1f) and scales alpha by [opacity]. */
    fun darkenArgb(argb: Long, factor: Float, opacity: Float = 1f): Int {
        val baseAlpha = ((argb ushr 24) and 0xFFL).toInt()
        val a = (baseAlpha * opacity.coerceIn(0f, 1f)).roundToInt().coerceIn(0, 255)
        val keep = (1f - factor.coerceIn(0f, 1f))
        val r = ((((argb ushr 16) and 0xFFL).toInt()) * keep).roundToInt().coerceIn(0, 255)
        val g = ((((argb ushr 8) and 0xFFL).toInt()) * keep).roundToInt().coerceIn(0, 255)
        val b = (((argb and 0xFFL).toInt()) * keep).roundToInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

/** Pure text sizing & quote wrapping helpers so selection bounds and alignment fit text content. */
object TextSizing {

    fun estimateLineWidth(line: String, fontSize: Float, isBold: Boolean): Float {
        var units = 0f
        for (ch in line) {
            units += when {
                ch == ' ' -> 0.30f
                ch in "ilI1.,:;'!|()[]{}" -> 0.32f
                ch in "MW@#%&" -> 0.88f
                ch.isUpperCase() -> 0.68f
                ch.code > 0x2000 -> 1.0f
                else -> 0.56f
            }
        }
        val boldFactor = if (isBold) 1.08f else 1.0f
        return units * fontSize * boldFactor
    }

    fun withEstimatedBounds(layer: Layer): Layer {
        return when (layer.type) {
            LayerType.TEXT -> {
                val lines = layer.text.split("\n").ifEmpty { listOf("") }
                val maxLineW = lines.maxOfOrNull { estimateLineWidth(it, layer.fontSize, layer.isBold) } ?: 0f
                val lineHeight = layer.fontSize * 1.2f * layer.lineHeightMultiplier
                val rawH = lines.size * lineHeight

                val bgPad = if (layer.textBackgroundColor != null) layer.textBackgroundPadding * 2f else 0f
                val strokePad = layer.strokeWidth * 2f
                val extrudeX = if (layer.is3D) abs(cos(layer.angle3D * PI / 180.0)).toFloat() * layer.depth3D else 0f
                val extrudeY = if (layer.is3D) abs(sin(layer.angle3D * PI / 180.0)).toFloat() * layer.depth3D else 0f

                val estW = (maxLineW + bgPad + strokePad + extrudeX + 24f).coerceAtLeast(80f)
                val estH = (rawH + bgPad + strokePad + extrudeY + 20f).coerceAtLeast(48f)
                layer.copy(width = estW, height = estH)
            }
            LayerType.STICKER -> {
                val box = (layer.fontSize.coerceAtLeast(36f) * 1.6f).coerceAtLeast(64f)
                layer.copy(width = box, height = box)
            }
            else -> layer
        }
    }

    fun wrapQuote(quote: Quote, maxCharsPerLine: Int = 28): String {
        val words = quote.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val lines = mutableListOf<String>()
        var current = StringBuilder("\"")

        for (word in words) {
            if (current.length > 1 && current.length + 1 + word.length > maxCharsPerLine) {
                lines.add(current.toString())
                current = StringBuilder(word)
            } else {
                if (current.length > 1) current.append(' ')
                current.append(word)
            }
        }
        current.append('"')
        lines.add(current.toString())

        val author = quote.author.ifBlank { "Unknown" }
        lines.add("- $author")
        return lines.joinToString("\n")
    }
}
