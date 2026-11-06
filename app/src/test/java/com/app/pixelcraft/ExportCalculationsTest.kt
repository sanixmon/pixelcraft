package com.app.pixelcraft

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.domain.ExportQuality
import com.app.pixelcraft.domain.ImageExporter
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportCalculationsTest {

    @Test
    fun testQualityMultipliers() {
        val (ultraW, ultraH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.ULTRA.multiplier)
        assertEquals(3840, ultraW)
        assertEquals(2160, ultraH) // Standard 4K UHD!

        val (mediumW, mediumH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.MEDIUM.multiplier)
        assertEquals(1280, mediumW)
        assertEquals(720, mediumH)

        val (lowW, lowH) = ImageExporter.computeTargetDimensions(1280, 720, ExportQuality.LOW.multiplier)
        assertEquals(640, lowW)
        assertEquals(360, lowH)
    }

    @Test
    fun computeTargetDimensionsCapsMaxSidePreservingAspectRatio() {
        // 4096x2048 at 3.0x would be 12288x6144 -> capped at 8192x4096 (2:1 preserved)
        val (cappedW, cappedH) = ImageExporter.computeTargetDimensions(4096, 2048, ExportQuality.ULTRA.multiplier)
        assertEquals(ImageExporter.MAX_EXPORT_DIMENSION, cappedW)
        assertEquals(4096, cappedH)

        // Defensive clamp against zero/negative inputs
        val (minW, minH) = ImageExporter.computeTargetDimensions(0, -10, 1.0f)
        assertEquals(64, minW)
        assertEquals(64, minH)
    }

    @Test
    fun testAspectRatioPresets() {
        assertEquals(1.0f, AspectRatioPreset.SQUARE.ratio, 0.01f)
        assertEquals(16f / 9f, AspectRatioPreset.YOUTUBE_THUMBNAIL.ratio, 0.01f)
        assertEquals(9f / 16f, AspectRatioPreset.STORY_REELS.ratio, 0.01f)
    }
}
