package com.example.m3showcase.core.domain.model

/*
 * Date arithmetic on CalendarDate without java.time (minSdk 24 has no desugaring here).
 * Epoch-day conversion uses Howard Hinnant's civil-from-days algorithm, the same math
 * java.time.LocalDate uses, so results match it exactly.
 */

fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

/** Days since 1970-01-01. */
fun CalendarDate.toEpochDay(): Long {
    val y = if (month <= 2) year - 1L else year.toLong()
    val era = Math.floorDiv(y, 400L)
    val yoe = y - era * 400
    val mp = (month + 9) % 12
    val doy = (153 * mp + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146_097 + doe - 719_468
}

fun calendarDateOfEpochDay(epochDay: Long): CalendarDate {
    val z = epochDay + 719_468
    val era = Math.floorDiv(z, 146_097L)
    val doe = z - era * 146_097
    val yoe = (doe - doe / 1_460 + doe / 36_524 - doe / 146_096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val month = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val year = (yoe + era * 400 + if (month <= 2) 1 else 0).toInt()
    return CalendarDate(year, month, day)
}

fun CalendarDate.plusDays(days: Long): CalendarDate = calendarDateOfEpochDay(toEpochDay() + days)

/** Same day [months] later; the 31st becomes the month's last day when needed (Jan 31 + 1 = Feb 28/29). */
fun CalendarDate.plusMonths(months: Int): CalendarDate {
    val total = year * 12 + (month - 1) + months
    val y = Math.floorDiv(total, 12)
    val m = Math.floorMod(total, 12) + 1
    return CalendarDate(y, m, minOf(day, daysInMonth(y, m)))
}

fun CalendarDate.daysUntil(other: CalendarDate): Long = other.toEpochDay() - toEpochDay()

/** Whole months from this date to [other] (negative if [other] is earlier). */
fun CalendarDate.monthsUntil(other: CalendarDate): Int {
    var months = (other.year - year) * 12 + (other.month - month)
    if (months > 0 && plusMonths(months) > other) months--
    if (months < 0 && plusMonths(months) < other) months++
    return months
}

/** "2026-11-23" → CalendarDate; the wire format the fake APIs use. */
fun parseIsoDate(text: String): CalendarDate = text.split('-').let { (y, m, d) -> CalendarDate(y.toInt(), m.toInt(), d.toInt()) }

fun CalendarDate.toIsoString(): String = "%04d-%02d-%02d".format(year, month, day)
