package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.fet1sov.akinorev.model.ProfileBadge
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.BadgeIcon
import kotlin.math.roundToInt

@Composable
fun ProfileBadges(
    badges: List<ProfileBadge>,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
) {
    if (badges.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        badges.sortedBy { it.order }.forEach { badge ->
            val icon = badge.icon
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center,
            ) {
                if (isRemoteIcon(icon)) {
                    RemoteImage(
                        url = resolveMediaUrl(icon),
                        contentDescription = badge.name,
                        modifier = Modifier.size(size * 0.75f),
                        shape = CircleShape,
                    )
                } else {
                    BadgeIcon(
                        slug = icon.ifBlank { "badge-common" },
                        contentDescription = badge.name,
                        size = (size * 0.75f).value.roundToInt().coerceAtLeast(1),
                    )
                }
            }
        }
    }
}

private fun isRemoteIcon(icon: String): Boolean =
    icon.startsWith("http://", ignoreCase = true) ||
        icon.startsWith("https://", ignoreCase = true) ||
        icon.startsWith("/")
