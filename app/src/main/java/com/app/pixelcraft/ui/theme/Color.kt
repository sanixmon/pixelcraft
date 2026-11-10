package com.app.pixelcraft.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Legacy colors (deprecated, kept for backward compatibility)
val PixelCraftCyan = Color(0xFF00E5FF)
val PixelCraftDark = Color(0xFF14161D)
val PixelCraftSurface = Color(0xFF1E222B)
val PixelCraftAccent = Color(0xFFFFD700)

/**
 * Warm editorial palette: paper base, amber accents, editorial confidence.
 * Inspired by design studios, craft publications, and tactile print materials.
 */
@Immutable
object PixelCraftColors {
    // Base surfaces: warm paper tones
    val Background = Color(0xFFF7F2EA)        // Cream paper base
    val Surface = Color(0xFFFFFDF8)           // Warm white card surface
    val SurfaceElevated = Color(0xFFEFE7DA)   // Pressed/hover state
    
    // Canvas and panels (for editor, will update later)
    val CanvasStage = Color(0xFFF7F2EA)       // Match background for now
    val Panel = Color(0xFFEFE7DA)
    val PanelElevated = Color(0xFFE5DDD0)
    val DialogSurface = Color(0xFFFFFDF8)
    val CardSurface = Color(0xFFFFFDF8)
    
    // Borders and dividers
    val Border = Color(0xFFE2D8C8)            // Hairline warm gray
    val Hairline = Color(0xFFE2D8C8)
    
    // Accent colors: amber and warm accents
    val Accent = Color(0xFFC8871A)            // Rich amber primary
    val AccentHover = Color(0xFFA66A2E)       // Caramel deep hover
    val AccentGold = Color(0xFFC8871A)        // Unified with primary
    val Terracotta = Color(0xFFC25E3F)        // Warm terracotta for badges
    
    // Status colors
    val Success = Color(0xFF2E7D32)           // Warm forest green
    val Warning = Color(0xFFC8871A)           // Use amber for warnings
    val Danger = Color(0xFFC25E3F)            // Terracotta for danger
    
    // Text colors
    val TextPrimary = Color(0xFF1C1814)       // Warm black ink
    val TextSecondary = Color(0xFF6B6259)     // Warm gray muted
    val TextMuted = Color(0xFF9B8F82)         // Lighter muted
    
    // Editor-specific (keep for now, will update with editor)
    val OutlineSelected = Color(0xFFC8871A)
    val OutlineLocked = Color(0xFFC25E3F)
    val SmartGuide = Color(0xFFC8871A)
    val GridLine = Color(0x66C8871A)
}
