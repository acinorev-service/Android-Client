package ru.fet1sov.akinorev.feature.profile

import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.CommunityProfile
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.ProfileRelation

enum class ProfileLoadError {
    Network,
    NotFound,
    Banned,
    Forbidden,
    RateLimited,
    Server,
    Unknown,
}

enum class ProfileActionError {
    Network,
    Unauthorized,
    Forbidden,
    NotFound,
    RateLimited,
    Banned,
    Generic,
}

enum class ProfileEditError {
    Network,
    Unauthorized,
    UsernameTaken,
    UsernameLimited,
    SubscriptionRequired,
    BioTooLong,
    UsernameTooShort,
    UsernameTooLong,
    InvalidValue,
    AvatarUnsupported,
    AvatarTooLarge,
    AvatarUnreadable,
    Generic,
}

enum class PostActionError {
    Network,
    Unauthorized,
    Forbidden,
    NotFound,
    RateLimited,
    Banned,
    BadRequest,
    ImageUnsupported,
    ImageTooLarge,
    ImageUnreadable,
    Generic,
}

data class ComposerImageUi(
    val id: String,
    val uri: String? = null,
    val url: String? = null,
    val mime: String = "",
) {
    fun asPostImage(): PostImage? = url?.takeIf { it.isNotBlank() }?.let { PostImage(url = it, mime = mime) }
}

data class PostComposerUiState(
    val content: Map<String, String> = mapOf("ru" to "", "en" to ""),
    val activeLocale: String = "ru",
    val images: List<ComposerImageUi> = emptyList(),
    val isPublishing: Boolean = false,
    val error: PostActionError? = null,
)

data class PostEditorUiState(
    val postId: String? = null,
    val content: Map<String, String> = mapOf("ru" to "", "en" to ""),
    val activeLocale: String = "ru",
    val images: List<ComposerImageUi> = emptyList(),
    val isSaving: Boolean = false,
    val error: PostActionError? = null,
) {
    val isOpen: Boolean
        get() = postId != null
}

data class RepostDialogUiState(
    val post: CommunityPost? = null,
    val isSubmitting: Boolean = false,
    val error: PostActionError? = null,
) {
    val isOpen: Boolean
        get() = post != null
}

data class ReportDialogUiState(
    val post: CommunityPost? = null,
    val reason: String = "",
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val error: PostActionError? = null,
) {
    val isOpen: Boolean
        get() = post != null

    val canSubmit: Boolean
        get() = reason.isNotBlank() && !isSubmitting
}

data class ProfileEditUiState(
    val isActive: Boolean = false,
    val username: String = "",
    val bio: String = "",
    val nameColor: String = "#ffffff",
    val nameAnimation: String = "",
    val avatarUri: String? = null,
    val isSaving: Boolean = false,
    val error: ProfileEditError? = null,
)

data class ProfileUiState(
    val username: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val profile: CommunityProfile? = null,
    val relation: ProfileRelation? = null,
    val isAuthenticated: Boolean = false,
    val isSelf: Boolean = false,
    val loadError: ProfileLoadError? = null,
    val isBanned: Boolean = false,
    val emojiUrls: Map<String, String> = emptyMap(),
    val isOnline: Boolean = false,
    val presenceDevice: String = "",
    val lastSeenMillis: Long? = null,
    val lastSeenDevice: String = "",
    val posts: List<CommunityPost> = emptyList(),
    val isPostsLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePosts: Boolean = false,
    val postsLoadError: ProfileLoadError? = null,
    val loadMoreError: PostActionError? = null,
    val actionError: ProfileActionError? = null,
    val isActionPending: Boolean = false,
    val pendingPostIds: Set<String> = emptySet(),
    val composer: PostComposerUiState = PostComposerUiState(),
    val editing: ProfileEditUiState = ProfileEditUiState(),
    val postEditor: PostEditorUiState = PostEditorUiState(),
    val repost: RepostDialogUiState = RepostDialogUiState(),
    val report: ReportDialogUiState = ReportDialogUiState(),
    val pendingDeletePostId: String? = null,
    val isDeletingPost: Boolean = false,
    val postError: PostActionError? = null,
    val reportedPostIds: Set<String> = emptySet(),
    val renamedTo: String? = null,
) {
    val displayUsername: String
        get() = profile?.username ?: username

    val isBlocked: Boolean
        get() = profile?.isBlocked == true ||
            relation?.isBlocked == true ||
            relation?.blockedMe == true

    val canManagePosts: Boolean
        get() = isSelf && isAuthenticated

    val showPresence: Boolean
        get() = profile != null && !isBlocked && !isBanned

    val isNotFound: Boolean
        get() = loadError == ProfileLoadError.NotFound
}
