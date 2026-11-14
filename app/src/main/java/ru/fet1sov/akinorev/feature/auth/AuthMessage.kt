package ru.fet1sov.akinorev.feature.auth

import ru.fet1sov.akinorev.data.remote.AuthError

enum class AuthMessage {
    EmptyFields,
    InvalidUsername,
    ShortPassword,
    PasswordMismatch,
    InvalidEmail,
    InvalidCredentials,
    UserExists,
    Banned,
    RateLimited,
    BadRequest,
    ServerError,
    Network,
    Unknown,
}

fun AuthError.toAuthMessage(): AuthMessage = when (this) {
    AuthError.Network -> AuthMessage.Network
    AuthError.Unexpected -> AuthMessage.Unknown
    is AuthError.Server -> when (code) {
        "invalid_credentials" -> AuthMessage.InvalidCredentials
        "user_exists" -> AuthMessage.UserExists
        "user_banned", "banned" -> AuthMessage.Banned
        "rate_limited" -> AuthMessage.RateLimited
        "bad_request", "invalid_username", "weak_password" -> AuthMessage.BadRequest
        "server_error" -> AuthMessage.ServerError
        "unauthorized", "invalid_refresh", "forbidden" -> AuthMessage.Unknown
        else -> when {
            status == 400 -> AuthMessage.BadRequest
            status == 409 -> AuthMessage.UserExists
            status == 423 -> AuthMessage.Banned
            status == 429 -> AuthMessage.RateLimited
            status in 500..599 -> AuthMessage.ServerError
            else -> AuthMessage.Unknown
        }
    }
}
