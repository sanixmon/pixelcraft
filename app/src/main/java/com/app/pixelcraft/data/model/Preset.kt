package com.app.pixelcraft.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
enum class PresetCategory(val label: String) {
    QUOTE("Quote"),
    YOUTUBE("YouTube"),
    PROMO("Promo"),
    GREETING("Greeting"),
    MEME("Meme"),
    STORY("Story")
}

@Immutable
@Serializable
data class StylePreset(
    val id: String,
    val title: String,
    val description: String,
    val canvasConfig: CanvasConfig,
    val initialLayers: List<Layer>,
    val category: PresetCategory = PresetCategory.QUOTE
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
    HELP,
    UNSAVED_CHANGES,
    RENAME_LAYER,
    HISTORY_SHEET,
    DATE_FORMAT,
    SAVED_STYLES
}
