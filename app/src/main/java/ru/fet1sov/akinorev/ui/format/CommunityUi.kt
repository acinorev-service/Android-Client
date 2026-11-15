package ru.fet1sov.akinorev.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.fet1sov.akinorev.R
import java.util.Locale

@Composable
fun rememberCompactCount(value: Int): String {
    val locale = Locale.getDefault()
    val suffixes = CompactSuffixes(
        thousand = stringResource(R.string.community_compact_thousand),
        million = stringResource(R.string.community_compact_million),
        billion = stringResource(R.string.community_compact_billion),
        trillion = stringResource(R.string.community_compact_trillion),
    )
    return formatCompactNumber(value, suffixes, decimalComma = locale.language == "ru")
}

@Composable
fun rememberPostTimestamp(millis: Long?): String {
    val locale = Locale.getDefault()
    return when (val timestamp = formatPostTimestamp(millis, locale)) {
        is PostTimestamp.Today -> stringResource(R.string.community_timestamp_today, timestamp.time)
        is PostTimestamp.Yesterday -> stringResource(R.string.community_timestamp_yesterday, timestamp.time)
        is PostTimestamp.FullDate -> timestamp.text
        PostTimestamp.Empty -> ""
    }
}

@Composable
fun rememberLastSeenLabel(millis: Long?): String {
    val locale = Locale.getDefault()
    return when (val status = lastSeenStatus(millis, locale)) {
        LastSeenStatus.Offline -> stringResource(R.string.community_profile_offline)
        LastSeenStatus.Recently -> stringResource(R.string.community_profile_last_seen_recently)
        is LastSeenStatus.Minutes -> stringResource(R.string.community_profile_last_seen_minutes, status.count)
        is LastSeenStatus.Hours -> stringResource(R.string.community_profile_last_seen_hours, status.count)
        is LastSeenStatus.Today -> stringResource(R.string.community_profile_last_seen_today, status.time)
        LastSeenStatus.Yesterday -> stringResource(R.string.community_profile_last_seen_yesterday)
        is LastSeenStatus.Days -> stringResource(R.string.community_profile_last_seen_days, status.count)
        LastSeenStatus.Week -> stringResource(R.string.community_profile_last_seen_week)
        is LastSeenStatus.SinceDate -> stringResource(R.string.community_profile_last_seen_since, status.date)
    }
}
