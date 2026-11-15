package ru.fet1sov.akinorev.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

private val RainbowColors = listOf(
    Color(0xFFFF4D6D),
    Color(0xFFFFD166),
    Color(0xFF06D6A0),
    Color(0xFF4DABF7),
    Color(0xFFB197FC),
    Color(0xFFFF4D6D),
)

@Composable
fun ProfileName(
    username: String,
    nameColor: String,
    nameAnimation: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Black,
) {
    val palette = navigationPalette()
    val base = remember(nameColor) { parseHexColor(nameColor, palette.textPrimary) }
    val animation = nameAnimation.trim().lowercase()
    val animationState = LocalNameAnimation.current

    val style = when (animation) {
        "rainbow" -> TextStyle(
            brush = animatedBrush(RainbowColors, animationState?.profileProgress),
            fontSize = fontSize,
            fontWeight = fontWeight,
        )

        "gradient" -> TextStyle(
            brush = animatedBrush(
                listOf(shade(base, -30), base, shade(base, 30)),
                animationState?.profileProgress,
            ),
            fontSize = fontSize,
            fontWeight = fontWeight,
        )

        else -> TextStyle(
            color = base,
            fontSize = fontSize,
            fontWeight = fontWeight,
        )
    }

    Text(
        text = username,
        style = style,
        maxLines = 1,
        modifier = modifier,
    )
}

private fun animatedBrush(colors: List<Color>, progress: Float?): Brush {
    if (progress == null) return Brush.linearGradient(colors)

    val shift = progress * 480f
    return Brush.linearGradient(
        colors = colors,
        start = Offset(shift - 240f, 0f),
        end = Offset(shift + 240f, 0f),
    )
}

private fun parseHexColor(value: String, fallback: Color): Color {
    val hex = value.trim().removePrefix("#")
    val normalized = when (hex.length) {
        3 -> hex.map { "$it$it" }.joinToString("")
        6 -> hex
        else -> return fallback
    }
    val argb = normalized.toLongOrNull(16)?.let { 0xFF000000L or it } ?: return fallback
    return Color(argb)
}

private fun shade(color: Color, percent: Int): Color {
    val delta = percent / 100f
    fun channel(value: Float): Float = (value + delta).coerceIn(0f, 1f)
    return Color(
        red = channel(color.red),
        green = channel(color.green),
        blue = channel(color.blue),
        alpha = color.alpha,
    )
}
