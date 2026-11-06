package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.data.model.CanvasFilters

@Composable
fun FilterToolPanel(
    filters: CanvasFilters,
    onUpdateFilters: ((CanvasFilters) -> CanvasFilters) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFF232730))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text("Post-Processing & Magic Filters", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Vignette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vignette", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = filters.vignetteEnabled,
                onCheckedChange = { en -> onUpdateFilters { it.copy(vignetteEnabled = en) } }
            )
        }
        if (filters.vignetteEnabled) {
            Text("Intensity: ${(filters.vignetteIntensity * 100).toInt()}%", color = Color(0xFFBBBBBB), fontSize = 11.sp)
            Slider(
                value = filters.vignetteIntensity,
                onValueChange = { i -> onUpdateFilters { it.copy(vignetteIntensity = i) } },
                valueRange = 0.1f..0.9f
            )
        }

        // Stripes / Scanlines
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("TV Scanlines", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = filters.stripesEnabled,
                onCheckedChange = { en -> onUpdateFilters { it.copy(stripesEnabled = en) } }
            )
        }
        if (filters.stripesEnabled) {
            Text("Spacing: ${filters.stripesSpacing.toInt()} px", color = Color(0xFFBBBBBB), fontSize = 11.sp)
            Slider(
                value = filters.stripesSpacing,
                onValueChange = { s -> onUpdateFilters { it.copy(stripesSpacing = s) } },
                valueRange = 4f..30f
            )
        }

        // Brightness
        Text("Brightness: ${(filters.brightness * 100).toInt()}", color = Color.White, fontSize = 12.sp)
        Slider(
            value = filters.brightness,
            onValueChange = { b -> onUpdateFilters { it.copy(brightness = b) } },
            valueRange = -0.5f..0.5f
        )

        // Contrast
        Text("Contrast: ${(filters.contrast * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
        Slider(
            value = filters.contrast,
            onValueChange = { c -> onUpdateFilters { it.copy(contrast = c) } },
            valueRange = 0.5f..2.0f
        )
    }
}
