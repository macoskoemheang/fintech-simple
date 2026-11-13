package com.example.m3showcase

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.AppTheme
import com.example.m3showcase.core.designsystem.isDark
import com.example.m3showcase.main.BankApp
import com.example.m3showcase.main.MainUiState
import com.example.m3showcase.main.MainViewModel
import com.example.m3showcase.feature.splash.presentation.BrandSplash
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.m3showcase.core.language.LanguageSwitchController
import com.example.m3showcase.core.language.LanguageSwitchOverlay
import org.koin.android.ext.android.inject

/** AppCompatActivity (not ComponentActivity) so the per-app language applies below Android 13 too. */
class MainActivity : AppCompatActivity() {

    // Activity-scoped so the Activity callbacks below and Compose share one instance.
    private val viewModel: MainViewModel by viewModel()
    private val languageSwitch: LanguageSwitchController by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate: swaps the launch theme for the app theme.
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // Hold the system splash until settings are read, so the first frame has the right theme.
        splash.setKeepOnScreenCondition { viewModel.uiState.value is MainUiState.Loading }
        // BrandSplash starts on an identical frame, so the system splash can go without its own fade.
        splash.setOnExitAnimationListener { it.remove() }
        enableEdgeToEdge()
        setContent {
            // Plays once per launch; not again after rotation or when state is restored.
            var showIntro by rememberSaveable { mutableStateOf(savedInstanceState == null) }
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
                Box(Modifier.fillMaxSize()) {
                    // The first screen composes underneath, so it is ready when the intro fades away.
                    BankApp(isLoggedIn = ready.isLoggedIn, bannerState = banner)
                    if (showIntro) BrandSplash(onFinished = { showIntro = false })
                    // Above everything: covers the Activity recreation when the language changes.
                    LanguageSwitchOverlay(controller = languageSwitch)
                }
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
