package ru.fet1sov.akinorev.feature.community

import androidx.compose.runtime.Immutable
import ru.fet1sov.akinorev.feature.profile.PostActionError
import ru.fet1sov.akinorev.feature.profile.RepostDialogUiState
import ru.fet1sov.akinorev.feature.profile.ReportDialogUiState
import ru.fet1sov.akinorev.model.CommunityFeedMode
import ru.fet1sov.akinorev.model.CommunityPost

@Immutable
data class CommunityUiState(
    val mode: CommunityFeedMode = CommunityFeedMode.Popular,
    val isAuthenticated: Boolean = false,
    val isAuthPending: Boolean = true,
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadError: Boolean = false,
    val loadMoreError: Boolean = false,
    val emojiUrls: Map<String, String> = emptyMap(),
    val pendingPostIds: Set<String> = emptySet(),
    val reportedPostIds: Set<String> = emptySet(),
    val actionError: PostActionError? = null,
    val repost: RepostDialogUiState = RepostDialogUiState(),
    val report: ReportDialogUiState = ReportDialogUiState(),
) {
    val isFollowingLocked: Boolean
        get() = mode == CommunityFeedMode.Following && !isAuthenticated && !isAuthPending

    val showLoading: Boolean
        get() = posts.isEmpty() && (isLoading || (mode == CommunityFeedMode.Following && isAuthPending))
}
