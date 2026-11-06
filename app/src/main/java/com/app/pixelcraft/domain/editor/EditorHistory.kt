package com.app.pixelcraft.domain.editor

data class HistoryEntry<T>(
    val label: String,
    val snapshot: T,
    val timestampMs: Long,
    val coalesceTag: String? = null
)

/**
 * Bounded O(1) undo/redo history backed by [ArrayDeque], supporting:
 * - Action labels for tooltips & the interactive History Sheet
 * - Continuous gesture/slider edit coalescing (`continuous = true`)
 * - Rapid sequential action coalescing (e.g. repeated nudges within [coalesceWindowMs] = 500ms)
 */
class EditorHistory<T>(
    private val limit: Int = 50,
    private val coalesceWindowMs: Long = 500L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val undoDeque = ArrayDeque<HistoryEntry<T>>(limit + 1)
    private val redoDeque = ArrayDeque<HistoryEntry<T>>(limit + 1)
    private var isContinuousActive = false

    val canUndo: Boolean get() = undoDeque.isNotEmpty()
    val canRedo: Boolean get() = redoDeque.isNotEmpty()
    val undoSize: Int get() = undoDeque.size
    val redoSize: Int get() = redoDeque.size

    val lastUndoLabel: String? get() = undoDeque.lastOrNull()?.label
    val lastRedoLabel: String? get() = redoDeque.lastOrNull()?.label

    fun undoEntries(): List<HistoryEntry<T>> = undoDeque.toList()

    fun beginStep(
        currentSnapshot: T,
        label: String = "Edit",
        continuous: Boolean = false,
        coalesceTag: String? = null
    ) {
        val now = clock()
        if (continuous) {
            if (!isContinuousActive) {
                isContinuousActive = true
                pushUndo(HistoryEntry(label = label, snapshot = currentSnapshot, timestampMs = now))
                redoDeque.clear()
            }
            return
        }

        isContinuousActive = false
        if (coalesceTag != null) {
            val previous = undoDeque.lastOrNull()
            if (previous != null &&
                previous.coalesceTag == coalesceTag &&
                (now - previous.timestampMs) in 0..coalesceWindowMs
            ) {
                // Update timestamp to extend rapid-nudge window while keeping original pre-nudge snapshot
                undoDeque.removeLast()
                undoDeque.addLast(previous.copy(timestampMs = now))
                redoDeque.clear()
                return
            }
        }

        pushUndo(
            HistoryEntry(
                label = label,
                snapshot = currentSnapshot,
                timestampMs = now,
                coalesceTag = coalesceTag
            )
        )
        redoDeque.clear()
    }

    fun finishContinuousEdit() {
        isContinuousActive = false
    }

    fun clear() {
        isContinuousActive = false
        undoDeque.clear()
        redoDeque.clear()
    }

    fun undo(currentSnapshot: T): T? {
        isContinuousActive = false
        if (undoDeque.isEmpty()) return null
        val target = undoDeque.removeLast()
        pushRedo(HistoryEntry(label = target.label, snapshot = currentSnapshot, timestampMs = clock()))
        return target.snapshot
    }

    fun redo(currentSnapshot: T): T? {
        isContinuousActive = false
        if (redoDeque.isEmpty()) return null
        val target = redoDeque.removeLast()
        pushUndo(HistoryEntry(label = target.label, snapshot = currentSnapshot, timestampMs = clock()))
        return target.snapshot
    }

    /** Jumps back to [undoIndex] (`0` = oldest entry, `undoDeque.lastIndex` = most recent undo step). */
    fun jumpToUndoIndex(undoIndex: Int, currentSnapshot: T): T? {
        if (undoIndex !in undoDeque.indices) return null
        var active = currentSnapshot
        while (undoDeque.size > undoIndex) {
            active = undo(active) ?: break
        }
        return active
    }

    private fun pushUndo(entry: HistoryEntry<T>) {
        if (undoDeque.size >= limit) {
            undoDeque.removeFirst()
        }
        undoDeque.addLast(entry)
    }

    private fun pushRedo(entry: HistoryEntry<T>) {
        if (redoDeque.size >= limit) {
            redoDeque.removeFirst()
        }
        redoDeque.addLast(entry)
    }
}
