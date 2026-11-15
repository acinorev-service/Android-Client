package ru.fet1sov.akinorev.feature.profile.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.profile.PostActionError
import ru.fet1sov.akinorev.feature.profile.components.ProfileInlineError
import ru.fet1sov.akinorev.feature.profile.components.postActionErrorText
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.ui.components.CommunityRichText
import ru.fet1sov.akinorev.ui.components.StyledName
import ru.fet1sov.akinorev.ui.components.UserAvatar
import ru.fet1sov.akinorev.ui.format.formatCommentDate
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import java.util.Locale

@Composable
fun PostComments(
    username: String,
    postId: String,
    onCountChanged: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostCommentsViewModel = viewModel(
        key = "comments-$postId",
        factory = PostCommentsViewModel.factory(username, postId),
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()

    var rootText by rememberSaveable { mutableStateOf("") }
    var replyTargetId by rememberSaveable { mutableStateOf<String?>(null) }
    var replyText by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editText by rememberSaveable { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<PostComment?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PostCommentsEvent.Submitted -> {
                    if (event.parentId == null) {
                        rootText = ""
                    } else {
                        replyText = ""
                        replyTargetId = null
                    }
                }

                is PostCommentsEvent.Edited -> {
                    editingId = null
                    editText = ""
                }

                is PostCommentsEvent.Deleted -> deleteTarget = null
            }
        }
    }

    LaunchedEffect(state.total, state.isLoading, state.loadError) {
        if (!state.isLoading && state.loadError == null) {
            onCountChanged(state.total)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(palette.barBorder.copy(alpha = 0.6f)),
        )

        Text(
            text = stringResource(R.string.community_comments_title).uppercase() +
                if (state.total > 0) " · ${state.total}" else "",
            color = palette.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

        when {
            state.isLoading -> Text(
                text = stringResource(R.string.community_comments_loading),
                color = palette.textMuted,
                fontSize = 12.sp,
            )

            state.loadError != null -> ProfileInlineError(
                text = postActionErrorText(state.loadError ?: PostActionError.Generic),
                action = stringResource(R.string.community_profile_retry),
                onAction = viewModel::retry,
            )

            state.comments.isEmpty() -> Text(
                text = stringResource(R.string.community_comments_empty),
                color = palette.textMuted,
                fontSize = 12.sp,
            )

            else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.comments.forEach { comment ->
                    CommentNode(
                        comment = comment,
                        emojiUrls = state.emojiUrls,
                        isAuthenticated = state.isAuthenticated,
                        pendingLikeIds = state.pendingLikeIds,
                        updatingCommentId = state.updatingCommentId,
                        replyTargetId = replyTargetId,
                        replyText = replyText,
                        editingId = editingId,
                        editText = editText,
                        onReplyTextChange = { replyText = it.take(CommentMaxLength) },
                        onStartReply = { id ->
                            replyTargetId = id
                            replyText = ""
                            editingId = null
                            editText = ""
                        },
                        onCancelReply = {
                            replyTargetId = null
                            replyText = ""
                        },
                        onSendReply = { commentId ->
                            viewModel.submit(replyText, commentId)
                        },
                        onEditTextChange = { editText = it.take(CommentMaxLength) },
                        onStartEdit = { target ->
                            editingId = target.id
                            editText = target.content
                            replyTargetId = null
                            replyText = ""
                        },
                        onCancelEdit = {
                            editingId = null
                            editText = ""
                            viewModel.clearEditError()
                        },
                        onSaveEdit = { commentId ->
                            viewModel.updateComment(commentId, editText)
                        },
                        onLike = viewModel::toggleLike,
                        onDeleteRequest = { deleteTarget = it },
                    )
                }
            }
        }

        if (state.hasMore) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = viewModel::loadMore,
                    enabled = !state.isLoadingMore,
                ) {
                    Text(
                        text = stringResource(
                            if (state.isLoadingMore) {
                                R.string.community_comments_loading_more
                            } else {
                                R.string.community_comments_show_more
                            },
                        ),
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (state.isLoadingMore) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = palette.textPrimary,
                    )
                }
            }
        }

        state.loadMoreError?.let { error ->
            ProfileInlineError(
                text = postActionErrorText(error),
                action = stringResource(R.string.community_profile_retry),
                onAction = viewModel::retryLoadMore,
            )
        }

        state.editError?.let { error ->
            Text(
                text = postActionErrorText(error),
                color = ErrorColor,
                fontSize = 12.sp,
            )
        }

        state.actionError?.let { error ->
            ProfileInlineError(
                text = postActionErrorText(error),
                action = stringResource(R.string.community_profile_retry),
                onAction = viewModel::clearActionError,
            )
        }

        if (state.isAuthenticated) {
            OutlinedTextField(
                value = rootText,
                onValueChange = { rootText = it.take(CommentMaxLength) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !state.isSubmitting,
                placeholder = {
                    Text(
                        text = stringResource(R.string.community_comments_placeholder),
                        color = palette.textMuted,
                        fontSize = 13.sp,
                    )
                },
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.textMuted,
                    unfocusedBorderColor = palette.barBorder,
                    focusedContainerColor = palette.screen,
                    unfocusedContainerColor = palette.screen,
                    cursorColor = palette.textPrimary,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary,
                    disabledTextColor = palette.textMuted,
                ),
            )

            state.submitError?.let { error ->
                Text(
                    text = postActionErrorText(error),
                    color = ErrorColor,
                    fontSize = 12.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = {
                        rootText = ""
                        viewModel.clearSubmitError()
                        onClose()
                    },
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                ) {
                    Text(
                        text = stringResource(R.string.community_comments_cancel),
                        color = palette.textPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    onClick = { viewModel.submit(rootText) },
                    enabled = !state.isSubmitting && rootText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    color = palette.idleContent,
                ) {
                    Text(
                        text = stringResource(
                            if (state.isSubmitting) {
                                R.string.community_comments_sending
                            } else {
                                R.string.community_comments_send
                            },
                        ),
                        color = palette.bar,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }
            }
        }

        val currentDelete = deleteTarget
        if (currentDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    if (state.deletingCommentId == null) deleteTarget = null
                },
                title = {
                    Text(
                        text = stringResource(R.string.community_comments_delete_title),
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.community_comments_delete_message),
                            color = palette.textMuted,
                            fontSize = 14.sp,
                        )
                        state.deleteError?.let { error ->
                            Text(
                                text = postActionErrorText(error),
                                color = ErrorColor,
                                fontSize = 13.sp,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearDeleteError()
                            viewModel.deleteComment(currentDelete.id)
                        },
                        enabled = state.deletingCommentId == null,
                    ) {
                        Text(
                            text = stringResource(
                                if (state.deletingCommentId != null) {
                                    R.string.community_comments_deleting
                                } else {
                                    R.string.community_comments_delete_confirm
                                },
                            ),
                            color = ErrorColor,
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            deleteTarget = null
                            viewModel.clearDeleteError()
                        },
                        enabled = state.deletingCommentId == null,
                    ) {
                        Text(
                            text = stringResource(R.string.community_comments_delete_cancel),
                            color = palette.textPrimary,
                        )
                    }
                },
                containerColor = palette.bar,
            )
        }
    }
}

@Composable
private fun CommentNode(
    comment: PostComment,
    emojiUrls: Map<String, String>,
    isAuthenticated: Boolean,
    pendingLikeIds: Set<String>,
    updatingCommentId: String?,
    replyTargetId: String?,
    replyText: String,
    editingId: String?,
    editText: String,
    onReplyTextChange: (String) -> Unit,
    onStartReply: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSendReply: (String) -> Unit,
    onEditTextChange: (String) -> Unit,
    onStartEdit: (PostComment) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveEdit: (String) -> Unit,
    onLike: (String) -> Unit,
    onDeleteRequest: (PostComment) -> Unit,
) {
    val palette = navigationPalette()
    var visibleReplies by rememberSaveable(comment.id) { mutableIntStateOf(RepliesPageSize) }
    val isEditing = editingId == comment.id
    val isReplying = replyTargetId == comment.id
    val isUpdating = updatingCommentId == comment.id

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            UserAvatar(
                avatar = comment.author?.avatarPath.orEmpty(),
                userId = comment.author?.id.orEmpty(),
                username = comment.author?.username.orEmpty(),
                size = 30.dp,
                isSubscribed = comment.author?.isSubscribed == true,
                shape = CircleShape,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val author = comment.author
                    if (author != null && author.username.isNotBlank()) {
                        StyledName(
                            text = author.username,
                            nameColor = if (author.isSubscribed) author.nameColor else "",
                            nameAnimation = if (author.isSubscribed) author.nameAnimation else "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fallbackColor = palette.textPrimary,
                            maxLines = 1,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.community_comments_deleted_author),
                            color = palette.textMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Text(
                        text = formatCommentDate(comment.createdAtMillis, Locale.getDefault()),
                        color = palette.textMuted,
                        fontSize = 10.sp,
                    )

                    if (comment.isEdited) {
                        Text(
                            text = stringResource(R.string.community_comments_edited),
                            color = palette.textMuted,
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
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
                }

                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = onEditTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isUpdating,
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.textMuted,
                            unfocusedBorderColor = palette.barBorder,
                            focusedContainerColor = palette.screen,
                            unfocusedContainerColor = palette.screen,
                            cursorColor = palette.textPrimary,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onCancelEdit, enabled = !isUpdating) {
                            Text(
                                text = stringResource(R.string.community_comments_cancel),
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                            )
                        }
                        TextButton(
                            onClick = { onSaveEdit(comment.id) },
                            enabled = !isUpdating && editText.isNotBlank(),
                        ) {
                            Text(
                                text = stringResource(
                                    if (isUpdating) {
                                        R.string.community_comments_saving
                                    } else {
                                        R.string.community_comments_save
                                    },
                                ),
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    CommunityRichText(
                        text = comment.content,
                        emojiUrls = emojiUrls,
                        style = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        emojiSize = 15.sp,
                    )

                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (isAuthenticated) {
                            CommentAction(
                                text = stringResource(
                                    if (isReplying) {
                                        R.string.community_comments_cancel
                                    } else {
                                        R.string.community_comments_reply
                                    },
                                ),
                                onClick = {
                                    if (isReplying) onCancelReply() else onStartReply(comment.id)
                                },
                            )
                        }

                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Surface(
                                onClick = { onLike(comment.id) },
                                shape = CircleShape,
                                color = Color.Transparent,
                                enabled = comment.id !in pendingLikeIds,
                            ) {
                                AppIcon(
                                    kind = AppIconKind.Heart,
                                    tint = if (comment.likedByMe) LikeColor else palette.textPrimary.copy(alpha = 0.7f),
                                    contentDescription = stringResource(
                                        if (comment.likedByMe) {
                                            R.string.community_comments_unlike
                                        } else {
                                            R.string.community_comments_like
                                        },
                                    ),
                                    size = 12.dp,
                                )
                            }
                            Text(
                                text = comment.likesCount.toString(),
                                color = if (comment.likedByMe) LikeColor else palette.textMuted,
                                fontSize = 11.sp,
                            )
                        }

                        if (comment.canEdit && !isEditing) {
                            CommentAction(
                                text = stringResource(R.string.community_comments_edit),
                                onClick = { onStartEdit(comment) },
                            )
                        }

                        if (comment.canDelete) {
                            CommentAction(
                                text = stringResource(R.string.community_comments_delete),
                                color = ErrorColor,
                                onClick = { onDeleteRequest(comment) },
                            )
                        }
                    }
                }

                if (isReplying) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = onReplyTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isUpdating,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.community_comments_reply_placeholder),
                                color = palette.textMuted,
                                fontSize = 12.sp,
                            )
                        },
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = palette.textMuted,
                            unfocusedBorderColor = palette.barBorder,
                            focusedContainerColor = palette.screen,
                            unfocusedContainerColor = palette.screen,
                            cursorColor = palette.textPrimary,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onCancelReply) {
                            Text(
                                text = stringResource(R.string.community_comments_cancel),
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                            )
                        }
                        TextButton(
                            onClick = { onSendReply(comment.id) },
                            enabled = replyText.isNotBlank(),
                        ) {
                            Text(
                                text = stringResource(R.string.community_comments_send),
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        if (comment.children.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                comment.children.take(visibleReplies).forEach { child ->
                    CommentNode(
                        comment = child,
                        emojiUrls = emojiUrls,
                        isAuthenticated = isAuthenticated,
                        pendingLikeIds = pendingLikeIds,
                        updatingCommentId = updatingCommentId,
                        replyTargetId = replyTargetId,
                        replyText = replyText,
                        editingId = editingId,
                        editText = editText,
                        onReplyTextChange = onReplyTextChange,
                        onStartReply = onStartReply,
                        onCancelReply = onCancelReply,
                        onSendReply = onSendReply,
                        onEditTextChange = onEditTextChange,
                        onStartEdit = onStartEdit,
                        onCancelEdit = onCancelEdit,
                        onSaveEdit = onSaveEdit,
                        onLike = onLike,
                        onDeleteRequest = onDeleteRequest,
                    )
                }

                val hidden = comment.children.size - visibleReplies
                if (hidden > 0) {
                    CommentAction(
                        text = stringResource(R.string.community_comments_show_more_replies, hidden),
                        onClick = { visibleReplies += RepliesPageSize },
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentAction(
    text: String,
    color: Color = Color.Unspecified,
    onClick: () -> Unit,
) {
    val palette = navigationPalette()
    TextButton(onClick = onClick) {
        Text(
            text = text,
            color = if (color == Color.Unspecified) palette.textPrimary else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private const val CommentMaxLength = 2000
private const val RepliesPageSize = 2
private val LikeColor = Color(0xFFEF4444)
private val ErrorColor = Color(0xFFE5484D)
