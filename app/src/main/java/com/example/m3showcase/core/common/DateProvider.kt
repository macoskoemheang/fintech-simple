package com.example.m3showcase.core.common

import com.example.m3showcase.core.domain.model.CalendarDate
import java.util.Calendar
import java.util.TimeZone

/** Injected so tests can fix "today". */
fun interface DateProvider {
    fun today(): CalendarDate
}

/** Festivals follow Cambodian dates wherever the phone is. */
class CambodiaDateProvider : DateProvider {
    override fun today(): CalendarDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Phnom_Penh"))
        return CalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}
