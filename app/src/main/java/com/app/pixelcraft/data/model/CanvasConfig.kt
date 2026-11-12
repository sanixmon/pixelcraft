package com.app.pixelcraft.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class AspectRatioPreset(
    val title: String,
    val defaultWidth: Int,
    val defaultHeight: Int,
    val ratio: Float
) {
    SQUARE("Square (1:1)", 1080, 1080, 1.0f),
    INSTAGRAM_PORTRAIT_4_5("Instagram Portrait (4:5)", 1080, 1350, 4f / 5f),
    STORY_REELS("Story / Reels / TikTok (9:16)", 1080, 1920, 9f / 16f),
    WHATSAPP_STATUS("WhatsApp Status (9:16)", 1080, 1920, 9f / 16f),
    YOUTUBE_THUMBNAIL("YouTube Thumbnail (16:9)", 1280, 720, 16f / 9f),
    YOUTUBE_BANNER("YouTube Channel Banner", 2560, 1440, 16f / 9f),
    MARKETPLACE_PRODUCT("Product Listing (1:1 HD)", 1200, 1200, 1.0f),
    PROFILE_PICTURE("Profile Picture", 800, 800, 1.0f),
    FACEBOOK_COVER("Facebook Cover", 1200, 628, 1200f / 628f),
    TWITTER_HEADER("X / Twitter Header (3:1)", 1500, 500, 3.0f),
    POSTER_4_3("Standard (4:3)", 1024, 768, 4f / 3f),
    PORTRAIT_3_4("Portrait (3:4)", 768, 1024, 3f / 4f),
    A4_PRINT("A4 Poster Print", 2480, 3508, 2480f / 3508f),
    CUSTOM("Custom", 1080, 1080, 1.0f)
}

@Serializable
enum class BackgroundType {
    SOLID_COLOR,
    GRADIENT,
    TRANSPARENT,
    CUSTOM_IMAGE
}

@Serializable
enum class GradientType(val label: String) {
    LINEAR("Linear"),
    RADIAL("Radial"),
    SWEEP("Sweep")
}

@Immutable
@Serializable
data class CanvasFilters(
    val vignetteEnabled: Boolean = false,
    val vignetteIntensity: Float = 0.5f,
    val vignetteColor: Long = 0xAA000000,
    val stripesEnabled: Boolean = false,
    val stripesSpacing: Float = 12f,
    val grainEnabled: Boolean = false,
    val grainIntensity: Float = 0.15f
)

@Immutable
@Serializable
data class CanvasConfig(
    val width: Int = 1080,
    val height: Int = 1080,
    val preset: AspectRatioPreset = AspectRatioPreset.SQUARE,
    val backgroundType: BackgroundType = BackgroundType.SOLID_COLOR,
    val backgroundColor: Long = 0xFFFFFFFFL,
    val gradientColors: List<Long> = listOf(0xFF1F1C2C, 0xFF928DAB),
    val gradientAngle: Float = 45f,
    val gradientType: GradientType = GradientType.LINEAR,
    val customImageUri: String? = null,
    val backgroundDim: Float = 0f,
    val backgroundBlur: Float = 0f,
    val filters: CanvasFilters = CanvasFilters()
)
