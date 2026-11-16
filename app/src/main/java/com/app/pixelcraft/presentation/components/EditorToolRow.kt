package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.R
import com.app.pixelcraft.ui.theme.EditorColors

/** Entries of the top-level CapCut-style tool row, in display order. */
enum class EditorTool {
    TEMPLATES, TEXT, DATE, SHAPES, PHOTO, CANVAS, EFFECTS, LAYERS
}

/** Top-level scrolling icon row: one icon + one-word label per tool. */
@Composable
fun EditorToolRow(
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(EditorColors.Panel)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (tool in EditorTool.entries) {
            val (icon, label) = toolIconAndLabel(tool)
            EditorToolItem(icon = icon, label = label, onClick = { onToolSelected(tool) })
        }
    }
}

@Composable
private fun toolIconAndLabel(tool: EditorTool): Pair<ImageVector, String> = when (tool) {
    EditorTool.TEMPLATES -> Icons.Default.DashboardCustomize to stringResource(R.string.nav_templates)
    EditorTool.TEXT -> Icons.Default.TextFields to stringResource(R.string.nav_text)
    EditorTool.DATE -> Icons.Default.CalendarToday to stringResource(R.string.nav_date)
    EditorTool.SHAPES -> Icons.Default.Category to stringResource(R.string.nav_shapes)
    EditorTool.PHOTO -> Icons.Default.AddPhotoAlternate to stringResource(R.string.ui_photo)
    EditorTool.CANVAS -> Icons.Default.ColorLens to stringResource(R.string.nav_canvas)
    EditorTool.EFFECTS -> Icons.Default.AutoAwesome to stringResource(R.string.nav_effects)
    EditorTool.LAYERS -> Icons.Default.Layers to stringResource(R.string.nav_layers)
}

@Composable
private fun EditorToolItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(min = 60.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EditorColors.TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = EditorColors.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Second level of the tool row: a back arrow pinned on the left and the tool's own
 * sub-tool row (or compact settings) beside it.
 */
@Composable
fun EditorToolLevel(
    onBack: () -> Unit,
    showBack: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorColors.Panel),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.ui_back),
                    tint = EditorColors.TextPrimary
                )
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(EditorColors.Border)
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

/** Half-height sheet over the editor, used for pickers too big for the compact panel. */
@Composable
fun BoxScope.EditorHalfSheet(
    title: String?,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(EditorColors.Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    )
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .fillMaxHeight(0.5f)
            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(EditorColors.Surface)
            // Swallow taps so they don't reach the scrim.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .navigationBarsPadding()
    ) {
        if (title != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = EditorColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.ui_close),
                        tint = EditorColors.TextSecondary
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            Column { content() }
        }
    }
}
