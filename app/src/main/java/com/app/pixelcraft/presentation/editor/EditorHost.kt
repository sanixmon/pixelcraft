package com.app.pixelcraft.presentation.editor

import androidx.annotation.StringRes
import com.app.pixelcraft.data.model.EditorSnapshot
import com.app.pixelcraft.presentation.PixelCraftUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The narrow slice of [com.app.pixelcraft.presentation.PixelCraftViewModel] that its feature
 * controllers (drafts, projects, export) are allowed to touch. Keeps the undo history and
 * gesture tracking private to the ViewModel while letting each controller own its workflow.
 */
internal interface EditorHost {
    val state: MutableStateFlow<PixelCraftUiState>
    val scope: CoroutineScope

    fun takeSnapshot(): EditorSnapshot

    /** Records a discrete undo step labelled [label] without touching the dirty flag. */
    fun recordHistoryStep(label: String)

    /** Ends any in-flight continuous edit or gesture before the document is swapped out. */
    fun endEditingSession()

    fun clearHistory()

    fun syncHistoryFlags(markDirty: Boolean?)

    fun string(@StringRes id: Int, fallback: String, vararg args: Any): String
}
