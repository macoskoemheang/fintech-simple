package com.example.m3showcase.feature.deposits.presentation.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.deposits.domain.usecase.DepositPortfolio
import com.example.m3showcase.feature.deposits.domain.usecase.GetDepositsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class DepositsViewState(val content: Loadable<DepositPortfolio> = Loadable.Loading, val isRefreshing: Boolean = false)

sealed interface DepositsViewAction {
    data object OnRefresh : DepositsViewAction
    data object OnRetry : DepositsViewAction
    data class OnDepositClicked(val id: String) : DepositsViewAction
    data object OnBackClicked : DepositsViewAction
}

sealed interface DepositsEvent {
    data class ShowMessage(val text: UiText) : DepositsEvent
    data class OpenDeposit(val id: String) : DepositsEvent
    data object NavigateBack : DepositsEvent
}

class DepositsViewModel(private val getDeposits: GetDepositsUseCase) : ViewModel() {

    private val _viewState = MutableStateFlow(DepositsViewState())
    val viewState: StateFlow<DepositsViewState> = _viewState.asStateFlow()

    private val _events = Channel<DepositsEvent>(Channel.BUFFERED)
    val events: Flow<DepositsEvent> = _events.receiveAsFlow()

    init {
        load(refresh = false)
    }

    fun onAction(action: DepositsViewAction) {
        when (action) {
            DepositsViewAction.OnRefresh -> load(refresh = true)
            DepositsViewAction.OnRetry -> load(refresh = false)
            is DepositsViewAction.OnDepositClicked -> _events.trySend(DepositsEvent.OpenDeposit(action.id))
            DepositsViewAction.OnBackClicked -> _events.trySend(DepositsEvent.NavigateBack)
        }
    }

    private fun load(refresh: Boolean) {
        if (_viewState.value.isRefreshing) return
        viewModelScope.launch {
            _viewState.update { if (refresh) it.copy(isRefreshing = true) else it.copy(content = Loadable.Loading) }
            val result = getDeposits()
            _viewState.update { current ->
                val failedRefresh = refresh && result.isFailure && current.content is Loadable.Success
                if (failedRefresh) current.copy(isRefreshing = false) // keep what's on screen
                else current.copy(content = result.toLoadable(), isRefreshing = false)
            }
            if (refresh) result.exceptionOrNull()?.let { _events.send(DepositsEvent.ShowMessage(it.toUserMessage())) }
        }
    }
}
