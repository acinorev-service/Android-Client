package ru.fet1sov.akinorev.data.play

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import ru.fet1sov.akinorev.data.realtime.RealtimeEvent
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult

class DefaultPlaySessionRepository(
    private val socket: RealtimeSocket,
    private val api: AcinorevApi,
    private val client: ApiClient,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PlaySessionRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val lock = Any()

    private val mutablePlayers = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val players: StateFlow<Map<String, Int>> = mutablePlayers.asStateFlow()

    private val watchedGames = linkedSetOf<String>()

    @Volatile
    private var activeGameId: String? = null

    init {
        scope.launch {
            socket.events.collect(::onEvent)
        }
        scope.launch {
            socket.isConnected.collect { connected ->
                if (connected) restoreSubscriptions()
            }
        }
    }

    override fun start(gameId: String) {
        if (gameId.isBlank()) return
        activeGameId = gameId
        socket.connect()
        socket.send(event(PlayStartEvent, gameId))
        bump(gameId, 1)
    }

    override fun heartbeat(gameId: String) {
        if (gameId.isBlank() || activeGameId != gameId) return
        socket.send(event(PlayHeartbeatEvent, gameId))
    }

    override fun end(gameId: String) {
        if (activeGameId == gameId) activeGameId = null
        socket.send(event(PlayEndEvent, gameId))
        bump(gameId, -1)
    }

    override fun watch(gameId: String) {
        if (gameId.isBlank()) return
        val first = synchronized(lock) { watchedGames.add(gameId) }
        socket.connect()
        if (first) {
            socket.send(event(WatchGamePlayersEvent, gameId))
        }
    }

    override fun unwatch(gameId: String) {
        if (gameId.isBlank()) return
        val removed = synchronized(lock) { watchedGames.remove(gameId) }
        if (removed) {
            socket.send(event(UnwatchGamePlayersEvent, gameId))
        }
    }

    override suspend fun fetchPlayers(gameId: String): Int {
        if (gameId.isBlank()) return 0

        return when (val result = client.execute { api.getGamePlayers(gameId) }) {
            is AuthResult.Success -> {
                val count = result.value.count.coerceAtLeast(0)
                applyPlayers(gameId, count)
                count
            }

            is AuthResult.Failure -> countOf(gameId)
        }
    }

    private fun restoreSubscriptions() {
        activeGameId?.let { socket.send(event(PlayStartEvent, it)) }
        val games = synchronized(lock) { watchedGames.toList() }
        games.forEach { socket.send(event(WatchGamePlayersEvent, it)) }
    }

    private fun onEvent(event: RealtimeEvent) {
        when (event.type) {
            GamePlayersEvent -> {
                val payload = event.payload as? JsonObject ?: return
                val gameId = payload.string("game_id") ?: return
                val count = payload.int("count") ?: return
                applyPlayers(gameId, count.coerceAtLeast(0))
            }

            PlayStateEvent -> {
                val payload = event.payload as? JsonObject ?: return
                val gameId = payload.string("game_id") ?: return
                if (payload["ok"]?.jsonPrimitive?.contentOrNull == "false") {
                    applyPlayers(gameId, countOf(gameId))
                }
            }
        }
    }

    private fun applyPlayers(gameId: String, count: Int) {
        mutablePlayers.update { it + (gameId to count) }
    }

    private fun bump(gameId: String, delta: Int) {
        mutablePlayers.update { current ->
            val next = ((current[gameId] ?: 0) + delta).coerceAtLeast(0)
            current + (gameId to next)
        }
    }

    private fun countOf(gameId: String): Int = mutablePlayers.value[gameId] ?: 0

    private fun event(type: String, gameId: String): String = buildJsonObject {
        put("type", type)
        put("game_id", gameId)
    }.toString()

    private companion object {
        const val PlayStartEvent = "play_start"
        const val PlayHeartbeatEvent = "play_heartbeat"
        const val PlayEndEvent = "play_end"
        const val PlayStateEvent = "play_state"
        const val GamePlayersEvent = "game_players"
        const val WatchGamePlayersEvent = "watch_game_players"
        const val UnwatchGamePlayersEvent = "unwatch_game_players"
    }
}

private fun JsonObject.string(name: String): String? =
    get(name)?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

private fun JsonObject.int(name: String): Int? =
    get(name)?.jsonPrimitive?.intOrNull
