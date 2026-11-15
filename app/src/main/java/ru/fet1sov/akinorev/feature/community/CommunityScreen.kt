package ru.fet1sov.akinorev.feature.community

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.LoginActivity
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.data.community.CommunityFeedPageSize
import ru.fet1sov.akinorev.feature.profile.components.PostCard
import ru.fet1sov.akinorev.feature.profile.components.PostCardSkeleton
import ru.fet1sov.akinorev.feature.profile.components.ReportDialog
import ru.fet1sov.akinorev.feature.profile.components.RepostDialog
import ru.fet1sov.akinorev.feature.profile.components.postActionErrorText
import ru.fet1sov.akinorev.model.CommunityFeedMode
import ru.fet1sov.akinorev.ui.components.UnauthorizedAccess
import ru.fet1sov.akinorev.ui.components.rememberRefreshWithHaptic
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val tabs = CommunityFeedMode.entries
    val pagerState = rememberPagerState { tabs.size }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val listStates = remember(tabs) { List(tabs.size) { LazyListState() } }
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = if (headerHeightPx > 0) {
        with(density) { headerHeightPx.toDp() }
    } else {
        132.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen)
            .clipToBounds(),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { tabs[it] },
            beyondViewportPageCount = 1,
        ) { page ->
            CommunityFeedPage(
                mode = tabs[page],
                onOpenProfile = onOpenProfile,
                listState = listStates[page],
                headerHeight = headerHeight,
                modifier = Modifier.fillMaxSize(),
            )
        }

        CommunityHeader(
            mode = tabs[pagerState.currentPage],
            onSelectMode = { mode ->
                val index = tabs.indexOf(mode)
                if (index >= 0) {
                    scope.launch { pagerState.animateScrollToPage(index) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val state = listStates[pagerState.settledPage]
                    val scrolled = if (state.firstVisibleItemIndex > 0) {
                        headerHeightPx
                    } else {
                        state.firstVisibleItemScrollOffset.coerceAtMost(headerHeightPx)
                    }
                    IntOffset(0, -scrolled)
                }
                .onSizeChanged { headerHeightPx = it.height },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityHeader(
    mode: CommunityFeedMode,
    onSelectMode: (CommunityFeedMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    Column(
        modifier = modifier
            .background(palette.screen)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.community_feed_title).uppercase(),
                color = palette.textPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = stringResource(R.string.community_feed_subtitle),
                color = palette.textMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }

        FeedModeRow(
            mode = mode,
            onSelect = onSelectMode,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 8.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityFeedPage(
    mode: CommunityFeedMode,
    onOpenProfile: (String) -> Unit,
    listState: LazyListState,
    headerHeight: Dp,
    modifier: Modifier = Modifier,
    viewModel: CommunityViewModel = viewModel(
        key = "community-${mode.name}",
        factory = CommunityViewModel.factory(mode),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastIndex = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastIndex >= info.totalItemsCount - 2
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd) viewModel.loadMore()
            }
    }

    val shareCopied = stringResource(R.string.community_posts_share_copied)
    val shareError = stringResource(R.string.community_posts_share_error)
    val sharePost: (String, String) -> Unit = { postId, title ->
        val url = BuildConfig.SITE_URL.trimEnd('/') + "/post/" + postId
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, url)
        }
        val shared = runCatching {
            context.startActivity(Intent.createChooser(intent, null))
        }.isSuccess

        if (!shared) {
            val copied = runCatching {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("post", url))
            }.isSuccess
            scope.launch {
                snackbarHostState.showSnackbar(if (copied) shareCopied else shareError)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.screen),
    ) {
        val refreshWithHaptic = rememberRefreshWithHaptic(viewModel::refresh)

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = refreshWithHaptic,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = headerHeight + 8.dp, bottom = 20.dp),
            ) {
                val actionError = uiState.actionError
                if (actionError != null) {
                    item(key = ActionErrorKey) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = postActionErrorText(actionError),
                                color = DangerColor,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                            )
                            Surface(
                                onClick = viewModel::dismissActionError,
                                shape = RoundedCornerShape(8.dp),
                                color = palette.bar,
                            ) {
                                Text(
                                    text = stringResource(R.string.community_error_dismiss),
                                    color = palette.textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                )
                            }
                        }
                    }
                }

                when {
                    uiState.isFollowingLocked -> item(key = UnauthorizedKey) {
                        UnauthorizedAccess(
                            title = stringResource(R.string.community_feed_unauthorized_title),
                            description = stringResource(R.string.community_feed_unauthorized_description),
                            buttonLabel = stringResource(R.string.community_feed_unauthorized_login),
                            onAction = {
                                context.startActivity(Intent(context, LoginActivity::class.java))
                            },
                        )
                    }

                    uiState.showLoading -> items(SkeletonKeys) {
                        PostCardSkeleton(
                            palette = palette,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }

                    uiState.loadError -> item(key = LoadErrorKey) {
                        FeedError(onRetry = viewModel::retry)
                    }

                    uiState.posts.isEmpty() -> item(key = EmptyKey) {
                        Text(
                            text = stringResource(
                                if (uiState.mode == CommunityFeedMode.Following) {
                                    R.string.community_feed_following_empty
                                } else {
                                    R.string.community_feed_empty
                                },
                            ),
                            color = palette.textMuted,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                        )
                    }

                    else -> {
                        items(
                            items = uiState.posts,
                            key = { it.id },
                            contentType = { "post" },
                        ) { post ->
                            val shareTitle = stringResource(
                                R.string.community_posts_share_title,
                                post.target.author.username,
                            )
                            PostCard(
                                post = post,
                                emojiUrls = uiState.emojiUrls,
                                canManage = false,
                                isPending = post.id in uiState.pendingPostIds,
                                isReported = post.target.id in uiState.reportedPostIds,
                                onOpenProfile = onOpenProfile,
                                onLike = { viewModel.togglePostLike(post.id) },
                                onCommentsCountChanged = { count ->
                                    viewModel.onCommentsCountChanged(post.id, count)
                                },
                                onRepost = { viewModel.togglePostRepost(post.id) },
                                onShare = { sharePost(post.target.id, shareTitle) },
                                onReport = { viewModel.openReport(post.id) },
                                onPin = {},
                                onEdit = {},
                                onDelete = {},
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                            )
                        }

                        if (uiState.hasMore || uiState.isLoadingMore || uiState.loadMoreError) {
                            item(key = FooterKey) {
                                FeedFooter(
                                    isLoadingMore = uiState.isLoadingMore,
                                    loadMoreError = uiState.loadMoreError,
                                    onLoadMore = viewModel::loadMore,
                                    onRetry = viewModel::retryLoadMore,
                                    palette = palette,
                                )
                            }
                        } else if (uiState.posts.size >= CommunityFeedPageSize) {
                            item(key = EndKey) {
                                Text(
                                    text = stringResource(R.string.community_feed_end),
                                    color = palette.textMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }

    if (uiState.repost.isOpen) {
        RepostDialog(
            state = uiState.repost,
            onDismiss = viewModel::dismissRepost,
            onConfirm = viewModel::confirmRepost,
        )
    }

    if (uiState.report.isOpen) {
        ReportDialog(
            state = uiState.report,
            onDismiss = viewModel::dismissReport,
            onReasonChange = viewModel::onReportReasonChange,
            onCommentChange = viewModel::onReportCommentChange,
            onSubmit = viewModel::submitReport,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeedModeRow(
    mode: CommunityFeedMode,
    onSelect: (CommunityFeedMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CommunityFeedMode.entries.forEach { value ->
            val selected = value == mode
            Row(
                modifier = Modifier
                    .alpha(if (selected) 1f else 0.5f)
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppIcon(
                    kind = when (value) {
                        CommunityFeedMode.Popular -> AppIconKind.Heart
                        CommunityFeedMode.New -> AppIconKind.Calendar
                        CommunityFeedMode.Following -> AppIconKind.Account
                    },
                    tint = palette.textPrimary,
                    size = 16.dp,
                )
                Text(
                    text = stringResource(
                        when (value) {
                            CommunityFeedMode.Popular -> R.string.community_feed_popular
                            CommunityFeedMode.New -> R.string.community_feed_new
                            CommunityFeedMode.Following -> R.string.community_feed_following
                        },
                    ).uppercase(),
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun FeedError(onRetry: () -> Unit) {
    val palette = navigationPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.community_feed_error),
            color = palette.textMuted,
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
        )
        Surface(
            onClick = onRetry,
            shape = RoundedCornerShape(10.dp),
            color = palette.idleContent,
        ) {
            Text(
                text = stringResource(R.string.community_feed_retry),
                color = palette.bar,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun FeedFooter(
    isLoadingMore: Boolean,
    loadMoreError: Boolean,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    palette: ru.fet1sov.akinorev.ui.navigation.NavigationPalette,
) {
    if (isLoadingMore) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            repeat(2) {
                PostCardSkeleton(palette = palette)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                if (loadMoreError) R.string.community_feed_error else R.string.community_feed_loading_more,
            ),
            color = palette.textMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Surface(
            onClick = if (loadMoreError) onRetry else onLoadMore,
            shape = RoundedCornerShape(10.dp),
            color = palette.idleContent,
        ) {
            Text(
                text = stringResource(
                    if (loadMoreError) R.string.community_feed_retry else R.string.community_feed_load_more,
                ),
                color = palette.bar,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

private val DangerColor = Color(0xFFE5484D)
private val SkeletonKeys = listOf("community-skeleton-1", "community-skeleton-2", "community-skeleton-3")
private const val ActionErrorKey = "community-action-error"
private const val UnauthorizedKey = "community-unauthorized"
private const val LoadErrorKey = "community-load-error"
private const val EmptyKey = "community-empty"
private const val FooterKey = "community-footer"
private const val EndKey = "community-end"
