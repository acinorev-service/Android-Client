package ru.fet1sov.akinorev.feature.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultAuthRepository(api, sessionManager)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    @Test
    fun guestSession_clearsUserScopedCache() = runTest(mainDispatcher) {
        var cacheClears = 0

        val viewModel = SessionViewModel(repository) { cacheClears += 1 }
        advanceUntilIdle()

        assertTrue(viewModel.state.value is SessionState.Guest)
        assertTrue(cacheClears > 0)
    }

    @Test
    fun authorizedSession_keepsUserScopedCache() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }
        var cacheClears = 0

        val viewModel = SessionViewModel(repository) { cacheClears += 1 }
        advanceUntilIdle()

        assertTrue(viewModel.state.value is SessionState.Authorized)
        assertEquals(0, cacheClears)
    }

    @Test
    fun logoutClearsUserScopedCache() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }
        var cacheClears = 0

        val viewModel = SessionViewModel(repository) { cacheClears += 1 }
        advanceUntilIdle()
        assertEquals(0, cacheClears)

        repository.logout()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is SessionState.Guest)
        assertTrue(cacheClears > 0)
    }
}
