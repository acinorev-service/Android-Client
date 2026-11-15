package ru.fet1sov.akinorev.data.realtime

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import ru.fet1sov.akinorev.data.community.parseCommunityTimestamp

data class PresenceEvent(
    val username: String,
    val online: Boolean,
    val device: String,
    val lastSeenMillis: Long?,
    val lastSeenDevice: String,
)

data class FollowersEvent(
    val username: String,
    val count: Int,
)

fun parsePresenceEvent(payload: JsonElement?): PresenceEvent? {
    val objectBody = payload as? JsonObject ?: return null
    val username = objectBody.string("username") ?: return null
    val lastSeen = parseCommunityTimestamp(objectBody.string("last_seen"))
        ?.takeIf { it > MinimumValidTimestampMillis }

    return PresenceEvent(
        username = username,
        online = objectBody["online"]?.jsonPrimitive?.booleanOrNull ?: false,
        device = objectBody.string("device").orEmpty(),
        lastSeenMillis = lastSeen,
        lastSeenDevice = objectBody.string("last_seen_device").orEmpty(),
    )
}

fun parseFollowersEvent(payload: JsonElement?): FollowersEvent? {
    val objectBody = payload as? JsonObject ?: return null
    val username = objectBody.string("username") ?: return null
    val count = objectBody["count"]?.jsonPrimitive?.intOrNull ?: return null
    return FollowersEvent(username = username, count = count)
}

private fun JsonObject.string(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

private const val MinimumValidTimestampMillis = 0L
