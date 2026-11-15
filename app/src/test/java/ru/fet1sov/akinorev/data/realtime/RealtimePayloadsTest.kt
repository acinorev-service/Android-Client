package ru.fet1sov.akinorev.data.realtime

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RealtimePayloadsTest {
    @Test
    fun parsePresenceEvent_readsFields() {
        val payload = Json.parseToJsonElement(
            """
            {
              "user_id": "user-1",
              "username": "fet1sov",
              "online": true,
              "device": "mobile",
              "last_seen": "2026-11-15T10:00:00Z",
              "last_seen_device": "mobile",
              "playing_game": ""
            }
            """.trimIndent(),
        )

        val event = parsePresenceEvent(payload)

        assertNotNull(event)
        assertEquals("fet1sov", event?.username)
        assertTrue(event?.online == true)
        assertEquals("mobile", event?.device)
        assertEquals("mobile", event?.lastSeenDevice)
        assertNotNull(event?.lastSeenMillis)
    }

    @Test
    fun parsePresenceEvent_ignoresZeroTimestamp() {
        val payload = Json.parseToJsonElement(
            """{"username":"fet1sov","online":false,"last_seen":"0001-01-01T00:00:00Z"}""",
        )

        val event = parsePresenceEvent(payload)

        assertNotNull(event)
        assertNull(event?.lastSeenMillis)
        assertFalse(event?.online == true)
    }

    @Test
    fun parsePresenceEvent_rejectsMissingUsername() {
        val payload = Json.parseToJsonElement("""{"online":true}""")

        assertNull(parsePresenceEvent(payload))
        assertNull(parsePresenceEvent(null))
    }

    @Test
    fun parseFollowersEvent_readsCount() {
        val payload = Json.parseToJsonElement(
            """{"user_id":"user-1","username":"fet1sov","count":42}""",
        )

        val event = parseFollowersEvent(payload)

        assertNotNull(event)
        assertEquals("fet1sov", event?.username)
        assertEquals(42, event?.count)
    }

    @Test
    fun parseFollowersEvent_rejectsMissingCount() {
        val payload = Json.parseToJsonElement("""{"username":"fet1sov"}""")

        assertNull(parseFollowersEvent(payload))
    }
}
