package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val text: String,
    val author: String = "",
    val category: String = "Motivation"
)

@Serializable
data class StylePreset(
    val id: String,
    val title: String,
    val description: String,
    val canvasConfig: CanvasConfig,
    val initialLayers: List<Layer>
)

enum class BottomNavTab {
    PRESETS,
    TEXT,
    OBJECTS,
    BACKGROUND,
    FILTERS
}

enum class ActiveDialog {
    TEXT_EDIT,
    IMAGE_SIZE,
    EXPORT,
    QUOTES,
    SAVE_PROJECT,
    OPEN_PROJECT,
    ADD_SHAPE,
    ADD_STICKER,
    COLOR_PICKER,
    HELP
}
