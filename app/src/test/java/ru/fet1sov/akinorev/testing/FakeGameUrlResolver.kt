package ru.fet1sov.akinorev.testing

import ru.fet1sov.akinorev.data.play.GameUrlResolver

class FakeGameUrlResolver(
    private val baseUrl: String = "http://192.168.1.142:8090/b/build-ref/index.html?mt_game=game-1",
) : GameUrlResolver {
    val resolvedRooms = mutableListOf<String?>()

    override suspend fun resolve(gameId: String, room: String?): String {
        resolvedRooms += room
        if (room.isNullOrBlank()) return baseUrl
        return "$baseUrl&room=$room&online=1"
    }
}
