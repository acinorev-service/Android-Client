package ru.fet1sov.akinorev.data.files

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class ImageUploadFile(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
)

sealed interface ImageFileResult {
    data class Ready(val file: ImageUploadFile) : ImageFileResult

    data object Unsupported : ImageFileResult

    data object TooLarge : ImageFileResult

    data object Unreadable : ImageFileResult
}

interface ImageFileSource {
    suspend fun read(uri: String): ImageFileResult
}

class ContentResolverImageFileSource(
    context: Context,
    private val maxBytes: Int = MaxImageBytes,
) : ImageFileSource {
    private val resolver: ContentResolver = context.applicationContext.contentResolver

    override suspend fun read(uri: String): ImageFileResult = withContext(Dispatchers.IO) {
        if (uri.isBlank()) return@withContext ImageFileResult.Unreadable

        val parsed = runCatching { uri.toUri() }.getOrNull()
            ?: return@withContext ImageFileResult.Unreadable

        val declaredMime = runCatching { resolver.getType(parsed) }.getOrNull().orEmpty().lowercase()
        val displayName = queryDisplayName(parsed) ?: parsed.lastPathSegment.orEmpty()
        val mime = resolveMime(declaredMime, displayName)

        if (mime == null) return@withContext ImageFileResult.Unsupported

        val bytes = runCatching {
            resolver.openInputStream(parsed)?.use { it.readBytesCapped(maxBytes) }
        }.getOrNull() ?: return@withContext ImageFileResult.Unreadable

        if (bytes.isEmpty()) return@withContext ImageFileResult.Unreadable

        ImageFileResult.Ready(
            ImageUploadFile(
                fileName = displayName.ifBlank { "image.${extensionFor(mime)}" },
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

    private companion object {
        fun resolveMime(declared: String, name: String): String? {
            if (declared in SupportedMimes) return declared
            val extension = name.substringAfterLast('.', missingDelimiterValue = "").lowercase()
            return when (extension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                "gif" -> "image/gif"
                else -> null
            }
        }

        fun extensionFor(mime: String): String = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "jpg"
        }
    }
}

private fun InputStream.readBytesCapped(maxBytes: Int): ByteArray? {
    val output = ByteArrayOutputStream(minOf(maxBytes, BufferSize))
    val buffer = ByteArray(BufferSize)
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

private const val MaxImageBytes = 10 * 1024 * 1024
private const val BufferSize = 16 * 1024
private val SupportedMimes = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
