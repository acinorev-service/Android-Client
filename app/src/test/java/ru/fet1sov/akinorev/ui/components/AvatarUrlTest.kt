package ru.fet1sov.akinorev.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarUrlTest {
    private val apiBaseUrl = "http://192.168.1.142:8080/api/"

    @Test
    fun loopbackCdnHost_isRewrittenToApiHost() {
        assertEquals(
            "http://192.168.1.142:8090/f/a.gif",
            rewriteLoopbackHost("http://localhost:8090/f/a.gif", apiBaseUrl),
        )
        assertEquals(
            "http://192.168.1.142:8090/f/a.gif",
            rewriteLoopbackHost("http://127.0.0.1:8090/f/a.gif", apiBaseUrl),
        )
        assertEquals(
            "http://192.168.1.142:8090/f/a.gif",
            rewriteLoopbackHost("http://0.0.0.0:8090/f/a.gif", apiBaseUrl),
        )
    }

    @Test
    fun loopbackCdnHost_withoutExplicitPortKeepsScheme() {
        assertEquals(
            "https://192.168.1.142/f/a.gif",
            rewriteLoopbackHost("https://localhost/f/a.gif", apiBaseUrl),
        )
    }

    @Test
    fun queryIsPreserved() {
        assertEquals(
            "http://192.168.1.142:8090/f/a.gif?size=large",
            rewriteLoopbackHost("http://localhost:8090/f/a.gif?size=large", apiBaseUrl),
        )
    }

    @Test
    fun publicCdnHost_isKeptAsIs() {
        assertEquals(
            "https://cdn.akinorev.ru/f/a.gif",
            rewriteLoopbackHost("https://cdn.akinorev.ru/f/a.gif", apiBaseUrl),
        )
    }

    @Test
    fun loopbackApiBase_leavesUrlUntouched() {
        assertEquals(
            "http://localhost:8090/f/a.gif",
            rewriteLoopbackHost("http://localhost:8090/f/a.gif", "http://localhost:8080/api/"),
        )
    }

    @Test
    fun malformedUrl_isReturnedAsIs() {
        assertEquals("http://[::1", rewriteLoopbackHost("http://[::1", apiBaseUrl))
    }

    @Test
    fun resolveMediaUrl_rewritesEmojiCdnUrl() {
        assertEquals(
            "http://192.168.1.142:8090/f/emoji.gif",
            resolveMediaUrl("http://localhost:8090/f/emoji.gif", apiBaseUrl),
        )
        assertEquals(
            "http://192.168.1.142:8090/f/emoji.webp",
            resolveMediaUrl("http://127.0.0.1:8090/f/emoji.webp", apiBaseUrl),
        )
    }

    @Test
    fun resolveMediaUrl_keepsPublicEmojiUrl() {
        assertEquals(
            "https://cdn.akinorev.ru/f/emoji.gif",
            resolveMediaUrl("https://cdn.akinorev.ru/f/emoji.gif", apiBaseUrl),
        )
    }

    @Test
    fun resolveMediaUrl_prefixesRelativeEmojiPath() {
        assertEquals(
            "http://192.168.1.142:8080/f/emoji.gif",
            resolveMediaUrl("/f/emoji.gif", apiBaseUrl),
        )
    }
}
