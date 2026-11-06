package com.app.pixelcraft.presentation.components.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
    onUpdateFilters: (Boolean, (CanvasFilters) -> CanvasFilters) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
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
                onCheckedChange = { en -> onUpdateFilters(false) { it.copy(vignetteEnabled = en) } }
            )
        }
        if (filters.vignetteEnabled) {
            Text("Intensity: ${(filters.vignetteIntensity * 100).toInt()}%", color = Color(0xFFBBBBBB), fontSize = 11.sp)
            Slider(
                value = filters.vignetteIntensity.coerceIn(0.1f, 0.9f),
                onValueChange = { i -> onUpdateFilters(true) { it.copy(vignetteIntensity = i) } },
                onValueChangeFinished = onFinishContinuousEdit,
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
                onCheckedChange = { en -> onUpdateFilters(false) { it.copy(stripesEnabled = en) } }
            )
        }
        if (filters.stripesEnabled) {
            Text("Spacing: ${filters.stripesSpacing.toInt()} px", color = Color(0xFFBBBBBB), fontSize = 11.sp)
            Slider(
                value = filters.stripesSpacing.coerceIn(4f..30f),
                onValueChange = { s -> onUpdateFilters(true) { it.copy(stripesSpacing = s) } },
                onValueChangeFinished = onFinishContinuousEdit,
                valueRange = 4f..30f
            )
        }
    }
}
