package ru.fet1sov.akinorev.data.notifications

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.NotificationPage

interface NotificationsRepository {
    suspend fun fetchPage(limit: Int, offset: Int): AuthResult<NotificationPage>

    suspend fun markRead(ids: List<String>): AuthResult<Int>

    suspend fun markAllRead(): AuthResult<Int>
}
