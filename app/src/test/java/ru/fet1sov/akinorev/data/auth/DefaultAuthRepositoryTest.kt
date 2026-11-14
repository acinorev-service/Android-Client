package ru.fet1sov.akinorev.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage
import java.io.IOException

class DefaultAuthRepositoryTest {
    private val api = FakeAcinorevApi()
    private val storage = InMemorySessionStorage()
    private val sessionManager = SessionManager(storage)
    private val repository = DefaultAuthRepository(api, sessionManager)

    @Test
    fun login_persistsTokensAndUser() = runTest {
        api.onLogin = {
            ApiFixtures.success(AuthPayloadDto(user = ApiFixtures.user, tokens = ApiFixtures.tokens))
        }

        val result = repository.login("fet1sov", "secret1")

        assertTrue(result is AuthResult.Success)
        assertEquals("fet1sov", (result as AuthResult.Success).value.username)
        assertEquals("access-1", sessionManager.accessToken())
        assertEquals("refresh-1", sessionManager.refreshToken())
        assertEquals("fet1sov", storage.session?.user?.username)
        assertTrue(sessionManager.state.value is SessionState.Authorized)
    }

    @Test
    fun login_mapsServerErrorCode() = runTest {
        api.onLogin = { ApiFixtures.error(401, "invalid_credentials") }

        val result = repository.login("fet1sov", "wrong")

        val error = (result as AuthResult.Failure).error
        assertTrue(error is AuthError.Server)
        assertEquals("invalid_credentials", (error as AuthError.Server).code)
        assertNull(sessionManager.accessToken())
    }

    @Test
    fun login_mapsNetworkFailure() = runTest {
        api.onLogin = { throw IOException("offline") }

        val result = repository.login("fet1sov", "secret1")

        assertEquals(AuthError.Network, (result as AuthResult.Failure).error)
    }

    @Test
    fun fetchProfile_refreshesExpiredTokenAndRetries() = runTest {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())

        var attempts = 0
        api.onGetProfile = {
            attempts += 1
            if (attempts == 1) {
                ApiFixtures.error(401, "unauthorized")
            } else {
                ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user.copy(bio = "hello")))
            }
        }
        api.onRefresh = {
            ApiFixtures.success(
                ApiFixtures.tokens.copy(accessToken = "access-2", refreshToken = "refresh-2"),
            )
        }

        val result = repository.fetchProfile()

        assertTrue(result is AuthResult.Success)
        assertEquals(2, attempts)
        assertEquals(1, api.refreshCalls)
        assertEquals("access-2", sessionManager.accessToken())
        assertEquals("hello", (result as AuthResult.Success).value.bio)
    }

    @Test
    fun fetchProfile_withRejectedRefresh_clearsSession() = runTest {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.error(401, "unauthorized") }
        api.onRefresh = { ApiFixtures.error(401, "invalid_refresh") }

        val result = repository.fetchProfile()

        assertTrue(result is AuthResult.Failure)
        assertEquals(1, api.refreshCalls)
        assertNull(sessionManager.accessToken())
        assertEquals(SessionState.Guest, sessionManager.state.value)
    }

    @Test
    fun logout_clearsStoredSession() = runTest {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())

        repository.logout()

        assertNull(storage.session)
        assertEquals(SessionState.Guest, sessionManager.state.value)
    }
}
