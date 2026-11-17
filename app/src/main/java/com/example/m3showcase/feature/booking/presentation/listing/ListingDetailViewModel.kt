package com.example.m3showcase.feature.booking.presentation.listing

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.common.DateProvider
import com.example.m3showcase.core.domain.model.CalendarDate
import com.example.m3showcase.core.domain.model.plusDays
import com.example.m3showcase.core.domain.usecase.ObserveSessionUseCase
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.feature.booking.domain.model.BookingError
import com.example.m3showcase.feature.booking.domain.model.BookingQuote
import com.example.m3showcase.feature.booking.domain.model.BookingRequest
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.usecase.CheckAvailabilityUseCase
import com.example.m3showcase.feature.booking.domain.usecase.CreateBookingUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetListingUseCase
import com.example.m3showcase.feature.booking.presentation.toBookingLoadable
import com.example.m3showcase.feature.booking.presentation.toBookingMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ListingDetailViewState(
    val listing: Loadable<Listing> = Loadable.Loading,
    val today: CalendarDate,
    val checkIn: CalendarDate,
    val nights: Int = 2,
    val guests: Int = 2,
    val guestName: String = "",
    val note: String = "",
    val quote: Loadable<BookingQuote> = Loadable.Loading,
    val showDatePicker: Boolean = false,
    val isBooking: Boolean = false,
) {
    val canBook: Boolean
        get() = listing is Loadable.Success && (quote as? Loadable.Success)?.data?.available == true && guestName.isNotBlank() && !isBooking
}

sealed interface ListingDetailViewAction {
    data object OnRetry : ListingDetailViewAction
    data object OnBackClicked : ListingDetailViewAction
    data object OnCheckInClicked : ListingDetailViewAction
    data class OnCheckInSelected(val date: CalendarDate) : ListingDetailViewAction
    data object OnDatePickerDismissed : ListingDetailViewAction
    data class OnNightsChanged(val delta: Int) : ListingDetailViewAction
    data class OnGuestsChanged(val delta: Int) : ListingDetailViewAction
    data class OnGuestNameChanged(val value: String) : ListingDetailViewAction
    data class OnNoteChanged(val value: String) : ListingDetailViewAction
    data object OnRetryQuote : ListingDetailViewAction
    data object OnBookClicked : ListingDetailViewAction
}

sealed interface ListingDetailEvent {
    data object NavigateBack : ListingDetailEvent
    data class BookingCreated(val bookingId: String) : ListingDetailEvent
    data class ShowMessage(val text: UiText) : ListingDetailEvent
}

private data class QuoteInput(val listingId: String, val checkIn: CalendarDate, val nights: Int, val guests: Int)

@OptIn(FlowPreview::class)
class ListingDetailViewModel(
    private val listingId: String,
    private val getListing: GetListingUseCase,
    private val checkAvailability: CheckAvailabilityUseCase,
    private val createBooking: CreateBookingUseCase,
    observeSession: ObserveSessionUseCase,
    dateProvider: DateProvider,
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        dateProvider.today().let { today ->
            ListingDetailViewState(today = today, checkIn = today.plusDays(7), guestName = observeSession().value?.fullName.orEmpty())
        },
    )
    val viewState: StateFlow<ListingDetailViewState> = _viewState.asStateFlow()

    private val _events = Channel<ListingDetailEvent>(Channel.BUFFERED)
    val events: Flow<ListingDetailEvent> = _events.receiveAsFlow()

    /** Bumped to fetch a fresh quote for the same inputs. */
    private val quoteAttempt = MutableStateFlow(0)

    init {
        load()
        viewModelScope.launch {
            // A new quote whenever the dates or guests change, after a short pause.
            _viewState
                .map { state -> (state.listing as? Loadable.Success)?.let { QuoteInput(it.data.id, state.checkIn, state.nights, state.guests) } }
                .filterNotNull()
                .distinctUntilChanged()
                .combine(quoteAttempt) { input, _ -> input }
                .debounce(250)
                .collectLatest { input ->
                    _viewState.update { it.copy(quote = Loadable.Loading) }
                    val quote = checkAvailability(input.listingId, input.checkIn, input.nights, input.guests).toBookingLoadable()
                    _viewState.update { it.copy(quote = quote) }
                }
        }
    }

    fun onAction(action: ListingDetailViewAction) {
        when (action) {
            ListingDetailViewAction.OnRetry -> load()
            ListingDetailViewAction.OnBackClicked -> _events.trySend(ListingDetailEvent.NavigateBack)
            ListingDetailViewAction.OnCheckInClicked -> _viewState.update { it.copy(showDatePicker = true) }
            ListingDetailViewAction.OnDatePickerDismissed -> _viewState.update { it.copy(showDatePicker = false) }
            is ListingDetailViewAction.OnCheckInSelected -> _viewState.update {
                it.copy(checkIn = maxOf(action.date, it.today), showDatePicker = false)
            }
            is ListingDetailViewAction.OnNightsChanged -> _viewState.update { it.copy(nights = (it.nights + action.delta).coerceIn(1, MaxNights)) }
            is ListingDetailViewAction.OnGuestsChanged -> _viewState.update { state ->
                val max = (state.listing as? Loadable.Success)?.data?.maxGuests ?: 1
                state.copy(guests = (state.guests + action.delta).coerceIn(1, max))
            }
            is ListingDetailViewAction.OnGuestNameChanged -> _viewState.update { it.copy(guestName = action.value) }
            is ListingDetailViewAction.OnNoteChanged -> _viewState.update { it.copy(note = action.value.take(MaxNoteLength)) }
            ListingDetailViewAction.OnRetryQuote -> quoteAttempt.value++
            ListingDetailViewAction.OnBookClicked -> book()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _viewState.update { it.copy(listing = Loadable.Loading) }
            val listing = getListing(listingId).toBookingLoadable()
            _viewState.update { state ->
                val max = (listing as? Loadable.Success)?.data?.maxGuests ?: state.guests
                state.copy(listing = listing, guests = state.guests.coerceIn(1, max))
            }
        }
    }

    private fun book() {
        val state = _viewState.value
        if (!state.canBook) return
        _viewState.update { it.copy(isBooking = true) }
        viewModelScope.launch {
            val quote = (state.quote as Loadable.Success).data
            createBooking(
                BookingRequest(
                    listingId = listingId,
                    checkIn = state.checkIn,
                    nights = quote.nights,
                    guests = state.guests,
                    guestName = state.guestName,
                    note = state.note,
                ),
            ).fold(
                onSuccess = { _events.send(ListingDetailEvent.BookingCreated(it.id)) }, // Stays "booking" while we leave.
                onFailure = { error ->
                    _viewState.update { it.copy(isBooking = false) }
                    _events.send(ListingDetailEvent.ShowMessage(error.toBookingMessage()))
                    // Someone else may have taken the dates: show the fresh answer.
                    if (error is BookingError) quoteAttempt.value++
                },
            )
        }
    }

    private companion object {
        const val MaxNights = 30
        const val MaxNoteLength = 200
    }
}
