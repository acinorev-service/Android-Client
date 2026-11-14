package ru.fet1sov.akinorev.feature.emojis

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeEmojisRepository
import ru.fet1sov.akinorev.testing.InMemorySessionStorage
import ru.fet1sov.akinorev.testing.fakeEmoji
import ru.fet1sov.akinorev.testing.fakeEmojiPage

@OptIn(ExperimentalCoroutinesApi::class)
class EmojisViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val emojisRepository = FakeEmojisRepository()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private suspend fun authorize(subscribed: Boolean = false) {
        val user = ApiFixtures.user.copy(isSubscribed = subscribed)
        sessionManager.save(user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = user)) }
    }

    private fun viewModel() = EmojisViewModel(
        authRepository = authRepository,
        emojisRepository = emojisRepository,
    )

    @Test
    fun authorizedSession_loadsFirstPage() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.pages[1] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a", name = "pepe")),
            page = 1,
            totalPages = 1,
        )

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertEquals(1, state.items.size)
        assertEquals("pepe", state.items.single().name)
        assertEquals(":pepe:", ":${state.items.single().name}:")
        assertEquals(EmojisViewModel.PageSize, emojisRepository.defaultPageSize)
        assertFalse(state.hasMore)
    }

    @Test
    fun subscriptionFlag_isExposedFromSession() = runTest(mainDispatcher) {
        authorize(subscribed = true)
        emojisRepository.pages[1] = fakeEmojiPage()

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSubscribed)
    }

    @Test
    fun loadMore_appendsItemsAndStopsOnLastPage() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.pages[1] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a")),
            page = 1,
            totalPages = 2,
            total = 2,
        )
        emojisRepository.pages[2] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "b", name = "doge")),
            page = 2,
            totalPages = 2,
            total = 2,
        )

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.items.size)
        assertEquals(listOf("a", "b"), state.items.map { it.id })
        assertFalse(state.hasMore)
        assertFalse(state.isLoadingMore)

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(listOf(1, 2), emojisRepository.requestedPages)
    }

    @Test
    fun loadMore_skipsDuplicatesFromPreviousPages() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.pages[1] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a")),
            page = 1,
            totalPages = 2,
            total = 2,
        )
        emojisRepository.pages[2] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a"), fakeEmoji(id = "b")),
            page = 2,
            totalPages = 2,
            total = 2,
        )

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf("a", "b"), viewModel.uiState.value.items.map { it.id })
    }

    @Test
    fun refreshFailureWithEmptyList_showsUnavailable() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.fetchResult = AuthResult.Failure(AuthError.Server("server_error", 500))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isUnavailable)
        assertFalse(state.isLoading)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun delete_removesEmojiAndEmitsEvent() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.pages[1] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a", mine = true)),
            page = 1,
            totalPages = 1,
        )
        emojisRepository.deleteResult = AuthResult.Success("a")

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val events = mutableListOf<EmojisEvent>()
        val collector = launch { viewModel.events.collect { events += it } }
        advanceUntilIdle()

        viewModel.delete("a")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(0, state.total)
        assertEquals(listOf("a"), emojisRepository.deletedIds)
        assertEquals(listOf<EmojisEvent>(EmojisEvent.Deleted), events)

        collector.cancel()
    }

    @Test
    fun deleteFailure_keepsEmojiAndEmitsFailure() = runTest(mainDispatcher) {
        authorize()
        emojisRepository.pages[1] = fakeEmojiPage(
            items = listOf(fakeEmoji(id = "a", mine = true)),
            page = 1,
            totalPages = 1,
        )
        emojisRepository.deleteResult = AuthResult.Failure(AuthError.Server("server_error", 500))

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val events = mutableListOf<EmojisEvent>()
        val collector = launch { viewModel.events.collect { events += it } }
        advanceUntilIdle()

        viewModel.delete("a")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(listOf<EmojisEvent>(EmojisEvent.DeleteFailed), events)

        collector.cancel()
    }

    @Test
    fun guestSession_hasNoItems() = runTest(mainDispatcher) {
        emojisRepository.pages[1] = fakeEmojiPage()

        val viewModel = viewModel()
        advanceUntilIdle()
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isAuthPending)
        assertTrue(state.items.isEmpty())
        assertFalse(state.isSubscribed)
        assertTrue(emojisRepository.requestedPages.isEmpty())
    }
}
