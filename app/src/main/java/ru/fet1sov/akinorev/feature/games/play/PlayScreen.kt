package ru.fet1sov.akinorev.feature.games.play

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.format.genreDisplayName
import ru.fet1sov.akinorev.ui.format.genreIconKind
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.DarkNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.LightNavigationPalette
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun PlayScreen(
    gameId: String,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayViewModel = viewModel(factory = PlayViewModel.factory(gameId, title)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val app = context.applicationContext as AcinorevApplication
    val settings by app.container.appSettingsRepository.settings.collectAsStateWithLifecycle()
    val appDarkTheme = settings.theme.isDark(isSystemInDarkTheme())
    val palette = if (appDarkTheme) DarkNavigationPalette else LightNavigationPalette

    var isPageLoading by remember { mutableStateOf(true) }
    var hasPageError by remember { mutableStateOf(false) }
    var isInviteOpen by remember { mutableStateOf(false) }
    var isLinkCopied by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.isFullscreen) { viewModel.exitFullscreen() }

    PlayerSystemBarsEffect(
        isFullscreen = uiState.isFullscreen,
        appDarkTheme = appDarkTheme,
        statusBarColor = headerTopColor(palette),
        navigationBarColor = palette.screen,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        if (!uiState.isFullscreen) {
            PlayHeader(
                game = uiState.game,
                title = uiState.title,
                palette = palette,
                onBack = onBack,
                onToggleFullscreen = viewModel::toggleFullscreen,
                fullscreenAvailable = uiState.game?.supportsMobile != false,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black),
        ) {
            when {
                uiState.isUnavailable -> PlayUnavailable(
                    title = stringResource(R.string.play_not_found),
                    hint = stringResource(R.string.play_not_found_description),
                    palette = palette,
                    action = stringResource(R.string.play_retry),
                    onAction = viewModel::retry,
                )

                uiState.isLoading || uiState.gameUrl.isBlank() -> PlayLoading(palette = palette)

                uiState.game?.supportsMobile == false -> PlayUnavailable(
                    title = stringResource(R.string.play_unavailable_mobile),
                    hint = stringResource(R.string.play_unavailable_description),
                    palette = palette,
                )

                hasPageError -> PlayUnavailable(
                    title = stringResource(R.string.play_load_error),
                    palette = palette,
                    action = stringResource(R.string.play_retry),
                    onAction = {
                        hasPageError = false
                        isPageLoading = true
                        viewModel.retry()
                    },
                )

                else -> GameWebView(
                    url = uiState.gameUrl,
                    modifier = Modifier.fillMaxSize(),
                    onPageStarted = {
                        isPageLoading = true
                        hasPageError = false
                    },
                    onPageFinished = { isPageLoading = false },
                    onError = {
                        isPageLoading = false
                        hasPageError = true
                    },
                )
            }

            if (isPageLoading && !uiState.isUnavailable && uiState.game?.supportsMobile != false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.play_loading),
                            color = Color.White,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }

        if (!uiState.isFullscreen) {
            PlayToolbar(
                uiState = uiState,
                palette = palette,
                onInvite = {
                    viewModel.ensureRoom()
                    isLinkCopied = false
                    isInviteOpen = true
                },
            )
        }
    }

    if (isInviteOpen) {
        InviteDialog(
            roomCode = uiState.roomCode,
            inviteLink = uiState.inviteLink,
            isCopied = isLinkCopied,
            onCopy = {
                copyToClipboard(context, uiState.inviteLink)
                isLinkCopied = true
            },
            onShare = { shareInvite(context, uiState.title, uiState.inviteLink) },
            onDismiss = { isInviteOpen = false },
        )
    }

    LaunchedEffect(isLinkCopied) {
        if (!isLinkCopied) return@LaunchedEffect
        kotlinx.coroutines.delay(2_000)
        isLinkCopied = false
    }
}

@Composable
private fun PlayHeader(
    game: CatalogGame?,
    title: String,
    palette: NavigationPalette,
    onBack: () -> Unit,
    onToggleFullscreen: () -> Unit,
    fullscreenAvailable: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fadeBrush(headerTopColor(palette), palette.screen))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconSurface(palette = palette, onClick = onBack, description = null) {
            AppIcon(
                kind = AppIconKind.ArrowLeft,
                tint = palette.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (game?.iconPath?.isNotBlank() == true) {
            RemoteImage(
                url = resolveMediaUrl(game.iconPath),
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title.uppercase(),
            color = palette.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        if (fullscreenAvailable) {
            IconSurface(palette = palette, onClick = onToggleFullscreen, description = null) {
                AppIcon(
                    kind = if (fullscreenAvailable) AppIconKind.Expand else AppIconKind.Collapse,
                    tint = palette.textPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayToolbar(
    uiState: PlayUiState,
    palette: NavigationPalette,
    onInvite: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.bar)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        val game = uiState.game

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = game?.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.play_no_description),
                color = palette.textPrimary,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(10.dp))
            InviteButton(palette = palette, onInvite = onInvite)
        }

        if (game != null) {
            Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val genreName = genreDisplayName(game.genreSlug, game.genreTitle)
            if (genreName.isNotBlank()) {
                AppIcon(
                    kind = genreIconKind(game.genreSlug),
                    tint = palette.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = genreName.uppercase(),
                    color = palette.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (game.developerName.isNotBlank()) {
                Text(
                    text = "· ${game.developerName}",
                    color = palette.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }

            game.publishedAtMillis?.let { millis ->
                Text(
                    text = "· ${formatReleased(millis)}",
                    color = palette.textMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        }
    }
}

@Composable
private fun InviteButton(
    palette: NavigationPalette,
    onInvite: () -> Unit,
) {
    Surface(
        onClick = onInvite,
        shape = RoundedCornerShape(10.dp),
        color = palette.idleContent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                kind = AppIconKind.Plus,
                tint = palette.bar,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.play_invite),
                color = palette.bar,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun IconSurface(
    palette: NavigationPalette,
    onClick: () -> Unit,
    description: String?,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun PlayLoading(palette: NavigationPalette) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = palette.textPrimary)
    }
}

@Composable
private fun PlayUnavailable(
    title: String,
    palette: NavigationPalette,
    hint: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(palette.screen, palette.bar),
                ),
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppIcon(
            kind = AppIconKind.CloudOff,
            tint = palette.textMuted,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (hint != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = hint,
                color = palette.textMuted,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        if (action != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                color = palette.idleContent,
            ) {
                Text(
                    text = action,
                    color = palette.bar,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun InviteDialog(
    roomCode: String,
    inviteLink: String,
    isCopied: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.play_invite_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.play_invite_hint),
                    fontSize = 13.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.play_room),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = roomCode,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
                Text(
                    text = inviteLink,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isCopied) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.play_link_copied),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text(text = stringResource(R.string.play_share_link))
            }
        },
        dismissButton = {
            TextButton(onClick = onCopy) {
                Text(text = stringResource(R.string.play_copy_link))
            }
        },
    )
}

@Composable
private fun PlayerSystemBarsEffect(
    isFullscreen: Boolean,
    appDarkTheme: Boolean,
    statusBarColor: Color,
    navigationBarColor: Color,
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    DisposableEffect(activity, isFullscreen, appDarkTheme, statusBarColor, navigationBarColor) {
        val window = activity?.window
        if (window == null) return@DisposableEffect onDispose { }

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.isAppearanceLightStatusBars = !appDarkTheme
        controller.isAppearanceLightNavigationBars = !appDarkTheme

        @Suppress("DEPRECATION")
        window.navigationBarColor = navigationBarColor.toArgb()
        @Suppress("DEPRECATION")
        window.statusBarColor = statusBarColor.toArgb()

        if (isFullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.isAppearanceLightStatusBars = !appDarkTheme
            controller.isAppearanceLightNavigationBars = !appDarkTheme
        }
    }
}

private fun copyToClipboard(context: Context, value: String) {
    if (value.isBlank()) return
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    manager.setPrimaryClip(ClipData.newPlainText("game-invite", value))
}

private fun shareInvite(context: Context, title: String, link: String) {
    if (link.isBlank()) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "$title\n$link")
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, title))
    }
}

private fun headerTopColor(palette: NavigationPalette): Color = lerp(palette.bar, palette.screen, 0.5f)

private fun fadeBrush(top: Color, bottom: Color): Brush {
    val steps = List(GradientSteps) { index ->
        val t = index.toFloat() / (GradientSteps - 1)
        val eased = t * t * (3f - 2f * t)
        lerp(top, bottom, eased)
    }
    return Brush.verticalGradient(steps)
}

private fun formatReleased(millis: Long): String {
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}


private const val GradientSteps = 6
