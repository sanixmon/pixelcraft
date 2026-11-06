package com.app.pixelcraft.data.presets

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.FontRes
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.res.ResourcesCompat
import com.app.pixelcraft.R
import com.app.pixelcraft.domain.DraftStore
import kotlinx.coroutines.Dispatchers
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
        )
    )

    private val fontsByKey: Map<String, BundledFont> = fonts.associateBy { it.key }
    private val customFontsMap = ConcurrentHashMap<String, BundledFont>()
    private val baseResourceCache = ConcurrentHashMap<Int, Typeface>()
    private val styledTypefaceCache = ConcurrentHashMap<StyledFontCacheKey, Typeface>()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun allFonts(): List<BundledFont> {
        val customSorted = customFontsMap.values.sortedBy { it.displayName.lowercase() }
        return customSorted + fonts
    }

    fun find(fontKey: String): BundledFont {
        return customFontsMap[fontKey] ?: fontsByKey[fontKey] ?: fonts.first()
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
