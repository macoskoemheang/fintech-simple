package com.example.m3showcase.feature.locator.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.feature.locator.domain.model.BankLocation
import com.example.m3showcase.feature.locator.domain.model.LocationType
import com.example.m3showcase.feature.locator.domain.model.NearbyLocation
import com.example.m3showcase.feature.locator.domain.usecase.FilterLocationsUseCase
import com.example.m3showcase.feature.locator.domain.usecase.GetNearbyLocationsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class LocatorViewState(
    val content: Loadable<List<NearbyLocation>> = Loadable.Loading,
    val query: String = "",
    val type: LocationType? = null,
    val openNowOnly: Boolean = false,
    /** Shown in the bottom sheet. */
    val selected: NearbyLocation? = null,
)

sealed interface LocatorViewAction {
    data object OnRetry : LocatorViewAction
    data class OnQueryChanged(val query: String) : LocatorViewAction
    data class OnTypeSelected(val type: LocationType?) : LocatorViewAction
    data class OnOpenNowToggled(val enabled: Boolean) : LocatorViewAction
    data class OnLocationClicked(val id: String) : LocatorViewAction
    data object OnSheetDismissed : LocatorViewAction
    data class OnDirectionsClicked(val location: BankLocation) : LocatorViewAction
    data class OnCallClicked(val phone: String) : LocatorViewAction
    data object OnBackClicked : LocatorViewAction
}

sealed interface LocatorEvent {
    data class OpenDirections(val location: BankLocation) : LocatorEvent
    data class Dial(val phone: String) : LocatorEvent
    data object NavigateBack : LocatorEvent
}

class LocatorViewModel(
    private val getNearbyLocations: GetNearbyLocationsUseCase,
    private val filterLocations: FilterLocationsUseCase,
) : ViewModel() {

    private val all = MutableStateFlow<Loadable<List<NearbyLocation>>>(Loadable.Loading)
    private val query = MutableStateFlow("")
    private val type = MutableStateFlow<LocationType?>(null)
    private val openNowOnly = MutableStateFlow(false)
    private val selectedId = MutableStateFlow<String?>(null)

    private val _events = Channel<LocatorEvent>(Channel.BUFFERED)
    val events: Flow<LocatorEvent> = _events.receiveAsFlow()

    val viewState: StateFlow<LocatorViewState> = combine(all, query, type, openNowOnly, selectedId) { content, q, t, open, id ->
        val list = (content as? Loadable.Success)?.data.orEmpty()
        LocatorViewState(
            content = if (content is Loadable.Success) Loadable.Success(filterLocations(list, q, t, open)) else content,
            query = q,
            type = t,
            openNowOnly = open,
            selected = list.firstOrNull { it.location.id == id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LocatorViewState())

    init {
        load()
    }

    fun onAction(action: LocatorViewAction) {
        when (action) {
            LocatorViewAction.OnRetry -> load()
            is LocatorViewAction.OnQueryChanged -> query.value = action.query
            is LocatorViewAction.OnTypeSelected -> type.value = action.type
            is LocatorViewAction.OnOpenNowToggled -> openNowOnly.value = action.enabled
            is LocatorViewAction.OnLocationClicked -> selectedId.value = action.id
            LocatorViewAction.OnSheetDismissed -> selectedId.value = null
            is LocatorViewAction.OnDirectionsClicked -> _events.trySend(LocatorEvent.OpenDirections(action.location))
            is LocatorViewAction.OnCallClicked -> _events.trySend(LocatorEvent.Dial(action.phone))
            LocatorViewAction.OnBackClicked -> _events.trySend(LocatorEvent.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            all.value = Loadable.Loading
            all.value = getNearbyLocations().toLoadable()
        }
    }
}
