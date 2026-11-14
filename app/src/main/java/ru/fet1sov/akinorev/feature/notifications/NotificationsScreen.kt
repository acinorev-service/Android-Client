package ru.fet1sov.akinorev.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.model.NotificationType
import ru.fet1sov.akinorev.ui.components.ProfileName
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.UserAvatar
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
fun NotificationsScreen(
    onOpenProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = viewModel(factory = NotificationsViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = navigationPalette()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.mapNotNull { info -> info.key as? String } }
            .collectLatest { visible ->
                val visibleIds = visible.toSet()
                val unread = viewModel.uiState.value.items
                    .filter { it.id in visibleIds && !it.isRead }
                    .map { it.id }
                if (unread.isEmpty()) return@collectLatest
                delay(ReadDwellMillis)
                viewModel.markRead(unread)
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastIndex ->
                val state = viewModel.uiState.value
                if (state.items.isNotEmpty() && lastIndex >= state.items.size - LoadMoreThreshold) {
                    viewModel.loadMore()
                }
            }
    }

    val showSkeleton = uiState.isAuthPending || (uiState.isLoading && uiState.items.isEmpty())

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = HeaderKey) {
            NotificationsHeader(
                unread = uiState.unread,
                palette = palette,
                onMarkAllRead = viewModel::markAllRead,
            )
        }

        when {
            showSkeleton -> items(SkeletonRows) { index ->
                NotificationSkeletonRow(index = index)
            }

            uiState.items.isEmpty() -> item(key = EmptyKey) {
                NotificationsEmpty(palette = palette)
            }

            else -> items(uiState.items, key = { it.id }) { item ->
                NotificationRow(
                    item = item,
                    palette = palette,
                    onOpenProfile = onOpenProfile,
                )
            }
        }

        if (uiState.isLoadingMore) {
            item(key = FooterKey) {
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
}

@Composable
private fun NotificationsHeader(
    unread: Int,
    palette: NavigationPalette,
    onMarkAllRead: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.nav_notifications).uppercase(),
            color = palette.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
        )

        if (unread > 0) {
            Surface(
                onClick = onMarkAllRead,
                shape = RoundedCornerShape(10.dp),
                color = Color.Transparent,
            ) {
                Text(
                    text = stringResource(R.string.notifications_mark_all_read),
                    color = palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(
    item: NotificationUi,
    palette: NavigationPalette,
    onOpenProfile: (String) -> Unit,
) {
    val background = if (item.isRead) palette.bar else palette.textPrimary
    val contentColor = if (item.isRead) palette.textPrimary else palette.bar
    val openProfile = item.actorUsername.takeIf { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .then(
                if (openProfile != null) {
                    Modifier.clickable { onOpenProfile(openProfile) }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.type == NotificationType.Broadcast) {
            BroadcastIcon(contentColor = contentColor)
        } else {
            UserAvatar(
                avatar = item.actor?.avatar.orEmpty(),
                userId = item.actor?.id ?: item.actorId,
                username = item.actorUsername,
                size = 48.dp,
                shape = CircleShape,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (item.type == NotificationType.Broadcast) {
                Text(
                    text = item.title,
                    color = contentColor,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Black,
                )
                if (item.body.isNotBlank()) {
                    Text(
                        text = item.body,
                        color = contentColor.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            } else {
                val actor = item.actor
                if (actor != null && actor.username.isNotBlank()) {
                    if (actor.isSubscribed) {
                        ProfileName(
                            username = actor.username,
                            nameColor = actor.nameColor,
                            nameAnimation = actor.nameAnimation,
                            fontSize = 15.sp,
                        )
                    } else {
                        Text(
                            text = actor.username,
                            color = contentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                        )
                    }
                }
                item.type.actionRes?.let { actionRes ->
                    Text(
                        text = stringResource(actionRes),
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                    )
                }
            }

            Text(
                text = notificationTime(item.createdAtMillis),
                color = contentColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
            )
        }

        if (!item.isRead) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(contentColor),
            )
        }
    }
}

@Composable
private fun BroadcastIcon(contentColor: Color) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(contentColor.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            kind = AppIconKind.Notifications,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun NotificationsEmpty(palette: NavigationPalette) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            kind = AppIconKind.Notifications,
            tint = palette.textPrimary,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = stringResource(R.string.notifications_empty).uppercase(),
            color = palette.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun NotificationSkeletonRow(index: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBlock(height = 48.dp, width = 48.dp, shape = CircleShape)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SkeletonBlock(height = 15.dp, width = 150.dp)
            SkeletonBlock(height = 15.dp, width = 110.dp)
            SkeletonBlock(height = 12.dp, width = 90.dp)
        }
        if (index % 2 == 0) {
            SkeletonBlock(height = 10.dp, width = 10.dp, shape = CircleShape)
        }
    }
}

@Composable
private fun notificationTime(millis: Long): String {
    val minutes = ((System.currentTimeMillis() - millis) / 60_000L).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(R.string.notifications_just_now)
        minutes < 60 -> stringResource(R.string.notifications_minutes_ago, minutes.toInt())
        minutes < DayMinutes -> stringResource(R.string.notifications_hours_ago, (minutes / 60).toInt())
        else -> {
            val formatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(formatter)
        }
    }
}

private val NotificationType.actionRes: Int?
    get() = when (this) {
        NotificationType.Follow -> R.string.notifications_followed_you
        NotificationType.Block -> R.string.notifications_blocked_you
        NotificationType.Post -> R.string.notifications_new_post
        NotificationType.Like -> R.string.notifications_liked_post
        NotificationType.Broadcast -> null
    }

private const val ReadDwellMillis = 600L
private const val LoadMoreThreshold = 2
private const val SkeletonRows = 4
private const val DayMinutes = 24 * 60L
private const val HeaderKey = "notifications-header"
private const val EmptyKey = "notifications-empty"
private const val FooterKey = "notifications-footer"
