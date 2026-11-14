package ru.fet1sov.akinorev.data.auth

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import retrofit2.Response
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.AcinorevJson
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.ApiEnvelope
import ru.fet1sov.akinorev.data.remote.dto.ApiErrorDto
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.RefreshRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateLocaleRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateProfileRequest
import ru.fet1sov.akinorev.data.remote.isUnauthorized
import ru.fet1sov.akinorev.data.remote.isUserGone
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.KeyCustody
import java.io.IOException

class DefaultAuthRepository(
    private val api: AcinorevApi,
    private val sessionManager: SessionManager,
    private val json: Json = AcinorevJson.instance,
) : AuthRepository {
    private val refreshMutex = Mutex()

    override val session: StateFlow<SessionState> = sessionManager.state

    override suspend fun restoreSession() {
        val state = sessionManager.restore()
        if (state !is SessionState.Authorized) return

        when (val result = fetchProfile()) {
            is AuthResult.Success -> Unit
            is AuthResult.Failure -> if (result.error.isUnauthorized() || result.error.isUserGone()) {
                sessionManager.clear()
            }
        }
    }

    override suspend fun login(username: String, password: String): AuthResult<AuthUser> =
        when (val result = execute(retryOnUnauthorized = false) {
            api.login(LoginRequest(username = username, password = password))
        }) {
            is AuthResult.Success -> persistSession(result.value)
            is AuthResult.Failure -> result
        }

    override suspend fun register(username: String, email: String, password: String): AuthResult<AuthUser> =
        when (val result = execute(retryOnUnauthorized = false) {
            api.register(RegisterRequest(username = username, email = email, password = password))
        }) {
            is AuthResult.Success -> persistSession(result.value)
            is AuthResult.Failure -> result
        }

    override suspend fun refreshTokens(): AuthResult<Unit> = refreshMutex.withLock {
        val refreshToken = sessionManager.refreshToken()
            ?: return@withLock AuthResult.Failure(AuthError.Server(Unauthorized, 401))

        when (val result = execute(retryOnUnauthorized = false) {
            api.refresh(RefreshRequest(refreshToken))
        }) {
            is AuthResult.Success -> {
                val tokens = result.value
                if (tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) {
                    sessionManager.clear()
                    AuthResult.Failure(AuthError.Unexpected)
                } else {
                    sessionManager.saveTokens(tokens.asDomain())
                    AuthResult.Success(Unit)
                }
            }

            is AuthResult.Failure -> {
                if (result.error.isUnauthorized()) sessionManager.clear()
                result
            }
        }
    }

    override suspend fun fetchProfile(): AuthResult<AuthUser> =
        when (val result = execute { api.getProfile() }) {
            is AuthResult.Success -> {
                val user = result.value.user?.asDomain()
                if (user == null) {
                    AuthResult.Failure(AuthError.Unexpected)
                } else {
                    sessionManager.saveUser(user)
                    AuthResult.Success(user)
                }
            }

            is AuthResult.Failure -> result
        }

    override suspend fun updateLocale(locale: String): AuthResult<Unit> =
        when (val result = execute { api.updateLocale(UpdateLocaleRequest(locale)) }) {
            is AuthResult.Success -> {
                val current = sessionManager.cachedUser()
                val resolved = result.value.locale.ifBlank { locale }
                if (current != null) sessionManager.saveUser(current.copy(locale = resolved))
                AuthResult.Success(Unit)
            }

            is AuthResult.Failure -> result
        }

    override suspend fun updateProfile(
        bio: String?,
        username: String?,
        avatar: String?,
        dhPubKey: String?,
        nameColor: String?,
        nameAnimation: String?,
    ): AuthResult<AuthUser> =
        when (val result = execute {
            api.updateProfile(
                UpdateProfileRequest(
                    bio = bio,
                    avatar = avatar,
                    username = username,
                    dhPubKey = dhPubKey,
                    nameColor = nameColor,
                    nameAnimation = nameAnimation,
                ),
            )
        }) {
            is AuthResult.Success -> {
                val user = result.value.user?.asDomain()
                if (user == null) {
                    AuthResult.Failure(AuthError.Unexpected)
                } else {
                    sessionManager.saveUser(user)
                    AuthResult.Success(user)
                }
            }

            is AuthResult.Failure -> result
        }

    override suspend fun fetchKeyCustody(): AuthResult<KeyCustody> =
        when (val result = execute { api.getKeyCustody() }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain(deposited = true))
            is AuthResult.Failure -> result
        }

    override suspend fun depositKeyCustody(privateKey: String, dhPubKey: String): AuthResult<KeyCustody> =
        when (val result = execute {
            api.depositKeyCustody(DepositKeyRequest(privateKey = privateKey, dhPubKey = dhPubKey))
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain(deposited = true))
            is AuthResult.Failure -> result
        }

    override suspend fun logout() {
        sessionManager.clear()
    }

    private suspend fun persistSession(payload: AuthPayloadDto): AuthResult<AuthUser> {
        val user = payload.user?.asDomain()
        val tokens = payload.tokens
        if (user == null || tokens == null || tokens.accessToken.isBlank() || tokens.refreshToken.isBlank()) {
            return AuthResult.Failure(AuthError.Unexpected)
        }
        sessionManager.save(user = user, tokens = tokens.asDomain())
        return AuthResult.Success(user)
    }

    private suspend fun <T> execute(
        retryOnUnauthorized: Boolean = true,
        call: suspend () -> Response<ApiEnvelope<T>>,
    ): AuthResult<T> {
        val response = try {
            call()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: IOException) {
            return AuthResult.Failure(AuthError.Network)
        } catch (unexpected: Exception) {
            return AuthResult.Failure(AuthError.Unexpected)
        }

        if (response.isSuccessful) {
            val data = response.body()?.data
            return if (data == null) {
                AuthResult.Failure(AuthError.Unexpected)
            } else {
                AuthResult.Success(data)
            }
        }

        if (response.code() == 401 && retryOnUnauthorized && sessionManager.refreshToken() != null) {
            if (refreshTokens() is AuthResult.Success) {
                return execute(retryOnUnauthorized = false, call = call)
            }
        }

        return AuthResult.Failure(errorFrom(response))
    }

    private fun errorFrom(response: Response<*>): AuthError {
        val code = response.errorBody()?.string()?.let { body ->
            runCatching {
                json.decodeFromString(ApiEnvelope.serializer(ApiErrorDto.serializer()), body).data?.code
            }.getOrNull()
        }.orEmpty()

        val resolved = code.ifBlank { fallbackCode(response.code()) }
        return AuthError.Server(resolved, response.code())
    }

    private fun fallbackCode(status: Int): String = when (status) {
        400 -> "bad_request"
        401 -> Unauthorized
        403 -> "forbidden"
        404 -> "not_found"
        409 -> "user_exists"
        423 -> "user_banned"
        429 -> "rate_limited"
        in 500..599 -> "server_error"
        else -> "unknown"
    }

    private fun KeyCustodyDto.toDomain(deposited: Boolean): KeyCustody =
        KeyCustody(
            deposited = this.deposited ?: deposited,
            privateKey = privateKey,
            dhPubKey = dhPubKey,
        )

    private companion object {
        const val Unauthorized = "unauthorized"
    }
}
