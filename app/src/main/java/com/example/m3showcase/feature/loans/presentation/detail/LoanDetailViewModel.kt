package com.example.m3showcase.feature.loans.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.m3showcase.core.ui.Loadable
import com.example.m3showcase.core.ui.toLoadable
import com.example.m3showcase.feature.loans.domain.model.LoanDetail
import com.example.m3showcase.feature.loans.domain.usecase.GetLoanDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoanDetailViewModel(private val loanId: String, private val getLoan: GetLoanDetailUseCase) : ViewModel() {

    private val _viewState = MutableStateFlow<Loadable<LoanDetail>>(Loadable.Loading)
    val viewState: StateFlow<Loadable<LoanDetail>> = _viewState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _viewState.value = Loadable.Loading
            _viewState.value = getLoan(loanId).toLoadable()
        }
    }
}
