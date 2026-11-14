package ru.fet1sov.akinorev.feature.presence

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.realtime.RealtimeEvent
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class PresenceViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultAuthRepository(api, sessionManager)
    private val socket = FakeRealtimeSocket()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    @Test
    fun start_connectsForAuthorizedSession() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())

        val viewModel = PresenceViewModel(repository, socket)
        advanceUntilIdle()

        viewModel.start()
        advanceUntilIdle()

        assertEquals(1, socket.connectCalls)
    }

    @Test
    fun start_doesNotConnectWhileSessionIsLoading() = runTest(mainDispatcher) {
        val viewModel = PresenceViewModel(repository, socket)
        advanceUntilIdle()

        viewModel.start()
        advanceUntilIdle()

        assertEquals(0, socket.connectCalls)
    }

    @Test
    fun stop_disconnectsSocket() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())

        val viewModel = PresenceViewModel(repository, socket)
        advanceUntilIdle()
        viewModel.start()
        advanceUntilIdle()

        viewModel.stop()
        advanceUntilIdle()

        assertEquals(1, socket.disconnectCalls)
    }

    @Test
    fun logout_disconnectsSocket() = runTest(mainDispatcher) {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())

        val viewModel = PresenceViewModel(repository, socket)
        advanceUntilIdle()
        viewModel.start()
        advanceUntilIdle()

        repository.logout()
        advanceUntilIdle()

        assertTrue(socket.disconnectCalls > 0)
    }

    @Test
    fun isOnline_followsSocketState() = runTest(mainDispatcher) {
        val viewModel = PresenceViewModel(repository, socket)
        advanceUntilIdle()

        socket.connected.value = true

        assertTrue(viewModel.isOnline.value)
    }

    private class FakeRealtimeSocket : RealtimeSocket {
        val connected = MutableStateFlow(false)
        override val isConnected: StateFlow<Boolean> = connected
        override val events: SharedFlow<RealtimeEvent> = MutableSharedFlow()

        var connectCalls = 0
            private set
        var disconnectCalls = 0
            private set

        override fun connect() {
            connectCalls += 1
            connected.value = true
        }

        override fun disconnect() {
            disconnectCalls += 1
            connected.value = false
        }
    }
}
