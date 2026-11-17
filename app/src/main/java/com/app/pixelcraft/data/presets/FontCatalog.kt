package com.app.pixelcraft.data.presets

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.app.pixelcraft.R
import com.app.pixelcraft.domain.DraftStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class CustomFontEntry(
    val key: String,
    val displayName: String,
    val fileName: String
)

data class BundledFont(
    val key: String,
    val displayName: String,
    val category: String,
    @FontRes val regularResId: Int? = null,
    @FontRes val boldResId: Int? = null,
    val widthFactor: Float = 1.0f,
    val customFile: File? = null
)

private data class StyledFontCacheKey(
    val fontKey: String,
    val isBold: Boolean,
    val isItalic: Boolean
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
        ),
        // Added 0.4: static Latin cuts from Fontsource (SIL OFL 1.1 / Apache 2.0), see THIRD_PARTY_FONTS.md
        BundledFont(
            key = "inter",
            displayName = "Inter",
            category = "Sans",
            regularResId = R.font.inter_regular,
            boldResId = R.font.inter_bold,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "montserrat",
            displayName = "Montserrat",
            category = "Sans",
            regularResId = R.font.montserrat_regular,
            boldResId = R.font.montserrat_bold,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "roboto",
            displayName = "Roboto",
            category = "Sans",
            regularResId = R.font.roboto_regular,
            boldResId = R.font.roboto_bold,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "open_sans",
            displayName = "Open Sans",
            category = "Sans",
            regularResId = R.font.open_sans_regular,
            boldResId = R.font.open_sans_bold,
            widthFactor = 1.03f
        ),
        BundledFont(
            key = "nunito",
            displayName = "Nunito",
            category = "Sans",
            regularResId = R.font.nunito_regular,
            boldResId = R.font.nunito_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "raleway",
            displayName = "Raleway",
            category = "Sans",
            regularResId = R.font.raleway_regular,
            boldResId = R.font.raleway_bold,
            widthFactor = 1.05f
        ),
        BundledFont(
            key = "work_sans",
            displayName = "Work Sans",
            category = "Sans",
            regularResId = R.font.work_sans_regular,
            boldResId = R.font.work_sans_bold,
            widthFactor = 1.04f
        ),
        BundledFont(
            key = "dm_sans",
            displayName = "DM Sans",
            category = "Sans",
            regularResId = R.font.dm_sans_regular,
            boldResId = R.font.dm_sans_bold,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "manrope",
            displayName = "Manrope",
            category = "Sans",
            regularResId = R.font.manrope_regular,
            boldResId = R.font.manrope_bold,
            widthFactor = 1.04f
        ),
        BundledFont(
            key = "rubik",
            displayName = "Rubik",
            category = "Sans",
            regularResId = R.font.rubik_regular,
            boldResId = R.font.rubik_bold,
            widthFactor = 1.03f
        ),
        BundledFont(
            key = "outfit",
            displayName = "Outfit",
            category = "Sans",
            regularResId = R.font.outfit_regular,
            boldResId = R.font.outfit_bold,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "plus_jakarta_sans",
            displayName = "Plus Jakarta Sans",
            category = "Sans",
            regularResId = R.font.plus_jakarta_sans_regular,
            boldResId = R.font.plus_jakarta_sans_bold,
            widthFactor = 1.04f
        ),
        BundledFont(
            key = "barlow",
            displayName = "Barlow",
            category = "Sans",
            regularResId = R.font.barlow_regular,
            boldResId = R.font.barlow_bold,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "josefin_sans",
            displayName = "Josefin Sans",
            category = "Sans",
            regularResId = R.font.josefin_sans_regular,
            boldResId = R.font.josefin_sans_bold,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "quicksand",
            displayName = "Quicksand",
            category = "Sans",
            regularResId = R.font.quicksand_regular,
            boldResId = R.font.quicksand_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "space_grotesk",
            displayName = "Space Grotesk",
            category = "Sans",
            regularResId = R.font.space_grotesk_regular,
            boldResId = R.font.space_grotesk_bold,
            widthFactor = 1.03f
        ),
        BundledFont(
            key = "league_spartan",
            displayName = "League Spartan",
            category = "Sans",
            regularResId = R.font.league_spartan_regular,
            boldResId = R.font.league_spartan_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "oswald",
            displayName = "Oswald",
            category = "Display",
            regularResId = R.font.oswald_regular,
            boldResId = R.font.oswald_bold,
            widthFactor = 0.8f
        ),
        BundledFont(
            key = "righteous",
            displayName = "Righteous",
            category = "Display",
            regularResId = R.font.righteous_regular,
            boldResId = null,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "alfa_slab_one",
            displayName = "Alfa Slab One",
            category = "Display",
            regularResId = R.font.alfa_slab_one_regular,
            boldResId = null,
            widthFactor = 1.15f
        ),
        BundledFont(
            key = "black_ops_one",
            displayName = "Black Ops One",
            category = "Display",
            regularResId = R.font.black_ops_one_regular,
            boldResId = null,
            widthFactor = 1.12f
        ),
        BundledFont(
            key = "bungee",
            displayName = "Bungee",
            category = "Display",
            regularResId = R.font.bungee_regular,
            boldResId = null,
            widthFactor = 1.25f
        ),
        BundledFont(
            key = "fredoka",
            displayName = "Fredoka",
            category = "Display",
            regularResId = R.font.fredoka_regular,
            boldResId = R.font.fredoka_bold,
            widthFactor = 1.05f
        ),
        BundledFont(
            key = "lilita_one",
            displayName = "Lilita One",
            category = "Display",
            regularResId = R.font.lilita_one_regular,
            boldResId = null,
            widthFactor = 0.98f
        ),
        BundledFont(
            key = "staatliches",
            displayName = "Staatliches",
            category = "Display",
            regularResId = R.font.staatliches_regular,
            boldResId = null,
            widthFactor = 0.85f
        ),
        BundledFont(
            key = "teko",
            displayName = "Teko",
            category = "Display",
            regularResId = R.font.teko_regular,
            boldResId = R.font.teko_bold,
            widthFactor = 0.72f
        ),
        BundledFont(
            key = "titan_one",
            displayName = "Titan One",
            category = "Display",
            regularResId = R.font.titan_one_regular,
            boldResId = null,
            widthFactor = 1.15f
        ),
        BundledFont(
            key = "monoton",
            displayName = "Monoton",
            category = "Display",
            regularResId = R.font.monoton_regular,
            boldResId = null,
            widthFactor = 1.4f
        ),
        BundledFont(
            key = "orbitron",
            displayName = "Orbitron",
            category = "Display",
            regularResId = R.font.orbitron_regular,
            boldResId = R.font.orbitron_bold,
            widthFactor = 1.25f
        ),
        BundledFont(
            key = "syne",
            displayName = "Syne",
            category = "Display",
            regularResId = R.font.syne_regular,
            boldResId = R.font.syne_bold,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "playfair_display",
            displayName = "Playfair Display",
            category = "Serif",
            regularResId = R.font.playfair_display_regular,
            boldResId = R.font.playfair_display_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "merriweather",
            displayName = "Merriweather",
            category = "Serif",
            regularResId = R.font.merriweather_regular,
            boldResId = R.font.merriweather_bold,
            widthFactor = 1.1f
        ),
        BundledFont(
            key = "lora",
            displayName = "Lora",
            category = "Serif",
            regularResId = R.font.lora_regular,
            boldResId = R.font.lora_bold,
            widthFactor = 1.02f
        ),
        BundledFont(
            key = "dm_serif_display",
            displayName = "DM Serif Display",
            category = "Serif",
            regularResId = R.font.dm_serif_display_regular,
            boldResId = null,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "cormorant_garamond",
            displayName = "Cormorant Garamond",
            category = "Serif",
            regularResId = R.font.cormorant_garamond_regular,
            boldResId = R.font.cormorant_garamond_bold,
            widthFactor = 0.92f
        ),
        BundledFont(
            key = "libre_baskerville",
            displayName = "Libre Baskerville",
            category = "Serif",
            regularResId = R.font.libre_baskerville_regular,
            boldResId = R.font.libre_baskerville_bold,
            widthFactor = 1.12f
        ),
        BundledFont(
            key = "eb_garamond",
            displayName = "EB Garamond",
            category = "Serif",
            regularResId = R.font.eb_garamond_regular,
            boldResId = R.font.eb_garamond_bold,
            widthFactor = 0.93f
        ),
        BundledFont(
            key = "cinzel",
            displayName = "Cinzel",
            category = "Serif",
            regularResId = R.font.cinzel_regular,
            boldResId = R.font.cinzel_bold,
            widthFactor = 1.2f
        ),
        BundledFont(
            key = "roboto_slab",
            displayName = "Roboto Slab",
            category = "Serif",
            regularResId = R.font.roboto_slab_regular,
            boldResId = R.font.roboto_slab_bold,
            widthFactor = 1.05f
        ),
        BundledFont(
            key = "prata",
            displayName = "Prata",
            category = "Serif",
            regularResId = R.font.prata_regular,
            boldResId = null,
            widthFactor = 1.08f
        ),
        BundledFont(
            key = "dancing_script",
            displayName = "Dancing Script",
            category = "Script",
            regularResId = R.font.dancing_script_regular,
            boldResId = R.font.dancing_script_bold,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "satisfy",
            displayName = "Satisfy",
            category = "Script",
            regularResId = R.font.satisfy_regular,
            boldResId = null,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "lobster",
            displayName = "Lobster",
            category = "Script",
            regularResId = R.font.lobster_regular,
            boldResId = null,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "sacramento",
            displayName = "Sacramento",
            category = "Script",
            regularResId = R.font.sacramento_regular,
            boldResId = null,
            widthFactor = 0.85f
        ),
        BundledFont(
            key = "kaushan_script",
            displayName = "Kaushan Script",
            category = "Script",
            regularResId = R.font.kaushan_script_regular,
            boldResId = null,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "parisienne",
            displayName = "Parisienne",
            category = "Script",
            regularResId = R.font.parisienne_regular,
            boldResId = null,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "caveat",
            displayName = "Caveat",
            category = "Hand",
            regularResId = R.font.caveat_regular,
            boldResId = R.font.caveat_bold,
            widthFactor = 0.85f
        ),
        BundledFont(
            key = "amatic_sc",
            displayName = "Amatic SC",
            category = "Hand",
            regularResId = R.font.amatic_sc_regular,
            boldResId = R.font.amatic_sc_bold,
            widthFactor = 0.7f
        ),
        BundledFont(
            key = "shadows_into_light",
            displayName = "Shadows Into Light",
            category = "Hand",
            regularResId = R.font.shadows_into_light_regular,
            boldResId = null,
            widthFactor = 0.95f
        ),
        BundledFont(
            key = "indie_flower",
            displayName = "Indie Flower",
            category = "Hand",
            regularResId = R.font.indie_flower_regular,
            boldResId = null,
            widthFactor = 1.0f
        ),
        BundledFont(
            key = "jetbrains_mono",
            displayName = "JetBrains Mono",
            category = "Mono",
            regularResId = R.font.jetbrains_mono_regular,
            boldResId = R.font.jetbrains_mono_bold,
            widthFactor = 1.2f
        ),
        BundledFont(
            key = "ibm_plex_mono",
            displayName = "IBM Plex Mono",
            category = "Mono",
            regularResId = R.font.ibm_plex_mono_regular,
            boldResId = R.font.ibm_plex_mono_bold,
            widthFactor = 1.2f
        ),
        BundledFont(
            key = "silkscreen",
            displayName = "Silkscreen",
            category = "Pixel",
            regularResId = R.font.silkscreen_regular,
            boldResId = R.font.silkscreen_bold,
            widthFactor = 1.3f
        ),
        BundledFont(
            key = "vt323",
            displayName = "VT323",
            category = "Pixel",
            regularResId = R.font.vt323_regular,
            boldResId = null,
            widthFactor = 0.9f
        )
    )

    private val fontsByKey: Map<String, BundledFont> = fonts.associateBy { it.key }
    private val customFontsMap = ConcurrentHashMap<String, BundledFont>()
    private val deviceFontsMap = ConcurrentHashMap<String, BundledFont>()
    private val deviceFontsLock = Mutex()
    private val baseResourceCache = ConcurrentHashMap<Int, Typeface>()
    private val styledTypefaceCache = ConcurrentHashMap<StyledFontCacheKey, Typeface>()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun allFonts(): List<BundledFont> {
        val customSorted = customFontsMap.values.sortedBy { it.displayName.lowercase() }
        val deviceSorted = deviceFontsMap.values.sortedBy { it.displayName.lowercase() }
        return customSorted + deviceSorted + fonts
    }

    fun find(fontKey: String): BundledFont {
        return customFontsMap[fontKey] ?: deviceFontsMap[fontKey] ?: fontsByKey[fontKey] ?: fonts.first()
    }

    fun widthFactor(fontKey: String): Float = find(fontKey).widthFactor

    fun resolveComposeFontFamily(fontKey: String): FontFamily {
        val entry = find(fontKey)
        if (entry.customFile != null && entry.customFile.exists()) {
            val tf = runCatching { Typeface.createFromFile(entry.customFile) }.getOrNull()
            if (tf != null) {
                return FontFamily(tf)
            }
        }
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

    /**
     * Resolves and caches the styled [Typeface] for `(fontKey, isBold, isItalic)` so per-frame
     * rendering never invokes `Typeface.create(base, style)` repeatedly (fixes Bug 2.19).
     */
    fun resolveTypeface(
        context: Context,
        fontKey: String,
        isBold: Boolean,
        isItalic: Boolean
    ): Typeface {
        val cacheKey = StyledFontCacheKey(fontKey, isBold, isItalic)
        styledTypefaceCache[cacheKey]?.let { return it }

        val entry = find(fontKey)
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        val resolved: Typeface = when {
            entry.customFile != null && entry.customFile.exists() -> {
                val base = runCatching { Typeface.createFromFile(entry.customFile) }.getOrNull()
                    ?: Typeface.DEFAULT
                if (style == Typeface.NORMAL) base else Typeface.create(base, style)
            }
            else -> {
                val resId = if (isBold && entry.boldResId != null) entry.boldResId else entry.regularResId
                if (resId == null) {
                    Typeface.create(Typeface.DEFAULT, style)
                } else {
                    val base = baseResourceCache.getOrPut(resId) {
                        runCatching { ResourcesCompat.getFont(context, resId) }.getOrNull() ?: Typeface.DEFAULT
                    }
                    if (style == Typeface.NORMAL || (style == Typeface.BOLD && entry.boldResId != null)) {
                        base
                    } else {
                        Typeface.create(base, style)
                    }
                }
            }
        }

        styledTypefaceCache[cacheKey] = resolved
        return resolved
    }

    /** Preloads default preset fonts on a background thread during startup so frame 1 never janks. */
    suspend fun preloadStarterFonts(context: Context) = withContext(Dispatchers.IO) {
        loadSavedCustomFonts(context)
        runCatching { loadDeviceFonts(context) }
        val starterKeys = listOf("poppins", "archivo_black", "anton", "bebas_neue", "russo_one")
        for (key in starterKeys) {
            runCatching { resolveTypeface(context, key, isBold = false, isItalic = false) }
        }
    }

    suspend fun loadSavedCustomFonts(context: Context): List<BundledFont> = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "fonts").apply { if (!exists()) mkdirs() }
        val indexFile = File(dir, "fonts_index.json")
        if (!indexFile.exists()) return@withContext emptyList()

        val entries = runCatching {
            json.decodeFromString<List<CustomFontEntry>>(indexFile.readText())
        }.getOrDefault(emptyList())

        customFontsMap.clear()
        for (item in entries) {
            val f = File(dir, item.fileName)
            if (f.exists()) {
                customFontsMap[item.key] = BundledFont(
                    key = item.key,
                    displayName = item.displayName,
                    category = "Imported",
                    customFile = f
                )
            }
        }
        allFonts()
    }

    /** Imports a user `.ttf` or `.otf` font via SAF into `files/fonts/` and registers it immediately. */
    suspend fun importCustomFont(context: Context, uri: Uri): Result<BundledFont> = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "fonts").apply { if (!exists()) mkdirs() }
            val rawName = queryDisplayName(context, uri) ?: "CustomFont.ttf"
            val ext = rawName.substringAfterLast('.', "ttf").lowercase()
            require(ext == "ttf" || ext == "otf") { "Please choose a .ttf or .otf font file" }

            val cleanDisplay = rawName.substringBeforeLast('.')
                .replace(Regex("[_-]+"), " ")
                .trim()
                .ifBlank { "Custom Font" }

            val id = "custom_${UUID.randomUUID().toString().take(8)}"
            val destFileName = "$id.$ext"
            val destFile = File(dir, destFileName)
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("Cannot read font file")
            DraftStore.writeAtomically(destFile, bytes)

            // Verify Android can parse the font before registering
            val verifiedTypeface = Typeface.createFromFile(destFile)
            check(verifiedTypeface != Typeface.DEFAULT) { "Unsupported or corrupted font file" }

            val indexFile = File(dir, "fonts_index.json")
            val existing = if (indexFile.exists()) {
                runCatching { json.decodeFromString<List<CustomFontEntry>>(indexFile.readText()) }
                    .getOrDefault(emptyList())
            } else {
                emptyList()
            }
            val updated = existing + CustomFontEntry(
                key = id,
                displayName = cleanDisplay,
                fileName = destFileName
            )
            DraftStore.writeAtomically(indexFile, json.encodeToString(updated).toByteArray(Charsets.UTF_8))

            val registered = BundledFont(
                key = id,
                displayName = cleanDisplay,
                category = "Imported",
                customFile = destFile
            )
            customFontsMap[id] = registered
            styledTypefaceCache[StyledFontCacheKey(id, false, false)] = verifiedTypeface
            registered
        }
    }

    // --- Device fonts: the user's /sdcard/Fonts/ folder ---
    //
    // Android 11+ won't let an app read .ttf/.otf files in shared storage without
    // "All files access", which Play rejects. Instead the user grants the folder once
    // through the system picker (opened at /sdcard/Fonts/); the grant is persisted and
    // the folder is re-scanned on every launch. Fonts are copied into app storage so
    // Typeface.createFromFile and the existing custom-font path work unchanged.

    const val DEVICE_CATEGORY = "Device"
    private const val DEVICE_PREFS = "device_fonts"
    private const val PREF_TREE_URI = "tree_uri"
    private const val PREF_ASKED = "asked"
    private const val DEVICE_KEY_PREFIX = "device_"

    /** Where the folder picker opens: /sdcard/Fonts/ on primary storage. */
    val deviceFontsInitialUri: Uri
        get() = DocumentsContract.buildDocumentUri(
        "com.android.externalstorage.documents",
        "primary:Fonts"
    )

    private fun devicePrefs(context: Context) =
        context.getSharedPreferences(DEVICE_PREFS, Context.MODE_PRIVATE)

    private fun grantedDeviceFontsTree(context: Context): Uri? {
        val saved = devicePrefs(context).getString(PREF_TREE_URI, null)?.let(Uri::parse) ?: return null
        val stillGranted = context.contentResolver.persistedUriPermissions
            .any { it.uri == saved && it.isReadPermission }
        return saved.takeIf { stillGranted }
    }

    /** True once per install until the user has granted (or been offered) the fonts folder. */
    fun shouldAskForDeviceFontsFolder(context: Context): Boolean =
        !devicePrefs(context).getBoolean(PREF_ASKED, false) && grantedDeviceFontsTree(context) == null

    fun markDeviceFontsFolderAsked(context: Context) {
        devicePrefs(context).edit().putBoolean(PREF_ASKED, true).apply()
    }

    /** Persists the folder the user picked and scans it. Returns the number of fonts found. */
    suspend fun setDeviceFontsFolder(context: Context, treeUri: Uri): Int = withContext(Dispatchers.IO) {
        context.contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        devicePrefs(context).edit()
            .putString(PREF_TREE_URI, treeUri.toString())
            .putBoolean(PREF_ASKED, true)
            .apply()
        loadDeviceFonts(context)
    }

    /** Re-scans the granted fonts folder. Returns the number of device fonts registered. */
    suspend fun loadDeviceFonts(context: Context): Int = withContext(Dispatchers.IO) {
        deviceFontsLock.withLock { scanDeviceFonts(context) }
    }

    private fun scanDeviceFonts(context: Context): Int {
        val tree = grantedDeviceFontsTree(context) ?: return deviceFontsMap.size
        val cacheDir = File(context.filesDir, "device_fonts").apply { if (!exists()) mkdirs() }
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            tree,
            DocumentsContract.getTreeDocumentId(tree)
        )
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE
        )

        val found = HashMap<String, BundledFont>()
        context.contentResolver.query(childrenUri, projection, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val docId = c.getString(0) ?: continue
                val name = c.getString(1) ?: continue
                val size = if (c.isNull(2)) -1L else c.getLong(2)
                val ext = name.substringAfterLast('.', "").lowercase()
                if (ext != "ttf" && ext != "otf") continue

                val base = name.substringBeforeLast('.')
                // Stable key from the file name so saved designs keep pointing at the same font.
                val key = DEVICE_KEY_PREFIX + base.lowercase().replace(Regex("[^a-z0-9]+"), "_") + "_" + ext
                val dest = File(cacheDir, "$key.$ext")
                val upToDate = dest.exists() && (size < 0 || dest.length() == size)
                if (!upToDate) {
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)
                    val bytes = runCatching {
                        context.contentResolver.openInputStream(docUri)?.use { it.readBytes() }
                    }.getOrNull() ?: continue
                    DraftStore.writeAtomically(dest, bytes)
                }
                val parses = runCatching { Typeface.createFromFile(dest) != Typeface.DEFAULT }.getOrDefault(false)
                if (!parses) {
                    dest.delete()
                    continue
                }
                found[key] = BundledFont(
                    key = key,
                    displayName = base.replace(Regex("[_-]+"), " ").trim().ifBlank { base },
                    category = DEVICE_CATEGORY,
                    customFile = dest
                )
            }
        }

        // Drop copies of fonts that were removed from the folder.
        val keepNames = found.values.mapNotNull { it.customFile?.name }.toSet()
        cacheDir.listFiles()?.filter { it.name !in keepNames }?.forEach { it.delete() }

        deviceFontsMap.clear()
        deviceFontsMap.putAll(found)
        styledTypefaceCache.keys.removeAll { it.fontKey.startsWith(DEVICE_KEY_PREFIX) }
        return deviceFontsMap.size
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                uri.lastPathSegment
            }
        } catch (_: Exception) {
            uri.lastPathSegment
        } finally {
            cursor?.close()
        }
    }
}
