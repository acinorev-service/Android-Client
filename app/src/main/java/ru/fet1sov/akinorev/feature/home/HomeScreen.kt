package ru.fet1sov.akinorev.feature.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.format.genreIconKind
import ru.fet1sov.akinorev.ui.format.genreLabelRes
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun HomeScreen(
    onPlay: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()

    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = HeaderKey, span = { GridItemSpan(maxLineSpan) }) {
            CatalogHeader(
                uiState = uiState,
                palette = palette,
                onQueryChange = viewModel::onQueryChange,
            )
        }

        item(key = GenresKey, span = { GridItemSpan(maxLineSpan) }) {
            GenreRow(
                genres = uiState.genres,
                selectedGenre = uiState.selectedGenre,
                palette = palette,
                onGenreClick = viewModel::toggleGenre,
            )
        }

        when {
            uiState.isLoading -> items(SkeletonCards) {
                GameCardSkeleton(palette = palette)
            }

            uiState.isUnavailable -> item(key = ErrorKey, span = { GridItemSpan(maxLineSpan) }) {
                CatalogMessage(
                    title = stringResource(R.string.home_unavailable),
                    palette = palette,
                    action = stringResource(R.string.home_retry),
                    onAction = viewModel::retry,
                )
            }

            uiState.isFilteredEmpty -> item(key = EmptyKey, span = { GridItemSpan(maxLineSpan) }) {
                CatalogMessage(
                    title = stringResource(R.string.home_empty),
                    hint = stringResource(R.string.home_empty_hint),
                    palette = palette,
                    action = stringResource(R.string.home_retry),
                    onAction = viewModel::clearFilters,
                )
            }

            else -> items(uiState.games, key = { it.id }) { game ->
                GameCard(
                    game = game,
                    palette = palette,
                    onPlay = { onPlay(game.id, game.title) },
                )
            }
        }
    }
}

@Composable
private fun CatalogHeader(
    uiState: HomeUiState,
    palette: NavigationPalette,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_title),
                color = palette.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(modifier = Modifier.width(10.dp))
            if (!uiState.isLoading && !uiState.isUnavailable) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = palette.barBorder.copy(alpha = 0.45f),
                ) {
                    Text(
                        text = stringResource(R.string.home_games_count, uiState.games.size),
                        color = palette.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.home_search_placeholder),
                    color = palette.textMuted,
                    fontSize = 14.sp,
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.textMuted,
                unfocusedBorderColor = palette.barBorder,
                focusedContainerColor = palette.bar,
                unfocusedContainerColor = palette.bar,
                cursorColor = palette.textPrimary,
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
            ),
        )
    }
}

@Composable
private fun GenreRow(
    genres: List<String>,
    selectedGenre: String,
    palette: NavigationPalette,
    onGenreClick: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        item(key = "all-genres") {
            GenreChip(
                label = stringResource(R.string.home_all_genres),
                icon = null,
                selected = selectedGenre.isEmpty(),
                palette = palette,
                onClick = { onGenreClick("") },
            )
        }

        items(genres, key = { it }) { slug ->
            GenreChip(
                label = genreLabelRes(slug)?.let { stringResource(it) }
                    ?: slug.replaceFirstChar { it.uppercase() },
                icon = genreIconKind(slug),
                selected = selectedGenre == slug,
                palette = palette,
                onClick = { onGenreClick(slug) },
            )
        }
    }
}

@Composable
private fun GenreChip(
    label: String,
    icon: AppIconKind?,
    selected: Boolean,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) palette.idleContent else palette.bar,
        border = if (selected) null else BorderStroke(1.dp, palette.barBorder.copy(alpha = 0.6f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                AppIcon(
                    kind = icon,
                    tint = if (selected) palette.bar else palette.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label.uppercase(),
                color = if (selected) palette.bar else palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun GameCard(
    game: GameCardUi,
    palette: NavigationPalette,
    onPlay: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(palette.bar),
    ) {
        if (game.bannerPath.isBlank()) {
            GameBannerPlaceholder(palette = palette)
        } else {
            RemoteImage(
                url = resolveMediaUrl(game.bannerPath),
                contentDescription = game.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
                placeholder = { GameBannerPlaceholder(palette = palette) },
            )
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = game.title.uppercase(),
                color = palette.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp),
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    kind = genreIconKind(game.genreSlug),
                    tint = palette.textMuted,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = genreLabelRes(game.genreSlug)?.let { stringResource(it) }
                        ?.uppercase()
                        ?: "",
                    color = palette.textMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                onClick = onPlay,
                shape = RoundedCornerShape(10.dp),
                color = palette.idleContent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.game_play),
                    color = palette.bar,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun GameBannerPlaceholder(palette: NavigationPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(palette.barBorder.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            kind = AppIconKind.Games,
            tint = palette.textMuted,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun GameCardSkeleton(palette: NavigationPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(palette.bar),
    ) {
        SkeletonBlock(
            height = 130.dp,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        )
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            SkeletonBlock(height = 16.dp, width = 120.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(height = 16.dp, width = 80.dp)
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBlock(height = 10.dp, width = 60.dp)
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonBlock(height = 34.dp, shape = RoundedCornerShape(10.dp))
        }
    }
}

@Composable
private fun CatalogMessage(
    title: String,
    palette: NavigationPalette,
    action: String,
    onAction: () -> Unit,
    hint: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppIcon(
            kind = AppIconKind.CloudOff,
            tint = palette.textMuted,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = title,
            color = palette.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        if (hint != null) {
            Text(
                text = hint,
                color = palette.textMuted,
                fontSize = 13.sp,
            )
        }
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
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            )
        }
    }
}

private const val SkeletonCards = 4
private const val HeaderKey = "home-header"
private const val GenresKey = "home-genres"
private const val ErrorKey = "home-error"
private const val EmptyKey = "home-empty"
