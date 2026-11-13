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

/** Day of week (1 = Monday … 7 = Sunday) and minutes since midnight. */
data class LocalMoment(val dayOfWeek: Int, val minuteOfDay: Int)

/** Injected so tests can fix "now" for opening-hours checks. */
fun interface LocalTimeProvider {
    fun now(): LocalMoment
}

/** Branch hours are local Cambodian time wherever the phone is. */
class CambodiaTimeProvider : LocalTimeProvider {
    override fun now(): LocalMoment {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Phnom_Penh"))
        val dow = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // Calendar: Sunday = 1 → 7, Monday = 2 → 1
        return LocalMoment(dow, cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE))
    }
}
