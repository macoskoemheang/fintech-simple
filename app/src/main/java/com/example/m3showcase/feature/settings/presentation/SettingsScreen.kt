package com.example.m3showcase.feature.settings.presentation

import com.example.m3showcase.core.designsystem.bankTopAppBarColors
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.ui.semantics.Role
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.m3showcase.core.designsystem.component.LoadingState
import com.example.m3showcase.core.domain.model.EventTheme
import com.example.m3showcase.core.domain.model.SessionTimeout
import com.example.m3showcase.core.domain.model.ThemeMode
import com.example.m3showcase.core.ui.displayName
import com.example.m3showcase.core.domain.model.UserProfile
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(
    onOpenTheme: () -> Unit,
    onOpenShowcase: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.OpenTheme -> onOpenTheme()
                SettingsEvent.OpenShowcase -> onOpenShowcase()
            }
        }
    }
    SettingsScreen(state, viewModel::onAction)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(state: SettingsViewState, onAction: (SettingsViewAction) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        containerColor = Color.Transparent, // AppBackground shows through.
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(colors = bankTopAppBarColors(), title = { Text("More") }, scrollBehavior = scrollBehavior) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                SettingsViewState.Loading -> LoadingState()
                is SettingsViewState.Success -> SettingsContent(state, onAction)
            }
        }
    }
    if (state is SettingsViewState.Success && state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(SettingsViewAction.OnLogoutDismissed) },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
            title = { Text("Log out?") },
            text = { Text("You'll need your PIN to sign in again.") },
            confirmButton = { TextButton(onClick = { onAction(SettingsViewAction.OnLogoutConfirmed) }) { Text("Log out") } },
            dismissButton = { TextButton(onClick = { onAction(SettingsViewAction.OnLogoutDismissed) }) { Text("Cancel") } },
        )
    }
    if (state is SettingsViewState.Success && state.showSessionTimeoutDialog) {
        SessionTimeoutDialog(selected = state.sessionTimeout, onAction = onAction)
    }
}

@Composable
private fun SessionTimeoutDialog(selected: SessionTimeout, onAction: (SettingsViewAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsViewAction.OnSessionTimeoutDismissed) },
        icon = { Icon(Icons.Rounded.Timer, null) },
        title = { Text("Auto-lock") },
        text = {
            Column(Modifier.selectableGroup()) {
                Text(
                    "Lock the app and ask for your PIN after this much time without activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SessionTimeout.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selected,
                                role = Role.RadioButton,
                                onClick = { onAction(SettingsViewAction.OnSessionTimeoutSelected(option)) },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null) // Row handles the click.
                        Text(option.displayName, Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { onAction(SettingsViewAction.OnSessionTimeoutDismissed) }) { Text("Cancel") } },
    )
}

@Composable
private fun SettingsContent(state: SettingsViewState.Success, onAction: (SettingsViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileCard(state.user)

        SettingsSection("Appearance") {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnThemeClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = {
                    Icon(
                        when (state.themeMode) {
                            ThemeMode.System -> Icons.Rounded.PhoneAndroid
                            ThemeMode.Light -> Icons.Rounded.LightMode
                            ThemeMode.Dark -> Icons.Rounded.DarkMode
                        },
                        contentDescription = null,
                    )
                },
                headlineContent = { Text("Theme") },
                supportingContent = {
                    val skin = if (state.eventTheme == EventTheme.Auto) "Seasonal" else state.eventTheme.displayName
                    Text("${state.themeMode.name} · $skin")
                },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
        }

        SettingsSection("Privacy & security") {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnSessionTimeoutClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.Timer, null) },
                headlineContent = { Text("Auto-lock") },
                supportingContent = { Text("After ${state.sessionTimeout.displayName} of inactivity") },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.VisibilityOff, null) },
                headlineContent = { Text("Hide balances") },
                supportingContent = { Text("Mask amounts on Home and Accounts") },
                trailingContent = {
                    Switch(
                        checked = state.isBalanceHidden,
                        onCheckedChange = { onAction(SettingsViewAction.OnHideBalanceChanged(it)) },
                    )
                },
            )
        }

        SettingsSection("Explore") {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnOpenShowcaseClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
                headlineContent = { Text("Material 3 Expressive showcase") },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
        }

        SettingsSection(null) {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnLogoutClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) },
                headlineContent = { Text("Log out", color = MaterialTheme.colorScheme.error) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfileCard(user: UserProfile?) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = MaterialShapes.Cookie7Sided.toShape(),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2).orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(user?.fullName.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(user?.phoneNumber.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String?, content: @Composable () -> Unit) {
    Column {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            )
        }
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        ) { content() }
    }
}
