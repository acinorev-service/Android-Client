package ru.fet1sov.akinorev.feature.home

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    private fun page(start: Int, count: Int, total: Int) = ApiFixtures.catalogPayload(
        games = (start until start + count).map { index ->
            ApiFixtures.catalogGame(
                id = "game-$index",
                title = "Game $index",
                genreSlug = "arcade",
            )
        },
        total = total,
    )

    @Test
    fun firstPage_requestsSixMobileGames() = runTest(mainDispatcher) {
        var requestedLimit = 0
        var requestedOffset = -1
        var requestedGenre: String? = "unset"
        var requestedPlatform: String? = null

        api.onGetGames = { limit, offset, _, genre, platform ->
            requestedLimit = limit
            requestedOffset = offset
            requestedGenre = genre
            requestedPlatform = platform
            ApiFixtures.success(page(start = 0, count = 6, total = 12))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(6, requestedLimit)
        assertEquals(0, requestedOffset)
        assertEquals("mobile", requestedPlatform)
        assertNull(requestedGenre)
        assertEquals(6, state.games.size)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
    }

    @Test
    fun loadMore_appendsNextPage() = runTest(mainDispatcher) {
        val requestedOffsets = mutableListOf<Int>()
        api.onGetGames = { _, offset, _, _, _ ->
            requestedOffsets += offset
            ApiFixtures.success(page(start = offset, count = 6, total = 12))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(6, viewModel.uiState.value.games.size)

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(0, 6), requestedOffsets)
        assertEquals(12, state.games.size)
        assertFalse(state.hasMore)
    }

    @Test
    fun loadMore_ignoredWhenNoMorePages() = runTest(mainDispatcher) {
        var calls = 0
        api.onGetGames = { _, _, _, _, _ ->
            calls += 1
            ApiFixtures.success(page(start = 0, count = 6, total = 6))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(1, calls)
    }

    @Test
    fun toggleGenre_resetsPaginationAndPassesGenre() = runTest(mainDispatcher) {
        val genres = mutableListOf<String?>()
        val offsets = mutableListOf<Int>()

        api.onGetGames = { _, offset, _, genre, _ ->
            genres += genre
            offsets += offset
            ApiFixtures.success(page(start = 0, count = 6, total = 6))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleGenre("rpg")
        advanceUntilIdle()

        assertEquals(listOf(null, "rpg"), genres)
        assertEquals(listOf(0, 0), offsets)
        assertEquals("rpg", viewModel.uiState.value.selectedGenre)
    }

    @Test
    fun failure_marksCatalogUnavailable() = runTest(mainDispatcher) {
        api.onGetGames = { _, _, _, _, _ -> ApiFixtures.error(500, "server_error") }
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
        api.onGetGames = { _, _, _, _, _ ->
            ApiFixtures.success(ApiFixtures.catalogPayload(games = emptyList(), total = 0))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.games.isEmpty())
        assertTrue(state.isFilteredEmpty)
        assertFalse(state.isUnavailable)
        assertFalse(state.hasMore)
    }

    @Test
    fun clearFilters_restoresAllGames() = runTest(mainDispatcher) {
        var lastGenre: String? = "unset"
        api.onGetGames = { _, _, _, genre, _ ->
            lastGenre = genre
            ApiFixtures.success(page(start = 0, count = 6, total = 6))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.toggleGenre("rpg")
        advanceUntilIdle()
        assertEquals("rpg", lastGenre)

        viewModel.clearFilters()
        advanceUntilIdle()

        assertNull(lastGenre)
        assertEquals("", viewModel.uiState.value.selectedGenre)
    }

    @Test
    fun openSearch_keepsCatalogWithoutReloadWhenNoGenreSelected() = runTest(mainDispatcher) {
        var calls = 0
        api.onGetGames = { _, _, _, _, _ ->
            calls += 1
            ApiFixtures.success(page(start = 0, count = 6, total = 6))
        }
        api.onGetGenres = { ApiFixtures.success(ApiFixtures.catalogGenres()) }

        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(1, calls)

        viewModel.openSearch()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSearchOpen)
        assertEquals(1, calls)
    }
}
