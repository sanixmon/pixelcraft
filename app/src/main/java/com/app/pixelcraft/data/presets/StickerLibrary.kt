package com.app.pixelcraft.data.presets

data class StickerItem(
    val id: String,
    val name: String,
    val category: String,
    val emojiOrIcon: String
)

object StickerLibrary {
    val categories = listOf("Emojis", "Badges & Stars", "Decorative", "Sale & Labels")

    val stickers = listOf(
        // Emojis
        StickerItem("emoji_fire", "Fire", "Emojis", "🔥"),
        StickerItem("emoji_sunglasses", "Cool", "Emojis", "😎"),
        StickerItem("emoji_heart_eyes", "Heart Eyes", "Emojis", "😍"),
        StickerItem("emoji_sparkles", "Sparkles", "Emojis", "✨"),
        StickerItem("emoji_100", "Hundred", "Emojis", "💯"),
        StickerItem("emoji_party", "Party", "Emojis", "🎉"),
        StickerItem("emoji_rocket", "Rocket", "Emojis", "🚀"),
        StickerItem("emoji_crown", "Crown", "Emojis", "👑"),

        // Badges & Stars
        StickerItem("badge_star", "Gold Star", "Badges & Stars", "⭐"),
        StickerItem("badge_glowing_star", "Glowing Star", "Badges & Stars", "🌟"),
        StickerItem("badge_trophy", "Trophy", "Badges & Stars", "🏆"),
        StickerItem("badge_medal", "Medal", "Badges & Stars", "🥇"),
        StickerItem("badge_check", "Verified", "Badges & Stars", "✅"),

        // Decorative
        StickerItem("dec_heart", "Red Heart", "Decorative", "❤️"),
        StickerItem("dec_lightning", "Lightning", "Decorative", "⚡"),
        StickerItem("dec_diamond", "Gem Diamond", "Decorative", "💎"),
        StickerItem("dec_bulb", "Idea Bulb", "Decorative", "💡"),
        StickerItem("dec_bell", "Notification", "Decorative", "🔔"),

        // Sale & Labels
        StickerItem("sale_tag", "Price Tag", "Sale & Labels", "🏷️"),
        StickerItem("sale_gift", "Gift Box", "Sale & Labels", "🎁"),
        StickerItem("sale_megaphone", "Announcement", "Sale & Labels", "📢"),
        StickerItem("sale_target", "Bulls Eye", "Sale & Labels", "🎯")
    )
}
