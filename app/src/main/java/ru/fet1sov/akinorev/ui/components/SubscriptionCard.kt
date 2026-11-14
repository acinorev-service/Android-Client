package ru.fet1sov.akinorev.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import ru.fet1sov.akinorev.ui.system.LocalUiAnimationsEnabled

private val CrystalGreen = Color(0xFF0EA800)
private val CrystalGreenDark = Color(0xFF0C8F00)
private val CrystalLabelLight = listOf(Color(0xFF033B00), CrystalGreen, Color(0xFF076B00))
private val CrystalLabelDark = listOf(CrystalGreenDark, CrystalGreen, Color(0xFF054500))

@Composable
fun SubscriptionCard(
    isSubscribed: Boolean,
    expiry: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val animationsEnabled = LocalUiAnimationsEnabled.current
    val isDark = palette.screen.luminance() < 0.5f

    val transition = rememberInfiniteTransition(label = "subscription")
    val glow by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "subscriptionGlow",
    )
    val sheen by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
        ),
        label = "subscriptionSheen",
    )
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "subscriptionPulse",
    )

    val glowAlpha = if (animationsEnabled) 0.22f * glow else 0.22f
    val labelBrush = Brush.horizontalGradient(if (isDark) CrystalLabelDark else CrystalLabelLight)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(CrystalGreen.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.width * 0.9f,
                    ),
                )
                if (animationsEnabled) {
                    val sheenX = -size.width * 0.4f + sheen * size.width * 1.8f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.24f),
                                Color.Transparent,
                            ),
                            startX = sheenX,
                            endX = sheenX + size.width * 0.35f,
                        ),
                    )
                }
            }
            .clickable(enabled = onClick != null) { onClick?.invoke() },
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (animationsEnabled) {
                Sparkles(pulse, maxWidth, maxHeight)
            }

            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    kind = AppIconKind.Diamond,
                    tint = CrystalGreen,
                    modifier = Modifier
                        .size(28.dp)
                        .graphicsLayer {
                            if (animationsEnabled) {
                                scaleX = 1f + 0.1f * pulse
                                scaleY = 1f + 0.1f * pulse
                                rotationZ = 8f * pulse
                                transformOrigin = TransformOrigin.Center
                            }
                        },
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.subscription_name),
                        style = TextStyle(
                            brush = labelBrush,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                        ),
                    )
                    if (isSubscribed && expiry.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.subscription_active_until, expiry),
                            color = palette.textMuted,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Sparkles(
    pulse: Float,
    maxWidth: Dp,
    maxHeight: Dp,
) {
    SparkleText(
        progress = pulse,
        modifier = Modifier.offset(x = maxWidth * 0.14f, y = maxHeight * 0.18f),
    )
    SparkleText(
        progress = 1f - pulse,
        modifier = Modifier.offset(x = maxWidth * 0.24f, y = maxHeight * 0.62f),
    )
    SparkleText(
        progress = (pulse + 0.33f) % 1f,
        modifier = Modifier.offset(x = maxWidth * 0.78f, y = maxHeight * 0.24f),
    )
    SparkleText(
        progress = (pulse + 0.66f) % 1f,
        modifier = Modifier.offset(x = maxWidth * 0.8f, y = maxHeight * 0.7f),
    )
}

@Composable
private fun SparkleText(progress: Float, modifier: Modifier) {
    Text(
        text = "\u2726",
        color = CrystalGreenDark,
        fontSize = 10.sp,
        modifier = modifier.graphicsLayer {
            alpha = (1f - progress).coerceIn(0f, 1f)
            translationY = -progress * 10f
        },
    )
}
