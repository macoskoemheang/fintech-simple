package com.example.m3showcase.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val destination: Destination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(Destination.Home, "Home", Icons.Rounded.Home, Icons.Outlined.Home),
    Accounts(Destination.Accounts, "Accounts", Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    Receive(Destination.Receive, "Receive", Icons.Rounded.QrCode2, Icons.Outlined.QrCode2),
    More(Destination.More, "More", Icons.Rounded.Menu, Icons.Outlined.Menu),
}
