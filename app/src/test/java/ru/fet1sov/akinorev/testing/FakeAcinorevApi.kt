package ru.fet1sov.akinorev.testing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiDeletedDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiListDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiUpdateRequest
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
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.PushDeviceResponse
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterPushDeviceRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
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
}
