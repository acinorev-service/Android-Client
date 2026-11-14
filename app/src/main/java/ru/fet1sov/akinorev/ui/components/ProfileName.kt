package ru.fet1sov.akinorev.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import ru.fet1sov.akinorev.ui.system.LocalUiAnimationsEnabled

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
    val animationsEnabled = LocalUiAnimationsEnabled.current
    val base = remember(nameColor) { parseHexColor(nameColor, palette.textPrimary) }
    val animation = nameAnimation.trim().lowercase()

    val style = when (animation) {
        "rainbow" -> TextStyle(
            brush = animatedBrush(RainbowColors, animationsEnabled),
            fontSize = fontSize,
            fontWeight = fontWeight,
        )

        "gradient" -> TextStyle(
            brush = animatedBrush(
                listOf(shade(base, -30), base, shade(base, 30)),
                animationsEnabled,
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

@Composable
private fun animatedBrush(colors: List<Color>, animationsEnabled: Boolean): Brush {
    if (!animationsEnabled) return Brush.linearGradient(colors)

    val transition = rememberInfiniteTransition(label = "profileName")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
        ),
        label = "profileNameProgress",
    )

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
