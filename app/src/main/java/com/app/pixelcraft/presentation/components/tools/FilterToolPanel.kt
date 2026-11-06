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
import com.app.pixelcraft.presentation.components.dialogs.ColorPaletteRow

private val VignettePalette = listOf(
    0xFF000000L, 0xFF0A0A18L, 0xFF2B091BL, 0xFF06232EL, 0xFFFFFFFFL
)

@Composable
fun FilterToolPanel(
    filters: CanvasFilters,
    recentColors: List<Long> = emptyList(),
    onUpdateFilters: (Boolean, (CanvasFilters) -> CanvasFilters) -> Unit,
    onFinishContinuousEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 235.dp)
            .background(Color(0xFF1B1F2B))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "Canvas Post-Processing Effects",
            color = Color(0xFF00E5FF),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Vignette
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vignette Frame", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = filters.vignetteEnabled,
                onCheckedChange = { en -> onUpdateFilters(false) { it.copy(vignetteEnabled = en) } }
            )
        }
        if (filters.vignetteEnabled) {
            ColorPaletteRow(
                colors = VignettePalette,
                recentColors = recentColors,
                selectedColor = filters.vignetteColor,
                onSelectColor = { c -> onUpdateFilters(false) { it.copy(vignetteColor = c) } },
                allowAlpha = true,
                pickerTitle = "Vignette Color"
            )
            PrecisionSlider(
                label = "Vignette Intensity",
                value = filters.vignetteIntensity,
                valueRange = 0.1f..0.9f,
                defaultValue = 0.5f,
                fineStep = 0.05f,
                unitSuffix = "%",
                valueFormatter = { "${(it * 100).toInt()}" },
                onValueChange = { i, cont -> onUpdateFilters(cont) { it.copy(vignetteIntensity = i) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }

        // 2. Film Grain (§ 7 Phase 4)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Film Grain Texture", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = filters.grainEnabled,
                onCheckedChange = { en -> onUpdateFilters(false) { it.copy(grainEnabled = en) } }
            )
        }
        if (filters.grainEnabled) {
            PrecisionSlider(
                label = "Grain Amount",
                value = filters.grainIntensity,
                valueRange = 0.03f..0.60f,
                defaultValue = 0.15f,
                fineStep = 0.02f,
                unitSuffix = "%",
                valueFormatter = { "${(it * 100).toInt()}" },
                onValueChange = { g, cont -> onUpdateFilters(cont) { it.copy(grainIntensity = g) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }

        // 3. Retro Scanlines
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Retro Scanlines", color = Color.White, fontSize = 12.sp)
            Switch(
                checked = filters.stripesEnabled,
                onCheckedChange = { en -> onUpdateFilters(false) { it.copy(stripesEnabled = en) } }
            )
        }
        if (filters.stripesEnabled) {
            PrecisionSlider(
                label = "Line Spacing",
                value = filters.stripesSpacing,
                valueRange = 4f..30f,
                defaultValue = 12f,
                fineStep = 1f,
                unitSuffix = "px",
                onValueChange = { s, cont -> onUpdateFilters(cont) { it.copy(stripesSpacing = s) } },
                onValueChangeFinished = onFinishContinuousEdit
            )
        }
    }
}
