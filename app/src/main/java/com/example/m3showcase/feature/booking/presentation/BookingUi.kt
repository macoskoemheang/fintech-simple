package com.example.m3showcase.feature.booking.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirportShuttle
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LunchDining
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Tour
import androidx.compose.material.icons.rounded.Villa
import androidx.compose.material.icons.rounded.WineBar
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Hiking
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.m3showcase.R
import com.example.m3showcase.core.designsystem.component.StatusPill
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.booking.domain.model.Amenity
import com.example.m3showcase.feature.booking.domain.model.BookingError
import com.example.m3showcase.feature.booking.domain.model.BookingStatus
import com.example.m3showcase.feature.booking.domain.model.ListingCategory

/* Presentation for the booking feature: labels, icons, Expressive shapes and colors. */

@get:StringRes
internal val ListingCategory.labelRes: Int
    get() = when (this) {
        ListingCategory.Hotel -> R.string.booking_category_hotel
        ListingCategory.Resort -> R.string.booking_category_resort
        ListingCategory.Tour -> R.string.booking_category_tour
        ListingCategory.Dining -> R.string.booking_category_dining
    }

internal val ListingCategory.icon: ImageVector
    get() = when (this) {
        ListingCategory.Hotel -> Icons.Rounded.Hotel
        ListingCategory.Resort -> Icons.Rounded.Villa
        ListingCategory.Tour -> Icons.Rounded.Tour
        ListingCategory.Dining -> Icons.Rounded.Restaurant
    }

/** Each category gets its own Material 3 Expressive shape. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ListingCategory.shape(): Shape = when (this) {
    ListingCategory.Hotel -> MaterialShapes.Cookie9Sided
    ListingCategory.Resort -> MaterialShapes.Sunny
    ListingCategory.Tour -> MaterialShapes.Clover4Leaf
    ListingCategory.Dining -> MaterialShapes.Pill
}.toShape()

internal data class CategoryColors(val container: Color, val content: Color)

@Composable
internal fun ListingCategory.colors(): CategoryColors {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        ListingCategory.Hotel -> CategoryColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        ListingCategory.Resort -> CategoryColors(scheme.tertiaryContainer, scheme.onTertiaryContainer)
        ListingCategory.Tour -> CategoryColors(scheme.secondaryContainer, scheme.onSecondaryContainer)
        ListingCategory.Dining -> CategoryColors(scheme.errorContainer, scheme.onErrorContainer)
    }
}

/** The category icon inside its shape. */
@Composable
internal fun CategoryBadge(category: ListingCategory, size: Dp = 52.dp, modifier: Modifier = Modifier) {
    val colors = category.colors()
    Surface(shape = category.shape(), color = colors.container, contentColor = colors.content, modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) { Icon(category.icon, null, Modifier.size(size * 0.46f)) }
    }
}

@get:StringRes
internal val Amenity.labelRes: Int
    get() = when (this) {
        Amenity.Wifi -> R.string.booking_amenity_wifi
        Amenity.Breakfast -> R.string.booking_amenity_breakfast
        Amenity.Pool -> R.string.booking_amenity_pool
        Amenity.Gym -> R.string.booking_amenity_gym
        Amenity.Spa -> R.string.booking_amenity_spa
        Amenity.AirportTransfer -> R.string.booking_amenity_transfer
        Amenity.Bikes -> R.string.booking_amenity_bikes
        Amenity.AllInclusive -> R.string.booking_amenity_all_inclusive
        Amenity.Beach -> R.string.booking_amenity_beach
        Amenity.Guide -> R.string.booking_amenity_guide
        Amenity.Lunch -> R.string.booking_amenity_lunch
        Amenity.SetMenu -> R.string.booking_amenity_set_menu
        Amenity.Vegetarian -> R.string.booking_amenity_vegetarian
        Amenity.WinePairing -> R.string.booking_amenity_wine
    }

internal val Amenity.icon: ImageVector
    get() = when (this) {
        Amenity.Wifi -> Icons.Rounded.Wifi
        Amenity.Breakfast -> Icons.Rounded.FreeBreakfast
        Amenity.Pool -> Icons.Rounded.Pool
        Amenity.Gym -> Icons.Rounded.FitnessCenter
        Amenity.Spa -> Icons.Rounded.Spa
        Amenity.AirportTransfer -> Icons.Rounded.AirportShuttle
        Amenity.Bikes -> Icons.Rounded.PedalBike
        Amenity.AllInclusive -> Icons.Rounded.AllInclusive
        Amenity.Beach -> Icons.Rounded.BeachAccess
        Amenity.Guide -> Icons.Rounded.Hiking
        Amenity.Lunch -> Icons.Rounded.LunchDining
        Amenity.SetMenu -> Icons.Rounded.RestaurantMenu
        Amenity.Vegetarian -> Icons.Rounded.Eco
        Amenity.WinePairing -> Icons.Rounded.WineBar
    }

@Composable
internal fun BookingStatusPill(status: BookingStatus, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    when (status) {
        BookingStatus.Confirmed -> StatusPill(stringResource(R.string.booking_status_confirmed), scheme.primaryContainer, scheme.onPrimaryContainer, modifier)
        BookingStatus.Completed -> StatusPill(stringResource(R.string.booking_status_completed), scheme.surfaceContainerHighest, scheme.onSurfaceVariant, modifier)
        BookingStatus.Cancelled -> StatusPill(stringResource(R.string.booking_status_cancelled), scheme.errorContainer, scheme.onErrorContainer, modifier)
    }
}

/** "4.9" with one decimal, independent of locale. */
internal fun formatRating(rating: Double): String = "%.1f".format(java.util.Locale.US, rating)

/** Booking tool errors get their own wording; everything else falls back to the shared messages. */
internal fun Throwable.toBookingMessage(): UiText = when (this) {
    is BookingError -> when (code) {
        BookingError.Code.NotFound -> UiText.res(R.string.booking_error_not_found)
        BookingError.Code.NotAvailable -> UiText.res(R.string.booking_error_not_available)
        BookingError.Code.TooManyGuests -> UiText.res(R.string.booking_error_too_many_guests)
        BookingError.Code.PastDate -> UiText.res(R.string.booking_error_past_date)
        BookingError.Code.NotCancellable -> UiText.res(R.string.booking_error_not_cancellable)
        BookingError.Code.InvalidArgument -> UiText.res(R.string.booking_error_invalid)
        BookingError.Code.Unknown -> UiText.res(R.string.booking_error_server, message.orEmpty())
    }
    else -> toUserMessage()
}

internal fun <T> Result<T>.toBookingLoadable(): Loadable<T> =
    fold({ Loadable.Success(it) }, { Loadable.Error(it.toBookingMessage(), it is BankError.NoInternet) })
