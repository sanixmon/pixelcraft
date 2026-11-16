package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.app.pixelcraft.data.presets.BundledFont
import com.app.pixelcraft.data.presets.FontCatalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.presentation.i18n.labelRes
import com.app.pixelcraft.ui.theme.EditorColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.TextAlignment
import com.app.pixelcraft.domain.TextCaseMode
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.AlignmentAction
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

/**
 * CapCut-style font picker: category chips with search/import icons on one line,
 * then a 3-column grid where each tile shows the font's name set in that font.
 */
@Composable
internal fun ColumnScope.TextFontInspector(
    layer: Layer,
    favoriteFontKeys: Set<String>,
    availableFontsCount: Int,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onToggleFavoriteFont: (String) -> Unit,
    onImportFont: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchOpen by remember { mutableStateOf(false) }
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

    // Header: search field (when open) or category chips, with search + import icons.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (searchOpen) {
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(color = EditorColors.TextPrimary, fontSize = 14.sp),
                cursorBrush = SolidColor(EditorColors.Accent),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EditorColors.SurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                decorationBox = { inner ->
                    Box {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = stringResource(R.string.ui_search_fonts, allFonts.size),
                                color = EditorColors.TextMuted,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        inner()
                    }
                }
            )
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (cat in categories) {
                    FontCategoryChip(
                        label = when (cat) {
                            "All" -> stringResource(R.string.ui_tab_all)
                            "Favorites" -> stringResource(R.string.ui_font_cat_favorites)
                            "Imported" -> stringResource(R.string.ui_font_cat_imported)
                            else -> cat
                        },
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }
        IconButton(
            onClick = {
                if (searchOpen) searchQuery = ""
                searchOpen = !searchOpen
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (searchOpen) Icons.Default.Close else Icons.Default.Search,
                contentDescription = stringResource(R.string.ui_search_fonts, allFonts.size),
                tint = EditorColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onImportFont, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.ui_import),
                tint = EditorColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 3-column grid built from rows: the inspector already scrolls vertically,
    // so a lazy grid can't be nested here.
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (rowFonts in filteredFonts.chunked(FontGridColumns)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (font in rowFonts) {
                    FontTile(
                        font = font,
                        isSelected = layer.fontKey == font.key,
                        isFavorite = font.key in favoriteFontKeys,
                        onSelect = { onUpdate(false) { it.copy(fontKey = font.key) } },
                        onToggleFavorite = { onToggleFavoriteFont(font.key) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(FontGridColumns - rowFonts.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private const val FontGridColumns = 3

@Composable
private fun FontCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) EditorColors.TextPrimary else EditorColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) EditorColors.Background else EditorColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

@Composable
private fun FontTile(
    font: BundledFont,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composeFamily = remember(font.key) { FontCatalog.resolveComposeFontFamily(font.key) }
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) EditorColors.Accent.copy(alpha = 0.16f) else EditorColors.SurfaceElevated)
            .border(
                width = 1.5.dp,
                color = if (isSelected) EditorColors.Accent else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onSelect)
    ) {
        Text(
            text = font.displayName,
            fontFamily = composeFamily,
            color = if (isSelected) EditorColors.Accent else EditorColors.TextPrimary,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 10.dp)
        )
        // Small star in the corner; only shown solid when the font is a favorite.
        Icon(
            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = stringResource(R.string.ui_favorite_named, font.displayName),
            tint = if (isFavorite) EditorColors.Accent else EditorColors.TextMuted,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleFavorite)
                .padding(3.dp)
        )
    }
}

@Composable
internal fun ColumnScope.TextSizeInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
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

@Composable
internal fun ColumnScope.TextSpacingInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
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

@Composable
internal fun ColumnScope.TextCurveInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit
) {
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

@Composable
internal fun ColumnScope.TextStyleInspector(
    layer: Layer,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onApplyTextCase: (TextCaseMode) -> Unit
) {
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
    Text(stringResource(R.string.ui_transform_case), color = EditorColors.TextSecondary, fontSize = 12.sp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { onApplyTextCase(TextCaseMode.UPPERCASE) },
            border = BorderStroke(1.dp, EditorColors.Border)
        ) {
            Text(stringResource(R.string.ui_uppercase), color = EditorColors.TextPrimary, fontSize = 11.sp)
        }
        OutlinedButton(
            onClick = { onApplyTextCase(TextCaseMode.LOWERCASE) },
            border = BorderStroke(1.dp, EditorColors.Border)
        ) {
            Text("lowercase", color = EditorColors.TextPrimary, fontSize = 11.sp)
        }
        OutlinedButton(
            onClick = { onApplyTextCase(TextCaseMode.TITLE_CASE) },
            border = BorderStroke(1.dp, EditorColors.Border)
        ) {
            Text(stringResource(R.string.ui_title_case), color = EditorColors.TextPrimary, fontSize = 11.sp)
        }
    }
}
