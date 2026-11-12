package com.app.pixelcraft.presentation.components.tools

import com.app.pixelcraft.presentation.i18n.localizedLabel
import com.app.pixelcraft.ui.theme.PixelCraftColors
import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
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
fun ObjectToolPanel(
    selectedLayer: Layer?,
    activeSubTool: ActiveSubTool,
    recentColors: List<Long> = emptyList(),
    hasCopiedStyle: Boolean = false,
    onSubToolSelected: (ActiveSubTool) -> Unit,
    onUpdateLayer: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit = {},
    onCopyStyle: () -> Unit = {},
    onPasteStyle: () -> Unit = {},
    onOpenSavedStyles: () -> Unit = {},
    onDeleteLayer: () -> Unit,
    onDuplicateLayer: () -> Unit,
    onBringToFront: () -> Unit,
    onSendToBack: () -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    onAddImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (
        selectedLayer == null || (
            selectedLayer.type != LayerType.SHAPE &&
                selectedLayer.type != LayerType.IMAGE
            )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(PixelCraftColors.Surface)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onOpenDialog(ActiveDialog.ADD_SHAPE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Accent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.FormatShapes, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ui_shapes_balloons), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onAddImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelCraftColors.Success,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.ui_photo_layer), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Hoisted so the tool row keeps its scroll position while an inspector replaces it.
    val toolRowScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PixelCraftColors.Surface)
    ) {
        // The inspector replaces the tool rows in place instead of stacking above them,
        // so an open sub-tool does not cover more of the canvas.
        if (activeSubTool != ActiveSubTool.NONE) {
            ObjectSubToolInspector(
                subTool = activeSubTool,
                layer = selectedLayer,
                recentColors = recentColors,
                onUpdate = onUpdateLayer,
                onFinishContinuousEdit = onFinishContinuousEdit,
                onStartEyedropper = onStartEyedropper,
                onClose = { onSubToolSelected(ActiveSubTool.NONE) }
            )
        } else {
        // Quick Add + Style Clipboard bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelCraftColors.PanelElevated)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniActionPill(
                icon = Icons.Default.FormatShapes,
                label = "+ Shape",
                onClick = { onOpenDialog(ActiveDialog.ADD_SHAPE) }
            )
            MiniActionPill(
                icon = Icons.Default.AddPhotoAlternate,
                label = "+ Photo",
                onClick = onAddImage
            )
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
                .horizontalScroll(toolRowScroll)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selectedLayer.type == LayerType.SHAPE) {
                ObjectToolItem(
                    icon = Icons.Default.Category,
                    label = stringResource(R.string.ui_shape),
                    isActive = activeSubTool == ActiveSubTool.SHAPE_TYPE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_TYPE) }
                )
                ObjectToolItem(
                    icon = Icons.Default.FormatColorFill,
                    label = stringResource(R.string.ui_fill),
                    isActive = activeSubTool == ActiveSubTool.SHAPE_COLOR,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_COLOR) }
                )
                ObjectToolItem(
                    icon = Icons.Default.BorderStyle,
                    label = stringResource(R.string.ui_stroke),
                    isActive = activeSubTool == ActiveSubTool.SHAPE_STROKE,
                    onClick = { onSubToolSelected(ActiveSubTool.SHAPE_STROKE) }
                )
            }

            if (selectedLayer.type == LayerType.IMAGE) {
                ObjectToolItem(
                    icon = Icons.Default.Crop,
                    label = stringResource(R.string.ui_crop),
                    isActive = activeSubTool == ActiveSubTool.IMAGE_CROP,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_CROP) }
                )
                ObjectToolItem(
                    icon = Icons.Default.Tune,
                    label = stringResource(R.string.ui_adjust),
                    isActive = activeSubTool == ActiveSubTool.IMAGE_ADJUST,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_ADJUST) }
                )
                ObjectToolItem(
                    icon = Icons.Default.FilterVintage,
                    label = stringResource(R.string.ui_mask),
                    isActive = activeSubTool == ActiveSubTool.IMAGE_MASK,
                    onClick = { onSubToolSelected(ActiveSubTool.IMAGE_MASK) }
                )
            }

            // Shared across BOTH Shape AND Image layers (Fixes Bug 2.17!)
            ObjectToolItem(
                icon = Icons.Default.Lightbulb,
                label = stringResource(R.string.ui_shadow),
                isActive = activeSubTool == ActiveSubTool.SHAPE_SHADOW,
                onClick = { onSubToolSelected(ActiveSubTool.SHAPE_SHADOW) }
            )
            ObjectToolItem(
                icon = Icons.Default.BlurOn,
                label = stringResource(R.string.ui_blend),
                isActive = activeSubTool == ActiveSubTool.LAYER_BLEND,
                onClick = { onSubToolSelected(ActiveSubTool.LAYER_BLEND) }
            )
            ObjectToolItem(
                icon = Icons.Default.Flip,
                label = stringResource(R.string.ui_flip_h),
                isActive = selectedLayer.flipHorizontal,
                onClick = {
                    onUpdateLayer(false) { it.copy(flipHorizontal = !it.flipHorizontal) }
                }
            )
            ObjectToolItem(
                icon = Icons.Default.Flip,
                label = stringResource(R.string.ui_flip_v),
                isActive = selectedLayer.flipVertical,
                iconRotation = 90f,
                onClick = {
                    onUpdateLayer(false) { it.copy(flipVertical = !it.flipVertical) }
                }
            )
            ObjectToolItem(
                icon = Icons.Default.AspectRatio,
                label = stringResource(R.string.ui_size),
                isActive = activeSubTool == ActiveSubTool.OBJECT_SIZE,
                onClick = { onSubToolSelected(ActiveSubTool.OBJECT_SIZE) }
            )
            ObjectToolItem(
                icon = Icons.Default.Rotate90DegreesCcw,
                label = stringResource(R.string.ui_rotate),
                isActive = activeSubTool == ActiveSubTool.OBJECT_ROTATE,
                onClick = { onSubToolSelected(ActiveSubTool.OBJECT_ROTATE) }
            )
            ObjectToolItem(
                icon = Icons.Default.Opacity,
                label = stringResource(R.string.ui_opacity),
                isActive = activeSubTool == ActiveSubTool.SHAPE_OPACITY,
                onClick = { onSubToolSelected(ActiveSubTool.SHAPE_OPACITY) }
            )
            ObjectToolItem(
                icon = Icons.Default.Layers,
                label = stringResource(R.string.ui_to_front),
                isActive = false,
                onClick = onBringToFront
            )
            ObjectToolItem(
                icon = Icons.Default.Layers,
                label = stringResource(R.string.ui_to_back),
                isActive = false,
                onClick = onSendToBack
            )
            ObjectToolItem(
                icon = Icons.Default.ContentCopy,
                label = stringResource(R.string.action_duplicate),
                isActive = false,
                onClick = onDuplicateLayer
            )
            ObjectToolItem(
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
private fun ObjectToolItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    iconRotation: Float = 0f,
    onClick: () -> Unit
) {
    val activeColor = PixelCraftColors.Accent
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
            tint = if (isActive) activeColor else PixelCraftColors.TextPrimary,
            modifier = Modifier
                .size(22.dp)
                .rotate(iconRotation)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) activeColor else PixelCraftColors.TextSecondary
        )
    }
}

@Composable
private fun ObjectSubToolInspector(
    subTool: ActiveSubTool,
    layer: Layer,
    recentColors: List<Long>,
    onUpdate: (Boolean, (Layer) -> Layer) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    onStartEyedropper: (EyedropperTarget) -> Unit,
    onClose: () -> Unit
) {
    // CapCut-style: back button pinned at the far left, inspector content beside it.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 240.dp)
            .background(PixelCraftColors.PanelElevated),
        verticalAlignment = Alignment.CenterVertically
    ) {
    IconButton(
        onClick = onClose,
        modifier = Modifier
            .padding(start = 6.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(PixelCraftColors.Surface)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.ui_back),
            tint = PixelCraftColors.TextPrimary
        )
    }
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
    ) {
        Text(
            text = subTool.name
                .removePrefix("SHAPE_")
                .removePrefix("OBJECT_")
                .removePrefix("IMAGE_")
                .removePrefix("LAYER_")
                .replace("_", " "),
            fontWeight = FontWeight.Bold,
            color = PixelCraftColors.Accent,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        when (subTool) {
            ActiveSubTool.OBJECT_SIZE -> ObjectSizeInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.OBJECT_ROTATE -> ObjectRotateInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.SHAPE_TYPE -> ShapeTypeInspector(layer = layer, onUpdate = onUpdate)

            ActiveSubTool.SHAPE_COLOR -> ShapeColorInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.SHAPE_STROKE -> ShapeStrokeInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.SHAPE_SHADOW -> ShapeShadowInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            ActiveSubTool.SHAPE_OPACITY -> ObjectOpacityInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.LAYER_BLEND -> ObjectBlendInspector(layer = layer, onUpdate = onUpdate)

            ActiveSubTool.IMAGE_CROP -> PhotoCropInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.IMAGE_ADJUST -> PhotoAdjustInspector(layer = layer, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit)

            ActiveSubTool.IMAGE_MASK -> PhotoMaskInspector(layer = layer, recentColors = recentColors, onUpdate = onUpdate, onFinishContinuousEdit = onFinishContinuousEdit, onStartEyedropper = onStartEyedropper)

            else -> {}
        }
    }
    }
}
