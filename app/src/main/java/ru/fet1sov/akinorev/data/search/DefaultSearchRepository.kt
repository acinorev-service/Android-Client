package ru.fet1sov.akinorev.data.search

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
    private val emojiCache = mutableMapOf<String, String>()

    override suspend fun search(query: String, limit: Int): AuthResult<SearchPage> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AuthResult.Success(SearchPage(query = "", groups = emptyList(), total = 0))
        }

        return when (val result = client.execute { api.search(trimmed, limit) }) {
            is AuthResult.Success -> {
                val groups = enrichUsers(result.value.categories.mapNotNull(SearchCategoryDto::toGroup))
                AuthResult.Success(
                    SearchPage(
                        query = result.value.query.ifBlank { trimmed },
                        groups = groups,
                        total = result.value.total,
                        emojis = resolveEmojiUrls(groups),
                    ),
                )
            }

            is AuthResult.Failure -> result
        }
    }

    private suspend fun enrichUsers(groups: List<SearchGroup>): List<SearchGroup> = coroutineScope {
        var budget = ProfileEnrichLimit
        groups.map { group ->
            if (group.category != SearchCategory.Users) return@map group

            val deferred = group.results.map { result ->
                if (result is SearchResult.User && budget > 0) {
                    budget -= 1
                    async { enrichUser(result) }
                } else {
                    null
                }
            }

            group.copy(
                results = deferred.mapIndexed { index, job ->
                    job?.await() ?: group.results[index]
                },
            )
        }
    }

    private suspend fun enrichUser(result: SearchResult): SearchResult {
        val user = result as? SearchResult.User ?: return result
        if (user.username.isBlank()) return user
        if (user.nameColor.isNotBlank() || user.nameAnimation.isNotBlank()) return user

        return when (val response = client.execute { api.getUser(user.username) }) {
            is AuthResult.Success -> response.value.user?.let { profile ->
                user.copy(
                    bio = profile.bio.ifBlank { user.bio },
                    nameColor = profile.nameColor,
                    nameAnimation = profile.nameAnimation,
                    isSubscribed = profile.isSubscribed,
                )
            } ?: user

            is AuthResult.Failure -> user
        }
    }

    private suspend fun resolveEmojiUrls(groups: List<SearchGroup>): Map<String, String> = coroutineScope {
        val names = groups.asSequence()
            .flatMap { it.results.asSequence() }
            .filterIsInstance<SearchResult.User>()
            .flatMap { user ->
                EmojiMarker.findAll(user.bio).map { match -> match.groupValues[2].lowercase() }.asSequence()
            }
            .distinct()
            .filter { it !in emojiCache }
            .take(EmojiLookupLimit)
            .toList()

        names.map { name ->
            async {
                when (val response = client.execute { api.getEmojiByName(name) }) {
                    is AuthResult.Success -> response.value.emoji?.url
                        ?.takeIf { it.isNotBlank() }
                        ?.let { emojiCache[name] = it }

                    is AuthResult.Failure -> Unit
                }
            }
        }.awaitAll()

        emojiCache.toMap()
    }

    private companion object {
        const val ProfileEnrichLimit = 8
        const val EmojiLookupLimit = 24

        val EmojiMarker = Regex("(^|\\s):([a-zA-Z0-9_]{1,32}):(?=\$|\\s|[.,!?])")
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
            nameColor = nameColor.orEmpty(),
            nameAnimation = nameAnimation.orEmpty(),
            isSubscribed = isSubscribed,
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
