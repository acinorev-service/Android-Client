package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationActorDto(
    val id: String = "",
    val username: String = "",
    val avatar: String? = null,
    @SerialName("is_subscribed") val isSubscribed: Boolean = false,
    @SerialName("name_color") val nameColor: String? = null,
    @SerialName("name_animation") val nameAnimation: String? = null,
)

@Serializable
data class NotificationDto(
    val id: String = "",
    val type: String = "",
    @SerialName("actor_id") val actorId: String = "",
    val actor: NotificationActorDto? = null,
    @SerialName("post_id") val postId: String? = null,
    val title: Map<String, String>? = null,
    val body: Map<String, String>? = null,
    val read: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class NotificationListDto(
    val notifications: List<NotificationDto> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    val unread: Int = 0,
)

@Serializable
data class MarkNotificationsReadRequest(
    val ids: List<String>? = null,
)

@Serializable
data class MarkNotificationsReadDto(
    val updated: Int = 0,
)
