package com.example.m3showcase.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import com.example.m3showcase.core.designsystem.StatusBarIcons
import com.example.m3showcase.core.designsystem.AppBackground
import androidx.compose.ui.graphics.Color
import com.example.m3showcase.core.designsystem.component.ConnectivityBanner
import com.example.m3showcase.core.designsystem.component.ConnectivityBannerState
import com.example.m3showcase.core.navigation.AppNavigator
import com.example.m3showcase.core.navigation.Destination
import com.example.m3showcase.core.navigation.TopLevelDestination
import com.example.m3showcase.feature.accounts.presentation.history.TransactionHistoryRoute
import com.example.m3showcase.feature.accounts.presentation.list.AccountsRoute
import com.example.m3showcase.feature.auth.presentation.LoginRoute
import com.example.m3showcase.feature.home.presentation.HomeRoute
import com.example.m3showcase.feature.receipt.presentation.ReceiptRoute
import com.example.m3showcase.feature.report.presentation.TriggerReportRoute
import com.example.m3showcase.feature.settings.presentation.SettingsRoute
import com.example.m3showcase.feature.settings.presentation.theme.ThemeRoute
import com.example.m3showcase.feature.showcase.presentation.ShowcaseRoute
import com.example.m3showcase.feature.transfer.presentation.TransferRoute

/** Tabs fade into each other instead of sliding like pushed screens. */
private val TabTransition =
    NavDisplay.transitionSpec { fadeIn() togetherWith fadeOut() } +
        NavDisplay.popTransitionSpec { fadeIn() togetherWith fadeOut() }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BankApp(isLoggedIn: Boolean, bannerState: ConnectivityBannerState) {
    // Navigation 3: we own the back stack. It is saveable because every key is @Serializable.
    val backStack = rememberNavBackStack(Destination.Login)
    val navigator = remember(backStack) { AppNavigator(backStack) }

    // The session decides auth navigation. Logout from anywhere, or process death
    // with a restored back stack, always lands on Login.
    LaunchedEffect(isLoggedIn) {
        val onLogin = navigator.current == Destination.Login
        when {
            isLoggedIn && onLogin -> navigator.replaceAll(listOf(Destination.Home))
            !isLoggedIn && !onLogin -> navigator.replaceAll(listOf(Destination.Login))
        }
    }

    val currentTab = TopLevelDestination.entries.firstOrNull { it.destination == backStack.lastOrNull() }

    // Login and Home draw a navy hero behind the status bar, and the offline banner is
    // a solid color, so those need light icons even in light theme.
    val heroOnTop = backStack.lastOrNull() == Destination.Login || backStack.lastOrNull() == Destination.Home
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    StatusBarIcons(light = darkTheme || heroOnTop || bannerState != ConnectivityBannerState.Hidden)

    // One shared backdrop behind every screen. Screens are transparent, so the
    // default crossfade between them keeps the background perfectly still.
    AppBackground {
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0), // Each screen handles its own insets.
        bottomBar = {
            AnimatedVisibility(
                visible = currentTab != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                ShortNavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f)) {
                    TopLevelDestination.entries.forEach { tab ->
                        val selected = tab == currentTab
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = { navigator.selectTopLevel(tab.destination) },
                            icon = { Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            ConnectivityBanner(bannerState)
            val bannerVisible = bannerState != ConnectivityBannerState.Hidden
            Box(
                Modifier
                    .weight(1f)
                    // The banner already covers the status bar, so screens below shouldn't add it again.
                    .then(if (bannerVisible) Modifier.consumeWindowInsets(WindowInsets.statusBars) else Modifier),
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { navigator.goBack() },
                    entryDecorators = listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        // Each back stack entry gets its own ViewModelStore. ViewModels are
                        // cleared when their entry is popped, not when the Activity dies.
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                    entryProvider = entryProvider {
                        entry<Destination.Login> { LoginRoute() }

                        entry<Destination.Home>(metadata = TabTransition) {
                            HomeRoute(
                                onOpenAccounts = { navigator.selectTopLevel(Destination.Accounts) },
                                onOpenTransfer = { navigator.navigate(Destination.Transfer()) },
                                onOpenReceive = { navigator.selectTopLevel(Destination.Receive) },
                                onOpenTransactionHistory = { navigator.navigate(Destination.TransactionHistory(it)) },
                                onOpenReceipt = { navigator.navigate(Destination.Receipt(it, isNewTransfer = false)) },
                            )
                        }
                        entry<Destination.Accounts>(metadata = TabTransition) {
                            AccountsRoute(
                                onOpenHistory = { navigator.navigate(Destination.TransactionHistory(it)) },
                                onOpenTransfer = { navigator.navigate(Destination.Transfer(fromAccountId = it)) },
                            )
                        }
                        entry<Destination.Receive>(metadata = TabTransition) { TriggerReportRoute() }
                        entry<Destination.More>(metadata = TabTransition) {
                            SettingsRoute(
                                onOpenTheme = { navigator.navigate(Destination.Theme) },
                                onOpenShowcase = { navigator.navigate(Destination.Showcase) },
                            )
                        }
                        entry<Destination.Theme> { ThemeRoute(onBack = navigator::goBack) }

                        entry<Destination.TransactionHistory> { key ->
                            TransactionHistoryRoute(
                                accountId = key.accountId,
                                onBack = navigator::goBack,
                                onOpenReceipt = { navigator.navigate(Destination.Receipt(it, isNewTransfer = false)) },
                            )
                        }
                        entry<Destination.Transfer> { key ->
                            TransferRoute(
                                fromAccountId = key.fromAccountId,
                                onBack = navigator::goBack,
                                // Replace Transfer with the receipt so Back can't resubmit the form.
                                onTransferCompleted = { txId ->
                                    navigator.replaceAll(listOf(Destination.Home, Destination.Receipt(txId, isNewTransfer = true)))
                                },
                            )
                        }
                        entry<Destination.Receipt> { key ->
                            ReceiptRoute(
                                transactionId = key.transactionId,
                                isNewTransfer = key.isNewTransfer,
                                onClose = { if (key.isNewTransfer) navigator.popToHome() else navigator.goBack() },
                            )
                        }
                        entry<Destination.Showcase> { ShowcaseRoute() }
                    },
                )
            }
        }
    }
    }
}
