package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

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
    SAVE_PROJECT,
    ADD_SHAPE,
    HELP
}
