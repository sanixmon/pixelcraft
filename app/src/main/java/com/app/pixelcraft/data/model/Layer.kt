package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LayerType {
    TEXT,
    SHAPE,
    STICKER,
    IMAGE
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
data class Layer(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,

    // Transform
    val positionX: Float = 0f,
    val positionY: Float = 0f,
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
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textAlign: TextAlignment = TextAlignment.CENTER,
    val lineHeightMultiplier: Float = 1.15f,

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

    // 3D Text
    val is3D: Boolean = false,
    val depth3D: Float = 16f,
    val darken3D: Float = 0.4f,
    val angle3D: Float = 30f,

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
    val shapeStrokeColor: Long = 0xFFFFFFFF,
    val shapeStrokeWidth: Float = 0f,
    val shapeCornerRadius: Float = 24f,

    // Sticker / Image Attributes
    val stickerKey: String = "star",
    val imageLocalPath: String? = null
)
