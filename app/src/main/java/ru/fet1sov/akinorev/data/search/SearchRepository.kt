package ru.fet1sov.akinorev.data.search

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.SearchPage

interface SearchRepository {
    suspend fun search(query: String, limit: Int): AuthResult<SearchPage>
}

const val SearchPageSize = 20
