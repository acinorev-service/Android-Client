package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommunityRichText(
    text: String,
    emojiUrls: Map<String, String>,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    emojiSize: TextUnit = 15.sp,
) {
    val inlineContent = remember(text, emojiUrls, emojiSize) {
        mutableMapOf<String, InlineTextContent>()
    }
    val annotated = remember(text, emojiUrls, emojiSize, style) {
        buildCommunityText(
            source = text,
            emojiUrls = emojiUrls,
            emojiSize = emojiSize,
            inlineContent = inlineContent,
            accent = LinkAccent,
        )
    }

    if (inlineContent.isEmpty()) {
        Text(
            text = annotated,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
        )
    } else {
        Text(
            text = annotated,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
            inlineContent = inlineContent,
        )
    }
}

private fun buildCommunityText(
    source: String,
    emojiUrls: Map<String, String>,
    emojiSize: TextUnit,
    inlineContent: MutableMap<String, InlineTextContent>,
    accent: Color,
): AnnotatedString = buildAnnotatedString {
    val context = InlineContext(
        builder = this,
        emojiUrls = emojiUrls,
        inlineContent = inlineContent,
        emojiSize = emojiSize,
        accent = accent,
    )
    appendBlocks(source, context)
}

private class InlineContext(
    val builder: AnnotatedString.Builder,
    val emojiUrls: Map<String, String>,
    val inlineContent: MutableMap<String, InlineTextContent>,
    val emojiSize: TextUnit,
    val accent: Color,
)

private fun AnnotatedString.Builder.appendBlocks(source: String, context: InlineContext) {
    val lines = source.replace("\r\n", "\n").split('\n')
    var inCode = false
    var first = true

    lines.forEach { rawLine ->
        if (!first) append('\n')
        first = false

        if (rawLine.trimStart().startsWith("```")) {
            inCode = !inCode
            return@forEach
        }

        if (inCode) {
            pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = CodeBackground))
            append(rawLine)
            pop()
            return@forEach
        }

        val heading = HeadingRegex.matchEntire(rawLine)
        if (heading != null) {
            pushStyle(SpanStyle(fontWeight = FontWeight.Black))
            context.appendInline(heading.groupValues[2], 0, heading.groupValues[2].length)
            pop()
            return@forEach
        }

        val listItem = ListItemRegex.matchEntire(rawLine)
        if (listItem != null) {
            append("• ")
            val body = listItem.groupValues[2]
            context.appendInline(body, 0, body.length)
            return@forEach
        }

        val quote = QuoteRegex.matchEntire(rawLine)
        if (quote != null) {
            append("│ ")
            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
            val body = quote.groupValues[1]
            context.appendInline(body, 0, body.length)
            pop()
            return@forEach
        }

        if (rawLine.trim() in HorizontalRules) {
            append("———")
            return@forEach
        }

        context.appendInline(rawLine, 0, rawLine.length)
    }
}

private fun InlineContext.appendInline(
    text: String,
    start: Int,
    end: Int,
) {
    var index = start
    while (index < end) {
        when {
            text.startsWith("**", index) -> {
                val close = text.indexOf("**", index + 2)
                if (close in (index + 2) until end) {
                    builder.pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    appendInline(text, index + 2, close)
                    builder.pop()
                    index = close + 2
                } else {
                    builder.append(text[index])
                    index += 1
                }
            }

            text[index] == '*' -> {
                val close = text.indexOf('*', index + 1)
                if (close in (index + 1) until end) {
                    builder.pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    appendInline(text, index + 1, close)
                    builder.pop()
                    index = close + 1
                } else {
                    builder.append(text[index])
                    index += 1
                }
            }

            text[index] == '`' -> {
                val close = text.indexOf('`', index + 1)
                if (close in (index + 1) until end) {
                    builder.pushStyle(
                        SpanStyle(fontFamily = FontFamily.Monospace, background = CodeBackground),
                    )
                    builder.append(text.substring(index + 1, close))
                    builder.pop()
                    index = close + 1
                } else {
                    builder.append(text[index])
                    index += 1
                }
            }

            text[index] == '[' -> {
                val labelEnd = text.indexOf(']', index + 1)
                val urlStart = labelEnd + 1
                val urlEnd = if (labelEnd in (index + 1) until end && urlStart < end && text[urlStart] == '(') {
                    text.indexOf(')', urlStart + 1)
                } else {
                    -1
                }
                if (labelEnd in (index + 1) until end && urlEnd in (urlStart + 1) until end) {
                    val label = text.substring(index + 1, labelEnd)
                    val url = text.substring(urlStart + 1, urlEnd)
                    if (url.startsWith("https://") || url.startsWith("http://")) {
                        val styles = TextLinkStyles(
                            style = SpanStyle(
                                color = accent,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                            ),
                        )
                        builder.withLink(LinkAnnotation.Url(url, styles)) {
                            append(label)
                        }
                    } else {
                        builder.append(label)
                    }
                    index = urlEnd + 1
                } else {
                    builder.append(text[index])
                    index += 1
                }
            }

            text[index] == ':' && isBoundaryBefore(text, index, start) -> {
                val emojiEnd = matchEmoji(text, index, end)
                if (emojiEnd != null) {
                    val name = text.substring(index + 1, emojiEnd - 1).lowercase()
                    val url = emojiUrls[name]
                    if (url != null) {
                        val key = "emoji_${inlineContent.size}"
                        inlineContent[key] = InlineTextContent(
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
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                            )
                        }
                        builder.appendInlineContent(key, EmojiPlaceholder)
                        index = emojiEnd
                    } else {
                        builder.append(text[index])
                        index += 1
                    }
                } else {
                    builder.append(text[index])
                    index += 1
                }
            }

            else -> {
                builder.append(text[index])
                index += 1
            }
        }
    }
}

private fun isBoundaryBefore(text: String, index: Int, start: Int): Boolean {
    if (index == start) return true
    val previous = text[index - 1]
    return previous.isWhitespace() || previous == '(' || previous == '>'
}

private fun matchEmoji(text: String, index: Int, end: Int): Int? {
    val maxName = 32
    var cursor = index + 1
    while (cursor < end && cursor - index - 1 <= maxName) {
        val char = text[cursor]
        when {
            char == ':' -> {
                if (cursor == index + 1) return null
                val after = if (cursor + 1 < end) text[cursor + 1] else ' '
                if (after.isWhitespace() || after in EmojiTrailingPunctuation) {
                    return cursor + 1
                }
                return null
            }

            char.isLetterOrDigit() || char == '_' -> cursor += 1
            else -> return null
        }
    }
    return null
}

private val HeadingRegex = Regex("^(#{1,6})\\s+(.*)$")
private val ListItemRegex = Regex("^(\\s*)[-*+]\\s+(.*)$")
private val QuoteRegex = Regex("^>\\s?(.*)$")
private val HorizontalRules = setOf("---", "***", "___")
private val EmojiTrailingPunctuation = setOf('.', ',', '!', '?')
private const val EmojiPlaceholder = "\uFFFC"
private val CodeBackground = Color(0x1F94A3B8)
private val LinkAccent = Color(0xFF0EA800)
