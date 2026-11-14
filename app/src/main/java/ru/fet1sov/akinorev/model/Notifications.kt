package ru.fet1sov.akinorev.model

enum class NotificationType {
    Follow,
    Block,
    Post,
    Like,
    Broadcast,
    ;

    companion object {
        fun fromWire(value: String): NotificationType? = when (value) {
            "follow" -> Follow
            "block" -> Block
            "post" -> Post
            "like" -> Like
            "broadcast" -> Broadcast
            else -> null
        }
    }
}

data class NotificationActor(
    val id: String,
    val username: String,
    val avatar: String,
    val isSubscribed: Boolean,
    val nameColor: String,
    val nameAnimation: String,
)

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val actorId: String,
    val actor: NotificationActor?,
    val postId: String?,
    val title: Map<String, String>,
    val body: Map<String, String>,
    val isRead: Boolean,
    val createdAtMillis: Long,
)

data class NotificationPage(
    val items: List<AppNotification>,
    val hasMore: Boolean,
    val unread: Int,
)

fun AppNotification.localizedTitle(locale: String): String = localizedText(title, locale)

fun AppNotification.localizedBody(locale: String): String = localizedText(body, locale)

internal fun localizedText(map: Map<String, String>, locale: String): String {
    if (map.isEmpty()) return ""
    val current = locale.lowercase().take(2)
    map[current]?.takeIf { it.isNotBlank() }?.let { return it }
    map["ru"]?.takeIf { it.isNotBlank() }?.let { return it }
    map["en"]?.takeIf { it.isNotBlank() }?.let { return it }
    return map.values.firstOrNull { it.isNotBlank() }.orEmpty()
}
