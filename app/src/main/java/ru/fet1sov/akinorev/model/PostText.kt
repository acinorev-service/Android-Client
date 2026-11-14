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
    var usedImages = 0
    ImageMarker.findAll(source).forEach { match ->
        val index = match.groupValues[1].toIntOrNull() ?: return@forEach
        val image = images.getOrNull(index) ?: return@forEach
        if (match.range.first > last) {
            tokens += PostContentPart.Text(source.substring(last, match.range.first))
        }
        tokens += PostContentPart.Images(listOf(image))
        usedImages += 1
        last = match.range.last + 1
    }
    if (last < source.length) {
        tokens += PostContentPart.Text(source.substring(last))
    }
    val referenced = source.contains(ImageMarker)
    val trailing = if (referenced) images.filterIndexed { index, _ -> index >= usedImages } else emptyList()
    if (trailing.isNotEmpty()) tokens += PostContentPart.Images(trailing)
    if (tokens.isEmpty() && source.isNotBlank()) tokens += PostContentPart.Text(source)
    return groupConsecutiveImages(tokens)
}

fun rebuildImageMarkers(
    content: Map<String, String>,
    imageCount: Int,
): Map<String, String> = content.mapValues { (_, text) ->
    var index = 0
    ImageMarker.replace(text) { match ->
        val current = index++
        if (current < imageCount) "[[img:$current]]" else ""
    }
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
private val ImageMarker = Regex("\\[\\[img:(\\d+)]]")
