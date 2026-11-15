package ru.fet1sov.akinorev.data.community

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.fet1sov.akinorev.data.files.ImageFileResult
import ru.fet1sov.akinorev.data.files.ImageFileSource
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.CdnApi
import ru.fet1sov.akinorev.data.remote.dto.CreateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.CreatePostRequest
import ru.fet1sov.akinorev.data.remote.dto.RepostRequest
import ru.fet1sov.akinorev.data.remote.dto.ReportPostRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdatePostRequest
import ru.fet1sov.akinorev.model.CommentLikeResult
import ru.fet1sov.akinorev.model.CommunityFeedMode
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

class DefaultCommunityRepository(
    private val api: AcinorevApi,
    private val cdnApi: CdnApi,
    private val client: ApiClient,
    private val imageFileSource: ImageFileSource,
) : CommunityRepository {
    override suspend fun fetchProfile(username: String): AuthResult<CommunityProfile> =
        when (val result = client.execute { api.getUser(username) }) {
            is AuthResult.Success -> {
                val user = result.value.user
                if (user == null) {
                    AuthResult.Failure(AuthError.Unexpected)
                } else {
                    AuthResult.Success(user.toCommunityProfile())
                }
            }

            is AuthResult.Failure -> result
        }

    override suspend fun fetchPosts(username: String, limit: Int, offset: Int): AuthResult<PostsPage> =
        when (val result = client.execute { api.getPosts(username, limit, offset) }) {
            is AuthResult.Success -> AuthResult.Success(
                PostsPage(
                    posts = result.value.posts.mapNotNull { it.toDomain() },
                    hasMore = result.value.hasMore,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun fetchFeed(
        mode: CommunityFeedMode,
        limit: Int,
        offset: Int,
    ): AuthResult<PostsPage> =
        when (val result = client.execute {
            api.getCommunityFeed(
                limit = limit,
                offset = offset,
                sort = mode.sort,
                filter = mode.filter,
            )
        }) {
            is AuthResult.Success -> AuthResult.Success(
                PostsPage(
                    posts = result.value.posts.mapNotNull { it.toDomain() },
                    hasMore = result.value.hasMore,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun fetchRelation(username: String): AuthResult<ProfileRelation> =
        when (val result = client.execute { api.getRelation(username) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain())
            is AuthResult.Failure -> result
        }

    override suspend fun setFollowing(username: String, follow: Boolean): AuthResult<FollowResult> =
        when (val result = client.execute {
            if (follow) api.followUser(username) else api.unfollowUser(username)
        }) {
            is AuthResult.Success -> AuthResult.Success(
                FollowResult(
                    isFollowing = result.value.isFollowing,
                    followersCount = result.value.followersCount,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun setBlocked(username: String, blocked: Boolean): AuthResult<Boolean> =
        when (val result = client.execute {
            if (blocked) api.blockUser(username) else api.unblockUser(username)
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.isBlocked)
            is AuthResult.Failure -> result
        }

    override suspend fun createPost(
        username: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost> =
        when (val result = client.execute {
            api.createPost(
                username = username,
                body = CreatePostRequest(
                    content = content,
                    images = images.map(PostImage::toDto),
                ),
            )
        }) {
            is AuthResult.Success -> result.value.post.toPostResult()
            is AuthResult.Failure -> result
        }

    override suspend fun updatePost(
        username: String,
        postId: String,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<CommunityPost> =
        when (val result = client.execute {
            api.updatePost(
                username = username,
                id = postId,
                body = UpdatePostRequest(
                    content = content,
                    images = images.map(PostImage::toDto),
                ),
            )
        }) {
            is AuthResult.Success -> result.value.post.toPostResult()
            is AuthResult.Failure -> result
        }

    override suspend fun deletePost(username: String, postId: String): AuthResult<Unit> =
        client.executeNoContent { api.deletePost(username, postId) }

    override suspend fun setPinned(
        username: String,
        postId: String,
        pinned: Boolean,
    ): AuthResult<CommunityPost> =
        when (val result = client.execute {
            if (pinned) api.pinPost(username, postId) else api.unpinPost(username, postId)
        }) {
            is AuthResult.Success -> result.value.post.toPostResult()
            is AuthResult.Failure -> result
        }

    override suspend fun setLiked(
        username: String,
        postId: String,
        liked: Boolean,
    ): AuthResult<PostLikeResult> =
        when (val result = client.execute {
            if (liked) api.likePost(username, postId) else api.unlikePost(username, postId)
        }) {
            is AuthResult.Success -> AuthResult.Success(
                PostLikeResult(
                    postId = result.value.postId.ifBlank { postId },
                    likesCount = result.value.likesCount,
                    likedByMe = result.value.likedByMe,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun setReposted(
        username: String,
        postId: String,
        reposted: Boolean,
        content: Map<String, String>,
        images: List<PostImage>,
    ): AuthResult<PostRepostResult> =
        when (val result = client.execute {
            if (reposted) {
                api.repostPost(
                    username = username,
                    id = postId,
                    body = RepostRequest(
                        content = content.takeIf { it.isNotEmpty() },
                        images = images.map(PostImage::toDto),
                    ),
                )
            } else {
                api.unrepostPost(username, postId)
            }
        }) {
            is AuthResult.Success -> AuthResult.Success(
                PostRepostResult(
                    postId = result.value.postId.ifBlank { postId },
                    repostsCount = result.value.repostsCount,
                    repostedByMe = result.value.repostedByMe,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun registerView(username: String, postId: String): AuthResult<Int> =
        when (val result = client.execute { api.viewPost(username, postId) }) {
            is AuthResult.Success -> AuthResult.Success(result.value.views)
            is AuthResult.Failure -> result
        }

    override suspend fun reportPost(
        username: String,
        postId: String,
        reason: String,
        comment: String,
    ): AuthResult<Unit> =
        when (val result = client.execute {
            api.reportPost(
                username = username,
                id = postId,
                body = ReportPostRequest(reason = reason, comment = comment),
            )
        }) {
            is AuthResult.Success -> AuthResult.Success(Unit)
            is AuthResult.Failure -> result
        }

    override suspend fun uploadImage(uri: String): ImageUploadResult =
        when (val file = imageFileSource.read(uri)) {
            ImageFileResult.Unsupported -> ImageUploadResult.Unsupported
            ImageFileResult.TooLarge -> ImageUploadResult.TooLarge
            ImageFileResult.Unreadable -> ImageUploadResult.Unreadable
            is ImageFileResult.Ready -> when (val result = client.execute { cdnApi.uploadFile(file.file.asPart()) }) {
                is AuthResult.Success -> {
                    val url = result.value.url
                    if (url.isBlank()) {
                        ImageUploadResult.Failure(AuthError.Unexpected)
                    } else {
                        ImageUploadResult.Success(
                            CommunityImage(
                                url = url,
                                mime = file.file.mimeType,
                                size = file.file.bytes.size.toLong(),
                            ),
                        )
                    }
                }

                is AuthResult.Failure -> ImageUploadResult.Failure(result.error)
            }
        }

    override suspend fun fetchComments(
        username: String,
        postId: String,
        limit: Int,
        offset: Int,
    ): AuthResult<PostCommentsPage> =
        when (val result = client.execute { api.getPostComments(username, postId, limit, offset) }) {
            is AuthResult.Success -> AuthResult.Success(
                PostCommentsPage(
                    comments = result.value.comments.mapNotNull { it.toDomain() },
                    total = result.value.total,
                    rootsTotal = result.value.rootsTotal,
                    hasMore = result.value.hasMore,
                ),
            )

            is AuthResult.Failure -> result
        }

    override suspend fun createComment(
        username: String,
        postId: String,
        content: String,
        parentId: String?,
    ): AuthResult<PostComment> =
        when (val result = client.execute {
            api.createPostComment(
                username = username,
                id = postId,
                body = CreateCommentRequest(content = content, parentId = parentId),
            )
        }) {
            is AuthResult.Success -> result.value.comment.toCommentResult()
            is AuthResult.Failure -> result
        }

    override suspend fun updateComment(
        username: String,
        postId: String,
        commentId: String,
        content: String,
    ): AuthResult<PostComment> =
        when (val result = client.execute {
            api.updatePostComment(
                username = username,
                id = postId,
                commentId = commentId,
                body = UpdateCommentRequest(content = content),
            )
        }) {
            is AuthResult.Success -> result.value.comment.toCommentResult()
            is AuthResult.Failure -> result
        }

    override suspend fun deleteComment(
        username: String,
        postId: String,
        commentId: String,
    ): AuthResult<Unit> = client.executeNoContent {
        api.deletePostComment(username, postId, commentId)
    }

    override suspend fun setCommentLiked(
        username: String,
        postId: String,
        commentId: String,
        liked: Boolean,
    ): AuthResult<CommentLikeResult> =
        when (val result = client.execute {
            if (liked) {
                api.likePostComment(username, postId, commentId)
            } else {
                api.unlikePostComment(username, postId, commentId)
            }
        }) {
            is AuthResult.Success -> AuthResult.Success(
                CommentLikeResult(
                    commentId = result.value.commentId.ifBlank { commentId },
                    likesCount = result.value.likesCount,
                    likedByMe = result.value.likedByMe,
                ),
            )

            is AuthResult.Failure -> result
        }

    private fun ru.fet1sov.akinorev.data.remote.dto.PostDto?.toPostResult(): AuthResult<CommunityPost> {
        val post = this?.toDomain() ?: return AuthResult.Failure(AuthError.Unexpected)
        return AuthResult.Success(post)
    }

    private fun ru.fet1sov.akinorev.data.remote.dto.PostCommentDto?.toCommentResult(): AuthResult<PostComment> {
        val comment = this?.toDomain() ?: return AuthResult.Failure(AuthError.Unexpected)
        return AuthResult.Success(comment)
    }
}

private fun ru.fet1sov.akinorev.data.files.ImageUploadFile.asPart(): MultipartBody.Part {
    val mediaType = mimeType.ifBlank { "image/jpeg" }.toMediaTypeOrNull()
    return MultipartBody.Part.createFormData(
        name = "file",
        filename = fileName.ifBlank { "image.jpg" },
        body = bytes.toRequestBody(mediaType),
    )
}
