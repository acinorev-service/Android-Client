package ru.fet1sov.akinorev.data.notifications

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadDto
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.model.NotificationType
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

class DefaultNotificationsRepositoryTest {
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val repository = DefaultNotificationsRepository(api, ApiClient(api, sessionManager))

    @Before
    fun authorizeSession() = runBlocking {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
    }

    @Test
    fun fetchPage_mapsItemsAndCounters() = runTest {
        api.onGetNotifications = { limit, offset ->
            assertEquals(25, limit)
            assertEquals(0, offset)
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(
                        ApiFixtures.notification(id = "n-1", type = "follow"),
                        ApiFixtures.notification(
                            id = "n-2",
                            type = "broadcast",
                            actor = null,
                            title = mapOf("ru" to "Объявление", "en" to "Announcement"),
                        ),
                    ),
                    hasMore = true,
                    unread = 2,
                ),
            )
        }

        val result = repository.fetchPage(limit = 25, offset = 0)

        val page = (result as AuthResult.Success).value
        assertEquals(2, page.items.size)
        assertEquals(NotificationType.Follow, page.items[0].type)
        assertEquals(NotificationType.Broadcast, page.items[1].type)
        assertTrue(page.hasMore)
        assertEquals(2, page.unread)
    }

    @Test
    fun fetchPage_skipsUnknownType() = runTest {
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(
                        ApiFixtures.notification(id = "n-1", type = "unknown"),
                        ApiFixtures.notification(id = "n-2", type = "like"),
                    ),
                ),
            )
        }

        val page = (repository.fetchPage(limit = 25, offset = 0) as AuthResult.Success).value

        assertEquals(1, page.items.size)
        assertEquals("n-2", page.items.single().id)
    }

    @Test
    fun markRead_sendsIds() = runTest {
        var captured: MarkNotificationsReadRequest? = null
        api.onMarkNotificationsRead = { request ->
            captured = request
            ApiFixtures.success(MarkNotificationsReadDto(updated = request.ids?.size ?: 0))
        }

        val result = repository.markRead(listOf("n-1", "n-2"))

        assertEquals(listOf("n-1", "n-2"), captured?.ids)
        assertEquals(2, (result as AuthResult.Success).value)
    }

    @Test
    fun markAllRead_sendsEmptyIds() = runTest {
        var captured: MarkNotificationsReadRequest? = null
        api.onMarkNotificationsRead = { request ->
            captured = request
            ApiFixtures.success(MarkNotificationsReadDto(updated = 5))
        }

        val result = repository.markAllRead()

        assertTrue(captured != null)
        assertNull(captured?.ids)
        assertEquals(5, (result as AuthResult.Success).value)
    }

    @Test
    fun fetchPage_mapsServerError() = runTest {
        api.onGetNotifications = { _, _ -> ApiFixtures.error(500, "server_error") }

        val result = repository.fetchPage(limit = 25, offset = 0)

        val error = (result as AuthResult.Failure).error
        assertTrue(error is AuthError.Server)
        assertEquals("server_error", (error as AuthError.Server).code)
    }

    @Test
    fun fetchPage_marksUnreadItems() = runTest {
        api.onGetNotifications = { _, _ ->
            ApiFixtures.success(
                ApiFixtures.notificationList(
                    items = listOf(
                        ApiFixtures.notification(id = "n-1", read = false),
                        ApiFixtures.notification(id = "n-2", read = true),
                    ),
                ),
            )
        }

        val page = (repository.fetchPage(limit = 25, offset = 0) as AuthResult.Success).value

        assertFalse(page.items[0].isRead)
        assertTrue(page.items[1].isRead)
    }
}
