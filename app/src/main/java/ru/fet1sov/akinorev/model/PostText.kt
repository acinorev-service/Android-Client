package ru.fet1sov.akinorev.model

val PostLocales: List<String> = listOf("ru", "en")

sealed interface PostContentPart {
    data class Text(val value: String) : PostContentPart

    data class Images(val images: List<PostImage>) : PostContentPart
}

fun postLocaleTag(languageTag: String?): String {
    val tag = languageTag.orEmpty().lowercase().take(2)
    return if (tag in PostLocales) tag else "en"
}

fun localizedPostText(content: Map<String, String>?, languageTag: String?): String {
    if (content.isNullOrEmpty()) return ""
    val preferred = postLocaleTag(languageTag)
    content[preferred]?.takeIf { it.isNotBlank() }?.let { return it }
    for (locale in PostLocales) {
        content[locale]?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return content.values.firstOrNull { it.isNotBlank() }.orEmpty()
}

fun filledPostContent(content: Map<String, String>): Map<String, String> = content
    .filterKeys { it in PostLocales }
    .mapValues { (_, value) -> value.trim() }
    .filterValues { it.isNotEmpty() }

fun collectEmojiNames(text: String): List<String> =
    EmojiMarker.findAll(text).map { it.groupValues[2].lowercase() }.distinct().toList()

fun splitPostContent(
    source: String,
    images: List<PostImage>,
): List<PostContentPart> {
    val tokens = mutableListOf<PostContentPart>()
    var last = 0
    var hasMarker = false
    imageMarkers(source).forEach { marker ->
        hasMarker = true
        if (marker.start > last) {
            tokens += PostContentPart.Text(source.substring(last, marker.start))
        }
        val image = images.getOrNull(marker.index)
        if (image != null) {
            tokens += PostContentPart.Images(listOf(image))
        }
        last = marker.end
    }
    if (last < source.length) {
        tokens += PostContentPart.Text(source.substring(last))
    }
    val trailing = if (hasMarker) emptyList() else images
    if (trailing.isNotEmpty()) tokens += PostContentPart.Images(trailing)
    if (tokens.isEmpty() && source.isNotBlank() && !hasMarker) {
        tokens += PostContentPart.Text(source)
    }
    return groupConsecutiveImages(tokens)
}

fun rebuildImageMarkers(
    content: Map<String, String>,
    imageCount: Int,
): Map<String, String> = content.mapValues { (_, text) ->
    rebuildImageMarkers(text, imageCount)
}

private fun rebuildImageMarkers(text: String, imageCount: Int): String {
    val builder = StringBuilder(text.length)
    var last = 0
    var index = 0
    imageMarkers(text).forEach { marker ->
        builder.append(text, last, marker.start)
        if (index < imageCount) {
            builder.append(MarkerPrefix).append(index).append(MarkerSuffix)
        }
        index += 1
        last = marker.end
    }
    builder.append(text, last, text.length)
    return builder.toString()
}

fun appendImageMarkers(
    text: String,
    firstIndex: Int,
    count: Int,
): String {
    if (count <= 0) return text
    val block = (0 until count).joinToString("\n") { offset -> "[[img:${firstIndex + offset}]]" }
    if (text.isBlank()) return block
    val prefix = if (text.endsWith("\n")) "" else "\n"
    return text + prefix + block
}

private fun groupConsecutiveImages(parts: List<PostContentPart>): List<PostContentPart> {
    val result = mutableListOf<PostContentPart>()
    val run = mutableListOf<PostImage>()

    fun flush() {
        if (run.isNotEmpty()) {
            result += PostContentPart.Images(run.toList())
            run.clear()
        }
    }

    parts.forEach { part ->
        when (part) {
            is PostContentPart.Images -> run += part.images
            is PostContentPart.Text -> {
                flush()
                result += part
            }
        }
    }
    flush()
    return result
}

internal val EmojiMarker = Regex("(^|\\s):([a-zA-Z0-9_]{1,32}):(?=\$|\\s|[.,!?])")

private class ImageMarkerMatch(
    val start: Int,
    val end: Int,
    val index: Int,
)

private fun imageMarkers(source: String): List<ImageMarkerMatch> {
    val markers = mutableListOf<ImageMarkerMatch>()
    var searchFrom = 0

    while (true) {
        val start = source.indexOf(MarkerPrefix, searchFrom)
        if (start < 0) break

        val digitsStart = start + MarkerPrefix.length
        var cursor = digitsStart
        while (cursor < source.length && source[cursor].isDigit()) cursor += 1

        val index = if (cursor > digitsStart && source.startsWith(MarkerSuffix, cursor)) {
            source.substring(digitsStart, cursor).toIntOrNull()
        } else {
            null
        }

        if (index != null) {
            val end = cursor + MarkerSuffix.length
            markers += ImageMarkerMatch(start = start, end = end, index = index)
            searchFrom = end
        } else {
            searchFrom = start + 1
        }
    }

    return markers
}

private const val MarkerPrefix = "[[img:"
private const val MarkerSuffix = "]]"
