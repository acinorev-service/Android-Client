package ru.fet1sov.akinorev.data.library

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

class DefaultLibraryRepositoryTest {
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultLibraryRepository(api, ApiClient(api, sessionManager))

    @Before
    fun authorizeSession() = runBlocking {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
    }

    @Test
    fun fetchLibrary_mapsGamesAndTotals() = runTest {
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(
                        ApiFixtures.libraryGame(
                            id = "game-1",
                            title = "Neon Runner",
                            genreSlug = "arcade",
                            secondsPlayed = 5_400,
                            lastPlayedAt = "2026-11-10T12:00:00Z",
                        ),
                    ),
                    secondsPlayed = 5_400,
                ),
            )
        }

        val page = (repository.fetchLibrary() as AuthResult.Success).value

        assertEquals(1, page.games.size)
        val game = page.games.single()
        assertEquals("Neon Runner", game.title)
        assertEquals("arcade", game.genreSlug)
        assertEquals(5_400L, game.secondsPlayed)
        assertEquals("http://localhost:8090/f/icon.png", game.iconPath)
        assertEquals(
            java.time.OffsetDateTime.parse("2026-11-10T12:00:00Z").toInstant().toEpochMilli(),
            game.lastPlayedAtMillis,
        )
        assertEquals(1, page.totals.games)
        assertEquals(5_400L, page.totals.secondsPlayed)
    }

    @Test
    fun fetchLibrary_fallsBackToBannerWhenIconIsMissing() = runTest {
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(
                        ApiFixtures.libraryGame(icon = null, lastPlayedAt = null),
                    ),
                ),
            )
        }

        val game = (repository.fetchLibrary() as AuthResult.Success).value.games.single()

        assertEquals("", game.iconPath)
        assertNull(game.lastPlayedAtMillis)
    }

    @Test
    fun fetchLibrary_skipsGamesWithoutId() = runTest {
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(
                        ApiFixtures.libraryGame(id = ""),
                        ApiFixtures.libraryGame(id = "game-2"),
                    ),
                ),
            )
        }

        val page = (repository.fetchLibrary() as AuthResult.Success).value

        assertEquals(listOf("game-2"), page.games.map { it.id })
    }

    @Test
    fun fetchLibrary_fallsBackToGamesCountWhenTotalsAreEmpty() = runTest {
        api.onGetLibraryGames = {
            ApiFixtures.success(
                ApiFixtures.libraryPayload(
                    games = listOf(ApiFixtures.libraryGame(id = "a"), ApiFixtures.libraryGame(id = "b")),
                    totalGames = 0,
                    secondsPlayed = 0,
                ),
            )
        }

        val page = (repository.fetchLibrary() as AuthResult.Success).value

        assertEquals(2, page.totals.games)
        assertEquals(0L, page.totals.secondsPlayed)
    }

    @Test
    fun fetchLibrary_mapsServerError() = runTest {
        api.onGetLibraryGames = { ApiFixtures.error(500, "server_error") }

        val result = repository.fetchLibrary()

        val error = (result as AuthResult.Failure).error
        assertTrue(error is AuthError.Server)
        assertEquals("server_error", (error as AuthError.Server).code)
    }
}
