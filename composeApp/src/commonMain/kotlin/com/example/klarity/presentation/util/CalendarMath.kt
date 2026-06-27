package com.example.klarity.presentation.util

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Date helpers for the task Calendar / date pickers.
 *
 * Deliberately uses only kotlinx-datetime 0.6.1 API (e.g. [LocalDate.dayOfMonth], [LocalDate.monthNumber])
 * — NOT the 0.7.x `day`/`Month.number` renames — because the build force-pins datetime to 0.6.1 (see
 * composeApp/build.gradle.kts). The whole reason the app ships a custom calendar instead of the Material 3
 * date picker is that M3's desktop CalendarModel is compiled against those 0.7.x APIs and crashes under the pin.
 */

internal val MONTH_ABBR = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
internal val MONTH_FULL = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)
internal val WEEKDAY_ABBR = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

internal fun appZone(): TimeZone = TimeZone.currentSystemDefault()

internal fun Instant.toLocalDate(): LocalDate = toLocalDateTime(appZone()).date
internal fun LocalDate.toStartInstant(): Instant = atStartOfDayIn(appZone())
internal fun todayDate(): LocalDate = Clock.System.todayIn(appZone())

internal fun Instant.toShortDate(): String {
    val d = toLocalDate()
    return "${MONTH_ABBR[d.monthNumber - 1]} ${d.dayOfMonth}"
}

internal fun Instant.toDisplayDate(): String {
    val d = toLocalDate()
    return "${MONTH_ABBR[d.monthNumber - 1]} ${d.dayOfMonth}, ${d.year}"
}

internal fun LocalDate.monthTitle(): String = "${MONTH_FULL[monthNumber - 1]} $year"

internal fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year, monthNumber, 1)
internal fun LocalDate.addMonths(n: Int): LocalDate = firstOfMonth().plus(n, DateTimeUnit.MONTH)

internal fun LocalDate.lengthOfMonth(): Int {
    val first = firstOfMonth()
    return first.daysUntil(first.plus(1, DateTimeUnit.MONTH))
}

/**
 * A Sunday-first calendar grid covering this date's month, padded with the leading/trailing days that
 * complete whole weeks. Cells outside the month are real dates too — callers compare [monthNumber] to grey them out.
 */
internal fun LocalDate.monthGrid(): List<LocalDate> {
    val first = firstOfMonth()
    val lead = first.dayOfWeek.isoDayNumber % 7 // SUN(7) -> 0, MON(1) -> 1, … SAT(6) -> 6
    val start = first.plus(-lead, DateTimeUnit.DAY)
    val cells = ((lead + lengthOfMonth() + 6) / 7) * 7
    return (0 until cells).map { start.plus(it, DateTimeUnit.DAY) }
}
