package ru.fet1sov.akinorev.data.catalog

import ru.fet1sov.akinorev.data.library.parseOptionalTimestamp
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.CatalogGameDto
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.model.CatalogGenre

class DefaultCatalogRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : CatalogRepository {
    override suspend fun fetchGames(limit: Int): AuthResult<List<CatalogGame>> =
        when (val result = client.execute { api.getGames(limit = limit, offset = 0) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.games.mapNotNull(CatalogGameDto::toDomain))
            is AuthResult.Failure -> result
        }

    override suspend fun fetchGame(id: String): AuthResult<CatalogGame> {
        if (id.isBlank()) return AuthResult.Failure(ru.fet1sov.akinorev.data.remote.AuthError.Unexpected)

        return when (val result = client.execute { api.getGame(id) }) {
            is AuthResult.Success -> {
                val game = result.value.game?.toDomain()
                if (game == null) {
                    AuthResult.Failure(ru.fet1sov.akinorev.data.remote.AuthError.Server("game_not_found", 404))
                } else {
                    AuthResult.Success(game)
                }
            }

            is AuthResult.Failure -> result
        }
    }

    override suspend fun fetchGenres(): AuthResult<List<CatalogGenre>> =
        when (val result = client.execute { api.getGenres() }) {
            is AuthResult.Success -> AuthResult.Success(
                result.value.genres.mapNotNull { dto ->
                    val slug = dto.slug.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    CatalogGenre(
                        id = dto.id,
                        slug = slug,
                        titleKey = dto.title.takeIf { it.isNotBlank() } ?: "genres.$slug",
                    )
                },
            )

            is AuthResult.Failure -> result
        }
}

internal fun CatalogGameDto.toDomain(): CatalogGame? {
    val id = id.takeIf { it.isNotBlank() } ?: return null

    return CatalogGame(
        id = id,
        title = title,
        description = description,
        bannerPath = banner.orEmpty(),
        iconPath = icon.orEmpty(),
        genreSlug = genre?.slug.orEmpty(),
        genreTitle = genre?.name?.takeIf { it.isNotBlank() }
            ?: genre?.title?.removePrefix("genres.").orEmpty(),
        developerId = developer?.id.orEmpty(),
        developerName = developer?.name.orEmpty(),
        developerAvatarPath = developer?.avatar.orEmpty(),
        authorUsername = author?.username.orEmpty(),
        platformSlugs = platforms.mapNotNull { it.slug.takeIf(String::isNotBlank) },
        publishedAtMillis = parseOptionalTimestamp(publishedAt ?: createdAt),
    )
}
