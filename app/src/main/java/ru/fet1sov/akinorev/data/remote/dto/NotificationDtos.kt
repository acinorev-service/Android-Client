package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationPreferencesDto(
    val follow: Boolean? = null,
    val block: Boolean? = null,
    val post: Boolean? = null,
    val like: Boolean? = null,
    val broadcast: Boolean? = null,
)

@Serializable
data class NotificationPreferencesPayloadDto(
    val preferences: NotificationPreferencesDto? = null,
)

@Serializable
data class UpdateNotificationPreferencesRequest(
    val follow: Boolean? = null,
    val block: Boolean? = null,
    val post: Boolean? = null,
    val like: Boolean? = null,
    val broadcast: Boolean? = null,
)
