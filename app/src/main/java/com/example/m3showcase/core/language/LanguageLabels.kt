package com.example.m3showcase.core.language

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.LocaleManagerCompat
import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.AppLanguage
import java.util.Locale

/**
 * The device language, ignoring the per-app language. (Once an app language is set,
 * Resources.getSystem() reports it too on recent Android, so it can't be used for this.)
 */
fun deviceLocale(context: Context): Locale =
    LocaleManagerCompat.getSystemLocales(context)[0] ?: Locale.getDefault()

/** The locale a language resolves to; System means the device's own language. */
fun AppLanguage.locale(device: Locale): Locale = tag?.let(Locale::forLanguageTag) ?: device

/**
 * The language's own name in its own script, so people can find theirs whatever the app is
 * showing now. Only "System default" is translated.
 */
@Composable
fun AppLanguage.endonym(): String = when (this) {
    AppLanguage.System -> stringResource(R.string.settings_language_system)
    AppLanguage.English -> "English"
    AppLanguage.Khmer -> "ខ្មែរ"
    AppLanguage.Chinese -> "中文（简体）"
}

/** For the transition overlay: System shows the device language's own name, e.g. "English". */
fun AppLanguage.nativeName(device: Locale): String = when (this) {
    AppLanguage.System -> device.getDisplayLanguage(device).replaceFirstChar { it.titlecase(device) }
    AppLanguage.English -> "English"
    AppLanguage.Khmer -> "ខ្មែរ"
    AppLanguage.Chinese -> "中文（简体）"
}

/** A string from the TARGET language's resources, before that language is applied to the app. */
@Composable
fun stringResourceIn(locale: Locale, @StringRes id: Int): String {
    val context = LocalContext.current
    return remember(locale, id) { context.localized(locale).getString(id) }
}

private fun Context.localized(locale: Locale): Context =
    createConfigurationContext(Configuration(resources.configuration).apply { setLocale(locale) })
