package ru.fet1sov.akinorev.feature.search

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.search.DefaultSearchRepository
import ru.fet1sov.akinorev.data.remote.dto.SearchCategoryDto
import ru.fet1sov.akinorev.data.remote.dto.SearchPayloadDto
import ru.fet1sov.akinorev.model.SearchCategory
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val repository = DefaultSearchRepository(
        api = api,
        client = ApiClient(api, SessionManager(InMemorySessionStorage())),
    )

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel() = SearchViewModel(repository)

    @Test
    fun search_exposesCategories() = runTest(mainDispatcher) {
        api.onSearch = { query, _ -> ApiFixtures.success(ApiFixtures.searchPayload(query = query)) }

        val viewModel = viewModel()
        viewModel.onQueryChange("neon")
        advanceTimeBy(DebounceMillis + 50)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(3, state.groups.size)
        assertEquals(
            listOf(SearchCategory.Games, SearchCategory.Developers, SearchCategory.Users),
            state.groups.map { it.category },
        )
        assertEquals(3, state.total)
        assertFalse(state.isSearching)
        assertFalse(state.hasError)
    }

    @Test
    fun blankQuery_clearsResults() = runTest(mainDispatcher) {
        api.onSearch = { query, _ -> ApiFixtures.success(ApiFixtures.searchPayload(query = query)) }

        val viewModel = viewModel()
        viewModel.onQueryChange("neon")
        advanceTimeBy(DebounceMillis + 50)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.groups.size)

        viewModel.onQueryChange("")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.groups.isEmpty())
        assertFalse(state.isSearching)
        assertFalse(state.isEmpty)
    }

    @Test
    fun failure_setsError() = runTest(mainDispatcher) {
        api.onSearch = { _, _ -> ApiFixtures.error(500, "server_error") }

        val viewModel = viewModel()
        viewModel.onQueryChange("neon")
        advanceTimeBy(DebounceMillis + 50)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasError)
        assertTrue(state.groups.isEmpty())
    }

    @Test
    fun loadMore_increasesLimit() = runTest(mainDispatcher) {
        var lastLimit = 0
        api.onSearch = { query, limit ->
            lastLimit = limit
            ApiFixtures.success(
                SearchPayloadDto(
                    query = query,
                    categories = listOf(SearchCategoryDto("games", listOf(ApiFixtures.searchGameItem()))),
                    total = 25,
                    limit = limit,
                ),
            )
        }

        val viewModel = viewModel()
        viewModel.onQueryChange("neon")
        advanceTimeBy(DebounceMillis + 50)
        advanceUntilIdle()
        assertEquals(20, lastLimit)
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.loadMore()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
        assertEquals(2, viewModel.uiState.value.groups.size)
    }

    @Test
    fun clear_resetsState() = runTest(mainDispatcher) {
        api.onSearch = { query, _ -> ApiFixtures.success(ApiFixtures.searchPayload(query = query)) }

        val viewModel = viewModel()
        viewModel.onQueryChange("neon")
        advanceTimeBy(DebounceMillis + 50)
        advanceUntilIdle()

        viewModel.clear()

        assertEquals(SearchUiState(), viewModel.uiState.value)
    }

    private companion object {
        const val DebounceMillis = 300L
    }
}
