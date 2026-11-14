package ru.fet1sov.akinorev.model

enum class NotificationPreferenceKey {
    Follow,
    Block,
    Post,
    Like,
    Broadcast,
}

data class NotificationPreferences(
    val follow: Boolean = true,
    val block: Boolean = true,
    val post: Boolean = true,
    val like: Boolean = true,
    val broadcast: Boolean = true,
) {
    operator fun get(key: NotificationPreferenceKey): Boolean = when (key) {
        NotificationPreferenceKey.Follow -> follow
        NotificationPreferenceKey.Block -> block
        NotificationPreferenceKey.Post -> post
        NotificationPreferenceKey.Like -> like
        NotificationPreferenceKey.Broadcast -> broadcast
    }

    fun updated(key: NotificationPreferenceKey, enabled: Boolean): NotificationPreferences = when (key) {
        NotificationPreferenceKey.Follow -> copy(follow = enabled)
        NotificationPreferenceKey.Block -> copy(block = enabled)
        NotificationPreferenceKey.Post -> copy(post = enabled)
        NotificationPreferenceKey.Like -> copy(like = enabled)
        NotificationPreferenceKey.Broadcast -> copy(broadcast = enabled)
    }

    companion object {
        val Default = NotificationPreferences()
    }
}
