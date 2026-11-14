package ru.fet1sov.akinorev.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.TokenPair

class SessionManager(
    private val storage: SessionStorage,
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<SessionState>(SessionState.Loading)

    @Volatile
    private var current: StoredSession? = null

    val state: StateFlow<SessionState> = mutableState.asStateFlow()

    suspend fun restore(): SessionState = mutex.withLock {
        val existing = mutableState.value
        if (existing != SessionState.Loading) return existing

        val stored = storage.read()
        current = stored
        val restored = if (stored != null) {
            SessionState.Authorized(stored.user.asDomain())
        } else {
            SessionState.Guest
        }
        mutableState.value = restored
        return restored
    }

    suspend fun save(user: AuthUser, tokens: TokenPair) = mutex.withLock {
        val session = StoredSession(tokens = tokens.asStored(), user = user.asStored())
        current = session
        storage.write(session)
        mutableState.value = SessionState.Authorized(user)
    }

    suspend fun saveUser(user: AuthUser) = mutex.withLock {
        val session = current ?: return@withLock
        val updated = session.copy(user = user.asStored())
        current = updated
        storage.write(updated)
        mutableState.value = SessionState.Authorized(user)
    }

    suspend fun saveTokens(tokens: TokenPair) = mutex.withLock {
        val session = current ?: return@withLock
        val updated = session.copy(tokens = tokens.asStored())
        current = updated
        storage.write(updated)
    }

    suspend fun clear() = mutex.withLock {
        current = null
        storage.clear()
        mutableState.value = SessionState.Guest
    }

    fun accessToken(): String? = current?.tokens?.accessToken?.takeIf { it.isNotBlank() }

    fun refreshToken(): String? = current?.tokens?.refreshToken?.takeIf { it.isNotBlank() }

    fun cachedUser(): AuthUser? = current?.user?.asDomain()
}

private fun AuthUser.asStored(): StoredUser =
    StoredUser(
        id = id,
        username = username,
        email = email,
        bio = bio,
        wallet = wallet,
        avatar = avatar,
        dhPubKey = dhPubKey,
        isSubscribed = isSubscribed,
        subscriptionExpiresAt = subscriptionExpiresAt,
        nameColor = nameColor,
        nameAnimation = nameAnimation,
        locale = locale,
        createdAt = createdAt,
    )
