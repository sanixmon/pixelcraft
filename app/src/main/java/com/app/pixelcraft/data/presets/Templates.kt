package com.app.pixelcraft.data.presets

import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.CanvasConfig
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

/**
 * Starter templates for the formats people actually post: social posts, stories,
 * thumbnails, sales, hiring, events and greetings.
 *
 * Sizes are canvas pixels (`fontSize` included) and positions are offsets of each layer's
 * centre from the canvas centre, so a 1080×1080 post spans x/y −540..540. Layouts keep
 * roughly a 90px margin and a clear type hierarchy: one dominant headline, one
 * supporting line, one detail line.
 */
internal object Templates {

    // --- Builders -----------------------------------------------------------------------

    private fun square(bg: Long) = solid(1080, 1080, AspectRatioPreset.SQUARE, bg)
    private fun portrait(bg: Long) = solid(1080, 1350, AspectRatioPreset.INSTAGRAM_PORTRAIT_4_5, bg)
    private fun story(bg: Long) = solid(1080, 1920, AspectRatioPreset.STORY_REELS, bg)
    private fun thumbnail(bg: Long) = solid(1280, 720, AspectRatioPreset.YOUTUBE_THUMBNAIL, bg)

    private fun solid(w: Int, h: Int, preset: AspectRatioPreset, bg: Long) = CanvasConfig(
        width = w, height = h, preset = preset,
        backgroundType = BackgroundType.SOLID_COLOR, backgroundColor = bg
    )

    private fun gradient(
        w: Int,
        h: Int,
        preset: AspectRatioPreset,
        colors: List<Long>,
        angle: Float = 135f,
        type: GradientType = GradientType.LINEAR
    ) = CanvasConfig(
        width = w, height = h, preset = preset,
        backgroundType = BackgroundType.GRADIENT,
        gradientColors = colors, gradientAngle = angle, gradientType = type
    )

    private fun text(
        id: String,
        text: String,
        font: String,
        size: Float,
        color: Long,
        y: Float = 0f,
        x: Float = 0f,
        bold: Boolean = false,
        italic: Boolean = false,
        align: TextAlignment = TextAlignment.CENTER,
        spacing: Float = 0f,
        lineHeight: Float = 1.15f,
        background: Long? = null,
        backgroundPadding: Float = 16f,
        backgroundRadius: Float = 8f,
        stroke: Float = 0f,
        strokeColor: Long = 0xFF000000,
        shadow: Boolean = false,
        shadowColor: Long = 0x66000000,
        shadowRadius: Float = 14f,
        name: String = text.lineSequence().first().take(24)
    ) = TextSizing.withEstimatedBounds(
        Layer(
            id = id,
            name = name,
            type = LayerType.TEXT,
            text = text,
            fontKey = font,
            fontSize = size,
            textColor = color,
            positionX = x,
            positionY = y,
            isBold = bold,
            isItalic = italic,
            textAlign = align,
            letterSpacing = spacing,
            lineHeightMultiplier = lineHeight,
            textBackgroundColor = background,
            textBackgroundPadding = backgroundPadding,
            textBackgroundRadius = backgroundRadius,
            strokeWidth = stroke,
            strokeColor = strokeColor,
            strokePosition = StrokePosition.OUTSIDE,
            shadowEnabled = shadow,
            shadowColor = shadowColor,
            shadowRadius = shadowRadius,
            shadowDx = 0f,
            shadowDy = if (shadow) 6f else 0f
        )
    )

    private fun shape(
        id: String,
        w: Float,
        h: Float,
        fill: Long,
        x: Float = 0f,
        y: Float = 0f,
        type: ShapeType = ShapeType.RECTANGLE,
        radius: Float = 0f,
        strokeWidth: Float = 0f,
        strokeColor: Long = 0xFFFFFFFF,
        strokePattern: StrokePattern = StrokePattern.SOLID,
        gradientEnd: Long? = null,
        gradientAngle: Float = 90f,
        rotation: Float = 0f,
        opacity: Float = 1f,
        name: String = id.removePrefix("l_").replace('_', ' ').replaceFirstChar { it.uppercase() }
    ) = Layer(
        id = id,
        name = name,
        type = LayerType.SHAPE,
        shapeType = type,
        width = w,
        height = h,
        positionX = x,
        positionY = y,
        rotation = rotation,
        opacity = opacity,
        shapeFillColor = fill,
        shapeGradientEnabled = gradientEnd != null,
        shapeGradientEndColor = gradientEnd ?: fill,
        shapeGradientAngle = gradientAngle,
        shapeCornerRadius = radius,
        shapeStrokeWidth = strokeWidth,
        shapeStrokeColor = strokeColor,
        strokePosition = StrokePosition.INSIDE,
        strokePattern = strokePattern
    )

    /** Stacks layers in the order given: first is the back-most. */
    private fun stack(vararg layers: Layer): List<Layer> =
        layers.mapIndexed { i, layer -> layer.copy(zIndex = i) }

    private fun template(
        id: String,
        title: String,
        description: String,
        category: PresetCategory,
        canvas: CanvasConfig,
        vararg layers: Layer
    ) = StylePreset(id, title, description, canvas, stack(*layers), category)

    // Palette
    private const val INK = 0xFF111111L
    private const val PAPER = 0xFFF6F1E9L
    private const val WHITE = 0xFFFFFFFFL
    private const val MUTED_DARK = 0xFF6B6B6BL
    private const val MUTED_LIGHT = 0xB3FFFFFFL
    private const val YELLOW = 0xFFFFD233L
    private const val RED = 0xFFE63946L
    private const val BLUE = 0xFF2457F5L
    private const val NAVY = 0xFF0B1F3AL
    private const val GREEN = 0xFF1F5C4AL
    private const val GOLD = 0xFFC9A227L
    private const val PINK = 0xFFFF5C8AL

    // --- Templates ----------------------------------------------------------------------

    val all: List<StylePreset> = listOf(
        // Editor starting canvas: must stay first.
        template(
            "preset_default", "Blank Canvas", "Clean square canvas with one headline", PresetCategory.QUOTE,
            square(WHITE),
            text("layer_default_text", "Your headline", "inter", 96f, INK, bold = true, name = "Headline")
        ),

        // ---------- Quote ----------
        template(
            "preset_quote_minimal", "Minimal Quote", "Editorial serif quote with author line", PresetCategory.QUOTE,
            square(PAPER),
            text("l_mark", "“", "playfair_display", 260f, GOLD, y = -270f, bold = true, name = "Quote Mark"),
            text(
                "l_quote", "Simplicity is the\nultimate sophistication.", "playfair_display", 68f, INK,
                y = -10f, lineHeight = 1.25f, name = "Quote"
            ),
            shape("l_divider", 120f, 4f, GOLD, y = 170f),
            text("l_author", "LEONARDO DA VINCI", "inter", 28f, MUTED_DARK, y = 230f, bold = true, spacing = 0.18f, name = "Author")
        ),
        template(
            "preset_quote_bold", "Bold Statement", "Big condensed statement on black", PresetCategory.QUOTE,
            square(INK),
            text("l_statement", "DONE IS\nBETTER THAN\nPERFECT", "anton", 150f, WHITE, y = -40f, lineHeight = 1.0f, name = "Statement"),
            shape("l_underline", 360f, 14f, YELLOW, y = 250f),
            text("l_handle", "@yourbrand", "inter", 30f, MUTED_LIGHT, y = 400f, name = "Handle")
        ),
        template(
            "preset_keep_calm", "Keep Calm Poster", "Classic crown poster, portrait", PresetCategory.QUOTE,
            portrait(0xFFC8102EL),
            Layer(
                id = "layer_crown", name = "Crown", type = LayerType.SHAPE, shapeType = ShapeType.CROWN,
                width = 200f, height = 140f, positionY = -400f, shapeFillColor = WHITE
            ),
            text(
                "l_keep_calm", "KEEP\nCALM", "oswald", 190f, WHITE, y = -110f, bold = true, lineHeight = 0.95f,
                spacing = 0.04f, name = "Keep Calm"
            ),
            text("l_and", "AND", "oswald", 56f, WHITE, y = 120f, bold = true, spacing = 0.1f),
            text(
                "l_carry_on", "CARRY\nON", "oswald", 150f, WHITE, y = 320f, bold = true, lineHeight = 0.95f,
                spacing = 0.04f, name = "Carry On"
            )
        ),

        // ---------- YouTube ----------
        template(
            "preset_youtube", "Bold Thumbnail", "High-contrast title with highlight word", PresetCategory.YOUTUBE,
            gradient(1280, 720, AspectRatioPreset.YOUTUBE_THUMBNAIL, listOf(0xFF0F0F14L, 0xFF2A1035L), 120f),
            shape("l_glow", 760f, 760f, 0x55FF3D7FL, x = 420f, y = 40f, type = ShapeType.CIRCLE),
            text(
                "l_yt_title", "I TRIED IT\nFOR 30 DAYS", "anton", 140f, WHITE, x = -180f, y = -40f,
                lineHeight = 1.0f, stroke = 6f, shadow = true, name = "Title"
            ),
            text(
                "layer_yt_sub", "HERE'S WHAT HAPPENED", "bebas_neue", 64f, INK, x = -180f, y = 210f,
                background = YELLOW, backgroundPadding = 18f, backgroundRadius = 6f, name = "Subtitle"
            )
        ),
        template(
            "preset_yt_tutorial", "Tutorial Thumbnail", "Clean how-to layout with step tag", PresetCategory.YOUTUBE,
            thumbnail(WHITE),
            shape("l_side", 460f, 720f, BLUE, x = 410f),
            text(
                "l_tag", "STEP BY STEP", "inter", 30f, WHITE, x = -230f, y = -210f, bold = true, spacing = 0.12f,
                background = BLUE, backgroundPadding = 16f, backgroundRadius = 24f, name = "Tag"
            ),
            text(
                "l_title", "Edit Photos\nLike a Pro", "montserrat", 104f, INK, x = -200f, y = -10f,
                bold = true, lineHeight = 1.05f, name = "Title"
            ),
            text("l_sub", "Beginner friendly · 10 minutes", "inter", 34f, MUTED_DARK, x = -230f, y = 190f, name = "Subtitle"),
            text("l_number", "01", "syne", 260f, WHITE, x = 410f, bold = true, name = "Number")
        ),
        template(
            "preset_yt_versus", "Versus Thumbnail", "Split comparison with VS badge", PresetCategory.YOUTUBE,
            thumbnail(INK),
            shape("l_left", 640f, 720f, RED, x = -320f),
            shape("l_right", 640f, 720f, BLUE, x = 320f),
            text("l_left_label", "IPHONE", "anton", 120f, WHITE, x = -320f, y = 200f, shadow = true, name = "Left Label"),
            text("l_right_label", "ANDROID", "anton", 120f, WHITE, x = 320f, y = 200f, shadow = true, name = "Right Label"),
            shape("l_vs_badge", 200f, 200f, YELLOW, type = ShapeType.CIRCLE, strokeWidth = 10f, strokeColor = INK),
            text("l_vs", "VS", "anton", 96f, INK, name = "VS")
        ),
        template(
            "preset_yt_podcast", "Podcast Episode", "Episode cover with guest name", PresetCategory.YOUTUBE,
            gradient(1280, 720, AspectRatioPreset.YOUTUBE_THUMBNAIL, listOf(0xFF141E30L, 0xFF243B55L), 90f),
            shape("l_photo_spot", 420f, 420f, 0x33FFFFFFL, x = 360f, type = ShapeType.CIRCLE, strokeWidth = 8f, strokeColor = YELLOW),
            text("l_ep", "EPISODE 42", "inter", 30f, YELLOW, x = -260f, y = -190f, bold = true, spacing = 0.2f, name = "Episode"),
            text(
                "l_title", "Building a\nBrand From Zero", "dm_serif_display", 92f, WHITE, x = -200f, y = 0f,
                lineHeight = 1.05f, name = "Title"
            ),
            text("l_guest", "with Sarah Kim", "inter", 36f, MUTED_LIGHT, x = -320f, y = 190f, name = "Guest")
        ),

        // ---------- Promo ----------
        template(
            "preset_flash_sale", "Flash Sale", "Urgent sale with discount and CTA", PresetCategory.PROMO,
            square(RED),
            text("l_kicker", "48 HOURS ONLY", "inter", 32f, WHITE, y = -330f, bold = true, spacing = 0.2f, name = "Kicker"),
            text("l_headline", "FLASH\nSALE", "anton", 230f, WHITE, y = -90f, lineHeight = 0.92f, name = "Headline"),
            text(
                "l_offer", "UP TO 50% OFF", "anton", 72f, INK, y = 170f,
                background = YELLOW, backgroundPadding = 20f, backgroundRadius = 4f, name = "Offer"
            ),
            text(
                "l_cta", "SHOP NOW  →", "inter", 34f, RED, y = 340f, bold = true, spacing = 0.08f,
                background = WHITE, backgroundPadding = 22f, backgroundRadius = 40f, name = "CTA"
            )
        ),
        template(
            "preset_product_launch", "Product Launch", "Dark premium reveal for a new product", PresetCategory.PROMO,
            gradient(1080, 1080, AspectRatioPreset.SQUARE, listOf(0xFF2B354FL, 0xFF0B0E17L), 0f, GradientType.RADIAL),
            text(
                "l_new", "NEW", "inter", 26f, INK, y = -330f, bold = true, spacing = 0.25f,
                background = WHITE, backgroundPadding = 14f, backgroundRadius = 20f, name = "Badge"
            ),
            text("l_intro", "Introducing", "inter", 44f, MUTED_LIGHT, y = -220f, name = "Intro"),
            text("l_product", "Aurora One", "syne", 150f, WHITE, y = -80f, bold = true, name = "Product"),
            shape("l_product_spot", 380f, 260f, 0x22FFFFFFL, y = 170f, type = ShapeType.ROUNDED_RECTANGLE, radius = 32f, name = "Product Photo Spot"),
            text("l_available", "Available now · yourbrand.com", "inter", 30f, MUTED_LIGHT, y = 400f, name = "Availability")
        ),
        template(
            "preset_discount_code", "Discount Code", "Coupon with dashed cut-out border", PresetCategory.PROMO,
            square(0xFFFDE8E4L),
            text("l_save", "Treat yourself", "playfair_display", 64f, INK, y = -300f, italic = true, name = "Kicker"),
            shape(
                "l_coupon", 820f, 420f, WHITE, y = 20f, type = ShapeType.ROUNDED_RECTANGLE, radius = 28f,
                strokeWidth = 6f, strokeColor = PINK, strokePattern = StrokePattern.DASHED, name = "Coupon"
            ),
            text("l_percent", "20% OFF", "anton", 170f, PINK, y = -30f, name = "Discount"),
            text("l_code", "USE CODE  SAVE20", "jetbrains_mono", 40f, INK, y = 130f, bold = true, name = "Code"),
            text("l_terms", "Valid until 31 Dec · online & in store", "inter", 28f, MUTED_DARK, y = 330f, name = "Terms")
        ),
        template(
            "preset_giveaway", "Giveaway", "Contest post with entry steps", PresetCategory.PROMO,
            gradient(1080, 1080, AspectRatioPreset.SQUARE, listOf(0xFF8E2DE2L, 0xFF4A00E0L), 135f),
            shape("l_star_a", 90f, 90f, YELLOW, x = -380f, y = -380f, type = ShapeType.STAR, rotation = -12f),
            shape("l_star_b", 60f, 60f, WHITE, x = 400f, y = -300f, type = ShapeType.STAR, rotation = 18f),
            text("l_title", "GIVEAWAY", "bungee", 130f, WHITE, y = -230f, shadow = true, name = "Title"),
            text("l_prize", "Win a year of free coffee", "inter", 44f, YELLOW, y = -100f, bold = true, name = "Prize"),
            text(
                "l_steps", "1.  Follow @yourbrand\n2.  Like this post\n3.  Tag two friends", "inter", 42f, WHITE,
                y = 110f, lineHeight = 1.5f, name = "Steps"
            ),
            text("l_deadline", "Winner announced Friday", "inter", 28f, MUTED_LIGHT, y = 380f, name = "Deadline")
        ),
        template(
            "preset_menu", "Menu & Price List", "Cafe menu with items and prices", PresetCategory.PROMO,
            portrait(0xFF1E1A17L),
            text("l_cafe", "THE CORNER CAFE", "inter", 28f, GOLD, y = -540f, bold = true, spacing = 0.25f, name = "Venue"),
            text("l_menu", "Menu", "playfair_display", 140f, PAPER, y = -410f, italic = true, name = "Title"),
            shape("l_rule", 140f, 3f, GOLD, y = -300f),
            text(
                "l_items",
                "Espresso — 25\nCappuccino — 32\nCaffè Latte — 35\nMatcha Latte — 38\nCroissant — 28\nBanana Bread — 30",
                "lora", 50f, PAPER, y = 0f, lineHeight = 1.6f, name = "Items"
            ),
            text("l_hours", "Open daily 7am – 9pm", "inter", 30f, 0x99F6F1E9L, y = 520f, name = "Hours")
        ),

        // ---------- Business ----------
        template(
            "preset_hiring", "We're Hiring", "Job post with role, type and how to apply", PresetCategory.BUSINESS,
            square(BLUE),
            text("l_title", "WE'RE\nHIRING", "montserrat", 150f, WHITE, y = -190f, bold = true, lineHeight = 1.0f, name = "Title"),
            text(
                "l_role", "Senior Product Designer", "inter", 46f, BLUE, y = 70f, bold = true,
                background = WHITE, backgroundPadding = 22f, backgroundRadius = 40f, name = "Role"
            ),
            text("l_details", "Remote  ·  Full-time  ·  Jakarta", "inter", 34f, MUTED_LIGHT, y = 180f, name = "Details"),
            text("l_apply", "Apply: careers@yourcompany.com", "inter", 32f, WHITE, y = 380f, bold = true, name = "Apply")
        ),
        template(
            "preset_announcement", "Announcement", "Clean company news post", PresetCategory.BUSINESS,
            square(WHITE),
            shape("l_accent", 1080f, 18f, INK, y = -531f),
            text("l_label", "ANNOUNCEMENT", "inter", 28f, BLUE, y = -300f, bold = true, spacing = 0.25f, name = "Label"),
            text(
                "l_headline", "We're opening\nour second studio", "dm_serif_display", 96f, INK, y = -110f,
                lineHeight = 1.1f, name = "Headline"
            ),
            text(
                "l_body", "Join us for the grand opening on 12 March\nat 21 Market Street.", "inter", 34f, MUTED_DARK,
                y = 110f, lineHeight = 1.4f, name = "Body"
            ),
            text("l_brand", "YOUR BRAND", "inter", 28f, INK, y = 420f, bold = true, spacing = 0.3f, name = "Brand")
        ),
        template(
            "preset_testimonial", "Customer Review", "Five-star testimonial card", PresetCategory.BUSINESS,
            square(0xFFEEF1F5L),
            shape("l_card", 880f, 700f, WHITE, type = ShapeType.ROUNDED_RECTANGLE, radius = 40f, name = "Card"),
            shape("l_star1", 56f, 56f, 0xFFFFB400L, x = -140f, y = -230f, type = ShapeType.STAR),
            shape("l_star2", 56f, 56f, 0xFFFFB400L, x = -70f, y = -230f, type = ShapeType.STAR),
            shape("l_star3", 56f, 56f, 0xFFFFB400L, x = 0f, y = -230f, type = ShapeType.STAR),
            shape("l_star4", 56f, 56f, 0xFFFFB400L, x = 70f, y = -230f, type = ShapeType.STAR),
            shape("l_star5", 56f, 56f, 0xFFFFB400L, x = 140f, y = -230f, type = ShapeType.STAR),
            text(
                "l_review", "“Fast delivery and the quality\nis even better than the photos.\nWill order again!”",
                "lora", 46f, INK, y = -20f, italic = true, lineHeight = 1.4f, name = "Review"
            ),
            text("l_name", "Rina Putri", "inter", 34f, INK, y = 190f, bold = true, name = "Name"),
            text("l_role", "Verified customer", "inter", 28f, MUTED_DARK, y = 240f, name = "Role")
        ),
        template(
            "preset_milestone", "Milestone", "Big number thank-you post", PresetCategory.BUSINESS,
            square(INK),
            text("l_number", "10K", "syne", 330f, YELLOW, y = -60f, bold = true, name = "Number"),
            text("l_label", "followers", "inter", 56f, WHITE, y = 150f, name = "Label"),
            text("l_thanks", "Thank you for growing with us", "inter", 34f, MUTED_LIGHT, y = 360f, name = "Thanks")
        ),
        template(
            "preset_tips_cover", "Tips Carousel Cover", "First slide of a swipeable tips post", PresetCategory.BUSINESS,
            portrait(0xFFFFF4D6L),
            text("l_kicker", "5 SIMPLE WAYS TO", "inter", 38f, INK, y = -300f, bold = true, spacing = 0.15f, name = "Kicker"),
            text(
                "l_title", "Grow Your\nSmall Business", "dm_serif_display", 120f, INK, y = -90f,
                lineHeight = 1.05f, name = "Title"
            ),
            shape("l_underline", 300f, 16f, 0xFFFF8A00L, y = 110f),
            text("l_swipe", "Swipe", "inter", 34f, INK, x = 300f, y = 560f, bold = true, name = "Swipe"),
            shape("l_arrow", 80f, 50f, INK, x = 420f, y = 560f, type = ShapeType.ARROW, name = "Arrow")
        ),

        // ---------- Event ----------
        template(
            "preset_event", "Event Poster", "Date, title, venue and registration", PresetCategory.EVENT,
            portrait(NAVY),
            text(
                "l_date", "NOV\n28", "anton", 110f, NAVY, y = -420f, lineHeight = 0.95f,
                background = YELLOW, backgroundPadding = 26f, backgroundRadius = 12f, name = "Date"
            ),
            text("l_title", "Design\nMeetup\n2026", "syne", 150f, WHITE, y = -20f, bold = true, lineHeight = 1.0f, name = "Title"),
            text(
                "l_details", "Sat · 7:00 PM\nThe Hall, Jakarta", "inter", 40f, MUTED_LIGHT, y = 330f,
                lineHeight = 1.4f, name = "Details"
            ),
            text("l_register", "Register at yourevent.com", "inter", 32f, YELLOW, y = 530f, bold = true, name = "Register")
        ),
        template(
            "preset_webinar", "Webinar Banner", "Link-preview banner for an online session", PresetCategory.EVENT,
            gradient(1200, 628, AspectRatioPreset.FACEBOOK_COVER, listOf(0xFF0F2027L, 0xFF2C5364L), 0f),
            text(
                "l_badge", "FREE WEBINAR", "inter", 26f, INK, x = -330f, y = -200f, bold = true, spacing = 0.2f,
                background = YELLOW, backgroundPadding = 14f, backgroundRadius = 20f, name = "Badge"
            ),
            text(
                "l_title", "Marketing on a\nZero Budget", "montserrat", 84f, WHITE, x = -220f, y = -30f,
                bold = true, lineHeight = 1.05f, name = "Title"
            ),
            text("l_when", "Thursday, 20 Feb · 3 PM WIB", "inter", 30f, MUTED_LIGHT, x = -290f, y = 150f, name = "When"),
            shape("l_speaker", 300f, 300f, 0x33FFFFFFL, x = 400f, type = ShapeType.CIRCLE, strokeWidth = 6f, strokeColor = YELLOW, name = "Speaker Photo Spot"),
            text("l_speaker_name", "Andi Pratama", "inter", 28f, WHITE, x = 400f, y = 200f, bold = true, name = "Speaker")
        ),
        template(
            "preset_save_the_date", "Save the Date", "Elegant wedding announcement", PresetCategory.EVENT,
            portrait(PAPER),
            shape(
                "l_frame", 900f, 1170f, 0x00000000, type = ShapeType.RECTANGLE,
                strokeWidth = 3f, strokeColor = GOLD, name = "Frame"
            ),
            text("l_save", "Save the Date", "great_vibes", 150f, GOLD, y = -330f, name = "Script Title"),
            text("l_names", "ANNA & DAVID", "cinzel", 72f, INK, y = -80f, bold = true, spacing = 0.08f, name = "Names"),
            shape("l_rule", 160f, 2f, GOLD, y = 30f),
            text("l_date", "14 · 06 · 2027", "cinzel", 56f, INK, y = 130f, name = "Date"),
            text("l_place", "Bali, Indonesia", "lora", 40f, MUTED_DARK, y = 230f, italic = true, name = "Place"),
            text("l_invite", "Formal invitation to follow", "inter", 26f, MUTED_DARK, y = 470f, spacing = 0.1f, name = "Note")
        ),

        // ---------- Greeting ----------
        template(
            "preset_birthday", "Happy Birthday", "Playful birthday card with confetti", PresetCategory.GREETING,
            gradient(1080, 1080, AspectRatioPreset.SQUARE, listOf(0xFFFF9A9EL, 0xFFFECFEFL), 135f),
            shape("l_dot_a", 60f, 60f, YELLOW, x = -390f, y = -360f, type = ShapeType.CIRCLE),
            shape("l_dot_b", 40f, 40f, 0xFF7C4DFFL, x = 380f, y = -400f, type = ShapeType.CIRCLE),
            shape("l_star", 80f, 80f, WHITE, x = 400f, y = 330f, type = ShapeType.STAR, rotation = 15f),
            shape("l_dot_c", 50f, 50f, 0xFF00B8A9L, x = -360f, y = 380f, type = ShapeType.CIRCLE),
            text("l_happy", "Happy", "pacifico", 150f, WHITE, y = -170f, shadow = true, shadowColor = 0x33000000, name = "Happy"),
            text("l_birthday", "BIRTHDAY", "bungee", 130f, 0xFF3D0C4EL, y = 20f, name = "Birthday"),
            text("l_name", "Sarah!", "pacifico", 90f, WHITE, y = 190f, name = "Name"),
            text("l_wish", "Wishing you a year full of joy", "inter", 32f, 0xFF3D0C4EL, y = 360f, name = "Wish")
        ),
        template(
            "preset_holiday", "Season's Greetings", "Warm holiday card", PresetCategory.GREETING,
            square(GREEN),
            shape("l_frame", 920f, 920f, 0x00000000, strokeWidth = 3f, strokeColor = GOLD, name = "Frame"),
            text("l_title", "Happy\nHolidays", "playfair_display", 150f, PAPER, y = -90f, italic = true, lineHeight = 1.0f, name = "Title"),
            shape("l_star", 60f, 60f, GOLD, y = 160f, type = ShapeType.STAR),
            text("l_msg", "Warm wishes from all of us", "inter", 34f, 0xCCF6F1E9L, y = 260f, name = "Message"),
            text("l_brand", "YOUR BRAND", "inter", 26f, GOLD, y = 380f, bold = true, spacing = 0.3f, name = "Brand")
        ),
        template(
            "preset_thank_you", "Thank You", "Script thank-you card", PresetCategory.GREETING,
            square(PAPER),
            text("l_thanks", "Thank you", "great_vibes", 200f, INK, y = -60f, name = "Thank You"),
            text(
                "l_line", "for being part of our journey", "lora", 44f, MUTED_DARK, y = 110f, italic = true, name = "Line"
            ),
            shape("l_heart", 56f, 50f, RED, y = 260f, type = ShapeType.HEART)
        ),

        // ---------- Meme ----------
        template(
            "preset_meme_classic", "Classic Meme", "Top and bottom impact captions over a photo", PresetCategory.MEME,
            square(0xFF3A3A3AL),
            text("l_top", "TOP TEXT", "anton", 120f, WHITE, y = -400f, stroke = 8f, name = "Top Text"),
            text("l_hint", "Add a photo as the background", "inter", 30f, 0x80FFFFFFL, name = "Hint"),
            text("l_bottom", "BOTTOM TEXT", "anton", 120f, WHITE, y = 400f, stroke = 8f, name = "Bottom Text")
        ),
        template(
            "preset_meme_caption", "Caption Meme", "Modern white caption bar above a photo", PresetCategory.MEME,
            square(0xFF3A3A3AL),
            shape("l_bar", 1080f, 300f, WHITE, y = -390f, name = "Caption Bar"),
            text(
                "l_caption", "When the code works\non the first try", "inter", 58f, INK, y = -390f,
                bold = true, lineHeight = 1.2f, name = "Caption"
            ),
            text("l_hint", "Add a photo below", "inter", 30f, 0x80FFFFFFL, y = 150f, name = "Hint")
        ),

        // ---------- Story ----------
        template(
            "preset_story_news", "Big News Story", "Announcement story with link sticker spot", PresetCategory.STORY,
            gradient(1080, 1920, AspectRatioPreset.STORY_REELS, listOf(0xFFFF416CL, 0xFFFF4B2BL), 160f),
            text("l_kicker", "IT'S OFFICIAL", "inter", 36f, WHITE, y = -520f, bold = true, spacing = 0.25f, name = "Kicker"),
            text("l_title", "BIG\nNEWS", "anton", 300f, WHITE, y = -230f, lineHeight = 0.9f, shadow = true, name = "Title"),
            text(
                "l_body", "Our new collection drops\nthis Friday at 7 PM", "inter", 50f, WHITE, y = 130f,
                lineHeight = 1.35f, name = "Body"
            ),
            shape("l_arrow", 70f, 90f, WHITE, y = 520f, type = ShapeType.ARROW, rotation = -90f, name = "Arrow"),
            text(
                "l_link", "Tap the link", "inter", 40f, 0xFFFF416CL, y = 660f, bold = true,
                background = WHITE, backgroundPadding = 24f, backgroundRadius = 48f, name = "Link Button"
            )
        ),
        template(
            "preset_story_poll", "This or That", "Two-option story for polls", PresetCategory.STORY,
            story(INK),
            text("l_q", "Which one\nare you?", "montserrat", 90f, WHITE, y = -700f, bold = true, lineHeight = 1.05f, name = "Question"),
            shape("l_opt_a", 860f, 520f, 0xFF6F4E37L, y = -230f, type = ShapeType.ROUNDED_RECTANGLE, radius = 40f, name = "Option A"),
            text("l_opt_a_text", "COFFEE", "anton", 140f, WHITE, y = -230f, name = "Option A Label"),
            shape("l_vs", 150f, 150f, YELLOW, y = 70f, type = ShapeType.CIRCLE, strokeWidth = 8f, strokeColor = INK, name = "VS Badge"),
            text("l_vs_text", "OR", "anton", 64f, INK, y = 70f, name = "VS"),
            shape("l_opt_b", 860f, 520f, 0xFF3E8E41L, y = 370f, type = ShapeType.ROUNDED_RECTANGLE, radius = 40f, name = "Option B"),
            text("l_opt_b_text", "TEA", "anton", 140f, WHITE, y = 370f, name = "Option B Label")
        ),
        template(
            "preset_story_quote", "Story Quote", "Full-screen quote with handle", PresetCategory.STORY,
            story(0xFF1C1C28L),
            text("l_mark", "“", "playfair_display", 320f, GOLD, y = -480f, bold = true, name = "Quote Mark"),
            text(
                "l_quote", "Small steps\nevery day\nadd up to\nbig results.", "playfair_display", 104f, WHITE,
                y = -40f, lineHeight = 1.15f, name = "Quote"
            ),
            shape("l_rule", 120f, 4f, GOLD, y = 400f),
            text("l_handle", "@yourbrand", "inter", 38f, MUTED_LIGHT, y = 480f, name = "Handle")
        )
    )
}
