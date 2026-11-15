package ru.fet1sov.akinorev.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.model.AppTheme
import ru.fet1sov.akinorev.ui.components.LocalNameAnimation
import ru.fet1sov.akinorev.ui.components.rememberNameAnimation
import ru.fet1sov.akinorev.ui.navigation.DarkNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.LightNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.LocalNavigationPalette
import ru.fet1sov.akinorev.ui.system.LocalUiAnimationsEnabled

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
    theme: AppTheme = AppTheme.System,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = theme.isDark(isSystemInDarkTheme())
    val palette = if (darkTheme) DarkNavigationPalette else LightNavigationPalette
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val nameAnimation = rememberNameAnimation(animationsEnabled)

    CompositionLocalProvider(
        LocalNavigationPalette provides palette,
        LocalUiAnimationsEnabled provides animationsEnabled,
        LocalNameAnimation provides nameAnimation,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}

@Composable
fun AcinorevAppTheme(
    settingsRepository: AppSettingsRepository,
    content: @Composable () -> Unit,
) {
    val settings by settingsRepository.settings.collectAsStateWithLifecycle()

    AkinorevTheme(
        theme = settings.theme,
        animationsEnabled = settings.animationsEnabled,
        content = content,
    )
}
