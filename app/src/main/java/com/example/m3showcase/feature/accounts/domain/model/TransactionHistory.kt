package com.example.m3showcase.feature.accounts.domain.model

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.Transaction

enum class TransactionFilter { All, MoneyIn, MoneyOut }

data class TransactionHistory(
    /** null when the history shows every account. */
    val account: Account?,
    val transactions: List<Transaction>,
    val isBalanceHidden: Boolean,
)
