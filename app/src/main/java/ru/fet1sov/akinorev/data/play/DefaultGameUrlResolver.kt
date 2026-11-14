package ru.fet1sov.akinorev.data.play

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.ui.components.apiOrigin

interface GameUrlResolver {
    suspend fun resolve(gameId: String, room: String?): String
}

/**
 * Telegram-подобный редирект сервера может указывать на localhost (CDN_PUBLIC_BASE
 * на сервере). На телефоне localhost — это сам телефон, а Chromium блокирует
 * переходы из частной сети в local (Private Network Access). Поэтому адрес
 * сборки вычисляем заранее по Location и подменяем хост на CDN из BuildConfig.
 */
class DefaultGameUrlResolver(
    private val client: OkHttpClient,
    private val apiBaseUrl: String = BuildConfig.API_BASE_URL,
    private val cdnBaseUrl: String = BuildConfig.CDN_BASE_URL,
) : GameUrlResolver {
    override suspend fun resolve(gameId: String, room: String?): String = withContext(Dispatchers.IO) {
        val fallback = fallbackUrl(gameId, room)
        if (gameId.isBlank()) return@withContext fallback

        val landing = "${apiOrigin(apiBaseUrl)}/game/$gameId/"
        val location = readLocation(landing) ?: return@withContext fallback
        val playable = normalizeCdnUrl(location) ?: return@withContext fallback

        appendRoom(playable, room)
    }

    private fun readLocation(url: String): String? = runCatching {
        val request = Request.Builder().url(url).head().build()
        client.newCall(request).execute().use { response ->
            val location = response.header("Location") ?: return null
            response.request.url.resolve(location)?.toString()
        }
    }.getOrNull()

    private fun normalizeCdnUrl(raw: String): String? {
        val parsed = runCatching { raw.toHttpUrlOrNull() }.getOrNull() ?: return null
        val cdn = runCatching { cdnBaseUrl.toHttpUrlOrNull() }.getOrNull()
        val useCdn = cdn != null && isLoopbackHost(parsed.host)

        val builder = parsed.newBuilder()
        if (useCdn) {
            builder.scheme(cdn.scheme)
            builder.host(cdn.host)
            builder.port(cdn.port)
        }

        if (parsed.encodedPath.endsWith(BootPath)) {
            builder.encodedPath(parsed.encodedPath.removeSuffix(BootPath) + "/index.html")
            val gameId = parsed.queryParameter("game")
            builder.removeAllQueryParameters("game")
            if (!gameId.isNullOrBlank()) {
                builder.addQueryParameter("mt_game", gameId)
            }
        }

        return builder.build().toString()
    }

    private fun appendRoom(raw: String, room: String?): String {
        if (room.isNullOrBlank()) return raw

        val parsed = runCatching { raw.toHttpUrlOrNull() }.getOrNull() ?: return raw
        if (!parsed.queryParameter("room").isNullOrBlank()) return raw

        return parsed.newBuilder()
            .addQueryParameter("room", room)
            .addQueryParameter("online", "1")
            .build()
            .toString()
    }

    private fun fallbackUrl(gameId: String, room: String?): String {
        val base = "${apiOrigin(apiBaseUrl)}/game/$gameId"
        if (room.isNullOrBlank()) return base
        return "$base?room=$room&online=1"
    }

    private fun isLoopbackHost(host: String): Boolean {
        val normalized = host.lowercase().removePrefix("[").removeSuffix("]")
        return normalized == "localhost" ||
            normalized == "127.0.0.1" ||
            normalized == "0.0.0.0" ||
            normalized == "::1"
    }

    private companion object {
        const val BootPath = "/__acinorev/boot.html"
    }
}

