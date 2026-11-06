package com.app.pixelcraft.presentation

import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.domain.ExportOptions

enum class AlignmentAction {
    CENTER_HORIZONTAL,
    CENTER_VERTICAL,
    ALIGN_LEFT,
    ALIGN_RIGHT,
    ALIGN_TOP,
    ALIGN_BOTTOM
}

enum class ActiveSubTool {
    NONE,
    TEXT_FONT,
    TEXT_POSITION,
    TEXT_RELATIVE_POSITION,
    TEXT_SIZE,
    TEXT_COLOR,
    TEXT_OPACITY,
    TEXT_ROTATE,
    TEXT_STYLE,
    TEXT_BACKGROUND,
    TEXT_STROKE,
    TEXT_SHADOW,
    TEXT_3D,
    TEXT_REFLECTION,

    // Object / PicSay Pro Shape Subtools
    SHAPE_TYPE,
    SHAPE_COLOR,
    SHAPE_STROKE,
    SHAPE_SHADOW,
    SHAPE_OPACITY,
    OBJECT_SIZE,
    OBJECT_ROTATE
}

data class PixelCraftUiState(
    val canvasConfig: CanvasConfig = CanvasConfig(),
    val layers: List<Layer> = emptyList(),
    val selectedLayerId: String? = null,
    val activeTab: BottomNavTab = BottomNavTab.TEXT,
    val activeSubTool: ActiveSubTool = ActiveSubTool.NONE,
    val activeDialog: ActiveDialog? = null,

    // Grid & Snapping (anchors active ONLY when isGridEnabled == true)
    val isGridEnabled: Boolean = false,
    val gridColumns: Int = 3,
    val gridRows: Int = 3,
    val activeVerticalGuideX: Float? = null,
    val activeHorizontalGuideY: Float? = null,

    // Layers Drawer
    val isLayersDrawerOpen: Boolean = false,

    // History (Undo / Redo)
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,

    // Projects & Storage
    val currentProjectName: String = "Untitled",
    val currentProjectId: String? = null,
    val savedProjects: List<ProjectData> = emptyList(),

    // Export State
    val exportOptions: ExportOptions = ExportOptions(),
    val isExporting: Boolean = false,
    val lastExportedUri: android.net.Uri? = null,
    val userMessage: String? = null
) {
    val selectedLayer: Layer?
        get() = layers.find { it.id == selectedLayerId }

    val showVerticalGuide: Boolean
        get() = activeVerticalGuideX != null

    val showHorizontalGuide: Boolean
        get() = activeHorizontalGuideY != null
}
