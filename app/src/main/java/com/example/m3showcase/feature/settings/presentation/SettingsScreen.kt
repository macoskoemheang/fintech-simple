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
import com.example.m3showcase.core.domain.model.UserProfile
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.AppLanguage
import androidx.compose.material.icons.rounded.Translate
import com.example.m3showcase.core.ui.labelRes
import com.example.m3showcase.core.ui.nameRes
import com.example.m3showcase.core.language.endonym

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
        topBar = { LargeFlexibleTopAppBar(colors = bankTopAppBarColors(), title = { Text(stringResource(R.string.nav_more)) }, scrollBehavior = scrollBehavior) },
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
            title = { Text(stringResource(R.string.settings_logout_title)) },
            text = { Text(stringResource(R.string.settings_logout_message)) },
            confirmButton = { TextButton(onClick = { onAction(SettingsViewAction.OnLogoutConfirmed) }) { Text(stringResource(R.string.settings_logout)) } },
            dismissButton = { TextButton(onClick = { onAction(SettingsViewAction.OnLogoutDismissed) }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
    if (state is SettingsViewState.Success && state.showSessionTimeoutDialog) {
        SessionTimeoutDialog(selected = state.sessionTimeout, onAction = onAction)
    }
    if (state is SettingsViewState.Success && state.showLanguageDialog) {
        LanguageDialog(selected = state.language, onAction = onAction)
    }
}

@Composable
private fun SessionTimeoutDialog(selected: SessionTimeout, onAction: (SettingsViewAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsViewAction.OnSessionTimeoutDismissed) },
        icon = { Icon(Icons.Rounded.Timer, null) },
        title = { Text(stringResource(R.string.settings_auto_lock)) },
        text = {
            Column(Modifier.selectableGroup()) {
                Text(
                    stringResource(R.string.settings_auto_lock_dialog_message),
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
                        Text(stringResource(option.labelRes), Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { onAction(SettingsViewAction.OnSessionTimeoutDismissed) }) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun SettingsContent(state: SettingsViewState.Success, onAction: (SettingsViewAction) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ProfileCard(state.user)

        SettingsSection(stringResource(R.string.settings_appearance)) {
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
                headlineContent = { Text(stringResource(R.string.theme_title)) },
                supportingContent = {
                    val skin = if (state.eventTheme == EventTheme.Auto) stringResource(R.string.settings_seasonal) else stringResource(state.eventTheme.nameRes)
                    Text("${stringResource(state.themeMode.labelRes)} · $skin")
                },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnLanguageClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.Translate, null) },
                headlineContent = { Text(stringResource(R.string.settings_language)) },
                supportingContent = { Text(state.language.endonym()) },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
        }

        SettingsSection(stringResource(R.string.settings_privacy_security)) {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnSessionTimeoutClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.Timer, null) },
                headlineContent = { Text(stringResource(R.string.settings_auto_lock)) },
                supportingContent = { Text(stringResource(R.string.settings_auto_lock_after, stringResource(state.sessionTimeout.labelRes))) },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.VisibilityOff, null) },
                headlineContent = { Text(stringResource(R.string.settings_hide_balances)) },
                supportingContent = { Text(stringResource(R.string.settings_hide_balances_desc)) },
                trailingContent = {
                    Switch(
                        checked = state.isBalanceHidden,
                        onCheckedChange = { onAction(SettingsViewAction.OnHideBalanceChanged(it)) },
                    )
                },
            )
        }

        SettingsSection(stringResource(R.string.settings_explore)) {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnOpenShowcaseClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.Rounded.AutoAwesome, null) },
                headlineContent = { Text(stringResource(R.string.settings_showcase)) },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            )
        }

        SettingsSection(null) {
            ListItem(
                modifier = Modifier.clickable { onAction(SettingsViewAction.OnLogoutClicked) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                leadingContent = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error) },
                headlineContent = { Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error) },
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

@Composable
private fun LanguageDialog(selected: AppLanguage, onAction: (SettingsViewAction) -> Unit) {
    AlertDialog(
        onDismissRequest = { onAction(SettingsViewAction.OnLanguageDismissed) },
        icon = { Icon(Icons.Rounded.Translate, null) },
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column(Modifier.selectableGroup()) {
                AppLanguage.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == selected,
                                role = Role.RadioButton,
                                onClick = { onAction(SettingsViewAction.OnLanguageSelected(option)) },
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null) // Row handles the click.
                        Text(option.endonym(), Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = { onAction(SettingsViewAction.OnLanguageDismissed) }) { Text(stringResource(R.string.action_cancel)) } },
    )
}
