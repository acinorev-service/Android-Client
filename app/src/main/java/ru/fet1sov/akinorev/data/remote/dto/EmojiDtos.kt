package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmojiDto(
    val id: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("owner_username") val ownerUsername: String? = null,
    @SerialName("owner_avatar") val ownerAvatar: String? = null,
    @SerialName("owner_is_subscribed") val ownerIsSubscribed: Boolean = false,
    @SerialName("owner_name_color") val ownerNameColor: String? = null,
    @SerialName("owner_name_animation") val ownerNameAnimation: String? = null,
    val name: String = "",
    val title: String? = null,
    val description: String? = null,
    val url: String = "",
    val mime: String = "",
    val size: Long = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val mine: Boolean = false,
)

@Serializable
data class EmojiListDto(
    val emojis: List<EmojiDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 0,
    @SerialName("total_pages") val totalPages: Int = 1,
)

@Serializable
data class EmojiPayloadDto(
    val emoji: EmojiDto? = null,
)

@Serializable
data class EmojiDeletedDto(
    @SerialName("emoji_id") val emojiId: String = "",
)

@Serializable
data class EmojiRegisterRequest(
    val url: String,
    val mime: String,
    val size: Long,
    val name: String,
    val title: String,
    val description: String,
)

@Serializable
data class EmojiUpdateRequest(
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
)

@Serializable
data class CdnUploadDto(
    val url: String = "",
)
