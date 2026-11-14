package ru.fet1sov.akinorev.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto
import ru.fet1sov.akinorev.data.remote.dto.LocaleDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.TokenPairDto
import ru.fet1sov.akinorev.data.remote.dto.UpdateLocaleRequest
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
}
