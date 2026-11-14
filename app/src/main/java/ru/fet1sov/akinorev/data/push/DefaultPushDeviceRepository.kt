package ru.fet1sov.akinorev.data.push

import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.RegisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.UnregisterPushDeviceRequest

class DefaultPushDeviceRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : PushDeviceRepository {
    override suspend fun register(
        token: String,
        appVersion: String,
        locale: String,
    ): AuthResult<Unit> = when (val result = client.execute {
        api.registerPushDevice(
            RegisterPushDeviceRequest(
                token = token,
                appVersion = appVersion,
                locale = locale,
            ),
        )
    }) {
        is AuthResult.Success -> AuthResult.Success(Unit)
        is AuthResult.Failure -> result
    }

    override suspend fun unregister(token: String): AuthResult<Unit> =
        when (val result = client.execute {
            api.unregisterPushDevice(UnregisterPushDeviceRequest(token = token))
        }) {
            is AuthResult.Success -> AuthResult.Success(Unit)
            is AuthResult.Failure -> result
        }
}
