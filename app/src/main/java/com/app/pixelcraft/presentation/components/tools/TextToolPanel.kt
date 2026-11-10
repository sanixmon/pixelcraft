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

private val TextColorPalette = listOf(
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
            ActiveSubTool.TEXT_FONT -> {
                var searchQuery by remember { mutableStateOf("") }
                var selectedCategory by remember { mutableStateOf("All") }
                val allFonts = remember(availableFontsCount) { FontCatalog.allFonts() }
                val categories = remember(allFonts, favoriteFontKeys) {
                    buildList {
                        add("All")
                        if (favoriteFontKeys.isNotEmpty()) add("Favorites")
                        if (allFonts.any { it.category == "Imported" }) add("Imported")
                        allFonts.map { it.category }.distinct().filter { it != "Imported" }.forEach { add(it) }
                    }
                }
                val filteredFonts = remember(allFonts, searchQuery, selectedCategory, favoriteFontKeys) {
                    allFonts.filter { font ->
                        val matchesCategory = when (selectedCategory) {
                            "All" -> true
                            "Favorites" -> font.key in favoriteFontKeys
                            else -> font.category.equals(selectedCategory, ignoreCase = true)
                        }
                        val matchesSearch = searchQuery.isBlank() ||
                            font.displayName.contains(searchQuery.trim(), ignoreCase = true) ||
                            font.category.contains(searchQuery.trim(), ignoreCase = true)
                        matchesCategory && matchesSearch
                    }
                }
                val previewSample = remember(layer.text) {
                    layer.text.lineSequence().firstOrNull()?.trim()?.take(10)?.ifBlank { "Abc" } ?: "Abc"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(stringResource(R.string.ui_search_fonts, allFonts.size), fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = onImportFont) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(".ttf/.otf", color = Color(0xFF00E5FF), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (cat in categories) {
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(
                                    text = when (cat) {
                                        "All" -> stringResource(R.string.ui_tab_all)
                                        "Favorites" -> stringResource(R.string.ui_font_cat_favorites)
                                        "Imported" -> stringResource(R.string.ui_font_cat_imported)
                                        else -> cat
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (font in filteredFonts) {
                        val isSelected = layer.fontKey == font.key
                        val isFav = font.key in favoriteFontKeys
                        val composeFamily = remember(font.key) {
                            FontCatalog.resolveComposeFontFamily(font.key)
                        }
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF263346) else Color(0xFF11141C))
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF2D3240),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onUpdate(false) { it.copy(fontKey = font.key) } }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = previewSample,
                                    fontFamily = composeFamily,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = stringResource(R.string.ui_favorite_named, font.displayName),
                                    tint = if (isFav) Color(0xFFFFD54F) else Color(0xFF687082),
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clickable { onToggleFavoriteFont(font.key) }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = font.displayName,
                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFFCCCCCC),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = font.category,
                                color = Color(0xFF7D8597),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            ActiveSubTool.TEXT_POSITION -> {
                var stepPx by remember { mutableFloatStateOf(10f) }
                PrecisionSlider(
                    label = stringResource(R.string.ui_nudge_step),
                    value = stepPx,
                    valueRange = 1f..50f,
                    defaultValue = 10f,
                    fineStep = 1f,
                    unitSuffix = "px",
                    onValueChange = { next, _ -> stepPx = next },
                    onValueChangeFinished = {}
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNudge(-stepPx, 0f) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_nudge_left), tint = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { onNudge(0f, -stepPx) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.ui_nudge_up), tint = Color.White)
                        }
                        IconButton(onClick = { onNudge(0f, stepPx) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = stringResource(R.string.ui_nudge_down), tint = Color.White)
                        }
                    }
                    IconButton(onClick = { onNudge(stepPx, 0f) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.ui_nudge_right), tint = Color.White)
                    }
                }
            }

            ActiveSubTool.TEXT_RELATIVE_POSITION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_LEFT) }) {
                        Icon(Icons.AutoMirrored.Filled.AlignHorizontalLeft, contentDescription = stringResource(R.string.ui_align_left), tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.CENTER_HORIZONTAL) }) {
                        Icon(Icons.Default.AlignHorizontalCenter, contentDescription = stringResource(R.string.ui_center_horizontally), tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_RIGHT) }) {
                        Icon(Icons.AutoMirrored.Filled.AlignHorizontalRight, contentDescription = stringResource(R.string.ui_align_right), tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_TOP) }) {
                        Icon(Icons.Default.AlignVerticalTop, contentDescription = stringResource(R.string.ui_align_top), tint = Color.White)
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.CENTER_VERTICAL) }) {
                        Icon(Icons.Default.AlignVerticalCenter, contentDescription = stringResource(R.string.ui_center_vertically), tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = { onAlign(AlignmentAction.ALIGN_BOTTOM) }) {
                        Icon(Icons.Default.AlignVerticalBottom, contentDescription = stringResource(R.string.ui_align_bottom), tint = Color.White)
                    }
                }
            }

            ActiveSubTool.TEXT_SIZE -> {
                PrecisionSlider(
                    label = stringResource(R.string.ui_font_size),
                    value = layer.fontSize,
                    valueRange = 10f..260f,
                    defaultValue = 44f,
                    fineStep = 1f,
                    unitSuffix = "sp",
                    onValueChange = { sz, cont -> onUpdate(cont) { it.copy(fontSize = sz) } },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_SPACING -> {
                PrecisionSlider(
                    label = stringResource(R.string.ui_letter_spacing),
                    value = layer.letterSpacing,
                    valueRange = -0.15f..1.0f,
                    defaultValue = 0f,
                    fineStep = 0.02f,
                    unitSuffix = "em",
                    valueFormatter = { "%.2f".format(it) },
                    onValueChange = { ls, cont -> onUpdate(cont) { it.copy(letterSpacing = ls) } },
                    onValueChangeFinished = onFinishContinuousEdit
                )
                PrecisionSlider(
                    label = stringResource(R.string.ui_line_spacing),
                    value = layer.lineHeightMultiplier,
                    valueRange = 0.8f..2.2f,
                    defaultValue = 1.15f,
                    fineStep = 0.05f,
                    unitSuffix = "x",
                    valueFormatter = { "%.2f".format(it) },
                    onValueChange = { lh, cont -> onUpdate(cont) { it.copy(lineHeightMultiplier = lh) } },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_CURVE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        stringResource(R.string.ui_curve_arch, "+90°") to 90f,
                        stringResource(R.string.ui_curve_arch, "+45°") to 45f,
                        stringResource(R.string.ui_curve_straight, "0°") to 0f,
                        stringResource(R.string.ui_curve_smile, "-45°") to -45f,
                        stringResource(R.string.ui_curve_smile, "-90°") to -90f
                    ).forEach { (label, deg) ->
                        FilterChip(
                            selected = layer.curveAngle.roundToInt() == deg.roundToInt(),
                            onClick = { onUpdate(false) { it.copy(curveAngle = deg) } },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
                PrecisionSlider(
                    label = stringResource(R.string.ui_arc_curve_angle),
                    value = layer.curveAngle,
                    valueRange = -180f..180f,
                    defaultValue = 0f,
                    fineStep = 2f,
                    unitSuffix = "°",
                    onValueChange = { deg, cont -> onUpdate(cont) { it.copy(curveAngle = deg) } },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_COLOR -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !layer.textGradientEnabled,
                        onClick = { onUpdate(false) { it.copy(textGradientEnabled = false) } },
                        label = { Text(stringResource(R.string.ui_solid_fill), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.textGradientEnabled && !layer.textGradientRadial,
                        onClick = {
                            onUpdate(false) { it.copy(textGradientEnabled = true, textGradientRadial = false) }
                        },
                        label = { Text(stringResource(R.string.ui_linear_gradient), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.textGradientEnabled && layer.textGradientRadial,
                        onClick = {
                            onUpdate(false) { it.copy(textGradientEnabled = true, textGradientRadial = true) }
                        },
                        label = { Text(stringResource(R.string.ui_radial_gradient), fontSize = 12.sp) }
                    )
                }

                Text(
                    text = if (layer.textGradientEnabled) stringResource(R.string.ui_start_color) else stringResource(R.string.ui_text_color),
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp
                )
                ColorPaletteRow(
                    colors = TextColorPalette,
                    recentColors = recentColors,
                    selectedColor = layer.textColor,
                    onSelectColor = { colorValue -> onUpdate(false) { it.copy(textColor = colorValue) } },
                    onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_COLOR) },
                    allowAlpha = true,
                    pickerTitle = stringResource(R.string.ui_text_color)
                )

                if (layer.textGradientEnabled) {
                    Text(stringResource(R.string.ui_end_color), color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    ColorPaletteRow(
                        colors = TextColorPalette,
                        recentColors = recentColors,
                        selectedColor = layer.textGradientEndColor,
                        onSelectColor = { endCol -> onUpdate(false) { it.copy(textGradientEndColor = endCol) } },
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_GRADIENT_END) },
                        allowAlpha = true,
                        pickerTitle = stringResource(R.string.ui_gradient_end_color)
                    )
                    if (!layer.textGradientRadial) {
                        PrecisionSlider(
                            label = stringResource(R.string.ui_gradient_angle),
                            value = layer.textGradientAngle,
                            valueRange = 0f..360f,
                            defaultValue = 0f,
                            fineStep = 5f,
                            unitSuffix = "°",
                            onValueChange = { ang, cont -> onUpdate(cont) { it.copy(textGradientAngle = ang) } },
                            onValueChangeFinished = onFinishContinuousEdit
                        )
                    }
                }
            }

            ActiveSubTool.TEXT_OPACITY -> {
                PrecisionSlider(
                    label = stringResource(R.string.ui_opacity),
                    value = layer.opacity * 100f,
                    valueRange = 0f..100f,
                    defaultValue = 100f,
                    fineStep = 2f,
                    unitSuffix = "%",
                    onValueChange = { pct, cont ->
                        onUpdate(cont) { it.copy(opacity = (pct / 100f).coerceIn(0f, 1f)) }
                    },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_ROTATE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-90f, -45f, 0f, 45f, 90f).forEach { deg ->
                        FilterChip(
                            selected = layer.rotation.roundToInt() == deg.roundToInt(),
                            onClick = { onUpdate(false) { it.copy(rotation = deg) } },
                            label = { Text("${deg.toInt()}°", fontSize = 11.sp) }
                        )
                    }
                }
                PrecisionSlider(
                    label = stringResource(R.string.ui_rotation_angle),
                    value = layer.rotation,
                    valueRange = -180f..180f,
                    defaultValue = 0f,
                    fineStep = 1f,
                    unitSuffix = "°",
                    onValueChange = { newRot, cont -> onUpdate(cont) { it.copy(rotation = newRot) } },
                    onValueChangeFinished = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_STYLE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = layer.isBold,
                        onClick = { onUpdate(false) { it.copy(isBold = !it.isBold) } },
                        label = { Text(stringResource(R.string.ui_bold), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.isItalic,
                        onClick = { onUpdate(false) { it.copy(isItalic = !it.isItalic) } },
                        label = { Text(stringResource(R.string.ui_italic), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.isUnderline,
                        onClick = { onUpdate(false) { it.copy(isUnderline = !it.isUnderline) } },
                        label = { Text(stringResource(R.string.ui_underline), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.flipHorizontal,
                        onClick = { onUpdate(false) { it.copy(flipHorizontal = !it.flipHorizontal) } },
                        leadingIcon = { Icon(Icons.Default.Flip, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        label = { Text(stringResource(R.string.ui_flip_h), fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = layer.flipVertical,
                        onClick = { onUpdate(false) { it.copy(flipVertical = !it.flipVertical) } },
                        label = { Text(stringResource(R.string.ui_flip_v), fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (align in TextAlignment.entries) {
                        FilterChip(
                            selected = layer.textAlign == align,
                            onClick = { onUpdate(false) { it.copy(textAlign = align) } },
                            label = {
                                Text(
                                    text = stringResource(align.labelRes()),
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.ui_transform_case), color = Color(0xFFCCCCCC), fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { onApplyTextCase(TextCaseMode.UPPERCASE) }) {
                        Text(stringResource(R.string.ui_uppercase), color = Color.White, fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { onApplyTextCase(TextCaseMode.LOWERCASE) }) {
                        Text("lowercase", color = Color.White, fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = { onApplyTextCase(TextCaseMode.TITLE_CASE) }) {
                        Text(stringResource(R.string.ui_title_case), color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            ActiveSubTool.TEXT_BACKGROUND -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_enable_text_background_pill), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.textBackgroundColor != null,
                        onCheckedChange = { enable ->
                            onUpdate(false) {
                                it.copy(textBackgroundColor = if (enable) 0xCC000000L else null)
                            }
                        }
                    )
                }
                if (layer.textBackgroundColor != null) {
                    val bgPalette = listOf(
                        0xCC000000L, 0xCCFFFFFFL, 0xCCFF0055L, 0xCC00E5FFL,
                        0xCC651FFFL, 0xCCFFEB3BL, 0xCC00C853L
                    )
                    ColorPaletteRow(
                        colors = bgPalette,
                        recentColors = recentColors,
                        selectedColor = layer.textBackgroundColor,
                        onSelectColor = { c -> onUpdate(false) { it.copy(textBackgroundColor = c) } },
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.TEXT_BACKGROUND) },
                        allowAlpha = true,
                        pickerTitle = stringResource(R.string.ui_text_background_color)
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_pill_padding),
                        value = layer.textBackgroundPadding,
                        valueRange = 0f..60f,
                        defaultValue = 16f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { p, cont -> onUpdate(cont) { it.copy(textBackgroundPadding = p) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_corner_radius),
                        value = layer.textBackgroundRadius,
                        valueRange = 0f..48f,
                        defaultValue = 8f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { r, cont -> onUpdate(cont) { it.copy(textBackgroundRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.TEXT_STROKE -> {
                VectorStrokeInspector(
                    strokeWidth = layer.strokeWidth,
                    maxStrokeWidth = 36f,
                    defaultEnableWidth = 4f,
                    strokeColor = layer.strokeColor,
                    strokePosition = layer.strokePosition,
                    strokePattern = layer.strokePattern,
                    dashLength = layer.strokeDashLength,
                    dashGap = layer.strokeDashGap,
                    dashPhase = layer.strokePhase,
                    strokeJoin = layer.strokeJoin,
                    miterLimit = layer.strokeMiterLimit,
                    strokeCap = layer.strokeCap,
                    palette = TextColorPalette,
                    recentColors = recentColors,
                    onPickFromCanvas = { onStartEyedropper(EyedropperTarget.STROKE_COLOR) },
                    onToggleEnable = { _, width ->
                        onUpdate(false) { it.copy(strokeWidth = width) }
                    },
                    onWidthChange = { w, cont -> onUpdate(cont) { it.copy(strokeWidth = w) } },
                    onColorChange = { c -> onUpdate(false) { it.copy(strokeColor = c) } },
                    onPositionChange = { pos -> onUpdate(false) { it.copy(strokePosition = pos) } },
                    onPatternChange = { pat -> onUpdate(false) { it.copy(strokePattern = pat) } },
                    onDashLengthChange = { d, cont -> onUpdate(cont) { it.copy(strokeDashLength = d) } },
                    onDashGapChange = { g, cont -> onUpdate(cont) { it.copy(strokeDashGap = g) } },
                    onDashPhaseChange = { p, cont -> onUpdate(cont) { it.copy(strokePhase = p) } },
                    onJoinChange = { j -> onUpdate(false) { it.copy(strokeJoin = j) } },
                    onMiterLimitChange = { m, cont -> onUpdate(cont) { it.copy(strokeMiterLimit = m) } },
                    onCapChange = { c -> onUpdate(false) { it.copy(strokeCap = c) } },
                    onFinishContinuousEdit = onFinishContinuousEdit
                )
            }

            ActiveSubTool.TEXT_GLOW -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_enable_outer_neon_glow), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.glowEnabled,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(glowEnabled = enable) } }
                    )
                }
                if (layer.glowEnabled) {
                    ColorPaletteRow(
                        colors = listOf(
                            0xFF00E5FFL, 0xFFFF007FL, 0xFF76FF03L, 0xFFFFEA00L,
                            0xFFD500F9L, 0xFFFF6D00L, 0xFFFFFFFFL
                        ),
                        recentColors = recentColors,
                        selectedColor = layer.glowColor,
                        onSelectColor = { c -> onUpdate(false) { it.copy(glowColor = c) } },
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.GLOW_COLOR) },
                        allowAlpha = true,
                        pickerTitle = stringResource(R.string.ui_outer_glow_color)
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_glow_blur_radius),
                        value = layer.glowRadius,
                        valueRange = 2f..60f,
                        defaultValue = 18f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { rad, cont -> onUpdate(cont) { it.copy(glowRadius = rad) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.TEXT_SHADOW -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_enable_drop_shadow), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.shadowEnabled,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(shadowEnabled = enable) } }
                    )
                }
                if (layer.shadowEnabled) {
                    ColorPaletteRow(
                        colors = listOf(
                            0xAA000000L, 0xFF000000L, 0xCCFFFFFFL, 0xFFFF0055L,
                            0xFF00E5FFL, 0xFFFFEB3BL, 0xFF651FFFL
                        ),
                        recentColors = recentColors,
                        selectedColor = layer.shadowColor,
                        onSelectColor = { c -> onUpdate(false) { it.copy(shadowColor = c) } },
                        onPickFromCanvas = { onStartEyedropper(EyedropperTarget.SHADOW_COLOR) },
                        allowAlpha = true,
                        pickerTitle = stringResource(R.string.ui_text_shadow_color)
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_blur_radius),
                        value = layer.shadowRadius,
                        valueRange = 0f..48f,
                        defaultValue = 10f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { r, cont -> onUpdate(cont) { it.copy(shadowRadius = r) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_offset_x),
                        value = layer.shadowDx,
                        valueRange = -40f..40f,
                        defaultValue = 4f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { dx, cont -> onUpdate(cont) { it.copy(shadowDx = dx) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_offset_y),
                        value = layer.shadowDy,
                        valueRange = -40f..40f,
                        defaultValue = 6f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { dy, cont -> onUpdate(cont) { it.copy(shadowDy = dy) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.TEXT_3D -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_enable_3d_extrusion), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.is3D,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(is3D = enable) } }
                    )
                }
                if (layer.is3D) {
                    PrecisionSlider(
                        label = "3D Depth",
                        value = layer.depth3D,
                        valueRange = 1f..36f,
                        defaultValue = 16f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { d, cont -> onUpdate(cont) { it.copy(depth3D = d) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_side_darken),
                        value = layer.darken3D * 100f,
                        valueRange = 0f..85f,
                        defaultValue = 40f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { dk, cont -> onUpdate(cont) { it.copy(darken3D = dk / 100f) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_extrusion_angle),
                        value = layer.angle3D,
                        valueRange = 0f..360f,
                        defaultValue = 30f,
                        fineStep = 5f,
                        unitSuffix = "°",
                        onValueChange = { a, cont -> onUpdate(cont) { it.copy(angle3D = a) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_floor_shadow), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.shadow3DEnabled,
                        onCheckedChange = { en -> onUpdate(false) { it.copy(shadow3DEnabled = en) } }
                    )
                }
                if (layer.shadow3DEnabled) {
                    ColorPaletteRow(
                        colors = listOf(0x66000000L, 0xAA000000L, 0x6600E5FFL, 0x66FF007FL),
                        recentColors = recentColors,
                        selectedColor = layer.shadow3DColor,
                        onSelectColor = { c -> onUpdate(false) { it.copy(shadow3DColor = c) } },
                        allowAlpha = true,
                        pickerTitle = "3D Floor Shadow Color"
                    )
                }
            }

            ActiveSubTool.TEXT_REFLECTION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.ui_enable_mirror_reflection), color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = layer.reflectionEnabled,
                        onCheckedChange = { enable -> onUpdate(false) { it.copy(reflectionEnabled = enable) } }
                    )
                }
                if (layer.reflectionEnabled) {
                    PrecisionSlider(
                        label = stringResource(R.string.ui_gap_distance),
                        value = layer.reflectionDistance,
                        valueRange = 0f..48f,
                        defaultValue = 4f,
                        fineStep = 1f,
                        unitSuffix = "px",
                        onValueChange = { d, cont -> onUpdate(cont) { it.copy(reflectionDistance = d) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                    PrecisionSlider(
                        label = stringResource(R.string.ui_reflection_opacity),
                        value = layer.reflectionOpacity * 100f,
                        valueRange = 5f..85f,
                        defaultValue = 35f,
                        fineStep = 2f,
                        unitSuffix = "%",
                        onValueChange = { op, cont -> onUpdate(cont) { it.copy(reflectionOpacity = op / 100f) } },
                        onValueChangeFinished = onFinishContinuousEdit
                    )
                }
            }

            ActiveSubTool.LAYER_BLEND -> {
                Text(stringResource(R.string.ui_composite_blend_mode), color = Color(0xFFCCCCCC), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (mode in LayerBlendMode.entries) {
                        FilterChip(
                            selected = layer.blendMode == mode,
                            onClick = { onUpdate(false) { it.copy(blendMode = mode) } },
                            label = { Text(mode.localizedLabel(), fontSize = 12.sp) }
                        )
                    }
                }
            }

            else -> {}
        }
    }
}
