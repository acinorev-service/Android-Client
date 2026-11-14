package ru.fet1sov.akinorev.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayDestination(
    val gameId: String,
    val title: String = "",
) {
    val path: String
        get() = "/play/$gameId"
}
