package ru.fet1sov.akinorev.feature.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.profile.components.BioEmojiPickerDialog
import ru.fet1sov.akinorev.feature.profile.components.PostCard
import ru.fet1sov.akinorev.feature.profile.components.PostCardSkeleton
import ru.fet1sov.akinorev.feature.profile.components.ProfileDialogs
import ru.fet1sov.akinorev.feature.profile.components.PostComposer
import ru.fet1sov.akinorev.feature.profile.components.ProfileFollowers
import ru.fet1sov.akinorev.feature.profile.components.ProfileHeaderSection
import ru.fet1sov.akinorev.feature.profile.components.ProfileHeaderSkeleton
import ru.fet1sov.akinorev.feature.profile.components.ProfileInlineError
import ru.fet1sov.akinorev.feature.profile.components.ProfileMessageState
import ru.fet1sov.akinorev.feature.profile.components.postActionErrorText
import ru.fet1sov.akinorev.feature.profile.components.profileActionErrorText
import ru.fet1sov.akinorev.feature.profile.components.profileLoadErrorText
import ru.fet1sov.akinorev.model.PostLocales
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    onOpenProfile: (String) -> Unit,
    onProfileRenamed: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(username)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var emojiPickerOpen by rememberSaveable { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onAvatarPicked(uri.toString())
    }
    val pickAvatar = {
        avatarPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    LaunchedEffect(uiState.renamedTo) {
        val renamed = uiState.renamedTo ?: return@LaunchedEffect
        onProfileRenamed(renamed)
        viewModel.onRenameHandled()
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastIndex = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastIndex >= info.totalItemsCount - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd) viewModel.loadMorePosts()
            }
    }

    val shareCopied = stringResource(R.string.community_posts_share_copied)
    val shareError = stringResource(R.string.community_posts_share_error)
    val shareTitle = stringResource(R.string.community_posts_share_title, uiState.displayUsername)

    val sharePost: (String) -> Unit = { postId ->
        val url = BuildConfig.SITE_URL.trimEnd('/') + "/post/" + postId
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, shareTitle)
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
        when {
            uiState.isBanned -> ProfileMessageState(
                title = stringResource(R.string.community_profile_banned_title),
                body = stringResource(R.string.community_profile_banned_body),
                icon = AppIconKind.Block,
                danger = true,
            )

            uiState.profile == null && uiState.loadError != null -> {
                val error = uiState.loadError ?: ProfileLoadError.Unknown
                ProfileMessageState(
                    title = if (error == ProfileLoadError.NotFound) {
                        stringResource(R.string.community_profile_not_found_title)
                    } else {
                        stringResource(R.string.community_profile_load_error)
                    },
                    body = if (error == ProfileLoadError.NotFound) {
                        stringResource(R.string.community_profile_not_found_body)
                    } else {
                        profileLoadErrorText(error)
                    },
                    action = stringResource(R.string.community_profile_retry),
                    onAction = viewModel::retry,
                    icon = AppIconKind.CloudOff,
                )
            }

            uiState.profile == null -> ProfileLoading()

            else -> ProfileContent(
                uiState = uiState,
                palette = palette,
                listState = listState,
                onRefresh = viewModel::refresh,
                onOpenProfile = onOpenProfile,
                onPickAvatar = pickAvatar,
                onEditProfile = viewModel::startEditingProfile,
                onOpenEmojiPicker = { emojiPickerOpen = true },
                onEditUsernameChange = viewModel::onEditUsernameChange,
                onEditBioChange = viewModel::onEditBioChange,
                onEditNameColorChange = viewModel::onEditNameColorChange,
                onEditNameAnimationChange = viewModel::onEditNameAnimationChange,
                onSaveProfile = viewModel::saveProfile,
                onCancelEdit = viewModel::cancelEditingProfile,
                onFollow = viewModel::toggleFollow,
                onBlock = viewModel::toggleBlock,
                onComposerTextChange = viewModel::onComposerTextChange,
                onComposerLocaleChange = viewModel::onComposerLocaleChange,
                onComposerImagesPicked = viewModel::onComposerImagesPicked,
                onRemoveComposerImage = viewModel::removeComposerImage,
                onPublish = viewModel::publishPost,
                onRetryPosts = viewModel::refresh,
                onLoadMore = viewModel::loadMorePosts,
                onRetryLoadMore = viewModel::retryLoadMore,
                onLike = viewModel::togglePostLike,
                onCommentsCountChanged = viewModel::onCommentsCountChanged,
                onRepost = viewModel::togglePostRepost,
                onShare = sharePost,
                onReport = viewModel::openReport,
                onPin = viewModel::togglePinned,
                onEdit = viewModel::openPostEditor,
                onDelete = viewModel::askDeletePost,
                onRetryAction = viewModel::clearActionError,
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 104.dp, start = 16.dp, end = 16.dp),
        )

        ProfileDialogs(
            uiState = uiState,
            onDismissDelete = viewModel::cancelDeletePost,
            onConfirmDelete = viewModel::confirmDeletePost,
            onDismissEdit = viewModel::closePostEditor,
            onEditTextChange = viewModel::onEditorTextChange,
            onEditorLocaleChange = viewModel::onEditorLocaleChange,
            onEditorImagesPicked = viewModel::onEditorImagesPicked,
            onRemoveEditorImage = viewModel::removeEditorImage,
            onSaveEdit = viewModel::savePostEdit,
            onDismissRepost = viewModel::dismissRepost,
            onConfirmRepost = viewModel::confirmRepost,
            onDismissReport = viewModel::dismissReport,
            onReportReasonChange = viewModel::onReportReasonChange,
            onReportCommentChange = viewModel::onReportCommentChange,
            onSubmitReport = viewModel::submitReport,
        )

        if (emojiPickerOpen) {
            BioEmojiPickerDialog(
                onSelect = { name -> viewModel.appendBioEmoji(name) },
                onDismiss = { emojiPickerOpen = false },
            )
        }
    }
}

@Composable
private fun ProfileLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp),
    ) {
        ProfileHeaderSkeleton()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    palette: NavigationPalette,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onRefresh: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onPickAvatar: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onEditUsernameChange: (String) -> Unit,
    onEditBioChange: (String) -> Unit,
    onEditNameColorChange: (String) -> Unit,
    onEditNameAnimationChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onFollow: () -> Unit,
    onBlock: () -> Unit,
    onComposerTextChange: (String, String) -> Unit,
    onComposerLocaleChange: (String) -> Unit,
    onComposerImagesPicked: (List<String>) -> Unit,
    onRemoveComposerImage: (String) -> Unit,
    onPublish: () -> Unit,
    onRetryPosts: () -> Unit,
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onLike: (String) -> Unit,
    onCommentsCountChanged: (String, Int) -> Unit,
    onRepost: (String) -> Unit,
    onShare: (String) -> Unit,
    onReport: (String) -> Unit,
    onPin: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRetryAction: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item(key = HeaderKey) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProfileHeaderSection(
                        state = uiState,
                        onAvatarClick = onPickAvatar,
                        onEditProfile = onEditProfile,
                        onOpenEmojiPicker = onOpenEmojiPicker,
                        onEditUsernameChange = onEditUsernameChange,
                        onEditBioChange = onEditBioChange,
                        onEditNameColorChange = onEditNameColorChange,
                        onEditNameAnimationChange = onEditNameAnimationChange,
                        onSaveProfile = onSaveProfile,
                        onCancelEdit = onCancelEdit,
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    ProfileFollowers(count = uiState.profile?.followersCount ?: 0)

                    if (uiState.isAuthenticated && !uiState.isSelf) {
                        Spacer(modifier = Modifier.height(14.dp))
                        ProfileRelationActions(
                            uiState = uiState,
                            palette = palette,
                            onFollow = onFollow,
                            onBlock = onBlock,
                        )
                    }

                    val actionError = uiState.actionError
                    if (actionError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = profileActionErrorText(actionError),
                                color = DangerColor,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                            )
                            Surface(
                                onClick = onRetryAction,
                                shape = RoundedCornerShape(50),
                                color = palette.bar,
                            ) {
                                Text(
                                    text = stringResource(R.string.community_error_dismiss),
                                    color = palette.textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (!uiState.isBlocked) {
                if (uiState.canManagePosts) {
                    item(key = ComposerKey) {
                        ComposerSection(
                            uiState = uiState,
                            palette = palette,
                            onTextChange = onComposerTextChange,
                            onLocaleChange = onComposerLocaleChange,
                            onImagesPicked = onComposerImagesPicked,
                            onRemoveImage = onRemoveComposerImage,
                            onPublish = onPublish,
                            onRetry = onRetryAction,
                        )
                    }
                }

                val postsError = uiState.postsLoadError
                if (postsError != null) {
                    item(key = PostsErrorKey) {
                        ProfileInlineError(
                            text = profileLoadErrorText(postsError),
                            action = stringResource(R.string.community_profile_retry),
                            onAction = onRetryPosts,
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                        )
                    }
                }

                when {
                    uiState.isPostsLoading -> items(SkeletonPosts) {
                        PostCardSkeleton(
                            palette = palette,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                    }

                    uiState.posts.isEmpty() && postsError == null -> item(key = EmptyKey) {
                        Text(
                            text = stringResource(R.string.community_posts_empty),
                            color = palette.textMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                        )
                    }

                    else -> items(
                        items = uiState.posts,
                        key = { it.id },
                        contentType = { "post" },
                    ) { post ->
                        PostCard(
                            post = post,
                            emojiUrls = uiState.emojiUrls,
                            canManage = uiState.canManagePosts,
                            isPending = post.id in uiState.pendingPostIds,
                            isReported = post.target.id in uiState.reportedPostIds,
                            onOpenProfile = onOpenProfile,
                            onLike = { onLike(post.id) },
                            onCommentsCountChanged = { count -> onCommentsCountChanged(post.id, count) },
                            onRepost = { onRepost(post.id) },
                            onShare = { onShare(post.target.id) },
                            onReport = { onReport(post.id) },
                            onPin = { onPin(post.id) },
                            onEdit = { onEdit(post.id) },
                            onDelete = { onDelete(post.id) },
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                        )
                    }
                }

                if (uiState.hasMorePosts || uiState.isLoadingMore || uiState.loadMoreError != null) {
                    item(key = LoadMoreKey) {
                        LoadMoreFooter(
                            isLoading = uiState.isLoadingMore,
                            error = uiState.loadMoreError,
                            onLoadMore = onLoadMore,
                            onRetry = onRetryLoadMore,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRelationActions(
    uiState: ProfileUiState,
    palette: NavigationPalette,
    onFollow: () -> Unit,
    onBlock: () -> Unit,
) {
    val relation = uiState.relation
    val following = relation?.isFollowing == true
    val blocked = relation?.isBlocked == true
    val enabled = !uiState.isActionPending

    Row(
        modifier = Modifier.alpha(if (enabled) 1f else 0.6f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            onClick = onFollow,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            color = palette.idleContent,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AppIcon(
                    kind = AppIconKind.Heart,
                    tint = palette.bar,
                    contentDescription = null,
                    size = 20.dp,
                )
                Text(
                    text = stringResource(
                        if (following) {
                            R.string.community_profile_unfollow
                        } else {
                            R.string.community_profile_follow
                        },
                    ),
                    color = palette.bar,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Surface(
            onClick = onBlock,
            enabled = enabled,
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                AppIcon(
                    kind = AppIconKind.Block,
                    tint = if (blocked) DangerColor else palette.textPrimary,
                    contentDescription = stringResource(
                        if (blocked) {
                            R.string.community_profile_unblock
                        } else {
                            R.string.community_profile_block
                        },
                    ),
                    size = 22.dp,
                )
            }
        }
    }
}

@Composable
private fun ComposerSection(
    uiState: ProfileUiState,
    palette: NavigationPalette,
    onTextChange: (String, String) -> Unit,
    onLocaleChange: (String) -> Unit,
    onImagesPicked: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onPublish: () -> Unit,
    onRetry: () -> Unit,
) {
    val composer = uiState.composer
    val canPublish = !composer.isPublishing && composer.content.values.any { it.isNotBlank() }
    val error = composer.error

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PostComposer(
            text = composer.content[composer.activeLocale].orEmpty(),
            placeholder = stringResource(R.string.community_posts_placeholder),
            images = composer.images,
            onTextChange = { value -> onTextChange(composer.activeLocale, value) },
            onImagesPicked = onImagesPicked,
            onRemoveImage = onRemoveImage,
            enabled = !composer.isPublishing,
        )

        if (error != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = postActionErrorText(error),
                    color = DangerColor,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    onClick = onRetry,
                    shape = RoundedCornerShape(50),
                    color = palette.bar,
                ) {
                    Text(
                        text = stringResource(R.string.community_error_dismiss),
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PostLocales.forEach { locale ->
                    val selected = locale == composer.activeLocale
                    Surface(
                        onClick = { onLocaleChange(locale) },
                        shape = RoundedCornerShape(50),
                        color = if (selected) palette.idleContent else palette.bar,
                    ) {
                        Text(
                            text = locale.uppercase(),
                            color = if (selected) palette.bar else palette.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                onClick = onPublish,
                enabled = canPublish,
                shape = RoundedCornerShape(12.dp),
                color = palette.idleContent,
            ) {
                Text(
                    text = stringResource(
                        if (composer.isPublishing) {
                            R.string.community_posts_publishing
                        } else {
                            R.string.community_posts_publish
                        },
                    ),
                    color = palette.bar,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadMoreFooter(
    isLoading: Boolean,
    error: PostActionError?,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    if (error != null) {
        ProfileInlineError(
            text = postActionErrorText(error),
            action = stringResource(R.string.community_profile_retry),
            onAction = onRetry,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
        )
        return
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = navigationPalette().textPrimary,
            )
        }
        return
    }

    LaunchedEffect(Unit) { onLoadMore() }
    Spacer(modifier = Modifier.height(24.dp))
}

private val DangerColor = Color(0xFFE5484D)
private const val SkeletonPosts = 3
private const val HeaderKey = "profile-header"
private const val ComposerKey = "profile-composer"
private const val PostsErrorKey = "profile-posts-error"
private const val EmptyKey = "profile-posts-empty"
private const val LoadMoreKey = "profile-load-more"
