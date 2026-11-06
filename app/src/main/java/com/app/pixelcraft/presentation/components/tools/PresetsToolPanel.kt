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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF232730))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Styles & Presets", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Button(
                onClick = { onOpenDialog(ActiveDialog.SAVE_PROJECT) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Save Current", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Templates Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (preset in DefaultPresets.presets) {
                val cfg = preset.canvasConfig
                val thumbModifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .let { base ->
                        if (cfg.backgroundType == BackgroundType.GRADIENT && cfg.gradientColors.size >= 2) {
                            base.background(Brush.linearGradient(cfg.gradientColors.map { Color(it) }))
                        } else {
                            base.background(Color(cfg.backgroundColor))
                        }
                    }
                val textLayer = preset.initialLayers.firstOrNull { it.type == LayerType.TEXT }
                val previewLabel = textLayer?.text?.lineSequence()?.firstOrNull()?.take(5)
                    ?: preset.title.take(4)
                val previewColor = textLayer?.let { Color(it.textColor) } ?: Color.White

                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B1D24))
                        .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
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
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Saved User Projects
        if (savedProjects.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text("My Projects (${savedProjects.size})", color = Color(0xFFCCCCCC), fontSize = 11.sp)
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B1D24))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onLoadProject(p.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = p.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 140.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onDeleteProject(p.id) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
