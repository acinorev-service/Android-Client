package ru.fet1sov.akinorev.model

data class Emoji(
    val id: String,
    val ownerId: String,
    val ownerUsername: String,
    val ownerAvatar: String,
    val ownerIsSubscribed: Boolean,
    val ownerNameColor: String,
    val ownerNameAnimation: String,
    val name: String,
    val title: String,
    val description: String,
    val url: String,
    val mime: String,
    val size: Long,
    val createdAtMillis: Long,
    val isMine: Boolean,
)

data class EmojiPage(
    val items: List<Emoji>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
)

data class EmojiMetadata(
    val name: String,
    val title: String = "",
    val description: String = "",
)

const val MaxEmojiNameLength = 32
const val MaxEmojiTitleLength = 64
const val MaxEmojiDescriptionLength = 300
const val MaxEmojiFileBytes = 5 * 1024 * 1024

private val EmojiNameRegex = Regex("[a-z0-9_]{1,$MaxEmojiNameLength}")
private val EmojiImageExtensions = setOf("gif", "png", "webp", "jpg", "jpeg", "jpe", "jfif")
private val EmojiImageMimeTypes = setOf("image/gif", "image/png", "image/webp", "image/jpeg")

fun normalizeEmojiName(raw: String): String {
    val builder = StringBuilder(raw.length)
    raw.forEach { character ->
        when {
            character in 'a'..'z' -> builder.append(character)
            character in 'A'..'Z' -> builder.append(character.lowercaseChar())
            character in '0'..'9' -> builder.append(character)
            character == '_' -> builder.append(character)
        }
    }
    return builder.toString()
}

fun isValidEmojiName(raw: String): Boolean = EmojiNameRegex.matches(normalizeEmojiName(raw))

fun emojiMarker(name: String): String = ":$name:"

fun isSupportedEmojiMime(mime: String): Boolean =
    EmojiImageMimeTypes.contains(mime.trim().lowercase())

fun isSupportedEmojiFileExtension(fileName: String): Boolean {
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
    return EmojiImageExtensions.contains(extension)
}

fun emojiMimeFromUrl(url: String, fallback: String = "image/webp"): String {
    val path = url.substringBefore('?').lowercase()
    return when {
        path.endsWith(".gif") -> "image/gif"
        path.endsWith(".webp") -> "image/webp"
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") || path.endsWith(".jpeg") ||
            path.endsWith(".jpe") || path.endsWith(".jfif") -> "image/jpeg"

        else -> fallback
    }
}
