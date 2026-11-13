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
import androidx.annotation.StringRes
import com.example.m3showcase.R

enum class TopLevelDestination(
    val destination: Destination,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(Destination.Home, R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    Accounts(Destination.Accounts, R.string.nav_accounts, Icons.Rounded.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    Receive(Destination.Receive, R.string.nav_receive, Icons.Rounded.QrCode2, Icons.Outlined.QrCode2),
    More(Destination.More, R.string.nav_more, Icons.Rounded.Menu, Icons.Outlined.Menu),
}
