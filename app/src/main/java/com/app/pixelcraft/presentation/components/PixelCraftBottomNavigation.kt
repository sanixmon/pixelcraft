package com.app.pixelcraft.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .height(56.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "My Projects",
            icon = Icons.Default.DashboardCustomize,
            isSelected = activeTab == BottomNavTab.PRESETS,
            onClick = { onTabSelected(BottomNavTab.PRESETS) }
        )
        BottomNavItem(
            label = "A (Text)",
            icon = Icons.Default.TextFields,
            isSelected = activeTab == BottomNavTab.TEXT,
            onClick = { onTabSelected(BottomNavTab.TEXT) }
        )
        BottomNavItem(
            label = "Objects",
            icon = Icons.Default.Category,
            isSelected = activeTab == BottomNavTab.OBJECTS,
            onClick = { onTabSelected(BottomNavTab.OBJECTS) }
        )
        BottomNavItem(
            label = "Canvas",
            icon = Icons.Default.ColorLens,
            isSelected = activeTab == BottomNavTab.BACKGROUND,
            onClick = { onTabSelected(BottomNavTab.BACKGROUND) }
        )
        BottomNavItem(
            label = "Wand",
            icon = Icons.Default.AutoAwesome,
            isSelected = activeTab == BottomNavTab.FILTERS,
            onClick = { onTabSelected(BottomNavTab.FILTERS) }
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    val inactiveColor = Color(0xFF9E9E9E)

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
