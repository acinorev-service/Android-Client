package ru.fet1sov.akinorev.testing

import ru.fet1sov.akinorev.data.emojis.EmojiUploadFile
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.Emoji
import ru.fet1sov.akinorev.model.EmojiMetadata
import ru.fet1sov.akinorev.model.EmojiPage

class FakeEmojisRepository : EmojisRepository {
    val pages: MutableMap<Int, EmojiPage> = mutableMapOf()
    val requestedPages: MutableList<Int> = mutableListOf()
    val uploadedMetadata: MutableList<EmojiMetadata> = mutableListOf()
    val deletedIds: MutableList<String> = mutableListOf()

    var defaultPageSize: Int = 0
        private set

    var fetchResult: AuthResult<EmojiPage>? = null
    var uploadResult: AuthResult<Emoji> = AuthResult.Failure(AuthError.Unexpected)
    var deleteResult: AuthResult<String> = AuthResult.Success("")
    var resolvedUrls: Map<String, String> = emptyMap()

    override suspend fun fetchPage(page: Int, limit: Int): AuthResult<EmojiPage> {
        requestedPages += page
        defaultPageSize = limit
        fetchResult?.let { return it }
        val stored = pages[page] ?: return AuthResult.Failure(AuthError.Unexpected)
        return AuthResult.Success(stored)
    }

    override suspend fun upload(file: EmojiUploadFile, metadata: EmojiMetadata): AuthResult<Emoji> {
        uploadedMetadata += metadata
        return uploadResult
    }

    override suspend fun update(id: String, metadata: EmojiMetadata): AuthResult<Emoji> =
        uploadResult

    override suspend fun delete(id: String): AuthResult<String> {
        deletedIds += id
        return deleteResult
    }

    override suspend fun resolveUrls(names: Collection<String>): Map<String, String> {
        val requested = names.map(String::lowercase).toSet()
        return resolvedUrls.filterKeys { it.lowercase() in requested }
    }
}

fun fakeEmoji(
    id: String = "emoji-1",
    name: String = "pepe",
    mine: Boolean = false,
    ownerUsername: String = "fet1sov",
    url: String = "http://localhost:8090/f/$id.webp",
): Emoji = Emoji(
    id = id,
    ownerId = "user-1",
    ownerUsername = ownerUsername,
    ownerAvatar = "",
    ownerIsSubscribed = false,
    ownerNameColor = "",
    ownerNameAnimation = "",
    name = name,
    title = "",
    description = "",
    url = url,
    mime = "image/webp",
    size = 1024,
    createdAtMillis = 1_760_000_000_000,
    isMine = mine,
)

fun fakeEmojiPage(
    items: List<Emoji> = listOf(fakeEmoji()),
    page: Int = 1,
    totalPages: Int = 1,
    total: Int = items.size,
): EmojiPage = EmojiPage(
    items = items,
    total = total,
    page = page,
    totalPages = totalPages,
)
