package ru.fet1sov.akinorev.data.auth

import kotlinx.serialization.Serializable
import ru.fet1sov.akinorev.data.remote.dto.TokenPairDto
import ru.fet1sov.akinorev.data.remote.dto.UserDto
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.TokenPair

@Serializable
data class StoredSession(
    val tokens: StoredTokens,
    val user: StoredUser,
)

@Serializable
data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 0,
)

@Serializable
data class StoredUser(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val wallet: Double = 0.0,
    val avatar: String = "",
    val dhPubKey: String = "",
    val isSubscribed: Boolean = false,
    val subscriptionExpiresAt: String? = null,
    val nameColor: String = "",
    val nameAnimation: String = "",
    val locale: String = "",
    val createdAt: String? = null,
)

internal fun UserDto.asStored(): StoredUser =
    StoredUser(
        id = id,
        username = username,
        email = email,
        bio = bio,
        wallet = wallet,
        avatar = avatar,
        dhPubKey = dhPubKey,
        isSubscribed = isSubscribed,
        subscriptionExpiresAt = subscriptionExpiresAt,
        nameColor = nameColor,
        nameAnimation = nameAnimation,
        locale = locale,
        createdAt = createdAt,
    )

internal fun StoredUser.asDomain(): AuthUser =
    AuthUser(
        id = id,
        username = username,
        email = email,
        bio = bio,
        wallet = wallet,
        avatar = avatar,
        dhPubKey = dhPubKey,
        isSubscribed = isSubscribed,
        subscriptionExpiresAt = subscriptionExpiresAt,
        nameColor = nameColor,
        nameAnimation = nameAnimation,
        locale = locale,
        createdAt = createdAt,
    )

internal fun UserDto.asDomain(): AuthUser = asStored().asDomain()

internal fun TokenPairDto.asStored(): StoredTokens =
    StoredTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
    )

internal fun TokenPairDto.asDomain(): TokenPair =
    TokenPair(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
    )
