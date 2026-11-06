package com.app.pixelcraft

import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.domain.CropRect
import com.app.pixelcraft.domain.ImageFit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageFitTest {

    @Test
    fun fitWithinKeepsAspectAndRespectsTheLimitingSide() {
        // Landscape photo into a square box: width is the limit.
        val wide = ImageFit.fitWithin(4000, 2000, 600f, 600f)
        assertEquals(600f, wide.width, 0.01f)
        assertEquals(300f, wide.height, 0.01f)

        // Portrait photo into a wide box: height is the limit.
        val tall = ImageFit.fitWithin(1000, 3000, 900f, 300f)
        assertEquals(100f, tall.width, 0.01f)
        assertEquals(300f, tall.height, 0.01f)
    }

    @Test
    fun coverCropTrimsTheSidesOfAWiderSource() {
        // 4000x2000 (2:1) onto a 1:1 canvas -> centred 2000x2000 square.
        assertEquals(CropRect(1000, 0, 3000, 2000), ImageFit.coverCrop(4000, 2000, 1080f, 1080f))
    }

    @Test
    fun coverCropTrimsTopAndBottomOfATallerSource() {
        // 1080x1920 story photo onto a 16:9 thumbnail -> centred 1080x608 band.
        val crop = ImageFit.coverCrop(1080, 1920, 1280f, 720f)
        assertEquals(0, crop.left)
        assertEquals(1080, crop.right)
        assertEquals(608, crop.bottom - crop.top)
        assertEquals(656, crop.top)
    }

    @Test
    fun coverCropOfMatchingAspectIsTheWholeImage() {
        assertEquals(CropRect(0, 0, 1280, 720), ImageFit.coverCrop(1280, 720, 1920f, 1080f))
    }

    @Test
    fun sampleSizeBringsTheLongSideUnderTheLimit() {
        assertEquals(1, ImageFit.sampleSize(2048, 1000, 2048))
        assertEquals(2, ImageFit.sampleSize(4000, 3000, 2048))
        assertEquals(8, ImageFit.sampleSize(3000, 12000, 2048)) // 12000/4 = 3000 is still too big
    }

    @Test
    fun imageLayerPathSurvivesProjectSerialization() {
        val layer = Layer(id = "p1", name = "Photo", type = LayerType.IMAGE, imageLocalPath = "/data/images/a.jpg", width = 600f, height = 300f)
        val json = Json { ignoreUnknownKeys = true }
        val restored = json.decodeFromString<Layer>(json.encodeToString(layer))
        assertEquals(layer, restored)
    }
}
