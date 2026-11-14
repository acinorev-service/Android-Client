package ru.fet1sov.akinorev.data.catalog

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.model.CatalogGenre

interface CatalogRepository {
    suspend fun fetchGames(
        limit: Int,
        offset: Int,
        genre: String? = null,
        platform: String? = null,
    ): AuthResult<CatalogPage>

    suspend fun fetchGame(id: String): AuthResult<CatalogGame>

    suspend fun fetchGenres(): AuthResult<List<CatalogGenre>>
}

data class CatalogPage(
    val games: List<CatalogGame>,
    val total: Int,
)
