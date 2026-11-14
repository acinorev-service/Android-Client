package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LibraryGenreDto(
    val id: String = "",
    val slug: String = "",
    val name: String = "",
    val title: String = "",
)

@Serializable
data class LibraryGameDto(
    val id: String = "",
    val title: String = "",
    val icon: String? = null,
    val banner: String? = null,
    val genre: LibraryGenreDto? = null,
    @SerialName("seconds_played") val secondsPlayed: Long = 0,
    val sessions: Long = 0,
    @SerialName("last_played_at") val lastPlayedAt: String? = null,
)

@Serializable
data class LibraryTotalsDto(
    val games: Int = 0,
    @SerialName("seconds_played") val secondsPlayed: Long = 0,
)

@Serializable
data class LibraryGamesPayloadDto(
    val games: List<LibraryGameDto> = emptyList(),
    val totals: LibraryTotalsDto = LibraryTotalsDto(),
)
