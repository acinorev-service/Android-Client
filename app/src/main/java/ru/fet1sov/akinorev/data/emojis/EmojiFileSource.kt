package ru.fet1sov.akinorev.data.emojis

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.fet1sov.akinorev.model.MaxEmojiFileBytes
import ru.fet1sov.akinorev.model.emojiMimeFromUrl
import ru.fet1sov.akinorev.model.isSupportedEmojiFileExtension
import ru.fet1sov.akinorev.model.isSupportedEmojiMime
import java.io.ByteArrayOutputStream
import java.io.InputStream

sealed interface EmojiFileResult {
    data class Ready(val file: EmojiUploadFile) : EmojiFileResult

    data object Unsupported : EmojiFileResult

    data object TooLarge : EmojiFileResult

    data object Unreadable : EmojiFileResult
}

interface EmojiFileSource {
    suspend fun read(uri: String): EmojiFileResult
}

class ContentResolverEmojiFileSource(
    context: Context,
) : EmojiFileSource {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun read(uri: String): EmojiFileResult = withContext(Dispatchers.IO) {
        if (uri.isBlank()) return@withContext EmojiFileResult.Unreadable

        val parsed = runCatching { uri.toUri() }.getOrNull()
            ?: return@withContext EmojiFileResult.Unreadable

        val declaredMime = runCatching { resolver.getType(parsed) }.getOrNull().orEmpty().lowercase()
        val displayName = queryDisplayName(parsed) ?: parsed.lastPathSegment.orEmpty()

        if (!isSupportedEmojiMime(declaredMime) && !isSupportedEmojiFileExtension(displayName)) {
            return@withContext EmojiFileResult.Unsupported
        }

        val bytes = runCatching {
            resolver.openInputStream(parsed)?.use { it.readBytesCapped(MaxEmojiFileBytes) }
        }.getOrNull() ?: return@withContext EmojiFileResult.Unreadable

        if (bytes.isEmpty()) return@withContext EmojiFileResult.Unreadable

        val mime = if (isSupportedEmojiMime(declaredMime)) {
            declaredMime
        } else {
            emojiMimeFromUrl(displayName, "image/png")
        }

        EmojiFileResult.Ready(
            EmojiUploadFile(
                fileName = displayName.withExtensionFor(mime),
                mimeType = mime,
                bytes = bytes,
            ),
        )
    }

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index < 0) null else cursor.getString(index)
        }
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

private fun String.withExtensionFor(mime: String): String {
    val name = substringAfterLast('/', missingDelimiterValue = this).trim()
    if (isSupportedEmojiFileExtension(name)) return name
    val extension = when (mime) {
        "image/gif" -> "gif"
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        else -> "webp"
    }
    return "$name.$extension"
}

private fun InputStream.readBytesCapped(maxBytes: Int): ByteArray? {
    val output = ByteArrayOutputStream(minOf(maxBytes, DefaultBufferSize))
    val buffer = ByteArray(DefaultBufferSize)
    var total = 0

    while (true) {
        val read = read(buffer)
        if (read <= 0) break
        total += read
        if (total > maxBytes) return null
        output.write(buffer, 0, read)
    }

    return output.toByteArray()
}

private const val DefaultBufferSize = 8 * 1024
