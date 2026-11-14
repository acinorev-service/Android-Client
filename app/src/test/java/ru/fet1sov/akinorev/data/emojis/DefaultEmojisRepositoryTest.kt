package ru.fet1sov.akinorev.data.emojis

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.CdnUploadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiDeletedDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.model.EmojiMetadata
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.FakeCdnApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

class DefaultEmojisRepositoryTest {
    private val api = FakeAcinorevApi()
    private val cdnApi = FakeCdnApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultEmojisRepository(
        api = api,
        cdnApi = cdnApi,
        client = ApiClient(api, sessionManager),
    )

    @Test
    fun fetchPage_mapsPayload() = runTest {
        api.onGetEmojis = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.emojiList(
                    items = listOf(ApiFixtures.emoji(id = "a", name = "pepe", mine = true)),
                    page = 2,
                    totalPages = 4,
                    total = 80,
                ),
            )
        }

        val result = repository.fetchPage(page = 2, limit = 24)

        assertTrue(result is AuthResult.Success)
        val page = (result as AuthResult.Success).value
        assertEquals(2, page.page)
        assertEquals(4, page.totalPages)
        assertEquals(80, page.total)
        assertEquals("pepe", page.items.single().name)
        assertTrue(page.items.single().isMine)
    }

    @Test
    fun upload_sendsCdnUrlAndMetadata() = runTest {
        var request: EmojiRegisterRequest? = null
        cdnApi.onUploadFile = {
            ApiFixtures.success(CdnUploadDto(url = "http://localhost:8090/f/abc.webp"))
        }
        api.onCreateEmoji = { body ->
            request = body
            ApiFixtures.success(
                EmojiPayloadDto(emoji = ApiFixtures.emoji(id = "a", name = "pepe", mine = true)),
            )
        }

        val result = repository.upload(
            file = EmojiUploadFile(
                fileName = "pepe.png",
                mimeType = "image/png",
                bytes = byteArrayOf(1, 2, 3),
            ),
            metadata = EmojiMetadata(name = "pepe", title = "Pepe", description = ""),
        )

        assertTrue(result is AuthResult.Success)
        assertEquals(1, cdnApi.uploads)
        assertEquals("http://localhost:8090/f/abc.webp", request?.url)
        assertEquals("image/webp", request?.mime)
        assertEquals(3L, request?.size)
        assertEquals("pepe", request?.name)
        assertEquals("Pepe", request?.title)
    }

    @Test
    fun upload_cdnFailureStopsRegistration() = runTest {
        cdnApi.onUploadFile = { ApiFixtures.error(500, "server_error") }
        var registerCalls = 0
        api.onCreateEmoji = {
            registerCalls += 1
            ApiFixtures.error(500)
        }

        val result = repository.upload(
            file = EmojiUploadFile("pepe.png", "image/png", byteArrayOf(1)),
            metadata = EmojiMetadata(name = "pepe"),
        )

        assertTrue(result is AuthResult.Failure)
        assertEquals(0, registerCalls)
    }

    @Test
    fun delete_returnsDeletedId() = runTest {
        api.onDeleteEmoji = { ApiFixtures.success(EmojiDeletedDto(emojiId = "a")) }

        val result = repository.delete("a")

        assertTrue(result is AuthResult.Success)
        assertEquals("a", (result as AuthResult.Success).value)
    }
}
