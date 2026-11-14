package ru.fet1sov.akinorev.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Modifier.authReveal(index: Int): Modifier {
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(index) {
        delay(index * FieldRevealStepMillis)
        appeared = true
    }

    val progress by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = FieldRevealDurationMillis, easing = FastOutSlowInEasing),
        label = "authReveal",
    )

    return graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * 14.dp.toPx()
    }
}

private const val FieldRevealStepMillis = 90L
private const val FieldRevealDurationMillis = 320
