package com.example.m3showcase.feature.loans.presentation.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.core.ui.toUserMessage
import com.example.m3showcase.feature.loans.domain.model.LoanPortfolio
import com.example.m3showcase.feature.loans.domain.usecase.GetLoansUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class LoansViewState(val content: Loadable<LoanPortfolio> = Loadable.Loading, val isRefreshing: Boolean = false)

sealed interface LoansViewAction {
    data object OnRefresh : LoansViewAction
    data object OnRetry : LoansViewAction
    data class OnLoanClicked(val id: String) : LoansViewAction
    data object OnReportClicked : LoansViewAction
    data object OnBackClicked : LoansViewAction
}

sealed interface LoansEvent {
    data class ShowMessage(val text: UiText) : LoansEvent
    data class OpenLoan(val id: String) : LoansEvent
    data object OpenReport : LoansEvent
    data object NavigateBack : LoansEvent
}

class LoansViewModel(private val getLoans: GetLoansUseCase) : ViewModel() {

    private val _viewState = MutableStateFlow(LoansViewState())
    val viewState: StateFlow<LoansViewState> = _viewState.asStateFlow()

    private val _events = Channel<LoansEvent>(Channel.BUFFERED)
    val events: Flow<LoansEvent> = _events.receiveAsFlow()

    init {
        load(refresh = false)
    }

    fun onAction(action: LoansViewAction) {
        when (action) {
            LoansViewAction.OnRefresh -> load(refresh = true)
            LoansViewAction.OnRetry -> load(refresh = false)
            is LoansViewAction.OnLoanClicked -> _events.trySend(LoansEvent.OpenLoan(action.id))
            LoansViewAction.OnReportClicked -> _events.trySend(LoansEvent.OpenReport)
            LoansViewAction.OnBackClicked -> _events.trySend(LoansEvent.NavigateBack)
        }
    }

    private fun load(refresh: Boolean) {
        if (_viewState.value.isRefreshing) return
        viewModelScope.launch {
            _viewState.update { if (refresh) it.copy(isRefreshing = true) else it.copy(content = Loadable.Loading) }
            val result = getLoans()
            _viewState.update { current ->
                if (refresh && result.isFailure && current.content is Loadable.Success) current.copy(isRefreshing = false)
                else current.copy(content = result.toLoadable(), isRefreshing = false)
            }
            if (refresh) result.exceptionOrNull()?.let { _events.send(LoansEvent.ShowMessage(it.toUserMessage())) }
        }
    }
}
