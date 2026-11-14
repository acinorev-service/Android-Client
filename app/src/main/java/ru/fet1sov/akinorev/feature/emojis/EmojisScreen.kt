package ru.fet1sov.akinorev.feature.emojis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun EmojisScreen(
    onOpenCreate: () -> Unit,
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EmojisViewModel = viewModel(factory = EmojisViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val gridState = rememberLazyGridState()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<EmojiUi?>(null) }

    val deletedMessage = stringResource(R.string.emojis_deleted)
    val deleteFailedMessage = stringResource(R.string.emojis_delete_failed)

    LaunchedEffect(viewModel) {
        viewModel.refresh()
    }

    LaunchedEffect(viewModel, deletedMessage, deleteFailedMessage) {
        viewModel.events.collect { event ->
            val message = when (event) {
                EmojisEvent.Deleted -> deletedMessage
                EmojisEvent.DeleteFailed -> deleteFailedMessage
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(gridState, viewModel) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collectLatest { lastVisible ->
                val total = gridState.layoutInfo.totalItemsCount
                if (total > 0 && lastVisible >= total - LoadMoreThreshold) {
                    viewModel.loadMore()
                }
            }
    }

    val showSkeleton = uiState.isAuthPending || (uiState.isLoading && uiState.items.isEmpty())

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = EmojiCellMinSize),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = HeaderKey, span = { GridItemSpan(maxLineSpan) }) {
                EmojisHeader(uiState = uiState, onOpenCreate = onOpenCreate)
            }

            when {
                showSkeleton -> items(SkeletonCells) {
                    EmojiSkeletonCell(modifier = Modifier.fillMaxWidth())
                }

                !uiState.isAuthenticated -> item(key = UnauthorizedKey, span = { GridItemSpan(maxLineSpan) }) {
                    EmojisUnauthorized(palette = palette)
                }

                uiState.isUnavailable && uiState.items.isEmpty() -> item(
                    key = ErrorKey,
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    EmojisUnavailable(palette = palette, onRetry = viewModel::refresh)
                }

                uiState.items.isEmpty() && !uiState.isLoading -> item(
                    key = EmptyKey,
                    span = { GridItemSpan(maxLineSpan) },
                ) {
                    EmojisEmpty(palette = palette)
                }

                else -> items(uiState.items, key = { it.id }) { emoji ->
                    EmojiCell(
                        emoji = emoji,
                        isDeleting = uiState.deletingId == emoji.id,
                        onOpenOwner = { onOpenProfile(emoji.ownerUsername) },
                        onDelete = { pendingDelete = emoji },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (uiState.isLoadingMore) {
                item(key = FooterKey, span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = palette.accent,
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = palette.idleContent,
                contentColor = palette.bar,
                shape = RoundedCornerShape(12.dp),
            )
        }
    }

    pendingDelete?.let { emoji ->
        DeleteEmojiDialog(
            emoji = emoji,
            palette = palette,
            onConfirm = {
                viewModel.delete(emoji.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun EmojisHeader(
    uiState: EmojisUiState,
    onOpenCreate: () -> Unit,
) {
    val palette = navigationPalette()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.emojis_title).uppercase(),
                color = palette.textPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = stringResource(R.string.emojis_description),
                color = palette.textMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
        }

        if (uiState.isSubscribed) {
            EmojiPrimaryButton(
                text = stringResource(R.string.emojis_upload).uppercase(),
                onClick = onOpenCreate,
                icon = AppIconKind.Plus,
                modifier = Modifier.fillMaxWidth(),
            )
        } else if (uiState.isAuthenticated) {
            EmojiLockedBanner()
        }
    }
}

@Composable
private fun EmojisEmpty(palette: NavigationPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            kind = AppIconKind.Emojis,
            tint = palette.textMuted,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(R.string.emojis_empty),
            color = palette.textMuted,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EmojisUnavailable(
    palette: NavigationPalette,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.emojis_unavailable),
            color = palette.textMuted,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        EmojiPrimaryButton(
            text = stringResource(R.string.emojis_retry),
            onClick = onRetry,
        )
    }
}

@Composable
private fun EmojisUnauthorized(palette: NavigationPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AppIcon(
            kind = AppIconKind.Emojis,
            tint = palette.textMuted,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(R.string.emojis_unauthorized_title),
            color = palette.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.emojis_unauthorized_description),
            color = palette.textMuted,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DeleteEmojiDialog(
    emoji: EmojiUi,
    palette: NavigationPalette,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.bar,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textMuted,
        title = {
            Text(
                text = stringResource(R.string.emojis_delete_confirm_title),
                fontWeight = FontWeight.Black,
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.emojis_delete_confirm_message,
                    ":${emoji.name}:",
                ),
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.emojis_delete),
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

private val EmojiCellMinSize = 92.dp
private const val SkeletonCells = 9
private const val LoadMoreThreshold = 3
private const val HeaderKey = "emojis-header"
private const val EmptyKey = "emojis-empty"
private const val ErrorKey = "emojis-error"
private const val UnauthorizedKey = "emojis-unauthorized"
private const val FooterKey = "emojis-footer"
