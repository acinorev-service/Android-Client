package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatalogGenreDto(
    val id: String = "",
    val slug: String = "",
    val name: String = "",
    val icon: String? = null,
    val title: String = "",
)

@Serializable
data class CatalogPlatformDto(
    val id: String = "",
    val slug: String = "",
    val name: String = "",
    val title: String = "",
)

@Serializable
data class CatalogDeveloperDto(
    val id: String = "",
    val name: String = "",
    val avatar: String? = null,
    val website: String? = null,
)

@Serializable
data class CatalogAuthorDto(
    val id: String = "",
    val username: String = "",
    val avatar: String? = null,
)

@Serializable
data class CatalogGameDto(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val banner: String? = null,
    val icon: String? = null,
    val status: String = "",
    val plays: Long = 0,
    @SerialName("published_at") val publishedAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val genre: CatalogGenreDto? = null,
    val developer: CatalogDeveloperDto? = null,
    val author: CatalogAuthorDto? = null,
    val platforms: List<CatalogPlatformDto> = emptyList(),
)

@Serializable
data class CatalogGamesPayloadDto(
    val games: List<CatalogGameDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
)

@Serializable
data class CatalogGamePayloadDto(
    val game: CatalogGameDto? = null,
)

@Serializable
data class CatalogGenresPayloadDto(
    val genres: List<CatalogGenreDto> = emptyList(),
)

@Serializable
data class GamePlayersPayloadDto(
    @SerialName("game_id") val gameId: String = "",
    val count: Int = 0,
)

@Serializable
data class PlayStatePayloadDto(
    @SerialName("game_id") val gameId: String = "",
    val playing: Boolean = false,
    val ok: Boolean = false,
)
