package ru.fet1sov.akinorev.ui.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

internal object RemoteImageLoader {
    private val client = OkHttpClient()

    suspend fun load(url: String): AvatarImage? {
        AvatarImageCache.get(url)?.let { return it }

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val bytes = response.body?.bytes() ?: return@use null
                    decodeAvatarImage(bytes)?.also { AvatarImageCache.put(url, it) }
                }
            }.getOrNull()
        }
    }
}
