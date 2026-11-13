package com.example.m3showcase.core.data.repository

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Backed by AppCompat's per-app language API:
 * - Android 13+: the system LocaleManager stores it and lists it in system Settings.
 * - Below 13: AppCompat stores it (autoStoreLocales in the manifest) and applies it to AppCompatActivity.
 * Either way the Activity is recreated, so every string resource reloads in the new language.
 */
class AppCompatLanguageRepository : LanguageRepository {

    private val _language = MutableStateFlow(current())
    override val language: StateFlow<AppLanguage> = _language.asStateFlow()

    override fun setLanguage(language: AppLanguage) {
        if (language == _language.value) return
        _language.value = language
        val locales = language.tag?.let(LocaleListCompat::forLanguageTags) ?: LocaleListCompat.getEmptyLocaleList()
        AppCompatDelegate.setApplicationLocales(locales) // Must run on the main thread.
    }

    private fun current(): AppLanguage =
        AppLanguage.fromTag(AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(','))
}
