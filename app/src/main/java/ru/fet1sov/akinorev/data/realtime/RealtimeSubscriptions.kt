package ru.fet1sov.akinorev.data.realtime

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class SubscriptionMessage(
    val type: String,
    val usernames: List<String>,
)

fun watchPresenceMessage(usernames: List<String>): String =
    subscriptionMessage(PresenceWatch, usernames)

fun unwatchPresenceMessage(usernames: List<String>): String =
    subscriptionMessage(PresenceUnwatch, usernames)

fun watchFollowersMessage(usernames: List<String>): String =
    subscriptionMessage(FollowersWatch, usernames)

fun unwatchFollowersMessage(usernames: List<String>): String =
    subscriptionMessage(FollowersUnwatch, usernames)

private fun subscriptionMessage(type: String, usernames: List<String>): String =
    Json.encodeToString(
        SubscriptionMessage.serializer(),
        SubscriptionMessage(type = type, usernames = usernames),
    )

private const val PresenceWatch = "watch_presence"
private const val PresenceUnwatch = "unwatch_presence"
private const val FollowersWatch = "watch_followers"
private const val FollowersUnwatch = "unwatch_followers"
