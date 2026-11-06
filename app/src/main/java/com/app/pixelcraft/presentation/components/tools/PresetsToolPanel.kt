package com.app.pixelcraft.presentation.components.tools

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.ActiveDialog
import com.app.pixelcraft.data.model.BackgroundType
import com.app.pixelcraft.data.model.LayerType
import com.app.pixelcraft.data.model.PresetCategory
import com.app.pixelcraft.data.model.ProjectData
import com.app.pixelcraft.data.model.StylePreset
import com.app.pixelcraft.data.presets.DefaultPresets

@Composable
fun PresetsToolPanel(
    savedProjects: List<ProjectData>,
    onApplyPreset: (StylePreset) -> Unit,
    onLoadProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onOpenDialog: (ActiveDialog) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PresetCategory?>(null) }
    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == null) {
            DefaultPresets.presets
        } else {
            DefaultPresets.presets.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 235.dp)
            .background(Color(0xFF1B1F2B))
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Templates (${filteredPresets.size})",
                color = Color(0xFF00E5FF),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Button(
                onClick = { onOpenDialog(ActiveDialog.SAVE_PROJECT) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3242)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Project", fontSize = 12.sp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All", fontSize = 11.sp) }
            )
            for (cat in PresetCategory.entries) {
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (preset in filteredPresets) {
                val cfg = preset.canvasConfig
                val thumbModifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .let { base ->
                        if (cfg.backgroundType == BackgroundType.GRADIENT && cfg.gradientColors.size >= 2) {
                            base.background(Brush.linearGradient(cfg.gradientColors.map { Color(it) }))
                        } else {
                            base.background(Color(cfg.backgroundColor))
                        }
                    }
                val textLayer = preset.initialLayers.firstOrNull { it.type == LayerType.TEXT }
                val previewLabel = textLayer?.text?.lineSequence()?.firstOrNull()?.take(6)
                    ?: preset.title.take(4)
                val previewColor = textLayer?.let { Color(it.textColor) } ?: Color.White

                Column(
                    modifier = Modifier
                        .width(114.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF13161F))
                        .border(1.dp, Color(0xFF2B3140), RoundedCornerShape(10.dp))
                        .clickable { onApplyPreset(preset) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = thumbModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = previewLabel,
                            color = previewColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = preset.category.label,
                        color = Color(0xFF8E96A8),
                        fontSize = 10.sp
                    )
                }
            }
        }

        if (savedProjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Recent Saved Projects (${savedProjects.size})", color = Color(0xFFCCCCCC), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (p in savedProjects) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF13161F))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .clickable { onLoadProject(p.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = p.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onDeleteProject(p.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete ${p.name}", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
