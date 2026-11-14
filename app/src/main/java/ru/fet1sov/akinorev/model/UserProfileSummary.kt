package ru.fet1sov.akinorev.model

data class UserProfileSummary(
    val id: String,
    val username: String,
    val wallet: Double,
    val avatar: String,
    val isSubscribed: Boolean,
    val subscriptionExpiresAt: String?,
    val nameColor: String,
    val nameAnimation: String,
)

fun AuthUser.toProfileSummary(): UserProfileSummary = UserProfileSummary(
    id = id,
    username = displayName,
    wallet = wallet,
    avatar = avatar,
    isSubscribed = isSubscribed,
    subscriptionExpiresAt = subscriptionExpiresAt,
    nameColor = nameColor,
    nameAnimation = nameAnimation,
)
