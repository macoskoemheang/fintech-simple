package com.example.m3showcase.feature.auth.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.AppTheme
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.BrandColors
import com.example.m3showcase.core.designsystem.FestivalHeroArt
import com.example.m3showcase.feature.auth.domain.usecase.LoginWithPinUseCase
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

/** Stateful entry: owns the ViewModel and collects one-time events. */
@Composable
fun LoginRoute(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.ShowMessage -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }
    LoginScreen(state = state, snackbarHostState = snackbarHostState, onAction = viewModel::onAction)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    state: LoginViewState,
    snackbarHostState: SnackbarHostState,
    onAction: (LoginViewAction) -> Unit,
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = Color.Transparent) { _ ->
        Box(Modifier.fillMaxSize().background(BankTheme.colors.heroBrush)) {
        FestivalHeroArt(modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            BrandMark()
            Spacer(Modifier.height(20.dp))
            Text(
                "Welcome back",
                style = MaterialTheme.typography.headlineSmall,
                color = BankTheme.colors.onHero,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Enter your 4-digit PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = BankTheme.colors.onHeroVariant,
            )
            Spacer(Modifier.height(32.dp))

            Box(Modifier.height(40.dp), contentAlignment = Alignment.Center) {
                AnimatedContent(targetState = state.isLoading, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "pin") { loading ->
                    if (loading) {
                        LoadingIndicator(color = BankTheme.colors.heroAccent)
                    } else {
                        PinDots(filled = state.pin.length, failureCount = state.failureCount)
                    }
                }
            }
            Text(
                text = state.error.orEmpty(),
                color = if (BankTheme.eventTheme.isFestival) Color(0xFFFFDAD6) else BrandColors.Coral,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp).height(20.dp),
            )

            Spacer(Modifier.weight(1f))
            PinPad(enabled = !state.isLoading, onAction = onAction)
            TextButton(onClick = { onAction(LoginViewAction.OnForgotPinClicked) }) {
                Text("Forgot PIN?", color = BankTheme.colors.heroAccent)
            }
            Text(
                "Demo PIN: 1234",
                style = MaterialTheme.typography.labelSmall,
                color = BankTheme.colors.onHeroVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BrandMark() {
    Surface(
        shape = MaterialShapes.Cookie9Sided.toShape(),
        color = BrandColors.Cyan,
        modifier = Modifier.size(84.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("N", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

/** Filled dots spring-scale in as a Sunny shape; the row shakes on a wrong PIN. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PinDots(filled: Int, failureCount: Int) {
    val shake = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(failureCount) {
        if (failureCount == 0) return@LaunchedEffect
        haptics.performHapticFeedback(HapticFeedbackType.Reject) // Stronger buzz alongside the shake.
        for (target in listOf(-24f, 20f, -14f, 8f, 0f)) {
            shake.animateTo(target, spring(stiffness = Spring.StiffnessHigh))
        }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
    ) {
        repeat(LoginWithPinUseCase.PIN_LENGTH) { index ->
            val isFilled = index < filled
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0.6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot",
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
                    .then(
                        if (isFilled) Modifier.background(BankTheme.colors.heroAccent, MaterialShapes.Sunny.toShape())
                        else Modifier.border(2.dp, BankTheme.colors.onHeroVariant, CircleShape)
                    ),
            )
        }
    }
}

@Composable
private fun PinPad(enabled: Boolean, onAction: (LoginViewAction) -> Unit) {
    val rows = listOf("123", "456", "789")
    // Haptics are pure UI feedback, so they live here rather than in the ViewModel.
    // performHapticFeedback needs no VIBRATE permission and respects the system "touch vibration" setting.
    val haptics = LocalHapticFeedback.current
    val onKey: (LoginViewAction) -> Unit = { action ->
        haptics.performHapticFeedback(HapticFeedbackType.KeyboardTap)
        onAction(action)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                row.forEach { digit -> PinKey(digit, enabled) { onKey(LoginViewAction.OnDigitClicked(digit)) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.size(76.dp))
            PinKey('0', enabled) { onKey(LoginViewAction.OnDigitClicked('0')) }
            Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                IconButton(onClick = { onKey(LoginViewAction.OnDeleteClicked) }, enabled = enabled) {
                    Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Delete", tint = Color.White)
                }
            }
        }
    }
}

/** Expressive button: `ButtonDefaults.shapes()` morphs circle → squircle when pressed. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PinKey(digit: Char, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.10f),
            contentColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = Color.White.copy(alpha = 0.4f),
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = Modifier.size(76.dp),
    ) {
        Text(digit.toString(), style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    AppTheme(darkTheme = true) {
        LoginScreen(LoginViewState(pin = "12"), remember { SnackbarHostState() }) {}
    }
}
