package com.app.pixelcraft.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class LayerType {
    TEXT,
    SHAPE,
    IMAGE
}

@Serializable
enum class LayerBlendMode(val label: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    PLUS("Add / Glow")
}

@Serializable
enum class PhotoFilterPreset(val label: String) {
    NONE("Original"),
    BW("B&W"),
    VINTAGE("Vintage"),
    COOL("Cool"),
    WARM("Warm"),
    VIVID("Vivid"),
    NOIR("Noir"),
    FADE("Matte Fade")
}

@Immutable
@Serializable
data class ImageAdjustments(
    val brightness: Float = 0f,     // -0.5f .. 0.5f
    val contrast: Float = 1f,       // 0.5f .. 1.8f
    val saturation: Float = 1f,     // 0f .. 2.0f
    val warmth: Float = 0f,         // -0.4f .. 0.4f
    val filterPreset: PhotoFilterPreset = PhotoFilterPreset.NONE
) {
    val isDefault: Boolean
        get() = brightness == 0f &&
            contrast == 1f &&
            saturation == 1f &&
            warmth == 0f &&
            filterPreset == PhotoFilterPreset.NONE
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

    // Word Balloons
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

@Immutable
@Serializable
data class PointF(
    val x: Float,
    val y: Float
)

@Immutable
@Serializable
data class Layer(
    val id: String,
    val name: String,
    val type: LayerType,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val groupId: String? = null,
    val blendMode: LayerBlendMode = LayerBlendMode.NORMAL,

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
    val textGradientEnabled: Boolean = false,
    val textGradientEndColor: Long = 0xFF00E5FF,
    val textGradientAngle: Float = 0f,
    val textGradientRadial: Boolean = false,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val textAlign: TextAlignment = TextAlignment.CENTER,
    val lineHeightMultiplier: Float = 1.15f,
    val letterSpacing: Float = 0f,
    val curveAngle: Float = 0f,

    // Text Background
    val textBackgroundColor: Long? = null,
    val textBackgroundPadding: Float = 16f,
    val textBackgroundRadius: Float = 8f,

    // Vector Stroke / Outline (shared parameters across Text & Shape)
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

    // Outer Glow
    val glowEnabled: Boolean = false,
    val glowColor: Long = 0xFF00E5FF,
    val glowRadius: Float = 18f,

    // Drop Shadow
    val shadowEnabled: Boolean = false,
    val shadowColor: Long = 0xAA000000,
    val shadowRadius: Float = 10f,
    val shadowDx: Float = 4f,
    val shadowDy: Float = 6f,

    // 3D Extrusion
    val is3D: Boolean = false,
    val depth3D: Float = 16f,
    val darken3D: Float = 0.4f,
    val angle3D: Float = 30f,

    // 3D Floor Shadow
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
    val shapeGradientEnabled: Boolean = false,
    val shapeGradientEndColor: Long = 0xFF651FFF,
    val shapeGradientAngle: Float = 90f,
    val shapeStrokeColor: Long = 0xFFFFFFFF,
    val shapeStrokeWidth: Float = 0f,
    val shapeCornerRadius: Float = 24f,

    // Image Attributes
    val imageLocalPath: String? = null,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val imageAdjustments: ImageAdjustments = ImageAdjustments(),
    val imageMaskShape: ShapeType? = null,
    val imageCornerRadius: Float = 0f,
    val imageBorderWidth: Float = 0f,
    val imageBorderColor: Long = 0xFFFFFFFF
) {
    /** Copies visual styling (colors, fonts, stroke, shadow, glow, 3D, blend) from [source] onto this layer. */
    fun copyVisualStyleFrom(source: Layer): Layer {
        return copy(
            opacity = source.opacity,
            blendMode = source.blendMode,
            fontKey = source.fontKey,
            fontSize = if (type == LayerType.TEXT && source.type == LayerType.TEXT) source.fontSize else fontSize,
            textColor = source.textColor,
            textGradientEnabled = source.textGradientEnabled,
            textGradientEndColor = source.textGradientEndColor,
            textGradientAngle = source.textGradientAngle,
            textGradientRadial = source.textGradientRadial,
            isBold = source.isBold,
            isItalic = source.isItalic,
            isUnderline = source.isUnderline,
            textAlign = source.textAlign,
            lineHeightMultiplier = source.lineHeightMultiplier,
            letterSpacing = source.letterSpacing,
            curveAngle = source.curveAngle,
            textBackgroundColor = source.textBackgroundColor,
            textBackgroundPadding = source.textBackgroundPadding,
            textBackgroundRadius = source.textBackgroundRadius,
            strokeColor = source.strokeColor,
            strokeWidth = source.strokeWidth,
            strokePosition = source.strokePosition,
            strokeJoin = source.strokeJoin,
            strokeCap = source.strokeCap,
            strokePattern = source.strokePattern,
            strokeDashLength = source.strokeDashLength,
            strokeDashGap = source.strokeDashGap,
            strokePhase = source.strokePhase,
            strokeMiterLimit = source.strokeMiterLimit,
            glowEnabled = source.glowEnabled,
            glowColor = source.glowColor,
            glowRadius = source.glowRadius,
            shadowEnabled = source.shadowEnabled,
            shadowColor = source.shadowColor,
            shadowRadius = source.shadowRadius,
            shadowDx = source.shadowDx,
            shadowDy = source.shadowDy,
            is3D = source.is3D,
            depth3D = source.depth3D,
            darken3D = source.darken3D,
            angle3D = source.angle3D,
            shadow3DEnabled = source.shadow3DEnabled,
            shadow3DColor = source.shadow3DColor,
            shadow3DScale = source.shadow3DScale,
            shadow3DOpacity = source.shadow3DOpacity,
            reflectionEnabled = source.reflectionEnabled,
            reflectionDistance = source.reflectionDistance,
            reflectionOpacity = source.reflectionOpacity,
            shapeFillColor = source.shapeFillColor,
            shapeGradientEnabled = source.shapeGradientEnabled,
            shapeGradientEndColor = source.shapeGradientEndColor,
            shapeGradientAngle = source.shapeGradientAngle,
            shapeStrokeColor = source.shapeStrokeColor,
            shapeStrokeWidth = source.shapeStrokeWidth,
            shapeCornerRadius = source.shapeCornerRadius
        )
    }
}

@Immutable
@Serializable
data class SavedLayerStyle(
    val id: String,
    val name: String,
    val styleLayer: Layer
)
