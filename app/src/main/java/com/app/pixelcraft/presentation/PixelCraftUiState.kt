package com.app.pixelcraft.presentation

import com.app.pixelcraft.presentation.i18n.UiText
import android.net.Uri
import androidx.compose.runtime.Immutable
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.BottomNavTab
import com.app.pixelcraft.data.model.CanvasConfig
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.ProjectSummary
import com.app.pixelcraft.data.model.SavedLayerStyle
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
    TEXT_SPACING,
    TEXT_CURVE,
    TEXT_COLOR,
    TEXT_OPACITY,
    TEXT_ROTATE,
    TEXT_STYLE,
    TEXT_BACKGROUND,
    TEXT_STROKE,
    TEXT_GLOW,
    TEXT_SHADOW,
    TEXT_3D,
    TEXT_REFLECTION,

    // Object (Shape & Photo) Subtools
    SHAPE_TYPE,
    SHAPE_COLOR,
    SHAPE_STROKE,
    SHAPE_SHADOW,
    SHAPE_OPACITY,
    OBJECT_SIZE,
    OBJECT_ROTATE,
    LAYER_BLEND,
    IMAGE_CROP,
    IMAGE_ADJUST,
    IMAGE_MASK
}

enum class EyedropperTarget {
    TEXT_COLOR,
    TEXT_GRADIENT_END,
    TEXT_BACKGROUND,
    STROKE_COLOR,
    GLOW_COLOR,
    SHADOW_COLOR,
    SHAPE_FILL,
    SHAPE_GRADIENT_END,
    SHAPE_STROKE,
    IMAGE_BORDER,
    CANVAS_BACKGROUND
}

@Immutable
data class PixelCraftUiState(
    val canvasConfig: CanvasConfig = CanvasConfig(),
    val layers: List<Layer> = emptyList(),
    val selectedLayerId: String? = null,
    val selectedLayerIds: Set<String> = emptySet(),
    val activeTab: BottomNavTab = BottomNavTab.TEXT,
    val activeSubTool: ActiveSubTool = ActiveSubTool.NONE,
    val activeDialog: ActiveDialog? = null,

    // Grid, Safe Zone & Snapping (magnetic anchors active when isGridEnabled == true)
    val isGridEnabled: Boolean = false,
    val gridColumns: Int = 3,
    val gridRows: Int = 3,
    val showSafeZone: Boolean = false,
    val activeVerticalGuideX: Float? = null,
    val activeHorizontalGuideY: Float? = null,

    // Layers Drawer
    val isLayersDrawerOpen: Boolean = false,

    // History (Undo / Redo with descriptive labels)
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val lastUndoLabel: String? = null,
    val lastRedoLabel: String? = null,
    val undoHistoryLabels: List<String> = emptyList(),

    // Lifecycle, Autosave & Dirty State
    val isReady: Boolean = false,
    val isDirty: Boolean = false,
    val hasRecoverableDraft: Boolean = false,
    val draftTimestamp: Long? = null,
    val draftProjectName: String? = null,
    val lastAutosavedAt: Long? = null,

    // Projects & Storage
    val currentProjectName: String = "Untitled",
    val currentProjectId: String? = null,
    val savedProjects: List<ProjectData> = emptyList(),
    val projectSummaries: List<ProjectSummary> = emptyList(),

    // Style Clipboard, Saved Styles, Recent Colors, Fonts & Coach Marks
    val copiedStyleLayer: Layer? = null,
    val savedStyles: List<SavedLayerStyle> = emptyList(),
    val recentColors: List<Long> = emptyList(),
    val favoriteFontKeys: Set<String> = emptySet(),
    val availableFontsCount: Int = 16,
    val onboardingDismissed: Boolean = false,

    // Canvas Eyedropper
    val activeEyedropperTarget: EyedropperTarget? = null,

    // Export & Direct Share State
    val exportOptions: ExportOptions = ExportOptions(),
    val isExporting: Boolean = false,
    val lastExportedUri: Uri? = null,
    val pendingShareUri: Uri? = null,
    val pendingShareMimeType: String = "image/png",
    val userMessage: UiText? = null
) {
    /** Cached once per state instance so 20+ accesses per Compose frame are O(1) (§ 4.4). */
    val selectedLayer: Layer? = layers.find { it.id == selectedLayerId }

    val isEyedropperActive: Boolean
        get() = activeEyedropperTarget != null

    val showVerticalGuide: Boolean
        get() = activeVerticalGuideX != null

    val showHorizontalGuide: Boolean
        get() = activeHorizontalGuideY != null
}
