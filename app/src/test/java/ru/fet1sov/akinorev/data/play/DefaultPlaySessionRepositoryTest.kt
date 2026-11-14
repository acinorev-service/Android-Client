package ru.fet1sov.akinorev.data.play

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeRealtimeSocket
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPlaySessionRepositoryTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val socket = FakeRealtimeSocket()
    private val api = FakeAcinorevApi()
    private val client = ApiClient(api, SessionManager(InMemorySessionStorage()))

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun repository() = DefaultPlaySessionRepository(
        socket = socket,
        api = api,
        client = client,
        dispatcher = mainDispatcher,
    )

    @Test
    fun start_sendsPlayStartAndBumpsPlayers() = runTest(mainDispatcher) {
        val repository = repository()
        advanceUntilIdle()

        repository.start("game-1")
        advanceUntilIdle()

        assertEquals(1, repository.players.value["game-1"])
        assertTrue(socket.sent.any { it.contains("\"play_start\"") && it.contains("game-1") })
    }

    @Test
    fun end_sendsPlayEndAndDecrementsPlayers() = runTest(mainDispatcher) {
        val repository = repository()
        advanceUntilIdle()

        repository.start("game-1")
        repository.end("game-1")
        advanceUntilIdle()

        assertEquals(0, repository.players.value["game-1"])
        assertTrue(socket.sent.any { it.contains("\"play_end\"") })
    }

    @Test
    fun heartbeatIgnoredForAnotherGame() = runTest(mainDispatcher) {
        val repository = repository()
        advanceUntilIdle()

        repository.heartbeat("game-1")
        advanceUntilIdle()

        assertTrue(socket.sent.none { it.contains("\"play_heartbeat\"") })
    }

    @Test
    fun reconnect_restoresPlaySessionAndSubscriptions() = runTest(mainDispatcher) {
        socket.connected.value = true
        val repository = repository()
        advanceUntilIdle()

        repository.start("game-1")
        repository.watch("game-1")
        advanceUntilIdle()

        socket.sent.clear()
        socket.connected.value = false
        socket.connected.value = true
        advanceUntilIdle()

        assertTrue(socket.sent.any { it.contains("\"play_start\"") })
        assertTrue(socket.sent.any { it.contains("\"watch_game_players\"") })
    }

    @Test
    fun gamePlayersEvent_updatesCounter() = runTest(mainDispatcher) {
        val repository = repository()
        advanceUntilIdle()

        socket.emit(
            type = "game_players",
            payload = buildJsonObject {
                put("game_id", "game-1")
                put("count", 5)
            },
        )
        advanceUntilIdle()

        assertEquals(5, repository.players.value["game-1"])
    }

    @Test
    fun fetchPlayers_usesServerCount() = runTest(mainDispatcher) {
        api.onGetGamePlayers = { ApiFixtures.success(ApiFixtures.gamePlayers("game-1", 7)) }
        val repository = repository()
        advanceUntilIdle()

        val count = repository.fetchPlayers("game-1")

        assertEquals(7, count)
        assertEquals(7, repository.players.value["game-1"])
    }

    @Test
    fun unwatch_sendsUnwatchOnce() = runTest(mainDispatcher) {
        val repository = repository()
        advanceUntilIdle()

        repository.watch("game-1")
        repository.unwatch("game-1")
        repository.unwatch("game-1")
        advanceUntilIdle()

        assertEquals(1, socket.sent.count { it.contains("\"unwatch_game_players\"") })
    }
}
