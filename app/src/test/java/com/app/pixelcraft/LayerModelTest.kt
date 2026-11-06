package com.app.pixelcraft

import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokeCapStyle
import com.app.pixelcraft.data.model.StrokeJoinStyle
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.presets.DefaultPresets
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.ShapePathBuilder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayerModelTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun testSerializationRoundTripWithIvyStrokeAndPicSayShape() {
        val textLayer = Layer(
            id = "layer_1",
            name = "Title",
            type = LayerType.TEXT,
            text = "PixelCraft Test",
            fontKey = "bebas_neue",
            is3D = true,
            depth3D = 20f,
            strokeWidth = 6f,
            strokePosition = StrokePosition.OUTSIDE,
            strokePattern = StrokePattern.DASH_DOT,
            strokeDashLength = 22f,
            strokeDashGap = 12f,
            strokePhase = 5f,
            strokeJoin = StrokeJoinStyle.MITER,
            strokeMiterLimit = 8f,
            strokeCap = StrokeCapStyle.SQUARE,
            reflectionEnabled = true
        )

        val balloonLayer = Layer(
            id = "layer_2",
            name = "Thought Cloud",
            type = LayerType.SHAPE,
            shapeType = ShapeType.THOUGHT_CLOUD,
            flipHorizontal = true,
            flipVertical = false,
            shapeGradientEnabled = true,
            shapeFillColor = 0xFFFFFFFFL,
            shapeGradientEndColor = 0xFF00E5FFL,
            shapeGradientAngle = 135f,
            shapeStrokeWidth = 5f
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
                layers = listOf(textLayer, balloonLayer),
                canvasConfig = config,
                selectedLayerId = balloonLayer.id
            )
        )

        val encoded = json.encodeToString(project)
        val decoded = json.decodeFromString<ProjectData>(encoded)

        assertEquals("proj_1", decoded.id)
        assertEquals("My Test Project", decoded.name)
        assertEquals(2, decoded.snapshot.layers.size)

        val decodedText = decoded.snapshot.layers[0]
        assertEquals("PixelCraft Test", decodedText.text)
        assertEquals("bebas_neue", decodedText.fontKey)
        assertEquals(StrokePosition.OUTSIDE, decodedText.strokePosition)
        assertEquals(StrokePattern.DASH_DOT, decodedText.strokePattern)
        assertEquals(StrokeJoinStyle.MITER, decodedText.strokeJoin)
        assertEquals(StrokeCapStyle.SQUARE, decodedText.strokeCap)
        assertEquals(8f, decodedText.strokeMiterLimit, 0.001f)

        val decodedShape = decoded.snapshot.layers[1]
        assertEquals(ShapeType.THOUGHT_CLOUD, decodedShape.shapeType)
        assertTrue(decodedShape.flipHorizontal)
        assertTrue(decodedShape.shapeGradientEnabled)
        assertEquals(135f, decodedShape.shapeGradientAngle, 0.001f)
    }

    @Test
    fun testAll24PicSayProShapesAndBundledFontsCatalog() {
        assertEquals(24, ShapeType.entries.size)
        for (cat in ShapeCategory.entries) {
            assertTrue(
                "Category $cat must contain shapes",
                ShapeType.entries.any { it.category == cat }
            )
        }
        for (shape in ShapeType.entries) {
            assertTrue(shape.defaultWidth > 0f && shape.defaultHeight > 0f)
            assertNotNull(
                ShapePathBuilder.buildPath(
                    shapeType = shape,
                    width = shape.defaultWidth,
                    height = shape.defaultHeight,
                    flipHorizontal = true,
                    flipVertical = true
                )
            )
        }

        // System Sans + 15 bundled open-source fonts = 16 entries
        assertEquals(16, FontCatalog.fonts.size)
        assertEquals(16, FontCatalog.fonts.map { it.key }.toSet().size)
        val bundledOnly = FontCatalog.fonts.filter { it.key != FontCatalog.DEFAULT_KEY }
        assertEquals(15, bundledOnly.size)
        for (font in bundledOnly) {
            assertNotNull("Bundled font '${font.key}' must have regularResId", font.regularResId)
            assertTrue(font.widthFactor > 0.5f)
        }
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
