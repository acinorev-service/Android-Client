package ru.fet1sov.akinorev.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppTheme
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import ru.fet1sov.akinorev.ui.system.findActivity
import ru.fet1sov.akinorev.ui.theme.ErrorRed

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val tabs = SettingsTab.entries
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(uiState.activeTab).coerceAtLeast(0),
    ) { tabs.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            tabs.getOrNull(page)?.let(viewModel::selectTab)
        }
    }

    LaunchedEffect(uiState.activeTab) {
        val index = tabs.indexOf(uiState.activeTab)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.animateScrollToPage(index)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = palette.textPrimary,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == pagerState.currentPage
                Tab(
                    selected = selected,
                    onClick = {
                        viewModel.selectTab(tab)
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    icon = {
                        AppIcon(
                            kind = tab.icon,
                            tint = if (selected) palette.textPrimary else palette.textMuted,
                            modifier = Modifier.size(24.dp),
                            contentDescription = stringResource(tab.labelRes),
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            key = { index -> tabs[index] },
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                when (tabs[page]) {
                    SettingsTab.Appearance -> AppearanceSection(
                        uiState = uiState,
                        onThemeSelected = viewModel::setTheme,
                        onLanguageSelected = { language ->
                            if (language != uiState.language) {
                                viewModel.setLanguage(language)
                                context.findActivity()?.recreate()
                            }
                        },
                        onAnimationsChanged = viewModel::setAnimationsEnabled,
                    )

                    SettingsTab.Notifications -> NotificationsSection(
                        uiState = uiState,
                        palette = palette,
                        onPreferenceChange = viewModel::setNotificationPreference,
                        onRetry = viewModel::retryNotificationPreferences,
                    )

                    SettingsTab.Account -> AccountSection(
                        palette = palette,
                        onLogout = viewModel::logout,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    uiState: SettingsUiState,
    onThemeSelected: (AppTheme) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onAnimationsChanged: (Boolean) -> Unit,
) {
    SettingsTitle(stringResource(R.string.settings_themes_title))
    SettingsDescription(stringResource(R.string.settings_themes_description))

    SettingsChoiceRow(
        options = AppTheme.entries.map { theme ->
            ChoiceOption(theme, stringResource(theme.labelRes))
        },
        selected = uiState.theme,
        onSelect = onThemeSelected,
    )

    Spacer(modifier = Modifier.height(24.dp))

    SettingsTitle(stringResource(R.string.settings_language_title))
    SettingsDescription(stringResource(R.string.settings_language_description))

    SettingsChoiceRow(
        options = (listOf(AppLanguage.System) + AppLanguage.supported).map { language ->
            ChoiceOption(language, stringResource(language.labelRes))
        },
        selected = uiState.language,
        onSelect = onLanguageSelected,
    )

    Spacer(modifier = Modifier.height(24.dp))

    SettingsTitle(stringResource(R.string.settings_animations_title))
    SettingsDescription(stringResource(R.string.settings_animations_description))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = uiState.animationsEnabled,
            onCheckedChange = onAnimationsChanged,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = navigationPalette().accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = navigationPalette().barBorder,
            ),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.settings_animations_enabled),
            color = navigationPalette().textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun NotificationsSection(
    uiState: SettingsUiState,
    palette: NavigationPalette,
    onPreferenceChange: (NotificationPreferenceKey, Boolean) -> Unit,
    onRetry: () -> Unit,
) {
    if (uiState.isAuthPending) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SkeletonBlock(height = 20.dp, width = 240.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBlock(height = 14.dp, width = 300.dp)
        }
        return
    }

    if (!uiState.isAuthenticated) return

    SettingsTitle(stringResource(R.string.settings_notifications_types_title))
    SettingsDescription(stringResource(R.string.settings_notifications_types_description))

    if (uiState.isNotificationsLoading) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
            repeat(NotificationPreferenceLabels.size) { index ->
                SkeletonBlock(
                    height = 20.dp,
                    width = 220.dp,
                    modifier = Modifier.padding(top = if (index == 0) 0.dp else 12.dp),
                )
            }
        }
        return
    }

    Column(modifier = Modifier.padding(top = 12.dp)) {
        NotificationPreferenceLabels.forEach { (key, labelRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = uiState.notifications[key],
                    onCheckedChange = { enabled -> onPreferenceChange(key, enabled) },
                    enabled = uiState.savingPreference != key,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = palette.accent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = palette.barBorder,
                    ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(labelRes),
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    val message = uiState.notificationMessage
    if (message != null) {
        Text(
            text = stringResource(message.textRes),
            color = ErrorRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (message == SettingsNotificationMessage.LoadFailed) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                color = palette.bar,
                border = BorderStroke(1.dp, palette.barBorder),
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications_retry),
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun AccountSection(
    palette: NavigationPalette,
    onLogout: () -> Unit,
) {
    SettingsTitle(stringResource(R.string.settings_account_title))
    SettingsDescription(stringResource(R.string.settings_account_description))

    Surface(
        onClick = onLogout,
        shape = RoundedCornerShape(14.dp),
        color = ErrorRed.copy(alpha = 0.1f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                kind = AppIconKind.Logout,
                tint = ErrorRed,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.settings_account_logout),
                color = ErrorRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SettingsTitle(text: String) {
    Text(
        text = text,
        color = navigationPalette().textPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun SettingsDescription(text: String) {
    Text(
        text = text,
        color = navigationPalette().textMuted,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        modifier = Modifier.padding(top = 2.dp),
    )
}

private data class ChoiceOption<T>(val value: T, val label: String)

@Composable
private fun <T> SettingsChoiceRow(
    options: List<ChoiceOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val palette = navigationPalette()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            Surface(
                onClick = { onSelect(option.value) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) palette.idleContent else Color.Transparent,
                contentColor = if (isSelected) palette.bar else palette.textPrimary,
                border = BorderStroke(1.dp, palette.barBorder),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = option.label,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                )
            }
        }
    }
}

private val NotificationPreferenceLabels: List<Pair<NotificationPreferenceKey, Int>> = listOf(
    NotificationPreferenceKey.Follow to R.string.settings_notifications_type_follow,
    NotificationPreferenceKey.Block to R.string.settings_notifications_type_block,
    NotificationPreferenceKey.Post to R.string.settings_notifications_type_post,
    NotificationPreferenceKey.Like to R.string.settings_notifications_type_like,
    NotificationPreferenceKey.Broadcast to R.string.settings_notifications_type_broadcast,
)

private val AppTheme.labelRes: Int
    get() = when (this) {
        AppTheme.System -> R.string.themes_system
        AppTheme.Light -> R.string.themes_light
        AppTheme.Dark -> R.string.themes_dark
    }

private val AppLanguage.labelRes: Int
    get() = when (this) {
        AppLanguage.System -> R.string.settings_language_system
        AppLanguage.English -> R.string.locales_en
        AppLanguage.Russian -> R.string.locales_ru
    }

private val SettingsNotificationMessage.textRes: Int
    get() = when (this) {
        SettingsNotificationMessage.LoadFailed -> R.string.settings_notifications_load_failed
        SettingsNotificationMessage.SaveFailed -> R.string.settings_notifications_save_failed
    }
