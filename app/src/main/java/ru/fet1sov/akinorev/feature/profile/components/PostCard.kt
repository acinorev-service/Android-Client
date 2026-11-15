package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.profile.comments.PostComments
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.PostAuthor
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.model.localizedPostText
import ru.fet1sov.akinorev.ui.components.CommunityRichText
import ru.fet1sov.akinorev.ui.components.StyledName
import ru.fet1sov.akinorev.ui.components.UserAvatar
import ru.fet1sov.akinorev.ui.format.rememberCompactCount
import ru.fet1sov.akinorev.ui.format.rememberPostTimestamp
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: CommunityPost,
    emojiUrls: Map<String, String>,
    canManage: Boolean,
    isPending: Boolean,
    isReported: Boolean,
    onOpenProfile: (String) -> Unit,
    onLike: () -> Unit,
    onCommentsCountChanged: (Int) -> Unit,
    onRepost: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val target = post.target
    val isRepost = post.isRepost && post.repostOf != null
    val language = Locale.getDefault().language
    val content = localizedPostText(target.content, language)
    val repostCaption = localizedPostText(post.repostContent, language)
    val originalAuthor = post.repostOf?.author
    val headerAuthor = if (isRepost) (originalAuthor ?: post.author) else post.author
    var commentsOpen by rememberSaveable(post.id) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomEnd = 14.dp, bottomStart = 4.dp))
                .background(palette.bar)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = headerAuthor.username.isNotBlank(),
                            onClick = { onOpenProfile(headerAuthor.username) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    UserAvatar(
                        avatar = headerAuthor.avatarPath,
                        userId = headerAuthor.id,
                        username = headerAuthor.username,
                        size = 40.dp,
                        isSubscribed = headerAuthor.isSubscribed,
                        shape = CircleShape,
                    )
                    AuthorName(
                        author = headerAuthor,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isRepost) {
                        AppIcon(
                            kind = AppIconKind.Repost,
                            tint = RepostColor,
                            contentDescription = stringResource(R.string.community_posts_repost),
                            modifier = Modifier.size(16.dp),
                            size = 16.dp,
                        )
                    }
                }

                if (canManage) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ManageButton(
                            icon = AppIconKind.Pin,
                            tint = if (post.isPinned) PinnedColor else palette.textPrimary,
                            contentDescription = stringResource(
                                if (post.isPinned) {
                                    R.string.community_posts_unpin
                                } else {
                                    R.string.community_posts_pin
                                },
                            ),
                            enabled = !isPending,
                            onClick = onPin,
                        )
                        if (!isRepost) {
                            ManageButton(
                                icon = AppIconKind.Appearance,
                                tint = palette.textPrimary,
                                contentDescription = stringResource(R.string.community_posts_edit),
                                enabled = !isPending,
                                onClick = onEdit,
                            )
                        }
                        ManageButton(
                            icon = AppIconKind.Trashbin,
                            tint = palette.textMuted,
                            contentDescription = stringResource(R.string.community_posts_delete),
                            enabled = !isPending,
                            onClick = onDelete,
                        )
                    }
                }
            }

            if (isRepost && (repostCaption.isNotBlank() || post.repostImages.isNotEmpty())) {
                PostContent(
                    source = repostCaption,
                    images = post.repostImages,
                    emojiUrls = emojiUrls,
                )
            }

            PostContent(
                source = content,
                images = target.images,
                emojiUrls = emojiUrls,
            )

            PostActions(
                post = post,
                target = target,
                isReported = isReported,
                isPending = isPending,
                canManage = canManage,
                onLike = onLike,
                onToggleComments = { commentsOpen = !commentsOpen },
                onRepost = onRepost,
                onShare = onShare,
                onReport = onReport,
            )

            if (!commentsOpen && target.commentsPreview.isNotEmpty()) {
                CommentsPreview(
                    comments = target.commentsPreview,
                    emojiUrls = emojiUrls,
                    total = target.commentsCount,
                    onShowAll = { commentsOpen = true },
                )
            }

            if (commentsOpen) {
                PostComments(
                    username = target.author.username,
                    postId = target.id,
                    onCountChanged = onCommentsCountChanged,
                    onClose = { commentsOpen = false },
                )
            }
        }

        PostFooter(post = post, views = target.views)
    }
}

@Composable
private fun AuthorName(
    author: PostAuthor,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    StyledName(
        text = author.username,
        nameColor = if (author.isSubscribed) author.nameColor else "",
        nameAnimation = if (author.isSubscribed) author.nameAnimation else "",
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fallbackColor = palette.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostActions(
    post: CommunityPost,
    target: CommunityPost,
    isReported: Boolean,
    isPending: Boolean,
    canManage: Boolean,
    onLike: () -> Unit,
    onToggleComments: () -> Unit,
    onRepost: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
) {
    val palette = navigationPalette()
    val likeTint = if (target.likedByMe) LikeColor else palette.textPrimary.copy(alpha = 0.55f)
    val repostTint = if (target.repostedByMe) RepostColor else palette.textPrimary.copy(alpha = 0.55f)
    val commentTint = palette.textPrimary.copy(alpha = 0.55f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PostActionItem(
            icon = AppIconKind.Heart,
            tint = likeTint,
            text = rememberCompactCount(target.likesCount),
            contentDescription = stringResource(
                if (target.likedByMe) R.string.community_posts_unlike else R.string.community_posts_like,
            ),
            enabled = !isPending,
            onClick = onLike,
        )

        PostActionItem(
            icon = AppIconKind.Messages,
            tint = commentTint,
            text = stringResource(R.string.community_posts_comments),
            contentDescription = stringResource(R.string.community_posts_comments),
            enabled = true,
            onClick = onToggleComments,
        )

        PostActionItem(
            icon = AppIconKind.Repost,
            tint = repostTint,
            text = rememberCompactCount(target.repostsCount),
            contentDescription = stringResource(
                if (target.repostedByMe) R.string.community_posts_unrepost else R.string.community_posts_repost,
            ),
            enabled = !isPending,
            onClick = onRepost,
        )

        Spacer(modifier = Modifier.weight(1f))

        PostActionItem(
            icon = AppIconKind.Share,
            tint = ShareColor,
            text = null,
            contentDescription = stringResource(R.string.community_posts_share),
            enabled = true,
            onClick = onShare,
        )

        if (!canManage) {
            PostActionItem(
                icon = AppIconKind.Flag,
                tint = if (isReported) ReportColor else ReportColor.copy(alpha = 0.6f),
                text = null,
                contentDescription = stringResource(R.string.community_posts_report),
                enabled = !isReported,
                onClick = onReport,
            )
        }
    }
}

@Composable
private fun PostActionItem(
    icon: AppIconKind,
    tint: Color,
    text: String?,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            AppIcon(
                kind = icon,
                tint = tint,
                contentDescription = contentDescription,
                size = 16.dp,
            )
            if (text != null) {
                Text(
                    text = text,
                    color = tint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostFooter(
    post: CommunityPost,
    views: Int,
) {
    val palette = navigationPalette()
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (post.isPinned) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AppIcon(
                    kind = AppIconKind.Pin,
                    tint = PinnedColor,
                    contentDescription = null,
                    size = 14.dp,
                )
                Text(
                    text = stringResource(R.string.community_posts_pinned),
                    color = PinnedColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppIcon(
                kind = AppIconKind.Calendar,
                tint = palette.textMuted,
                contentDescription = null,
                size = 14.dp,
            )
            Text(
                text = rememberPostTimestamp(post.createdAtMillis),
                color = palette.textMuted,
                fontSize = 12.sp,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            AppIcon(
                kind = AppIconKind.Eye,
                tint = palette.textMuted,
                contentDescription = null,
                size = 14.dp,
            )
            Text(
                text = rememberCompactCount(views),
                color = palette.textMuted,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun CommentsPreview(
    comments: List<PostComment>,
    emojiUrls: Map<String, String>,
    total: Int,
    onShowAll: () -> Unit,
) {
    val palette = navigationPalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.barBorder.copy(alpha = 0.6f)),
        )
        comments.forEach { comment ->
            CommentPreviewItem(comment = comment, emojiUrls = emojiUrls)
        }
        if (total > comments.size) {
            Surface(
                onClick = onShowAll,
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent,
            ) {
                Text(
                    text = stringResource(R.string.community_comments_show_all, total),
                    color = palette.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun CommentPreviewItem(
    comment: PostComment,
    emojiUrls: Map<String, String>,
) {
    val palette = navigationPalette()
    val author = comment.author

    Row(verticalAlignment = Alignment.Top) {
        UserAvatar(
            avatar = author?.avatarPath.orEmpty(),
            userId = author?.id.orEmpty(),
            username = author?.username.orEmpty(),
            size = 24.dp,
            isSubscribed = author?.isSubscribed == true,
            shape = CircleShape,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (author != null && author.username.isNotBlank()) {
                    StyledName(
                        text = author.username,
                        nameColor = if (author.isSubscribed) author.nameColor else "",
                        nameAnimation = if (author.isSubscribed) author.nameAnimation else "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fallbackColor = palette.textPrimary,
                        maxLines = 1,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.community_comments_deleted_author),
                        color = palette.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (comment.likesCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        AppIcon(
                            kind = AppIconKind.Heart,
                            tint = LikeColor,
                            contentDescription = null,
                            size = 10.dp,
                        )
                        Text(
                            text = comment.likesCount.toString(),
                            color = LikeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                if (comment.isEdited) {
                    Text(
                        text = stringResource(R.string.community_comments_edited),
                        color = palette.textMuted,
                        fontSize = 10.sp,
                        fontStyle = FontStyle.Italic,
                    )
                }
            }

            CommunityRichText(
                text = comment.content,
                emojiUrls = emojiUrls,
                style = androidx.compose.ui.text.TextStyle(
                    color = palette.textPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 1.dp),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                emojiSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ManageButton(
    icon: AppIconKind,
    tint: Color,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                kind = icon,
                tint = tint,
                contentDescription = contentDescription,
                size = 18.dp,
            )
        }
    }
}

private val LikeColor = Color(0xFFEF4444)
private val RepostColor = Color(0xFF22C55E)
private val ShareColor = Color(0xFF3B82F6)
private val ReportColor = Color(0xFFB91C1C)
private val PinnedColor = Color(0xFFF59E0B)
