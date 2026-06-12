package com.elg.myges.adapters.primary.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun formatInstant(value: Instant?): String {
    if (value == null) return ""
    val locale = currentJavaLocale()
    return remember(value, locale) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(ZoneId.systemDefault())
            .format(value)
    }
}

@Composable
fun formatDate(value: LocalDate?): String {
    if (value == null) return ""
    val locale = currentJavaLocale()
    return remember(value, locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(value)
    }
}

@Composable
fun formatNumber(value: Double?): String {
    if (value == null) return ""
    val locale = currentJavaLocale()
    return remember(value, locale) {
        NumberFormat.getNumberInstance(locale).apply {
            maximumFractionDigits = 2
        }.format(value)
    }
}

@Composable
fun currentJavaLocale(): Locale {
    val configuration: Configuration = LocalConfiguration.current
    return configuration.locales[0] ?: Locale.getDefault()
}
