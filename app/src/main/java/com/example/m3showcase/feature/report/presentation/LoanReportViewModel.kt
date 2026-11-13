package com.example.m3showcase.feature.report.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.feature.report.domain.model.LoanReport
import com.example.m3showcase.feature.report.domain.model.TriggerRow
import com.example.m3showcase.feature.report.domain.usecase.GetLoanReportUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class LoanReportViewState(
    val content: Loadable<LoanReport> = Loadable.Loading,
    /** 0-based day highlighted in the chart; null = none. */
    val selectedDay: Int? = null,
)

sealed interface LoanReportViewAction {
    data object OnRetry : LoanReportViewAction
    data class OnDaySelected(val index: Int) : LoanReportViewAction
    data class OnCountClicked(val row: TriggerRow, val day: Int) : LoanReportViewAction
    data object OnBackClicked : LoanReportViewAction
}

sealed interface LoanReportEvent {
    /** The UI words it: "3 × New Enquiry on 12 Nov". [dayIndex] is 0-based. */
    data class ShowCount(val count: Int, val triggerType: String, val dayIndex: Int) : LoanReportEvent
    data object NavigateBack : LoanReportEvent
}

class LoanReportViewModel(private val getReport: GetLoanReportUseCase) : ViewModel() {

    private val _viewState = MutableStateFlow(LoanReportViewState())
    val viewState: StateFlow<LoanReportViewState> = _viewState.asStateFlow()

    private val _events = Channel<LoanReportEvent>(Channel.BUFFERED)
    val events: Flow<LoanReportEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onAction(action: LoanReportViewAction) {
        when (action) {
            LoanReportViewAction.OnRetry -> load()
            is LoanReportViewAction.OnDaySelected -> _viewState.update {
                it.copy(selectedDay = action.index.takeUnless { index -> index == it.selectedDay })
            }
            // The table's day is 1-based.
            is LoanReportViewAction.OnCountClicked -> _events.trySend(
                LoanReportEvent.ShowCount(action.row.dailyCounts[action.day - 1], action.row.triggerType, action.day - 1),
            )
            LoanReportViewAction.OnBackClicked -> _events.trySend(LoanReportEvent.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            _viewState.update { it.copy(content = Loadable.Loading) }
            val result = getReport().toLoadable()
            _viewState.update { it.copy(content = result, selectedDay = (result as? Loadable.Success)?.data?.summary?.busiestDayIndex) }
        }
    }
}
