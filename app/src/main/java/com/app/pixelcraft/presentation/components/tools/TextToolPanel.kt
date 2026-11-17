package com.app.pixelcraft.presentation.components.tools

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.presentation.i18n.labelRes
import com.app.pixelcraft.ui.theme.EditorColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.material.icons.filled.Texture
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Gradient
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
import androidx.compose.material3.HorizontalDivider
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

@Composable
internal fun ColumnScope.TextSubToolContent(
    subTool: ActiveSubTool,
    layer: Layer,
    recentColors: List<Long>,
    favoriteFontKeys: Set<String>,
    recentFontKeys: List<String>,
    availableFontsCount: Int,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onApplyTextCase: (TextCaseMode) -> Unit,
    onToggleFavoriteFont: (String) -> Unit,
    onImportFont: () -> Unit,
    onPickDeviceFontsFolder: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onNudge: (Float, Float) -> Unit,
    onAlign: (AlignmentAction) -> Unit,
    canvasWidth: Int,
    canvasHeight: Int
) {
    when (subTool) {
        ActiveSubTool.TEXT_FONT -> TextFontInspector(layer = layer, favoriteFontKeys = favoriteFontKeys, recentFontKeys = recentFontKeys, availableFontsCount = availableFontsCount, onUpdate = onUpdate, onToggleFavoriteFont = onToggleFavoriteFont, onImportFont = onImportFont, onPickDeviceFontsFolder = onPickDeviceFontsFolder)

        ActiveSubTool.TEXT_POSITION -> {
            TransformFields(layer = layer, canvasWidth = canvasWidth, canvasHeight = canvasHeight, onUpdate = onUpdate)
            Spacer(modifier = Modifier.height(8.dp))
            TextPositionInspector(onNudge = onNudge)
            Spacer(modifier = Modifier.height(8.dp))
            TextAlignInspector(onAlign = onAlign)
        }

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

        ActiveSubTool.TEXT_PRESETS -> TextPresetsInspector(layer = layer, onUpdate = onUpdate)

        ActiveSubTool.TEXT_INNER_SHADOW -> TextInnerShadowInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.TEXT_EMBOSS -> TextEmbossInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.LAYER_BLEND -> TextBlendInspector(layer = layer, onUpdate = onUpdate)

        else -> {}
    }
}
