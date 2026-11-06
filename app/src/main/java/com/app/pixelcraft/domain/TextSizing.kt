package com.app.pixelcraft.domain

import android.content.Context
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.FontCatalog
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

fun interface TextMeasurer {
    fun measureBounds(layer: Layer): Pair<Float, Float>?
}

/**
 * Pure ARGB, HSV, and Hex color operations (zero Android framework calls,
 * 100% safe in JVM unit tests and Compose `Color(Long)` which requires unsigned 32-bit `0..0xFFFFFFFFL`).
 */
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

    /** Extracts alpha in `0f..1f` from unsigned/signed 32-bit ARGB [argb]. */
    fun extractAlpha(argb: Long): Float {
        return (((argb ushr 24) and 0xFFL).toFloat() / 255f).coerceIn(0f, 1f)
    }

    /** Replaces the alpha channel of [argb] with [alpha] (`0f..1f`) and returns unsigned 32-bit `Long`. */
    fun withAlpha(argb: Long, alpha: Float): Long {
        val a = (alpha.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255).toLong()
        val rgb = argb and 0x00FFFFFFL
        return ((a shl 24) or rgb) and 0xFFFFFFFFL
    }

    /** Converts [argb] into `[hue (0..360), saturation (0..1), value (0..1)]`. */
    fun argbToHsv(argb: Long): FloatArray {
        val r = ((argb ushr 16) and 0xFFL).toFloat() / 255f
        val g = ((argb ushr 8) and 0xFFL).toFloat() / 255f
        val b = (argb and 0xFFL).toFloat() / 255f

        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC

        val h = when {
            delta <= 1e-6f -> 0f
            maxC == r -> 60f * (((g - b) / delta) % 6f)
            maxC == g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }.let { if (it < 0f) it + 360f else it }

        val s = if (maxC <= 1e-6f) 0f else (delta / maxC).coerceIn(0f, 1f)
        val v = maxC.coerceIn(0f, 1f)
        return floatArrayOf(h, s, v)
    }

    /** Converts HSV (`h` in `0..360`, `s` in `0..1`, `v` in `0..1`) + `alpha` (`0..1`) into unsigned 32-bit `0xAARRGGBBL`. */
    fun hsvToArgb(h: Float, s: Float, v: Float, alpha: Float = 1f): Long {
        val cleanH = ((h % 360f) + 360f) % 360f
        val cleanS = s.coerceIn(0f, 1f)
        val cleanV = v.coerceIn(0f, 1f)
        val cleanA = (alpha.coerceIn(0f, 1f) * 255f).roundToInt().coerceIn(0, 255).toLong()

        val c = cleanV * cleanS
        val x = c * (1f - abs(((cleanH / 60f) % 2f) - 1f))
        val m = cleanV - c

        val (rPrime, gPrime, bPrime) = when {
            cleanH < 60f -> Triple(c, x, 0f)
            cleanH < 120f -> Triple(x, c, 0f)
            cleanH < 180f -> Triple(0f, c, x)
            cleanH < 240f -> Triple(0f, x, c)
            cleanH < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        val r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
        val g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255).toLong()
        val b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255).toLong()

        return ((cleanA shl 24) or (r shl 16) or (g shl 8) or b) and 0xFFFFFFFFL
    }

    /** Formats [argb] as uppercase `#RRGGBB` or `#AARRGGBB`. */
    fun formatHex(argb: Long, includeAlpha: Boolean = false): String {
        val unsigned = argb and 0xFFFFFFFFL
        return if (includeAlpha) {
            String.format("#%08X", unsigned)
        } else {
            String.format("#%06X", unsigned and 0x00FFFFFFL)
        }
    }

    /**
     * Parses `#RRGGBB`, `RRGGBB`, `#AARRGGBB`, or `AARRGGBB` into unsigned 32-bit `0xAARRGGBBL`.
     * Returns `null` if [input] is not a valid 6- or 8-digit hexadecimal color.
     */
    fun parseHexOrNull(input: String, defaultAlpha: Int = 0xFF): Long? {
        val cleaned = input.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        if (!cleaned.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null

        val parsed = cleaned.toLongOrNull(16) ?: return null
        return if (cleaned.length == 6) {
            val a = defaultAlpha.coerceIn(0, 255).toLong()
            ((a shl 24) or parsed) and 0xFFFFFFFFL
        } else {
            parsed and 0xFFFFFFFFL
        }
    }
}

/**
 * Accurate text bounds calculator using [TextGeometryCache] via [TextMeasurer] on Android devices,
 * with a calibrated JVM fallback so unit tests run cleanly without Android layoutlib.
 */
object TextSizing {

    fun androidMeasurer(context: Context): TextMeasurer = TextMeasurer { layer ->
        runCatching {
            val tf = FontCatalog.resolveTypeface(context, layer.fontKey, layer.isBold, layer.isItalic)
            val geom = TextGeometryCache.getOrBuild(layer, tf)
            if (geom.maxLineWidth > 1f && geom.totalHeight > 1f) {
                geom.maxLineWidth to geom.totalHeight
            } else {
                null
            }
        }.getOrNull()
    }

    fun estimateLineWidth(
        line: String,
        fontSize: Float,
        isBold: Boolean,
        fontKey: String = FontCatalog.DEFAULT_KEY,
        letterSpacing: Float = 0f
    ): Float {
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
        val fontFactor = FontCatalog.widthFactor(fontKey)
        val spacingPx = if (line.length > 1) (line.length - 1) * letterSpacing * fontSize else 0f
        return (units * fontSize * boldFactor * fontFactor + spacingPx).coerceAtLeast(0f)
    }

    fun withEstimatedBounds(layer: Layer, measurer: TextMeasurer? = null): Layer {
        return when (layer.type) {
            LayerType.TEXT -> {
                val measured = measurer?.measureBounds(layer)
                val (rawW, rawH) = if (measured != null) {
                    measured
                } else {
                    val lines = layer.text.split("\n").ifEmpty { listOf("") }
                    val maxLineW = lines.maxOfOrNull {
                        estimateLineWidth(it, layer.fontSize, layer.isBold, layer.fontKey, layer.letterSpacing)
                    } ?: 0f
                    val lineHeight = layer.fontSize * 1.2f * layer.lineHeightMultiplier
                    maxLineW to (lines.size * lineHeight)
                }

                val bgPad = if (layer.textBackgroundColor != null) layer.textBackgroundPadding * 2f else 0f
                val strokePad = when {
                    layer.strokeWidth <= 0f -> 0f
                    layer.strokePosition == StrokePosition.OUTSIDE -> layer.strokeWidth * 2f
                    layer.strokePosition == StrokePosition.CENTER -> layer.strokeWidth
                    else -> 0f
                }
                val glowPad = if (layer.glowEnabled) layer.glowRadius * 1.5f else 0f
                val extrudeX = if (layer.is3D) abs(cos(layer.angle3D * PI / 180.0)).toFloat() * layer.depth3D else 0f
                val extrudeY = if (layer.is3D) abs(sin(layer.angle3D * PI / 180.0)).toFloat() * layer.depth3D else 0f

                val estW = (rawW + bgPad + strokePad + glowPad + extrudeX + 24f).coerceAtLeast(80f)
                val estH = (rawH + bgPad + strokePad + glowPad + extrudeY + 20f).coerceAtLeast(48f)
                layer.copy(width = estW, height = estH)
            }
            else -> layer
        }
    }

    fun transformCase(text: String, mode: TextCaseMode): String = when (mode) {
        TextCaseMode.UPPERCASE -> text.uppercase()
        TextCaseMode.LOWERCASE -> text.lowercase()
        TextCaseMode.TITLE_CASE -> text.split("\n").joinToString("\n") { line ->
            line.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.titlecase() }
            }
        }
    }
}

enum class TextCaseMode {
    UPPERCASE,
    LOWERCASE,
    TITLE_CASE
}
