package ru.fet1sov.akinorev.feature.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.community.CommunityFeedPageSize
import ru.fet1sov.akinorev.data.community.CommunityRepository
import ru.fet1sov.akinorev.data.community.ImageUploadResult
import ru.fet1sov.akinorev.data.community.MaxPostImages
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.feature.profile.ComposerImageUi
import ru.fet1sov.akinorev.feature.profile.PostActionError
import ru.fet1sov.akinorev.feature.profile.RepostDialogUiState
import ru.fet1sov.akinorev.feature.profile.ReportDialogUiState
import ru.fet1sov.akinorev.feature.profile.toPostActionError
import ru.fet1sov.akinorev.model.CommunityFeedMode
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.collectEmojiNames
import ru.fet1sov.akinorev.model.postLocaleTag
import java.util.Locale
import java.util.UUID

class CommunityViewModel(
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val emojisRepository: EmojisRepository,
    private val languageTagProvider: () -> String = { Locale.getDefault().language },
    initialMode: CommunityFeedMode = CommunityFeedMode.Popular,
) : ViewModel() {
    private val initialSession = authRepository.session.value
    private val mutableUiState = MutableStateFlow(
        CommunityUiState(
            mode = initialMode,
            isAuthenticated = initialSession is SessionState.Authorized,
            isAuthPending = initialSession is SessionState.Loading,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<CommunityUiState> = mutableUiState.asStateFlow()

    private var loadJob: Job? = null
    private var moreJob: Job? = null
    private var emojiJob: Job? = null

    private val viewedPostIds = mutableSetOf<String>()
    private val pendingLikes = mutableSetOf<String>()
    private val pendingReposts = mutableSetOf<String>()

    init {
        observeSession()
        loadFeed(keepContent = false)
    }

    fun selectMode(mode: CommunityFeedMode) {
        if (mutableUiState.value.mode == mode) return

        loadJob?.cancel()
        moreJob?.cancel()
        mutableUiState.update {
            it.copy(
                mode = mode,
                posts = emptyList(),
                hasMore = false,
                isLoading = false,
                isRefreshing = false,
                loadError = false,
                loadMoreError = false,
                actionError = null,
                repost = RepostDialogUiState(),
                report = ReportDialogUiState(),
            )
        }

        val state = mutableUiState.value
        if (mode == CommunityFeedMode.Following && !state.isAuthenticated) {
            if (state.isAuthPending) {
                mutableUiState.update { it.copy(isLoading = true) }
            }
            return
        }
        loadFeed(keepContent = false)
    }

    fun refresh() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing || state.isFollowingLocked) return
        loadFeed(keepContent = state.posts.isNotEmpty())
    }

    fun retry() {
        if (mutableUiState.value.isLoading) return
        loadFeed(keepContent = false)
    }

    fun loadMore() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore) return
        if (state.isFollowingLocked || !state.hasMore || state.loadMoreError) return

        moreJob?.cancel()
        moreJob = viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingMore = true) }
            val offset = mutableUiState.value.posts.size
            when (val result = communityRepository.fetchFeed(
                mode = mutableUiState.value.mode,
                limit = CommunityFeedPageSize,
                offset = offset,
            )) {
                is AuthResult.Success -> {
                    val existing = mutableUiState.value.posts
                    val seen = existing.mapTo(mutableSetOf()) { post -> post.id }
                    val fresh = result.value.posts.filter { post -> seen.add(post.id) }
                    mutableUiState.update {
                        it.copy(
                            posts = it.posts + fresh,
                            hasMore = result.value.hasMore,
                            isLoadingMore = false,
                            loadMoreError = false,
                        )
                    }
                    registerViews(fresh)
                    resolveEmojiUrls()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(isLoadingMore = false, loadMoreError = true)
                }
            }
        }
    }

    fun retryLoadMore() {
        mutableUiState.update { it.copy(loadMoreError = false) }
        loadMore()
    }

    fun dismissActionError() {
        mutableUiState.update { it.copy(actionError = null) }
    }

    fun togglePostLike(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = PostActionError.Unauthorized) }
            return
        }
        if (postId in pendingLikes) return

        val item = state.posts.firstOrNull { it.id == postId } ?: return
        val target = item.target
        if (target.author.username.isBlank()) return

        val nextLiked = !target.likedByMe
        pendingLikes += postId
        markPostPending(postId, pending = true)
        updatePostTarget(postId) {
            it.copy(
                likedByMe = nextLiked,
                likesCount = (it.likesCount + if (nextLiked) 1 else -1).coerceAtLeast(0),
            )
        }

        viewModelScope.launch {
            when (val result = communityRepository.setLiked(target.author.username, target.id, nextLiked)) {
                is AuthResult.Success -> updatePostTarget(postId) {
                    it.copy(likedByMe = result.value.likedByMe, likesCount = result.value.likesCount)
                }

                is AuthResult.Failure -> {
                    updatePostTarget(postId) {
                        it.copy(likedByMe = target.likedByMe, likesCount = target.likesCount)
                    }
                    mutableUiState.update { it.copy(actionError = result.error.toPostActionError()) }
                }
            }
            pendingLikes -= postId
            markPostPending(postId, pending = false)
        }
    }

    fun togglePostRepost(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = PostActionError.Unauthorized) }
            return
        }
        val item = state.posts.firstOrNull { it.id == postId } ?: return
        val target = item.target
        if (target.author.username.isBlank()) return

        if (!target.repostedByMe) {
            mutableUiState.update { it.copy(repost = RepostDialogUiState(post = item)) }
            return
        }
        if (postId in pendingReposts) return

        pendingReposts += postId
        markPostPending(postId, pending = true)
        updatePostTarget(postId) {
            it.copy(repostedByMe = false, repostsCount = (it.repostsCount - 1).coerceAtLeast(0))
        }

        viewModelScope.launch {
            when (val result = communityRepository.setReposted(
                username = target.author.username,
                postId = target.id,
                reposted = false,
                content = emptyMap(),
                images = emptyList(),
            )) {
                is AuthResult.Success -> updatePostTarget(postId) {
                    it.copy(
                        repostedByMe = result.value.repostedByMe,
                        repostsCount = result.value.repostsCount,
                    )
                }

                is AuthResult.Failure -> {
                    updatePostTarget(postId) {
                        it.copy(repostedByMe = target.repostedByMe, repostsCount = target.repostsCount)
                    }
                    mutableUiState.update { it.copy(actionError = result.error.toPostActionError()) }
                }
            }
            pendingReposts -= postId
            markPostPending(postId, pending = false)
        }
    }

    fun dismissRepost() {
        if (mutableUiState.value.repost.isSubmitting) return
        mutableUiState.update { it.copy(repost = RepostDialogUiState()) }
    }

    fun confirmRepost(caption: String, imageUris: List<String>) {
        val state = mutableUiState.value
        val post = state.repost.post ?: return
        if (state.repost.isSubmitting) return

        val target = post.target
        if (target.author.username.isBlank()) return

        val images = imageUris.take(MaxPostImages).map { uri ->
            ComposerImageUi(id = UUID.randomUUID().toString(), uri = uri)
        }
        mutableUiState.update { it.copy(repost = it.repost.copy(isSubmitting = true, error = null)) }

        viewModelScope.launch {
            when (val upload = uploadImages(images)) {
                is ImageUploadFailure -> mutableUiState.update {
                    it.copy(repost = it.repost.copy(isSubmitting = false, error = upload.error))
                }

                is ImageUploadSuccess -> {
                    val content = caption.trim().takeIf { it.isNotBlank() }
                        ?.let { mapOf(postLocaleTag(languageTagProvider()) to it) }
                        .orEmpty()
                    when (val result = communityRepository.setReposted(
                        username = target.author.username,
                        postId = target.id,
                        reposted = true,
                        content = content,
                        images = upload.images,
                    )) {
                        is AuthResult.Success -> {
                            updatePostTarget(post.id) {
                                it.copy(
                                    repostedByMe = result.value.repostedByMe,
                                    repostsCount = result.value.repostsCount,
                                )
                            }
                            mutableUiState.update { it.copy(repost = RepostDialogUiState()) }
                        }

                        is AuthResult.Failure -> mutableUiState.update {
                            it.copy(
                                repost = it.repost.copy(
                                    isSubmitting = false,
                                    error = result.error.toPostActionError(),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    fun openReport(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = PostActionError.Unauthorized) }
            return
        }
        val post = state.posts.firstOrNull { it.id == postId } ?: return
        mutableUiState.update { it.copy(report = ReportDialogUiState(post = post)) }
    }

    fun dismissReport() {
        if (mutableUiState.value.report.isSubmitting) return
        mutableUiState.update { it.copy(report = ReportDialogUiState()) }
    }

    fun onReportReasonChange(reason: String) {
        mutableUiState.update { it.copy(report = it.report.copy(reason = reason, error = null)) }
    }

    fun onReportCommentChange(comment: String) {
        mutableUiState.update {
            it.copy(report = it.report.copy(comment = comment.take(ReportCommentMaxLength), error = null))
        }
    }

    fun submitReport() {
        val state = mutableUiState.value
        val post = state.report.post ?: return
        if (!state.report.canSubmit) return

        val target = post.target
        if (target.author.username.isBlank()) return

        mutableUiState.update { it.copy(report = it.report.copy(isSubmitting = true, error = null)) }
        viewModelScope.launch {
            when (val result = communityRepository.reportPost(
                username = target.author.username,
                postId = target.id,
                reason = state.report.reason,
                comment = state.report.comment.trim(),
            )) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        report = ReportDialogUiState(),
                        reportedPostIds = it.reportedPostIds + target.id,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        report = it.report.copy(
                            isSubmitting = false,
                            error = result.error.toPostActionError(),
                        ),
                    )
                }
            }
        }
    }

    fun onCommentsCountChanged(postId: String, count: Int) {
        updatePostTarget(postId) { it.copy(commentsCount = count.coerceAtLeast(0)) }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                val authenticated = session is SessionState.Authorized
                val pending = session is SessionState.Loading
                val wasAuthenticated = mutableUiState.value.isAuthenticated

                mutableUiState.update {
                    it.copy(isAuthenticated = authenticated, isAuthPending = pending)
                }

                val state = mutableUiState.value
                if (authenticated && state.mode == CommunityFeedMode.Following && !wasAuthenticated) {
                    loadFeed(keepContent = false)
                }
                if (!authenticated && !pending && state.mode == CommunityFeedMode.Following) {
                    moreJob?.cancel()
                    mutableUiState.update {
                        it.copy(
                            posts = emptyList(),
                            hasMore = false,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            loadError = false,
                            loadMoreError = false,
                        )
                    }
                }
            }
        }
    }

    private fun loadFeed(keepContent: Boolean) {
        val state = mutableUiState.value
        if (state.isFollowingLocked) return

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableUiState.update {
                it.copy(
                    isLoading = !keepContent,
                    isRefreshing = keepContent,
                    loadError = false,
                    loadMoreError = false,
                )
            }

            when (val result = communityRepository.fetchFeed(
                mode = mutableUiState.value.mode,
                limit = CommunityFeedPageSize,
                offset = 0,
            )) {
                is AuthResult.Success -> {
                    val posts = result.value.posts
                    mutableUiState.update {
                        it.copy(
                            posts = posts,
                            hasMore = result.value.hasMore,
                            isLoading = false,
                            isRefreshing = false,
                            loadError = false,
                        )
                    }
                    registerViews(posts)
                    resolveEmojiUrls()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        loadError = it.posts.isEmpty(),
                    )
                }
            }
        }
    }

    private fun updatePostTarget(postId: String, transform: (CommunityPost) -> CommunityPost) {
        mutableUiState.update { state ->
            state.copy(posts = state.posts.map { if (it.id == postId) it.mapTarget(transform) else it })
        }
    }

    private fun markPostPending(postId: String, pending: Boolean) {
        mutableUiState.update { state ->
            state.copy(
                pendingPostIds = if (pending) {
                    state.pendingPostIds + postId
                } else {
                    state.pendingPostIds - postId
                },
            )
        }
    }

    private fun registerViews(posts: List<CommunityPost>) {
        posts.forEach { post ->
            val target = post.target
            if (target.id.isBlank() || target.author.username.isBlank()) return@forEach
            if (!viewedPostIds.add(target.id)) return@forEach

            viewModelScope.launch {
                communityRepository.registerView(target.author.username, target.id)
            }
        }
    }

    private fun resolveEmojiUrls() {
        val state = mutableUiState.value
        val names = buildList {
            state.posts.forEach { post ->
                post.content.values.forEach { addAll(collectEmojiNames(it)) }
                post.repostContent.values.forEach { addAll(collectEmojiNames(it)) }
                post.commentsPreview.forEach { addAll(collectEmojiNames(it.content)) }
            }
        }.filter { it !in state.emojiUrls }.distinct()

        if (names.isEmpty()) return

        emojiJob?.cancel()
        emojiJob = viewModelScope.launch {
            val resolved = emojisRepository.resolveUrls(names)
            if (resolved.isNotEmpty()) {
                mutableUiState.update { it.copy(emojiUrls = it.emojiUrls + resolved) }
            }
        }
    }

    private suspend fun uploadImages(images: List<ComposerImageUi>): ImageUpload {
        val uploaded = mutableListOf<PostImage>()
        images.forEach { image ->
            val existing = image.asPostImage()
            if (existing != null) {
                uploaded += existing
                return@forEach
            }
            val uri = image.uri ?: return@forEach
            when (val result = communityRepository.uploadImage(uri)) {
                is ImageUploadResult.Success -> uploaded += PostImage(
                    url = result.image.url,
                    mime = result.image.mime,
                    size = result.image.size,
                )

                ImageUploadResult.Unsupported -> return ImageUploadFailure(PostActionError.ImageUnsupported)
                ImageUploadResult.TooLarge -> return ImageUploadFailure(PostActionError.ImageTooLarge)
                ImageUploadResult.Unreadable -> return ImageUploadFailure(PostActionError.ImageUnreadable)
                is ImageUploadResult.Failure -> return ImageUploadFailure(result.error.toPostActionError())
            }
        }
        return ImageUploadSuccess(uploaded)
    }

    companion object {
        private const val ReportCommentMaxLength = 500

        fun factory(mode: CommunityFeedMode = CommunityFeedMode.Popular): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application =
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                    CommunityViewModel(
                        communityRepository = application.container.communityRepository,
                        authRepository = application.container.authRepository,
                        emojisRepository = application.container.emojisRepository,
                        initialMode = mode,
                    )
                }
            }
    }
}

private sealed interface ImageUpload

private data class ImageUploadSuccess(val images: List<PostImage>) : ImageUpload

private data class ImageUploadFailure(val error: PostActionError) : ImageUpload
