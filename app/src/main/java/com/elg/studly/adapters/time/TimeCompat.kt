package com.elg.studly.adapters.time

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Bridges the shared `kotlin.time.Instant` / `kotlinx.datetime.LocalDate` (now used by
 * the domain models) to the `java.time`-style API the Android adapters already rely on.
 *
 * ponytail: thin Android-only compat layer so the existing Android UI/storage code keeps
 * working without a per-call-site rewrite. iOS never sees this — it consumes the shared
 * types directly. Names deliberately mirror `java.time` so the Android call sites are
 * unchanged.
 */

// ---- kotlin.time.Instant <-> java.time (Android only) ----

fun Instant.toJavaInstant(): java.time.Instant =
    java.time.Instant.ofEpochSecond(epochSeconds, nanosecondsOfSecond.toLong())

fun java.time.Instant.toKotlinInstant(): Instant =
    Instant.fromEpochSeconds(epochSecond, nano.toLong())

fun java.time.LocalDate.toKotlinLocalDate(): LocalDate =
    LocalDate(year, monthValue, dayOfMonth)

fun Instant.atZone(zone: ZoneId): ZonedDateTime = toJavaInstant().atZone(zone)

fun Instant.toEpochMilli(): Long = toEpochMilliseconds()

fun Instant.isAfter(other: Instant): Boolean = this > other

fun Instant.isBefore(other: Instant): Boolean = this < other

fun Instant.plusSeconds(seconds: Long): Instant = this + seconds.seconds

fun Instant.minusSeconds(seconds: Long): Instant = this - seconds.seconds

// ---- kotlinx.datetime.LocalDate java.time-style helpers (Android only) ----

fun LocalDate.toJavaLocalDate(): java.time.LocalDate =
    java.time.LocalDate.of(year, monthNumber, dayOfMonth)

val LocalDate.monthValue: Int get() = monthNumber

fun LocalDate.plusDays(days: Long): LocalDate = plus(days.toInt(), DateTimeUnit.DAY)

fun LocalDate.minusDays(days: Long): LocalDate = minus(days.toInt(), DateTimeUnit.DAY)

fun LocalDate.plusWeeks(weeks: Long): LocalDate = plus(weeks.toInt(), DateTimeUnit.WEEK)

fun LocalDate.minusWeeks(weeks: Long): LocalDate = minus(weeks.toInt(), DateTimeUnit.WEEK)

fun LocalDate.plusMonths(months: Long): LocalDate = plus(months.toInt(), DateTimeUnit.MONTH)

fun LocalDate.minusMonths(months: Long): LocalDate = minus(months.toInt(), DateTimeUnit.MONTH)

fun LocalDate.withDayOfMonth(day: Int): LocalDate = LocalDate(year, monthNumber, day)

fun LocalDate.lengthOfMonth(): Int =
    LocalDate(year, monthNumber, 1).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth

/** java.time's `with(previousOrSame(MONDAY))` — the Monday of this date's week. */
fun LocalDate.previousOrSameMonday(): LocalDate = minus(dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
