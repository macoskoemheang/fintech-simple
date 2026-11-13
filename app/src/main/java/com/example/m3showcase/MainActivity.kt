package com.example.m3showcase

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.AppTheme
import com.example.m3showcase.core.designsystem.isDark
import com.example.m3showcase.main.BankApp
import com.example.m3showcase.main.MainUiState
import com.example.m3showcase.main.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    // Activity-scoped so the Activity callbacks below and Compose share one instance.
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val banner by viewModel.connectivityBanner.collectAsStateWithLifecycle()

            val ready = uiState as? MainUiState.Ready ?: return@setContent
            val darkTheme = ready.themeMode.isDark()

            // Match the system bar icons to the theme the user picked in-app, not only the system setting.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(LightScrim, DarkScrim) { darkTheme },
                )
                onDispose {}
            }

            AppTheme(darkTheme = darkTheme, eventTheme = ready.eventTheme) {
                BankApp(isLoggedIn = ready.isLoggedIn, bannerState = banner)
            }
        }
    }

    /** Called by the framework on touch-down and key-down only (not every move), so it's cheap. */
    override fun onUserInteraction() {
        super.onUserInteraction()
        viewModel.onUserInteraction()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    private companion object {
        val LightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
