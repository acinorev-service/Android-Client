package ru.fet1sov.akinorev.data.notifications

import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.data.remote.dto.NotificationDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationListDto
import ru.fet1sov.akinorev.model.NotificationActor
import ru.fet1sov.akinorev.model.NotificationPage
import ru.fet1sov.akinorev.model.NotificationType
import ru.fet1sov.akinorev.model.AppNotification
import java.time.Instant
import java.time.OffsetDateTime

class DefaultNotificationsRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : NotificationsRepository {
    override suspend fun fetchPage(limit: Int, offset: Int): AuthResult<NotificationPage> =
        when (val result = client.execute { api.getNotifications(limit = limit, offset = offset) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }

    override suspend fun markRead(ids: List<String>): AuthResult<Int> =
        when (val result = client.execute {
            api.markNotificationsRead(MarkNotificationsReadRequest(ids = ids))
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.updated)
            is AuthResult.Failure -> result
        }

    override suspend fun markAllRead(): AuthResult<Int> =
        when (val result = client.execute {
            api.markNotificationsRead(MarkNotificationsReadRequest(ids = null))
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.updated)
            is AuthResult.Failure -> result
        }
}

internal fun NotificationListDto.toDomain(): NotificationPage = NotificationPage(
    items = notifications.mapNotNull { it.toDomain() },
    hasMore = hasMore,
    unread = unread,
)

internal fun NotificationDto.toDomain(): AppNotification? {
    val id = id.takeIf { it.isNotBlank() } ?: return null
    val type = NotificationType.fromWire(type) ?: return null

    return AppNotification(
        id = id,
        type = type,
        actorId = actorId,
        actor = actor?.toDomain(),
        postId = postId?.takeIf { it.isNotBlank() },
        title = title.orEmpty(),
        body = body.orEmpty(),
        isRead = read,
        createdAtMillis = parseTimestamp(createdAt),
    )
}

private fun ru.fet1sov.akinorev.data.remote.dto.NotificationActorDto.toDomain(): NotificationActor =
    NotificationActor(
        id = id,
        username = username,
        avatar = avatar.orEmpty(),
        isSubscribed = isSubscribed,
        nameColor = nameColor.orEmpty(),
        nameAnimation = nameAnimation.orEmpty(),
    )

internal fun parseTimestamp(value: String?): Long {
    if (value.isNullOrBlank()) return System.currentTimeMillis()
    return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: System.currentTimeMillis()
}
