package com.example.m3showcase.core.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.m3showcase.core.designsystem.BrandColors
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R

enum class ConnectivityBannerState { Hidden, Offline, BackOnline }

/** App-wide strip at the top of the screen that says when the device goes offline and comes back. */
@Composable
fun ConnectivityBanner(state: ConnectivityBannerState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state != ConnectivityBannerState.Hidden,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        AnimatedContent(targetState = state, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "banner") { s ->
            val offline = s == ConnectivityBannerState.Offline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (offline) BrandColors.Coral else BrandColors.Mint)
                    .statusBarsPadding() // The banner sits at the top and colors the status bar area.
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    if (offline) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    stringResource(if (offline) R.string.banner_offline else R.string.banner_back_online),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
