package ru.fet1sov.akinorev.data.notifications

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.model.NotificationPreferences

interface NotificationPreferencesRepository {
    suspend fun fetchPreferences(): AuthResult<NotificationPreferences>

    suspend fun updatePreference(
        key: NotificationPreferenceKey,
        enabled: Boolean,
    ): AuthResult<NotificationPreferences>
}
