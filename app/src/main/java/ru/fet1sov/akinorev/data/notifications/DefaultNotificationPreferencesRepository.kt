package ru.fet1sov.akinorev.data.notifications

import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.NotificationPreferencesDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationPreferencesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.UpdateNotificationPreferencesRequest
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.model.NotificationPreferences

class DefaultNotificationPreferencesRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : NotificationPreferencesRepository {
    override suspend fun fetchPreferences(): AuthResult<NotificationPreferences> =
        when (val result = client.execute { api.getNotificationPreferences() }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }

    override suspend fun updatePreference(
        key: NotificationPreferenceKey,
        enabled: Boolean,
    ): AuthResult<NotificationPreferences> =
        when (val result = client.execute {
            api.updateNotificationPreferences(requestFor(key, enabled))
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }

    private fun requestFor(
        key: NotificationPreferenceKey,
        enabled: Boolean,
    ): UpdateNotificationPreferencesRequest = when (key) {
        NotificationPreferenceKey.Follow -> UpdateNotificationPreferencesRequest(follow = enabled)
        NotificationPreferenceKey.Block -> UpdateNotificationPreferencesRequest(block = enabled)
        NotificationPreferenceKey.Post -> UpdateNotificationPreferencesRequest(post = enabled)
        NotificationPreferenceKey.Like -> UpdateNotificationPreferencesRequest(like = enabled)
        NotificationPreferenceKey.Broadcast -> UpdateNotificationPreferencesRequest(broadcast = enabled)
    }
}

internal fun NotificationPreferencesPayloadDto?.toDomain(): NotificationPreferences {
    val dto = this?.preferences ?: NotificationPreferencesDto()
    return NotificationPreferences(
        follow = dto.follow ?: true,
        block = dto.block ?: true,
        post = dto.post ?: true,
        like = dto.like ?: true,
        broadcast = dto.broadcast ?: true,
    )
}
