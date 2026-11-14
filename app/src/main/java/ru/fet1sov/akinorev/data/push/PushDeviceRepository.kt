package ru.fet1sov.akinorev.data.push

import ru.fet1sov.akinorev.data.remote.AuthResult

interface PushDeviceRepository {
    suspend fun register(token: String, appVersion: String, locale: String): AuthResult<Unit>

    suspend fun unregister(token: String): AuthResult<Unit>
}
