package ru.fet1sov.akinorev.model

data class LibraryGame(
    val id: String,
    val title: String,
    val iconPath: String,
    val genreSlug: String,
    val secondsPlayed: Long,
    val sessions: Long,
    val lastPlayedAtMillis: Long?,
)

data class LibraryTotals(
    val games: Int,
    val secondsPlayed: Long,
)

data class LibraryPage(
    val games: List<LibraryGame>,
    val totals: LibraryTotals,
)
