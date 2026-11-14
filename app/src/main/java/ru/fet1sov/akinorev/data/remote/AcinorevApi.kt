package ru.fet1sov.akinorev.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGenresPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.BlockPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CommentLikePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CreateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.CreatePostRequest
import ru.fet1sov.akinorev.data.remote.dto.FollowPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.GamePlayersPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiDeletedDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiListDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiUpdateRequest
import ru.fet1sov.akinorev.data.remote.dto.PostCommentPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostCommentsPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostLikePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostRepostPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostViewPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostsPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ProfileRelationDto
import ru.fet1sov.akinorev.data.remote.dto.ReportPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ReportPostRequest
import ru.fet1sov.akinorev.data.remote.dto.RepostRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdatePostRequest
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryGamesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.LocaleDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadDto
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.data.remote.dto.NotificationListDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationPreferencesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PlayStatePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PublicUserPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PushDeviceResponse
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.SearchPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.TokenPairDto
import ru.fet1sov.akinorev.data.remote.dto.UnregisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateLocaleRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateNotificationPreferencesRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateProfileRequest

interface AcinorevApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiEnvelope<AuthPayloadDto>>

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<AuthPayloadDto>>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): Response<ApiEnvelope<TokenPairDto>>

    @GET("auth/keys")
    suspend fun getKeyCustody(): Response<ApiEnvelope<KeyCustodyDto>>

    @POST("auth/keys")
    suspend fun depositKeyCustody(@Body body: DepositKeyRequest): Response<ApiEnvelope<KeyCustodyDto>>

    @GET("user/profile")
    suspend fun getProfile(): Response<ApiEnvelope<ProfilePayloadDto>>

    @GET("user/{username}")
    suspend fun getUser(
        @Path("username") username: String,
    ): Response<ApiEnvelope<PublicUserPayloadDto>>

    @GET("user/{username}/posts")
    suspend fun getPosts(
        @Path("username") username: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<ApiEnvelope<PostsPayloadDto>>

    @POST("user/{username}/posts")
    suspend fun createPost(
        @Path("username") username: String,
        @Body body: CreatePostRequest,
    ): Response<ApiEnvelope<PostPayloadDto>>

    @PUT("user/{username}/posts/{id}")
    suspend fun updatePost(
        @Path("username") username: String,
        @Path("id") id: String,
        @Body body: UpdatePostRequest,
    ): Response<ApiEnvelope<PostPayloadDto>>

    @DELETE("user/{username}/posts/{id}")
    suspend fun deletePost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<Unit>

    @POST("user/{username}/posts/{id}/pin")
    suspend fun pinPost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostPayloadDto>>

    @DELETE("user/{username}/posts/{id}/pin")
    suspend fun unpinPost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostPayloadDto>>

    @POST("user/{username}/posts/{id}/like")
    suspend fun likePost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostLikePayloadDto>>

    @DELETE("user/{username}/posts/{id}/like")
    suspend fun unlikePost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostLikePayloadDto>>

    @POST("user/{username}/posts/{id}/repost")
    suspend fun repostPost(
        @Path("username") username: String,
        @Path("id") id: String,
        @Body body: RepostRequest,
    ): Response<ApiEnvelope<PostRepostPayloadDto>>

    @DELETE("user/{username}/posts/{id}/repost")
    suspend fun unrepostPost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostRepostPayloadDto>>

    @POST("user/{username}/posts/{id}/view")
    suspend fun viewPost(
        @Path("username") username: String,
        @Path("id") id: String,
    ): Response<ApiEnvelope<PostViewPayloadDto>>

    @POST("user/{username}/posts/{id}/report")
    suspend fun reportPost(
        @Path("username") username: String,
        @Path("id") id: String,
        @Body body: ReportPostRequest,
    ): Response<ApiEnvelope<ReportPayloadDto>>

    @GET("user/{username}/posts/{id}/comments")
    suspend fun getPostComments(
        @Path("username") username: String,
        @Path("id") id: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<ApiEnvelope<PostCommentsPayloadDto>>

    @POST("user/{username}/posts/{id}/comments")
    suspend fun createPostComment(
        @Path("username") username: String,
        @Path("id") id: String,
        @Body body: CreateCommentRequest,
    ): Response<ApiEnvelope<PostCommentPayloadDto>>

    @PUT("user/{username}/posts/{id}/comments/{commentId}")
    suspend fun updatePostComment(
        @Path("username") username: String,
        @Path("id") id: String,
        @Path("commentId") commentId: String,
        @Body body: UpdateCommentRequest,
    ): Response<ApiEnvelope<PostCommentPayloadDto>>

    @DELETE("user/{username}/posts/{id}/comments/{commentId}")
    suspend fun deletePostComment(
        @Path("username") username: String,
        @Path("id") id: String,
        @Path("commentId") commentId: String,
    ): Response<Unit>

    @POST("user/{username}/posts/{id}/comments/{commentId}/like")
    suspend fun likePostComment(
        @Path("username") username: String,
        @Path("id") id: String,
        @Path("commentId") commentId: String,
    ): Response<ApiEnvelope<CommentLikePayloadDto>>

    @DELETE("user/{username}/posts/{id}/comments/{commentId}/like")
    suspend fun unlikePostComment(
        @Path("username") username: String,
        @Path("id") id: String,
        @Path("commentId") commentId: String,
    ): Response<ApiEnvelope<CommentLikePayloadDto>>

    @GET("user/{username}/relation")
    suspend fun getRelation(
        @Path("username") username: String,
    ): Response<ApiEnvelope<ProfileRelationDto>>

    @POST("user/{username}/follow")
    suspend fun followUser(
        @Path("username") username: String,
    ): Response<ApiEnvelope<FollowPayloadDto>>

    @DELETE("user/{username}/follow")
    suspend fun unfollowUser(
        @Path("username") username: String,
    ): Response<ApiEnvelope<FollowPayloadDto>>

    @POST("user/{username}/block")
    suspend fun blockUser(
        @Path("username") username: String,
    ): Response<ApiEnvelope<BlockPayloadDto>>

    @DELETE("user/{username}/block")
    suspend fun unblockUser(
        @Path("username") username: String,
    ): Response<ApiEnvelope<BlockPayloadDto>>

    @PATCH("user/profile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<ApiEnvelope<ProfilePayloadDto>>

    @PUT("user/locale")
    suspend fun updateLocale(@Body body: UpdateLocaleRequest): Response<ApiEnvelope<LocaleDto>>

    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<ApiEnvelope<NotificationListDto>>

    @POST("notifications/read")
    suspend fun markNotificationsRead(
        @Body body: MarkNotificationsReadRequest,
    ): Response<ApiEnvelope<MarkNotificationsReadDto>>

    @POST("push/devices")
    suspend fun registerPushDevice(
        @Body body: RegisterPushDeviceRequest,
    ): Response<ApiEnvelope<PushDeviceResponse>>

    @DELETE("push/devices")
    suspend fun unregisterPushDevice(
        @Body body: UnregisterPushDeviceRequest,
    ): Response<ApiEnvelope<PushDeviceResponse>>

    @GET("library/games")
    suspend fun getLibraryGames(): Response<ApiEnvelope<LibraryGamesPayloadDto>>

    @GET("games")
    suspend fun getGames(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") query: String? = null,
        @Query("genre") genre: String? = null,
        @Query("platform") platform: String? = null,
    ): Response<ApiEnvelope<CatalogGamesPayloadDto>>

    @GET("games/{id}")
    suspend fun getGame(
        @Path("id") id: String,
    ): Response<ApiEnvelope<CatalogGamePayloadDto>>

    @GET("genres")
    suspend fun getGenres(): Response<ApiEnvelope<CatalogGenresPayloadDto>>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int,
    ): Response<ApiEnvelope<SearchPayloadDto>>

    @GET("play/{id}/players")
    suspend fun getGamePlayers(
        @Path("id") id: String,
    ): Response<ApiEnvelope<GamePlayersPayloadDto>>

    @POST("play/{id}/start")
    suspend fun startPlay(
        @Path("id") id: String,
    ): Response<ApiEnvelope<PlayStatePayloadDto>>

    @POST("play/{id}/end")
    suspend fun endPlay(
        @Path("id") id: String,
    ): Response<ApiEnvelope<PlayStatePayloadDto>>

    @POST("play/{id}/heartbeat")
    suspend fun heartbeatPlay(
        @Path("id") id: String,
    ): Response<ApiEnvelope<PlayStatePayloadDto>>

    @GET("notifications/preferences")
    suspend fun getNotificationPreferences(): Response<ApiEnvelope<NotificationPreferencesPayloadDto>>

    @PUT("notifications/preferences")
    suspend fun updateNotificationPreferences(
        @Body body: UpdateNotificationPreferencesRequest,
    ): Response<ApiEnvelope<NotificationPreferencesPayloadDto>>

    @GET("emojis")
    suspend fun getEmojis(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
    ): Response<ApiEnvelope<EmojiListDto>>

    @GET("emojis/name/{name}")
    suspend fun getEmojiByName(
        @Path("name") name: String,
    ): Response<ApiEnvelope<EmojiPayloadDto>>

    @POST("emojis")
    suspend fun createEmoji(
        @Body body: EmojiRegisterRequest,
    ): Response<ApiEnvelope<EmojiPayloadDto>>

    @PATCH("emojis/{id}")
    suspend fun updateEmoji(
        @Path("id") id: String,
        @Body body: EmojiUpdateRequest,
    ): Response<ApiEnvelope<EmojiPayloadDto>>

    @DELETE("emojis/{id}")
    suspend fun deleteEmoji(
        @Path("id") id: String,
    ): Response<ApiEnvelope<EmojiDeletedDto>>
}

