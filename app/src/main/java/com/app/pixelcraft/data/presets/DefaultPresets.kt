package com.app.pixelcraft.data.presets

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.CanvasFilters
import com.app.pixelcraft.data.model.GradientType
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PresetCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.data.model.StrokePattern
import com.app.pixelcraft.data.model.StrokePosition
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.domain.TextSizing

data class CuratedGradient(
    val id: String,
    val name: String,
    val colors: List<Long>,
    val angle: Float = 45f,
    val type: GradientType = GradientType.LINEAR
)

/**
 * 20 categorized templates across Quote, YouTube, Promo, Greeting, Meme, and Story (§ 6.6)
 * plus 20 curated multi-stop background gradients.
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

    val presets: List<StylePreset> = listOf(
        // 1. Default Starter (Quote)
        StylePreset(
            id = "preset_default",
            title = "Clean Studio",
            description = "Balanced modern canvas ready for custom design",
            category = PresetCategory.QUOTE,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFFFFFFFFL
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_default_text",
                        name = "New Text",
                        type = LayerType.TEXT,
                        text = "New Text",
                        fontKey = "poppins",
                        fontSize = 48f,
                        textColor = 0xFF1C1814L,
                        positionX = 0f,
                        positionY = 0f,
                        zIndex = 0,
                        textAlign = TextAlignment.CENTER
                    )
                )
            )
        ),

        // 2. 3D Bold (Promo)
        StylePreset(
            id = "preset_3d_bold",
            title = "3D Gold Extrude",
            description = "High-depth 3D metallic headline",
            category = PresetCategory.PROMO,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
                gradientAngle = 45f
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_3d_text",
                        name = "3D Title",
                        type = LayerType.TEXT,
                        text = "PIXELCRAFT 3D",
                        fontKey = "archivo_black",
                        fontSize = 58f,
                        textColor = 0xFFFFD700,
                        textGradientEnabled = true,
                        textGradientEndColor = 0xFFFFA000,
                        textGradientAngle = 90f,
                        isBold = true,
                        zIndex = 0,
                        is3D = true,
                        depth3D = 24f,
                        darken3D = 0.5f,
                        angle3D = 40f,
                        strokeColor = 0xFF8B6508,
                        strokeWidth = 3f,
                        strokePosition = StrokePosition.OUTSIDE,
                        shadow3DEnabled = true,
                        shadow3DColor = 0xAA000000,
                        shadowEnabled = true,
                        shadowColor = 0x88000000,
                        shadowRadius = 15f
                    )
                )
            )
        ),

        // 3. Neon Glow (Promo)
        StylePreset(
            id = "preset_neon",
            title = "Neon Glow",
            description = "Vibrant cyberpunk neon sign with outer halo",
            category = PresetCategory.PROMO,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF0A0A12,
                filters = CanvasFilters(vignetteEnabled = true, vignetteIntensity = 0.7f)
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_neon_box",
                    name = "Neon Frame",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 560f,
                    height = 250f,
                    zIndex = 0,
                    shapeFillColor = 0x00000000,
                    shapeStrokeColor = 0xFFFF007F,
                    shapeStrokeWidth = 6f,
                    shapeCornerRadius = 32f,
                    glowEnabled = true,
                    glowColor = 0xFFFF007F,
                    glowRadius = 26f
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_neon_text",
                        name = "Neon Text",
                        type = LayerType.TEXT,
                        text = "CYBER GLOW",
                        fontKey = "russo_one",
                        fontSize = 48f,
                        textColor = 0xFF00F0FF,
                        isBold = true,
                        zIndex = 1,
                        glowEnabled = true,
                        glowColor = 0xFF00F0FF,
                        glowRadius = 28f,
                        strokeColor = 0xFFFFFFFF,
                        strokeWidth = 2f,
                        strokePosition = StrokePosition.OUTSIDE
                    )
                )
            )
        ),

        // 4. YouTube Viral Thumbnail (YouTube)
        StylePreset(
            id = "preset_youtube",
            title = "YouTube Viral",
            description = "High-CTR 16:9 thumbnail layout",
            category = PresetCategory.YOUTUBE,
            canvasConfig = CanvasConfig(
                width = 1280,
                height = 720,
                preset = AspectRatioPreset.YOUTUBE_THUMBNAIL,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFFFF416C, 0xFFFF4B2B),
                gradientAngle = 90f
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_yt_badge",
                    name = "Highlight Badge",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 540f,
                    height = 110f,
                    positionX = 0f,
                    positionY = -60f,
                    zIndex = 0,
                    shapeFillColor = 0xFF000000,
                    shapeStrokeColor = 0xFFFFD700,
                    shapeStrokeWidth = 4f,
                    shapeCornerRadius = 16f
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_title",
                        name = "Thumbnail Title",
                        type = LayerType.TEXT,
                        text = "100% VIRAL!",
                        fontKey = "anton",
                        fontSize = 56f,
                        textColor = 0xFFFFEB3B,
                        isBold = true,
                        positionX = 0f,
                        positionY = -60f,
                        zIndex = 1,
                        strokeColor = 0xFF000000,
                        strokeWidth = 5f,
                        strokePosition = StrokePosition.OUTSIDE,
                        shadowEnabled = true,
                        shadowRadius = 12f
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_sub",
                        name = "Subtitle",
                        type = LayerType.TEXT,
                        text = "WATCH BEFORE IT'S DELETED",
                        fontKey = "bebas_neue",
                        fontSize = 40f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        positionX = 0f,
                        positionY = 50f,
                        zIndex = 2,
                        strokeColor = 0xFF000000,
                        strokeWidth = 4f,
                        strokePosition = StrokePosition.OUTSIDE
                    )
                )
            )
        ),

        // 5. Keep Calm Poster (Quote)
        StylePreset(
            id = "preset_keep_calm",
            title = "Keep Calm",
            description = "Iconic heritage poster typography",
            category = PresetCategory.QUOTE,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFFC0392B
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_crown",
                    name = "Crown",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.CROWN,
                    width = 160f,
                    height = 115f,
                    shapeFillColor = 0xFFFFFFFF,
                    positionX = 0f,
                    positionY = -150f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_calm_text",
                        name = "Poster Text",
                        type = LayerType.TEXT,
                        text = "KEEP CALM\nAND\nCRAFT ON",
                        fontKey = "bebas_neue",
                        fontSize = 58f,
                        textColor = 0xFFFFFFFF,
                        isBold = true,
                        lineHeightMultiplier = 1.25f,
                        positionX = 0f,
                        positionY = 60f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 6. Studio Reflection (Quote)
        StylePreset(
            id = "preset_reflection",
            title = "Studio Reflect",
            description = "Floor mirror reflection with spotlight backdrop",
            category = PresetCategory.QUOTE,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF141E30, 0xFF243B55),
                gradientAngle = 90f
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_reflect_text",
                        name = "Reflection Text",
                        type = LayerType.TEXT,
                        text = "REFLECT",
                        fontKey = "russo_one",
                        fontSize = 62f,
                        textColor = 0xFFE0E0E0,
                        isBold = true,
                        positionX = 0f,
                        positionY = -40f,
                        zIndex = 0,
                        reflectionEnabled = true,
                        reflectionDistance = 8f,
                        reflectionOpacity = 0.4f,
                        shadowEnabled = true,
                        shadowColor = 0x99000000,
                        shadowRadius = 14f
                    )
                )
            )
        ),

        // 7. Editorial Serif Quote (Quote)
        StylePreset(
            id = "preset_editorial_quote",
            title = "Editorial Quote",
            description = "Minimalist warm serif quote for Instagram 4:5",
            category = PresetCategory.QUOTE,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1350,
                preset = AspectRatioPreset.INSTAGRAM_PORTRAIT_4_5,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFFF5F0E6,
                filters = CanvasFilters(grainEnabled = true, grainIntensity = 0.14f)
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_eq_body",
                        name = "Quote Body",
                        type = LayerType.TEXT,
                        text = "\"Simplicity is the\nultimate sophistication.\"",
                        fontKey = "pt_serif",
                        fontSize = 54f,
                        isItalic = true,
                        textColor = 0xFF1E1E1E,
                        lineHeightMultiplier = 1.3f,
                        positionY = -40f,
                        zIndex = 0
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_eq_author",
                        name = "Author",
                        type = LayerType.TEXT,
                        text = "— LEONARDO DA VINCI",
                        fontKey = "poppins",
                        fontSize = 24f,
                        isBold = true,
                        letterSpacing = 0.18f,
                        textColor = 0xFF7A6F5D,
                        positionY = 140f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 8. Midnight Stoic (Quote)
        StylePreset(
            id = "preset_midnight_stoic",
            title = "Midnight Motivation",
            description = "High-contrast dark typographic quote",
            category = PresetCategory.QUOTE,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF1F2937, 0xFF090B10),
                gradientType = GradientType.RADIAL
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_stoic_top",
                        name = "Lead Line",
                        type = LayerType.TEXT,
                        text = "DISCIPLINE",
                        fontKey = "bebas_neue",
                        fontSize = 84f,
                        isBold = true,
                        letterSpacing = 0.12f,
                        textColor = 0xFFFFFFFF,
                        positionY = -50f,
                        zIndex = 0
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_stoic_sub",
                        name = "Sub Line",
                        type = LayerType.TEXT,
                        text = "BEATS MOTIVATION EVERY DAY",
                        fontKey = "poppins",
                        fontSize = 28f,
                        letterSpacing = 0.15f,
                        textColor = 0xFF38BDF8,
                        positionY = 55f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 9. Gaming Versus Thumbnail (YouTube)
        StylePreset(
            id = "preset_yt_gaming",
            title = "Gaming Showdown",
            description = "Bold angled YouTube gaming header with lightning badge",
            category = PresetCategory.YOUTUBE,
            canvasConfig = CanvasConfig(
                width = 1280,
                height = 720,
                preset = AspectRatioPreset.YOUTUBE_THUMBNAIL,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF1A0B2E, 0xFF4A00E0, 0xFF00D2FF),
                gradientAngle = 35f,
                filters = CanvasFilters(vignetteEnabled = true, vignetteIntensity = 0.55f)
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_yt_bolt",
                    name = "Lightning",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.LIGHTNING,
                    width = 180f,
                    height = 280f,
                    shapeFillColor = 0xFFFFEA00,
                    shapeStrokeColor = 0xFF000000,
                    shapeStrokeWidth = 6f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_pro",
                        name = "Left Title",
                        type = LayerType.TEXT,
                        text = "PRO",
                        fontKey = "bangers",
                        fontSize = 110f,
                        textColor = 0xFF00E5FF,
                        strokeColor = 0xFF000000,
                        strokeWidth = 8f,
                        positionX = -300f,
                        rotation = -6f,
                        is3D = true,
                        depth3D = 14f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_yt_noob",
                        name = "Right Title",
                        type = LayerType.TEXT,
                        text = "LEGEND",
                        fontKey = "bangers",
                        fontSize = 110f,
                        textColor = 0xFFFF3D71,
                        strokeColor = 0xFF000000,
                        strokeWidth = 8f,
                        positionX = 310f,
                        rotation = 6f,
                        is3D = true,
                        depth3D = 14f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 10. Tech Tutorial Thumbnail (YouTube)
        StylePreset(
            id = "preset_yt_tutorial",
            title = "Tech Review",
            description = "Crisp modern tutorial & review 16:9 card",
            category = PresetCategory.YOUTUBE,
            canvasConfig = CanvasConfig(
                width = 1280,
                height = 720,
                preset = AspectRatioPreset.YOUTUBE_THUMBNAIL,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF0F172A, 0xFF1E293B),
                gradientAngle = 120f
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_tut_pill",
                    name = "Tag Pill",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 280f,
                    height = 68f,
                    shapeFillColor = 0xFF10B981,
                    shapeCornerRadius = 34f,
                    positionX = -380f,
                    positionY = -140f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_tut_tag",
                        name = "Tag Text",
                        type = LayerType.TEXT,
                        text = "FULL GUIDE",
                        fontKey = "poppins",
                        fontSize = 28f,
                        isBold = true,
                        textColor = 0xFF052E16,
                        positionX = -380f,
                        positionY = -140f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_tut_main",
                        name = "Headline",
                        type = LayerType.TEXT,
                        text = "MASTER DESIGN\nIN 10 MINUTES",
                        fontKey = "archivo_black",
                        fontSize = 68f,
                        textAlign = TextAlignment.LEFT,
                        textColor = 0xFFFFFFFF,
                        positionX = -160f,
                        positionY = 20f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 11. Flash Sale Promo (Promo)
        StylePreset(
            id = "preset_flash_sale",
            title = "Flash Sale 50%",
            description = "Retail & marketplace discount announcement",
            category = PresetCategory.PROMO,
            canvasConfig = CanvasConfig(
                width = 1200,
                height = 1200,
                preset = AspectRatioPreset.MARKETPLACE_PRODUCT,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFFDC2626, 0xFF7F1D1D),
                gradientType = GradientType.RADIAL
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_sale_burst",
                    name = "Starburst",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.STARBURST,
                    width = 620f,
                    height = 620f,
                    shapeFillColor = 0xFFFACC15,
                    shapeStrokeColor = 0xFFFFFFFF,
                    shapeStrokeWidth = 8f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_sale_top",
                        name = "Flash Sale",
                        type = LayerType.TEXT,
                        text = "FLASH SALE",
                        fontKey = "anton",
                        fontSize = 64f,
                        textColor = 0xFF991B1B,
                        positionY = -90f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_sale_pct",
                        name = "50% OFF",
                        type = LayerType.TEXT,
                        text = "50% OFF",
                        fontKey = "archivo_black",
                        fontSize = 92f,
                        textColor = 0xFF111827,
                        strokeColor = 0xFFFFFFFF,
                        strokeWidth = 6f,
                        positionY = 25f,
                        zIndex = 2
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_sale_cta",
                        name = "CTA",
                        type = LayerType.TEXT,
                        text = "LIMITED TIME ONLY",
                        fontKey = "poppins",
                        fontSize = 30f,
                        isBold = true,
                        letterSpacing = 0.16f,
                        textColor = 0xFFFFFFFF,
                        positionY = 410f,
                        zIndex = 3
                    )
                )
            )
        ),

        // 12. Curved Crest Emblem (Promo)
        StylePreset(
            id = "preset_curved_badge",
            title = "Curved Crest",
            description = "Circular arc badge for cafe, apparel, or studio logos",
            category = PresetCategory.PROMO,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF18181B
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_crest_ring",
                    name = "Badge Ring",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.CIRCLE,
                    width = 620f,
                    height = 620f,
                    shapeFillColor = 0x00000000,
                    shapeStrokeColor = 0xFFF59E0B,
                    shapeStrokeWidth = 6f,
                    strokePattern = StrokePattern.DASHED,
                    strokeDashLength = 20f,
                    strokeDashGap = 14f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_crest_top",
                        name = "Top Arc",
                        type = LayerType.TEXT,
                        text = "HANDCRAFTED QUALITY",
                        fontKey = "bebas_neue",
                        fontSize = 46f,
                        isBold = true,
                        letterSpacing = 0.12f,
                        curveAngle = 115f,
                        textColor = 0xFFFDE68A,
                        positionY = -140f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_crest_center",
                        name = "Center Mark",
                        type = LayerType.TEXT,
                        text = "EST. 2026",
                        fontKey = "abril_fatface",
                        fontSize = 54f,
                        isBold = true,
                        textColor = 0xFFFFFFFF,
                        positionY = 10f,
                        zIndex = 2
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_crest_bottom",
                        name = "Bottom Arc",
                        type = LayerType.TEXT,
                        text = "ORIGINAL STUDIO",
                        fontKey = "bebas_neue",
                        fontSize = 42f,
                        letterSpacing = 0.14f,
                        curveAngle = -100f,
                        textColor = 0xFFF59E0B,
                        positionY = 155f,
                        zIndex = 3
                    )
                )
            )
        ),

        // 13. Happy Birthday Card (Greeting)
        StylePreset(
            id = "preset_birthday",
            title = "Birthday Wish",
            description = "Warm celebratory greeting card with script lettering",
            category = PresetCategory.GREETING,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1350,
                preset = AspectRatioPreset.INSTAGRAM_PORTRAIT_4_5,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFFFFF1EB, 0xFFACE0F9),
                gradientAngle = 135f
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_bday_frame",
                    name = "Card Border",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 920f,
                    height = 1180f,
                    shapeFillColor = 0xCCFFFFFF,
                    shapeStrokeColor = 0xFFF472B6,
                    shapeStrokeWidth = 5f,
                    shapeCornerRadius = 40f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_bday_script",
                        name = "Happy Birthday",
                        type = LayerType.TEXT,
                        text = "Happy\nBirthday!",
                        fontKey = "great_vibes",
                        fontSize = 118f,
                        textColor = 0xFFDB2777,
                        lineHeightMultiplier = 1.05f,
                        positionY = -70f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_bday_sub",
                        name = "Wish Text",
                        type = LayerType.TEXT,
                        text = "WISHING YOU JOY, HEALTH & MAGIC",
                        fontKey = "poppins",
                        fontSize = 26f,
                        isBold = true,
                        letterSpacing = 0.10f,
                        textColor = 0xFF334155,
                        positionY = 180f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 14. Eid / Festive Emerald Greeting (Greeting)
        StylePreset(
            id = "preset_festive_gold",
            title = "Festive Gold",
            description = "Elegant emerald & gold festive greeting card",
            category = PresetCategory.GREETING,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF064E3B, 0xFF022C22),
                gradientType = GradientType.RADIAL
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_festive_diamond",
                    name = "Gold Ornament",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.OCTAGON,
                    width = 760f,
                    height = 760f,
                    shapeFillColor = 0x00000000,
                    shapeStrokeColor = 0xFFD4AF37,
                    shapeStrokeWidth = 5f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_festive_title",
                        name = "Festive Title",
                        type = LayerType.TEXT,
                        text = "WARM WISHES\n& BLESSINGS",
                        fontKey = "abril_fatface",
                        fontSize = 58f,
                        isBold = true,
                        textColor = 0xFFFDE68A,
                        textGradientEnabled = true,
                        textGradientEndColor = 0xFFD97706,
                        textGradientAngle = 90f,
                        positionY = -20f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_festive_from",
                        name = "From Family",
                        type = LayerType.TEXT,
                        text = "FROM OUR FAMILY TO YOURS",
                        fontKey = "poppins",
                        fontSize = 24f,
                        letterSpacing = 0.16f,
                        textColor = 0xFFA7F3D0,
                        positionY = 140f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 15. Thank You Card (Greeting)
        StylePreset(
            id = "preset_thank_you",
            title = "Thank You",
            description = "Sweet pastel appreciation card for customers or friends",
            category = PresetCategory.GREETING,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFFFFECD2, 0xFFFCB69F),
                gradientAngle = 45f
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_ty_heart",
                    name = "Heart",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.HEART,
                    width = 180f,
                    height = 165f,
                    shapeFillColor = 0xFFF43F5E,
                    positionY = -150f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_ty_main",
                        name = "Thank You",
                        type = LayerType.TEXT,
                        text = "Thank You!",
                        fontKey = "pacifico",
                        fontSize = 86f,
                        textColor = 0xFF4C1D95,
                        positionY = 15f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_ty_sub",
                        name = "Support Note",
                        type = LayerType.TEXT,
                        text = "FOR SUPPORTING SMALL BUSINESS",
                        fontKey = "poppins",
                        fontSize = 24f,
                        isBold = true,
                        letterSpacing = 0.14f,
                        textColor = 0xFF7C2D12,
                        positionY = 140f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 16. Classic Top/Bottom Meme (Meme)
        StylePreset(
            id = "preset_classic_meme",
            title = "Classic Meme",
            description = "Bold white outlined top & bottom caption format",
            category = PresetCategory.MEME,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF1E293B
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_meme_top",
                        name = "Top Text",
                        type = LayerType.TEXT,
                        text = "WHEN THE DESIGN EXPORTS",
                        fontKey = "anton",
                        fontSize = 68f,
                        textColor = 0xFFFFFFFF,
                        strokeColor = 0xFF000000,
                        strokeWidth = 7f,
                        strokePosition = StrokePosition.OUTSIDE,
                        positionY = -410f,
                        zIndex = 0
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_meme_bottom",
                        name = "Bottom Text",
                        type = LayerType.TEXT,
                        text = "ON THE FIRST TRY",
                        fontKey = "anton",
                        fontSize = 72f,
                        textColor = 0xFFFFFFFF,
                        strokeColor = 0xFF000000,
                        strokeWidth = 7f,
                        strokePosition = StrokePosition.OUTSIDE,
                        positionY = 410f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 17. Modern Caption Box Meme (Meme)
        StylePreset(
            id = "preset_caption_meme",
            title = "Modern Caption",
            description = "Clean white header bar meme ready for imported photo",
            category = PresetCategory.MEME,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.SOLID_COLOR,
                backgroundColor = 0xFF334155
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_cap_bar",
                    name = "White Header",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.RECTANGLE,
                    width = 1080f,
                    height = 240f,
                    shapeFillColor = 0xFFFFFFFF,
                    positionY = -420f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_cap_text",
                        name = "Caption",
                        type = LayerType.TEXT,
                        text = "POV: You found the perfect font\nafter 45 minutes of scrolling",
                        fontKey = "poppins",
                        fontSize = 38f,
                        isBold = true,
                        textAlign = TextAlignment.LEFT,
                        textColor = 0xFF0F172A,
                        positionY = -420f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 18. Comic Word Balloon (Meme)
        StylePreset(
            id = "preset_comic_shout",
            title = "Comic Pop!",
            description = "Retro comic speech burst with halftone vibe",
            category = PresetCategory.MEME,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1080,
                preset = AspectRatioPreset.SQUARE,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF38BDF8, 0xFF0284C7),
                gradientAngle = 45f,
                filters = CanvasFilters(stripesEnabled = true, stripesSpacing = 14f)
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_comic_burst",
                    name = "Shout Balloon",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.SHOUT_BURST,
                    width = 760f,
                    height = 560f,
                    shapeFillColor = 0xFFFFFBEB,
                    shapeStrokeColor = 0xFF000000,
                    shapeStrokeWidth = 10f,
                    shadowEnabled = true,
                    shadowColor = 0xFF000000,
                    shadowRadius = 2f,
                    shadowDx = 14f,
                    shadowDy = 14f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_comic_text",
                        name = "Comic Exclamation",
                        type = LayerType.TEXT,
                        text = "BOOM!",
                        fontKey = "bangers",
                        fontSize = 136f,
                        textColor = 0xFFEF4444,
                        strokeColor = 0xFF000000,
                        strokeWidth = 8f,
                        rotation = -8f,
                        is3D = true,
                        depth3D = 12f,
                        zIndex = 1
                    )
                )
            )
        ),

        // 19. Instagram / TikTok Story Aesthetic (Story)
        StylePreset(
            id = "preset_story_vibes",
            title = "Daily Story",
            description = "9:16 vertical story frame with safe-zone friendly layout",
            category = PresetCategory.STORY,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1920,
                preset = AspectRatioPreset.STORY_REELS,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF1F1C2C, 0xFF928DAB),
                gradientAngle = 90f,
                filters = CanvasFilters(grainEnabled = true, grainIntensity = 0.16f)
            ),
            initialLayers = listOf(
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_story_pill",
                        name = "Mood Pill",
                        type = LayerType.TEXT,
                        text = "NEW POST",
                        fontKey = "poppins",
                        fontSize = 28f,
                        isBold = true,
                        letterSpacing = 0.20f,
                        textColor = 0xFF1F1C2C,
                        textBackgroundColor = 0xFFFFFFFF,
                        textBackgroundPadding = 22f,
                        textBackgroundRadius = 40f,
                        positionY = -220f,
                        zIndex = 0
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_story_main",
                        name = "Story Title",
                        type = LayerType.TEXT,
                        text = "Weekend\nJournal",
                        fontKey = "pt_serif",
                        fontSize = 104f,
                        isItalic = true,
                        textColor = 0xFFFFFFFF,
                        lineHeightMultiplier = 1.05f,
                        positionY = -20f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_story_sub",
                        name = "Swipe Note",
                        type = LayerType.TEXT,
                        text = "TAP TO EXPLORE MORE",
                        fontKey = "poppins",
                        fontSize = 26f,
                        letterSpacing = 0.18f,
                        textColor = 0xFFE2E8F0,
                        positionY = 220f,
                        zIndex = 2
                    )
                )
            )
        ),

        // 20. Music Now Playing Story (Story)
        StylePreset(
            id = "preset_story_music",
            title = "Now Playing",
            description = "9:16 nocturnal music & podcast share story",
            category = PresetCategory.STORY,
            canvasConfig = CanvasConfig(
                width = 1080,
                height = 1920,
                preset = AspectRatioPreset.STORY_REELS,
                backgroundType = BackgroundType.GRADIENT,
                gradientColors = listOf(0xFF090A0F, 0xFF1B2735, 0xFF0F172A),
                gradientAngle = 110f,
                filters = CanvasFilters(vignetteEnabled = true, vignetteIntensity = 0.65f)
            ),
            initialLayers = listOf(
                Layer(
                    id = "layer_music_card",
                    name = "Album Frame",
                    type = LayerType.SHAPE,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    width = 720f,
                    height = 720f,
                    shapeFillColor = 0xFF1E293B,
                    shapeGradientEnabled = true,
                    shapeGradientEndColor = 0xFF4F46E5,
                    shapeGradientAngle = 45f,
                    shapeCornerRadius = 42f,
                    shadowEnabled = true,
                    shadowColor = 0xAA000000,
                    shadowRadius = 32f,
                    positionY = -180f,
                    zIndex = 0
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_music_track",
                        name = "Track Title",
                        type = LayerType.TEXT,
                        text = "MIDNIGHT DRIVE",
                        fontKey = "archivo_black",
                        fontSize = 54f,
                        textColor = 0xFFFFFFFF,
                        positionY = 300f,
                        zIndex = 1
                    )
                ),
                TextSizing.withEstimatedBounds(
                    Layer(
                        id = "layer_music_artist",
                        name = "Artist Name",
                        type = LayerType.TEXT,
                        text = "SYNTHWAVE COLLECTIVE • 03:42",
                        fontKey = "poppins",
                        fontSize = 26f,
                        letterSpacing = 0.14f,
                        textColor = 0xFF38BDF8,
                        positionY = 380f,
                        zIndex = 2
                    )
                )
            )
        )
    )
}
