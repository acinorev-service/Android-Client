package ru.fet1sov.akinorev.data.push

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.PushDeviceResponse
import ru.fet1sov.akinorev.data.remote.dto.RegisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.UnregisterPushDeviceRequest
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

class DefaultPushDeviceRepositoryTest {
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultPushDeviceRepository(api, ApiClient(api, sessionManager))

    @Before
    fun authorizeSession() = runBlocking {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
    }

    @Test
    fun register_sendsTokenAndMetadata() = runTest {
        var captured: RegisterPushDeviceRequest? = null
        api.onRegisterPushDevice = { request ->
            captured = request
            ApiFixtures.success(PushDeviceResponse(ok = true))
        }

        val result = repository.register("fcm-token", appVersion = "1.0", locale = "ru")

        assertTrue(result is AuthResult.Success)
        assertEquals("fcm-token", captured?.token)
        assertEquals("android", captured?.platform)
        assertEquals("1.0", captured?.appVersion)
        assertEquals("ru", captured?.locale)
    }

    @Test
    fun register_mapsServerError() = runTest {
        api.onRegisterPushDevice = { ApiFixtures.error(401, "unauthorized") }

        val result = repository.register("fcm-token", "1.0", "ru")

        val error = (result as AuthResult.Failure).error
        assertTrue(error is AuthError.Server)
        assertEquals(401, (error as AuthError.Server).status)
    }

    @Test
    fun unregister_sendsToken() = runTest {
        var captured: UnregisterPushDeviceRequest? = null
        api.onUnregisterPushDevice = { request ->
            captured = request
            ApiFixtures.success(PushDeviceResponse(ok = true))
        }

        val result = repository.unregister("fcm-token")

        assertTrue(result is AuthResult.Success)
        assertEquals("fcm-token", captured?.token)
    }
}
