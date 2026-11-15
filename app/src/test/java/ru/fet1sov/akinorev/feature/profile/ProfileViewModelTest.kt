package ru.fet1sov.akinorev.feature.profile

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.community.PostsPageSize
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.FollowResult
import ru.fet1sov.akinorev.model.PostLikeResult
import ru.fet1sov.akinorev.model.PostsPage
import ru.fet1sov.akinorev.model.ProfileRelation
import ru.fet1sov.akinorev.testing.FakeAuthRepository
import ru.fet1sov.akinorev.testing.FakeCommunityRepository
import ru.fet1sov.akinorev.testing.FakeEmojisRepository
import ru.fet1sov.akinorev.testing.FakeRealtimeSocket
import ru.fet1sov.akinorev.testing.authorizedSession
import ru.fet1sov.akinorev.testing.authorizedUser
import ru.fet1sov.akinorev.testing.fakeCommunityPost
import ru.fet1sov.akinorev.testing.fakeCommunityProfile

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val community = FakeCommunityRepository()
    private val auth = FakeAuthRepository(authorizedSession(authorizedUser()))
    private val emojis = FakeEmojisRepository()
    private val realtime = FakeRealtimeSocket()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel(username: String = "fet1sov") = ProfileViewModel(
        initialUsername = username,
        communityRepository = community,
        authRepository = auth,
        emojisRepository = emojis,
        realtimeSocket = realtime,
        languageTagProvider = { "ru" },
    )

    @Test
    fun load_populatesSelfProfileAndPosts() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(fakeCommunityProfile(followersCount = 3))
        community.postsResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = false),
        )
        community.viewResult = AuthResult.Success(1)

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("fet1sov", state.profile?.username)
        assertEquals(1, state.posts.size)
        assertTrue(state.isSelf)
        assertEquals(3, state.profile?.followersCount)
        assertFalse(state.isLoading)
        assertFalse(state.isPostsLoading)
        assertEquals(0, community.relationRequests)
    }

    @Test
    fun load_bannedProfileShowsBannedState() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Failure(AuthError.Server("user_banned", 423))

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isBanned)
        assertEquals(ProfileLoadError.Banned, state.loadError)
        assertFalse(state.isLoading)
    }

    @Test
    fun follow_updatesRelationAndFollowers() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(
            fakeCommunityProfile(id = "user-2", username = "other", followersCount = 2),
        )
        community.relationResult = AuthResult.Success(
            ProfileRelation(isSelf = false, isFollowing = false, isBlocked = false, blockedMe = false),
        )
        community.followingResult = AuthResult.Success(FollowResult(isFollowing = true, followersCount = 7))

        val viewModel = viewModel("other")
        advanceUntilIdle()

        viewModel.toggleFollow()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.relation?.isFollowing == true)
        assertEquals(7, state.profile?.followersCount)
        assertEquals(1, community.followingRequests)
    }

    @Test
    fun loadMore_appendsNextPage() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(
            fakeCommunityProfile(id = "user-2", username = "other"),
        )
        community.postsResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost(id = "post-1")), hasMore = true),
        )
        community.postsResults[1] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost(id = "post-2")), hasMore = false),
        )

        val viewModel = viewModel("other")
        advanceUntilIdle()

        viewModel.loadMorePosts()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("post-1", "post-2"), state.posts.map { it.id })
        assertFalse(state.hasMorePosts)
        assertTrue(community.postsRequests.contains(PostsPageSize to 1))
    }

    @Test
    fun toggleLike_updatesTargetCounts() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(
            fakeCommunityProfile(id = "user-2", username = "other"),
        )
        community.postsResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost(likesCount = 1)), hasMore = false),
        )
        community.likeResult = AuthResult.Success(
            PostLikeResult(postId = "post-1", likesCount = 2, likedByMe = true),
        )

        val viewModel = viewModel("other")
        advanceUntilIdle()

        viewModel.togglePostLike("post-1")
        advanceUntilIdle()

        val target = viewModel.uiState.value.posts.first().target
        assertEquals(2, target.likesCount)
        assertTrue(target.likedByMe)
        assertEquals(true, community.lastLike)
    }

    @Test
    fun saveProfile_rejectsShortUsername() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(fakeCommunityProfile())

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.startEditingProfile()
        viewModel.onEditUsernameChange("ab")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(ProfileEditError.UsernameTooShort, viewModel.uiState.value.editing.error)
        assertEquals(0, auth.updateProfileCalls)
    }

    @Test
    fun saveProfile_updatesBioAndRefreshesProfile() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(fakeCommunityProfile(bio = "hello"))

        val viewModel = viewModel()
        advanceUntilIdle()

        community.profileResult = AuthResult.Success(fakeCommunityProfile(bio = "new bio"))

        viewModel.startEditingProfile()
        viewModel.onEditBioChange("new bio")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, auth.updateProfileCalls)
        assertEquals("new bio", auth.lastUpdateBio)
        assertEquals("new bio", viewModel.uiState.value.profile?.bio)
        assertFalse(viewModel.uiState.value.editing.isActive)
    }

    @Test
    fun toggleBlock_clearsPostsAndPresence() = runTest(mainDispatcher) {
        community.profileResult = AuthResult.Success(
            fakeCommunityProfile(id = "user-2", username = "other"),
        )
        community.postsResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = false),
        )
        community.relationResult = AuthResult.Success(
            ProfileRelation(isSelf = false, isFollowing = false, isBlocked = false, blockedMe = false),
        )
        community.blockedResult = AuthResult.Success(true)

        val viewModel = viewModel("other")
        advanceUntilIdle()

        viewModel.toggleBlock()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isBlocked)
        assertTrue(state.posts.isEmpty())
        assertFalse(state.isOnline)
    }
}
