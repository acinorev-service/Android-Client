package ru.fet1sov.akinorev.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.community.CommunityRepository
import ru.fet1sov.akinorev.data.community.ImageUploadResult
import ru.fet1sov.akinorev.data.community.MaxPostImages
import ru.fet1sov.akinorev.data.community.PostsPageSize
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.data.realtime.parseFollowersEvent
import ru.fet1sov.akinorev.data.realtime.parsePresenceEvent
import ru.fet1sov.akinorev.data.realtime.unwatchFollowersMessage
import ru.fet1sov.akinorev.data.realtime.unwatchPresenceMessage
import ru.fet1sov.akinorev.data.realtime.watchFollowersMessage
import ru.fet1sov.akinorev.data.realtime.watchPresenceMessage
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.PostLocales
import ru.fet1sov.akinorev.model.ProfileRelation
import ru.fet1sov.akinorev.model.appendImageMarkers
import ru.fet1sov.akinorev.model.collectEmojiNames
import ru.fet1sov.akinorev.model.filledPostContent
import ru.fet1sov.akinorev.model.postLocaleTag
import ru.fet1sov.akinorev.model.rebuildImageMarkers
import java.util.Locale
import java.util.UUID

class ProfileViewModel(
    private val initialUsername: String,
    private val communityRepository: CommunityRepository,
    private val authRepository: AuthRepository,
    private val emojisRepository: EmojisRepository,
    private val realtimeSocket: RealtimeSocket,
    private val languageTagProvider: () -> String = { Locale.getDefault().language },
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        ProfileUiState(
            username = initialUsername,
            isLoading = true,
            composer = PostComposerUiState(
                content = emptyContent(),
                activeLocale = postLocaleTag(languageTagProvider()),
            ),
        ),
    )
    val uiState: StateFlow<ProfileUiState> = mutableUiState.asStateFlow()

    private var currentUsername: String = initialUsername
    private var ownUsername: String? = null
    private var isAuthenticated: Boolean = false

    private var loadJob: Job? = null
    private var postsJob: Job? = null
    private var emojiJob: Job? = null

    private val viewedPostIds = mutableSetOf<String>()
    private val pendingLikes = mutableSetOf<String>()
    private val pendingReposts = mutableSetOf<String>()
    private val pendingPins = mutableSetOf<String>()

    init {
        observeSession()
        observeRealtime()
        load(keepContent = false)
    }

    fun refresh() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing) return
        load(keepContent = state.posts.isNotEmpty())
    }

    fun retry() {
        if (mutableUiState.value.isLoading) return
        load(keepContent = false)
    }

    fun loadMorePosts() {
        val state = mutableUiState.value
        if (state.isBlocked || state.isPostsLoading || state.isLoadingMore) return
        if (!state.hasMorePosts || state.loadMoreError != null) return

        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
            val username = profileUsername()
            val offset = mutableUiState.value.posts.size
            when (val result = communityRepository.fetchPosts(username, PostsPageSize, offset)) {
                is AuthResult.Success -> {
                    val existing = mutableUiState.value.posts
                    val fresh = result.value.posts.filter { post -> existing.none { it.id == post.id } }
                    mutableUiState.update {
                        it.copy(
                            posts = it.posts + fresh,
                            isLoadingMore = false,
                            hasMorePosts = result.value.hasMore,
                        )
                    }
                    registerViews(fresh)
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
        loadMorePosts()
    }

    fun toggleFollow() {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = ProfileActionError.Unauthorized) }
            return
        }
        if (state.isSelf || state.isActionPending) return

        val username = profileUsername()
        val following = state.relation?.isFollowing == true
        mutableUiState.update { it.copy(isActionPending = true, actionError = null) }

        viewModelScope.launch {
            when (val result = communityRepository.setFollowing(username, follow = !following)) {
                is AuthResult.Success -> mutableUiState.update { current ->
                    val relation = current.relation?.copy(isFollowing = result.value.isFollowing)
                        ?: ProfileRelation(
                            isSelf = false,
                            isFollowing = result.value.isFollowing,
                            isBlocked = current.profile?.isBlocked == true,
                            blockedMe = false,
                        )
                    current.copy(
                        relation = relation,
                        profile = current.profile?.copy(followersCount = result.value.followersCount),
                        isActionPending = false,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isActionPending = false,
                        actionError = result.error.toProfileActionError(),
                    )
                }
            }
        }
    }

    fun toggleBlock() {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = ProfileActionError.Unauthorized) }
            return
        }
        if (state.isSelf || state.isActionPending) return

        val username = profileUsername()
        val blocked = state.relation?.isBlocked == true || state.profile?.isBlocked == true
        mutableUiState.update { it.copy(isActionPending = true, actionError = null) }

        viewModelScope.launch {
            when (val result = communityRepository.setBlocked(username, blocked = !blocked)) {
                is AuthResult.Success -> {
                    val isBlocked = result.value
                    mutableUiState.update { current ->
                        val relation = current.relation?.copy(
                            isBlocked = isBlocked,
                            isFollowing = if (isBlocked) false else current.relation.isFollowing,
                        )
                        current.copy(
                            relation = relation,
                            profile = current.profile?.copy(isBlocked = isBlocked),
                            isActionPending = false,
                            posts = if (isBlocked) emptyList() else current.posts,
                            hasMorePosts = if (isBlocked) false else current.hasMorePosts,
                            isOnline = if (isBlocked) false else current.isOnline,
                            presenceDevice = if (isBlocked) "" else current.presenceDevice,
                        )
                    }
                    if (isBlocked) unsubscribePresence(username)
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isActionPending = false,
                        actionError = result.error.toProfileActionError(),
                    )
                }
            }
        }
    }

    fun startEditingProfile() {
        val state = mutableUiState.value
        val profile = state.profile ?: return
        if (!state.isSelf) return

        mutableUiState.update {
            it.copy(
                editing = ProfileEditUiState(
                    isActive = true,
                    username = profile.username,
                    bio = profile.bio,
                    nameColor = profile.nameColor.ifBlank { DefaultNameColor },
                    nameAnimation = profile.nameAnimation,
                ),
            )
        }
    }

    fun cancelEditingProfile() {
        mutableUiState.update { it.copy(editing = ProfileEditUiState()) }
    }

    fun onEditUsernameChange(value: String) {
        mutableUiState.update { it.copy(editing = it.editing.copy(username = value.take(24), error = null)) }
    }

    fun onEditBioChange(value: String) {
        val limited = value.take(BioMaxLength)
        mutableUiState.update { it.copy(editing = it.editing.copy(bio = limited, error = null)) }
    }

    fun onEditNameColorChange(value: String) {
        mutableUiState.update { it.copy(editing = it.editing.copy(nameColor = value, error = null)) }
    }

    fun onEditNameAnimationChange(value: String) {
        mutableUiState.update { it.copy(editing = it.editing.copy(nameAnimation = value, error = null)) }
    }

    fun onAvatarPicked(uri: String) {
        mutableUiState.update { it.copy(editing = it.editing.copy(avatarUri = uri, error = null)) }
    }

    fun appendBioEmoji(name: String) {
        val state = mutableUiState.value
        if (!state.editing.isActive) return

        val marker = ":$name:"
        val current = state.editing.bio
        val separator = if (current.isEmpty() || current.last().isWhitespace()) "" else " "
        val next = current + separator + marker
        if (next.codePointCount(0, next.length) > BioMaxLength) return

        mutableUiState.update {
            it.copy(editing = it.editing.copy(bio = next, error = null))
        }
    }

    fun saveProfile() {
        val state = mutableUiState.value
        val profile = state.profile ?: return
        val edit = state.editing
        if (!edit.isActive || edit.isSaving) return

        val username = edit.username.trim()
        when {
            username.length < 3 -> {
                mutableUiState.update { it.copy(editing = it.editing.copy(error = ProfileEditError.UsernameTooShort)) }
                return
            }

            username.length > 24 || !UsernameRegex.matches(username) -> {
                mutableUiState.update { it.copy(editing = it.editing.copy(error = ProfileEditError.InvalidValue)) }
                return
            }

            edit.bio.codePointCount(0, edit.bio.length) > BioMaxLength -> {
                mutableUiState.update { it.copy(editing = it.editing.copy(error = ProfileEditError.BioTooLong)) }
                return
            }
        }

        if (profile.isSubscribed) {
            val colorChanged = edit.nameColor != profile.nameColor.ifBlank { DefaultNameColor }
            if (colorChanged && !HexColorRegex.matches(edit.nameColor)) {
                mutableUiState.update { it.copy(editing = it.editing.copy(error = ProfileEditError.InvalidValue)) }
                return
            }
            val animationChanged = edit.nameAnimation != profile.nameAnimation
            if (animationChanged && edit.nameAnimation !in NameAnimations) {
                mutableUiState.update { it.copy(editing = it.editing.copy(error = ProfileEditError.InvalidValue)) }
                return
            }
        }

        mutableUiState.update {
            it.copy(editing = it.editing.copy(isSaving = true, error = null))
        }

        viewModelScope.launch {
            val avatarUrl = if (edit.avatarUri != null) {
                when (val upload = communityRepository.uploadImage(edit.avatarUri)) {
                    is ImageUploadResult.Success -> upload.image.url
                    ImageUploadResult.Unsupported -> {
                        failEditing(ProfileEditError.AvatarUnsupported)
                        return@launch
                    }

                    ImageUploadResult.TooLarge -> {
                        failEditing(ProfileEditError.AvatarTooLarge)
                        return@launch
                    }

                    ImageUploadResult.Unreadable -> {
                        failEditing(ProfileEditError.AvatarUnreadable)
                        return@launch
                    }

                    is ImageUploadResult.Failure -> {
                        failEditing(upload.error.toProfileEditError())
                        return@launch
                    }
                }
            } else {
                null
            }

            val nameColor = edit.nameColor.takeIf {
                profile.isSubscribed && it != profile.nameColor.ifBlank { DefaultNameColor }
            }
            val nameAnimation = edit.nameAnimation.takeIf {
                profile.isSubscribed && it != profile.nameAnimation
            }
            val newUsername = username.takeIf { it != profile.username }
            val newBio = edit.bio.takeIf { it != profile.bio }

            if (avatarUrl == null && nameColor == null && nameAnimation == null && newUsername == null && newBio == null) {
                mutableUiState.update { it.copy(editing = ProfileEditUiState()) }
                return@launch
            }

            when (val result = authRepository.updateProfile(
                bio = newBio,
                username = newUsername,
                avatar = avatarUrl,
                nameColor = nameColor,
                nameAnimation = nameAnimation,
            )) {
                is AuthResult.Success -> {
                    val updated = result.value
                    val renamed = !updated.username.equals(profile.username, ignoreCase = true)
                    currentUsername = updated.username
                    mutableUiState.update {
                        it.copy(
                            editing = ProfileEditUiState(),
                            isSelf = true,
                            profile = it.profile?.copy(
                                username = updated.username,
                                bio = updated.bio,
                                avatarPath = updated.avatar.ifBlank { it.profile?.avatarPath.orEmpty() },
                                nameColor = updated.nameColor,
                                nameAnimation = updated.nameAnimation,
                            ),
                            renamedTo = if (renamed) updated.username else null,
                        )
                    }
                    if (!renamed) refresh()
                }

                is AuthResult.Failure -> failEditing(result.error.toProfileEditError())
            }
        }
    }

    fun onComposerTextChange(locale: String, text: String) {
        if (locale !in PostLocales) return
        mutableUiState.update { state ->
            state.copy(
                composer = state.composer.copy(
                    content = state.composer.content + (locale to text.take(PostMaxLength)),
                    error = null,
                ),
            )
        }
    }

    fun onComposerLocaleChange(locale: String) {
        if (locale !in PostLocales) return
        mutableUiState.update { it.copy(composer = it.composer.copy(activeLocale = locale)) }
    }

    fun onComposerImagesPicked(uris: List<String>) {
        if (uris.isEmpty()) return
        mutableUiState.update { state ->
            val remaining = MaxPostImages - state.composer.images.size
            if (remaining <= 0) return@update state

            val accepted = uris.take(remaining)
            val firstIndex = state.composer.images.size
            val images = state.composer.images + accepted.map { uri ->
                ComposerImageUi(id = UUID.randomUUID().toString(), uri = uri)
            }
            val active = state.composer.activeLocale
            val content = state.composer.content + (
                active to appendImageMarkers(
                    state.composer.content[active].orEmpty(),
                    firstIndex = firstIndex,
                    count = accepted.size,
                )
                )
            state.copy(composer = state.composer.copy(images = images, content = content, error = null))
        }
    }

    fun removeComposerImage(id: String) {
        mutableUiState.update { state ->
            val images = state.composer.images.filterNot { it.id == id }
            if (images.size == state.composer.images.size) return@update state
            state.copy(
                composer = state.composer.copy(
                    images = images,
                    content = rebuildImageMarkers(state.composer.content, images.size),
                    error = null,
                ),
            )
        }
    }

    fun publishPost() {
        val state = mutableUiState.value
        if (!state.canManagePosts || state.composer.isPublishing) return

        val content = filledPostContent(state.composer.content)
        if (content.isEmpty()) {
            mutableUiState.update { it.copy(composer = it.composer.copy(error = PostActionError.BadRequest)) }
            return
        }

        mutableUiState.update { it.copy(composer = it.composer.copy(isPublishing = true, error = null)) }
        viewModelScope.launch {
            when (val upload = uploadImages(state.composer.images)) {
                is ImageUploadFailure -> mutableUiState.update {
                    it.copy(composer = it.composer.copy(isPublishing = false, error = upload.error))
                }

                is ImageUploadSuccess -> when (val result = communityRepository.createPost(
                    username = profileUsername(),
                    content = content,
                    images = upload.images,
                )) {
                    is AuthResult.Success -> {
                        mutableUiState.update {
                            it.copy(
                                posts = sortPosts(listOf(result.value) + it.posts),
                                composer = PostComposerUiState(
                                    content = emptyContent(),
                                    activeLocale = it.composer.activeLocale,
                                ),
                            )
                        }
                        resolveEmojiUrls()
                    }

                    is AuthResult.Failure -> mutableUiState.update {
                        it.copy(
                            composer = it.composer.copy(
                                isPublishing = false,
                                error = result.error.toPostActionError(),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun clearComposerError() {
        mutableUiState.update { it.copy(composer = it.composer.copy(error = null)) }
    }

    fun togglePostLike(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = ProfileActionError.Unauthorized) }
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
                    mutableUiState.update { it.copy(actionError = result.error.toProfileActionError()) }
                }
            }
            pendingLikes -= postId
            markPostPending(postId, pending = false)
        }
    }

    fun togglePostRepost(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = ProfileActionError.Unauthorized) }
            return
        }
        val item = state.posts.firstOrNull { it.id == postId } ?: return
        val target = item.target
        if (target.author.username.isBlank()) return

        if (!target.repostedByMe) {
            mutableUiState.update {
                it.copy(repost = RepostDialogUiState(post = item))
            }
            return
        }

        if (postId in pendingReposts) return
        pendingReposts += postId
        markPostPending(postId, pending = true)
        updatePostTarget(postId) {
            it.copy(
                repostedByMe = false,
                repostsCount = (it.repostsCount - 1).coerceAtLeast(0),
            )
        }

        viewModelScope.launch {
            when (val result = communityRepository.setReposted(
                username = target.author.username,
                postId = target.id,
                reposted = false,
                content = emptyMap(),
                images = emptyList(),
            )) {
                is AuthResult.Success -> {
                    updatePostTarget(postId) {
                        it.copy(
                            repostedByMe = result.value.repostedByMe,
                            repostsCount = result.value.repostsCount,
                        )
                    }
                    if (mutableUiState.value.isSelf) refresh()
                }

                is AuthResult.Failure -> {
                    updatePostTarget(postId) {
                        it.copy(repostedByMe = target.repostedByMe, repostsCount = target.repostsCount)
                    }
                    mutableUiState.update { it.copy(actionError = result.error.toProfileActionError()) }
                }
            }
            pendingReposts -= postId
            markPostPending(postId, pending = false)
        }
    }

    fun dismissRepost() {
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
                            if (mutableUiState.value.isSelf) refresh()
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

    fun togglePinned(postId: String) {
        val state = mutableUiState.value
        if (!state.canManagePosts || postId in pendingPins) return

        val item = state.posts.firstOrNull { it.id == postId } ?: return
        val nextPinned = !item.isPinned
        pendingPins += postId
        markPostPending(postId, pending = true)

        viewModelScope.launch {
            when (val result = communityRepository.setPinned(profileUsername(), item.id, nextPinned)) {
                is AuthResult.Success -> {
                    val updated = result.value
                    mutableUiState.update { current ->
                        val posts = current.posts.map { post ->
                            when {
                                post.id == updated.id -> updated
                                updated.isPinned -> post.copy(isPinned = false)
                                else -> post
                            }
                        }
                        current.copy(posts = sortPosts(posts))
                    }
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(actionError = result.error.toProfileActionError())
                }
            }
            pendingPins -= postId
            markPostPending(postId, pending = false)
        }
    }

    fun openPostEditor(postId: String) {
        val state = mutableUiState.value
        if (!state.canManagePosts) return
        val post = state.posts.firstOrNull { it.id == postId } ?: return
        if (post.isRepost) return

        val activeLocale = postLocaleTag(languageTagProvider())
        val content = PostLocales.associateWith { locale -> post.content[locale].orEmpty() }
        val images = post.images.map { image ->
            ComposerImageUi(
                id = UUID.randomUUID().toString(),
                url = image.url,
                mime = image.mime,
            )
        }

        mutableUiState.update {
            it.copy(
                postEditor = PostEditorUiState(
                    postId = post.id,
                    content = content,
                    activeLocale = activeLocale,
                    images = images,
                ),
            )
        }
    }

    fun closePostEditor() {
        val state = mutableUiState.value
        if (state.postEditor.isSaving) return
        mutableUiState.update { it.copy(postEditor = PostEditorUiState()) }
    }

    fun onEditorTextChange(locale: String, text: String) {
        if (locale !in PostLocales) return
        mutableUiState.update { state ->
            state.copy(
                postEditor = state.postEditor.copy(
                    content = state.postEditor.content + (locale to text.take(PostMaxLength)),
                    error = null,
                ),
            )
        }
    }

    fun onEditorLocaleChange(locale: String) {
        if (locale !in PostLocales) return
        mutableUiState.update { it.copy(postEditor = it.postEditor.copy(activeLocale = locale)) }
    }

    fun onEditorImagesPicked(uris: List<String>) {
        if (uris.isEmpty()) return
        mutableUiState.update { state ->
            val remaining = MaxPostImages - state.postEditor.images.size
            if (remaining <= 0) return@update state

            val accepted = uris.take(remaining)
            val firstIndex = state.postEditor.images.size
            val images = state.postEditor.images + accepted.map { uri ->
                ComposerImageUi(id = UUID.randomUUID().toString(), uri = uri)
            }
            val active = state.postEditor.activeLocale
            val content = state.postEditor.content + (
                active to appendImageMarkers(
                    state.postEditor.content[active].orEmpty(),
                    firstIndex = firstIndex,
                    count = accepted.size,
                )
                )
            state.copy(postEditor = state.postEditor.copy(images = images, content = content, error = null))
        }
    }

    fun removeEditorImage(id: String) {
        mutableUiState.update { state ->
            val images = state.postEditor.images.filterNot { it.id == id }
            if (images.size == state.postEditor.images.size) return@update state
            state.copy(
                postEditor = state.postEditor.copy(
                    images = images,
                    content = rebuildImageMarkers(state.postEditor.content, images.size),
                    error = null,
                ),
            )
        }
    }

    fun savePostEdit() {
        val state = mutableUiState.value
        val postId = state.postEditor.postId ?: return
        if (state.postEditor.isSaving) return

        val content = filledPostContent(state.postEditor.content)
        if (content.isEmpty()) {
            mutableUiState.update { it.copy(postEditor = it.postEditor.copy(error = PostActionError.BadRequest)) }
            return
        }

        mutableUiState.update { it.copy(postEditor = it.postEditor.copy(isSaving = true, error = null)) }
        viewModelScope.launch {
            when (val upload = uploadImages(state.postEditor.images)) {
                is ImageUploadFailure -> mutableUiState.update {
                    it.copy(postEditor = it.postEditor.copy(isSaving = false, error = upload.error))
                }

                is ImageUploadSuccess -> when (val result = communityRepository.updatePost(
                    username = profileUsername(),
                    postId = postId,
                    content = content,
                    images = upload.images,
                )) {
                    is AuthResult.Success -> {
                        val updated = result.value
                        mutableUiState.update { current ->
                            current.copy(
                                posts = sortPosts(current.posts.map { if (it.id == updated.id) updated else it }),
                                postEditor = PostEditorUiState(),
                            )
                        }
                        resolveEmojiUrls()
                    }

                    is AuthResult.Failure -> mutableUiState.update {
                        it.copy(
                            postEditor = it.postEditor.copy(
                                isSaving = false,
                                error = result.error.toPostActionError(),
                            ),
                        )
                    }
                }
            }
        }
    }

    fun askDeletePost(postId: String) {
        if (!mutableUiState.value.canManagePosts) return
        mutableUiState.update { it.copy(pendingDeletePostId = postId, postError = null) }
    }

    fun cancelDeletePost() {
        if (mutableUiState.value.isDeletingPost) return
        mutableUiState.update { it.copy(pendingDeletePostId = null, postError = null) }
    }

    fun confirmDeletePost() {
        val state = mutableUiState.value
        val postId = state.pendingDeletePostId ?: return
        if (state.isDeletingPost) return

        mutableUiState.update { it.copy(isDeletingPost = true, postError = null) }
        viewModelScope.launch {
            when (val result = communityRepository.deletePost(profileUsername(), postId)) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        posts = it.posts.filterNot { post -> post.id == postId },
                        pendingDeletePostId = null,
                        isDeletingPost = false,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isDeletingPost = false,
                        postError = result.error.toPostActionError(),
                    )
                }
            }
        }
    }

    fun openReport(postId: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated) {
            mutableUiState.update { it.copy(actionError = ProfileActionError.Unauthorized) }
            return
        }
        val post = state.posts.firstOrNull { it.id == postId } ?: return
        mutableUiState.update {
            it.copy(report = ReportDialogUiState(post = post))
        }
    }

    fun dismissReport() {
        if (mutableUiState.value.report.isSubmitting) return
        mutableUiState.update { it.copy(report = ReportDialogUiState()) }
    }

    fun onReportReasonChange(reason: String) {
        mutableUiState.update {
            it.copy(report = it.report.copy(reason = reason, error = null))
        }
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

    fun clearActionError() {
        mutableUiState.update { it.copy(actionError = null) }
    }

    fun clearPostError() {
        mutableUiState.update { it.copy(postError = null) }
    }

    fun onRenameHandled() {
        mutableUiState.update { it.copy(renamedTo = null) }
    }

    private fun load(keepContent: Boolean) {
        loadJob?.cancel()
        postsJob?.cancel()

        val hasContent = keepContent && mutableUiState.value.posts.isNotEmpty()
        if (hasContent) {
            mutableUiState.update {
                it.copy(isRefreshing = true, isLoading = false, loadError = null, isBanned = false)
            }
        } else {
            mutableUiState.update {
                it.copy(
                    isLoading = true,
                    isRefreshing = false,
                    isLoadingMore = false,
                    loadError = null,
                    isBanned = false,
                    postsLoadError = null,
                    loadMoreError = null,
                    posts = emptyList(),
                    isPostsLoading = true,
                    hasMorePosts = false,
                )
            }
        }

        loadJob = viewModelScope.launch {
            when (val result = communityRepository.fetchProfile(currentUsername)) {
                is AuthResult.Success -> afterProfileLoaded(result.value, keepContent, hasContent)
                is AuthResult.Failure -> {
                    val error = result.error.toProfileLoadError()
                    if (hasContent) {
                        mutableUiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isPostsLoading = false,
                                actionError = result.error.toProfileActionError(),
                            )
                        }
                    } else {
                        mutableUiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                isPostsLoading = false,
                                loadError = error,
                                isBanned = error == ProfileLoadError.Banned,
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun afterProfileLoaded(
        profile: ru.fet1sov.akinorev.model.CommunityProfile,
        keepContent: Boolean,
        hasContent: Boolean,
    ) {
        currentUsername = profile.username
        val self = isOwnProfile(profile.username)

        mutableUiState.update { state ->
            state.copy(
                profile = profile,
                username = profile.username,
                isSelf = self,
                isLoading = false,
                isRefreshing = hasContent,
                isPostsLoading = !hasContent,
                loadError = null,
                isBanned = false,
                postsLoadError = null,
                lastSeenMillis = if (state.isOnline) state.lastSeenMillis else profile.lastSeenMillis,
                lastSeenDevice = profile.lastSeenDevice.ifBlank { state.lastSeenDevice },
                posts = if (keepContent) state.posts else emptyList(),
                hasMorePosts = if (keepContent) state.hasMorePosts else false,
            )
        }

        if (profile.isBlocked) {
            mutableUiState.update {
                it.copy(
                    isPostsLoading = false,
                    isRefreshing = false,
                    posts = emptyList(),
                    hasMorePosts = false,
                    isOnline = false,
                    presenceDevice = "",
                )
            }
            unsubscribePresence(profile.username)
            return
        }

        val (relation, postsResult) = coroutineScope {
            val relationDeferred = async {
                if (isAuthenticated && !self) {
                    communityRepository.fetchRelation(profile.username)
                } else {
                    null
                }
            }
            val postsDeferred = async {
                communityRepository.fetchPosts(profile.username, PostsPageSize, 0)
            }
            relationDeferred.await() to postsDeferred.await()
        }

        var relationValue: ProfileRelation? = mutableUiState.value.relation
        var blocked = false
        if (relation is AuthResult.Success) {
            relationValue = relation.value
            blocked = relation.value.isBlocked || relation.value.blockedMe
        }

        if (blocked) {
            mutableUiState.update {
                it.copy(
                    relation = relationValue,
                    isLoading = false,
                    isRefreshing = false,
                    isPostsLoading = false,
                    posts = emptyList(),
                    hasMorePosts = false,
                    isOnline = false,
                    presenceDevice = "",
                )
            }
            unsubscribePresence(profile.username)
            return
        }

        when (postsResult) {
            is AuthResult.Success -> {
                val posts = sortPosts(postsResult.value.posts)
                mutableUiState.update {
                    it.copy(
                        relation = relationValue,
                        posts = posts,
                        hasMorePosts = postsResult.value.hasMore,
                        isPostsLoading = false,
                        isRefreshing = false,
                        postsLoadError = null,
                        loadMoreError = null,
                    )
                }
                registerViews(posts)
                resolveEmojiUrls()
            }

            is AuthResult.Failure -> {
                mutableUiState.update {
                    it.copy(
                        relation = relationValue,
                        isPostsLoading = false,
                        isRefreshing = false,
                        postsLoadError = postsResult.error.toProfileLoadError(),
                        posts = if (keepContent) it.posts else emptyList(),
                        hasMorePosts = if (keepContent) it.hasMorePosts else false,
                    )
                }
            }
        }

        subscribePresence()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { session ->
                val authorized = session is SessionState.Authorized
                val username = (session as? SessionState.Authorized)?.user?.username
                ownUsername = username
                isAuthenticated = authorized

                val profileUsername = mutableUiState.value.profile?.username
                val self = authorized && profileUsername != null && username != null &&
                    username.equals(profileUsername, ignoreCase = true)

                mutableUiState.update {
                    it.copy(isAuthenticated = authorized, isSelf = self)
                }

                if (authorized) {
                    if (!self && profileUsername != null && mutableUiState.value.relation == null) {
                        loadRelation(profileUsername)
                    }
                    subscribePresence()
                }
            }
        }
    }

    private fun observeRealtime() {
        viewModelScope.launch {
            realtimeSocket.isConnected.collect { connected ->
                if (connected) subscribePresence()
            }
        }
        viewModelScope.launch {
            realtimeSocket.events.collect { event ->
                when (event.type) {
                    PresenceEventType -> parsePresenceEvent(event.payload)?.let(::applyPresence)
                    FollowersEventType -> parseFollowersEvent(event.payload)?.let(::applyFollowers)
                }
            }
        }
    }

    private fun applyPresence(event: ru.fet1sov.akinorev.data.realtime.PresenceEvent) {
        mutableUiState.update { state ->
            if (!state.username.equals(event.username, ignoreCase = true)) return@update state
            state.copy(
                isOnline = event.online,
                presenceDevice = if (event.online) event.device else "",
                lastSeenMillis = event.lastSeenMillis ?: state.lastSeenMillis,
                lastSeenDevice = event.lastSeenDevice.ifBlank { state.lastSeenDevice },
            )
        }
    }

    private fun applyFollowers(event: ru.fet1sov.akinorev.data.realtime.FollowersEvent) {
        mutableUiState.update { state ->
            if (!state.username.equals(event.username, ignoreCase = true)) return@update state
            state.copy(
                profile = state.profile?.copy(followersCount = event.count.coerceAtLeast(0)) ?: state.profile,
            )
        }
    }

    private fun loadRelation(username: String) {
        viewModelScope.launch {
            when (val result = communityRepository.fetchRelation(username)) {
                is AuthResult.Success -> {
                    val relation = result.value
                    val blocked = relation.isBlocked || relation.blockedMe
                    mutableUiState.update {
                        it.copy(
                            relation = relation,
                            isSelf = relation.isSelf || it.isSelf,
                            posts = if (blocked) emptyList() else it.posts,
                            hasMorePosts = if (blocked) false else it.hasMorePosts,
                            isOnline = if (blocked) false else it.isOnline,
                            presenceDevice = if (blocked) "" else it.presenceDevice,
                        )
                    }
                    if (blocked) unsubscribePresence(username)
                }

                is AuthResult.Failure -> Unit
            }
        }
    }

    private fun subscribePresence() {
        val state = mutableUiState.value
        val username = state.profile?.username ?: currentUsername
        if (!state.isAuthenticated || username.isBlank() || state.isBlocked) return

        realtimeSocket.send(watchPresenceMessage(listOf(username)))
        realtimeSocket.send(watchFollowersMessage(listOf(username)))
    }

    private fun unsubscribePresence(username: String) {
        if (username.isBlank()) return
        realtimeSocket.send(unwatchPresenceMessage(listOf(username)))
        realtimeSocket.send(unwatchFollowersMessage(listOf(username)))
    }

    private fun resolveEmojiUrls() {
        val state = mutableUiState.value
        val names = buildList {
            addAll(collectEmojiNames(state.profile?.bio.orEmpty()))
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

    private fun profileUsername(): String =
        mutableUiState.value.profile?.username ?: currentUsername

    private fun isOwnProfile(username: String?): Boolean {
        val own = ownUsername ?: return false
        return username != null && own.equals(username, ignoreCase = true)
    }

    private fun updatePost(postId: String, transform: (CommunityPost) -> CommunityPost) {
        mutableUiState.update { state ->
            state.copy(posts = state.posts.map { if (it.id == postId) transform(it) else it })
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

    private fun failEditing(error: ProfileEditError) {
        mutableUiState.update {
            it.copy(editing = it.editing.copy(isSaving = false, error = error))
        }
    }

    override fun onCleared() {
        val username = mutableUiState.value.profile?.username ?: currentUsername
        unsubscribePresence(username)
        super.onCleared()
    }

    companion object {
        private const val BioMaxLength = 30
        private const val PostMaxLength = 4000
        private const val ReportCommentMaxLength = 500
        private const val DefaultNameColor = "#ffffff"
        private const val PresenceEventType = "presence"
        private const val FollowersEventType = "followers_update"
        private val UsernameRegex = Regex("^[A-Za-z0-9]{3,24}$")
        private val HexColorRegex = Regex("^#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6})$")
        private val NameAnimations = setOf("", "gradient", "rainbow")

        fun factory(username: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                ProfileViewModel(
                    initialUsername = username,
                    communityRepository = application.container.communityRepository,
                    authRepository = application.container.authRepository,
                    emojisRepository = application.container.emojisRepository,
                    realtimeSocket = application.container.realtimeSocket,
                )
            }
        }
    }
}

private fun emptyContent(): Map<String, String> = PostLocales.associateWith { "" }

private fun sortPosts(posts: List<CommunityPost>): List<CommunityPost> = posts.sortedWith(
    compareByDescending<CommunityPost> { it.isPinned }
        .thenByDescending { it.createdAtMillis ?: 0L },
)

private sealed interface ImageUpload

private data class ImageUploadSuccess(val images: List<PostImage>) : ImageUpload

private data class ImageUploadFailure(val error: PostActionError) : ImageUpload
