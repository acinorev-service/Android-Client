package ru.fet1sov.akinorev.testing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import ru.fet1sov.akinorev.data.remote.AcinorevApi
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
}
