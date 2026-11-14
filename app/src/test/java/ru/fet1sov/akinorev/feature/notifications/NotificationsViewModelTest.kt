package ru.fet1sov.akinorev.feature.notifications

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.notifications.DefaultNotificationsRepository
import ru.fet1sov.akinorev.data.realtime.RealtimeEvent
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadDto
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppSettings
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeAppSettingsRepository
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val notificationsRepository = DefaultNotificationsRepository(api, ApiClient(api, sessionManager))
    private val settingsRepository = FakeAppSettingsRepository(
        AppSettings(language = AppLanguage.Russian),
    )
    private val socket = FakeRealtimeSocket()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private suspend fun authorize() {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }
    }

    private fun viewModel() = NotificationsViewModel(
        authRepository = authRepository,
        notificationsRepository = notificationsRepository,
        realtimeSocket = socket,
        appSettingsRepository = settingsRepository,
    )

    @Test
    fun authorizedSession_loadsFirstPage() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, offset ->
            assertEquals(0, offset)
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(ApiFixtures.notification(id = "n-1")),
                    hasMore = true,
                    unread = 3,
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated)
        assertFalse(state.isLoading)
        assertEquals(listOf("n-1"), state.items.map { it.id })
        assertEquals(3, state.unread)
        assertTrue(state.hasMore)
        assertEquals("fet1sov", state.items.single().actorUsername)
    }

    @Test
    fun guestSession_startsWithEmptyState() = runTest(mainDispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isAuthPending)
        assertTrue(state.items.isEmpty())
        assertEquals(0, state.unread)
    }

    @Test
    fun loadMore_appendsPageWithoutDuplicates() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, offset ->
            if (offset == 0) {
                ApiFixtures.success(
                    ApiFixtures.notificationList(
                        items = listOf(ApiFixtures.notification(id = "n-1")),
                        hasMore = true,
                    ),
                )
            } else {
                ApiFixtures.success(
                    ApiFixtures.notificationList(
                        items = listOf(
                            ApiFixtures.notification(id = "n-1"),
                            ApiFixtures.notification(id = "n-2"),
                        ),
                        hasMore = false,
                    ),
                )
            }
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("n-1", "n-2"), state.items.map { it.id })
        assertFalse(state.hasMore)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun markRead_updatesStateAndSendsBatch() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(ApiFixtures.notification(id = "n-1", read = false)),
                    unread = 1,
                ),
            )
        }
        val requests = mutableListOf<MarkNotificationsReadRequest>()
        api.onMarkNotificationsRead = { request ->
            requests += request
            ApiFixtures.success(MarkNotificationsReadDto(updated = request.ids?.size ?: 0))
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.markRead(listOf("n-1"))
        advanceTimeBy(300)
        runCurrent()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.single().isRead)
        assertEquals(0, viewModel.uiState.value.unread)
        assertEquals(listOf("n-1"), requests.single().ids)
    }

    @Test
    fun markAllRead_clearsUnreadAndSendsEmptyIds() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(
                        ApiFixtures.notification(id = "n-1"),
                        ApiFixtures.notification(id = "n-2"),
                    ),
                    unread = 2,
                ),
            )
        }
        var captured: MarkNotificationsReadRequest? = null
        api.onMarkNotificationsRead = { request ->
            captured = request
            ApiFixtures.success(MarkNotificationsReadDto(updated = 2))
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.markAllRead()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.all { it.isRead })
        assertEquals(0, viewModel.uiState.value.unread)
        assertNull(captured?.ids)
    }

    @Test
    fun websocketNotification_isInsertedOnTop() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(ApiFixtures.notification(id = "n-1")),
                    unread = 1,
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        socket.emit(
            RealtimeEvent(
                type = "notification",
                payload = Json.parseToJsonElement(
                    """{"id":"n-2","type":"like","actor_id":"user-2","actor":{"id":"user-2","username":"neo"},"read":false,"created_at":"2026-11-14T11:00:00Z"}""",
                ),
            ),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("n-2", "n-1"), state.items.map { it.id })
        assertEquals(2, state.unread)
        assertEquals("neo", state.items.first().actorUsername)
    }

    @Test
    fun broadcast_isLocalizedWithAppLanguage() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(
                        ApiFixtures.notification(
                            id = "n-1",
                            type = "broadcast",
                            actor = null,
                            title = mapOf("ru" to "Объявление", "en" to "Announcement"),
                            body = mapOf("ru" to "Текст", "en" to "Text"),
                        ),
                    ),
                ),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        val item = viewModel.uiState.value.items.single()
        assertEquals("Объявление", item.title)
        assertEquals("Текст", item.body)
    }

    private class FakeRealtimeSocket : RealtimeSocket {
        private val mutableEvents = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 8)
        override val events: SharedFlow<RealtimeEvent> = mutableEvents
        override val isConnected: StateFlow<Boolean> = MutableStateFlow(false)

        fun emit(event: RealtimeEvent) {
            mutableEvents.tryEmit(event)
        }

        override fun connect() = Unit

        override fun disconnect() = Unit

        override fun send(message: String): Boolean = false
    }
}
