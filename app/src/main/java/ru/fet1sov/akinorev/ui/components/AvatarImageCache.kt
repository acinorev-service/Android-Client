package ru.fet1sov.akinorev.ui.components

import android.util.LruCache

internal object AvatarImageCache {
    private val images = LruCache<String, AvatarImage>(MaxEntries)

    fun get(url: String): AvatarImage? = images.get(url)

    fun put(url: String, image: AvatarImage) {
        images.put(url, image)
    }

    fun clear() {
        images.evictAll()
    }

    private const val MaxEntries = 96
}

fun clearAvatarCache() {
    AvatarImageCache.clear()
}
