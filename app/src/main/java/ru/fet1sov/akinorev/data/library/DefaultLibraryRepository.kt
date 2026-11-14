package ru.fet1sov.akinorev.data.library

import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.LibraryGameDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryGamesPayloadDto
import ru.fet1sov.akinorev.model.LibraryGame
import ru.fet1sov.akinorev.model.LibraryPage
import ru.fet1sov.akinorev.model.LibraryTotals
import java.time.Instant
import java.time.OffsetDateTime

class DefaultLibraryRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : LibraryRepository {
    override suspend fun fetchLibrary(): AuthResult<LibraryPage> =
        when (val result = client.execute { api.getLibraryGames() }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }
}

internal fun LibraryGamesPayloadDto.toDomain(): LibraryPage = LibraryPage(
    games = games.mapNotNull { it.toDomain() },
    totals = LibraryTotals(
        games = totals.games.takeIf { it > 0 } ?: games.size,
        secondsPlayed = totals.secondsPlayed,
    ),
)

internal fun LibraryGameDto.toDomain(): LibraryGame? {
    val id = id.takeIf { it.isNotBlank() } ?: return null

    return LibraryGame(
        id = id,
        title = title,
        iconPath = icon?.takeIf { it.isNotBlank() } ?: banner.orEmpty(),
        genreSlug = genre?.slug.orEmpty(),
        secondsPlayed = secondsPlayed.coerceAtLeast(0),
        sessions = sessions.coerceAtLeast(0),
        lastPlayedAtMillis = parseOptionalTimestamp(lastPlayedAt),
    )
}

internal fun parseOptionalTimestamp(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
}
