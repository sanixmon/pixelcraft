package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.layout.ColumnScope
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
