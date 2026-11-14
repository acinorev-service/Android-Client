package ru.fet1sov.akinorev.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.fet1sov.akinorev.ui.theme.DarkPrimaryDark
import ru.fet1sov.akinorev.ui.theme.DarkPrimaryPrimary
import ru.fet1sov.akinorev.ui.theme.DarkSecondaryDark
import ru.fet1sov.akinorev.ui.theme.DarkSecondaryLight
import ru.fet1sov.akinorev.ui.theme.DarkSecondaryPrimary
import ru.fet1sov.akinorev.ui.theme.LightPrimaryDark
import ru.fet1sov.akinorev.ui.theme.LightPrimaryPrimary
import ru.fet1sov.akinorev.ui.theme.LightSecondaryDark
import ru.fet1sov.akinorev.ui.theme.LightSecondaryLight
import ru.fet1sov.akinorev.ui.theme.LightSecondaryPrimary

@Immutable
data class NavigationPalette(
    val screen: Color,
    val bar: Color,
    val barBorder: Color,
    val activePill: Color,
    val activePillBorder: Color,
    val accent: Color,
    val idleContent: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val badge: Color,
    val badgeText: Color,
)

val DarkNavigationPalette = NavigationPalette(
    screen = DarkSecondaryPrimary,
    bar = DarkSecondaryDark,
    barBorder = DarkSecondaryLight,
    activePill = DarkSecondaryLight,
    activePillBorder = DarkSecondaryLight,
    accent = DarkPrimaryPrimary,
    idleContent = DarkPrimaryDark,
    textPrimary = DarkPrimaryPrimary,
    textMuted = DarkPrimaryDark,
    badge = DarkPrimaryPrimary,
    badgeText = DarkSecondaryPrimary,
)

val LightNavigationPalette = NavigationPalette(
    screen = LightSecondaryDark,
    bar = LightSecondaryPrimary,
    barBorder = LightSecondaryLight,
    activePill = LightSecondaryDark,
    activePillBorder = LightSecondaryLight,
    accent = LightPrimaryPrimary,
    idleContent = LightPrimaryDark,
    textPrimary = LightPrimaryPrimary,
    textMuted = LightPrimaryDark,
    badge = LightPrimaryPrimary,
    badgeText = LightSecondaryPrimary,
)

val LocalNavigationPalette = staticCompositionLocalOf { LightNavigationPalette }

@Composable
@ReadOnlyComposable
fun navigationPalette(): NavigationPalette = LocalNavigationPalette.current
