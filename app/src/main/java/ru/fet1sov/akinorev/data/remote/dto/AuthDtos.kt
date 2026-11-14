package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    @SerialName("client_message") val clientMessage: String? = null,
    val data: T? = null,
)

@Serializable
data class ApiErrorDto(
    val code: String = "",
    val message: String = "",
)

@Serializable
data class UserDto(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val wallet: Double = 0.0,
    val avatar: String = "",
    @SerialName("dh_pub_key") val dhPubKey: String = "",
    @SerialName("is_subscribed") val isSubscribed: Boolean = false,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null,
    @SerialName("name_color") val nameColor: String = "",
    @SerialName("name_animation") val nameAnimation: String = "",
    val locale: String = "",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("last_seen_device") val lastSeenDevice: String? = null,
    @SerialName("followers_count") val followersCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("equipped_badges") val equippedBadges: List<EquippedBadgeDto>? = null,
)

@Serializable
data class EquippedBadgeDto(
    @SerialName("user_item_id") val userItemId: String = "",
    @SerialName("item_id") val itemId: String = "",
    @SerialName("equipped_order") val equippedOrder: Int = 0,
    val item: BadgeItemDto? = null,
)

@Serializable
data class BadgeItemDto(
    val id: String = "",
    val slug: String = "",
    val name: String = "",
    val type: String = "",
    val rarity: String = "",
    val icon: String? = null,
)

@Serializable
data class TokenPairDto(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("refresh_token") val refreshToken: String = "",
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_in") val expiresIn: Long = 0,
)

@Serializable
data class E2eKeyDto(
    @SerialName("private_key") val privateKey: String = "",
    @SerialName("dh_pub_key") val dhPubKey: String = "",
)

@Serializable
data class AuthPayloadDto(
    val user: UserDto? = null,
    val tokens: TokenPairDto? = null,
    val e2e: E2eKeyDto? = null,
)

@Serializable
data class ProfilePayloadDto(
    val user: UserDto? = null,
)

@Serializable
data class PublicUserPayloadDto(
    val user: UserDto? = null,
)

@Serializable
data class KeyCustodyDto(
    val deposited: Boolean? = null,
    @SerialName("private_key") val privateKey: String? = null,
    @SerialName("dh_pub_key") val dhPubKey: String? = null,
)

@Serializable
data class LocaleDto(
    val locale: String = "",
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class DepositKeyRequest(
    @SerialName("private_key") val privateKey: String,
    @SerialName("dh_pub_key") val dhPubKey: String,
)

@Serializable
data class UpdateLocaleRequest(
    val locale: String,
)

@Serializable
data class UpdateProfileRequest(
    val bio: String? = null,
    val avatar: String? = null,
    val username: String? = null,
    @SerialName("dh_pub_key") val dhPubKey: String? = null,
    @SerialName("name_color") val nameColor: String? = null,
    @SerialName("name_animation") val nameAnimation: String? = null,
)
