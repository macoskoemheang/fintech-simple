package com.example.m3showcase.feature.payments.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.BankTheme
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.feature.payments.domain.model.BillerCategory
import com.example.m3showcase.feature.payments.domain.model.PaymentStatus

/** Presentation for biller categories: label, icon and an Expressive shape each. */
@get:StringRes
internal val BillerCategory.labelRes: Int
    get() = when (this) {
        BillerCategory.Electricity -> R.string.biller_electricity
        BillerCategory.Water -> R.string.biller_water
        BillerCategory.Internet -> R.string.biller_internet
        BillerCategory.Mobile -> R.string.biller_mobile
        BillerCategory.Education -> R.string.biller_education
        BillerCategory.Insurance -> R.string.biller_insurance
    }

internal val BillerCategory.icon: ImageVector
    get() = when (this) {
        BillerCategory.Electricity -> Icons.Rounded.Bolt
        BillerCategory.Water -> Icons.Rounded.WaterDrop
        BillerCategory.Internet -> Icons.Rounded.Wifi
        BillerCategory.Mobile -> Icons.Rounded.PhoneAndroid
        BillerCategory.Education -> Icons.Rounded.School
        BillerCategory.Insurance -> Icons.Rounded.HealthAndSafety
    }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BillerCategory.shape(): Shape = when (this) {
    BillerCategory.Electricity -> MaterialShapes.Sunny
    BillerCategory.Water -> MaterialShapes.Cookie6Sided
    BillerCategory.Internet -> MaterialShapes.Clover4Leaf
    BillerCategory.Mobile -> MaterialShapes.Pill
    BillerCategory.Education -> MaterialShapes.Pentagon
    BillerCategory.Insurance -> MaterialShapes.Cookie9Sided
}.toShape()

@get:StringRes
internal val PaymentStatus.labelRes: Int
    get() = when (this) {
        PaymentStatus.Success -> R.string.payment_status_success
        PaymentStatus.Pending -> R.string.payment_status_pending
        PaymentStatus.Failed -> R.string.payment_status_failed
    }

@Composable
internal fun PaymentStatusPill(status: PaymentStatus) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        PaymentStatus.Success -> BankTheme.colors.credit.copy(alpha = 0.16f) to BankTheme.colors.credit
        PaymentStatus.Pending -> scheme.secondaryContainer to scheme.onSecondaryContainer
        PaymentStatus.Failed -> scheme.errorContainer to scheme.onErrorContainer
    }
    StatusPill(stringResource(status.labelRes), container, content)
}
