package com.app.pixelcraft.presentation.components.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.HorizontalDistribute
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalDistribute
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wysiwyg
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.app.pixelcraft.R
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.EyedropperTarget

/** Text: Edit · Font · Style · Effects · Arrange. */
@Composable
fun TextContextPanel(
    layer: Layer,
    activeSubTool: ActiveSubTool,
    recentColors: List<Long>,
    favoriteFontKeys: Set<String>,
    recentFontKeys: List<String>,
    availableFontsCount: Int,
    hasCopiedStyle: Boolean,
    canvasWidth: Int,
    canvasHeight: Int,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onApplyTextCase: (TextCaseMode) -> Unit,
    onToggleFavoriteFont: (String) -> Unit,
    onImportFont: () -> Unit,
    onPickDeviceFontsFolder: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onCopyStyle: () -> Unit,
    onPasteStyle: () -> Unit,
    onOpenSavedStyles: () -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onOpenEditDialog: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        PanelCategory(
            "edit", Icons.Default.Edit, stringResource(R.string.ui_edit),
            listOfNotNull(
                PanelTool(Icons.Default.Edit, stringResource(R.string.ui_edit_text), onAction = onOpenEditDialog),
                PanelTool(Icons.Default.Style, stringResource(R.string.ui_presets), ActiveSubTool.TEXT_PRESETS),
                PanelTool(Icons.Default.ContentCopy, stringResource(R.string.ui_copy_style), onAction = onCopyStyle),
                if (hasCopiedStyle) PanelTool<ActiveSubTool>(Icons.Default.ContentPaste, stringResource(R.string.ui_paste_style), onAction = onPasteStyle) else null,
                PanelTool(Icons.Default.Bookmarks, stringResource(R.string.ui_styles), onAction = onOpenSavedStyles),
                PanelTool(Icons.Default.ContentCopy, stringResource(R.string.action_duplicate), onAction = onDuplicateLayer),
                PanelTool(Icons.Default.Delete, stringResource(R.string.action_delete), onAction = onDeleteLayer)
            )
        ),
        PanelCategory(
            "font", Icons.Default.TextFields, stringResource(R.string.ui_font),
            listOf(
                PanelTool(Icons.Default.TextFields, stringResource(R.string.ui_font), ActiveSubTool.TEXT_FONT),
                PanelTool(Icons.Default.FormatSize, stringResource(R.string.ui_size), ActiveSubTool.TEXT_SIZE),
                PanelTool(Icons.Default.FormatBold, stringResource(R.string.ui_format), ActiveSubTool.TEXT_STYLE),
                PanelTool(Icons.Default.FormatLineSpacing, stringResource(R.string.ui_spacing), ActiveSubTool.TEXT_SPACING),
                PanelTool(Icons.Default.Wysiwyg, stringResource(R.string.ui_curve), ActiveSubTool.TEXT_CURVE)
            )
        ),
        PanelCategory(
            "style", Icons.Default.Palette, stringResource(R.string.ui_style),
            listOf(
                PanelTool(Icons.Default.FormatColorFill, stringResource(R.string.ui_color), ActiveSubTool.TEXT_COLOR),
                PanelTool(Icons.Default.BorderStyle, stringResource(R.string.ui_stroke), ActiveSubTool.TEXT_STROKE),
                PanelTool(Icons.Default.PhotoSizeSelectLarge, stringResource(R.string.ui_bg_pill), ActiveSubTool.TEXT_BACKGROUND),
                PanelTool(Icons.Default.Opacity, stringResource(R.string.ui_opacity), ActiveSubTool.TEXT_OPACITY),
                PanelTool(Icons.Default.BlurOn, stringResource(R.string.ui_blend), ActiveSubTool.LAYER_BLEND)
            )
        ),
        PanelCategory(
            "effects", Icons.Default.AutoAwesome, stringResource(R.string.ui_effects),
            listOf(
                PanelTool(Icons.Default.Lightbulb, stringResource(R.string.ui_shadow), ActiveSubTool.TEXT_SHADOW),
                PanelTool(Icons.Default.WbSunny, stringResource(R.string.ui_glow), ActiveSubTool.TEXT_GLOW),
                PanelTool(Icons.Default.ViewInAr, "3D", ActiveSubTool.TEXT_3D),
                PanelTool(Icons.Default.SettingsBackupRestore, stringResource(R.string.ui_reflect), ActiveSubTool.TEXT_REFLECTION),
                PanelTool(Icons.Default.Gradient, stringResource(R.string.ui_inner_shadow), ActiveSubTool.TEXT_INNER_SHADOW),
                PanelTool(Icons.Default.Texture, stringResource(R.string.ui_emboss), ActiveSubTool.TEXT_EMBOSS)
            )
        ),
        PanelCategory(
            "arrange", Icons.Default.OpenWith, stringResource(R.string.ui_arrange),
            listOf(
                PanelTool(Icons.Default.OpenWith, stringResource(R.string.ui_position), ActiveSubTool.TEXT_POSITION),
                PanelTool(Icons.Default.Rotate90DegreesCcw, stringResource(R.string.ui_rotate), ActiveSubTool.TEXT_ROTATE),
                PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_front), onAction = onBringToFront),
                PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_back), onAction = onSendToBack)
            )
        )
    )
    ContextualPanel(
        categories = categories,
        activeTool = activeSubTool,
        noTool = ActiveSubTool.NONE,
        selectionKey = layer.id,
        onToolSelected = onSubToolSelected,
        onDone = onDone,
        modifier = modifier
    ) { tool ->
        TextSubToolContent(
            subTool = tool, layer = layer, recentColors = recentColors, favoriteFontKeys = favoriteFontKeys,
            recentFontKeys = recentFontKeys, availableFontsCount = availableFontsCount, onUpdate = onUpdateLayer,
            onFinishContinuousEdit = onFinishContinuousEdit, onApplyTextCase = onApplyTextCase,
            onToggleFavoriteFont = onToggleFavoriteFont, onImportFont = onImportFont,
            onPickDeviceFontsFolder = onPickDeviceFontsFolder, onStartEyedropper = onStartEyedropper,
            onNudge = onNudge, onAlign = onAlign, canvasWidth = canvasWidth, canvasHeight = canvasHeight
        )
    }
}

/**
 * Photo: Replace · Crop · Adjust · Filter · Effects · Arrange.
 * Shape: Geometry · Fill · Stroke · Effects · Arrange.
 */
@Composable
fun ObjectContextPanel(
    layer: Layer,
    activeSubTool: ActiveSubTool,
    recentColors: List<Long>,
    hasCopiedStyle: Boolean,
    canvasWidth: Int,
    canvasHeight: Int,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onCopyStyle: () -> Unit,
    onPasteStyle: () -> Unit,
    onOpenSavedStyles: () -> Unit,
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onReplaceImage: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flipH = PanelTool<ActiveSubTool>(Icons.Default.Flip, stringResource(R.string.ui_flip_h), onAction = {
        onUpdateLayer(false) { it.copy(flipHorizontal = !it.flipHorizontal) }
    })
    val flipV = PanelTool<ActiveSubTool>(Icons.Default.Flip, stringResource(R.string.ui_flip_v), onAction = {
        onUpdateLayer(false) { it.copy(flipVertical = !it.flipVertical) }
    })
    val arrange = PanelCategory(
        "arrange", Icons.Default.OpenWith, stringResource(R.string.ui_arrange),
        listOf(
            PanelTool(Icons.Default.OpenWith, stringResource(R.string.ui_position), ActiveSubTool.OBJECT_POSITION),
            PanelTool(Icons.Default.AspectRatio, stringResource(R.string.ui_size), ActiveSubTool.OBJECT_SIZE),
            PanelTool(Icons.Default.Rotate90DegreesCcw, stringResource(R.string.ui_rotate), ActiveSubTool.OBJECT_ROTATE),
            flipH,
            flipV,
            PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_front), onAction = onBringToFront),
            PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_back), onAction = onSendToBack),
            PanelTool(Icons.Default.ContentCopy, stringResource(R.string.action_duplicate), onAction = onDuplicateLayer),
            PanelTool(Icons.Default.Delete, stringResource(R.string.action_delete), onAction = onDeleteLayer)
        )
    )
    val styleActions = listOfNotNull(
        PanelTool<ActiveSubTool>(Icons.Default.ContentCopy, stringResource(R.string.ui_copy_style), onAction = onCopyStyle),
        if (hasCopiedStyle) PanelTool<ActiveSubTool>(Icons.Default.ContentPaste, stringResource(R.string.ui_paste_style), onAction = onPasteStyle) else null,
        PanelTool<ActiveSubTool>(Icons.Default.Bookmarks, stringResource(R.string.ui_styles), onAction = onOpenSavedStyles)
    )
    val categories = if (layer.type == LayerType.IMAGE) {
        listOf(
            PanelCategory<ActiveSubTool>(
                "replace", Icons.Default.SwapHoriz, stringResource(R.string.ui_replace),
                listOf(PanelTool(Icons.Default.SwapHoriz, stringResource(R.string.ui_replace), onAction = onReplaceImage))
            ),
            PanelCategory(
                "crop", Icons.Default.Crop, stringResource(R.string.ui_crop),
                listOf(PanelTool(Icons.Default.Crop, stringResource(R.string.ui_crop), ActiveSubTool.IMAGE_CROP))
            ),
            PanelCategory(
                "adjust", Icons.Default.Tune, stringResource(R.string.ui_adjust),
                listOf(PanelTool(Icons.Default.Tune, stringResource(R.string.ui_adjust), ActiveSubTool.IMAGE_ADJUST))
            ),
            PanelCategory(
                "filter", Icons.Default.FilterVintage, stringResource(R.string.ui_filter),
                listOf(PanelTool(Icons.Default.FilterVintage, stringResource(R.string.ui_filter), ActiveSubTool.IMAGE_FILTER))
            ),
            PanelCategory(
                "effects", Icons.Default.AutoAwesome, stringResource(R.string.ui_effects),
                listOf(
                    PanelTool(Icons.Default.BorderStyle, stringResource(R.string.ui_stroke), ActiveSubTool.IMAGE_STROKE),
                    PanelTool(Icons.Default.Lightbulb, stringResource(R.string.ui_shadow), ActiveSubTool.SHAPE_SHADOW),
                    PanelTool(Icons.Default.LensBlur, stringResource(R.string.ui_mask), ActiveSubTool.IMAGE_MASK),
                    PanelTool(Icons.Default.Opacity, stringResource(R.string.ui_opacity), ActiveSubTool.SHAPE_OPACITY),
                    PanelTool(Icons.Default.BlurOn, stringResource(R.string.ui_blend), ActiveSubTool.LAYER_BLEND)
                ) + styleActions
            ),
            arrange
        )
    } else {
        listOf(
            PanelCategory(
                "geometry", Icons.Default.Category, stringResource(R.string.ui_geometry),
                listOf(
                    PanelTool(Icons.Default.Category, stringResource(R.string.ui_shape), ActiveSubTool.SHAPE_TYPE),
                    PanelTool(Icons.Default.AspectRatio, stringResource(R.string.ui_size), ActiveSubTool.OBJECT_SIZE)
                )
            ),
            PanelCategory(
                "fill", Icons.Default.FormatColorFill, stringResource(R.string.ui_fill),
                listOf(
                    PanelTool(Icons.Default.FormatColorFill, stringResource(R.string.ui_fill), ActiveSubTool.SHAPE_COLOR),
                    PanelTool(Icons.Default.Opacity, stringResource(R.string.ui_opacity), ActiveSubTool.SHAPE_OPACITY),
                    PanelTool(Icons.Default.BlurOn, stringResource(R.string.ui_blend), ActiveSubTool.LAYER_BLEND)
                )
            ),
            PanelCategory(
                "stroke", Icons.Default.BorderStyle, stringResource(R.string.ui_stroke),
                listOf(PanelTool(Icons.Default.BorderStyle, stringResource(R.string.ui_stroke), ActiveSubTool.SHAPE_STROKE))
            ),
            PanelCategory(
                "effects", Icons.Default.AutoAwesome, stringResource(R.string.ui_effects),
                listOf(PanelTool<ActiveSubTool>(Icons.Default.Lightbulb, stringResource(R.string.ui_shadow), ActiveSubTool.SHAPE_SHADOW)) + styleActions
            ),
            arrange
        )
    }
    ContextualPanel(
        categories = categories,
        activeTool = activeSubTool,
        noTool = ActiveSubTool.NONE,
        selectionKey = layer.id,
        onToolSelected = onSubToolSelected,
        onDone = onDone,
        modifier = modifier
    ) { tool ->
        ObjectSubToolContent(
            subTool = tool, layer = layer, recentColors = recentColors, onUpdate = onUpdateLayer,
            onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper,
            canvasWidth = canvasWidth, canvasHeight = canvasHeight, onNudge = onNudge, onAlign = onAlign
        )
    }
}

/**
 * A group or a multi-selection, edited as one object: Arrange · Align · Distribute · Style · Group.
 * Only controls that make sense for mixed layers appear.
 */
@Composable
fun SelectionContextPanel(
    selectionKey: Any,
    isGroup: Boolean,
    memberCount: Int,
    primaryLayer: Layer,
    recentColors: List<Long>,
    activeTool: GroupSubTool,
    onToolSelected: (GroupSubTool) -> Unit,
    onScale: (percent: Float, continuous: Boolean) -> Unit,
    onRotate: (degrees: Float, continuous: Boolean) -> Unit,
    onUpdateMembers: (continuous: Boolean, transform: (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onFlip: (horizontal: Boolean) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onDistribute: (horizontal: Boolean) -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onGroup: () -> Unit,
    onUngroup: () -> Unit,
    onMerge: () -> Unit,
    onEditItem: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOfNotNull(
        PanelCategory(
            "arrange", Icons.Default.OpenWith, stringResource(R.string.ui_arrange),
            listOf(
                PanelTool(Icons.Default.AspectRatio, stringResource(R.string.ui_size), GroupSubTool.SIZE),
                PanelTool(Icons.Default.Rotate90DegreesCcw, stringResource(R.string.ui_rotate), GroupSubTool.ROTATE),
                PanelTool(Icons.Default.Flip, stringResource(R.string.ui_flip_h), onAction = { onFlip(true) }),
                PanelTool(Icons.Default.Flip, stringResource(R.string.ui_flip_v), onAction = { onFlip(false) }),
                PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_front), onAction = onBringToFront),
                PanelTool(Icons.Default.Layers, stringResource(R.string.ui_to_back), onAction = onSendToBack)
            )
        ),
        PanelCategory(
            "align", Icons.Default.AlignHorizontalCenter, stringResource(R.string.ui_align),
            listOf(PanelTool(Icons.Default.AlignHorizontalCenter, stringResource(R.string.ui_align), GroupSubTool.ALIGN))
        ),
        if (memberCount >= 3) {
            PanelCategory(
                "distribute", Icons.Default.HorizontalDistribute, stringResource(R.string.ui_distribute),
                listOf(
                    PanelTool<GroupSubTool>(Icons.Default.HorizontalDistribute, stringResource(R.string.ui_across), onAction = { onDistribute(true) }),
                    PanelTool<GroupSubTool>(Icons.Default.VerticalDistribute, stringResource(R.string.ui_down), onAction = { onDistribute(false) })
                )
            )
        } else {
            null
        },
        PanelCategory(
            "style", Icons.Default.Palette, stringResource(R.string.ui_style),
            listOf(
                PanelTool(Icons.Default.BorderStyle, stringResource(R.string.ui_stroke), GroupSubTool.STROKE),
                PanelTool(Icons.Default.Opacity, stringResource(R.string.ui_opacity), GroupSubTool.OPACITY),
                PanelTool(Icons.Default.BlurOn, stringResource(R.string.ui_blend), GroupSubTool.BLEND)
            )
        ),
        PanelCategory<GroupSubTool>(
            "group", Icons.Default.GroupWork, stringResource(R.string.ui_group),
            if (isGroup) {
                listOf(
                    PanelTool(Icons.Default.LinkOff, stringResource(R.string.ui_ungroup), onAction = onUngroup),
                    PanelTool(Icons.Default.Layers, stringResource(R.string.ui_merge), onAction = onMerge),
                    PanelTool(Icons.Default.Edit, stringResource(R.string.ui_edit_item), onAction = onEditItem),
                    PanelTool(Icons.Default.ContentCopy, stringResource(R.string.action_duplicate), onAction = onDuplicate),
                    PanelTool(Icons.Default.Delete, stringResource(R.string.action_delete), onAction = onDelete)
                )
            } else {
                listOf(
                    PanelTool(Icons.Default.GroupWork, stringResource(R.string.ui_group), onAction = onGroup),
                    PanelTool(Icons.Default.ContentCopy, stringResource(R.string.action_duplicate), onAction = onDuplicate),
                    PanelTool(Icons.Default.Delete, stringResource(R.string.action_delete), onAction = onDelete)
                )
            }
        )
    )
    ContextualPanel(
        categories = categories,
        activeTool = activeTool,
        noTool = GroupSubTool.NONE,
        selectionKey = selectionKey,
        onToolSelected = onToolSelected,
        onDone = onDone,
        modifier = modifier
    ) { tool ->
        GroupSubToolContent(
            subTool = tool, primaryLayer = primaryLayer, onScale = onScale, onRotate = onRotate,
            onUpdateMembers = onUpdateMembers, onFinishContinuousEdit = onFinishContinuousEdit, onAlign = onAlign,
            recentColors = recentColors, memberCount = memberCount, onDistribute = onDistribute
        )
    }
}
