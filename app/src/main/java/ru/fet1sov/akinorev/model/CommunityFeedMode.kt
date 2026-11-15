package ru.fet1sov.akinorev.model

enum class CommunityFeedMode(
    val sort: String,
    val filter: String?,
) {
    Popular(sort = "popular", filter = null),
    New(sort = "new", filter = null),
    Following(sort = "new", filter = "following"),
}
