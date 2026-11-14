package ru.fet1sov.akinorev.feature.home

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
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.catalog.DefaultCatalogRepository
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val client = ApiClient(api, SessionManager(InMemorySessionStorage()))
    private val catalogRepository = DefaultCatalogRepository(api, client)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel() = HomeViewModel(catalogRepository)

    @Test
    fun load_exposesGamesAndGenres() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner", genreSlug = "arcade"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon", genreSlug = "rpg"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.games.size)
        assertEquals(listOf("arcade", "rpg"), state.genres)
        assertFalse(state.isLoading)
        assertFalse(state.isUnavailable)
    }

    @Test
    fun query_filtersByTitleAndAuthor() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon", authorUsername = "another"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("neon")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.games.size)
        assertEquals("Neon Runner", viewModel.uiState.value.games.single().title)
    }

    @Test
    fun toggleGenre_selectsAndClearsGenre() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner", genreSlug = "arcade"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon", genreSlug = "rpg"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleGenre("rpg")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.games.size)

        viewModel.toggleGenre("rpg")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.games.size)
    }

    @Test
    fun failure_marksCatalogUnavailable() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ -> ApiFixtures.error(500, "server_error") }
        api.onGetGenres = { ApiFixtures.error(500, "server_error") }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isUnavailable)
        assertTrue(state.games.isEmpty())
        assertEquals(FallbackGenres, state.genres)
    }

    @Test
    fun emptyResult_isReportedAsFilteredEmpty() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ -> ApiFixtures.success(ApiFixtures.catalogPayload(games = emptyList())) }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertTrue(state.isFilteredEmpty)
        assertFalse(state.isUnavailable)
    }

    @Test
    fun clearFilters_restoresFullList() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner", genreSlug = "arcade"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon", genreSlug = "rpg"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("unknown")
        viewModel.toggleGenre("rpg")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.games.isEmpty())

        viewModel.clearFilters()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.games.size)
        assertEquals("", viewModel.uiState.value.query)
        assertEquals("", viewModel.uiState.value.selectedGenre)
    }

    @Test
    fun openSearch_clearsGenreAndShowsAllGames() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner", genreSlug = "arcade"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon", genreSlug = "rpg"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleGenre("rpg")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.games.size)

        viewModel.openSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSearchOpen)
        assertEquals("", state.selectedGenre)
        assertEquals(2, state.games.size)
    }

    @Test
    fun closeSearch_resetsQueryAndReturnsToCatalog() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(
                        ApiFixtures.catalogGame(id = "game-1", title = "Neon Runner"),
                        ApiFixtures.catalogGame(id = "game-2", title = "Dungeon"),
                    ),
                ),
            )
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.openSearch()
        viewModel.onQueryChange("neon")
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.games.size)

        viewModel.closeSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSearchOpen)
        assertEquals("", state.query)
        assertEquals(2, state.games.size)
    }
}
