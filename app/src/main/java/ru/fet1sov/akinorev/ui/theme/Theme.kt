package ru.fet1sov.akinorev.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import ru.fet1sov.akinorev.ui.navigation.DarkNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.LightNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.LocalNavigationPalette

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryPrimary,
    onPrimary = LightSecondaryPrimary,
    primaryContainer = LightSecondaryDark,
    onPrimaryContainer = LightPrimaryPrimary,
    secondary = LightPrimaryDark,
    onSecondary = LightSecondaryPrimary,
    secondaryContainer = LightSecondaryDark,
    onSecondaryContainer = LightPrimaryPrimary,
    tertiary = LightPrimaryLight,
    onTertiary = LightSecondaryPrimary,
    background = LightSecondaryDark,
    onBackground = LightPrimaryPrimary,
    surface = LightSecondaryPrimary,
    onSurface = LightPrimaryPrimary,
    surfaceVariant = LightSecondaryDark,
    onSurfaceVariant = LightPrimaryDark,
    outline = LightSecondaryLight,
    outlineVariant = LightSecondaryDark,
    error = ErrorRed,
    onError = Color.White,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryPrimary,
    onPrimary = DarkSecondaryPrimary,
    primaryContainer = DarkSecondaryDark,
    onPrimaryContainer = DarkPrimaryPrimary,
    secondary = DarkPrimaryDark,
    onSecondary = DarkSecondaryPrimary,
    secondaryContainer = DarkSecondaryLight,
    onSecondaryContainer = DarkPrimaryPrimary,
    tertiary = DarkPrimaryLight,
    onTertiary = DarkSecondaryPrimary,
    background = DarkSecondaryPrimary,
    onBackground = DarkPrimaryPrimary,
    surface = DarkSecondaryDark,
    onSurface = DarkPrimaryPrimary,
    surfaceVariant = DarkSecondaryLight,
    onSurfaceVariant = DarkPrimaryDark,
    outline = DarkSecondaryLight,
    outlineVariant = DarkSecondaryDark,
    error = ErrorRed,
    onError = Color.White,
    scrim = Color.Black,
)

@Composable
fun AkinorevTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkNavigationPalette else LightNavigationPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalNavigationPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
