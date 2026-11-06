package com.app.pixelcraft

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.domain.TextSizing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportCalculationsTest {

    @Test
    fun exportQualityMultipliersAreAllUniqueWithoutDuplicates() {
        // Bug 2.14 regression: every ExportQuality entry must have a distinct multiplier
        val multipliers = ExportQuality.entries.map { it.multiplier }
        assertEquals(multipliers.size, multipliers.toSet().size)

        val (ultraW, ultraH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.ULTRA.multiplier)
        assertEquals(3840, ultraW)
        assertEquals(2160, ultraH)

        val (mediumW, mediumH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.MEDIUM.multiplier)
        assertEquals(1280, mediumW)
        assertEquals(720, mediumH)

        val (lowW, lowH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.LOW.multiplier)
        assertEquals(640, lowW)
        assertEquals(360, lowH)
    }

    @Test
    fun computeTargetDimensionsRespectsMaxSideAndDynamicMemoryBudget() {
        val (cappedW, cappedH) = ImageExporter.computeTargetDimensions(4096, 2048, ExportQuality.ULTRA.multiplier)
        assertEquals(ImageExporter.MAX_EXPORT_DIMENSION, cappedW)
        assertEquals(4096, cappedH)

        // Bug 2.6: on a constrained 128 MB JVM heap, safeMaxPixelsForHeap caps total pixels to <= 8M px (~32 MB)
        val constrainedHeap = 128L * 1024L * 1024L
        val pixelCap = ImageExporter.safeMaxPixelsForHeap(constrainedHeap)
        assertEquals(8L * 1024L * 1024L, pixelCap)

        val estimate = ImageExporter.estimateDimensions(
            baseWidth = 4096,
            baseHeight = 4096,
            quality = ExportQuality.ULTRA,
            maxHeapBytes = constrainedHeap
        )
        assertTrue(estimate.cappedByMemory)
        assertTrue(estimate.safeWidth.toLong() * estimate.safeHeight.toLong() <= pixelCap + 4096L)
        assertEquals(estimate.safeWidth, estimate.safeHeight) // 1:1 aspect ratio preserved

        val (minW, minH) = ImageExporter.computeTargetDimensions(0, -10, 1.0f)
        assertEquals(64, minW)
        assertEquals(64, minH)
    }

    @Test
    fun exportFormatsAndTextCaseTransformBehaveDeterministically() {
        assertTrue(ExportFormat.PNG.supportsTransparency)
        assertTrue(ExportFormat.WEBP_LOSSY.supportsTransparency)
        assertTrue(ExportFormat.WEBP_LOSSLESS.supportsTransparency)
        assertFalse(ExportFormat.JPEG.supportsTransparency)

        assertEquals("HELLO WORLD", TextSizing.transformCase("hello world", TextCaseMode.UPPERCASE))
        assertEquals("hello world", TextSizing.transformCase("HELLO WORLD", TextCaseMode.LOWERCASE))
        assertEquals("Pixel Craft Studio\nLine Two", TextSizing.transformCase("pIXEL cRAFT sTUDIO\nline two", TextCaseMode.TITLE_CASE))

        assertEquals(1.0f, AspectRatioPreset.SQUARE.ratio, 0.01f)
        assertEquals(16f / 9f, AspectRatioPreset.YOUTUBE_THUMBNAIL.ratio, 0.01f)
        assertEquals(9f / 16f, AspectRatioPreset.STORY_REELS.ratio, 0.01f)
    }
}
