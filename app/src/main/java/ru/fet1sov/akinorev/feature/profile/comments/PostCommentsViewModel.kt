package ru.fet1sov.akinorev.feature.profile.comments

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.community.CommentsPageSize
import ru.fet1sov.akinorev.data.community.CommunityRepository
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.feature.profile.PostActionError
import ru.fet1sov.akinorev.feature.profile.toPostActionError
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.model.collectEmojiNames

@Immutable
data class PostCommentsUiState(
    val comments: List<PostComment> = emptyList(),
    val total: Int = 0,
    val isLoading: Boolean = true,
    val loadError: PostActionError? = null,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val loadMoreError: PostActionError? = null,
    val isSubmitting: Boolean = false,
    val submitError: PostActionError? = null,
    val updatingCommentId: String? = null,
    val editError: PostActionError? = null,
    val deletingCommentId: String? = null,
    val deleteError: PostActionError? = null,
    val actionError: PostActionError? = null,
    val emojiUrls: Map<String, String> = emptyMap(),
    val isAuthenticated: Boolean = false,
    val pendingLikeIds: Set<String> = emptySet(),
)

sealed interface PostCommentsEvent {
    data class Submitted(val parentId: String?) : PostCommentsEvent

    data class Edited(val commentId: String) : PostCommentsEvent

    data class Deleted(val commentId: String) : PostCommentsEvent
}

class PostCommentsViewModel(
    private val username: String,
    private val postId: String,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val emojisRepository: EmojisRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(PostCommentsUiState())
    val uiState: StateFlow<PostCommentsUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = MutableSharedFlow<PostCommentsEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<PostCommentsEvent> = mutableEvents.asSharedFlow()

    private var loadJob: Job? = null
    private var emojiJob: Job? = null

    init {
        observeSession()
        load()
    }

    fun retry() {
        if (mutableUiState.value.isLoading) return
        load()
    }

    fun loadMore() {
        val state = mutableUiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
            when (val result = communityRepository.fetchComments(
                username = username,
                postId = postId,
                limit = CommentsPageSize,
                offset = mutableUiState.value.comments.size,
            )) {
                is AuthResult.Success -> {
                    val existing = mutableUiState.value.comments
                    val fresh = result.value.comments.filter { comment -> existing.none { it.id == comment.id } }
                    mutableUiState.update {
                        it.copy(
                            comments = it.comments + fresh,
                            total = result.value.total,
                            hasMore = result.value.hasMore,
                            isLoadingMore = false,
                        )
                    }
                    resolveEmojiUrls()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isLoadingMore = false,
                        loadMoreError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    fun retryLoadMore() {
        mutableUiState.update { it.copy(loadMoreError = null) }
        loadMore()
    }

    fun submit(content: String, parentId: String? = null) {
        val text = content.trim()
        if (text.isEmpty()) return
        if (mutableUiState.value.isSubmitting) return
        if (!mutableUiState.value.isAuthenticated) {
            mutableUiState.update { it.copy(submitError = PostActionError.Unauthorized) }
            return
        }

        mutableUiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = communityRepository.createComment(
                username = username,
                postId = postId,
                content = text,
                parentId = parentId,
            )) {
                is AuthResult.Success -> {
                    mutableUiState.update { it.copy(isSubmitting = false, submitError = null) }
                    mutableEvents.emit(PostCommentsEvent.Submitted(parentId))
                    load()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    fun updateComment(commentId: String, content: String) {
        val text = content.trim()
        if (text.isEmpty()) return
        if (mutableUiState.value.updatingCommentId != null) return

        mutableUiState.update { it.copy(updatingCommentId = commentId, editError = null) }
        viewModelScope.launch {
            when (val result = communityRepository.updateComment(
                username = username,
                postId = postId,
                commentId = commentId,
                content = text,
            )) {
                is AuthResult.Success -> {
                    val updated = result.value
                    mutableUiState.update { current ->
                        current.copy(
                            comments = current.comments.mapCommentTree(commentId) { existing ->
                                updated.copy(
                                    children = if (updated.children.isEmpty()) {
                                        existing.children
                                    } else {
                                        updated.children
                                    },
                                )
                            },
                            updatingCommentId = null,
                            editError = null,
                        )
                    }
                    mutableEvents.emit(PostCommentsEvent.Edited(commentId))
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        updatingCommentId = null,
                        editError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    fun deleteComment(commentId: String) {
        if (mutableUiState.value.deletingCommentId != null) return

        mutableUiState.update { it.copy(deletingCommentId = commentId, deleteError = null) }
        viewModelScope.launch {
            when (val result = communityRepository.deleteComment(username, postId, commentId)) {
                is AuthResult.Success -> {
                    mutableUiState.update { it.copy(deletingCommentId = null, deleteError = null) }
                    mutableEvents.emit(PostCommentsEvent.Deleted(commentId))
                    load()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        deletingCommentId = null,
                        deleteError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    fun toggleLike(commentId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = PostActionError.Unauthorized) }
            return
        }
        if (commentId in state.pendingLikeIds) return

        val comment = state.comments.findComment(commentId) ?: return
        val nextLiked = !comment.likedByMe
        mutableUiState.update {
            it.copy(
                pendingLikeIds = it.pendingLikeIds + commentId,
                comments = it.comments.mapCommentTree(commentId) { target ->
                    target.copy(
                        likedByMe = nextLiked,
                        likesCount = (target.likesCount + if (nextLiked) 1 else -1).coerceAtLeast(0),
                    )
                },
            )
        }

        viewModelScope.launch {
            when (val result = communityRepository.setCommentLiked(
                username = username,
                postId = postId,
                commentId = commentId,
                liked = nextLiked,
            )) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        comments = it.comments.mapCommentTree(commentId) { target ->
                            target.copy(
                                likedByMe = result.value.likedByMe,
                                likesCount = result.value.likesCount,
                            )
                        },
                        pendingLikeIds = it.pendingLikeIds - commentId,
                    )
                }

                is AuthResult.Failure -> {
                    mutableUiState.update {
                        it.copy(
                            comments = it.comments.mapCommentTree(commentId) { target ->
                                target.copy(
                                    likedByMe = comment.likedByMe,
                                    likesCount = comment.likesCount,
                                )
                            },
                            pendingLikeIds = it.pendingLikeIds - commentId,
                            actionError = result.error.toPostActionError(),
                        )
                    }
                }
            }
        }
    }

    fun clearSubmitError() {
        mutableUiState.update { it.copy(submitError = null) }
    }

    fun clearEditError() {
        mutableUiState.update { it.copy(editError = null) }
    }

    fun clearDeleteError() {
        mutableUiState.update { it.copy(deleteError = null) }
    }

    fun clearActionError() {
        mutableUiState.update { it.copy(actionError = null) }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val existing = mutableUiState.value.comments
            mutableUiState.update {
                it.copy(
                    isLoading = existing.isEmpty(),
                    loadError = null,
                    loadMoreError = null,
                )
            }

            val limit = maxOf(CommentsPageSize, existing.size).coerceAtMost(MaxRootsLimit)
            when (val result = communityRepository.fetchComments(
                username = username,
                postId = postId,
                limit = limit,
                offset = 0,
            )) {
                is AuthResult.Success -> {
                    mutableUiState.update {
                        it.copy(
                            comments = result.value.comments,
                            total = result.value.total,
                            hasMore = result.value.hasMore,
                            isLoading = false,
                            isLoadingMore = false,
                            loadError = null,
                        )
                    }
                    resolveEmojiUrls()
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                mutableUiState.update {
                    it.copy(isAuthenticated = session is SessionState.Authorized)
                }
            }
        }
    }

    private fun resolveEmojiUrls() {
        val state = mutableUiState.value
        val names = buildList {
            state.comments.forEach { comment -> addAll(comment.collectEmojiNames()) }
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

    companion object {
        private const val MaxRootsLimit = 50

        fun factory(username: String, postId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                PostCommentsViewModel(
                    username = username,
                    postId = postId,
                    communityRepository = application.container.communityRepository,
                    authRepository = application.container.authRepository,
                    emojisRepository = application.container.emojisRepository,
                )
            }
        }
    }
}

private fun List<PostComment>.mapCommentTree(
    commentId: String,
    transform: (PostComment) -> PostComment,
): List<PostComment> = map { comment ->
    if (comment.id == commentId) {
        transform(comment)
    } else if (comment.children.isEmpty()) {
        comment
    } else {
        comment.copy(children = comment.children.mapCommentTree(commentId, transform))
    }
}

private fun List<PostComment>.findComment(commentId: String): PostComment? {
    for (comment in this) {
        if (comment.id == commentId) return comment
        comment.children.findComment(commentId)?.let { return it }
    }
    return null
}

private fun PostComment.collectEmojiNames(): List<String> = buildList {
    addAll(collectEmojiNames(content))
    children.forEach { child -> addAll(child.collectEmojiNames()) }
}
