package ru.fet1sov.akinorev.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Имя пользователя со стилем подписки Acinorev Crystal: сплошной цвет,
 * переливающийся градиент на базе цвета или радуга — как в веб-клиенте.
 */
@Composable
fun StyledName(
    text: String,
    nameColor: String,
    nameAnimation: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    fallbackColor: Color = Color.Unspecified,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    val base = remember(nameColor) { parseHexColor(nameColor) }
    val animation = nameAnimation.lowercase()
    val progress = when {
        animation == AnimationRainbow -> LocalNameAnimation.current?.rainbowProgress
        animation == AnimationGradient -> LocalNameAnimation.current?.gradientProgress
        else -> null
    }

    if (progress == null) {
        Text(
            text = text,
            modifier = modifier,
            color = base ?: fallbackColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    val colors = remember(animation, base) {
        if (animation == AnimationRainbow) RainbowColors else gradientColors(base ?: Color.White)
    }
    val textStyle = remember(fontSize, fontWeight) {
        TextStyle(fontSize = fontSize, fontWeight = fontWeight)
    }
    val measurer = rememberTextMeasurer()
    val width = remember(text, textStyle) {
        measurer.measure(AnnotatedString(text), style = textStyle, maxLines = 1)
            .size.width.toFloat().coerceAtLeast(MinTextWidth)
    }
    val span = width * 2f
    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(-span + span * progress, 0f),
        end = Offset(width + span * progress, 0f),
    )

    Text(
        text = text,
        modifier = modifier,
        style = textStyle.copy(brush = brush),
        maxLines = maxLines,
        overflow = overflow,
    )
}

private fun gradientColors(base: Color): List<Color> = listOf(
    shade(base, -30),
    base,
    shade(base, 30),
    base,
    shade(base, -30),
)

private fun shade(color: Color, percent: Int): Color {
    fun adjust(channel: Float): Float =
        ((channel * 255f) + (percent / 100f) * 255f).coerceIn(0f, 255f) / 255f

    return Color(adjust(color.red), adjust(color.green), adjust(color.blue), color.alpha)
}

internal fun parseHexColor(raw: String): Color? {
    val value = raw.trim().removePrefix("#")
    return when (value.length) {
        3 -> {
            val r = value[0].digitToIntOrNull(16) ?: return null
            val g = value[1].digitToIntOrNull(16) ?: return null
            val b = value[2].digitToIntOrNull(16) ?: return null
            Color(r * 17, g * 17, b * 17)
        }

        6 -> {
            val rgb = value.toLongOrNull(16) ?: return null
            Color(
                red = ((rgb shr 16) and 0xFF).toInt(),
                green = ((rgb shr 8) and 0xFF).toInt(),
                blue = (rgb and 0xFF).toInt(),
            )
        }

        else -> null
    }
}

private const val AnimationGradient = "gradient"
private const val AnimationRainbow = "rainbow"
private const val MinTextWidth = 1f

private val RainbowColors = listOf(
    Color(0xFFFF4D6D),
    Color(0xFFFFD166),
    Color(0xFF06D6A0),
    Color(0xFF4DABF7),
    Color(0xFFB197FC),
    Color(0xFFFF4D6D),
)
