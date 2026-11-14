package ru.fet1sov.akinorev.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostAuthorDto(
    val id: String = "",
    val username: String = "",
    val avatar: String? = null,
    @SerialName("is_subscribed") val isSubscribed: Boolean = false,
    @SerialName("name_color") val nameColor: String? = null,
    @SerialName("name_animation") val nameAnimation: String? = null,
)

@Serializable
data class PostImageDto(
    val url: String = "",
    val mime: String? = null,
    val size: Long = 0,
)

@Serializable
data class PostDto(
    val id: String = "",
    val author: PostAuthorDto? = null,
    val content: Map<String, String>? = null,
    val images: List<PostImageDto>? = null,
    val views: Int = 0,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
    @SerialName("comments_count") val commentsCount: Int = 0,
    @SerialName("comments_preview") val commentsPreview: List<PostCommentDto>? = null,
    @SerialName("reposts_count") val repostsCount: Int = 0,
    @SerialName("reposted_by_me") val repostedByMe: Boolean = false,
    @SerialName("is_repost") val isRepost: Boolean = false,
    @SerialName("repost_of") val repostOf: PostDto? = null,
    @SerialName("repost_content") val repostContent: Map<String, String>? = null,
    @SerialName("repost_images") val repostImages: List<PostImageDto>? = null,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PostCommentDto(
    val id: String = "",
    @SerialName("post_id") val postId: String = "",
    @SerialName("parent_id") val parentId: String? = null,
    val depth: Int = 0,
    val content: String = "",
    val author: PostAuthorDto? = null,
    @SerialName("can_delete") val canDelete: Boolean = false,
    @SerialName("can_edit") val canEdit: Boolean = false,
    @SerialName("is_edited") val isEdited: Boolean = false,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val children: List<PostCommentDto> = emptyList(),
)

@Serializable
data class PostsPayloadDto(
    val posts: List<PostDto> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class PostPayloadDto(
    val post: PostDto? = null,
)

@Serializable
data class PostCommentsPayloadDto(
    val comments: List<PostCommentDto> = emptyList(),
    val total: Int = 0,
    @SerialName("roots_total") val rootsTotal: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
)

@Serializable
data class PostCommentPayloadDto(
    val comment: PostCommentDto? = null,
)

@Serializable
data class ProfileRelationDto(
    @SerialName("is_self") val isSelf: Boolean = false,
    @SerialName("is_following") val isFollowing: Boolean = false,
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("blocked_me") val blockedMe: Boolean = false,
)

@Serializable
data class PostLikePayloadDto(
    @SerialName("post_id") val postId: String = "",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
)

@Serializable
data class PostRepostPayloadDto(
    @SerialName("post_id") val postId: String = "",
    @SerialName("reposts_count") val repostsCount: Int = 0,
    @SerialName("reposted_by_me") val repostedByMe: Boolean = false,
)

@Serializable
data class PostViewPayloadDto(
    @SerialName("post_id") val postId: String = "",
    val views: Int = 0,
)

@Serializable
data class CommentLikePayloadDto(
    @SerialName("comment_id") val commentId: String = "",
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("liked_by_me") val likedByMe: Boolean = false,
)

@Serializable
data class FollowPayloadDto(
    @SerialName("is_following") val isFollowing: Boolean = false,
    @SerialName("followers_count") val followersCount: Int = 0,
)

@Serializable
data class BlockPayloadDto(
    @SerialName("is_blocked") val isBlocked: Boolean = false,
)

@Serializable
data class ReportPayloadDto(
    val report: ReportDto? = null,
)

@Serializable
data class ReportDto(
    val id: String = "",
)

@Serializable
data class CreatePostRequest(
    val content: Map<String, String>,
    val images: List<PostImageDto> = emptyList(),
)

@Serializable
data class UpdatePostRequest(
    val content: Map<String, String>,
    val images: List<PostImageDto> = emptyList(),
)

@Serializable
data class RepostRequest(
    val content: Map<String, String>? = null,
    val images: List<PostImageDto> = emptyList(),
)

@Serializable
data class CreateCommentRequest(
    val content: String,
    @SerialName("parent_id") val parentId: String? = null,
)

@Serializable
data class UpdateCommentRequest(
    val content: String,
)

@Serializable
data class ReportPostRequest(
    val reason: String,
    val comment: String = "",
)
