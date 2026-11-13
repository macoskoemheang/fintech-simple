package com.example.m3showcase.core.language

import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.usecase.ObserveAppLanguageUseCase
import com.example.m3showcase.core.domain.usecase.SetAppLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/** Where the language-change transition is. */
sealed interface LanguageSwitchPhase {
    data object Idle : LanguageSwitchPhase
    val labels: SwitchLabels? get() = null

    /** The overlay is growing over the screen; the language has not changed yet. */
    data class Covering(val target: AppLanguage, override val labels: SwitchLabels) : LanguageSwitchPhase
    /** The language is applied and the Activity is being recreated behind a fully drawn overlay. */
    data class Applying(val target: AppLanguage, override val labels: SwitchLabels) : LanguageSwitchPhase
}

/**
 * What the overlay shows, worked out *before* the switch: afterwards the app (and on recent
 * Android even the "system" resources) already report the new language, so "from" would be lost.
 */
data class SwitchLabels(val fromName: String, val targetName: String, val targetLocale: Locale)

/**
 * Runs a language change as a covered transition instead of a hard Activity flash:
 * cover the screen → apply the language (Activity recreates underneath) → reveal.
 *
 * App-scoped (Koin single) on purpose: it must outlive the Activity recreation so the new
 * Activity knows to start fully covered and then reveal. Plain Kotlin, unit-tested.
 */
class LanguageSwitchController(
    private val observeLanguage: ObserveAppLanguageUseCase,
    private val setLanguage: SetAppLanguageUseCase,
    /** The device language, for "System default". Injected so tests need no Android. */
    private val deviceLocale: () -> Locale,
) {
    private val _phase = MutableStateFlow<LanguageSwitchPhase>(LanguageSwitchPhase.Idle)
    val phase: StateFlow<LanguageSwitchPhase> = _phase.asStateFlow()

    /** Starts the transition. Ignored when the language is already active or a switch is running. */
    fun switchTo(language: AppLanguage) {
        val current = observeLanguage().value
        if (_phase.value != LanguageSwitchPhase.Idle || language == current) return
        val device = deviceLocale()
        _phase.value = LanguageSwitchPhase.Covering(
            target = language,
            labels = SwitchLabels(current.nativeName(device), language.nativeName(device), language.locale(device)),
        )
    }

    /** Called by the overlay once it covers the screen. Applying recreates the Activity. */
    fun onCovered() {
        val covering = _phase.value as? LanguageSwitchPhase.Covering ?: return
        _phase.value = LanguageSwitchPhase.Applying(covering.target, covering.labels)
        setLanguage(covering.target)
    }

    /** Called by the overlay when it has faded out. */
    fun onRevealed() {
        if (_phase.value is LanguageSwitchPhase.Applying) _phase.value = LanguageSwitchPhase.Idle
    }
}
