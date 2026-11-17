package com.app.pixelcraft.presentation.components.tools

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.EditorColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatShapes
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.ImageAdjustments
import com.app.pixelcraft.data.model.Layer
import com.app.pixelcraft.data.model.LayerBlendMode
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PhotoFilterPreset
import com.app.pixelcraft.data.model.ShapeCategory
import com.app.pixelcraft.data.model.ShapeType
import com.app.pixelcraft.presentation.ActiveSubTool
import com.app.pixelcraft.presentation.EyedropperTarget
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow
import kotlin.math.roundToInt

internal val ObjectPalette = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFF00B0FF, 0xFFFF0055,
    0xFFFFEB3B, 0xFF00E676, 0xFF651FFF, 0xFFFF9100
)

@Composable
internal fun ObjectToolItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    iconRotation: Float = 0f,
    onClick: () -> Unit
) {
    val activeColor = EditorColors.Accent
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else EditorColors.TextPrimary,
            modifier = Modifier
                .size(22.dp)
                .rotate(iconRotation)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else EditorColors.TextSecondary
        )
    }
}
@Composable
internal fun ColumnScope.ObjectSubToolContent(
    subTool: ActiveSubTool,
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    canvasWidth: Int,
    canvasHeight: Int,
    onNudge: (Float, Float) -> Unit,
    onAlign: (com.app.pixelcraft.presentation.AlignmentAction) -> Unit
) {
    when (subTool) {
        ActiveSubTool.OBJECT_POSITION -> {
            TransformFields(layer = layer, canvasWidth = canvasWidth, canvasHeight = canvasHeight, onUpdate = onUpdate)
            Spacer(modifier = Modifier.height(8.dp))
            TextPositionInspector(onNudge = onNudge)
            TextAlignInspector(onAlign = onAlign)
        }

        ActiveSubTool.OBJECT_SIZE -> ObjectSizeInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.OBJECT_ROTATE -> ObjectRotateInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.SHAPE_TYPE -> ShapeTypeInspector(layer = layer, onUpdate = onUpdate)

        ActiveSubTool.SHAPE_COLOR -> ShapeColorInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

        ActiveSubTool.SHAPE_STROKE -> ShapeStrokeInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

        ActiveSubTool.SHAPE_SHADOW -> ShapeShadowInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

        ActiveSubTool.SHAPE_OPACITY -> ObjectOpacityInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.LAYER_BLEND -> ObjectBlendInspector(layer = layer, onUpdate = onUpdate)

        ActiveSubTool.IMAGE_CROP -> PhotoCropInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.IMAGE_FILTER -> PhotoFilterInspector(layer = layer, onUpdate = onUpdate)

        ActiveSubTool.IMAGE_ADJUST -> PhotoAdjustInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

        ActiveSubTool.IMAGE_STROKE -> PhotoStrokeInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

        ActiveSubTool.IMAGE_MASK -> PhotoMaskInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

        else -> {}
    }
}
