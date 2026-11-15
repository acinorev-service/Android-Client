package ru.fet1sov.akinorev.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.remote.AuthError
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.KeyCustody

class FakeAuthRepository(
    initial: SessionState = SessionState.Guest,
) : AuthRepository {
    private val mutableSession = MutableStateFlow(initial)
    override val session: StateFlow<SessionState> = mutableSession.asStateFlow()

    var updateProfileCalls: Int = 0
        private set
    var lastUpdateUsername: String? = null
    var lastUpdateBio: String? = null
    var lastUpdateAvatar: String? = null
    var lastUpdateNameColor: String? = null
    var lastUpdateNameAnimation: String? = null
    var updateProfileResult: AuthResult<AuthUser>? = null

    override suspend fun restoreSession() = Unit

    override suspend fun login(username: String, password: String): AuthResult<AuthUser> =
        AuthResult.Failure(AuthError.Unexpected)

    override suspend fun register(username: String, email: String, password: String): AuthResult<AuthUser> =
        AuthResult.Failure(AuthError.Unexpected)

    override suspend fun refreshTokens(): AuthResult<Unit> = AuthResult.Failure(AuthError.Unexpected)

    override suspend fun fetchProfile(): AuthResult<AuthUser> = AuthResult.Failure(AuthError.Unexpected)

    override suspend fun updateLocale(locale: String): AuthResult<Unit> =
        AuthResult.Failure(AuthError.Unexpected)

    override suspend fun updateProfile(
        bio: String?,
        username: String?,
        avatar: String?,
        dhPubKey: String?,
        nameColor: String?,
        nameAnimation: String?,
    ): AuthResult<AuthUser> {
        updateProfileCalls += 1
        lastUpdateUsername = username
        lastUpdateBio = bio
        lastUpdateAvatar = avatar
        lastUpdateNameColor = nameColor
        lastUpdateNameAnimation = nameAnimation

        updateProfileResult?.let { return it }

        val current = (mutableSession.value as? SessionState.Authorized)?.user
            ?: return AuthResult.Failure(AuthError.Unexpected)
        val updated = current.copy(
            username = username ?: current.username,
            bio = bio ?: current.bio,
            avatar = avatar ?: current.avatar,
            nameColor = nameColor ?: current.nameColor,
            nameAnimation = nameAnimation ?: current.nameAnimation,
        )
        mutableSession.value = SessionState.Authorized(updated)
        return AuthResult.Success(updated)
    }

    override suspend fun fetchKeyCustody(): AuthResult<KeyCustody> =
        AuthResult.Failure(AuthError.Unexpected)

    override suspend fun depositKeyCustody(privateKey: String, dhPubKey: String): AuthResult<KeyCustody> =
        AuthResult.Failure(AuthError.Unexpected)

    override suspend fun logout() {
        mutableSession.value = SessionState.Guest
    }
}

fun authorizedUser(
    id: String = "user-1",
    username: String = "fet1sov",
    bio: String = "",
    avatar: String = "",
    isSubscribed: Boolean = false,
    nameColor: String = "",
    nameAnimation: String = "",
): AuthUser = AuthUser(
    id = id,
    username = username,
    email = "$username@example.com",
    bio = bio,
    wallet = 0.0,
    avatar = avatar,
    dhPubKey = "",
    isSubscribed = isSubscribed,
    subscriptionExpiresAt = null,
    nameColor = nameColor,
    nameAnimation = nameAnimation,
    locale = "ru",
    createdAt = "2026-01-01T00:00:00Z",
)

fun authorizedSession(user: AuthUser = authorizedUser()): SessionState =
    SessionState.Authorized(user)
