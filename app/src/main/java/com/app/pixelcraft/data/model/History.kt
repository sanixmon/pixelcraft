package com.app.pixelcraft.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EditorSnapshot(
    val layers: List<Layer>,
    val canvasConfig: CanvasConfig,
    val selectedLayerId: String?
)

@Serializable
data class ProjectData(
    val id: String,
    val name: String,
    val timestamp: Long,
    val snapshot: EditorSnapshot
)
