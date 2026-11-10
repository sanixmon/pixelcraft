package com.app.pixelcraft.presentation.editor

import android.graphics.Bitmap
import android.net.Uri
import com.app.pixelcraft.R
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.GallerySaver
import com.app.pixelcraft.domain.ImageExporter
import com.app.pixelcraft.domain.UserPreferencesStore
import com.app.pixelcraft.presentation.PixelCraftUiState
import com.app.pixelcraft.presentation.i18n.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Renders the design to an image and either saves it to the gallery or hands it to the share sheet. */
internal class ExportController(
    private val host: EditorHost,
    private val imageExporter: ImageExporter,
    private val gallerySaver: GallerySaver?,
    private val preferencesStore: UserPreferencesStore
) {
    private var exportJob: Job? = null

    fun setExportOptions(options: ExportOptions) {
        host.state.update { it.copy(exportOptions = options) }
        host.scope.launch {
            preferencesStore.saveExportPreferences(options.format.name, options.qualityPreset.name)
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        host.state.update { it.copy(isExporting = false, userMessage = UiText.Res(R.string.msg_export_cancelled)) }
    }

    fun exportCanvas() {
        val saver = gallerySaver ?: return
        val state = host.state.value
        runExport(
            state = state,
            options = state.exportOptions,
            selectedLayerIds = state.selectedLayerIds.ifEmpty { setOfNotNull(state.selectedLayerId) },
            encode = { bitmap -> saver.save(bitmap, state.exportOptions, baseName(state, "")).getOrThrow() },
            onSuccess = { uri ->
                copy(
                    lastExportedUri = uri,
                    userMessage = UiText.Res(R.string.msg_saved_to_gallery, GallerySaver.ALBUM),
                    activeDialog = null
                )
            },
            onFailure = { err ->
                copy(lastExportedUri = null, userMessage = UiText.Res(R.string.msg_export_failed, err.message.orEmpty()))
            }
        )
    }

    /**
     * Renders to `cacheDir/share/` via `FileProvider` and emits `pendingShareUri` so the user can share
     * straight to other apps without adding the image to the gallery.
     */
    fun shareCanvasDirectly() {
        val saver = gallerySaver ?: return
        val state = host.state.value
        val quickOptions = state.exportOptions.copy(selectedLayerOnly = false)
        runExport(
            state = state,
            options = quickOptions,
            selectedLayerIds = emptySet(),
            encode = { bitmap -> saver.saveToShareCache(bitmap, quickOptions, baseName(state, "_share")).getOrThrow() },
            onSuccess = { uri -> copy(pendingShareUri = uri, pendingShareMimeType = quickOptions.format.mimeType) },
            onFailure = { err -> copy(userMessage = UiText.Res(R.string.msg_share_failed, err.message.orEmpty())) }
        )
    }

    private fun runExport(
        state: PixelCraftUiState,
        options: ExportOptions,
        selectedLayerIds: Set<String>,
        encode: suspend (Bitmap) -> Uri,
        onSuccess: PixelCraftUiState.(Uri) -> PixelCraftUiState,
        onFailure: PixelCraftUiState.(Throwable) -> PixelCraftUiState
    ) {
        exportJob?.cancel()
        host.state.update { it.copy(isExporting = true) }
        exportJob = host.scope.launch {
            try {
                val bitmap = imageExporter.exportCanvasToBitmap(
                    canvasConfig = state.canvasConfig,
                    layers = state.layers,
                    options = options,
                    selectedLayerIds = selectedLayerIds
                )
                val uri = try {
                    encode(bitmap)
                } finally {
                    bitmap.recycle()
                }
                host.state.update { it.copy(isExporting = false).onSuccess(uri) }
            } catch (ce: CancellationException) {
                throw ce
            } catch (err: Throwable) {
                host.state.update { it.copy(isExporting = false).onFailure(err) }
            }
        }
    }

    private fun baseName(state: PixelCraftUiState, suffix: String): String {
        val sanitized = state.currentProjectName
            .replace("[^A-Za-z0-9_-]+".toRegex(), "_")
            .trim('_')
            .ifBlank { "PixelCraft" }
        return "${sanitized}${suffix}_${System.currentTimeMillis()}"
    }
}
