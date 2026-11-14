package ru.fet1sov.akinorev.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

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

private val DarkNavigationPalette = NavigationPalette(
    screen = Color(0xFF0E0E10),
    bar = Color(0xFF1B1B1F),
    barBorder = Color(0xFF2C2C32),
    activePill = Color(0xFF2C2139),
    activePillBorder = Color(0xFF48345F),
    accent = Color(0xFFC08BFF),
    idleContent = Color(0xFFF1F1F4),
    textPrimary = Color(0xFFF5F5F7),
    textMuted = Color(0xFF8D8D96),
    badge = Color(0xFFEF4444),
    badgeText = Color(0xFFFFFFFF),
)

private val LightNavigationPalette = NavigationPalette(
    screen = Color(0xFFF6F6F9),
    bar = Color(0xFFFFFFFF),
    barBorder = Color(0xFFE4E4EA),
    activePill = Color(0xFFF1E9FE),
    activePillBorder = Color(0xFFDFCCFB),
    accent = Color(0xFF7A3BE0),
    idleContent = Color(0xFF161619),
    textPrimary = Color(0xFF141417),
    textMuted = Color(0xFF6B6B75),
    badge = Color(0xFFEF4444),
    badgeText = Color(0xFFFFFFFF),
)

@Composable
@ReadOnlyComposable
fun navigationPalette(): NavigationPalette =
    if (isSystemInDarkTheme()) DarkNavigationPalette else LightNavigationPalette
