package com.app.pixelcraft

import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.presets.DefaultPresets
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayerModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testSerializationRoundTrip() {
        val layer = Layer(
            id = "layer_1",
            name = "Title",
            type = LayerType.TEXT,
            text = "PixelCraft Test",
            is3D = true,
            depth3D = 20f,
            strokeWidth = 3f,
            reflectionEnabled = true
        )

        val config = CanvasConfig(
            width = 1280,
            height = 720
        )

        val project = ProjectData(
            id = "proj_1",
            name = "My Test Project",
            timestamp = 123456789L,
            snapshot = EditorSnapshot(
                layers = listOf(layer),
                canvasConfig = config,
                selectedLayerId = layer.id
            )
        )

        val encoded = json.encodeToString(project)
        val decoded = json.decodeFromString<ProjectData>(encoded)

        assertEquals("proj_1", decoded.id)
        assertEquals("My Test Project", decoded.name)
        assertEquals(1, decoded.snapshot.layers.size)

        val decodedLayer = decoded.snapshot.layers.first()
        assertEquals("PixelCraft Test", decodedLayer.text)
        assertTrue(decodedLayer.is3D)
        assertEquals(20f, decodedLayer.depth3D, 0.001f)
        assertTrue(decodedLayer.reflectionEnabled)
    }

    @Test
    fun testDefaultPresets_Valid() {
        assertTrue(DefaultPresets.presets.isNotEmpty())
        for (preset in DefaultPresets.presets) {
            assertNotNull(preset.id)
            assertNotNull(preset.title)
            assertTrue(preset.initialLayers.isNotEmpty())
        }
    }
}
