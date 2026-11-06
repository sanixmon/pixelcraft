package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LayerType {
    TEXT,
    SHAPE,
    STICKER,
    IMAGE,
    DRAWING
}

@Serializable
enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECTANGLE,
    CIRCLE,
    STAR,
    TRIANGLE,
    HEART,
    HEXAGON,
    SPEECH_BUBBLE,
    ARROW
}

@Serializable
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

@Serializable
data class PointF(
    val x: Float,
    val y: Float
)

@Serializable
data class DrawPath(
    val points: List<PointF> = emptyList(),
    val color: Long = 0xFFFF0055,
    val strokeWidth: Float = 12f,
    val isNeon: Boolean = false
)

@Serializable
data class Layer(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,

    // Transform
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val zIndex: Int = 0,

    // Base dimensions for non-text or bounding boxes
    val width: Float = 300f,
    val height: Float = 200f,

    // Text Attributes
    val text: String = "New Text",
    val fontSize: Float = 42f,
    val textColor: Long = 0xFFFFFFFF,
    val textGradientColors: List<Long>? = null,
    val fontFamilyName: String = "Roboto",
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textAlign: TextAlignment = TextAlignment.CENTER,
    val letterSpacing: Float = 0f,
    val lineHeightMultiplier: Float = 1.15f,
    val curveArc: Float = 0f, // -100f to +100f

    // Text Background
    val textBackgroundColor: Long? = null,
    val textBackgroundPadding: Float = 16f,
    val textBackgroundRadius: Float = 8f,

    // Stroke / Outline
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,

    // Drop Shadow
    val shadowEnabled: Boolean = false,
    val shadowColor: Long = 0xAA000000,
    val shadowRadius: Float = 10f,
    val shadowDx: Float = 4f,
    val shadowDy: Float = 6f,

    // Inner Shadow
    val innerShadowEnabled: Boolean = false,
    val innerShadowColor: Long = 0x88000000,
    val innerShadowRadius: Float = 8f,
    val innerShadowDx: Float = 2f,
    val innerShadowDy: Float = 2f,

    // Emboss
    val embossEnabled: Boolean = false,
    val embossLightAngle: Float = 45f,
    val embossIntensity: Float = 0.5f,

    // 3D Text & 3D Rotate
    val is3D: Boolean = false,
    val depth3D: Float = 16f,
    val darken3D: Float = 0.4f,
    val angle3D: Float = 30f,
    val rotation3DX: Float = 0f,
    val rotation3DY: Float = 0f,

    // 3D Shadow (floor shadow)
    val shadow3DEnabled: Boolean = false,
    val shadow3DColor: Long = 0x66000000,
    val shadow3DScale: Float = 1.0f,
    val shadow3DOpacity: Float = 0.5f,

    // Reflection
    val reflectionEnabled: Boolean = false,
    val reflectionDistance: Float = 4f,
    val reflectionOpacity: Float = 0.35f,

    // Shape Attributes
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val shapeFillColor: Long = 0xFF00B0FF,
    val shapeGradientColors: List<Long>? = null,
    val shapeStrokeColor: Long = 0xFFFFFFFF,
    val shapeStrokeWidth: Float = 0f,
    val shapeCornerRadius: Float = 24f,

    // Sticker / Image Attributes
    val stickerKey: String = "star",
    val imageLocalPath: String? = null,
    val imagePresetName: String? = null,
    val filterBrightness: Float = 0f,
    val filterContrast: Float = 1f,
    val filterSaturation: Float = 1f,
    val filterHue: Float = 0f,
    val colorToErase: Long? = null,
    val eraseTolerance: Float = 0.15f,

    // Drawing paths
    val drawPaths: List<DrawPath> = emptyList()
)
