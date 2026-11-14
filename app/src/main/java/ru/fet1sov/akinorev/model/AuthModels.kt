package ru.fet1sov.akinorev.model

data class AuthUser(
    val id: String,
    val username: String,
    val email: String,
    val bio: String,
    val wallet: Double,
    val avatar: String,
    val dhPubKey: String,
    val isSubscribed: Boolean,
    val subscriptionExpiresAt: String?,
    val nameColor: String,
    val nameAnimation: String,
    val locale: String,
    val createdAt: String?,
) {
    val displayName: String
        get() = username.ifBlank { email.substringBefore('@') }
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long,
)

data class KeyCustody(
    val deposited: Boolean,
    val privateKey: String?,
    val dhPubKey: String?,
)
