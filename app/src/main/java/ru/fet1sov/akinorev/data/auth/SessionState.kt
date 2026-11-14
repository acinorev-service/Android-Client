package ru.fet1sov.akinorev.data.auth

import ru.fet1sov.akinorev.model.AuthUser
import ru.fet1sov.akinorev.model.TokenPair

sealed interface SessionState {
    data object Loading : SessionState
    data object Guest : SessionState
    data class Authorized(val user: AuthUser) : SessionState
}

internal fun TokenPair.asStored(): StoredTokens =
    StoredTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
    )

internal fun StoredTokens.asDomain(): TokenPair =
    TokenPair(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
    )
