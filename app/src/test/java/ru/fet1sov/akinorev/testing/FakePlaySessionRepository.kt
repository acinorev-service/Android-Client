package ru.fet1sov.akinorev.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.fet1sov.akinorev.data.play.PlaySessionRepository

class FakePlaySessionRepository : PlaySessionRepository {
    private val mutablePlayers = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val players: StateFlow<Map<String, Int>> = mutablePlayers.asStateFlow()

    val started = mutableListOf<String>()
    val heartbeats = mutableListOf<String>()
    val ended = mutableListOf<String>()
    val watched = mutableListOf<String>()
    val unwatched = mutableListOf<String>()

    var playersCount = 0

    override fun start(gameId: String) {
        started += gameId
        bump(gameId, 1)
    }

    override fun heartbeat(gameId: String) {
        heartbeats += gameId
    }

    override fun end(gameId: String) {
        ended += gameId
        bump(gameId, -1)
    }

    override fun watch(gameId: String) {
        watched += gameId
    }

    override fun unwatch(gameId: String) {
        unwatched += gameId
    }

    override suspend fun fetchPlayers(gameId: String): Int {
        mutablePlayers.update { it + (gameId to playersCount) }
        return playersCount
    }

    fun setPlayers(gameId: String, count: Int) {
        mutablePlayers.update { it + (gameId to count) }
    }

    private fun bump(gameId: String, delta: Int) {
        mutablePlayers.update { current ->
            current + (gameId to ((current[gameId] ?: 0) + delta).coerceAtLeast(0))
        }
    }
}
