package com.app.pixelcraft.presentation.components

import com.app.pixelcraft.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
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
            .background(Color(0xFF151821))
            .navigationBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavTabItem(
            label = stringResource(R.string.nav_templates),
            icon = Icons.Default.DashboardCustomize,
            isSelected = activeTab == BottomNavTab.PRESETS,
            onClick = { onTabSelected(BottomNavTab.PRESETS) }
        )
        BottomNavTabItem(
            label = stringResource(R.string.nav_text),
            icon = Icons.Default.TextFields,
            isSelected = activeTab == BottomNavTab.TEXT,
            onClick = { onTabSelected(BottomNavTab.TEXT) }
        )
        BottomNavTabItem(
            label = stringResource(R.string.nav_objects),
            icon = Icons.Default.Category,
            isSelected = activeTab == BottomNavTab.OBJECTS,
            onClick = { onTabSelected(BottomNavTab.OBJECTS) }
        )
        BottomNavTabItem(
            label = stringResource(R.string.nav_canvas),
            icon = Icons.Default.ColorLens,
            isSelected = activeTab == BottomNavTab.BACKGROUND,
            onClick = { onTabSelected(BottomNavTab.BACKGROUND) }
        )
        BottomNavTabItem(
            label = stringResource(R.string.nav_effects),
            icon = Icons.Default.AutoAwesome,
            isSelected = activeTab == BottomNavTab.FILTERS,
            onClick = { onTabSelected(BottomNavTab.FILTERS) }
        )
    }
}

@Composable
private fun BottomNavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = Color(0xFF00E5FF)
    val inactiveColor = Color(0xFF949CB0)

    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor,
            maxLines = 1
        )
    }
}
