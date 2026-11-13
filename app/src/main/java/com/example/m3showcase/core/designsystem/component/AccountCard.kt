package com.example.m3showcase.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.example.m3showcase.core.designsystem.BrandColors
import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.AccountType
import com.example.m3showcase.core.ui.formatAccountNumber
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import androidx.annotation.StringRes

/**
 * Each account type gets its own card colors, like physical bank cards, so people can tell
 * accounts apart at a glance. The colors are fixed (not from the theme) because text on
 * them is always white; every pair keeps white text above WCAG AA contrast.
 */
@Immutable
data class AccountCardPalette(val start: Color, val end: Color, val glow: Color)

fun AccountType.cardPalette(): AccountCardPalette = when (this) {
    AccountType.Savings -> AccountCardPalette(BrandColors.Navy, Color(0xFF00708A), BrandColors.CyanLight)
    AccountType.Current -> AccountCardPalette(Color(0xFF1B2A6B), Color(0xFF3D4FB0), Color(0xFFA9B6FF))
    AccountType.FixedDeposit -> AccountCardPalette(Color(0xFF3A1650), Color(0xFF8A2E5C), Color(0xFFFFB0C8))
}

@StringRes
fun AccountType.labelRes(): Int = when (this) {
    AccountType.Savings -> R.string.account_type_savings
    AccountType.Current -> R.string.account_type_current
    AccountType.FixedDeposit -> R.string.account_type_fixed_deposit
}

fun AccountType.icon(): ImageVector = when (this) {
    AccountType.Savings -> Icons.Rounded.Savings
    AccountType.Current -> Icons.Rounded.AccountBalanceWallet
    AccountType.FixedDeposit -> Icons.Rounded.Lock
}

private val CardOnColor = Color.White
private val CardOnColorVariant = Color.White.copy(alpha = 0.74f)

/**
 * Bank-card style surface for an [Account]: type gradient, two soft decorative shapes,
 * type chip, name, grouped number and balance.
 *
 * - [compact] is the smaller variant used in horizontal pickers.
 * - [selected] non-null makes it a selectable card: unselected cards shrink and dim, the
 *   selected one gets a ring and a check badge. Null means "not a picker".
 * - [actions] is an optional row under the balance (e.g. Transfer / History buttons).
 *
 * Pure UI: the caller passes the already-formatted [balanceText] so hiding balances stays
 * the caller's decision.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountCard(
    account: Account,
    balanceText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    selected: Boolean? = null,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val palette = account.type.cardPalette()
    val shape = RoundedCornerShape(if (compact) 24.dp else 28.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Springy press + selection feedback. Read inside graphicsLayer, so no recomposition per frame.
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.97f
            selected == false -> 0.94f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "cardScale",
    )
    val dim by animateFloatAsState(if (selected == false) 0.62f else 1f, label = "cardDim")
    val ring by animateDpAsState(if (selected == true) 3.dp else 0.dp, label = "cardRing")

    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        contentColor = CardOnColor,
        shadowElevation = if (selected == false) 0.dp else 6.dp,
        interactionSource = interaction,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = dim
            },
        border = if (ring > 0.dp) BorderStroke(ring, MaterialTheme.colorScheme.primary) else null,
    ) {
        // Drawn inside the Surface so its shape clips the gradient.
        Box(Modifier.cardBackground(palette)) {
            Column(Modifier.padding(if (compact) 16.dp else 20.dp)) {
                Header(account, compact)
                Spacer(Modifier.size(if (compact) 14.dp else 22.dp))
                Text(
                    account.name,
                    style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    account.number.formatAccountNumber(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    color = CardOnColorVariant,
                )
                Spacer(Modifier.size(if (compact) 10.dp else 18.dp))
                if (!compact) {
                    Text(stringResource(R.string.account_available_balance), style = MaterialTheme.typography.labelMedium, color = CardOnColorVariant)
                }
                Text(
                    balanceText,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                if (actions != null) {
                    Spacer(Modifier.size(16.dp))
                    actions()
                }
            }
            if (selected != null) {
                AnimatedVisibility(
                    visible = selected,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = scaleOut(),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                ) {
                    Surface(shape = MaterialShapes.Cookie9Sided.toShape(), color = Color.White, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.account_selected),
                            tint = palette.start,
                            modifier = Modifier.padding(3.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Header(account: Account, compact: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = MaterialShapes.Cookie6Sided.toShape(),
            color = Color.White.copy(alpha = 0.18f),
            contentColor = CardOnColor,
            modifier = Modifier.size(if (compact) 32.dp else 40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(account.type.icon(), contentDescription = null, modifier = Modifier.size(if (compact) 18.dp else 22.dp))
            }
        }
        Pill(stringResource(account.type.labelRes()))
        if (account.isDefault) Pill(stringResource(R.string.account_default), strong = true)
    }
}

@Composable
private fun Pill(text: String, strong: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = if (strong) 0.92f else 0.16f),
        contentColor = if (strong) BrandColors.Navy else CardOnColor,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Diagonal gradient plus two translucent Material shapes bleeding off the edges.
 * drawWithCache builds the brushes and outlines once per size, not every frame.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun Modifier.cardBackground(palette: AccountCardPalette): Modifier = drawWithCache {
    val gradient = Brush.linearGradient(listOf(palette.start, palette.end), start = Offset.Zero, end = Offset(size.width, size.height))
    val bigSide = size.height * 1.1f
    val big = MaterialShapes.Cookie9Sided.unitPath(bigSide)
    val smallSide = size.height * 0.7f
    val small = MaterialShapes.Circle.unitPath(smallSide)
    val sheen = Brush.radialGradient(
        listOf(palette.glow.copy(alpha = 0.35f), Color.Transparent),
        center = Offset(size.width, 0f),
        radius = size.width * 0.8f,
    )
    onDrawBehind {
        drawRect(gradient)
        drawRect(sheen)
        translate(left = size.width - bigSide * 0.62f, top = -bigSide * 0.38f) {
            rotate(18f, pivot = Offset(bigSide / 2, bigSide / 2)) {
                drawPath(big, Color.White.copy(alpha = 0.07f))
            }
        }
        translate(left = size.width * 0.42f, top = size.height - smallSide * 0.45f) {
            drawPath(small, Color.White.copy(alpha = 0.06f))
        }
    }
}

/** Material shapes are normalized to a 0..1 box; scale the path up to [side] px. */
private fun RoundedPolygon.unitPath(side: Float): Path =
    toPath().asComposePath().apply { transform(Matrix().apply { scale(side, side) }) }

/** Width used by compact cards in horizontal pickers. */
val CompactAccountCardWidth: Dp = 224.dp

/** The same gradient + decorative shapes as account cards, for other hero surfaces. */
fun Modifier.accountGradient(palette: AccountCardPalette): Modifier = cardBackground(palette)

/** Hero palettes for the service screens; white text stays above AA contrast on all of them. */
object ServicePalettes {
    val Payment = AccountCardPalette(BrandColors.Navy, Color(0xFF00708A), BrandColors.CyanLight)
    val Deposit = AccountCardPalette(Color(0xFF0B3D3A), Color(0xFF0F6E60), Color(0xFF7FE3C8))
    val Loan = AccountCardPalette(Color(0xFF4A2511), Color(0xFF8E4316), Color(0xFFFFD7A8))
    val Report = AccountCardPalette(Color(0xFF1B2A6B), Color(0xFF3D4FB0), Color(0xFFA9B6FF))
}
