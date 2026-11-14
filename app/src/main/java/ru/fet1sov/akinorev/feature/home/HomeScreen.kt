package ru.fet1sov.akinorev.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.search.SearchUiState
import ru.fet1sov.akinorev.feature.search.SearchViewModel
import ru.fet1sov.akinorev.model.SearchCategory
import ru.fet1sov.akinorev.model.SearchResult
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.UserAvatar
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.format.genreDisplayName
import ru.fet1sov.akinorev.ui.format.genreIconKind
import ru.fet1sov.akinorev.ui.format.genreLabelRes
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun HomeScreen(
    onPlay: (String, String) -> Unit,
    onOpenUser: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    searchViewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()

    LaunchedEffect(Unit) { viewModel.refresh() }

    val closeSearch = {
        searchViewModel.clear()
        viewModel.closeSearch()
    }

    AnimatedContent(
        targetState = uiState.isSearchOpen,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState) {
                (
                    slideInHorizontally(tween(SearchAnimationMillis)) { it / 4 } +
                        fadeIn(tween(SearchAnimationMillis))
                    ) togetherWith fadeOut(tween(SearchAnimationMillis / 2))
            } else {
                fadeIn(tween(SearchAnimationMillis)) togetherWith (
                    slideOutHorizontally(tween(SearchAnimationMillis)) { it / 4 } +
                        fadeOut(tween(SearchAnimationMillis))
                    )
            }
        },
        label = "home-search",
    ) { searchOpen ->
        if (searchOpen) {
            SearchPane(
                state = searchState,
                palette = palette,
                onQueryChange = searchViewModel::onQueryChange,
                onSubmit = searchViewModel::submit,
                onLoadMore = searchViewModel::loadMore,
                onRetry = searchViewModel::retry,
                onClose = closeSearch,
                onPlay = onPlay,
                onOpenUser = onOpenUser,
            )
        } else {
            CatalogPane(
                uiState = uiState,
                palette = palette,
                onOpenSearch = viewModel::openSearch,
                onGenreClick = viewModel::toggleGenre,
                onRetry = viewModel::retry,
                onClearFilters = viewModel::clearFilters,
                onPlay = onPlay,
            )
        }
    }
}

@Composable
private fun CatalogPane(
    uiState: HomeUiState,
    palette: NavigationPalette,
    onOpenSearch: () -> Unit,
    onGenreClick: (String) -> Unit,
    onRetry: () -> Unit,
    onClearFilters: () -> Unit,
    onPlay: (String, String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = HeaderKey, span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.home_title),
                    color = palette.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                )
                Spacer(modifier = Modifier.height(10.dp))
                SearchEntry(
                    palette = palette,
                    onClick = onOpenSearch,
                )
            }
        }

        item(key = GenresKey, span = { GridItemSpan(maxLineSpan) }) {
            GenreRow(
                genres = uiState.genres,
                selectedGenre = uiState.selectedGenre,
                palette = palette,
                onGenreClick = onGenreClick,
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
                    onAction = onRetry,
                )
            }

            uiState.isFilteredEmpty -> item(key = EmptyKey, span = { GridItemSpan(maxLineSpan) }) {
                CatalogMessage(
                    title = stringResource(R.string.home_empty),
                    hint = stringResource(R.string.home_empty_hint),
                    palette = palette,
                    action = stringResource(R.string.home_retry),
                    onAction = onClearFilters,
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
private fun SearchPane(
    state: SearchUiState,
    palette: NavigationPalette,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    onPlay: (String, String) -> Unit,
    onOpenUser: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    BackHandler(onBack = onClose)

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchIconButton(
                icon = AppIconKind.ArrowLeft,
                palette = palette,
                onClick = onClose,
            )

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
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
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        keyboard?.hide()
                        onSubmit()
                    },
                ),
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

            if (state.query.isNotEmpty()) {
                SearchIconButton(
                    icon = AppIconKind.Close,
                    palette = palette,
                    onClick = { onQueryChange("") },
                )
            }
        }

        when {
            state.isSearching && !state.hasResults -> SearchSkeleton(palette = palette)

            state.hasError -> CatalogMessage(
                title = stringResource(R.string.search_error),
                palette = palette,
                action = stringResource(R.string.home_retry),
                onAction = onRetry,
            )

            state.query.isBlank() -> CatalogMessage(
                title = stringResource(R.string.search_prompt),
                palette = palette,
                action = null,
                onAction = null,
            )

            state.isEmpty -> CatalogMessage(
                title = stringResource(R.string.home_empty),
                hint = stringResource(R.string.home_empty_hint),
                palette = palette,
                action = null,
                onAction = null,
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.groups.forEach { group ->
                    item(key = "header-${group.category.name}") {
                        Text(
                            text = stringResource(categoryTitleRes(group.category)).uppercase(),
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                        )
                    }

                    items(group.results, key = { result -> resultKey(result) }) { result ->
                        SearchResultRow(
                            result = result,
                            palette = palette,
                            onPlay = onPlay,
                            onOpenUser = onOpenUser,
                        )
                    }
                }

                if (state.hasMore) {
                    item(key = "load-more") {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            TextButton(
                                onClick = {
                                    keyboard?.hide()
                                    onLoadMore()
                                },
                            ) {
                                if (state.isSearching) {
                                    CircularProgressIndicator(
                                        color = palette.textPrimary,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.search_show_more),
                                        color = palette.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSkeleton(palette: NavigationPalette) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SkeletonBlock(height = 16.dp, width = 120.dp)
        repeat(SkeletonRows) {
            SkeletonBlock(height = 60.dp, shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    palette: NavigationPalette,
    onPlay: (String, String) -> Unit,
    onOpenUser: (String) -> Unit,
) {
    when (result) {
        is SearchResult.Game -> SearchRowSurface(
            palette = palette,
            onClick = { onPlay(result.game.id, result.game.title) },
        ) {
            GameResultArtwork(game = result, palette = palette)
            SearchRowText(
                title = result.game.title,
                subtitle = genreDisplayName(result.game.genreSlug, result.game.genreTitle),
                palette = palette,
            )
            PlayBadge(palette = palette)
        }

        is SearchResult.User -> SearchRowSurface(
            palette = palette,
            onClick = { onOpenUser(result.username) },
        ) {
            UserAvatar(
                avatar = result.avatarPath,
                userId = result.id,
                username = result.username,
                size = 44.dp,
            )
            SearchRowText(
                title = result.username,
                subtitle = result.bio,
                palette = palette,
                prefix = "@",
            )
        }

        is SearchResult.Developer -> SearchRowSurface(palette = palette) {
            if (result.avatarPath.isNotBlank()) {
                RemoteImage(
                    url = resolveMediaUrl(result.avatarPath),
                    contentDescription = result.name,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { DeveloperPlaceholder(palette = palette) },
                )
            } else {
                DeveloperPlaceholder(palette = palette)
            }
            SearchRowText(
                title = result.name,
                subtitle = result.handle.takeIf { it.isNotBlank() }?.let { "@$it" }.orEmpty(),
                palette = palette,
            )
        }
    }
}

@Composable
private fun SearchRowSurface(
    palette: NavigationPalette,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val contentModifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = palette.bar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = contentModifier, verticalAlignment = Alignment.CenterVertically) {
                content()
            }
        }
    } else {
        Surface(
            shape = shape,
            color = palette.bar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(modifier = contentModifier, verticalAlignment = Alignment.CenterVertically) {
                content()
            }
        }
    }
}

@Composable
private fun GameResultArtwork(
    game: SearchResult.Game,
    palette: NavigationPalette,
) {
    val path = game.game.iconPath.takeIf { it.isNotBlank() } ?: game.game.bannerPath
    if (path.isBlank()) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.barBorder.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                kind = AppIconKind.Games,
                tint = palette.textMuted,
                modifier = Modifier.size(22.dp),
            )
        }
        return
    }

    RemoteImage(
        url = resolveMediaUrl(path),
        contentDescription = game.game.title,
        modifier = Modifier.size(44.dp),
        shape = RoundedCornerShape(10.dp),
    )
}

@Composable
private fun DeveloperPlaceholder(palette: NavigationPalette) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.barBorder.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            kind = AppIconKind.Account,
            tint = palette.textMuted,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun RowScope.SearchRowText(
    title: String,
    subtitle: String,
    palette: NavigationPalette,
    prefix: String = "",
) {
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = if (prefix.isEmpty()) title.uppercase() else "$prefix$title",
            color = palette.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                color = palette.textMuted,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlayBadge(palette: NavigationPalette) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = palette.idleContent,
    ) {
        Text(
            text = stringResource(R.string.game_play),
            color = palette.bar,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun categoryTitleRes(category: SearchCategory): Int = when (category) {
    SearchCategory.Games -> R.string.search_categories_games
    SearchCategory.Developers -> R.string.search_categories_developers
    SearchCategory.Users -> R.string.search_categories_users
}

private fun resultKey(result: SearchResult): String = when (result) {
    is SearchResult.Game -> "game-${result.game.id}"
    is SearchResult.User -> "user-${result.id}"
    is SearchResult.Developer -> "developer-${result.id}"
}

@Composable
private fun SearchEntry(
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = palette.bar,
        border = BorderStroke(1.dp, palette.barBorder.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                kind = AppIconKind.Search,
                tint = palette.textMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.home_search_placeholder),
                color = palette.textMuted,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun SearchIconButton(
    icon: AppIconKind,
    palette: NavigationPalette,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                kind = icon,
                tint = palette.textPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
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

            val genreName = genreDisplayName(game.genreSlug, game.genreTitle)
            Box(modifier = Modifier.height(18.dp)) {
                if (genreName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            kind = genreIconKind(game.genreSlug),
                            tint = palette.textMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = genreName.uppercase(),
                            color = palette.textMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
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
                    textAlign = TextAlign.Center,
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
    action: String?,
    onAction: (() -> Unit)?,
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
            textAlign = TextAlign.Center,
        )
        if (hint != null) {
            Text(
                text = hint,
                color = palette.textMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }
        if (action != null && onAction != null) {
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
}

private const val SkeletonCards = 4
private const val SkeletonRows = 4
private const val SearchAnimationMillis = 260
private const val HeaderKey = "home-header"
private const val GenresKey = "home-genres"
private const val ErrorKey = "home-error"
private const val EmptyKey = "home-empty"
