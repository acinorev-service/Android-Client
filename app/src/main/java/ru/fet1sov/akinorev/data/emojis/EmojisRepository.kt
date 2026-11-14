package ru.fet1sov.akinorev.data.emojis

import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.Emoji
import ru.fet1sov.akinorev.model.EmojiMetadata
import ru.fet1sov.akinorev.model.EmojiPage

class EmojiUploadFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)

interface EmojisRepository {
    suspend fun fetchPage(page: Int, limit: Int): AuthResult<EmojiPage>

    suspend fun upload(file: EmojiUploadFile, metadata: EmojiMetadata): AuthResult<Emoji>

    suspend fun update(id: String, metadata: EmojiMetadata): AuthResult<Emoji>

    suspend fun delete(id: String): AuthResult<String>

    suspend fun resolveUrls(names: Collection<String>): Map<String, String>
}
