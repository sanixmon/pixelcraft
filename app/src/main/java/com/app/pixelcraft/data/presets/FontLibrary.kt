package com.app.pixelcraft.data.presets

import androidx.compose.ui.text.font.FontFamily

data class FontItem(
    val name: String,
    val family: FontFamily,
    val previewText: String = "PixelCraft Text"
)

object FontLibrary {
    val fonts = listOf(
        FontItem("Default / Roboto", FontFamily.Default),
        FontItem("Sans-Serif", FontFamily.SansSerif),
        FontItem("Serif / Elegant", FontFamily.Serif),
        FontItem("Monospace / Code", FontFamily.Monospace),
        FontItem("Cursive / Script", FontFamily.Cursive)
    )

    fun getFontFamily(name: String): FontFamily {
        return fonts.find { it.name == name }?.family ?: FontFamily.Default
    }
}
