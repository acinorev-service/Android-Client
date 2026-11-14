package ru.fet1sov.akinorev.feature.games

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.library.DefaultLibraryRepository
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val libraryRepository = DefaultLibraryRepository(api, ApiClient(api, sessionManager))

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private suspend fun authorize() {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }
    }

    private fun viewModel() = GamesViewModel(
        authRepository = authRepository,
        libraryRepository = libraryRepository,
    )

    @Test
    fun authorizedSession_loadsLibrary() = runTest(mainDispatcher) {
        authorize()
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(
                        ApiFixtures.libraryGame(
                            id = "game-1",
                            title = "Neon Runner",
                            genreSlug = "arcade",
                            secondsPlayed = 5_400,
                        ),
                    ),
                    secondsPlayed = 5_400,
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertEquals(1, state.games.size)
        assertEquals("Neon Runner", state.games.single().title)
        assertEquals("arcade", state.games.single().genreSlug)
        assertEquals(5_400L, state.totalSeconds)
        assertFalse(state.isUnavailable)
    }

    @Test
    fun totalSeconds_fallsBackToSumWhenServerTotalsMissing() = runTest(mainDispatcher) {
        authorize()
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(
                        ApiFixtures.libraryGame(id = "a", secondsPlayed = 1_200),
                        ApiFixtures.libraryGame(id = "b", secondsPlayed = 1_800),
                    ),
                    secondsPlayed = 0,
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(3_000L, viewModel.uiState.value.totalSeconds)
    }

    @Test
    fun failureWithoutGames_showsUnavailable() = runTest(mainDispatcher) {
        authorize()
        api.onGetLibraryGames = { ApiFixtures.error(500, "server_error") }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isUnavailable)
        assertTrue(state.games.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun refreshFailure_keepsLoadedGames() = runTest(mainDispatcher) {
        authorize()
        var calls = 0
        api.onGetLibraryGames = {
            calls += 1
            if (calls == 1) {
                ApiFixtures.success(
                    ApiFixtures.libraryPayload(
                        games = listOf(ApiFixtures.libraryGame(id = "a", secondsPlayed = 600)),
                    ),
                )
            } else {
                ApiFixtures.error(500, "server_error")
            }
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.games.size)
        assertFalse(state.isUnavailable)
        assertEquals(2, calls)
    }

    @Test
    fun refresh_reloadsLibrary() = runTest(mainDispatcher) {
        authorize()
        var calls = 0
        api.onGetLibraryGames = {
            calls += 1
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(ApiFixtures.libraryGame(id = "a", secondsPlayed = calls * 600L)),
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(1, calls)

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(2, calls)
        assertEquals(1_200L, viewModel.uiState.value.totalSeconds)
    }

    @Test
    fun guestSession_resetsLibrary() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isAuthPending)
        assertTrue(state.games.isEmpty())
        assertEquals(0L, state.totalSeconds)
    }
}
