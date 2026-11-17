package com.app.pixelcraft.data.presets

import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.StylePreset

data class CuratedGradient(
    val id: String,
    val name: String,
    val colors: List<Long>,
    val angle: Float = 45f,
    val type: GradientType = GradientType.LINEAR
)

/**
 * Starter templates (defined in [Templates]) plus 20 curated multi-stop background gradients.
 */
object DefaultPresets {

    val curatedGradients: List<CuratedGradient> = listOf(
        CuratedGradient("grad_midnight", "Midnight Ocean", listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364), 45f),
        CuratedGradient("grad_sunset", "Coral Sunset", listOf(0xFFFF416C, 0xFFFF4B2B), 135f),
        CuratedGradient("grad_cyber", "Cyber Neon", listOf(0xFF00F2FE, 0xFF4FACFE, 0xFF7B2FF7), 45f),
        CuratedGradient("grad_aurora", "Northern Aurora", listOf(0xFF0B486B, 0xFF3B8686, 0xFF79BD9A), 120f),
        CuratedGradient("grad_royal", "Royal Velvet", listOf(0xFF141E30, 0xFF243B55), 90f),
        CuratedGradient("grad_gold", "Luxury Amber", listOf(0xFF1E130C, 0xFF9A8478, 0xFFD4AF37), 45f),
        CuratedGradient("grad_emerald", "Emerald Forest", listOf(0xFF134E5E, 0xFF71B280), 60f),
        CuratedGradient("grad_berry", "Electric Berry", listOf(0xFF8E2DE2, 0xFF4A00E0), 45f),
        CuratedGradient("grad_peach", "Warm Peach", listOf(0xFFFFECD2, 0xFFFCB69F), 135f),
        CuratedGradient("grad_mango", "Tropical Mango", listOf(0xFFFF8008, 0xFFFFC837), 30f),
        CuratedGradient("grad_dusk", "Violet Dusk", listOf(0xFF1F1C2C, 0xFF928DAB), 45f),
        CuratedGradient("grad_obsidian", "Obsidian Carbon", listOf(0xFF090A0F, 0xFF1B2735, 0xFF2C3E50), 90f),
        CuratedGradient("grad_flamingo", "Pink Flamingo", listOf(0xFFF953C6, 0xFFB91D73), 120f),
        CuratedGradient("grad_mint", "Arctic Mint", listOf(0xFF00B09B, 0xFF96C93D), 45f),
        CuratedGradient("grad_crimson", "Crimson Cinema", listOf(0xFF200122, 0xFF6F0000), 90f),
        CuratedGradient("grad_sky", "Clear Sky", listOf(0xFF2980B9, 0xFF6DD5FA, 0xFFFFFFFF), 90f),
        CuratedGradient("grad_espresso", "Warm Espresso", listOf(0xFF3E2723, 0xFF1B0000), 135f),
        CuratedGradient("grad_radial_glow", "Studio Spotlight", listOf(0xFF2B354F, 0xFF0B0E17), 0f, GradientType.RADIAL),
        CuratedGradient("grad_radial_sun", "Solar Halo", listOf(0xFFFFD54F, 0xFFF57F17, 0xFF3E2723), 0f, GradientType.RADIAL),
        CuratedGradient("grad_sweep_prism", "Iridescent Prism", listOf(0xFF00E5FF, 0xFF7C4DFF, 0xFFFF4081, 0xFFFFCA28, 0xFF00E5FF), 0f, GradientType.SWEEP)
    )

    /** Starter templates; see [Templates]. The first one is the editor's starting canvas. */
    val presets: List<StylePreset> = Templates.all
}
