package ru.fet1sov.akinorev.ui.format

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private val ExpiryFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

fun formatMoney(value: Double): String {
    val safe = if (value.isFinite()) value else 0.0
    val fixed = String.format(java.util.Locale.US, "%.2f", abs(safe))
    val parts = fixed.split('.')
    val integer = parts.getOrElse(0) { "0" }
    val fraction = parts.getOrElse(1) { "00" }
    val grouped = integer.reversed().chunked(3).joinToString(" ").reversed()
    val sign = if (safe < 0) "-" else ""
    return "$sign$grouped.$fraction\$"
}

fun formatSubscriptionExpiry(value: String?): String {
    if (value.isNullOrBlank()) return ""
    val date = runCatching { OffsetDateTime.parse(value).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).toLocalDate() }.getOrNull()
        ?: runCatching { java.time.Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
        ?: return ""
    return date.format(ExpiryFormatter)
}
