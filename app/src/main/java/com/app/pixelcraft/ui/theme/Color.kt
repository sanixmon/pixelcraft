package com.app.pixelcraft.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
object PixelCraftColors {
    // Base surfaces: warm paper tones
    val Background = Color(0xFFF7F2EA)        // Cream paper base
    val Surface = Color(0xFFFFFDF8)           // Warm white card surface
    val SurfaceElevated = Color(0xFFEFE7DA)   // Pressed/hover state
    
    // Canvas and panels
    val CanvasStage = Color(0xFFEDE5D8)       // Warm stone canvas workspace stage
    val Panel = Color(0xFFFFFDF8)             // Primary tool panel surface
    val PanelElevated = Color(0xFFF3ECE0)     // Sub-tool inspector & group bar surface
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

    // Containers & Inverses for Material 3
    val PrimaryContainer = Color(0xFFFBEBD0)
    val OnPrimaryContainer = Color(0xFF1C1814)
    val SecondaryContainer = Color(0xFFEFE7DA)
    val OnSecondaryContainer = Color(0xFF1C1814)
    val TertiaryContainer = Color(0xFFE8F5E9)
    val OnTertiaryContainer = Color(0xFF1C1814)
    val ErrorContainer = Color(0xFFFBEAE5)
    val OnErrorContainer = Color(0xFFC25E3F)
    val InverseSurface = Color(0xFF2C251E)
    val InverseOnSurface = Color(0xFFF7F2EA)
    val Scrim = Color(0x331C1814)
}

// Legacy colors (mapped to warm editorial equivalents for compatibility)
val PixelCraftCyan = PixelCraftColors.Accent
val PixelCraftDark = PixelCraftColors.Background
val PixelCraftSurface = PixelCraftColors.Surface
val PixelCraftAccent = PixelCraftColors.AccentGold
