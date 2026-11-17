package com.example.m3showcase.feature.booking.presentation.hub

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.feature.booking.domain.model.Listing
import com.example.m3showcase.feature.booking.domain.model.ListingCategory
import com.example.m3showcase.feature.booking.domain.model.McpConnectionState
import com.example.m3showcase.feature.booking.domain.usecase.BookingGroups
import com.example.m3showcase.feature.booking.domain.usecase.ConnectBookingServerUseCase
import com.example.m3showcase.feature.booking.domain.usecase.GetBookingsUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveBookingsVersionUseCase
import com.example.m3showcase.feature.booking.domain.usecase.ObserveMcpConnectionUseCase
import com.example.m3showcase.feature.booking.domain.usecase.SearchListingsUseCase
import com.example.m3showcase.feature.booking.presentation.toBookingLoadable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BookingTab { Explore, Trips }

@Immutable
data class BookingHubViewState(
    val connection: McpConnectionState = McpConnectionState.Idle,
    val tab: BookingTab = BookingTab.Explore,
    val query: String = "",
    /** null = every category. */
    val category: ListingCategory? = null,
    val listings: Loadable<List<Listing>> = Loadable.Loading,
    val bookings: Loadable<BookingGroups> = Loadable.Loading,
)

sealed interface BookingHubViewAction {
    data class OnTabSelected(val tab: BookingTab) : BookingHubViewAction
    data class OnQueryChanged(val query: String) : BookingHubViewAction
    data class OnCategorySelected(val category: ListingCategory?) : BookingHubViewAction
    data class OnListingClicked(val id: String) : BookingHubViewAction
    data class OnBookingClicked(val id: String) : BookingHubViewAction
    data object OnRetry : BookingHubViewAction
    data object OnReconnect : BookingHubViewAction
    data object OnInspectorClicked : BookingHubViewAction
    data object OnBackClicked : BookingHubViewAction
}

sealed interface BookingHubEvent {
    data class OpenListing(val id: String) : BookingHubEvent
    data class OpenBooking(val id: String) : BookingHubEvent
    data object OpenInspector : BookingHubEvent
    data object NavigateBack : BookingHubEvent
}

private data class LocalState(val tab: BookingTab, val query: String, val category: ListingCategory?)

@OptIn(FlowPreview::class)
class BookingHubViewModel(
    observeConnection: ObserveMcpConnectionUseCase,
    observeBookingsVersion: ObserveBookingsVersionUseCase,
    private val connect: ConnectBookingServerUseCase,
    private val searchListings: SearchListingsUseCase,
    private val getBookings: GetBookingsUseCase,
) : ViewModel() {

    private val tab = MutableStateFlow(BookingTab.Explore)
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ListingCategory?>(null)
    private val listings = MutableStateFlow<Loadable<List<Listing>>>(Loadable.Loading)
    private val bookings = MutableStateFlow<Loadable<BookingGroups>>(Loadable.Loading)
    /** Bumped by retry so the search runs again with the same inputs. */
    private val searchAttempt = MutableStateFlow(0)
    private val bookingsVersion = observeBookingsVersion()

    private val _events = Channel<BookingHubEvent>(Channel.BUFFERED)
    val events: Flow<BookingHubEvent> = _events.receiveAsFlow()

    private val local = combine(tab, query, category, ::LocalState)

    val viewState: StateFlow<BookingHubViewState> = combine(observeConnection(), local, listings, bookings) { connection, local, listings, bookings ->
        BookingHubViewState(connection, local.tab, local.query, local.category, listings, bookings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookingHubViewState())

    init {
        viewModelScope.launch { connect() }
        viewModelScope.launch {
            // Typing waits for a pause; a category tap or retry searches right away.
            combine(query.debounce { if (it.isEmpty()) 0 else 300 }, category, searchAttempt) { q, c, _ -> q to c }
                .collectLatest { (q, c) ->
                    if (listings.value !is Loadable.Success) listings.value = Loadable.Loading
                    listings.value = searchListings(q, c).toBookingLoadable()
                }
        }
        // Reloads on start and whenever a booking is made or cancelled anywhere.
        viewModelScope.launch { bookingsVersion.collectLatest { loadBookings() } }
    }

    fun onAction(action: BookingHubViewAction) {
        when (action) {
            is BookingHubViewAction.OnTabSelected -> tab.value = action.tab
            is BookingHubViewAction.OnQueryChanged -> query.value = action.query
            is BookingHubViewAction.OnCategorySelected -> category.value = action.category
            is BookingHubViewAction.OnListingClicked -> _events.trySend(BookingHubEvent.OpenListing(action.id))
            is BookingHubViewAction.OnBookingClicked -> _events.trySend(BookingHubEvent.OpenBooking(action.id))
            BookingHubViewAction.OnRetry -> retry()
            BookingHubViewAction.OnReconnect -> viewModelScope.launch {
                if (connect(reconnect = true).isSuccess) retry()
            }
            BookingHubViewAction.OnInspectorClicked -> _events.trySend(BookingHubEvent.OpenInspector)
            BookingHubViewAction.OnBackClicked -> _events.trySend(BookingHubEvent.NavigateBack)
        }
    }

    private fun retry() {
        if (listings.value is Loadable.Error) listings.value = Loadable.Loading
        searchAttempt.value++
        viewModelScope.launch { loadBookings() }
    }

    private suspend fun loadBookings() {
        if (bookings.value !is Loadable.Success) bookings.value = Loadable.Loading
        val result = getBookings().toBookingLoadable()
        // A failed refresh keeps the list already on screen.
        if (result is Loadable.Success || bookings.value !is Loadable.Success) bookings.value = result
    }
}
