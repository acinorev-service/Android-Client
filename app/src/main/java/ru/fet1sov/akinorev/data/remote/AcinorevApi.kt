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
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiDeletedDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiListDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.EmojiRegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.EmojiUpdateRequest
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto
import ru.fet1sov.akinorev.data.remote.dto.LibraryGamesPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.LocaleDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadDto
import ru.fet1sov.akinorev.data.remote.dto.MarkNotificationsReadRequest
import ru.fet1sov.akinorev.data.remote.dto.NotificationListDto
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
