package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun UserAvatar(
    avatar: String,
    userId: String,
    username: String,
    size: Dp,
    modifier: Modifier = Modifier,
    isSubscribed: Boolean = false,
    shape: Shape = RoundedCornerShape(10.dp),
) {
    val palette = navigationPalette()
    val url = remember(avatar, userId) { resolveAvatarUrl(avatar, userId) }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        RemoteImage(
            url = url,
            contentDescription = username,
            modifier = Modifier.size(size),
            shape = shape,
            placeholder = {
                Image(
                    painter = painterResource(R.drawable.no_avatar_user),
                    contentDescription = username,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(shape),
                )
            },
        )

        if (isSubscribed) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size.value * 0.34f).dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(palette.bar),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    kind = AppIconKind.Diamond,
                    tint = palette.accent,
                    modifier = Modifier.size((size.value * 0.24f).dp),
                )
            }
        }
    }
}

fun resolveAvatarUrl(avatar: String, userId: String): String {
    val trimmed = avatar.trim()
    if (trimmed.isEmpty() || userId.isBlank()) return ""
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        return rewriteLoopbackHost(trimmed)
    }
    val base = BuildConfig.API_BASE_URL.trimEnd('/')
    return "$base/user/avatar/${java.net.URLEncoder.encode(userId, "UTF-8")}"
}

internal fun rewriteLoopbackHost(
    url: String,
    apiBaseUrl: String = BuildConfig.API_BASE_URL,
): String {
    val parsed = runCatching { java.net.URI(url) }.getOrNull() ?: return url
    val host = parsed.host ?: return url
    if (!isLoopbackHost(host)) return url

    val apiHost = runCatching { java.net.URI(apiBaseUrl).host }.getOrNull()
    if (apiHost.isNullOrBlank() || isLoopbackHost(apiHost)) return url

    val port = if (parsed.port > 0) ":${parsed.port}" else ""
    val path = parsed.rawPath.orEmpty().ifEmpty { "/" }
    val query = parsed.rawQuery?.let { "?$it" }.orEmpty()
    return "${parsed.scheme}://$apiHost$port$path$query"
}

private fun isLoopbackHost(host: String): Boolean {
    val normalized = host.lowercase().removePrefix("[").removeSuffix("]")
    return normalized == "localhost" ||
        normalized == "127.0.0.1" ||
        normalized == "0.0.0.0" ||
        normalized == "::1"
}
