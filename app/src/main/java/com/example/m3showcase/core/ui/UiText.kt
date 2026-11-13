package com.example.m3showcase.core.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Text produced outside the UI (ViewModels, mappers) without a Context.
 *
 * ViewModels hold a resource id plus arguments, and the UI turns it into a string when it
 * draws. So a message already on screen re-translates when the language changes, and
 * ViewModels stay testable on the JVM (compare UiText values, no Android resources).
 *
 * Arguments may themselves be [UiText]; they are resolved first.
 */
@Immutable
sealed interface UiText {
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
    data class Plural(@PluralsRes val id: Int, val count: Int, val args: List<Any> = listOf(count)) : UiText
    /** Text that is already in the user's language (e.g. a name, or text the server localized). */
    data class Raw(val value: String) : UiText

    fun asString(context: Context): String = when (this) {
        is Raw -> value
        is Res -> context.getString(id, *args.resolve(context))
        is Plural -> context.resources.getQuantityString(id, count, *args.resolve(context))
    }

    companion object {
        fun res(@StringRes id: Int, vararg args: Any): UiText = Res(id, args.toList())
    }
}

private fun List<Any>.resolve(context: Context): Array<Any> =
    map { if (it is UiText) it.asString(context) else it }.toTypedArray()

/** Resolves in composition; re-reads when the configuration (locale) changes. */
@Composable
fun UiText.asString(): String {
    LocalConfiguration.current
    return asString(LocalContext.current)
}
