package ru.fet1sov.akinorev.feature.menu

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultAuthRepository(api, sessionManager)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    @Test
    fun uiState_isPendingUntilSessionRestored() = runTest(mainDispatcher) {
        val viewModel = MenuViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        assertTrue(viewModel.uiState.value.isAuthPending)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAuthPending)
        assertNull(viewModel.uiState.value.profile)
    }

    @Test
    fun uiState_mapsAuthorizedAccount() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }

        val viewModel = MenuViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val profile = viewModel.uiState.value.profile
        assertNotNull(profile)
        assertEquals("fet1sov", profile?.username)
        assertEquals(ApiFixtures.user.wallet, profile?.wallet)
        assertFalse(profile?.isSubscribed ?: true)
    }

    @Test
    fun uiState_formatsSubscriptionExpiry() = runTest(mainDispatcher) {
        val subscriber = ApiFixtures.user.copy(
            isSubscribed = true,
            subscriptionExpiresAt = "2026-11-14T10:30:00Z",
        )
        sessionManager.save(subscriber.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = subscriber)) }

        val viewModel = MenuViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profile?.isSubscribed == true)
        assertEquals("14-11-2026", viewModel.uiState.value.subscriptionExpiry)
    }

    @Test
    fun logout_clearsSession() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }

        val viewModel = MenuViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.profile)
        assertNull(sessionManager.accessToken())
        assertFalse(viewModel.uiState.value.isLoggingOut)
    }

    @Test
    fun uiState_exposesMenuEntries() = runTest(mainDispatcher) {
        val viewModel = MenuViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        assertEquals(MenuEntry.entries, viewModel.uiState.value.entries)
    }
}
