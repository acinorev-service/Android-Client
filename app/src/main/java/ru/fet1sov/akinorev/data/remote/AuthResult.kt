package ru.fet1sov.akinorev.data.remote

sealed interface AuthError {
    data class Server(val code: String, val status: Int) : AuthError
    data object Network : AuthError
    data object Unexpected : AuthError
}

sealed interface AuthResult<out T> {
    data class Success<T>(val value: T) : AuthResult<T>
    data class Failure(val error: AuthError) : AuthResult<Nothing>
}

internal fun AuthError.isUnauthorized(): Boolean =
    this is AuthError.Server && (status == 401 || code == "unauthorized" || code == "invalid_refresh")

internal fun AuthError.isUserGone(): Boolean =
    this is AuthError.Server && code == "user_not_found"
