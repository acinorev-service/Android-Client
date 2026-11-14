package ru.fet1sov.akinorev.data.search

import ru.fet1sov.akinorev.data.catalog.toDomain
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.CatalogGameDto
import ru.fet1sov.akinorev.data.remote.dto.SearchCategoryDto
import ru.fet1sov.akinorev.data.remote.dto.SearchItemDto
import ru.fet1sov.akinorev.model.SearchCategory
import ru.fet1sov.akinorev.model.SearchGroup
import ru.fet1sov.akinorev.model.SearchPage
import ru.fet1sov.akinorev.model.SearchResult

class DefaultSearchRepository(
    private val api: AcinorevApi,
    private val client: ApiClient,
) : SearchRepository {
    override suspend fun search(query: String, limit: Int): AuthResult<SearchPage> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AuthResult.Success(SearchPage(query = "", groups = emptyList(), total = 0))
        }

        return when (val result = client.execute { api.search(trimmed, limit) }) {
            is AuthResult.Success -> AuthResult.Success(
                SearchPage(
                    query = result.value.query.ifBlank { trimmed },
                    groups = result.value.categories.mapNotNull(SearchCategoryDto::toGroup),
                    total = result.value.total,
                ),
            )

            is AuthResult.Failure -> result
        }
    }
}

private fun SearchCategoryDto.toGroup(): SearchGroup? {
    val category = parseCategory(category) ?: return null
    val results = items.mapNotNull { it.toResult(category) }
    if (results.isEmpty()) return null

    return SearchGroup(category = category, results = results)
}

private fun SearchItemDto.toResult(groupCategory: SearchCategory): SearchResult? {
    val id = id.takeIf { it.isNotBlank() } ?: return null

    return when (parseCategory(category) ?: groupCategory) {
        SearchCategory.Games -> toCatalogGame(id)?.let(SearchResult::Game)

        SearchCategory.Developers -> SearchResult.Developer(
            id = id,
            name = name.ifBlank { title },
            handle = handle,
            avatarPath = avatar.orEmpty(),
        )

        SearchCategory.Users -> SearchResult.User(
            id = id,
            username = username.ifBlank { title },
            avatarPath = avatar.orEmpty(),
            bio = bio,
        )
    }
}

private fun SearchItemDto.toCatalogGame(id: String) = CatalogGameDto(
    id = id,
    title = title,
    description = description,
    banner = banner,
    icon = icon,
    genre = genre,
    developer = developer,
    author = author,
    platforms = platforms,
).toDomain()

private fun parseCategory(raw: String): SearchCategory? = when (raw.lowercase()) {
    "games" -> SearchCategory.Games
    "developers" -> SearchCategory.Developers
    "users" -> SearchCategory.Users
    else -> null
}
