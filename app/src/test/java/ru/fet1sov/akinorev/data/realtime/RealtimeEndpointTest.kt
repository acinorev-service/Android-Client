package ru.fet1sov.akinorev.data.realtime

import org.junit.Assert.assertEquals
import org.junit.Test

class RealtimeEndpointTest {
    @Test
    fun websocketEndpoint_stripsApiSuffix() {
        assertEquals("ws://10.0.2.2:8080", websocketEndpoint("http://10.0.2.2:8080/api/"))
        assertEquals("ws://192.168.1.142:8080", websocketEndpoint("http://192.168.1.142:8080/api"))
    }

    @Test
    fun websocketEndpoint_usesSecureSchemeForHttps() {
        assertEquals("wss://api.akinorev.ru", websocketEndpoint("https://api.akinorev.ru/api/"))
    }

    @Test
    fun websocketEndpoint_keepsBaseWithoutApi() {
        assertEquals("ws://host:9000", websocketEndpoint("http://host:9000/"))
        assertEquals("wss://host:9000", websocketEndpoint("wss://host:9000"))
    }

    @Test
    fun reconnectDelay_growsExponentiallyAndStopsAtCap() {
        assertEquals(1_000L, reconnectDelay(attempt = 0, jitterMillis = 0))
        assertEquals(2_000L, reconnectDelay(attempt = 1, jitterMillis = 0))
        assertEquals(4_000L, reconnectDelay(attempt = 2, jitterMillis = 0))
        assertEquals(30_000L, reconnectDelay(attempt = 6, jitterMillis = 0))
        assertEquals(30_250L, reconnectDelay(attempt = 50, jitterMillis = 250))
    }

    @Test
    fun reconnectDelay_addsJitter() {
        assertEquals(1_400L, reconnectDelay(attempt = 0, jitterMillis = 400))
    }
}
