package com.example.m3showcase.core.localization

import com.example.m3showcase.R
import com.example.m3showcase.core.domain.model.AppLanguage
import com.example.m3showcase.core.domain.model.BankError
import com.example.m3showcase.core.ui.UiText
import com.example.m3showcase.core.ui.toUserMessage
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizationTest {

    @Test
    fun `stored tags map back to languages`() {
        assertEquals(AppLanguage.System, AppLanguage.fromTag(null))
        assertEquals(AppLanguage.System, AppLanguage.fromTag(""))
        assertEquals(AppLanguage.English, AppLanguage.fromTag("en-US"))
        assertEquals(AppLanguage.Khmer, AppLanguage.fromTag("km"))
        assertEquals(AppLanguage.Khmer, AppLanguage.fromTag("km-KH"))
        assertEquals(AppLanguage.Chinese, AppLanguage.fromTag("zh-CN"))
        assertEquals(AppLanguage.Chinese, AppLanguage.fromTag("zh"))
        assertEquals(AppLanguage.System, AppLanguage.fromTag("fr"))
    }

    @Test
    fun `errors become resource ids, not English text`() {
        assertEquals(UiText.res(R.string.error_no_internet), BankError.NoInternet.toUserMessage())
        assertEquals(UiText.res(R.string.error_generic), IllegalStateException("boom").toUserMessage())
    }

    @Test
    fun `validation rules carry their limit into the message`() {
        assertEquals(
            UiText.res(R.string.validation_pin_length, 4),
            BankError.Validation(BankError.Field.Pin, BankError.Rule.WrongLength, 4).toUserMessage(),
        )
        assertEquals(
            UiText.res(R.string.validation_account_length, 9),
            BankError.Validation(BankError.Field.AccountNumber, BankError.Rule.WrongLength, 9).toUserMessage(),
        )
        assertEquals(
            UiText.Plural(R.plurals.validation_amount_decimals, 2),
            BankError.Validation(BankError.Field.Amount, BankError.Rule.TooManyDecimals, 2).toUserMessage(),
        )
    }
}
