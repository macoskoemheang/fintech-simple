package com.example.m3showcase.core.localization

import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.repository.LanguageRepository
import com.example.m3showcase.core.domain.usecase.ObserveAppLanguageUseCase
import com.example.m3showcase.core.domain.usecase.SetAppLanguageUseCase
import com.example.m3showcase.core.language.LanguageSwitchController
import com.example.m3showcase.core.language.LanguageSwitchPhase
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.m3showcase.core.language.SwitchLabels
import java.util.Locale

class LanguageSwitchControllerTest {

    private class FakeLanguageRepository : LanguageRepository {
        override val language = MutableStateFlow(AppLanguage.English)
        val applied = mutableListOf<AppLanguage>()
        override fun setLanguage(language: AppLanguage) {
            applied += language
            this.language.value = language
        }
    }

    private val repo = FakeLanguageRepository()
    private val controller = LanguageSwitchController(ObserveAppLanguageUseCase(repo), SetAppLanguageUseCase(repo)) { Locale.ENGLISH }
    private val labels = SwitchLabels(fromName = "English", targetName = "ខ្មែរ", targetLocale = Locale.forLanguageTag("km"))

    @Test
    fun `covers first, applies only once covered, then returns to idle`() {
        controller.switchTo(AppLanguage.Khmer)
        assertEquals(LanguageSwitchPhase.Covering(AppLanguage.Khmer, labels), controller.phase.value)
        assertEquals(emptyList<AppLanguage>(), repo.applied) // nothing changes under the user yet

        controller.onCovered()
        assertEquals(LanguageSwitchPhase.Applying(AppLanguage.Khmer, labels), controller.phase.value)
        assertEquals(listOf(AppLanguage.Khmer), repo.applied)

        controller.onRevealed()
        assertEquals(LanguageSwitchPhase.Idle, controller.phase.value)
    }

    @Test
    fun `System shows the device language name, not the app's`() {
        repo.language.value = AppLanguage.Khmer
        controller.switchTo(AppLanguage.System)
        val shown = controller.phase.value.labels!!
        assertEquals("ខ្មែរ", shown.fromName)
        assertEquals("English", shown.targetName)
        assertEquals(Locale.ENGLISH, shown.targetLocale)
    }

    @Test
    fun `picking the current language does nothing`() {
        controller.switchTo(AppLanguage.English)
        assertEquals(LanguageSwitchPhase.Idle, controller.phase.value)
    }

    @Test
    fun `a second pick during a switch is ignored`() {
        controller.switchTo(AppLanguage.Khmer)
        controller.switchTo(AppLanguage.Chinese)
        controller.onCovered()
        assertEquals(listOf(AppLanguage.Khmer), repo.applied)
    }

    @Test
    fun `out-of-order callbacks are ignored`() {
        controller.onCovered()
        controller.onRevealed()
        assertEquals(LanguageSwitchPhase.Idle, controller.phase.value)
        assertEquals(emptyList<AppLanguage>(), repo.applied)
    }
}
