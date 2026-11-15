package ru.fet1sov.akinorev.data.community

import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CommentLikeResult
import ru.fet1sov.akinorev.model.CommunityImage
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.CommunityProfile
import ru.fet1sov.akinorev.model.FollowResult
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.model.PostCommentsPage
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.PostLikeResult
import ru.fet1sov.akinorev.model.PostRepostResult
import ru.fet1sov.akinorev.model.PostsPage
import ru.fet1sov.akinorev.model.ProfileRelation

sealed interface ImageUploadResult {
    data class Success(val image: CommunityImage) : ImageUploadResult

    data object Unsupported : ImageUploadResult

    data object TooLarge : ImageUploadResult

    data object Unreadable : ImageUploadResult

    data class Failure(val error: AuthError) : ImageUploadResult
}

interface CommunityRepository {
    suspend fun fetchProfile(username: String): AuthResult<CommunityProfile>

    suspend fun fetchPosts(username: String, limit: Int, offset: Int): AuthResult<PostsPage>

    suspend fun fetchRelation(username: String): AuthResult<ProfileRelation>

    suspend fun setFollowing(username: String, follow: Boolean): AuthResult<FollowResult>

    suspend fun setBlocked(username: String, blocked: Boolean): AuthResult<Boolean>

    suspend fun createPost(
        username: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost>

    suspend fun updatePost(
        username: String,
        postId: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost>

    suspend fun deletePost(username: String, postId: String): AuthResult<Unit>

    suspend fun setPinned(username: String, postId: String, pinned: Boolean): AuthResult<CommunityPost>

    suspend fun setLiked(username: String, postId: String, liked: Boolean): AuthResult<PostLikeResult>

    suspend fun setReposted(
        username: String,
        postId: String,
        reposted: Boolean,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<PostRepostResult>

    suspend fun registerView(username: String, postId: String): AuthResult<Int>

    suspend fun reportPost(
        username: String,
        postId: String,
        reason: String,
        comment: String,
    ): AuthResult<Unit>

    suspend fun uploadImage(uri: String): ImageUploadResult

    suspend fun fetchComments(
        username: String,
        postId: String,
        limit: Int,
        offset: Int,
    ): AuthResult<PostCommentsPage>

    suspend fun createComment(
        username: String,
        postId: String,
        content: String,
        parentId: String?,
    ): AuthResult<PostComment>

    suspend fun updateComment(
        username: String,
        postId: String,
        commentId: String,
        content: String,
    ): AuthResult<PostComment>

    suspend fun deleteComment(username: String, postId: String, commentId: String): AuthResult<Unit>

    suspend fun setCommentLiked(
        username: String,
        postId: String,
        commentId: String,
        liked: Boolean,
    ): AuthResult<CommentLikeResult>
}

const val PostsPageSize = 10
const val CommentsPageSize = 10
const val MaxPostImages = 4
