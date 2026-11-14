package ru.fet1sov.akinorev.data.play

import kotlinx.coroutines.flow.StateFlow

interface PlaySessionRepository {
    val players: StateFlow<Map<String, Int>>

    fun start(gameId: String)

    fun heartbeat(gameId: String)

    fun end(gameId: String)

    fun watch(gameId: String)

    fun unwatch(gameId: String)

    suspend fun fetchPlayers(gameId: String): Int
}
