package ru.fet1sov.akinorev.feature.games.play

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
import ru.fet1sov.akinorev.data.catalog.DefaultCatalogRepository
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeGameUrlResolver
import ru.fet1sov.akinorev.testing.FakePlaySessionRepository
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class PlayViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val client = ApiClient(api, sessionManager)
    private val catalogRepository = DefaultCatalogRepository(api, client)
    private val playSessionRepository = FakePlaySessionRepository()
    private val gameUrlResolver = FakeGameUrlResolver()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel(gameId: String = "game-1", title: String = "Neon Runner") = PlayViewModel(
        gameId = gameId,
        fallbackTitle = title,
        catalogRepository = catalogRepository,
        playSessionRepository = playSessionRepository,
        authRepository = authRepository,
        gameUrlResolver = gameUrlResolver,
    )

    @Test
    fun load_exposesGameAndPlayers() = runTest(mainDispatcher) {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }
        api.onGetGamePlayers = { ApiFixtures.success(ApiFixtures.gamePlayers("game-1", 4)) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isUnavailable)
        assertEquals("Neon Runner", state.title)
        assertEquals(4, state.players)
        assertTrue(playSessionRepository.watched.contains("game-1"))
    }

    @Test
    fun authorizedSession_startsPlaySession() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }
        api.onGetGamePlayers = { ApiFixtures.success(ApiFixtures.gamePlayers("game-1", 0)) }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(playSessionRepository.started.contains("game-1"))
        assertTrue(viewModel.uiState.value.isSessionActive)
    }

    @Test
    fun guestSession_doesNotStartPlaySession() = runTest(mainDispatcher) {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }
        api.onGetGamePlayers = { ApiFixtures.success(ApiFixtures.gamePlayers("game-1", 2)) }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertFalse(playSessionRepository.started.contains("game-1"))
        assertFalse(viewModel.uiState.value.isSessionActive)
        assertEquals(2, viewModel.uiState.value.players)
    }

    @Test
    fun unavailableGame_isReported() = runTest(mainDispatcher) {
        api.onGetGame = { ApiFixtures.error(404, "game_not_found") }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isUnavailable)
        assertFalse(state.isLoading)
    }

    @Test
    fun ensureRoom_createsRoomAndInviteLink() = runTest(mainDispatcher) {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }
        api.onGetGamePlayers = { ApiFixtures.success(ApiFixtures.gamePlayers("game-1", 0)) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val room = viewModel.ensureRoom()

        assertEquals(6, room.length)
        assertEquals(room, viewModel.uiState.value.roomCode)
        assertTrue(viewModel.uiState.value.gameUrl.contains("room=$room"))
        assertTrue(viewModel.uiState.value.inviteLink.contains("/play/game-1?room=$room"))
        assertEquals(room, viewModel.ensureRoom())
    }

    @Test
    fun toggleFullscreen_switchesState() = runTest(mainDispatcher) {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleFullscreen()
        assertTrue(viewModel.uiState.value.isFullscreen)

        viewModel.exitFullscreen()
        assertFalse(viewModel.uiState.value.isFullscreen)
    }
}

