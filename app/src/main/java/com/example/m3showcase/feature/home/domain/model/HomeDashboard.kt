package com.example.m3showcase.feature.home.domain.model

import com.example.m3showcase.core.domain.model.Account
import com.example.m3showcase.core.domain.model.Transaction
import com.example.m3showcase.core.domain.model.UserProfile

data class HomeDashboard(
    val user: UserProfile?,
    val accounts: List<Account>,
    val recentTransactions: List<Transaction>,
    val isBalanceHidden: Boolean,
)
