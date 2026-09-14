@file:OptIn(ExperimentalTime::class)

package com.mhss.app.datetime

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun currentLocalDate() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

val Long.localDateTime: LocalDateTime
    get() = Instant.fromEpochMilliseconds(this).toLocalDateTime(
        TimeZone.currentSystemDefault()
    )

fun Long.inTheLast30Days(): Boolean {
    return Instant.fromEpochMilliseconds(this).daysUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) <= 30
}

fun Long.inTheLastYear(): Boolean {
    return Instant.fromEpochMilliseconds(this).yearsUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) == 0
}

fun Long.inTheLastWeek(): Boolean {
    return Instant.fromEpochMilliseconds(this).daysUntil(
        Clock.System.now(),
        TimeZone.currentSystemDefault()
    ) <= 7
}

fun lastWeekCutoff(): Long {
    val timeZone = TimeZone.currentSystemDefault()
    return Clock.System.now()
        .minus(8, DateTimeUnit.DAY, timeZone)
        .toEpochMilliseconds()
}

fun LocalDateTime.isCurrentYear(): Boolean {
    return year == now().localDateTime.year
}

fun Long?.isDueDateOverdue(): Boolean {
    return this != null && this < now()
}

fun todayPlusDays(days: Int): Long {
    return now() + days.days.inWholeMilliseconds
}

fun now() = Clock.System.now().toEpochMilliseconds()

fun LocalDateTime.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return today.year == year
            && today.month == month
            && today.day == day
}

val Long.hour: Int
    get() = localDateTime.hour

val Long.minute: Int
    get() = localDateTime.minute

fun Long.at(hours: Int, minutes: Int): Long {
    val date = localDateTime
    return LocalDateTime(
        year = date.year,
        month = date.month,
        day = date.day,
        hour = hours,
        minute = minutes,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun Long.utcDateAt(hours: Int, minutes: Int): Long {
    val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
    return LocalDateTime(
        year = date.year,
        month = date.month,
        day = date.day,
        hour = hours,
        minute = minutes,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

fun Long.toUtcMidnight(): Long {
    val date = localDateTime.date
    return LocalDateTime(
        year = date.year,
        month = date.month,
        day = date.day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.UTC).toEpochMilliseconds()
}

fun Long.toDayOfWeek(): DayOfWeek {
    return Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .dayOfWeek
}

fun LocalDate.withTimeFrom(timestamp: Long): Long {
    val time = timestamp.localDateTime
    return LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = time.hour,
        minute = time.minute,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}

const val HOUR_MILLIS = 60 * 60 * 1000L
