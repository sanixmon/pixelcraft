package com.app.pixelcraft.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

const val CURRENT_PROJECT_SCHEMA_VERSION = 2

@Immutable
@Serializable
data class EditorSnapshot(
    val layers: List<Layer>,
    val canvasConfig: CanvasConfig,
    val selectedLayerId: String?
)

@Immutable
@Serializable
data class ProjectData(
    val id: String,
    val name: String,
    val timestamp: Long,
    val snapshot: EditorSnapshot,
    val schemaVersion: Int = CURRENT_PROJECT_SCHEMA_VERSION,
    val thumbnailFileName: String? = null
) {
    fun toSummary(): ProjectSummary = ProjectSummary(
        id = id,
        name = name,
        timestamp = timestamp,
        width = snapshot.canvasConfig.width,
        height = snapshot.canvasConfig.height,
        layerCount = snapshot.layers.size,
        thumbnailFileName = thumbnailFileName
    )
}

@Immutable
@Serializable
data class ProjectSummary(
    val id: String,
    val name: String,
    val timestamp: Long,
    val width: Int = 1080,
    val height: Int = 1080,
    val layerCount: Int = 0,
    val thumbnailFileName: String? = null
)

@Immutable
@Serializable
data class DraftPayload(
    val schemaVersion: Int = CURRENT_PROJECT_SCHEMA_VERSION,
    val timestamp: Long,
    val currentProjectId: String? = null,
    val currentProjectName: String = "Untitled",
    val isDirty: Boolean = true,
    val snapshot: EditorSnapshot
)
