package com.app.pixelcraft

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.pixelcraft.presentation.HomeScreen
import com.app.pixelcraft.presentation.PixelCraftScreen
import com.app.pixelcraft.presentation.PixelCraftViewModel
import com.app.pixelcraft.ui.theme.PixelCraftTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PixelCraftViewModel by viewModels()
    private var pendingExternalImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !viewModel.uiState.value.isReady }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        val initialUri = if (savedInstanceState == null) extractIncomingImageUri(intent) else null
        if (initialUri != null) {
            viewModel.startCanvasFromPhoto(initialUri)
        }

        setContent {
            PixelCraftTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startRoute = if (initialUri != null) ROUTE_EDITOR else ROUTE_HOME

                    LaunchedEffect(pendingExternalImageUri) {
                        val incoming = pendingExternalImageUri ?: return@LaunchedEffect
                        pendingExternalImageUri = null
                        viewModel.startCanvasFromPhoto(incoming)
                        navController.navigate(ROUTE_EDITOR) {
                            launchSingleTop = true
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startRoute
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = {
                                    navController.navigate(ROUTE_EDITOR) {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(ROUTE_EDITOR) {
                            PixelCraftScreen(
                                viewModel = viewModel,
                                onNavigateHome = {
                                    if (!navController.popBackStack()) {
                                        navController.navigate(ROUTE_HOME) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractIncomingImageUri(intent)?.let { uri ->
            pendingExternalImageUri = uri
        }
    }

    private fun extractIncomingImageUri(intent: Intent?): Uri? {
        if (intent == null) return null
        return when (intent.action) {
            Intent.ACTION_SEND -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
            }
            Intent.ACTION_EDIT, Intent.ACTION_VIEW -> intent.data
            else -> null
        }
    }

    companion object {
        private const val ROUTE_HOME = "home"
        private const val ROUTE_EDITOR = "editor"
    }
}
