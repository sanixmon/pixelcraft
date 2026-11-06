package com.app.pixelcraft.data.presets

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.app.pixelcraft.R
import java.util.concurrent.ConcurrentHashMap

data class BundledFont(
    val key: String,
    val displayName: String,
    val category: String,
    @FontRes val regularResId: Int? = null,
    @FontRes val boldResId: Int? = null,
    val widthFactor: Float = 1.0f
)

object FontCatalog {

    const val DEFAULT_KEY = "default"

    val fonts: List<BundledFont> = listOf(
        BundledFont(
            key = DEFAULT_KEY,
            displayName = "System Sans",
            category = "Sans",
            regularResId = null,
            boldResId = null,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "poppins",
            displayName = "Poppins",
            category = "Sans",
            regularResId = R.font.poppins_regular,
            boldResId = R.font.poppins_bold,
            widthFactor = 1.05f
        ),
        BundledFont(
            key = "lato",
            displayName = "Lato",
            category = "Sans",
            regularResId = R.font.lato_regular,
            boldResId = R.font.lato_bold,
            widthFactor = 0.98f
        ),
        BundledFont(
            key = "bebas_neue",
            displayName = "Bebas Neue",
            category = "Display",
            regularResId = R.font.bebas_neue_regular,
            widthFactor = 0.76f
        ),
        BundledFont(
            key = "anton",
            displayName = "Anton",
            category = "Display",
            regularResId = R.font.anton_regular,
            widthFactor = 0.84f
        ),
        BundledFont(
            key = "archivo_black",
            displayName = "Archivo Black",
            category = "Display",
            regularResId = R.font.archivo_black_regular,
            widthFactor = 1.16f
        ),
        BundledFont(
            key = "russo_one",
            displayName = "Russo One",
            category = "Display",
            regularResId = R.font.russo_one_regular,
            widthFactor = 1.12f
        ),
        BundledFont(
            key = "bangers",
            displayName = "Bangers",
            category = "Comic",
            regularResId = R.font.bangers_regular,
            widthFactor = 0.88f
        ),
        BundledFont(
            key = "luckiest_guy",
            displayName = "Luckiest Guy",
            category = "Comic",
            regularResId = R.font.luckiest_guy_regular,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "abril_fatface",
            displayName = "Abril Fatface",
            category = "Serif",
            regularResId = R.font.abril_fatface_regular,
            widthFactor = 1.06f
        ),
        BundledFont(
            key = "pt_serif",
            displayName = "PT Serif",
            category = "Serif",
            regularResId = R.font.pt_serif_regular,
            boldResId = R.font.pt_serif_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "permanent_marker",
            displayName = "Permanent Marker",
            category = "Hand",
            regularResId = R.font.permanent_marker_regular,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "pacifico",
            displayName = "Pacifico",
            category = "Script",
            regularResId = R.font.pacifico_regular,
            widthFactor = 1.05f
        ),
        BundledFont(
            key = "great_vibes",
            displayName = "Great Vibes",
            category = "Script",
            regularResId = R.font.great_vibes_regular,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "press_start_2p",
            displayName = "Press Start 2P",
            category = "Pixel",
            regularResId = R.font.press_start_2p_regular,
            widthFactor = 1.45f
        ),
        BundledFont(
            key = "space_mono",
            displayName = "Space Mono",
            category = "Mono",
            regularResId = R.font.space_mono_regular,
            boldResId = R.font.space_mono_bold,
            widthFactor = 1.12f
        )
    )

    private val fontsByKey: Map<String, BundledFont> = fonts.associateBy { it.key }
    private val typefaceCache = ConcurrentHashMap<Int, Typeface>()

    fun find(fontKey: String): BundledFont = fontsByKey[fontKey] ?: fonts.first()

    fun widthFactor(fontKey: String): Float = find(fontKey).widthFactor

    fun resolveComposeFontFamily(fontKey: String): FontFamily {
        val entry = find(fontKey)
        val reg = entry.regularResId ?: return FontFamily.Default
        val bold = entry.boldResId
        return if (bold != null) {
            FontFamily(
                Font(reg, FontWeight.Normal),
                Font(bold, FontWeight.Bold)
            )
        } else {
            FontFamily(Font(reg, FontWeight.Normal))
        }
    }

    fun resolveTypeface(
        context: Context,
        fontKey: String,
        isBold: Boolean,
        isItalic: Boolean
    ): Typeface {
        val entry = find(fontKey)
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val resId = when {
            isBold && entry.boldResId != null -> entry.boldResId
            else -> entry.regularResId
        } ?: return Typeface.create(Typeface.DEFAULT, style)

        val base = typefaceCache.getOrPut(resId) {
            runCatching { ResourcesCompat.getFont(context, resId) }.getOrNull() ?: Typeface.DEFAULT
        }
        return if (style == Typeface.NORMAL || (style == Typeface.BOLD && entry.boldResId != null)) {
            base
        } else {
            Typeface.create(base, style)
        }
    }
}
