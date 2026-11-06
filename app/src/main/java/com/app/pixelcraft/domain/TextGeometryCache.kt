package com.app.pixelcraft.domain

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.LruCache
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.TextAlignment
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

data class TextGeometryKey(
    val text: String,
    val fontKey: String,
    val fontSizeTenths: Int,
    val isBold: Boolean,
    val isItalic: Boolean,
    val isUnderline: Boolean,
    val align: TextAlignment,
    val lineHeightHundredths: Int,
    val letterSpacingHundredths: Int,
    val curveDegrees: Int
) {
    companion object {
        fun fromLayer(layer: Layer): TextGeometryKey = TextGeometryKey(
            text = layer.text,
            fontKey = layer.fontKey,
            fontSizeTenths = (layer.fontSize * 10f).roundToInt(),
            isBold = layer.isBold,
            isItalic = layer.isItalic,
            isUnderline = layer.isUnderline,
            align = layer.textAlign,
            lineHeightHundredths = (layer.lineHeightMultiplier * 100f).roundToInt(),
            letterSpacingHundredths = (layer.letterSpacing * 100f).roundToInt(),
            curveDegrees = layer.curveAngle.roundToInt()
        )
    }
}

class TextGeometry(
    val path: Path,
    val bounds: RectF,
    val maxLineWidth: Float,
    val totalHeight: Float
)

/**
 * Thread-safe LRU cache of pre-computed vector glyph contours and exact [RectF] bounds per [TextGeometryKey].
 * Eliminates `getTextPath` & `Path.op(UNION)` allocations during 60-120fps drag/rotate/recolor gestures
 * AND serves as the single source of truth for text selection boxes (fixes Bug 2.8 & § 4.1).
 */
object TextGeometryCache {

    private val cache = object : LruCache<TextGeometryKey, TextGeometry>(96) {}

    private val measurePaintLocal = ThreadLocal.withInitial {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun getOrBuild(layer: Layer, typeface: Typeface): TextGeometry {
        val key = TextGeometryKey.fromLayer(layer)
        synchronized(cache) {
            cache.get(key)?.let { return it }
        }
        val built = buildGeometry(layer, typeface)
        synchronized(cache) {
            cache.put(key, built)
        }
        return built
    }

    fun evictAll() {
        synchronized(cache) {
            cache.evictAll()
        }
    }

    private fun buildGeometry(layer: Layer, typeface: Typeface): TextGeometry {
        val paint = measurePaintLocal.get()!!.apply {
            reset()
            flags = Paint.ANTI_ALIAS_FLAG
            textSize = layer.fontSize.coerceAtLeast(6f)
            textAlign = Paint.Align.LEFT
            this.typeface = typeface
            isFakeBoldText = layer.isBold && !typeface.isBold
            textSkewX = if (layer.isItalic && !typeface.isItalic) -0.20f else 0f
            letterSpacing = layer.letterSpacing.coerceIn(-0.2f, 1.5f)
        }

        val curveDeg = layer.curveAngle.coerceIn(-180f, 180f)
        if (abs(curveDeg) >= 2f) {
            return buildCurvedTextGeometry(layer, paint, curveDeg)
        }

        val lines = layer.text.split("\n")
        val fm = paint.fontMetrics
        val rawSpan = (fm.descent - fm.ascent).takeIf { it > 0.5f } ?: (layer.fontSize * 1.2f)
        val lineHeight = rawSpan * layer.lineHeightMultiplier
        val totalTextHeight = (lines.size * lineHeight).coerceAtLeast(lineHeight)
        val lineWidths = lines.map { line ->
            val measured = paint.measureText(line)
            if (measured > 0f || line.isEmpty()) {
                measured
            } else {
                TextSizing.estimateLineWidth(line, layer.fontSize, layer.isBold, layer.fontKey, layer.letterSpacing)
            }
        }
        val maxLineWidth = lineWidths.maxOrNull() ?: 0f
        val startY = -totalTextHeight / 2f + lineHeight / 2f - (fm.ascent + fm.descent) / 2f

        val combined = Path()
        val linePath = Path()
        for ((idx, line) in lines.withIndex()) {
            if (line.isEmpty()) continue
            val lw = lineWidths[idx]
            val lineLeftX = when (layer.textAlign) {
                TextAlignment.LEFT -> -maxLineWidth / 2f
                TextAlignment.CENTER -> -lw / 2f
                TextAlignment.RIGHT -> maxLineWidth / 2f - lw
            }
            val baselineY = startY + (idx * lineHeight)
            linePath.reset()
            paint.getTextPath(line, 0, line.length, lineLeftX, baselineY, linePath)
            combined.addPath(linePath)

            if (layer.isUnderline && lw > 0f) {
                val underlineOffset = layer.fontSize * 0.08f
                val underlineThickness = (layer.fontSize * 0.065f).coerceAtLeast(2f)
                val underlinePath = Path().apply {
                    addRect(
                        RectF(
                            lineLeftX,
                            baselineY + underlineOffset,
                            lineLeftX + lw,
                            baselineY + underlineOffset + underlineThickness
                        ),
                        Path.Direction.CW
                    )
                }
                combined.op(underlinePath, Path.Op.UNION)
            }
        }

        val exactBounds = RectF()
        combined.computeBounds(exactBounds, true)
        if (exactBounds.isEmpty) {
            exactBounds.set(-maxLineWidth / 2f, -totalTextHeight / 2f, maxLineWidth / 2f, totalTextHeight / 2f)
        }
        val effectiveWidth = max(maxLineWidth, exactBounds.width())
        val effectiveHeight = max(totalTextHeight, exactBounds.height())

        return TextGeometry(
            path = combined,
            bounds = exactBounds,
            maxLineWidth = effectiveWidth,
            totalHeight = effectiveHeight
        )
    }

    /**
     * Builds a vector glyph contour warped smoothly along a circular arc of [curveDegrees] (`-180°..+180°`),
     * recentered around `(0, 0)` so vector strokes, 3D extrusion, glow, and hit-testing all work seamlessly!
     */
    private fun buildCurvedTextGeometry(layer: Layer, paint: Paint, curveDegrees: Float): TextGeometry {
        val singleLine = layer.text.replace("\n", " ")
        if (singleLine.isEmpty()) {
            return TextGeometry(Path(), RectF(-40f, -24f, 40f, 24f), 80f, 48f)
        }

        val charWidths = FloatArray(singleLine.length)
        paint.getTextWidths(singleLine, charWidths)
        var totalAdvance = 0f
        for (i in singleLine.indices) {
            if (charWidths[i] <= 0f) {
                charWidths[i] = TextSizing.estimateLineWidth(
                    singleLine[i].toString(),
                    layer.fontSize,
                    layer.isBold,
                    layer.fontKey,
                    layer.letterSpacing
                )
            }
            totalAdvance += charWidths[i]
        }
        totalAdvance = totalAdvance.coerceAtLeast(layer.fontSize)

        val sweepRad = (abs(curveDegrees) * PI / 180.0).toFloat().coerceAtLeast(0.05f)
        val radius = (totalAdvance / sweepRad).coerceIn(layer.fontSize * 0.8f, 8000f)
        val downward = curveDegrees > 0f // Positive curves arch upward like a rainbow; negative curves smile downward

        val combined = Path()
        val glyphPath = Path()
        val matrix = Matrix()

        var cursorAdvance = -totalAdvance / 2f
        for (i in singleLine.indices) {
            val w = charWidths[i]
            val charCenterAdvance = cursorAdvance + w / 2f
            cursorAdvance += w

            val ch = singleLine[i]
            if (ch == ' ') continue

            glyphPath.reset()
            paint.getTextPath(singleLine, i, i + 1, -w / 2f, 0f, glyphPath)

            val theta = charCenterAdvance / radius
            val x: Float
            val y: Float
            val rotDeg: Float
            if (downward) {
                val angle = (-PI / 2.0 + theta).toFloat()
                x = cos(angle) * radius
                y = sin(angle) * radius + radius
                rotDeg = (theta * 180.0 / PI).toFloat()
            } else {
                val angle = (PI / 2.0 - theta).toFloat()
                x = cos(angle) * radius
                y = sin(angle) * radius - radius
                rotDeg = -(theta * 180.0 / PI).toFloat()
            }

            matrix.reset()
            matrix.postRotate(rotDeg)
            matrix.postTranslate(x, y)
            glyphPath.transform(matrix)
            combined.addPath(glyphPath)
        }

        // Recenter the curved contour around (0, 0) so rotation & selection handles sit cleanly on visual center
        val rawBounds = RectF()
        combined.computeBounds(rawBounds, true)
        if (!rawBounds.isEmpty) {
            val centerShift = Matrix().apply {
                setTranslate(-rawBounds.centerX(), -rawBounds.centerY())
            }
            combined.transform(centerShift)
            combined.computeBounds(rawBounds, true)
        }

        val w = rawBounds.width().coerceAtLeast(layer.fontSize)
        val h = rawBounds.height().coerceAtLeast(layer.fontSize)
        return TextGeometry(
            path = combined,
            bounds = rawBounds,
            maxLineWidth = w,
            totalHeight = h
        )
    }
}
