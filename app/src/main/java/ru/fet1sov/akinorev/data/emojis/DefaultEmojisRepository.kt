package ru.fet1sov.akinorev.data.emojis

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.CdnApi
import ru.fet1sov.akinorev.data.remote.dto.EmojiDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiListDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiUpdateRequest
import ru.fet1sov.akinorev.model.Emoji
import ru.fet1sov.akinorev.model.EmojiMetadata
import ru.fet1sov.akinorev.model.EmojiPage
import ru.fet1sov.akinorev.model.emojiMimeFromUrl
import java.time.Instant
import java.time.OffsetDateTime

class DefaultEmojisRepository(
    private val api: AcinorevApi,
    private val cdnApi: CdnApi,
    private val client: ApiClient,
) : EmojisRepository {
    override suspend fun fetchPage(page: Int, limit: Int): AuthResult<EmojiPage> =
        when (val result = client.execute { api.getEmojis(page = page, limit = limit) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }

    override suspend fun upload(file: EmojiUploadFile, metadata: EmojiMetadata): AuthResult<Emoji> {
        val uploadedUrl = when (val result = client.execute { cdnApi.uploadFile(file.asPart()) }) {
            is AuthResult.Success -> result.value.url
            is AuthResult.Failure -> return result
        }
        if (uploadedUrl.isBlank()) return AuthResult.Failure(AuthError.Unexpected)

        val body = EmojiRegisterRequest(
            url = uploadedUrl,
            mime = emojiMimeFromUrl(uploadedUrl, file.mimeType.ifBlank { "image/webp" }),
            size = file.bytes.size.toLong(),
            name = metadata.name,
            title = metadata.title,
            description = metadata.description,
        )
        return when (val result = client.execute { api.createEmoji(body) }) {
            is AuthResult.Success -> result.value.emoji.toEmojiResult()
            is AuthResult.Failure -> result
        }
    }

    override suspend fun update(id: String, metadata: EmojiMetadata): AuthResult<Emoji> {
        val body = EmojiUpdateRequest(
            name = metadata.name,
            title = metadata.title,
            description = metadata.description,
        )
        return when (val result = client.execute { api.updateEmoji(id = id, body = body) }) {
            is AuthResult.Success -> result.value.emoji.toEmojiResult()
            is AuthResult.Failure -> result
        }
    }

    override suspend fun delete(id: String): AuthResult<String> =
        when (val result = client.execute { api.deleteEmoji(id = id) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.emojiId.ifBlank { id })
            is AuthResult.Failure -> result
        }

    private fun EmojiDto?.toEmojiResult(): AuthResult<Emoji> {
        val emoji = this?.toDomain() ?: return AuthResult.Failure(AuthError.Unexpected)
        return AuthResult.Success(emoji)
    }
}

private fun EmojiUploadFile.asPart(): MultipartBody.Part {
    val mediaType = mimeType.ifBlank { "image/png" }.toMediaTypeOrNull()
    return MultipartBody.Part.createFormData(
        name = "file",
        filename = fileName.ifBlank { "emoji" },
        body = bytes.toRequestBody(mediaType),
    )
}

internal fun EmojiListDto.toDomain(): EmojiPage = EmojiPage(
    items = emojis.mapNotNull(EmojiDto::toDomain),
    total = total,
    page = page,
    totalPages = totalPages.coerceAtLeast(1),
)

internal fun EmojiDto.toDomain(): Emoji? {
    val id = id.takeIf { it.isNotBlank() } ?: return null
    val safeName = name.takeIf { it.isNotBlank() } ?: return null

    return Emoji(
        id = id,
        ownerId = ownerId,
        ownerUsername = ownerUsername.orEmpty(),
        ownerAvatar = ownerAvatar.orEmpty(),
        ownerIsSubscribed = ownerIsSubscribed,
        ownerNameColor = ownerNameColor.orEmpty(),
        ownerNameAnimation = ownerNameAnimation.orEmpty(),
        name = safeName,
        title = title.orEmpty(),
        description = description.orEmpty(),
        url = url,
        mime = mime,
        size = size,
        createdAtMillis = parseEmojiTimestamp(createdAt),
        isMine = mine,
    )
}

internal fun parseEmojiTimestamp(value: String?): Long {
    if (value.isNullOrBlank()) return System.currentTimeMillis()
    return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: System.currentTimeMillis()
}
