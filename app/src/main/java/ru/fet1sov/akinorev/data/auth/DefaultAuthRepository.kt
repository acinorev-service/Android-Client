package ru.fet1sov.akinorev.data.auth

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import ru.fet1sov.akinorev.data.remote.AcinorevApi
import ru.fet1sov.akinorev.data.remote.AcinorevJson
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.remote.dto.AuthPayloadDto
import ru.fet1sov.akinorev.data.remote.dto.DepositKeyRequest
import ru.fet1sov.akinorev.data.remote.dto.KeyCustodyDto
import ru.fet1sov.akinorev.data.remote.dto.LoginRequest
import ru.fet1sov.akinorev.data.remote.dto.RegisterRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateLocaleRequest
import ru.fet1sov.akinorev.data.remote.dto.UpdateProfileRequest
import ru.fet1sov.akinorev.data.remote.isUnauthorized
import ru.fet1sov.akinorev.data.remote.isUserGone
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.KeyCustody

class DefaultAuthRepository(
    private val api: AcinorevApi,
    private val sessionManager: SessionManager,
    json: Json = AcinorevJson.instance,
    private val client: ApiClient = ApiClient(api, sessionManager, json),
    private val onLogout: suspend () -> Unit = {},
) : AuthRepository {
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
        when (val result = client.execute(retryOnUnauthorized = false) {
            api.login(LoginRequest(username = username, password = password))
        }) {
            is AuthResult.Success -> persistSession(result.value)
            is AuthResult.Failure -> result
        }

    override suspend fun register(username: String, email: String, password: String): AuthResult<AuthUser> =
        when (val result = client.execute(retryOnUnauthorized = false) {
            api.register(RegisterRequest(username = username, email = email, password = password))
        }) {
            is AuthResult.Success -> persistSession(result.value)
            is AuthResult.Failure -> result
        }

    override suspend fun refreshTokens(): AuthResult<Unit> = client.refreshTokens()

    override suspend fun fetchProfile(): AuthResult<AuthUser> =
        when (val result = client.execute { api.getProfile() }) {
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
        when (val result = client.execute { api.updateLocale(UpdateLocaleRequest(locale)) }) {
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
        when (val result = client.execute {
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
        when (val result = client.execute { api.getKeyCustody() }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain(deposited = true))
            is AuthResult.Failure -> result
        }

    override suspend fun depositKeyCustody(privateKey: String, dhPubKey: String): AuthResult<KeyCustody> =
        when (val result = client.execute {
            api.depositKeyCustody(DepositKeyRequest(privateKey = privateKey, dhPubKey = dhPubKey))
        }) {
            is AuthResult.Success -> AuthResult.Success(result.value.toDomain(deposited = true))
            is AuthResult.Failure -> result
        }

    override suspend fun logout() {
        runCatching { onLogout() }
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

    private fun KeyCustodyDto.toDomain(deposited: Boolean): KeyCustody =
        KeyCustody(
            deposited = this.deposited ?: deposited,
            privateKey = privateKey,
            dhPubKey = dhPubKey,
        )
}
