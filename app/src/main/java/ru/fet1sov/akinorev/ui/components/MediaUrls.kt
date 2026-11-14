package ru.fet1sov.akinorev.ui.components

import ru.fet1sov.akinorev.BuildConfig

fun resolveMediaUrl(
    path: String?,
    apiBaseUrl: String = BuildConfig.API_BASE_URL,
): String {
    val trimmed = path?.trim().orEmpty()
    if (trimmed.isEmpty()) return ""
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        return rewriteLoopbackHost(trimmed, apiBaseUrl)
    }
    if (trimmed.startsWith("//")) {
        return rewriteLoopbackHost("https:$trimmed", apiBaseUrl)
    }

    val origin = apiOrigin(apiBaseUrl)
    val relative = trimmed.removePrefix("./")
    return if (relative.startsWith("/")) {
        "$origin$relative"
    } else {
        "$origin/$relative"
    }
}

internal fun apiOrigin(apiBaseUrl: String): String {
    val trimmed = apiBaseUrl.trim().trimEnd('/')
    return trimmed.removeSuffix("/api").trimEnd('/')
}
