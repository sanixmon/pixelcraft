package com.app.pixelcraft.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WarmLightColorScheme = lightColorScheme(
    primary = PixelCraftColors.Accent,
    onPrimary = Color.White,
    primaryContainer = PixelCraftColors.PrimaryContainer,
    onPrimaryContainer = PixelCraftColors.OnPrimaryContainer,
    inversePrimary = PixelCraftColors.AccentHover,
    secondary = PixelCraftColors.AccentHover,
    onSecondary = Color.White,
    secondaryContainer = PixelCraftColors.SecondaryContainer,
    onSecondaryContainer = PixelCraftColors.OnSecondaryContainer,
    tertiary = PixelCraftColors.Success,
    onTertiary = Color.White,
    tertiaryContainer = PixelCraftColors.TertiaryContainer,
    onTertiaryContainer = PixelCraftColors.OnTertiaryContainer,
    background = PixelCraftColors.Background,
    onBackground = PixelCraftColors.TextPrimary,
    surface = PixelCraftColors.Surface,
    onSurface = PixelCraftColors.TextPrimary,
    surfaceVariant = PixelCraftColors.SurfaceElevated,
    onSurfaceVariant = PixelCraftColors.TextSecondary,
    surfaceTint = PixelCraftColors.Accent,
    inverseSurface = PixelCraftColors.InverseSurface,
    inverseOnSurface = PixelCraftColors.InverseOnSurface,
    error = PixelCraftColors.Danger,
    onError = Color.White,
    errorContainer = PixelCraftColors.ErrorContainer,
    onErrorContainer = PixelCraftColors.OnErrorContainer,
    outline = PixelCraftColors.Border,
    outlineVariant = PixelCraftColors.Border,
    scrim = PixelCraftColors.Scrim,
    surfaceBright = PixelCraftColors.Surface,
    surfaceDim = PixelCraftColors.CanvasStage,
    surfaceContainer = PixelCraftColors.Surface,
    surfaceContainerHigh = PixelCraftColors.Surface,
    surfaceContainerHighest = PixelCraftColors.SurfaceElevated,
    surfaceContainerLow = PixelCraftColors.Background,
    surfaceContainerLowest = PixelCraftColors.Surface
)

val PixelCraftTypography = Typography(
    // Display typography for editorial headers
    displayLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).sp
    ),
    displayMedium = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp,
        letterSpacing = (-0.01).sp
    ),
    // Titles for section headers and card titles
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).sp
    ),
    titleMedium = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 18.sp
    ),
    // Body text for descriptions and content
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    ),
    // Labels for buttons, chips, captions
    labelLarge = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp
    )
)

val PixelCraftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PixelCraftTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WarmLightColorScheme,
        typography = PixelCraftTypography,
        shapes = PixelCraftShapes,
        content = content
    )
}
