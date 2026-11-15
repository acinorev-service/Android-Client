package ru.fet1sov.akinorev.feature.community

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
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.community.CommunityFeedPageSize
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CommunityFeedMode
import ru.fet1sov.akinorev.model.PostLikeResult
import ru.fet1sov.akinorev.model.PostsPage
import ru.fet1sov.akinorev.testing.FakeAuthRepository
import ru.fet1sov.akinorev.testing.FakeCommunityRepository
import ru.fet1sov.akinorev.testing.FakeEmojisRepository
import ru.fet1sov.akinorev.testing.authorizedSession
import ru.fet1sov.akinorev.testing.authorizedUser
import ru.fet1sov.akinorev.testing.fakeCommunityPost

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val community = FakeCommunityRepository()
    private val auth = FakeAuthRepository(authorizedSession(authorizedUser()))
    private val emojis = FakeEmojisRepository()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private fun viewModel() = CommunityViewModel(
        communityRepository = community,
        authRepository = auth,
        emojisRepository = emojis,
        languageTagProvider = { "ru" },
    )

    @Test
    fun initialLoad_requestsPopularFeed() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = true),
        )
        community.viewResult = AuthResult.Success(1)

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CommunityFeedMode.Popular, state.mode)
        assertEquals(1, state.posts.size)
        assertTrue(state.hasMore)
        assertFalse(state.isLoading)
        assertEquals(
            Triple(CommunityFeedMode.Popular, CommunityFeedPageSize, 0),
            community.feedRequests.first(),
        )
    }

    @Test
    fun selectMode_reloadsWithNewSort() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = false),
        )
        community.viewResult = AuthResult.Success(1)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectMode(CommunityFeedMode.New)
        advanceUntilIdle()

        assertEquals(CommunityFeedMode.New, viewModel.uiState.value.mode)
        assertEquals(
            Triple(CommunityFeedMode.New, CommunityFeedPageSize, 0),
            community.feedRequests.last(),
        )
    }

    @Test
    fun followingGuest_isLockedWithoutRequest() = runTest(mainDispatcher) {
        val guestAuth = FakeAuthRepository(SessionState.Guest)
        val viewModel = CommunityViewModel(
            communityRepository = community,
            authRepository = guestAuth,
            emojisRepository = emojis,
            languageTagProvider = { "ru" },
        )
        advanceUntilIdle()

        viewModel.selectMode(CommunityFeedMode.Following)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFollowingLocked)
        assertTrue(state.posts.isEmpty())
        assertTrue(community.feedRequests.none { it.first == CommunityFeedMode.Following })
    }

    @Test
    fun followingAuthorized_requestsFilteredFeed() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = false),
        )
        community.viewResult = AuthResult.Success(1)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectMode(CommunityFeedMode.Following)
        advanceUntilIdle()

        assertEquals(
            Triple(CommunityFeedMode.Following, CommunityFeedPageSize, 0),
            community.feedRequests.last(),
        )
        assertFalse(viewModel.uiState.value.isFollowingLocked)
    }

    @Test
    fun loadMore_appendsNextPage() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(
                posts = (0 until CommunityFeedPageSize).map { index -> fakeCommunityPost(id = "post-$index") },
                hasMore = true,
            ),
        )
        community.feedResults[CommunityFeedPageSize] = AuthResult.Success(
            PostsPage(
                posts = (CommunityFeedPageSize until CommunityFeedPageSize * 2).map { index ->
                    fakeCommunityPost(id = "post-$index")
                },
                hasMore = false,
            ),
        )
        community.viewResult = AuthResult.Success(1)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(CommunityFeedPageSize * 2, state.posts.size)
        assertFalse(state.hasMore)
        assertEquals(
            Triple(CommunityFeedMode.Popular, CommunityFeedPageSize, CommunityFeedPageSize),
            community.feedRequests.last(),
        )
    }

    @Test
    fun toggleLike_updatesTargetCounts() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(
                posts = listOf(fakeCommunityPost(id = "post-1", likesCount = 2, likedByMe = false)),
                hasMore = false,
            ),
        )
        community.viewResult = AuthResult.Success(1)
        community.likeResult = AuthResult.Success(
            PostLikeResult(postId = "post-1", likesCount = 3, likedByMe = true),
        )

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.togglePostLike("post-1")
        advanceUntilIdle()

        val post = viewModel.uiState.value.posts.single()
        assertTrue(post.likedByMe)
        assertEquals(3, post.likesCount)
        assertEquals(1, community.likeRequests)
    }

    @Test
    fun report_marksPostAsReported() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost(id = "post-1")), hasMore = false),
        )
        community.viewResult = AuthResult.Success(1)
        community.reportResult = AuthResult.Success(Unit)

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.openReport("post-1")
        viewModel.onReportReasonChange("spam")
        viewModel.submitReport()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("post-1" in state.reportedPostIds)
        assertFalse(state.report.isOpen)
    }

    @Test
    fun loadError_keepsEmptyFeedAndAllowsRetry() = runTest(mainDispatcher) {
        community.feedResults[0] = AuthResult.Failure(AuthError.Network)

        val viewModel = viewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loadError)
        assertTrue(viewModel.uiState.value.posts.isEmpty())

        community.feedResults[0] = AuthResult.Success(
            PostsPage(posts = listOf(fakeCommunityPost()), hasMore = false),
        )
        viewModel.retry()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loadError)
        assertEquals(1, viewModel.uiState.value.posts.size)
    }
}
