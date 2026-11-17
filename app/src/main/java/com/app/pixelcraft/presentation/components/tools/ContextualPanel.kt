package com.app.pixelcraft.presentation.components.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.R
import com.app.pixelcraft.ui.theme.EditorColors

/**
 * One entry in a category: either a property tool that opens controls ([key] != null) or a
 * one-shot action ([onAction] != null), e.g. Duplicate.
 */
internal class PanelTool<T>(
    val icon: ImageVector,
    val label: String,
    val key: T? = null,
    val onAction: (() -> Unit)? = null
)

internal class PanelCategory<T>(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val tools: List<PanelTool<T>>
)

/**
 * CapCut-style contextual editor shell, filling the fixed-height tool area:
 *
 * - bottom: the category toolbar, always visible while something is selected;
 * - above it: the active category's tools as chips, then the open tool's controls.
 *
 * The area never changes height, so the canvas above stays where it is. [activeTool] is
 * owned by the caller (the ViewModel for layers, local state for groups); [noTool] means
 * no property tool is open. [selectionKey] resets the category when the selection changes.
 */
@Composable
internal fun <T> ContextualPanel(
    categories: List<PanelCategory<T>>,
    activeTool: T,
    noTool: T,
    selectionKey: Any,
    onToolSelected: (T) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(T) -> Unit
) {
    fun categoryOf(tool: T): PanelCategory<T>? = categories.firstOrNull { c -> c.tools.any { it.key == tool } }

    var activeCategoryId by remember(selectionKey) {
        mutableStateOf(categoryOf(activeTool)?.id ?: categories.first().id)
    }
    // Follow tool changes coming from elsewhere (e.g. the canvas opens the font picker).
    LaunchedEffect(activeTool) {
        categoryOf(activeTool)?.let { activeCategoryId = it.id }
    }
    val category = categories.firstOrNull { it.id == activeCategoryId } ?: categories.first()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorColors.Panel)
    ) {
        AnimatedContent(
            targetState = category.id,
            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(120)) },
            label = "category",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { id ->
            val shown = categories.firstOrNull { it.id == id } ?: category
            Column(modifier = Modifier.fillMaxSize()) {
                ToolChipRow(shown.tools, activeTool, onToolSelected)
                val toolOpen = shown.tools.any { it.key == activeTool } && activeTool != noTool
                if (toolOpen) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        content(activeTool)
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(EditorColors.Border)
        )
        CategoryToolbar(
            categories = categories,
            activeId = category.id,
            onSelect = { c ->
                activeCategoryId = c.id
                // Open the category's first property tool straight away; action-only
                // categories just show their actions.
                onToolSelected(c.tools.firstOrNull { it.key != null }?.key ?: noTool)
            },
            onDone = onDone
        )
    }
}

@Composable
private fun <T> ToolChipRow(tools: List<PanelTool<T>>, activeTool: T, onToolSelected: (T) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (tool in tools) {
            val key = tool.key
            val selected = key != null && key == activeTool
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) EditorColors.Accent.copy(alpha = 0.16f) else EditorColors.SurfaceElevated)
                    .clickable { if (key != null) onToolSelected(key) else tool.onAction?.invoke() }
                    .semantics { this.selected = selected }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    tool.icon,
                    contentDescription = null,
                    tint = if (selected) EditorColors.Accent else EditorColors.TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    tool.label,
                    color = if (selected) EditorColors.Accent else EditorColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun <T> CategoryToolbar(
    categories: List<PanelCategory<T>>,
    activeId: String,
    onSelect: (PanelCategory<T>) -> Unit,
    onDone: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(EditorColors.Surface)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDone, modifier = Modifier.size(48.dp)) {
            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.ui_done), tint = EditorColors.TextPrimary)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(32.dp)
                .background(EditorColors.Border)
        )
        for (c in categories) {
            val selected = c.id == activeId
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(c) }
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = c.icon,
                    contentDescription = null,
                    tint = if (selected) EditorColors.Accent else EditorColors.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = c.label,
                    color = if (selected) EditorColors.Accent else EditorColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .width(18.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (selected) EditorColors.Accent else Color.Transparent)
                )
            }
        }
    }
}
