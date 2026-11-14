package ru.fet1sov.akinorev.feature.emojis

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.LocalImage
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette
import ru.fet1sov.akinorev.ui.theme.ErrorRed

@Composable
fun EmojiCreateScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmojiCreateViewModel = viewModel(factory = EmojiCreateViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.onFilePicked(uri.toString())
    }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    val requestClose: () -> Unit = {
        if (uiState.hasDraft) {
            showDiscardDialog = true
        } else {
            onClose()
        }
    }

    BackHandler(enabled = uiState.hasDraft && !showDiscardDialog) {
        showDiscardDialog = true
    }

    LaunchedEffect(uiState.isCreated) {
        if (uiState.isCreated) onClose()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = requestClose,
                shape = CircleShape,
                color = palette.barBorder.copy(alpha = 0.35f),
                contentColor = palette.textPrimary,
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        kind = AppIconKind.ArrowLeft,
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.emojis_create_title).uppercase(),
                    color = palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = stringResource(R.string.emojis_create_subtitle),
                    color = palette.textMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
            }
        }

        if (uiState.isAuthenticated && !uiState.isSubscribed) {
            EmojiLockedBanner()
        }

        EmojiFilePickerCard(
            uiState = uiState,
            onPickFile = { picker.launch(EmojiImageMimeTypes) },
        )

        EmojiTextInput(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = stringResource(R.string.emojis_name),
            placeholder = stringResource(R.string.emojis_name_placeholder),
            enabled = uiState.isSubscribed,
            helper = {
                if (uiState.isNameRejected) {
                    Text(
                        text = stringResource(R.string.emojis_error_name_invalid),
                        color = ErrorRed,
                        fontSize = 12.sp,
                    )
                } else {
                    Row {
                        Text(
                            text = stringResource(R.string.emojis_name_hint) + " ",
                            color = palette.textMuted,
                            fontSize = 12.sp,
                        )
                        if (uiState.nameMarker.isNotBlank()) {
                            Text(
                                text = uiState.nameMarker,
                                color = palette.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            },
        )

        EmojiTextInput(
            value = uiState.title,
            onValueChange = viewModel::onTitleChange,
            label = stringResource(R.string.emojis_title_label),
            placeholder = stringResource(R.string.emojis_title_placeholder),
            enabled = uiState.isSubscribed,
        )

        EmojiTextInput(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            label = stringResource(R.string.emojis_description_label),
            placeholder = stringResource(R.string.emojis_description_placeholder),
            singleLine = false,
            minHeight = 88.dp,
            enabled = uiState.isSubscribed,
        )

        uiState.error?.let { error ->
            Text(
                text = stringResource(error.messageRes()),
                color = ErrorRed,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }

        EmojiPrimaryButton(
            text = stringResource(
                if (uiState.isUploading) R.string.emojis_uploading else R.string.emojis_upload,
            ).uppercase(),
            onClick = viewModel::submit,
            enabled = uiState.canSubmit,
            isLoading = uiState.isUploading,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            palette = palette,
            onConfirm = {
                showDiscardDialog = false
                onClose()
            },
            onDismiss = { showDiscardDialog = false },
        )
    }
}

@Composable
private fun DiscardChangesDialog(
    palette: NavigationPalette,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val titleColor = if (palette.bar.luminance() > 0.5f) WarningYellowOnLight else WarningYellowOnDark

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.bar,
        titleContentColor = titleColor,
        textContentColor = palette.textMuted,
        title = {
            Text(
                text = stringResource(R.string.emojis_discard_title),
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.emojis_discard_message),
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.emojis_discard_confirm),
                    color = palette.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.emojis_cancel),
                    color = palette.textMuted,
                )
            }
        },
    )
}

@Composable
private fun EmojiFilePickerCard(
    uiState: EmojiCreateUiState,
    onPickFile: () -> Unit,
) {
    val palette = navigationPalette()
    val shape = RoundedCornerShape(14.dp)

    Surface(
        onClick = onPickFile,
        enabled = !uiState.isReadingFile,
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, palette.barBorder.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 132.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.pickedUri.isNotBlank()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LocalImage(
                        uri = uiState.pickedUri,
                        contentDescription = uiState.pickedFileName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(84.dp),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Text(
                        text = uiState.pickedFileName,
                        color = palette.textMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.emojis_replace_file),
                        color = palette.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (uiState.isReadingFile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = palette.accent,
                        )
                    } else {
                        AppIcon(
                            kind = AppIconKind.Plus,
                            tint = palette.textMuted,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.emojis_pick_file),
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = stringResource(R.string.emojis_pick_file_hint),
                        color = palette.textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@StringRes
private fun EmojiCreateError.messageRes(): Int = when (this) {
    EmojiCreateError.Format -> R.string.emojis_error_format
    EmojiCreateError.Size -> R.string.emojis_error_size
    EmojiCreateError.FileUnreadable -> R.string.emojis_error_read_file
    EmojiCreateError.NameTaken -> R.string.emojis_error_name_taken
    EmojiCreateError.SubscriptionRequired -> R.string.emojis_locked_description
    EmojiCreateError.Server -> R.string.emojis_error_server
    EmojiCreateError.Unknown -> R.string.emojis_error_unknown
}

private val EmojiImageMimeTypes = arrayOf(
    "image/gif",
    "image/png",
    "image/webp",
    "image/jpeg",
)

private val WarningYellowOnLight = Color(0xFFB07C00)
private val WarningYellowOnDark = Color(0xFFFFC93C)
