package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.format.rememberLastSeenLabel
import ru.fet1sov.akinorev.ui.icons.AppIcons
import ru.fet1sov.akinorev.ui.system.LocalUiAnimationsEnabled

@Composable
fun ProfilePresence(
    isOnline: Boolean,
    isSelf: Boolean,
    isBlocked: Boolean,
    device: String,
    lastSeenDevice: String,
    lastSeenMillis: Long?,
    modifier: Modifier = Modifier,
) {
    val online = isSelf || isOnline
    val color = when {
        isBlocked -> BlockedColor
        online -> OnlineColor
        else -> OfflineColor
    }
    val showPhone = isMobileDevice(if (online) device else lastSeenDevice)
    val label = when {
        isBlocked -> stringResource(R.string.community_profile_blocked)
        online -> stringResource(R.string.community_profile_online)
        else -> rememberLastSeenLabel(lastSeenMillis)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            isBlocked -> PresenceDot(color = color, animated = false)
            showPhone -> Icon(
                imageVector = AppIcons.Phone,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(width = 16.dp, height = 16.dp),
            )
            else -> PresenceDot(color = color, animated = online && LocalUiAnimationsEnabled.current)
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = label.uppercase(),
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp,
        )
    }
}

@Composable
private fun PresenceDot(
    color: Color,
    animated: Boolean,
) {
    Box(
        modifier = Modifier.size(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (animated) {
            val transition = rememberInfiniteTransition(label = "presence")
            val scale by transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 2.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "presence-scale",
            )
            val alpha by transition.animateFloat(
                initialValue = 0.55f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "presence-alpha",
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(color, CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape),
        )
    }
}

private fun isMobileDevice(raw: String): Boolean = raw.trim().equals("mobile", ignoreCase = true)

private val OnlineColor = Color(0xFF16A34A)
private val OfflineColor = Color(0xFF9CA3AF)
private val BlockedColor = Color(0xFFEF4444)
