package com.example.m3showcase.core.ui

import androidx.compose.runtime.Immutable
import com.example.m3showcase.core.domain.model.BankError

/** One-shot screen data: the three states every detail screen has. */
@Immutable
sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data class Error(val message: UiText, val isOffline: Boolean) : Loadable<Nothing>
    data class Success<T>(val data: T) : Loadable<T>
}

fun Throwable.toLoadableError(): Loadable.Error = Loadable.Error(toUserMessage(), this is BankError.NoInternet)

fun <T> Result<T>.toLoadable(): Loadable<T> = fold({ Loadable.Success(it) }, { it.toLoadableError() })
