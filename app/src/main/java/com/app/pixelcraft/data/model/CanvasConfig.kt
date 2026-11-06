package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class AspectRatioPreset(
    val title: String,
    val defaultWidth: Int,
    val defaultHeight: Int,
    val ratio: Float
) {
    SQUARE("Square (1:1)", 1080, 1080, 1.0f),
    PROFILE_PICTURE("Profile Picture", 800, 800, 1.0f),
    YOUTUBE_THUMBNAIL("YouTube Thumbnail (16:9)", 1280, 720, 16f / 9f),
    YOUTUBE_BANNER("YouTube Channel Banner", 2560, 1440, 16f / 9f),
    FACEBOOK_COVER("Facebook Cover", 1200, 628, 1200f / 628f),
    TWITTER_HEADER("Twitter Header", 1500, 500, 3.0f),
    STORY_REELS("Story / Reels (9:16)", 1080, 1920, 9f / 16f),
    POSTER_4_3("Standard (4:3)", 1024, 768, 4f / 3f),
    PORTRAIT_3_4("Portrait (3:4)", 768, 1024, 3f / 4f),
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
data class CanvasFilters(
    val vignetteEnabled: Boolean = false,
    val vignetteIntensity: Float = 0.5f,
    val vignetteColor: Long = 0xAA000000,
    val stripesEnabled: Boolean = false,
    val stripesSpacing: Float = 12f
)

@Serializable
data class CanvasConfig(
    val width: Int = 1080,
    val height: Int = 1080,
    val preset: AspectRatioPreset = AspectRatioPreset.SQUARE,
    val backgroundType: BackgroundType = BackgroundType.SOLID_COLOR,
    val backgroundColor: Long = 0xFF2A2E39,
    val gradientColors: List<Long> = listOf(0xFF1F1C2C, 0xFF928DAB),
    val gradientAngle: Float = 45f,
    val customImageUri: String? = null,
    val filters: CanvasFilters = CanvasFilters()
)
