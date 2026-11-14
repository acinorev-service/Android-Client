package ru.fet1sov.akinorev.model

import kotlinx.serialization.Serializable

@Serializable
data object NotificationsDestination {
    val path: String = "/notifications"
}

@Serializable
data object CommunityDestination {
    val path: String = "/community"
}

@Serializable
data object EmojisDestination {
    val path: String = "/emojis"
}

@Serializable
data object EmojiCreateDestination {
    val path: String = "/emojis/create"
}

@Serializable
data object SettingsDestination {
    val path: String = "/settings"
}

@Serializable
data class ProfileDestination(
    val username: String,
) {
    val path: String
        get() = "/profile/$username"
}
