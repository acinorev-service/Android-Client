package ru.fet1sov.akinorev.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Base64

class JwtTokenTest {
    @Test
    fun expiresAtMillis_readsExpFromPayload() {
        val token = tokenWithPayload("""{"exp":1763200000}""")

        assertEquals(1_763_200_000_000L, JwtToken.expiresAtMillis(token))
    }

    @Test
    fun expiresAtMillis_isNullForMalformedToken() {
        assertNull(JwtToken.expiresAtMillis("not-a-jwt"))
        assertNull(JwtToken.expiresAtMillis("header.%%%.signature"))
        assertNull(JwtToken.expiresAtMillis(tokenWithPayload("{}")))
    }

    @Test
    fun isExpiring_isTrueWithinSkewWindow() {
        val expSeconds = 1_763_200_000L
        val token = tokenWithPayload("""{"exp":$expSeconds}""")
        val expiresAt = expSeconds * 1000

        assertFalse(JwtToken.isExpiring(token, nowMillis = expiresAt - 60_000))
        assertTrue(JwtToken.isExpiring(token, nowMillis = expiresAt - 10_000))
        assertTrue(JwtToken.isExpiring(token, nowMillis = expiresAt + 1))
    }

    @Test
    fun isExpiring_isFalseWhenExpIsMissing() {
        assertFalse(JwtToken.isExpiring(tokenWithPayload("{}")))
    }

    private fun tokenWithPayload(payloadJson: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"HS256","typ":"JWT"}""".toByteArray())
        val payload = encoder.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.signature"
    }
}
