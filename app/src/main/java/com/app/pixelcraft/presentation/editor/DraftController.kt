package com.app.pixelcraft.presentation.editor

import com.app.pixelcraft.data.model.DraftPayload
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.domain.editor.LayerOperations.normalizeZIndices
import com.app.pixelcraft.presentation.ActiveSubTool
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Debounced crash-recovery autosave of the open design plus the "resume draft" flow. */
internal class DraftController(
    private val host: EditorHost,
    private val draftStore: DraftStore?
) {
    private var autosaveJob: Job? = null

    fun cancelAutosave() {
        autosaveJob?.cancel()
    }

    fun checkRecoverableDraft() {
        val ds = draftStore ?: return
        host.scope.launch {
            val loaded = ds.load()
            if (loaded != null && loaded.isDirty) {
                host.state.update {
                    it.copy(
                        hasRecoverableDraft = true,
                        draftTimestamp = loaded.timestamp,
                        draftProjectName = loaded.currentProjectName
                    )
                }
            }
        }
    }

    fun scheduleAutosave() {
        val ds = draftStore ?: return
        autosaveJob?.cancel()
        autosaveJob = host.scope.launch {
            delay(AUTOSAVE_DEBOUNCE_MS)
            val state = host.state.value
            if (!state.isDirty) return@launch
            val now = System.currentTimeMillis()
            val payload = DraftPayload(
                timestamp = now,
                currentProjectId = state.currentProjectId,
                currentProjectName = state.currentProjectName,
                isDirty = true,
                snapshot = host.takeSnapshot()
            )
            ds.save(payload).onSuccess {
                host.state.update {
                    it.copy(
                        lastAutosavedAt = now,
                        hasRecoverableDraft = true,
                        draftTimestamp = now,
                        draftProjectName = state.currentProjectName
                    )
                }
            }
        }
    }

    fun resumeSavedDraft() {
        val ds = draftStore ?: return
        host.scope.launch {
            val draft = ds.load() ?: return@launch
            host.clearHistory()
            host.endEditingSession()
            val normalized = draft.snapshot.layers.map { TextSizing.withEstimatedBounds(it) }.normalizeZIndices()
            val validSelected = draft.snapshot.selectedLayerId
                ?.takeIf { sel -> normalized.any { it.id == sel } }
                ?: normalized.maxByOrNull { it.zIndex }?.id
            host.state.update {
                it.copy(
                    canvasConfig = draft.snapshot.canvasConfig,
                    layers = normalized,
                    selectedLayerId = validSelected,
                    selectedLayerIds = setOfNotNull(validSelected),
                    currentProjectId = draft.currentProjectId,
                    currentProjectName = draft.currentProjectName,
                    isDirty = true,
                    activeSubTool = ActiveSubTool.NONE,
                    activeDialog = null
                )
            }
            host.syncHistoryFlags(markDirty = null)
        }
    }

    fun discardRecoverableDraft() {
        autosaveJob?.cancel()
        host.scope.launch {
            draftStore?.clear()
            host.state.update {
                it.copy(
                    hasRecoverableDraft = false,
                    draftTimestamp = null,
                    draftProjectName = null,
                    lastAutosavedAt = null
                )
            }
        }
    }

    fun markCleanAndDiscardDraft() {
        autosaveJob?.cancel()
        host.state.update { it.copy(isDirty = false, activeDialog = null) }
        host.scope.launch {
            draftStore?.clear()
            host.state.update {
                it.copy(
                    hasRecoverableDraft = false,
                    draftTimestamp = null,
                    draftProjectName = null
                )
            }
        }
    }

    private companion object {
        const val AUTOSAVE_DEBOUNCE_MS = 1500L
    }
}
