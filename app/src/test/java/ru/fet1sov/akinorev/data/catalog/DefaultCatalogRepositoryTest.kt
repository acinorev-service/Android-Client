package ru.fet1sov.akinorev.data.catalog

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamePayloadDto
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCatalogRepositoryTest {
    private val api = FakeAcinorevApi()
    private val repository = DefaultCatalogRepository(
        api = api,
        client = ApiClient(api, SessionManager(InMemorySessionStorage())),
    )

    @Test
    fun fetchGames_mapsCatalogFields() = runTest {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(ApiFixtures.catalogPayload(games = listOf(ApiFixtures.catalogGame())))
        }

        val result = repository.fetchGames()

        assertTrue(result is AuthResult.Success)
        val game = (result as AuthResult.Success).value.single()
        assertEquals("game-1", game.id)
        assertEquals("Neon Runner", game.title)
        assertEquals("arcade", game.genreSlug)
        assertEquals("Acinorev Studio", game.developerName)
        assertEquals(listOf("mobile", "pc"), game.platformSlugs)
        assertTrue(game.supportsMobile)
        assertTrue(game.supportsPc)
        assertNotNull(game.publishedAtMillis)
        assertFalse(game.publishedAtMillis!! <= 0L)
    }

    @Test
    fun fetchGames_withoutPlatforms_isAvailableEverywhere() = runTest {
        api.onGetGames = { _, _, _, _ ->
            ApiFixtures.success(
                ApiFixtures.catalogPayload(
                    games = listOf(ApiFixtures.catalogGame(platforms = emptyList())),
                ),
            )
        }

        val game = (repository.fetchGames() as AuthResult.Success).value.single()

        assertTrue(game.supportsMobile)
        assertTrue(game.supportsPc)
    }

    @Test
    fun fetchGames_failureIsPropagated() = runTest {
        api.onGetGames = { _, _, _, _ -> ApiFixtures.error(500, "server_error") }

        val result = repository.fetchGames()

        assertTrue(result is AuthResult.Failure)
    }

    @Test
    fun fetchGame_missingPayload_isNotFound() = runTest {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = null)) }

        val result = repository.fetchGame("game-1")

        assertTrue(result is AuthResult.Failure)
        val error = (result as AuthResult.Failure).error
        assertTrue(error is ru.fet1sov.akinorev.data.remote.AuthError.Server)
    }

    @Test
    fun fetchGame_mapsTitle() = runTest {
        api.onGetGame = { ApiFixtures.success(CatalogGamePayloadDto(game = ApiFixtures.catalogGame())) }

        val game = (repository.fetchGame("game-1") as AuthResult.Success).value

        assertEquals("Neon Runner", game.title)
    }

    @Test
    fun fetchGenres_fallsBackToTitleKey() = runTest {
        api.onGetGenres = {
            ApiFixtures.success(
                ApiFixtures.catalogGenres(
                    genres = listOf(
                        ru.fet1sov.akinorev.data.remote.dto.CatalogGenreDto(id = "1", slug = "arcade"),
                    ),
                ),
            )
        }

        val genres = (repository.fetchGenres() as AuthResult.Success).value

        assertEquals("arcade", genres.single().slug)
        assertEquals("genres.arcade", genres.single().titleKey)
    }
}
