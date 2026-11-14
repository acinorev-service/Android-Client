package ru.fet1sov.akinorev.data.notifications

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.UpdateNotificationPreferencesRequest
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

class DefaultNotificationPreferencesRepositoryTest {
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultNotificationPreferencesRepository(api, ApiClient(api, sessionManager))

    @Before
    fun authorizeSession() = runBlocking {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
    }

    @Test
    fun fetchPreferences_mapsPayload() = runTest {
        api.onGetNotificationPreferences = {
            ApiFixtures.success(ApiFixtures.notificationPreferences(follow = false, like = false))
        }

        val result = repository.fetchPreferences()

        val preferences = (result as AuthResult.Success).value
        assertFalse(preferences[NotificationPreferenceKey.Follow])
        assertFalse(preferences[NotificationPreferenceKey.Like])
        assertTrue(preferences[NotificationPreferenceKey.Post])
        assertTrue(preferences[NotificationPreferenceKey.Block])
        assertTrue(preferences[NotificationPreferenceKey.Broadcast])
    }

    @Test
    fun updatePreference_sendsSingleField() = runTest {
        var captured: UpdateNotificationPreferencesRequest? = null
        api.onUpdateNotificationPreferences = { request ->
            captured = request
            ApiFixtures.success(ApiFixtures.notificationPreferences(broadcast = false))
        }

        val result = repository.updatePreference(NotificationPreferenceKey.Broadcast, false)

        assertTrue(result is AuthResult.Success)
        val request = captured
        assertEquals(false, request?.broadcast)
        assertNull(request?.follow)
        assertNull(request?.block)
        assertNull(request?.post)
        assertNull(request?.like)
        assertFalse((result as AuthResult.Success).value[NotificationPreferenceKey.Broadcast])
    }

    @Test
    fun updatePreference_mapsServerError() = runTest {
        api.onUpdateNotificationPreferences = { ApiFixtures.error(500, "server_error") }

        val result = repository.updatePreference(NotificationPreferenceKey.Post, false)

        val error = (result as AuthResult.Failure).error
        assertTrue(error is AuthError.Server)
        assertEquals("server_error", (error as AuthError.Server).code)
    }
}
