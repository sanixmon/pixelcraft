package com.app.pixelcraft

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.pixelcraft.presentation.PixelCraftScreen
import com.app.pixelcraft.presentation.PixelCraftViewModel
import com.app.pixelcraft.ui.theme.PixelCraftTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PixelCraftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            PixelCraftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PixelCraftScreen(viewModel = viewModel)
                }
            }
        }
    }
}
