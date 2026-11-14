package ru.fet1sov.akinorev.data.auth

import kotlinx.coroutines.flow.StateFlow
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.KeyCustody

interface AuthRepository {
    val session: StateFlow<SessionState>

    suspend fun restoreSession()

    suspend fun login(username: String, password: String): AuthResult<AuthUser>

    suspend fun register(username: String, email: String, password: String): AuthResult<AuthUser>

    suspend fun refreshTokens(): AuthResult<Unit>

    suspend fun fetchProfile(): AuthResult<AuthUser>

    suspend fun updateLocale(locale: String): AuthResult<Unit>

    suspend fun updateProfile(
        bio: String? = null,
        username: String? = null,
        avatar: String? = null,
        dhPubKey: String? = null,
        nameColor: String? = null,
        nameAnimation: String? = null,
    ): AuthResult<AuthUser>

    suspend fun fetchKeyCustody(): AuthResult<KeyCustody>

    suspend fun depositKeyCustody(privateKey: String, dhPubKey: String): AuthResult<KeyCustody>

    suspend fun logout()
}
