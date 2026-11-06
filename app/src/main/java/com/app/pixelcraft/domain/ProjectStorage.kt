package com.app.pixelcraft.domain

import android.content.Context
import com.app.pixelcraft.data.model.ProjectData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ProjectStorage(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val projectsDir: File
        get() = File(context.filesDir, "pixelcraft_projects").apply {
            if (!exists()) mkdirs()
        }

    suspend fun saveProject(project: ProjectData): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(projectsDir, "${project.id}.json")
            val content = json.encodeToString(project)
            file.writeText(content)
        }
    }

    suspend fun loadProject(id: String): Result<ProjectData> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(projectsDir, "$id.json")
            if (!file.exists()) error("Project not found")
            val content = file.readText()
            json.decodeFromString<ProjectData>(content)
        }
    }

    suspend fun listProjects(): Result<List<ProjectData>> = withContext(Dispatchers.IO) {
        runCatching {
            val files = projectsDir.listFiles { _, name -> name.endsWith(".json") } ?: emptyArray()
            files.mapNotNull { file ->
                runCatching {
                    json.decodeFromString<ProjectData>(file.readText())
                }.getOrNull()
            }.sortedByDescending { it.timestamp }
        }
    }

    suspend fun deleteProject(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val file = File(projectsDir, "$id.json")
            if (file.exists()) {
                file.delete()
            }
        }
    }
}
