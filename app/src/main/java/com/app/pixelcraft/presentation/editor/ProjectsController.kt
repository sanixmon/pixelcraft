package com.app.pixelcraft.presentation.editor

import android.content.Context
import android.net.Uri
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ProjectSummary
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.CanvasRenderer
import com.app.pixelcraft.domain.DraftStore
import com.app.pixelcraft.domain.ImageStore
import com.app.pixelcraft.domain.ProjectStorage
import com.app.pixelcraft.domain.TextSizing
import com.app.pixelcraft.domain.editor.LayerOperations.normalizeZIndices
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.PixelCraftViewModel.Companion.MAX_CANVAS_DIMENSION
import com.app.pixelcraft.presentation.PixelCraftViewModel.Companion.MIN_CANVAS_DIMENSION
import com.app.pixelcraft.presentation.i18n.UiText
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/** Saved-project gallery: save, open, rename, duplicate, delete (with undo) and `.pixelcraft` archives. */
internal class ProjectsController(
    private val host: EditorHost,
    private val projectStorage: ProjectStorage,
    private val imageStore: ImageStore,
    private val draftStore: DraftStore?,
    private val drafts: DraftController,
    private val appContext: () -> Context?
) {
    private var recentlyDeletedProject: ProjectData? = null

    private suspend fun reloadSavedProjects(): Pair<List<ProjectData>, List<ProjectSummary>> {
        val projects = projectStorage.listProjects().getOrElse { host.state.value.savedProjects }
        val summaries = projectStorage.listProjectSummaries().getOrElse {
            projects.map { it.toSummary() }
        }
        return projects to summaries
    }

    private suspend fun publishProjectList(message: UiText? = null) {
        val (projects, summaries) = reloadSavedProjects()
        host.state.update {
            it.copy(
                savedProjects = projects,
                projectSummaries = summaries,
                userMessage = message ?: it.userMessage
            )
        }
    }

    private fun reportFailure(messageRes: Int, err: Throwable) {
        host.state.update { it.copy(userMessage = UiText.Res(messageRes, err.message.orEmpty())) }
    }

    fun refreshSavedProjects() {
        host.scope.launch { publishProjectList() }
    }

    fun saveProject(name: String, onSaved: (() -> Unit)? = null) {
        val state = host.state.value
        val cleanName = name.trim().ifBlank { host.string(R.string.ui_untitled_project, "Untitled Project") }
        val existingId = state.currentProjectId?.takeIf { cleanName == state.currentProjectName }
        val id = existingId ?: UUID.randomUUID().toString()
        val snapshot = host.takeSnapshot()
        val project = ProjectData(
            id = id,
            name = cleanName,
            timestamp = System.currentTimeMillis(),
            snapshot = snapshot
        )
        drafts.cancelAutosave()

        host.scope.launch {
            val thumbBitmap = runCatching {
                appContext()?.let { ctx ->
                    CanvasRenderer.renderThumbnail(
                        config = snapshot.canvasConfig,
                        layers = snapshot.layers,
                        bitmapLookup = { imageStore.loadPreviewBitmap(it) },
                        typefaceResolver = { k, b, i -> FontCatalog.resolveTypeface(ctx, k, b, i) },
                        maxSide = 256
                    )
                }
            }.getOrNull()

            projectStorage.saveProject(project, thumbBitmap).onSuccess {
                thumbBitmap?.recycle()
                draftStore?.clear()
                val (projects, summaries) = reloadSavedProjects()
                host.state.update {
                    it.copy(
                        currentProjectId = id,
                        currentProjectName = cleanName,
                        savedProjects = projects,
                        projectSummaries = summaries,
                        isDirty = false,
                        hasRecoverableDraft = false,
                        draftTimestamp = null,
                        draftProjectName = null,
                        activeDialog = null,
                        userMessage = UiText.Res(R.string.msg_project_saved, cleanName)
                    )
                }
                onSaved?.invoke()
            }.onFailure { err ->
                thumbBitmap?.recycle()
                reportFailure(R.string.msg_project_save_failed, err)
            }
        }
    }

    fun loadProject(id: String) {
        host.endEditingSession()
        drafts.cancelAutosave()
        host.scope.launch {
            projectStorage.loadProject(id).onSuccess { project ->
                host.recordHistoryStep("Open Project")
                val normalized = project.snapshot.layers
                    .map { TextSizing.withEstimatedBounds(it) }
                    .normalizeZIndices()
                val validSelectedId = project.snapshot.selectedLayerId
                    ?.takeIf { sel -> normalized.any { it.id == sel } }
                    ?: normalized.maxByOrNull { it.zIndex }?.id
                val config = project.snapshot.canvasConfig
                host.state.update {
                    it.copy(
                        currentProjectId = project.id,
                        currentProjectName = project.name,
                        canvasConfig = config.copy(
                            width = config.width.coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION),
                            height = config.height.coerceIn(MIN_CANVAS_DIMENSION, MAX_CANVAS_DIMENSION)
                        ),
                        layers = normalized,
                        selectedLayerId = validSelectedId,
                        selectedLayerIds = setOfNotNull(validSelectedId),
                        isDirty = false,
                        activeDialog = null,
                        activeSubTool = ActiveSubTool.NONE,
                        activeVerticalGuideX = null,
                        activeHorizontalGuideY = null
                    )
                }
                host.syncHistoryFlags(markDirty = false)
            }.onFailure { err ->
                reportFailure(R.string.msg_project_open_failed, err)
            }
        }
    }

    fun renameSavedProject(id: String, newName: String) {
        host.scope.launch {
            projectStorage.renameProject(id, newName).onSuccess { updated ->
                host.state.update { state ->
                    state.copy(
                        currentProjectName = if (state.currentProjectId == id) updated.name else state.currentProjectName
                    )
                }
                publishProjectList(UiText.Res(R.string.msg_project_renamed, updated.name))
            }
        }
    }

    fun duplicateSavedProject(id: String) {
        host.scope.launch {
            projectStorage.duplicateProject(id).onSuccess { copy ->
                publishProjectList(UiText.Res(R.string.msg_project_duplicated, copy.name))
            }
        }
    }

    fun deleteProject(id: String) {
        host.scope.launch {
            val backup = projectStorage.loadProject(id).getOrNull()
            projectStorage.deleteProject(id).onSuccess {
                recentlyDeletedProject = backup
                host.state.update { state ->
                    state.copy(currentProjectId = if (state.currentProjectId == id) null else state.currentProjectId)
                }
                publishProjectList()
            }.onFailure { err ->
                reportFailure(R.string.msg_project_delete_failed, err)
            }
        }
    }

    fun undoDeleteProject() {
        val backup = recentlyDeletedProject ?: return
        recentlyDeletedProject = null
        host.scope.launch {
            projectStorage.saveProject(backup).onSuccess {
                publishProjectList(UiText.Res(R.string.msg_project_restored, backup.name))
            }
        }
    }

    fun exportProjectArchive(projectId: String, destinationUri: Uri) {
        host.scope.launch {
            runCatching {
                val resolver = requireNotNull(appContext()).contentResolver
                resolver.openOutputStream(destinationUri)?.use { out ->
                    projectStorage.exportProjectArchive(projectId, imageStore.imagesDir, out).getOrThrow()
                } ?: error("Cannot write destination file")
            }.onSuccess {
                host.state.update { it.copy(userMessage = UiText.Res(R.string.msg_archive_exported)) }
            }.onFailure { err ->
                reportFailure(R.string.msg_archive_export_failed, err)
            }
        }
    }

    fun importProjectArchive(sourceUri: Uri, onImported: ((String) -> Unit)? = null) {
        host.scope.launch {
            runCatching {
                val resolver = requireNotNull(appContext()).contentResolver
                resolver.openInputStream(sourceUri)?.use { input ->
                    projectStorage.importProjectArchive(input, imageStore.imagesDir).getOrThrow()
                } ?: error("Cannot open .pixelcraft file")
            }.onSuccess { imported ->
                publishProjectList(UiText.Res(R.string.msg_archive_imported, imported.name))
                onImported?.invoke(imported.id)
            }.onFailure { err ->
                reportFailure(R.string.msg_archive_import_failed, err)
            }
        }
    }
}
