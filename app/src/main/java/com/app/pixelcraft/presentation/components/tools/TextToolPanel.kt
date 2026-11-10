package com.app.pixelcraft.presentation.components.tools

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.presentation.i18n.labelRes
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Looks
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.data.presets.FontCatalog
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

internal val TextColorPalette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFF0055, 0xFFFF5722, 0xFFFFEB3B,
    0xFF00E676, 0xFF00E5FF, 0xFF2979FF, 0xFF651FFF, 0xFFFF80AB
)

private enum class TextToolGroup(@androidx.annotation.StringRes val labelRes: Int) {
    ALL(R.string.ui_tab_all),
    TYPE(R.string.ui_tab_type),
    FX(R.string.ui_tab_fx),
    LAYOUT(R.string.ui_tab_layout)
}

@Composable
fun TextToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    recentColors: List<Long> = emptyList(),
    favoriteFontKeys: Set<String> = emptySet(),
    availableFontsCount: Int = 16,
    hasCopiedStyle: Boolean = false,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onApplyTextCase: (TextCaseMode) -> Unit = {},
    onToggleFavoriteFont: (String) -> Unit = {},
    onImportFont: () -> Unit = {},
    onStartEyedropper: (EyedropperTarget) -> Unit = {},
    onCopyStyle: () -> Unit = {},
    onPasteStyle: () -> Unit = {},
    onOpenSavedStyles: () -> Unit = {},
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onOpenEditDialog: () -> Unit,
    onAddText: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selectedLayer == null || selectedLayer.type != LayerType.TEXT) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(62.dp)
                .background(Color(0xFF1E222D))
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAddText,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("+ New Text", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = onOpenSavedStyles) {
                Icon(Icons.Default.Bookmarks, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ui_saved_styles), color = Color.White, fontSize = 12.sp)
            }
        }
        return
    }

    var selectedGroup by remember { mutableStateOf(TextToolGroup.ALL) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E222D))
    ) {
        if (activeSubTool != ActiveSubTool.NONE) {
            SubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                recentColors = recentColors,
                favoriteFontKeys = favoriteFontKeys,
                availableFontsCount = availableFontsCount,
                onUpdate = onUpdateLayer,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onApplyTextCase = onApplyTextCase,
                onToggleFavoriteFont = onToggleFavoriteFont,
                onImportFont = onImportFont,
                onStartEyedropper = onStartEyedropper,
                onNudge = onNudge,
                onAlign = onAlign,
                onClose = { onSubToolSelected(ActiveSubTool.NONE) }
            )
        }

        // Compact group filter + Style Clipboard bar (§ 5.2e & § 7.1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF181B24))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (group in TextToolGroup.entries) {
                val active = selectedGroup == group
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (active) Color(0xFF00E5FF) else Color(0xFF2D3343),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedGroup = group }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(group.labelRes),
                        color = if (active) Color(0xFF00E5FF) else Color(0xFFB5BDCE),
                        fontSize = 11.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            MiniActionPill(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.ui_copy_style),
                onClick = onCopyStyle
            )
            if (hasCopiedStyle) {
                MiniActionPill(
                    icon = Icons.Default.ContentPaste,
                    label = stringResource(R.string.ui_paste_style),
                    accent = true,
                    onClick = onPasteStyle
                )
            }
            MiniActionPill(
                icon = Icons.Default.Bookmarks,
                label = stringResource(R.string.ui_styles),
                onClick = onOpenSavedStyles
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val showType = selectedGroup == TextToolGroup.ALL || selectedGroup == TextToolGroup.TYPE
            val showFx = selectedGroup == TextToolGroup.ALL || selectedGroup == TextToolGroup.FX
            val showLayout = selectedGroup == TextToolGroup.ALL || selectedGroup == TextToolGroup.LAYOUT

            if (showType) {
                ToolButton(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.ui_edit),
                    isActive = false,
                    onClick = onOpenEditDialog
                )
                ToolButton(
                    icon = Icons.Default.FontDownload,
                    label = stringResource(R.string.ui_font),
                    isActive = activeSubTool == ActiveSubTool.TEXT_FONT,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_FONT) }
                )
                ToolButton(
                    icon = Icons.Default.FormatSize,
                    label = stringResource(R.string.ui_size),
                    isActive = activeSubTool == ActiveSubTool.TEXT_SIZE,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_SIZE) }
                )
                ToolButton(
                    icon = Icons.Default.FormatBold,
                    label = stringResource(R.string.ui_style),
                    isActive = activeSubTool == ActiveSubTool.TEXT_STYLE,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_STYLE) }
                )
                ToolButton(
                    icon = Icons.Default.FormatLineSpacing,
                    label = stringResource(R.string.ui_spacing),
                    isActive = activeSubTool == ActiveSubTool.TEXT_SPACING,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_SPACING) }
                )
                ToolButton(
                    icon = Icons.Default.Looks,
                    label = stringResource(R.string.ui_curve),
                    isActive = activeSubTool == ActiveSubTool.TEXT_CURVE,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_CURVE) }
                )
            }

            if (showFx) {
                ToolButton(
                    icon = Icons.Default.ColorLens,
                    label = stringResource(R.string.ui_color),
                    isActive = activeSubTool == ActiveSubTool.TEXT_COLOR,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_COLOR) }
                )
                ToolButton(
                    icon = Icons.Default.BorderStyle,
                    label = stringResource(R.string.ui_stroke),
                    isActive = activeSubTool == ActiveSubTool.TEXT_STROKE,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_STROKE) }
                )
                ToolButton(
                    icon = Icons.Default.AutoAwesome,
                    label = stringResource(R.string.ui_glow),
                    isActive = activeSubTool == ActiveSubTool.TEXT_GLOW,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_GLOW) }
                )
                ToolButton(
                    icon = Icons.Default.Lightbulb,
                    label = stringResource(R.string.ui_shadow),
                    isActive = activeSubTool == ActiveSubTool.TEXT_SHADOW,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_SHADOW) }
                )
                ToolButton(
                    icon = Icons.Default.FormatColorFill,
                    label = stringResource(R.string.ui_bg_pill),
                    isActive = activeSubTool == ActiveSubTool.TEXT_BACKGROUND,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_BACKGROUND) }
                )
                ToolButton(
                    icon = Icons.Default.ViewInAr,
                    label = "3D Text",
                    isActive = activeSubTool == ActiveSubTool.TEXT_3D,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_3D) }
                )
                ToolButton(
                    icon = Icons.Default.SettingsBackupRestore,
                    label = stringResource(R.string.ui_reflect),
                    isActive = activeSubTool == ActiveSubTool.TEXT_REFLECTION,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_REFLECTION) }
                )
                ToolButton(
                    icon = Icons.Default.BlurOn,
                    label = stringResource(R.string.ui_blend),
                    isActive = activeSubTool == ActiveSubTool.LAYER_BLEND,
                    onClick = { onSubToolSelected(ActiveSubTool.LAYER_BLEND) }
                )
                ToolButton(
                    icon = Icons.Default.Opacity,
                    label = stringResource(R.string.ui_opacity),
                    isActive = activeSubTool == ActiveSubTool.TEXT_OPACITY,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_OPACITY) }
                )
            }

            if (showLayout) {
                ToolButton(
                    icon = Icons.Default.Rotate90DegreesCcw,
                    label = stringResource(R.string.ui_rotate),
                    isActive = activeSubTool == ActiveSubTool.TEXT_ROTATE,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_ROTATE) }
                )
                ToolButton(
                    icon = Icons.Default.ArrowUpward,
                    label = stringResource(R.string.ui_nudge),
                    isActive = activeSubTool == ActiveSubTool.TEXT_POSITION,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_POSITION) }
                )
                ToolButton(
                    icon = Icons.Default.AlignHorizontalCenter,
                    label = stringResource(R.string.ui_align),
                    isActive = activeSubTool == ActiveSubTool.TEXT_RELATIVE_POSITION,
                    onClick = { onSubToolSelected(ActiveSubTool.TEXT_RELATIVE_POSITION) }
                )
                ToolButton(
                    icon = Icons.Default.Layers,
                    label = stringResource(R.string.ui_to_front),
                    isActive = false,
                    onClick = onBringToFront
                )
                ToolButton(
                    icon = Icons.Default.Layers,
                    label = stringResource(R.string.ui_to_back),
                    isActive = false,
                    onClick = onSendToBack
                )
                ToolButton(
                    icon = Icons.Default.ContentCopy,
                    label = stringResource(R.string.action_duplicate),
                    isActive = false,
                    onClick = onDuplicateLayer
                )
                ToolButton(
                    icon = Icons.Default.Delete,
                    label = stringResource(R.string.action_delete),
                    isActive = false,
                    onClick = onDeleteLayer
                )
            }
        }
    }
}

@Composable
internal fun MiniActionPill(
    icon: ImageVector,
    label: String,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    val fg = if (accent) Color(0xFF00E5FF) else Color(0xFFD5DAE5)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF252A38))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = fg, fontSize = 11.sp)
    }
}

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    val inactiveColor = Color.White

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) activeColor.copy(alpha = 0.18f) else Color.Transparent)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else Color(0xFFCCCCCC)
        )
    }
}

@Composable
private fun SubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    recentColors: List<Long>,
    favoriteFontKeys: Set<String>,
    availableFontsCount: Int,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onApplyTextCase: (TextCaseMode) -> Unit,
    onToggleFavoriteFont: (String) -> Unit,
    onImportFont: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(Color(0xFF161922))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subTool.name
                    .removePrefix("TEXT_")
                    .removePrefix("LAYER_")
                    .replace("_", " "),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                fontSize = 13.sp
            )
            IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.ui_close), tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        when (subTool) {
            ActiveSubTool.TEXT_FONT -> TextFontInspector(layer = layer, favoriteFontKeys = favoriteFontKeys, availableFontsCount = availableFontsCount, onUpdate = onUpdate, onToggleFavoriteFont = onToggleFavoriteFont, onImportFont = onImportFont)

            ActiveSubTool.TEXT_POSITION -> TextPositionInspector(onNudge = onNudge)

            ActiveSubTool.TEXT_RELATIVE_POSITION -> TextAlignInspector(onAlign = onAlign)

            ActiveSubTool.TEXT_SIZE -> TextSizeInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_SPACING -> TextSpacingInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_CURVE -> TextCurveInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_COLOR -> TextColorInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.TEXT_OPACITY -> TextOpacityInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_ROTATE -> TextRotateInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_STYLE -> TextStyleInspector(layer = layer, onUpdate = onUpdate, onApplyTextCase = onApplyTextCase)

            ActiveSubTool.TEXT_BACKGROUND -> TextBackgroundInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.TEXT_STROKE -> TextStrokeInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.TEXT_GLOW -> TextGlowInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.TEXT_SHADOW -> TextShadowInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.TEXT_3D -> Text3dInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.TEXT_REFLECTION -> TextReflectionInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.LAYER_BLEND -> TextBlendInspector(layer = layer, onUpdate = onUpdate)

            else -> {}
        }
    }
}
