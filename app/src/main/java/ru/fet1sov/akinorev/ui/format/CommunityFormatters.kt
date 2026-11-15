package ru.fet1sov.akinorev.ui.format

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToLong

data class CompactSuffixes(
    val thousand: String,
    val million: String,
    val billion: String,
    val trillion: String,
)

sealed interface PostTimestamp {
    data class Today(val time: String) : PostTimestamp

    data class Yesterday(val time: String) : PostTimestamp

    data class FullDate(val text: String) : PostTimestamp

    data object Empty : PostTimestamp
}

sealed interface LastSeenStatus {
    data object Offline : LastSeenStatus

    data object Recently : LastSeenStatus

    data class Minutes(val count: Int) : LastSeenStatus

    data class Hours(val count: Int) : LastSeenStatus

    data class Today(val time: String) : LastSeenStatus

    data object Yesterday : LastSeenStatus

    data class Days(val count: Int) : LastSeenStatus

    data object Week : LastSeenStatus

    data class SinceDate(val date: String) : LastSeenStatus
}

fun formatCompactNumber(
    value: Int,
    suffixes: CompactSuffixes,
    decimalComma: Boolean,
): String {
    val amount = abs(value.toLong())
    val scaled: Double
    val suffix: String

    when {
        amount >= 1_000_000_000_000L -> {
            scaled = amount / 1_000_000_000_000.0
            suffix = suffixes.trillion
        }

        amount >= 1_000_000_000L -> {
            scaled = amount / 1_000_000_000.0
            suffix = suffixes.billion
        }

        amount >= 1_000_000L -> {
            scaled = amount / 1_000_000.0
            suffix = suffixes.million
        }

        amount >= 1_000L -> {
            scaled = amount / 1_000.0
            suffix = suffixes.thousand
        }

        else -> return amount.toString()
    }

    val text = when {
        scaled >= 100 -> scaled.roundToLong().toString()
        scaled >= 10 -> String.format(Locale.US, "%.0f", scaled)
        else -> String.format(Locale.US, "%.1f", scaled)
    }
    val normalized = if (decimalComma) text.replace('.', ',') else text
    return "$normalized $suffix"
}

fun formatPostTimestamp(
    millis: Long?,
    locale: Locale,
    nowMillis: Long = System.currentTimeMillis(),
): PostTimestamp {
    if (millis == null) return PostTimestamp.Empty
    val zone = ZoneId.systemDefault()
    val date = runCatching { Instant.ofEpochMilli(millis).atZone(zone) }.getOrNull()
        ?: return PostTimestamp.Empty
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    val days = now.toLocalDate().toEpochDay() - date.toLocalDate().toEpochDay()
    val time = date.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))

    return when (days) {
        0L -> PostTimestamp.Today(time)
        1L -> PostTimestamp.Yesterday(time)
        else -> PostTimestamp.FullDate(
            date.format(
                DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale),
            ),
        )
    }
}

fun lastSeenStatus(
    millis: Long?,
    locale: Locale,
    nowMillis: Long = System.currentTimeMillis(),
): LastSeenStatus {
    if (millis == null) return LastSeenStatus.Offline
    val diff = nowMillis - millis
    if (diff < 0) return LastSeenStatus.Offline

    return when {
        diff < 60_000L -> LastSeenStatus.Recently
        diff < 3_600_000L -> LastSeenStatus.Minutes((diff / 60_000L).toInt().coerceAtLeast(1))
        diff < 10_800_000L -> LastSeenStatus.Hours((diff / 3_600_000L).toInt().coerceAtLeast(1))
        diff < 86_400_000L -> LastSeenStatus.Today(formatTime(millis, locale))
        else -> {
            val days = floor(diff / 86_400_000.0).toLong()
            when {
                days <= 1L -> LastSeenStatus.Yesterday
                days < 7L -> LastSeenStatus.Days(days.toInt())
                days == 7L -> LastSeenStatus.Week
                else -> LastSeenStatus.SinceDate(formatShortDate(millis, locale))
            }
        }
    }
}

fun formatMemberSince(millis: Long?, locale: Locale): String {
    if (millis == null) return ""
    return runCatching {
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
            .uppercase(locale)
    }.getOrDefault("")
}

fun formatCommentDate(millis: Long?, locale: Locale): String {
    if (millis == null) return ""
    return runCatching {
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM, HH:mm", locale))
    }.getOrDefault("")
}

fun formatTime(millis: Long, locale: Locale): String = runCatching {
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
}.getOrDefault("")

fun formatShortDate(millis: Long, locale: Locale): String = runCatching {
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", locale))
}.getOrDefault("")
