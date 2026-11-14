package ru.fet1sov.akinorev.data.auth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.util.Base64

object JwtToken {
    fun expiresAtMillis(token: String): Long? {
        val payload = token.split('.').getOrNull(1) ?: return null
        if (payload.isBlank()) return null

        val padded = payload.padEnd(payload.length + (4 - payload.length % 4) % 4, '=')
        val decoded = runCatching {
            Base64.getUrlDecoder().decode(padded).toString(Charsets.UTF_8)
        }.getOrNull() ?: return null

        val exp = runCatching {
            Json.parseToJsonElement(decoded).jsonObject["exp"]?.jsonPrimitive?.longOrNull
        }.getOrNull() ?: return null

        return exp * 1000
    }

    fun isExpiring(
        token: String,
        nowMillis: Long = System.currentTimeMillis(),
        skewMillis: Long = DefaultSkewMillis,
    ): Boolean {
        val expiresAt = expiresAtMillis(token) ?: return false
        return expiresAt - skewMillis <= nowMillis
    }

    private const val DefaultSkewMillis = 30_000L
}
