package ru.fet1sov.akinorev.feature.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun GamesScreen(
    onPlay: (String, String) -> Unit,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GamesViewModel = viewModel(factory = GamesViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val showSkeleton = uiState.isAuthPending ||
        (uiState.isLoading && uiState.games.isEmpty())

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = HeaderKey) {
            GamesHeader(uiState = uiState, palette = palette)
        }

        when {
            showSkeleton -> items(SkeletonRows) {
                GamesSkeletonRow()
            }

            uiState.isUnavailable && uiState.games.isEmpty() -> item(key = ErrorKey) {
                GamesErrorState(palette = palette, onRetry = viewModel::retry)
            }

            uiState.games.isEmpty() -> item(key = EmptyKey) {
                GamesEmptyState(palette = palette, onOpenCatalog = onOpenCatalog)
            }

            else -> items(uiState.games, key = { it.id }) { game ->
                GameRow(game = game, palette = palette, onPlay = onPlay)
            }
        }
    }
}

@Composable
private fun GamesHeader(
    uiState: GamesUiState,
    palette: NavigationPalette,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.library_title),
            color = palette.textPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.library_subtitle),
            color = palette.textMuted,
            fontSize = 14.sp,
        )

        if (uiState.games.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(
                    text = stringResource(R.string.library_games_count, uiState.games.size),
                    palette = palette,
                )
                StatChip(
                    text = stringResource(
                        R.string.library_total_playtime,
                        playtimeText(uiState.totalSeconds),
                    ),
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    text: String,
    palette: NavigationPalette,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = palette.barBorder.copy(alpha = 0.45f),
    ) {
        Text(
            text = text,
            color = palette.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun GameRow(
    game: LibraryGameUi,
    palette: NavigationPalette,
    onPlay: (String, String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = palette.bar,
        border = BorderStroke(1.dp, palette.barBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RemoteImage(
                    url = resolveMediaUrl(game.iconPath),
                    contentDescription = game.title,
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { GameIconPlaceholder(palette) },
                )

                Spacer(modifier = Modifier.width(15.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = game.title.uppercase(),
                        color = palette.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    genreLabel(game.genreSlug)?.let { genre ->
                        Text(
                            text = genre.uppercase(),
                            color = palette.textMuted,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    game.lastPlayedAtMillis?.let { millis ->
                        Text(
                            text = stringResource(
                                R.string.library_last_played,
                                formatDate(millis),
                            ),
                            color = palette.textMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatChip(text = playtimeText(game.secondsPlayed), palette = palette)
                Spacer(modifier = Modifier.width(10.dp))
                PlayButton(palette = palette, onClick = { onPlay(game.id, game.title) })
            }
        }
    }
}

@Composable
private fun PlayButton(
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = palette.idleContent,
    ) {
        Text(
            text = stringResource(R.string.game_play),
            color = palette.bar,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
        )
    }
}

@Composable
private fun GameIconPlaceholder(palette: NavigationPalette) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.barBorder.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            kind = AppIconKind.Games,
            tint = palette.textMuted,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun GamesSkeletonRow() {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, navigationPalette().barBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(height = 64.dp, width = 64.dp, shape = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(height = 18.dp, width = 160.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBlock(height = 12.dp, width = 100.dp)
            }
            SkeletonBlock(height = 36.dp, width = 90.dp, shape = RoundedCornerShape(10.dp))
        }
    }
}

@Composable
private fun GamesErrorState(
    palette: NavigationPalette,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.library_unavailable),
            color = palette.textMuted,
            fontSize = 18.sp,
        )
        ActionButton(text = stringResource(R.string.library_retry), palette = palette, onClick = onRetry)
    }
}

@Composable
private fun GamesEmptyState(
    palette: NavigationPalette,
    onOpenCatalog: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppIcon(
            kind = AppIconKind.Games,
            tint = palette.textPrimary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(R.string.library_empty),
            color = palette.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.library_empty_hint),
            color = palette.textMuted,
            fontSize = 14.sp,
        )
        ActionButton(
            text = stringResource(R.string.library_to_catalog),
            palette = palette,
            onClick = onOpenCatalog,
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = palette.idleContent,
    ) {
        Text(
            text = text,
            color = palette.bar,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun playtimeText(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = (safe / 3600).toInt()
    val minutes = ((safe % 3600) / 60).toInt()

    return when {
        hours > 0 && minutes > 0 -> stringResource(
            R.string.library_time_hours_minutes,
            hours,
            minutes,
        )

        hours > 0 -> stringResource(R.string.library_time_hours, hours)
        minutes > 0 -> stringResource(R.string.library_time_minutes, minutes)
        else -> stringResource(R.string.library_time_less_minute)
    }
}

@Composable
private fun genreLabel(slug: String): String? {
    if (slug.isBlank()) return null

    val resource = when (slug.lowercase()) {
        "rpg" -> R.string.genres_rpg
        "action" -> R.string.genres_action
        "puzzle" -> R.string.genres_puzzle
        "racing" -> R.string.genres_racing
        "casual" -> R.string.genres_casual
        "strategy" -> R.string.genres_strategy
        "shooter" -> R.string.genres_shooter
        "sports" -> R.string.genres_sports
        "adventure" -> R.string.genres_adventure
        "simulation" -> R.string.genres_simulation
        "platformer" -> R.string.genres_platformer
        "horror" -> R.string.genres_horror
        "indie" -> R.string.genres_indie
        "openworld" -> R.string.genres_openworld
        "fighting" -> R.string.genres_fighting
        "sandbox" -> R.string.genres_sandbox
        "roleplay" -> R.string.genres_roleplay
        "arcade" -> R.string.genres_arcade
        else -> null
    }

    return resource?.let { stringResource(it) }
        ?: slug.replaceFirstChar { char -> char.uppercase() }
}

private fun formatDate(millis: Long): String {
    val formatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)
        .withLocale(Locale.getDefault())
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

private const val SkeletonRows = 4
private const val HeaderKey = "games-header"
private const val ErrorKey = "games-error"
private const val EmptyKey = "games-empty"
