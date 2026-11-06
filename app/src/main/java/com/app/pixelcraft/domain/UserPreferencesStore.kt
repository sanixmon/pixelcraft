package com.app.pixelcraft.domain

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.pixelcraft.data.model.SavedLayerStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.pixelCraftDataStore by preferencesDataStore(name = "pixelcraft_preferences")

data class UserPreferencesSnapshot(
    val lastExportFormat: String = "PNG",
    val lastExportQuality: String = "MEDIUM",
    val recentColors: List<Long> = emptyList(),
    val favoriteFontKeys: Set<String> = emptySet(),
    val savedStyles: List<SavedLayerStyle> = emptyList(),
    val onboardingDismissed: Boolean = false
)

class UserPreferencesStore(private val context: Context?) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    val preferencesFlow: Flow<UserPreferencesSnapshot>? = context?.pixelCraftDataStore?.data?.map { prefs ->
        val recentColors = prefs[KEY_RECENT_COLORS]
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()
        val savedStyles = prefs[KEY_SAVED_STYLES]?.let { raw ->
            runCatching { json.decodeFromString<List<SavedLayerStyle>>(raw) }.getOrDefault(emptyList())
        } ?: emptyList()

        UserPreferencesSnapshot(
            lastExportFormat = prefs[KEY_EXPORT_FORMAT] ?: "PNG",
            lastExportQuality = prefs[KEY_EXPORT_QUALITY] ?: "MEDIUM",
            recentColors = recentColors,
            favoriteFontKeys = prefs[KEY_FAVORITE_FONTS] ?: emptySet(),
            savedStyles = savedStyles,
            onboardingDismissed = prefs[KEY_ONBOARDING_DISMISSED] ?: false
        )
    }

    suspend fun saveExportPreferences(formatName: String, qualityName: String) {
        val ds = context?.pixelCraftDataStore ?: return
        ds.edit { prefs ->
            prefs[KEY_EXPORT_FORMAT] = formatName
            prefs[KEY_EXPORT_QUALITY] = qualityName
        }
    }

    suspend fun recordRecentColor(colorArgb: Long) {
        val ds = context?.pixelCraftDataStore ?: return
        val normalized = colorArgb and 0xFFFFFFFFL
        ds.edit { prefs ->
            val current = prefs[KEY_RECENT_COLORS]
                ?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                .orEmpty()
                .filterNot { it == normalized }
            val updated = (listOf(normalized) + current).take(MAX_RECENT_COLORS)
            prefs[KEY_RECENT_COLORS] = updated.joinToString(",")
        }
    }

    suspend fun toggleFavoriteFont(fontKey: String) {
        val ds = context?.pixelCraftDataStore ?: return
        ds.edit { prefs ->
            val current = prefs[KEY_FAVORITE_FONTS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(fontKey)) {
                current.remove(fontKey)
            }
            prefs[KEY_FAVORITE_FONTS] = current
        }
    }

    suspend fun saveLayerStyle(style: SavedLayerStyle) {
        val ds = context?.pixelCraftDataStore ?: return
        ds.edit { prefs ->
            val existing = prefs[KEY_SAVED_STYLES]?.let {
                runCatching { json.decodeFromString<List<SavedLayerStyle>>(it) }.getOrDefault(emptyList())
            }.orEmpty().filterNot { it.id == style.id }
            val updated = (listOf(style) + existing).take(30)
            prefs[KEY_SAVED_STYLES] = json.encodeToString(updated)
        }
    }

    suspend fun deleteLayerStyle(styleId: String) {
        val ds = context?.pixelCraftDataStore ?: return
        ds.edit { prefs ->
            val existing = prefs[KEY_SAVED_STYLES]?.let {
                runCatching { json.decodeFromString<List<SavedLayerStyle>>(it) }.getOrDefault(emptyList())
            }.orEmpty().filterNot { it.id == styleId }
            prefs[KEY_SAVED_STYLES] = json.encodeToString(existing)
        }
    }

    suspend fun dismissOnboarding() {
        val ds = context?.pixelCraftDataStore ?: return
        ds.edit { prefs ->
            prefs[KEY_ONBOARDING_DISMISSED] = true
        }
    }

    companion object {
        private const val MAX_RECENT_COLORS = 12
        private val KEY_EXPORT_FORMAT = stringPreferencesKey("export_format")
        private val KEY_EXPORT_QUALITY = stringPreferencesKey("export_quality")
        private val KEY_RECENT_COLORS = stringPreferencesKey("recent_colors")
        private val KEY_FAVORITE_FONTS = stringSetPreferencesKey("favorite_fonts")
        private val KEY_SAVED_STYLES = stringPreferencesKey("saved_styles")
        private val KEY_ONBOARDING_DISMISSED = booleanPreferencesKey("onboarding_dismissed")
    }
}
