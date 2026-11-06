package com.app.pixelcraft.data.presets

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.domain.TextSizing

object DefaultPresets {
    val presets = listOf(
        StylePreset(
            id = "preset_default",
            title = "Default",
            description = "Classic PixelCraft starting canvas",
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF2A2E39
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_default_text",
                        name = "New Text",
                        type = LayerType.TEXT,
                        text = "New Text",
                        fontKey = "poppins",
                        fontSize = 48f,
                        textColor = 0xFFFFFFFF,
                        positionX = 0f,
                        positionY = 0f,
                        zIndex = 0,
                        textAlign = TextAlignment.CENTER
                    )
                )
            )
        ),
        StylePreset(
            id = "preset_3d_bold",
            title = "3D Bold",
            description = "High extrusion 3D typography",
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
                gradientAngle = 45f
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_3d_text",
                        name = "3D Title",
                        type = LayerType.TEXT,
                        text = "PIXELCRAFT 3D",
                        fontKey = "archivo_black",
                        fontSize = 58f,
                        textColor = 0xFFFFD700,
                        isBold = true,
                        zIndex = 0,
                        is3D = true,
                        depth3D = 24f,
                        darken3D = 0.5f,
                        angle3D = 40f,
                        strokeColor = 0xFF8B6508,
                        strokeWidth = 3f,
                        strokePosition = StrokePosition.OUTSIDE,
                        shadow3DEnabled = true,
                        shadow3DColor = 0xAA000000,
                        shadowEnabled = true,
                        shadowColor = 0x88000000,
                        shadowRadius = 15f
                    )
                )
            )
        ),
        StylePreset(
            id = "preset_neon",
            title = "Neon Glow",
            description = "Vibrant cyberpunk neon lights",
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF0A0A12,
                filters = CanvasFilters(vignetteEnabled = true, vignetteIntensity = 0.7f)
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_neon_box",
                    name = "Neon Border",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 520f,
                    height = 240f,
                    zIndex = 0,
                    shapeFillColor = 0x00000000,
                    shapeStrokeColor = 0xFFFF007F,
                    shapeStrokeWidth = 6f,
                    shapeCornerRadius = 32f,
                    shadowEnabled = true,
                    shadowColor = 0xFFFF007F,
                    shadowRadius = 24f
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_neon_text",
                        name = "Neon Text",
                        type = LayerType.TEXT,
                        text = "CYBER GLOW",
                        fontKey = "russo_one",
                        fontSize = 46f,
                        textColor = 0xFF00F0FF,
                        isBold = true,
                        zIndex = 1,
                        shadowEnabled = true,
                        shadowColor = 0xFF00F0FF,
                        shadowRadius = 28f,
                        strokeColor = 0xFFFFFFFF,
                        strokeWidth = 2f,
                        strokePosition = StrokePosition.OUTSIDE
                    )
                )
            )
        ),
        StylePreset(
            id = "preset_youtube",
            title = "YouTube Thumbnail",
            description = "High-CTR 16:9 thumbnail template",
            canvasConfig = CanvasConfig(
                width = 1280,
                height = 720,
                preset = AspectRatioPreset.YOUTUBE_THUMBNAIL,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFFFF416C, 0xFFFF4B2B),
                gradientAngle = 90f
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_yt_badge",
                    name = "Highlight Badge",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 540f,
                    height = 110f,
                    positionX = 0f,
                    positionY = -60f,
                    zIndex = 0,
                    shapeFillColor = 0xFF000000,
                    shapeStrokeColor = 0xFFFFD700,
                    shapeStrokeWidth = 4f,
                    shapeCornerRadius = 16f
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_title",
                        name = "Thumbnail Title",
                        type = LayerType.TEXT,
                        text = "100% VIRAL!",
                        fontKey = "anton",
                        fontSize = 56f,
                        textColor = 0xFFFFEB3B,
                        isBold = true,
                        positionX = 0f,
                        positionY = -60f,
                        zIndex = 1,
                        strokeColor = 0xFF000000,
                        strokeWidth = 5f,
                        strokePosition = StrokePosition.OUTSIDE,
                        shadowEnabled = true,
                        shadowRadius = 12f
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_sub",
                        name = "Subtitle",
                        type = LayerType.TEXT,
                        text = "WATCH BEFORE IT'S DELETED",
                        fontKey = "bebas_neue",
                        fontSize = 40f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        positionX = 0f,
                        positionY = 50f,
                        zIndex = 2,
                        strokeColor = 0xFF000000,
                        strokeWidth = 4f,
                        strokePosition = StrokePosition.OUTSIDE
                    )
                )
            )
        ),
        StylePreset(
            id = "preset_keep_calm",
            title = "Keep Calm",
            description = "Iconic poster layout",
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFFC0392B
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_crown",
                    name = "Crown",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.CROWN,
                    width = 160f,
                    height = 115f,
                    shapeFillColor = 0xFFFFFFFF,
                    positionX = 0f,
                    positionY = -150f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_calm_text",
                        name = "Poster Text",
                        type = LayerType.TEXT,
                        text = "KEEP CALM\nAND\nCRAFT ON",
                        fontKey = "bebas_neue",
                        fontSize = 58f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        lineHeightMultiplier = 1.25f,
                        positionX = 0f,
                        positionY = 60f,
                        zIndex = 1
                    )
                )
            )
        ),
        StylePreset(
            id = "preset_reflection",
            title = "Reflect & Glow",
            description = "Studio floor mirror reflection",
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF141E30, 0xFF243B55),
                gradientAngle = 90f
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_reflect_text",
                        name = "Reflection Text",
                        type = LayerType.TEXT,
                        text = "REFLECT",
                        fontKey = "russo_one",
                        fontSize = 62f,
                        textColor = 0xFFE0E0E0,
                        isBold = true,
                        positionX = 0f,
                        positionY = -40f,
                        zIndex = 0,
                        reflectionEnabled = true,
                        reflectionDistance = 8f,
                        reflectionOpacity = 0.4f,
                        shadowEnabled = true,
                        shadowColor = 0x99000000,
                        shadowRadius = 14f
                    )
                )
            )
        )
    )
}
