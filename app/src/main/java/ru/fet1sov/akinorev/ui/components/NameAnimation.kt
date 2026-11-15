package ru.fet1sov.akinorev.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
class NameAnimation internal constructor(
    private val gradient: State<Float>,
    private val rainbow: State<Float>,
    private val profile: State<Float>,
) {
    val gradientProgress: Float get() = gradient.value
    val rainbowProgress: Float get() = rainbow.value
    val profileProgress: Float get() = profile.value
}

internal val LocalNameAnimation = staticCompositionLocalOf<NameAnimation?> { null }

@Composable
internal fun rememberNameAnimation(enabled: Boolean): NameAnimation? {
    if (!enabled) return null

    val transition = rememberInfiniteTransition(label = "name-style")
    val gradient = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = NameGradientDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "name-gradient",
    )
    val rainbow = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = NameRainbowDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "name-rainbow",
    )
    val profile = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = NameProfileDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "name-profile",
    )

    return remember(transition) { NameAnimation(gradient, rainbow, profile) }
}

internal const val NameGradientDurationMillis = 4000
internal const val NameRainbowDurationMillis = 5000
internal const val NameProfileDurationMillis = 3200
