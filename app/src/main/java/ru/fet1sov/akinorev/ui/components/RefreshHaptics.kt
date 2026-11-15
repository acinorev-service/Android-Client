package ru.fet1sov.akinorev.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberRefreshWithHaptic(onRefresh: () -> Unit): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(onRefresh, haptic) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRefresh()
        }
    }
}
