package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.data.community.MaxPostImages
import ru.fet1sov.akinorev.feature.profile.ComposerImageUi
import ru.fet1sov.akinorev.feature.profile.ProfileUiState
import ru.fet1sov.akinorev.feature.profile.components.postActionErrorText
import ru.fet1sov.akinorev.ui.components.StyledName
import ru.fet1sov.akinorev.ui.components.UserAvatar
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import java.util.UUID

@Composable
fun ProfileDialogs(
    uiState: ProfileUiState,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissEdit: () -> Unit,
    onEditTextChange: (String, String) -> Unit,
    onEditorLocaleChange: (String) -> Unit,
    onEditorImagesPicked: (List<String>) -> Unit,
    onRemoveEditorImage: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onDismissRepost: () -> Unit,
    onConfirmRepost: (String, List<String>) -> Unit,
    onDismissReport: () -> Unit,
    onReportReasonChange: (String) -> Unit,
    onReportCommentChange: (String) -> Unit,
    onSubmitReport: () -> Unit,
) {
    if (uiState.pendingDeletePostId != null) {
        DeletePostDialog(
            uiState = uiState,
            onDismiss = onDismissDelete,
            onConfirm = onConfirmDelete,
        )
    }

    if (uiState.postEditor.isOpen) {
        EditPostDialog(
            uiState = uiState,
            onDismiss = onDismissEdit,
            onTextChange = onEditTextChange,
            onLocaleChange = onEditorLocaleChange,
            onImagesPicked = onEditorImagesPicked,
            onRemoveImage = onRemoveEditorImage,
            onSave = onSaveEdit,
        )
    }

    if (uiState.repost.isOpen) {
        RepostDialog(
            uiState = uiState,
            onDismiss = onDismissRepost,
            onConfirm = onConfirmRepost,
        )
    }

    if (uiState.report.isOpen) {
        ReportDialog(
            uiState = uiState,
            onDismiss = onDismissReport,
            onReasonChange = onReportReasonChange,
            onCommentChange = onReportCommentChange,
            onSubmit = onSubmitReport,
        )
    }
}

@Composable
private fun DeletePostDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val palette = navigationPalette()
    ProfileDialogCard(
        title = stringResource(R.string.community_posts_delete_title),
        onDismiss = onDismiss,
        dismissEnabled = !uiState.isDeletingPost,
    ) {
        Text(
            text = stringResource(R.string.community_posts_delete_message),
            color = palette.textMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        uiState.postError?.let { error ->
            Text(
                text = postActionErrorText(error),
                color = DangerColor,
                fontSize = 13.sp,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogAction(
                text = stringResource(R.string.community_posts_delete_cancel),
                enabled = !uiState.isDeletingPost,
                onClick = onDismiss,
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogAction(
                text = stringResource(
                    if (uiState.isDeletingPost) {
                        R.string.community_posts_deleting
                    } else {
                        R.string.community_posts_delete_confirm
                    },
                ),
                enabled = !uiState.isDeletingPost,
                destructive = true,
                onClick = onConfirm,
            )
        }
    }
}

@Composable
private fun EditPostDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onTextChange: (String, String) -> Unit,
    onLocaleChange: (String) -> Unit,
    onImagesPicked: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onSave: () -> Unit,
) {
    val editor = uiState.postEditor
    val palette = navigationPalette()
    val hasContent = editor.content.values.any { it.isNotBlank() }

    ProfileDialogCard(
        title = stringResource(R.string.community_posts_edit_title),
        onDismiss = onDismiss,
        dismissEnabled = !editor.isSaving,
    ) {
        PostComposer(
            text = editor.content[editor.activeLocale].orEmpty(),
            placeholder = stringResource(R.string.community_posts_placeholder),
            images = editor.images,
            onTextChange = { value -> onTextChange(editor.activeLocale, value) },
            onImagesPicked = onImagesPicked,
            onRemoveImage = onRemoveImage,
            enabled = !editor.isSaving,
        )

        editor.error?.let { error ->
            Text(
                text = postActionErrorText(error),
                color = DangerColor,
                fontSize = 13.sp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PostLocaleSelector(
                activeLocale = editor.activeLocale,
                onLocaleChange = onLocaleChange,
            )
            Spacer(modifier = Modifier.weight(1f))
            DialogAction(
                text = stringResource(R.string.community_posts_delete_cancel),
                enabled = !editor.isSaving,
                onClick = onDismiss,
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogAction(
                text = stringResource(
                    if (editor.isSaving) {
                        R.string.community_posts_saving
                    } else {
                        R.string.community_posts_save
                    },
                ),
                enabled = !editor.isSaving && hasContent,
                primary = true,
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun RepostDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit,
) {
    val post = uiState.repost.post ?: return
    val author = post.target.author
    val palette = navigationPalette()

    key(post.id) {
        var caption by remember { mutableStateOf("") }
        var images by remember { mutableStateOf<List<ComposerImageUi>>(emptyList()) }

        ProfileDialogCard(
            title = stringResource(R.string.community_posts_repost_title),
            onDismiss = onDismiss,
            dismissEnabled = !uiState.repost.isSubmitting,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UserAvatar(
                    avatar = author.avatarPath,
                    userId = author.id,
                    username = author.username,
                    size = 32.dp,
                    isSubscribed = author.isSubscribed,
                    shape = CircleShape,
                )
                Column {
                    Text(
                        text = stringResource(R.string.community_posts_repost_hint),
                        color = palette.textMuted,
                        fontSize = 12.sp,
                    )
                    if (author.username.isNotBlank()) {
                        StyledName(
                            text = "@${author.username}",
                            nameColor = if (author.isSubscribed) author.nameColor else "",
                            nameAnimation = if (author.isSubscribed) author.nameAnimation else "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fallbackColor = palette.textPrimary,
                        )
                    }
                }
            }

            PostComposer(
                text = caption,
                placeholder = stringResource(R.string.community_posts_repost_placeholder),
                images = images,
                onTextChange = { caption = it },
                onImagesPicked = { uris ->
                    val remaining = MaxPostImages - images.size
                    if (remaining > 0) {
                        images = images + uris.take(remaining).map { uri ->
                            ComposerImageUi(id = UUID.randomUUID().toString(), uri = uri)
                        }
                    }
                },
                onRemoveImage = { id -> images = images.filterNot { it.id == id } },
                enabled = !uiState.repost.isSubmitting,
            )

            uiState.repost.error?.let { error ->
                Text(
                    text = postActionErrorText(error),
                    color = DangerColor,
                    fontSize = 13.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DialogAction(
                    text = stringResource(R.string.community_posts_delete_cancel),
                    enabled = !uiState.repost.isSubmitting,
                    onClick = onDismiss,
                )
                Spacer(modifier = Modifier.width(8.dp))
                DialogAction(
                    text = stringResource(
                        if (uiState.repost.isSubmitting) {
                            R.string.community_posts_reposting
                        } else {
                            R.string.community_posts_repost
                        },
                    ),
                    enabled = !uiState.repost.isSubmitting,
                    primary = true,
                    onClick = { onConfirm(caption, images.mapNotNull { it.uri }) },
                )
            }
        }
    }
}

@Composable
private fun ReportDialog(
    uiState: ProfileUiState,
    onDismiss: () -> Unit,
    onReasonChange: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val report = uiState.report
    val palette = navigationPalette()

    ProfileDialogCard(
        title = stringResource(R.string.community_posts_report_title),
        onDismiss = onDismiss,
        dismissEnabled = !report.isSubmitting,
    ) {
        Text(
            text = stringResource(R.string.community_posts_report_hint),
            color = palette.textMuted,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )

        ReportReasons.forEach { reason ->
            val selected = report.reason == reason.key
            val color = if (selected) DangerColor else palette.textPrimary
            Surface(
                onClick = { onReasonChange(reason.key) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) DangerColor.copy(alpha = 0.1f) else Color.Transparent,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) DangerColor else palette.barBorder,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(
                                width = 2.dp,
                                color = if (selected) DangerColor else palette.barBorder,
                                shape = CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(DangerColor, CircleShape),
                            )
                        }
                    }
                    Text(
                        text = stringResource(reason.labelRes),
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }

        OutlinedTextField(
            value = report.comment,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            enabled = !report.isSubmitting,
            placeholder = {
                Text(
                    text = stringResource(R.string.community_posts_report_comment_placeholder),
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

        report.error?.let { error ->
            Text(
                text = postActionErrorText(error),
                color = DangerColor,
                fontSize = 13.sp,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DialogAction(
                text = stringResource(R.string.community_posts_delete_cancel),
                enabled = !report.isSubmitting,
                onClick = onDismiss,
            )
            Spacer(modifier = Modifier.width(8.dp))
            DialogAction(
                text = stringResource(
                    if (report.isSubmitting) {
                        R.string.community_posts_report_sending
                    } else {
                        R.string.community_posts_report_submit
                    },
                ),
                enabled = report.canSubmit,
                destructive = true,
                onClick = onSubmit,
            )
        }
    }
}

@Composable
private fun ProfileDialogCard(
    title: String,
    onDismiss: () -> Unit,
    dismissEnabled: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = navigationPalette()
    Dialog(
        onDismissRequest = { if (dismissEnabled) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = palette.bar,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 620.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = title,
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            onClick = onDismiss,
                            enabled = dismissEnabled,
                            shape = CircleShape,
                            color = Color.Transparent,
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppIcon(
                                    kind = AppIconKind.Close,
                                    tint = palette.textPrimary,
                                    contentDescription = null,
                                    size = 18.dp,
                                )
                            }
                        }
                    }

                    content()
                }
            }
        }
    }
}

@Composable
private fun DialogAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    primary: Boolean = false,
    destructive: Boolean = false,
) {
    val palette = navigationPalette()
    val background = when {
        destructive -> DangerColor
        primary -> palette.idleContent
        else -> Color.Transparent
    }
    val contentColor = when {
        destructive -> Color.White
        primary -> palette.bar
        else -> palette.textPrimary
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        color = background,
        border = if (!primary && !destructive) BorderStroke(1.dp, palette.barBorder) else null,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

private data class ReportReasonOption(
    val key: String,
    val labelRes: Int,
)

private val ReportReasons = listOf(
    ReportReasonOption("spam", R.string.community_posts_report_reason_spam),
    ReportReasonOption("harassment", R.string.community_posts_report_reason_harassment),
    ReportReasonOption("hate", R.string.community_posts_report_reason_hate),
    ReportReasonOption("violence", R.string.community_posts_report_reason_violence),
    ReportReasonOption("sexual", R.string.community_posts_report_reason_sexual),
    ReportReasonOption("misinformation", R.string.community_posts_report_reason_misinformation),
    ReportReasonOption("other", R.string.community_posts_report_reason_other),
)

private val DangerColor = Color(0xFFE5484D)
