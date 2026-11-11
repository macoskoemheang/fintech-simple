package com.example.m3showcase.feature.home.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.m3showcase.core.common.MoneyFormatter
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.FestivalHeroArt
import com.example.m3showcase.core.ui.greeting
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.UserProfile
import com.example.m3showcase.core.ui.formatAccountNumber
import java.util.Calendar

/** Navy hero: greeting plus a swipeable card per account. */
@Composable
fun HomeHeader(
    user: UserProfile?,
    accounts: List<Account>,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onAccountClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(BankTheme.colors.heroBrush),
    ) {
    // Festival artwork sits behind the content and fills the hero, status bar included.
    FestivalHeroArt(modifier = Modifier.matchParentSize())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(bottom = 24.dp),
    ) {
        Greeting(user, onNotificationsClick)
        Spacer(Modifier.height(16.dp))

        val pagerState = rememberPagerState { accounts.size }
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
        ) { page ->
            AccountCard(
                account = accounts[page],
                isBalanceHidden = isBalanceHidden,
                onToggleBalance = onToggleBalance,
                onClick = { onAccountClick(accounts[page].id) },
            )
        }
        if (accounts.size > 1) {
            PagerDots(count = accounts.size, current = pagerState.currentPage)
        }
    }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Greeting(user: UserProfile?, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialShapes.Cookie7Sided.toShape(),
            color = BankTheme.colors.heroAccent,
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.joinToString("")?.take(2).orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00313F),
                )
            }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(BankTheme.eventTheme.greeting ?: greetingForNow(), style = MaterialTheme.typography.bodySmall, color = BankTheme.colors.onHeroVariant)
            Text(
                user?.fullName.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = BankTheme.colors.onHero,
                fontWeight = FontWeight.SemiBold,
            )
        }
        IconButton(onClick = onNotificationsClick) {
            Icon(Icons.Rounded.NotificationsNone, contentDescription = "Notifications", tint = BankTheme.colors.onHero)
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    isBalanceHidden: Boolean,
    onToggleBalance: () -> Unit,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = Color.White.copy(alpha = 0.10f),
        contentColor = BankTheme.colors.onHero,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(account.name, style = MaterialTheme.typography.titleSmall)
                        if (account.isDefault) {
                            Text(
                                "DEFAULT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00313F),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .background(BankTheme.colors.heroAccent, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        account.number.formatAccountNumber(),
                        style = MaterialTheme.typography.bodySmall,
                        color = BankTheme.colors.onHeroVariant,
                    )
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = BankTheme.colors.onHeroVariant)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedContent(
                    targetState = isBalanceHidden,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "balance",
                    modifier = Modifier.weight(1f),
                ) { hidden ->
                    Text(
                        if (hidden) MoneyFormatter.masked(account.balance.currency) else MoneyFormatter.format(account.balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onToggleBalance) {
                    Icon(
                        if (isBalanceHidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (isBalanceHidden) "Show balance" else "Hide balance",
                    )
                }
            }
        }
    }
}

@Composable
private fun PagerDots(count: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        repeat(count) { index ->
            val selected = index == current
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (selected) 18.dp else 6.dp)
                    .background(
                        if (selected) BankTheme.colors.heroAccent else BankTheme.colors.onHeroVariant.copy(alpha = 0.4f),
                        CircleShape,
                    ),
            )
        }
    }
}

private fun greetingForNow(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
