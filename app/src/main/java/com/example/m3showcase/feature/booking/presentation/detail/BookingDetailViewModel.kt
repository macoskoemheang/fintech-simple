package com.example.m3showcase.feature.booking.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.R
import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.Money
import com.example.m3showcase.core.domain.model.daysUntil
import com.example.m3showcase.feature.booking.domain.model.CancellationPolicy
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.feature.booking.domain.model.Booking
import com.example.m3showcase.feature.booking.domain.usecase.CancelBookingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetBookingUseCase
import com.example.m3showcase.feature.booking.presentation.toBookingLoadable
import com.example.m3showcase.feature.booking.presentation.toBookingMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class BookingDetailViewState(
    val booking: Loadable<Booking> = Loadable.Loading,
    /** True right after booking: the screen opens with a celebration header. */
    val isNew: Boolean,
    val showCancelDialog: Boolean = false,
    val isCancelling: Boolean = false,
    val today: CalendarDate,
) {
    /** Mirrors the server rule: free-cancellation bookings are refunded in full until 48 hours before check-in. */
    fun refundIfCancelled(booking: Booking): Money {
        val free = booking.listing.cancellation == CancellationPolicy.Free48h && today.daysUntil(booking.checkIn) >= 2
        return if (free) booking.total else booking.total.copy(minor = 0)
    }
}

sealed interface BookingDetailViewAction {
    data object OnRetry : BookingDetailViewAction
    data object OnBackClicked : BookingDetailViewAction
    data object OnCancelClicked : BookingDetailViewAction
    data object OnCancelConfirmed : BookingDetailViewAction
    data object OnCancelDismissed : BookingDetailViewAction
}

sealed interface BookingDetailEvent {
    data object NavigateBack : BookingDetailEvent
    data class ShowMessage(val text: UiText) : BookingDetailEvent
}

class BookingDetailViewModel(
    private val bookingId: String,
    isNew: Boolean,
    private val getBooking: GetBookingUseCase,
    private val cancelBooking: CancelBookingUseCase,
    dateProvider: DateProvider,
) : ViewModel() {

    private val _viewState = MutableStateFlow(BookingDetailViewState(isNew = isNew, today = dateProvider.today()))
    val viewState: StateFlow<BookingDetailViewState> = _viewState.asStateFlow()

    private val _events = Channel<BookingDetailEvent>(Channel.BUFFERED)
    val events: Flow<BookingDetailEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: BookingDetailViewAction) {
        when (action) {
            BookingDetailViewAction.OnRetry -> load()
            BookingDetailViewAction.OnBackClicked -> _events.trySend(BookingDetailEvent.NavigateBack)
            BookingDetailViewAction.OnCancelClicked -> _viewState.update { it.copy(showCancelDialog = true) }
            BookingDetailViewAction.OnCancelDismissed -> _viewState.update { it.copy(showCancelDialog = false) }
            BookingDetailViewAction.OnCancelConfirmed -> cancel()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _viewState.update { it.copy(booking = Loadable.Loading) }
            val booking = getBooking(bookingId).toBookingLoadable()
            _viewState.update { it.copy(booking = booking) }
        }
    }

    private fun cancel() {
        if (_viewState.value.isCancelling) return
        _viewState.update { it.copy(showCancelDialog = false, isCancelling = true) }
        viewModelScope.launch {
            cancelBooking(bookingId).fold(
                onSuccess = { cancelled ->
                    _viewState.update { it.copy(booking = Loadable.Success(cancelled), isCancelling = false, isNew = false) }
                    _events.send(BookingDetailEvent.ShowMessage(UiText.res(R.string.booking_cancelled_message)))
                },
                onFailure = { error ->
                    _viewState.update { it.copy(isCancelling = false) }
                    _events.send(BookingDetailEvent.ShowMessage(error.toBookingMessage()))
                },
            )
        }
    }
}
