package ru.fet1sov.akinorev.model

enum class SearchCategory {
    Games,
    Developers,
    Users,
}

sealed interface SearchResult {
    val id: String

    data class Game(val game: CatalogGame) : SearchResult {
        override val id: String
            get() = game.id
    }

    data class Developer(
        override val id: String,
        val name: String,
        val handle: String,
        val avatarPath: String,
    ) : SearchResult

    data class User(
        override val id: String,
        val username: String,
        val avatarPath: String,
        val bio: String,
    ) : SearchResult
}

data class SearchGroup(
    val category: SearchCategory,
    val results: List<SearchResult>,
)

data class SearchPage(
    val query: String,
    val groups: List<SearchGroup>,
    val total: Int,
)
