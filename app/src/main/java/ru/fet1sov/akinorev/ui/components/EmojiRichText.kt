package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Текст с маркерами смайликов (`:name:`) — как useEmojiHtml в веб-клиенте.
 * Неизвестные маркеры остаются обычным текстом до загрузки URL смайлика.
 */
@Composable
fun EmojiRichText(
    text: String,
    emojiUrls: Map<String, String>,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    emojiSize: TextUnit = 13.sp,
) {
    val rendered = remember(text, emojiUrls, emojiSize, maxLines) {
        buildEmojiText(text = text, emojiUrls = emojiUrls, emojiSize = emojiSize)
    }

    if (rendered.inlineContent.isEmpty()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    Text(
        text = rendered.text,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        inlineContent = rendered.inlineContent,
    )
}

private class EmojiRenderedText(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

private fun buildEmojiText(
    text: String,
    emojiUrls: Map<String, String>,
    emojiSize: TextUnit,
): EmojiRenderedText {
    if (text.isBlank() || emojiUrls.isEmpty()) {
        return EmojiRenderedText(AnnotatedString(text), emptyMap())
    }

    val builder = AnnotatedString.Builder()
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    var lastIndex = 0
    var counter = 0

    EmojiMarker.findAll(text).forEach { match ->
        val name = match.groupValues[2].lowercase()
        val url = emojiUrls[name] ?: return@forEach
        val lead = match.groupValues[1]

        builder.append(text.substring(lastIndex, match.range.first))
        if (lead.isNotEmpty()) builder.append(lead)

        val placeholder = "emoji_${counter++}"
        inlineContent[placeholder] = InlineTextContent(
            Placeholder(
                width = emojiSize,
                height = emojiSize,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            RemoteImage(
                url = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
            )
        }
        builder.appendInlineContent(placeholder, EmojiPlaceholder)

        lastIndex = match.range.last + 1
    }

    builder.append(text.substring(lastIndex))
    return EmojiRenderedText(builder.toAnnotatedString(), inlineContent)
}

private const val EmojiPlaceholder = "\uFFFC"
private val EmojiMarker = Regex("(^|\\s):([a-zA-Z0-9_]{1,32}):(?=\$|\\s|[.,!?])")
