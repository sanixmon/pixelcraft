package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LayerType {
    TEXT,
    SHAPE,
    IMAGE
}

@Serializable
enum class ShapeCategory(val label: String) {
    BASIC("Basic"),
    BALLOONS("Balloons"),
    ARROWS("Arrows"),
    BADGES("Badges")
}

@Serializable
enum class ShapeType(
    val label: String,
    val category: ShapeCategory,
    val defaultWidth: Float = 320f,
    val defaultHeight: Float = 320f
) {
    // Basic Shapes
    RECTANGLE("Rectangle", ShapeCategory.BASIC, 340f, 240f),
    ROUNDED_RECTANGLE("Rounded Rect", ShapeCategory.BASIC, 340f, 240f),
    CIRCLE("Circle / Oval", ShapeCategory.BASIC, 300f, 300f),
    TRIANGLE("Triangle", ShapeCategory.BASIC, 300f, 280f),
    DIAMOND("Diamond", ShapeCategory.BASIC, 300f, 300f),
    PENTAGON("Pentagon", ShapeCategory.BASIC, 300f, 300f),
    HEXAGON("Hexagon", ShapeCategory.BASIC, 300f, 300f),
    OCTAGON("Octagon", ShapeCategory.BASIC, 300f, 300f),

    // PicSay Pro Word Balloons
    SPEECH_ROUND("Oval Balloon", ShapeCategory.BALLOONS, 380f, 280f),
    SPEECH_BUBBLE("Speech Box", ShapeCategory.BALLOONS, 380f, 280f),
    THOUGHT_CLOUD("Thought Cloud", ShapeCategory.BALLOONS, 400f, 300f),
    SHOUT_BURST("Shout Burst", ShapeCategory.BALLOONS, 420f, 340f),

    // Arrows & Pointers
    ARROW("Block Arrow", ShapeCategory.ARROWS, 360f, 220f),
    ARROW_DOUBLE("Double Arrow", ShapeCategory.ARROWS, 400f, 200f),
    CHEVRON("Chevron", ShapeCategory.ARROWS, 320f, 220f),
    LOCATION_PIN("Callout Pin", ShapeCategory.ARROWS, 260f, 340f),

    // Badges & Symbols
    STAR("5-Point Star", ShapeCategory.BADGES, 300f, 300f),
    STARBURST("Starburst", ShapeCategory.BADGES, 340f, 340f),
    HEART("Heart", ShapeCategory.BADGES, 300f, 280f),
    SHIELD("Shield", ShapeCategory.BADGES, 280f, 340f),
    RIBBON_BANNER("Ribbon", ShapeCategory.BADGES, 440f, 180f),
    CROWN("Crown", ShapeCategory.BADGES, 300f, 220f),
    LIGHTNING("Lightning", ShapeCategory.BADGES, 240f, 360f),
    CROSS("Cross", ShapeCategory.BADGES, 280f, 280f)
}

@Serializable
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT
}

@Serializable
enum class StrokePosition(val label: String) {
    OUTSIDE("Outside"),
    CENTER("Center"),
    INSIDE("Inside")
}

@Serializable
enum class StrokeJoinStyle(val label: String) {
    ROUND("Round"),
    MITER("Miter"),
    BEVEL("Bevel")
}

@Serializable
enum class StrokeCapStyle(val label: String) {
    ROUND("Round"),
    BUTT("Butt"),
    SQUARE("Square")
}

@Serializable
enum class StrokePattern(val label: String) {
    SOLID("Solid"),
    DASHED("Dashed"),
    DOTTED("Dotted"),
    DASH_DOT("Dash-Dot")
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
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,

    // Base dimensions for non-text or bounding boxes
    val width: Float = 300f,
    val height: Float = 200f,

    // Text Attributes
    val text: String = "New Text",
    val fontKey: String = "default",
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

    // Ivy Draw Vector Stroke / Outline (shared parameters across Text & Shape)
    val strokeColor: Long = 0xFF000000,
    val strokeWidth: Float = 0f,
    val strokePosition: StrokePosition = StrokePosition.OUTSIDE,
    val strokeJoin: StrokeJoinStyle = StrokeJoinStyle.ROUND,
    val strokeCap: StrokeCapStyle = StrokeCapStyle.ROUND,
    val strokePattern: StrokePattern = StrokePattern.SOLID,
    val strokeDashLength: Float = 16f,
    val strokeDashGap: Float = 10f,
    val strokePhase: Float = 0f,
    val strokeMiterLimit: Float = 4f,

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

    // PicSay Pro Shape Attributes
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val shapeFillColor: Long = 0xFF00B0FF,
    val shapeGradientEnabled: Boolean = false,
    val shapeGradientEndColor: Long = 0xFF651FFF,
    val shapeGradientAngle: Float = 90f,
    val shapeStrokeColor: Long = 0xFFFFFFFF,
    val shapeStrokeWidth: Float = 0f,
    val shapeCornerRadius: Float = 24f,

    // Image Attributes
    val imageLocalPath: String? = null
)
