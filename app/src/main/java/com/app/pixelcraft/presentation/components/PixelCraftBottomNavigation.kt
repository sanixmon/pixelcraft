package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pixelcraft.data.model.BottomNavTab

@Composable
fun PixelCraftBottomNavigation(
    activeTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E212B))
            .navigationBarsPadding()
            .height(48.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavIconItem(
            contentDescription = "Projects & Presets",
            icon = Icons.Default.DashboardCustomize,
            isSelected = activeTab == BottomNavTab.PRESETS,
            onClick = { onTabSelected(BottomNavTab.PRESETS) }
        )
        BottomNavIconItem(
            contentDescription = "Text Tools",
            icon = Icons.Default.TextFields,
            isSelected = activeTab == BottomNavTab.TEXT,
            onClick = { onTabSelected(BottomNavTab.TEXT) }
        )
        BottomNavIconItem(
            contentDescription = "Shapes & Photos",
            icon = Icons.Default.Category,
            isSelected = activeTab == BottomNavTab.OBJECTS,
            onClick = { onTabSelected(BottomNavTab.OBJECTS) }
        )
        BottomNavIconItem(
            contentDescription = "Canvas Background",
            icon = Icons.Default.ColorLens,
            isSelected = activeTab == BottomNavTab.BACKGROUND,
            onClick = { onTabSelected(BottomNavTab.BACKGROUND) }
        )
        BottomNavIconItem(
            contentDescription = "Filters",
            icon = Icons.Default.AutoAwesome,
            isSelected = activeTab == BottomNavTab.FILTERS,
            onClick = { onTabSelected(BottomNavTab.FILTERS) }
        )
    }
}

@Composable
private fun BottomNavIconItem(
    contentDescription: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    val inactiveColor = Color(0xFF9E9E9E)

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
