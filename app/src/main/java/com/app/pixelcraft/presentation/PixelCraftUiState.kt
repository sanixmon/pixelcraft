package com.app.pixelcraft.presentation

import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.AspectRatioPreset
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.PointF
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.domain.ExportFormat
import com.app.pixelcraft.domain.ExportOptions
import com.app.pixelcraft.domain.ExportQuality
import java.io.File

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
    TEXT_EDIT,
    TEXT_DELETE,
    TEXT_COPY,
    TEXT_FRONT_BACK,
    TEXT_POSITION,
    TEXT_RELATIVE_POSITION,
    TEXT_SIZE,
    TEXT_PADDING,
    TEXT_COLOR,
    TEXT_OPACITY,
    TEXT_ROTATE,
    TEXT_FONT,
    TEXT_STYLE,
    TEXT_CURVE,
    TEXT_BACKGROUND,
    TEXT_STROKE,
    TEXT_SHADOW,
    TEXT_INNER_SHADOW,
    TEXT_EMBOSS,
    TEXT_3D_ROTATE,
    TEXT_3D,
    TEXT_3D_SHADOW,
    TEXT_REFLECTION,

    // Object Subtools
    SHAPE_TYPE,
    SHAPE_COLOR,
    SHAPE_STROKE,
    SHAPE_RADIUS,
    SHAPE_OPACITY,

    // Background Subtools
    BG_COLOR,
    BG_TRANSPARENT,
    BG_IMAGE_SIZE,
    BG_WALLPAPERS,

    // Filter Subtools
    FILTER_VIGNETTE,
    FILTER_NOISE,
    FILTER_STRIPES,
    FILTER_BRIGHTNESS,
    FILTER_CONTRAST,
    FILTER_HUE,
    FILTER_SATURATION
}

data class DrawingState(
    val isDrawing: Boolean = false,
    val brushColor: Long = 0xFFFF0055,
    val brushSize: Float = 14f,
    val isNeon: Boolean = false,
    val currentPoints: List<PointF> = emptyList()
)

data class PixelCraftUiState(
    val canvasConfig: CanvasConfig = CanvasConfig(),
    val layers: List<Layer> = emptyList(),
    val selectedLayerId: String? = null,
    val activeTab: BottomNavTab = BottomNavTab.TEXT,
    val activeSubTool: ActiveSubTool = ActiveSubTool.NONE,
    val activeDialog: ActiveDialog? = null,

    // Canvas Navigation
    val isZoomMode: Boolean = false,
    val zoomScale: Float = 1.0f,
    val panOffset: PointF = PointF(0f, 0f),

    // Grid & Snapping
    val isGridEnabled: Boolean = false,
    val isSnapToGrid: Boolean = false,
    val gridColumns: Int = 3,
    val gridRows: Int = 3,
    val showVerticalGuide: Boolean = false,
    val verticalGuideX: Float = 0f,
    val showHorizontalGuide: Boolean = false,
    val horizontalGuideY: Float = 0f,

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
    val userMessage: String? = null,

    // Drawing Tool
    val drawingState: DrawingState = DrawingState()
) {
    val selectedLayer: Layer?
        get() = layers.find { it.id == selectedLayerId }
}
