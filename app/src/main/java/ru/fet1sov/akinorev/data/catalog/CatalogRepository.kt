package ru.fet1sov.akinorev.data.catalog

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.model.CatalogGenre

interface CatalogRepository {
    suspend fun fetchGames(limit: Int = CatalogPageSize): AuthResult<List<CatalogGame>>

    suspend fun fetchGame(id: String): AuthResult<CatalogGame>

    suspend fun fetchGenres(): AuthResult<List<CatalogGenre>>
}

const val CatalogPageSize = 100
