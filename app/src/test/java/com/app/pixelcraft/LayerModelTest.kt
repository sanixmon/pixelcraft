package com.app.pixelcraft

import com.app.pixelcraft.data.model.CURRENT_PROJECT_SCHEMA_VERSION
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.ImageAdjustments
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PhotoFilterPreset
import com.app.pixelcraft.data.model.PresetCategory
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
    fun testSerializationRoundTripWithVectorStrokeShapesAndV2Fields() {
        val textLayer = Layer(
            id = "layer_1",
            name = "Title",
            type = LayerType.TEXT,
            text = "PixelCraft Test",
            fontKey = "bebas_neue",
            letterSpacing = 0.14f,
            curveAngle = 45f,
            textGradientEnabled = true,
            textGradientEndColor = 0xFFFF007FL,
            glowEnabled = true,
            glowColor = 0xFF00E5FFL,
            glowRadius = 24f,
            blendMode = LayerBlendMode.SCREEN,
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

        val photoLayer = Layer(
            id = "layer_3",
            name = "Masked Photo",
            type = LayerType.IMAGE,
            imageLocalPath = "sample.jpg",
            cropLeft = 0.1f,
            cropRight = 0.9f,
            imageAdjustments = ImageAdjustments(
                brightness = 0.12f,
                contrast = 1.2f,
                saturation = 1.15f,
                warmth = 0.08f,
                filterPreset = PhotoFilterPreset.VINTAGE
            ),
            imageMaskShape = ShapeType.HEXAGON,
            imageBorderWidth = 6f
        )

        val config = CanvasConfig(
            width = 1280,
            height = 720,
            gradientType = GradientType.SWEEP,
            backgroundBlur = 12f,
            backgroundDim = 0.35f,
            filters = CanvasFilters(grainEnabled = true, grainIntensity = 0.2f)
        )

        val project = ProjectData(
            id = "proj_1",
            name = "My Test Project",
            timestamp = 123456789L,
            schemaVersion = CURRENT_PROJECT_SCHEMA_VERSION,
            snapshot = EditorSnapshot(
                layers = listOf(textLayer, balloonLayer, photoLayer),
                canvasConfig = config,
                selectedLayerId = balloonLayer.id
            )
        )

        val encoded = json.encodeToString(project)
        val decoded = json.decodeFromString<ProjectData>(encoded)

        assertEquals("proj_1", decoded.id)
        assertEquals("My Test Project", decoded.name)
        assertEquals(CURRENT_PROJECT_SCHEMA_VERSION, decoded.schemaVersion)
        assertEquals(GradientType.SWEEP, decoded.snapshot.canvasConfig.gradientType)
        assertTrue(decoded.snapshot.canvasConfig.filters.grainEnabled)
        assertEquals(3, decoded.snapshot.layers.size)

        val decodedText = decoded.snapshot.layers[0]
        assertEquals("PixelCraft Test", decodedText.text)
        assertEquals("bebas_neue", decodedText.fontKey)
        assertEquals(0.14f, decodedText.letterSpacing, 0.001f)
        assertEquals(45f, decodedText.curveAngle, 0.001f)
        assertTrue(decodedText.glowEnabled)
        assertEquals(LayerBlendMode.SCREEN, decodedText.blendMode)
        assertEquals(StrokePosition.OUTSIDE, decodedText.strokePosition)
        assertEquals(StrokePattern.DASH_DOT, decodedText.strokePattern)
        assertEquals(StrokeJoinStyle.MITER, decodedText.strokeJoin)
        assertEquals(StrokeCapStyle.SQUARE, decodedText.strokeCap)

        val decodedShape = decoded.snapshot.layers[1]
        assertEquals(ShapeType.THOUGHT_CLOUD, decodedShape.shapeType)
        assertTrue(decodedShape.flipHorizontal)
        assertTrue(decodedShape.shapeGradientEnabled)
        assertEquals(135f, decodedShape.shapeGradientAngle, 0.001f)

        val decodedPhoto = decoded.snapshot.layers[2]
        assertEquals(PhotoFilterPreset.VINTAGE, decodedPhoto.imageAdjustments.filterPreset)
        assertEquals(ShapeType.HEXAGON, decodedPhoto.imageMaskShape)
    }

    @Test
    fun testAll24VectorShapesAndBundledFontsCatalog() {
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
    fun testDefaultPresetsCoverAllCategoriesAndValidBundledFonts() {
        val validFontKeys = FontCatalog.fonts.map { it.key }.toSet()
        assertEquals(20, DefaultPresets.presets.size)
        assertEquals(20, DefaultPresets.curatedGradients.size)

        for (cat in PresetCategory.entries) {
            assertTrue(
                "PresetCategory $cat should have at least one starter template",
                DefaultPresets.presets.any { it.category == cat }
            )
        }

        for (preset in DefaultPresets.presets) {
            assertNotNull(preset.id)
            assertNotNull(preset.title)
            assertTrue(preset.initialLayers.isNotEmpty())
            for (layer in preset.initialLayers) {
                if (layer.type == LayerType.TEXT) {
                    assertTrue(
                        "Preset '${preset.id}' uses unregistered fontKey '${layer.fontKey}'",
                        layer.fontKey in validFontKeys
                    )
                }
            }
        }
    }
}
