package ru.fet1sov.akinorev.model

data class ProfileBadge(
    val userItemId: String,
    val itemId: String,
    val order: Int,
    val name: String,
    val icon: String,
    val rarity: String,
)

data class CommunityProfile(
    val id: String,
    val username: String,
    val bio: String,
    val avatarPath: String,
    val createdAtMillis: Long?,
    val lastSeenMillis: Long?,
    val lastSeenDevice: String,
    val followersCount: Int,
    val followingCount: Int,
    val isBlocked: Boolean,
    val isSubscribed: Boolean,
    val nameColor: String,
    val nameAnimation: String,
    val badges: List<ProfileBadge>,
)

data class ProfileRelation(
    val isSelf: Boolean,
    val isFollowing: Boolean,
    val isBlocked: Boolean,
    val blockedMe: Boolean,
)

data class PostAuthor(
    val id: String,
    val username: String,
    val avatarPath: String,
    val isSubscribed: Boolean,
    val nameColor: String,
    val nameAnimation: String,
)

data class PostImage(
    val url: String,
    val mime: String = "",
    val size: Long = 0,
)

data class CommunityPost(
    val id: String,
    val author: PostAuthor,
    val content: Map<String, String>,
    val images: List<PostImage>,
    val views: Int,
    val likesCount: Int,
    val likedByMe: Boolean,
    val commentsCount: Int,
    val commentsPreview: List<PostComment>,
    val repostsCount: Int,
    val repostedByMe: Boolean,
    val isRepost: Boolean,
    val repostOf: CommunityPost?,
    val repostContent: Map<String, String>,
    val repostImages: List<PostImage>,
    val isPinned: Boolean,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
) {
    val target: CommunityPost
        get() = if (isRepost && repostOf != null) repostOf else this

    fun mapTarget(transform: (CommunityPost) -> CommunityPost): CommunityPost =
        if (isRepost && repostOf != null) copy(repostOf = transform(repostOf)) else transform(this)
}

data class PostComment(
    val id: String,
    val postId: String,
    val parentId: String?,
    val depth: Int,
    val content: String,
    val author: PostAuthor?,
    val canDelete: Boolean,
    val canEdit: Boolean,
    val isEdited: Boolean,
    val likesCount: Int,
    val likedByMe: Boolean,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val children: List<PostComment>,
)

data class PostsPage(
    val posts: List<CommunityPost>,
    val hasMore: Boolean,
)

data class PostCommentsPage(
    val comments: List<PostComment>,
    val total: Int,
    val rootsTotal: Int,
    val hasMore: Boolean,
)

data class PostLikeResult(
    val postId: String,
    val likesCount: Int,
    val likedByMe: Boolean,
)

data class PostRepostResult(
    val postId: String,
    val repostsCount: Int,
    val repostedByMe: Boolean,
)

data class CommentLikeResult(
    val commentId: String,
    val likesCount: Int,
    val likedByMe: Boolean,
)

data class FollowResult(
    val isFollowing: Boolean,
    val followersCount: Int,
)

data class CommunityImage(
    val url: String,
    val mime: String,
    val size: Long,
)
