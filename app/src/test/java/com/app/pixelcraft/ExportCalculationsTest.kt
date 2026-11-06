package com.app.pixelcraft

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportQuality
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportCalculationsTest {

    @Test
    fun testQualityMultipliers() {
        val baseW = 1280
        val baseH = 720

        val ultraW = (baseW * ExportQuality.ULTRA.multiplier).toInt()
        val ultraH = (baseH * ExportQuality.ULTRA.multiplier).toInt()

        assertEquals(3840, ultraW)
        assertEquals(2160, ultraH) // Standard 4K UHD!

        val mediumW = (baseW * ExportQuality.MEDIUM.multiplier).toInt()
        val mediumH = (baseH * ExportQuality.MEDIUM.multiplier).toInt()
        assertEquals(1280, mediumW)
        assertEquals(720, mediumH)
    }

    @Test
    fun testAspectRatioPresets() {
        assertEquals(1.0f, AspectRatioPreset.SQUARE.ratio, 0.01f)
        assertEquals(16f / 9f, AspectRatioPreset.YOUTUBE_THUMBNAIL.ratio, 0.01f)
        assertEquals(9f / 16f, AspectRatioPreset.STORY_REELS.ratio, 0.01f)
    }
}
