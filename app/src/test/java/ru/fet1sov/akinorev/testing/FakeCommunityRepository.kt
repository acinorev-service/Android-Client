package ru.fet1sov.akinorev.testing

import ru.fet1sov.akinorev.data.community.CommunityRepository
import ru.fet1sov.akinorev.data.community.ImageUploadResult
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CommentLikeResult
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.CommunityProfile
import ru.fet1sov.akinorev.model.FollowResult
import ru.fet1sov.akinorev.model.PostAuthor
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.model.PostCommentsPage
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.PostLikeResult
import ru.fet1sov.akinorev.model.PostRepostResult
import ru.fet1sov.akinorev.model.PostsPage
import ru.fet1sov.akinorev.model.ProfileBadge
import ru.fet1sov.akinorev.model.ProfileRelation

class FakeCommunityRepository : CommunityRepository {
    var profileResult: AuthResult<CommunityProfile> = AuthResult.Success(fakeCommunityProfile())
    var postsResults: MutableMap<Int, AuthResult<PostsPage>> = mutableMapOf()
    var relationResult: AuthResult<ProfileRelation> = AuthResult.Success(
        ProfileRelation(isSelf = false, isFollowing = false, isBlocked = false, blockedMe = false),
    )
    var followingResult: AuthResult<FollowResult> = AuthResult.Failure(AuthError.Unexpected)
    var blockedResult: AuthResult<Boolean> = AuthResult.Failure(AuthError.Unexpected)
    var createPostResult: AuthResult<CommunityPost> = AuthResult.Failure(AuthError.Unexpected)
    var updatePostResult: AuthResult<CommunityPost> = AuthResult.Failure(AuthError.Unexpected)
    var deletePostResult: AuthResult<Unit> = AuthResult.Failure(AuthError.Unexpected)
    var pinnedResult: AuthResult<CommunityPost> = AuthResult.Failure(AuthError.Unexpected)
    var likeResult: AuthResult<PostLikeResult> = AuthResult.Failure(AuthError.Unexpected)
    var repostResult: AuthResult<PostRepostResult> = AuthResult.Failure(AuthError.Unexpected)
    var viewResult: AuthResult<Int> = AuthResult.Failure(AuthError.Unexpected)
    var reportResult: AuthResult<Unit> = AuthResult.Failure(AuthError.Unexpected)
    var imageUploadResult: ImageUploadResult = ImageUploadResult.Failure(AuthError.Unexpected)
    var commentsResult: AuthResult<PostCommentsPage> = AuthResult.Failure(AuthError.Unexpected)
    var createCommentResult: AuthResult<PostComment> = AuthResult.Failure(AuthError.Unexpected)
    var updateCommentResult: AuthResult<PostComment> = AuthResult.Failure(AuthError.Unexpected)
    var deleteCommentResult: AuthResult<Unit> = AuthResult.Failure(AuthError.Unexpected)
    var commentLikeResult: AuthResult<CommentLikeResult> = AuthResult.Failure(AuthError.Unexpected)

    var profileRequests: Int = 0
        private set
    val postsRequests: MutableList<Pair<Int, Int>> = mutableListOf()
    var relationRequests: Int = 0
        private set
    var followingRequests: Int = 0
        private set
    var likeRequests: Int = 0
        private set
    var lastLike: Boolean? = null

    override suspend fun fetchProfile(username: String): AuthResult<CommunityProfile> {
        profileRequests += 1
        return profileResult
    }

    override suspend fun fetchPosts(username: String, limit: Int, offset: Int): AuthResult<PostsPage> {
        postsRequests += limit to offset
        return postsResults[offset] ?: AuthResult.Success(PostsPage(posts = emptyList(), hasMore = false))
    }

    override suspend fun fetchRelation(username: String): AuthResult<ProfileRelation> {
        relationRequests += 1
        return relationResult
    }

    override suspend fun setFollowing(username: String, follow: Boolean): AuthResult<FollowResult> {
        followingRequests += 1
        return followingResult
    }

    override suspend fun setBlocked(username: String, blocked: Boolean): AuthResult<Boolean> =
        blockedResult

    override suspend fun createPost(
        username: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost> = createPostResult

    override suspend fun updatePost(
        username: String,
        postId: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost> = updatePostResult

    override suspend fun deletePost(username: String, postId: String): AuthResult<Unit> =
        deletePostResult

    override suspend fun setPinned(
        username: String,
        postId: String,
        pinned: Boolean,
    ): AuthResult<CommunityPost> = pinnedResult

    override suspend fun setLiked(
        username: String,
        postId: String,
        liked: Boolean,
    ): AuthResult<PostLikeResult> {
        likeRequests += 1
        lastLike = liked
        return likeResult
    }

    override suspend fun setReposted(
        username: String,
        postId: String,
        reposted: Boolean,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<PostRepostResult> = repostResult

    override suspend fun registerView(username: String, postId: String): AuthResult<Int> = viewResult

    override suspend fun reportPost(
        username: String,
        postId: String,
        reason: String,
        comment: String,
    ): AuthResult<Unit> = reportResult

    override suspend fun uploadImage(uri: String): ImageUploadResult = imageUploadResult

    override suspend fun fetchComments(
        username: String,
        postId: String,
        limit: Int,
        offset: Int,
    ): AuthResult<PostCommentsPage> = commentsResult

    override suspend fun createComment(
        username: String,
        postId: String,
        content: String,
        parentId: String?,
    ): AuthResult<PostComment> = createCommentResult

    override suspend fun updateComment(
        username: String,
        postId: String,
        commentId: String,
        content: String,
    ): AuthResult<PostComment> = updateCommentResult

    override suspend fun deleteComment(
        username: String,
        postId: String,
        commentId: String,
    ): AuthResult<Unit> = deleteCommentResult

    override suspend fun setCommentLiked(
        username: String,
        postId: String,
        commentId: String,
        liked: Boolean,
    ): AuthResult<CommentLikeResult> = commentLikeResult
}

fun fakeCommunityProfile(
    id: String = "user-1",
    username: String = "fet1sov",
    bio: String = "hello",
    followersCount: Int = 0,
    followingCount: Int = 0,
    isBlocked: Boolean = false,
    isSubscribed: Boolean = false,
    nameColor: String = "",
    nameAnimation: String = "",
): CommunityProfile = CommunityProfile(
    id = id,
    username = username,
    bio = bio,
    avatarPath = "",
    createdAtMillis = 1_760_000_000_000,
    lastSeenMillis = null,
    lastSeenDevice = "",
    followersCount = followersCount,
    followingCount = followingCount,
    isBlocked = isBlocked,
    isSubscribed = isSubscribed,
    nameColor = nameColor,
    nameAnimation = nameAnimation,
    badges = emptyList<ProfileBadge>(),
)

fun fakePostAuthor(
    id: String = "user-1",
    username: String = "fet1sov",
): PostAuthor = PostAuthor(
    id = id,
    username = username,
    avatarPath = "",
    isSubscribed = false,
    nameColor = "",
    nameAnimation = "",
)

fun fakeCommunityPost(
    id: String = "post-1",
    author: PostAuthor = fakePostAuthor(),
    likesCount: Int = 0,
    likedByMe: Boolean = false,
    commentsCount: Int = 0,
    repostsCount: Int = 0,
    repostedByMe: Boolean = false,
    isPinned: Boolean = false,
    createdAtMillis: Long = 1_760_000_000_000,
): CommunityPost = CommunityPost(
    id = id,
    author = author,
    content = mapOf("ru" to "Текст поста", "en" to "Post text"),
    images = emptyList(),
    views = 0,
    likesCount = likesCount,
    likedByMe = likedByMe,
    commentsCount = commentsCount,
    commentsPreview = emptyList(),
    repostsCount = repostsCount,
    repostedByMe = repostedByMe,
    isRepost = false,
    repostOf = null,
    repostContent = emptyMap(),
    repostImages = emptyList(),
    isPinned = isPinned,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = null,
)
