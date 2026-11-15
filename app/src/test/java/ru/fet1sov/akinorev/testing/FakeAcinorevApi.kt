package ru.fet1sov.akinorev.testing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.BlockPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogAuthorDto
import ru.fet1sov.akinorev.data.remote.dto.CommentLikePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CreateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.CreatePostRequest
import ru.fet1sov.akinorev.data.remote.dto.FollowPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostCommentPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostCommentsPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostLikePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostRepostPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostViewPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PostsPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ProfileRelationDto
import ru.fet1sov.akinorev.data.remote.dto.PublicUserPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ReportPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ReportPostRequest
import ru.fet1sov.akinorev.data.remote.dto.RepostRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateCommentRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdatePostRequest
import ru.fet1sov.akinorev.data.remote.dto.CatalogDeveloperDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGameDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGamesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGenreDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogGenresPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.CatalogPlatformDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiDeletedDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiListDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiUpdateRequest
import ru.fet1sov.akinorev.data.remote.dto.GamePlayersPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto

import ru.fet1sov.akinorev.data.remote.dto.LibraryGameDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryGamesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryGenreDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryTotalsDto
import ru.fet1sov.akinorev.data.remote.dto.LocaleDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadDto
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.data.remote.dto.NotificationActorDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationListDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationPreferencesDto
import ru.fet1sov.akinorev.data.remote.dto.NotificationPreferencesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PlayStatePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PushDeviceResponse
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.SearchCategoryDto
import ru.fet1sov.akinorev.data.remote.dto.SearchItemDto
import ru.fet1sov.akinorev.data.remote.dto.SearchPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.TokenPairDto
import ru.fet1sov.akinorev.data.remote.dto.UnregisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateLocaleRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateNotificationPreferencesRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateProfileRequest
import ru.fet1sov.akinorev.data.remote.dto.UserDto
import ru.fet1sov.akinorev.testing.ApiFixtures.error

class FakeAcinorevApi : AcinorevApi {
    var onRegister: suspend (RegisterRequest) -> Response<ApiEnvelope<AuthPayloadDto>> = { error(404) }
    var onLogin: suspend (LoginRequest) -> Response<ApiEnvelope<AuthPayloadDto>> = { error(404) }
    var onRefresh: suspend (RefreshRequest) -> Response<ApiEnvelope<TokenPairDto>> = { error(404) }
    var onGetKeyCustody: suspend () -> Response<ApiEnvelope<KeyCustodyDto>> = { error(404) }
    var onDepositKeyCustody: suspend (DepositKeyRequest) -> Response<ApiEnvelope<KeyCustodyDto>> = { error(404) }
    var onGetProfile: suspend () -> Response<ApiEnvelope<ProfilePayloadDto>> = { error(404) }
    var onGetUser: suspend (String) -> Response<ApiEnvelope<PublicUserPayloadDto>> = { error(404) }
    var onGetEmojiByName: suspend (String) -> Response<ApiEnvelope<EmojiPayloadDto>> = { error(404) }
    var onUpdateProfile: suspend (UpdateProfileRequest) -> Response<ApiEnvelope<ProfilePayloadDto>> = { error(404) }
    var onUpdateLocale: suspend (UpdateLocaleRequest) -> Response<ApiEnvelope<LocaleDto>> = { error(404) }
    var onGetNotificationPreferences: suspend () -> Response<ApiEnvelope<NotificationPreferencesPayloadDto>> =
        { error(404) }
    var onUpdateNotificationPreferences: suspend (
        UpdateNotificationPreferencesRequest,
    ) -> Response<ApiEnvelope<NotificationPreferencesPayloadDto>> = { error(404) }
    var onGetLibraryGames: suspend () -> Response<ApiEnvelope<LibraryGamesPayloadDto>> = { error(404) }
    var onGetNotifications: suspend (Int, Int) -> Response<ApiEnvelope<NotificationListDto>> =
        { _, _ -> error(404) }
    var onMarkNotificationsRead: suspend (
        MarkNotificationsReadRequest,
    ) -> Response<ApiEnvelope<MarkNotificationsReadDto>> = { error(404) }
    var onRegisterPushDevice: suspend (
        RegisterPushDeviceRequest,
    ) -> Response<ApiEnvelope<PushDeviceResponse>> = { error(404) }
    var onUnregisterPushDevice: suspend (
        UnregisterPushDeviceRequest,
    ) -> Response<ApiEnvelope<PushDeviceResponse>> = { error(404) }
    var onGetEmojis: suspend (Int, Int) -> Response<ApiEnvelope<EmojiListDto>> = { _, _ -> error(404) }
    var onCreateEmoji: suspend (EmojiRegisterRequest) -> Response<ApiEnvelope<EmojiPayloadDto>> = { error(404) }
    var onUpdateEmoji: suspend (String, EmojiUpdateRequest) -> Response<ApiEnvelope<EmojiPayloadDto>> =
        { _, _ -> error(404) }
    var onDeleteEmoji: suspend (String) -> Response<ApiEnvelope<EmojiDeletedDto>> = { error(404) }
    var onGetPosts: suspend (String, Int, Int) -> Response<ApiEnvelope<PostsPayloadDto>> =
        { _, _, _ -> error(404) }
    var onCreatePost: suspend (String, CreatePostRequest) -> Response<ApiEnvelope<PostPayloadDto>> =
        { _, _ -> error(404) }
    var onUpdatePost: suspend (String, String, UpdatePostRequest) -> Response<ApiEnvelope<PostPayloadDto>> =
        { _, _, _ -> error(404) }
    var onDeletePost: suspend (String, String) -> Response<Unit> = { _, _ -> error(404) }
    var onPinPost: suspend (String, String) -> Response<ApiEnvelope<PostPayloadDto>> =
        { _, _ -> error(404) }
    var onUnpinPost: suspend (String, String) -> Response<ApiEnvelope<PostPayloadDto>> =
        { _, _ -> error(404) }
    var onLikePost: suspend (String, String) -> Response<ApiEnvelope<PostLikePayloadDto>> =
        { _, _ -> error(404) }
    var onUnlikePost: suspend (String, String) -> Response<ApiEnvelope<PostLikePayloadDto>> =
        { _, _ -> error(404) }
    var onRepostPost: suspend (String, String, RepostRequest) -> Response<ApiEnvelope<PostRepostPayloadDto>> =
        { _, _, _ -> error(404) }
    var onUnrepostPost: suspend (String, String) -> Response<ApiEnvelope<PostRepostPayloadDto>> =
        { _, _ -> error(404) }
    var onViewPost: suspend (String, String) -> Response<ApiEnvelope<PostViewPayloadDto>> =
        { _, _ -> error(404) }
    var onReportPost: suspend (String, String, ReportPostRequest) -> Response<ApiEnvelope<ReportPayloadDto>> =
        { _, _, _ -> error(404) }
    var onGetPostComments: suspend (String, String, Int, Int) -> Response<ApiEnvelope<PostCommentsPayloadDto>> =
        { _, _, _, _ -> error(404) }
    var onCreatePostComment: suspend (
        String,
        String,
        CreateCommentRequest,
    ) -> Response<ApiEnvelope<PostCommentPayloadDto>> = { _, _, _ -> error(404) }
    var onUpdatePostComment: suspend (
        String,
        String,
        String,
        UpdateCommentRequest,
    ) -> Response<ApiEnvelope<PostCommentPayloadDto>> = { _, _, _, _ -> error(404) }
    var onDeletePostComment: suspend (String, String, String) -> Response<Unit> = { _, _, _ -> error(404) }
    var onLikePostComment: suspend (
        String,
        String,
        String,
    ) -> Response<ApiEnvelope<CommentLikePayloadDto>> = { _, _, _ -> error(404) }
    var onUnlikePostComment: suspend (
        String,
        String,
        String,
    ) -> Response<ApiEnvelope<CommentLikePayloadDto>> = { _, _, _ -> error(404) }
    var onGetRelation: suspend (String) -> Response<ApiEnvelope<ProfileRelationDto>> = { error(404) }
    var onFollowUser: suspend (String) -> Response<ApiEnvelope<FollowPayloadDto>> = { error(404) }
    var onUnfollowUser: suspend (String) -> Response<ApiEnvelope<FollowPayloadDto>> = { error(404) }
    var onBlockUser: suspend (String) -> Response<ApiEnvelope<BlockPayloadDto>> = { error(404) }
    var onUnblockUser: suspend (String) -> Response<ApiEnvelope<BlockPayloadDto>> = { error(404) }

    var onGetGames: suspend (Int, Int, String?, String?, String?) -> Response<ApiEnvelope<CatalogGamesPayloadDto>> =
        { _, _, _, _, _ -> error(404) }
    var onGetGame: suspend (String) -> Response<ApiEnvelope<CatalogGamePayloadDto>> = { error(404) }
    var onGetGenres: suspend () -> Response<ApiEnvelope<CatalogGenresPayloadDto>> = { error(404) }
    var onSearch: suspend (String, Int) -> Response<ApiEnvelope<SearchPayloadDto>> = { _, _ -> error(404) }
    var onGetGamePlayers: suspend (String) -> Response<ApiEnvelope<GamePlayersPayloadDto>> = { error(404) }
    var onStartPlay: suspend (String) -> Response<ApiEnvelope<PlayStatePayloadDto>> = { error(404) }
    var onEndPlay: suspend (String) -> Response<ApiEnvelope<PlayStatePayloadDto>> = { error(404) }
    var onHeartbeatPlay: suspend (String) -> Response<ApiEnvelope<PlayStatePayloadDto>> = { error(404) }

    var refreshCalls: Int = 0
        private set

    override suspend fun register(body: RegisterRequest): Response<ApiEnvelope<AuthPayloadDto>> =
        onRegister(body)

    override suspend fun login(body: LoginRequest): Response<ApiEnvelope<AuthPayloadDto>> =
        onLogin(body)

    override suspend fun refresh(body: RefreshRequest): Response<ApiEnvelope<TokenPairDto>> {
        refreshCalls += 1
        return onRefresh(body)
    }

    override suspend fun getKeyCustody(): Response<ApiEnvelope<KeyCustodyDto>> = onGetKeyCustody()

    override suspend fun depositKeyCustody(body: DepositKeyRequest): Response<ApiEnvelope<KeyCustodyDto>> =
        onDepositKeyCustody(body)

    override suspend fun getProfile(): Response<ApiEnvelope<ProfilePayloadDto>> = onGetProfile()

    override suspend fun getUser(username: String): Response<ApiEnvelope<PublicUserPayloadDto>> =
        onGetUser(username)

    override suspend fun getEmojiByName(name: String): Response<ApiEnvelope<EmojiPayloadDto>> =
        onGetEmojiByName(name)

    override suspend fun updateProfile(body: UpdateProfileRequest): Response<ApiEnvelope<ProfilePayloadDto>> =
        onUpdateProfile(body)

    override suspend fun updateLocale(body: UpdateLocaleRequest): Response<ApiEnvelope<LocaleDto>> =
        onUpdateLocale(body)

    override suspend fun getNotificationPreferences(): Response<ApiEnvelope<NotificationPreferencesPayloadDto>> =
        onGetNotificationPreferences()

    override suspend fun updateNotificationPreferences(
        body: UpdateNotificationPreferencesRequest,
    ): Response<ApiEnvelope<NotificationPreferencesPayloadDto>> = onUpdateNotificationPreferences(body)

    override suspend fun getLibraryGames(): Response<ApiEnvelope<LibraryGamesPayloadDto>> =
        onGetLibraryGames()

    override suspend fun getNotifications(
        limit: Int,
        offset: Int,
    ): Response<ApiEnvelope<NotificationListDto>> = onGetNotifications(limit, offset)

    override suspend fun markNotificationsRead(
        body: MarkNotificationsReadRequest,
    ): Response<ApiEnvelope<MarkNotificationsReadDto>> = onMarkNotificationsRead(body)

    override suspend fun registerPushDevice(
        body: RegisterPushDeviceRequest,
    ): Response<ApiEnvelope<PushDeviceResponse>> = onRegisterPushDevice(body)

    override suspend fun unregisterPushDevice(
        body: UnregisterPushDeviceRequest,
    ): Response<ApiEnvelope<PushDeviceResponse>> = onUnregisterPushDevice(body)

    override suspend fun getEmojis(
        page: Int,
        limit: Int,
    ): Response<ApiEnvelope<EmojiListDto>> = onGetEmojis(page, limit)

    override suspend fun createEmoji(body: EmojiRegisterRequest): Response<ApiEnvelope<EmojiPayloadDto>> =
        onCreateEmoji(body)

    override suspend fun updateEmoji(
        id: String,
        body: EmojiUpdateRequest,
    ): Response<ApiEnvelope<EmojiPayloadDto>> = onUpdateEmoji(id, body)

    override suspend fun deleteEmoji(id: String): Response<ApiEnvelope<EmojiDeletedDto>> = onDeleteEmoji(id)

    override suspend fun getPosts(
        username: String,
        limit: Int,
        offset: Int,
    ): Response<ApiEnvelope<PostsPayloadDto>> = onGetPosts(username, limit, offset)

    override suspend fun createPost(
        username: String,
        body: CreatePostRequest,
    ): Response<ApiEnvelope<PostPayloadDto>> = onCreatePost(username, body)

    override suspend fun updatePost(
        username: String,
        id: String,
        body: UpdatePostRequest,
    ): Response<ApiEnvelope<PostPayloadDto>> = onUpdatePost(username, id, body)

    override suspend fun deletePost(username: String, id: String): Response<Unit> =
        onDeletePost(username, id)

    override suspend fun pinPost(username: String, id: String): Response<ApiEnvelope<PostPayloadDto>> =
        onPinPost(username, id)

    override suspend fun unpinPost(username: String, id: String): Response<ApiEnvelope<PostPayloadDto>> =
        onUnpinPost(username, id)

    override suspend fun likePost(username: String, id: String): Response<ApiEnvelope<PostLikePayloadDto>> =
        onLikePost(username, id)

    override suspend fun unlikePost(username: String, id: String): Response<ApiEnvelope<PostLikePayloadDto>> =
        onUnlikePost(username, id)

    override suspend fun repostPost(
        username: String,
        id: String,
        body: RepostRequest,
    ): Response<ApiEnvelope<PostRepostPayloadDto>> = onRepostPost(username, id, body)

    override suspend fun unrepostPost(
        username: String,
        id: String,
    ): Response<ApiEnvelope<PostRepostPayloadDto>> = onUnrepostPost(username, id)

    override suspend fun viewPost(username: String, id: String): Response<ApiEnvelope<PostViewPayloadDto>> =
        onViewPost(username, id)

    override suspend fun reportPost(
        username: String,
        id: String,
        body: ReportPostRequest,
    ): Response<ApiEnvelope<ReportPayloadDto>> = onReportPost(username, id, body)

    override suspend fun getPostComments(
        username: String,
        id: String,
        limit: Int,
        offset: Int,
    ): Response<ApiEnvelope<PostCommentsPayloadDto>> = onGetPostComments(username, id, limit, offset)

    override suspend fun createPostComment(
        username: String,
        id: String,
        body: CreateCommentRequest,
    ): Response<ApiEnvelope<PostCommentPayloadDto>> = onCreatePostComment(username, id, body)

    override suspend fun updatePostComment(
        username: String,
        id: String,
        commentId: String,
        body: UpdateCommentRequest,
    ): Response<ApiEnvelope<PostCommentPayloadDto>> =
        onUpdatePostComment(username, id, commentId, body)

    override suspend fun deletePostComment(
        username: String,
        id: String,
        commentId: String,
    ): Response<Unit> = onDeletePostComment(username, id, commentId)

    override suspend fun likePostComment(
        username: String,
        id: String,
        commentId: String,
    ): Response<ApiEnvelope<CommentLikePayloadDto>> = onLikePostComment(username, id, commentId)

    override suspend fun unlikePostComment(
        username: String,
        id: String,
        commentId: String,
    ): Response<ApiEnvelope<CommentLikePayloadDto>> = onUnlikePostComment(username, id, commentId)

    override suspend fun getRelation(username: String): Response<ApiEnvelope<ProfileRelationDto>> =
        onGetRelation(username)

    override suspend fun followUser(username: String): Response<ApiEnvelope<FollowPayloadDto>> =
        onFollowUser(username)

    override suspend fun unfollowUser(username: String): Response<ApiEnvelope<FollowPayloadDto>> =
        onUnfollowUser(username)

    override suspend fun blockUser(username: String): Response<ApiEnvelope<BlockPayloadDto>> =
        onBlockUser(username)

    override suspend fun unblockUser(username: String): Response<ApiEnvelope<BlockPayloadDto>> =
        onUnblockUser(username)

    override suspend fun getGames(
        limit: Int,
        offset: Int,
        query: String?,
        genre: String?,
        platform: String?,
    ): Response<ApiEnvelope<CatalogGamesPayloadDto>> = onGetGames(limit, offset, query, genre, platform)

    override suspend fun getGame(id: String): Response<ApiEnvelope<CatalogGamePayloadDto>> = onGetGame(id)

    override suspend fun getGenres(): Response<ApiEnvelope<CatalogGenresPayloadDto>> = onGetGenres()

    override suspend fun search(query: String, limit: Int): Response<ApiEnvelope<SearchPayloadDto>> =
        onSearch(query, limit)

    override suspend fun getGamePlayers(id: String): Response<ApiEnvelope<GamePlayersPayloadDto>> =
        onGetGamePlayers(id)

    override suspend fun startPlay(id: String): Response<ApiEnvelope<PlayStatePayloadDto>> = onStartPlay(id)

    override suspend fun endPlay(id: String): Response<ApiEnvelope<PlayStatePayloadDto>> = onEndPlay(id)

    override suspend fun heartbeatPlay(id: String): Response<ApiEnvelope<PlayStatePayloadDto>> = onHeartbeatPlay(id)
}

object ApiFixtures {
    private val jsonMediaType = "application/json".toMediaType()

    val user = UserDto(
        id = "user-1",
        username = "fet1sov",
        email = "fet1sov@example.com",
        locale = "ru",
    )

    val tokens = TokenPairDto(
        accessToken = "access-1",
        refreshToken = "refresh-1",
        tokenType = "Bearer",
        expiresIn = 900,
    )

    fun <T> error(status: Int, code: String = "unknown"): Response<T> =
        Response.error(
            status,
            """{"data":{"code":"$code","message":"error"}}""".toResponseBody(jsonMediaType),
        )

    fun <T> success(data: T): Response<ApiEnvelope<T>> = Response.success(ApiEnvelope(data = data))

    fun notificationPreferences(
        follow: Boolean = true,
        block: Boolean = true,
        post: Boolean = true,
        like: Boolean = true,
        broadcast: Boolean = true,
    ): NotificationPreferencesPayloadDto = NotificationPreferencesPayloadDto(
        preferences = NotificationPreferencesDto(
            follow = follow,
            block = block,
            post = post,
            like = like,
            broadcast = broadcast,
        ),
    )

    fun notification(
        id: String = "notification-1",
        type: String = "follow",
        actor: NotificationActorDto? = NotificationActorDto(id = user.id, username = user.username),
        read: Boolean = false,
        createdAt: String = "2026-11-14T10:00:00Z",
        title: Map<String, String>? = null,
        body: Map<String, String>? = null,
    ): NotificationDto = NotificationDto(
        id = id,
        type = type,
        actorId = actor?.id.orEmpty(),
        actor = actor,
        read = read,
        createdAt = createdAt,
        title = title,
        body = body,
    )

    fun notificationList(
        items: List<NotificationDto> = listOf(notification()),
        hasMore: Boolean = false,
        unread: Int = 0,
    ): NotificationListDto = NotificationListDto(
        notifications = items,
        hasMore = hasMore,
        unread = unread,
    )

    fun libraryGame(
        id: String = "game-1",
        title: String = "Neon Runner",
        icon: String? = "http://localhost:8090/f/icon.png",
        genreSlug: String = "arcade",
        secondsPlayed: Long = 3_600,
        sessions: Long = 2,
        lastPlayedAt: String? = "2026-11-10T12:00:00Z",
    ): LibraryGameDto = LibraryGameDto(
        id = id,
        title = title,
        icon = icon,
        genre = LibraryGenreDto(slug = genreSlug, title = "genres.$genreSlug"),
        secondsPlayed = secondsPlayed,
        sessions = sessions,
        lastPlayedAt = lastPlayedAt,
    )

    fun libraryPayload(
        games: List<LibraryGameDto> = listOf(libraryGame()),
        totalGames: Int = games.size,
        secondsPlayed: Long = games.sumOf { it.secondsPlayed },
    ): LibraryGamesPayloadDto = LibraryGamesPayloadDto(
        games = games,
        totals = LibraryTotalsDto(games = totalGames, secondsPlayed = secondsPlayed),
    )

    fun emoji(
        id: String = "emoji-1",
        name: String = "pepe",
        url: String = "http://localhost:8090/f/emoji-1.webp",
        mine: Boolean = false,
        ownerUsername: String? = user.username,
        createdAt: String = "2026-11-10T12:00:00Z",
    ): EmojiDto = EmojiDto(
        id = id,
        ownerId = user.id,
        ownerUsername = ownerUsername,
        name = name,
        url = url,
        mime = "image/webp",
        size = 1024,
        createdAt = createdAt,
        mine = mine,
    )

    fun emojiList(
        items: List<EmojiDto> = listOf(emoji()),
        total: Int = items.size,
        page: Int = 1,
        limit: Int = 24,
        totalPages: Int = ((total + limit - 1) / limit).coerceAtLeast(1),
    ): EmojiListDto = EmojiListDto(
        emojis = items,
        total = total,
        page = page,
        limit = limit,
        totalPages = totalPages,
    )

    fun catalogGame(
        id: String = "game-1",
        title: String = "Neon Runner",
        description: String = "A neon runner through the city",
        banner: String? = "http://localhost:8090/f/banner.png",
        icon: String? = "http://localhost:8090/f/icon.png",
        genreSlug: String = "arcade",
        developerName: String = "Acinorev Studio",
        authorUsername: String = user.username,
        platforms: List<String> = listOf("mobile", "pc"),
        publishedAt: String? = "2026-11-10T12:00:00Z",
    ): CatalogGameDto = CatalogGameDto(
        id = id,
        title = title,
        description = description,
        banner = banner,
        icon = icon,
        status = "published",
        publishedAt = publishedAt,
        createdAt = publishedAt,
        genre = CatalogGenreDto(
            id = "genre-$genreSlug",
            slug = genreSlug,
            name = genreSlug,
            title = "genres.$genreSlug",
        ),
        developer = CatalogDeveloperDto(id = "developer-1", name = developerName),
        author = CatalogAuthorDto(id = user.id, username = authorUsername),
        platforms = platforms.map { slug ->
            CatalogPlatformDto(id = "platform-$slug", slug = slug, name = slug, title = "platforms.$slug")
        },
    )

    fun catalogPayload(
        games: List<CatalogGameDto> = listOf(catalogGame()),
        total: Int = games.size,
    ): CatalogGamesPayloadDto = CatalogGamesPayloadDto(
        games = games,
        total = total,
        limit = games.size,
        offset = 0,
    )

    fun catalogGenres(
        genres: List<CatalogGenreDto> = listOf(
            CatalogGenreDto(id = "genre-arcade", slug = "arcade", name = "arcade", title = "genres.arcade"),
            CatalogGenreDto(id = "genre-rpg", slug = "rpg", name = "rpg", title = "genres.rpg"),
        ),
    ): CatalogGenresPayloadDto = CatalogGenresPayloadDto(genres = genres)

    fun gamePlayers(
        gameId: String = "game-1",
        count: Int = 3,
    ): GamePlayersPayloadDto = GamePlayersPayloadDto(gameId = gameId, count = count)

    fun playState(
        gameId: String = "game-1",
        playing: Boolean = true,
    ): PlayStatePayloadDto = PlayStatePayloadDto(gameId = gameId, playing = playing, ok = true)

    fun searchGameItem(
        id: String = "game-1",
        title: String = "Neon Runner",
        genreSlug: String = "arcade",
    ): SearchItemDto = SearchItemDto(
        id = id,
        title = title,
        description = "A neon runner",
        banner = "http://localhost:8090/f/banner.png",
        icon = "http://localhost:8090/f/icon.png",
        genre = CatalogGenreDto(slug = genreSlug, title = "genres.$genreSlug"),
        platforms = listOf(CatalogPlatformDto(slug = "mobile", title = "platforms.mobile")),
        category = "games",
        score = 90,
    )

    fun searchUserItem(
        id: String = user.id,
        username: String = user.username,
        bio: String = "Runner",
    ): SearchItemDto = SearchItemDto(
        id = id,
        username = username,
        avatar = "http://localhost:8090/f/avatar.png",
        bio = bio,
        category = "users",
        score = 70,
    )

    fun searchDeveloperItem(
        id: String = "developer-1",
        name: String = "Acinorev Studio",
        handle: String = "acinorev",
    ): SearchItemDto = SearchItemDto(
        id = id,
        name = name,
        handle = handle,
        avatar = "http://localhost:8090/f/dev.png",
        category = "developers",
        score = 80,
    )

    fun searchPayload(
        query: String = "neon",
        games: List<SearchItemDto> = listOf(searchGameItem()),
        developers: List<SearchItemDto> = listOf(searchDeveloperItem()),
        users: List<SearchItemDto> = listOf(searchUserItem()),
    ): SearchPayloadDto = SearchPayloadDto(
        query = query,
        categories = listOf(
            SearchCategoryDto(category = "games", items = games),
            SearchCategoryDto(category = "developers", items = developers),
            SearchCategoryDto(category = "users", items = users),
        ).filter { it.items.isNotEmpty() },
        total = games.size + developers.size + users.size,
        limit = 20,
        offset = 0,
    )
}
