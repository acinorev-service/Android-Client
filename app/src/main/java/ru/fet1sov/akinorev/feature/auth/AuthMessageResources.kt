package ru.fet1sov.akinorev.feature.auth

import androidx.annotation.StringRes
import ru.fet1sov.akinorev.R

val AuthMessage.textRes: Int
    @StringRes get() = when (this) {
        AuthMessage.EmptyFields -> R.string.auth_error_empty_fields
        AuthMessage.InvalidUsername -> R.string.auth_error_invalid_username
        AuthMessage.ShortPassword -> R.string.auth_error_short_password
        AuthMessage.PasswordMismatch -> R.string.auth_error_password_mismatch
        AuthMessage.InvalidEmail -> R.string.auth_error_invalid_email
        AuthMessage.InvalidCredentials -> R.string.auth_error_invalid_credentials
        AuthMessage.UserExists -> R.string.auth_error_user_exists
        AuthMessage.Banned -> R.string.auth_error_banned
        AuthMessage.RateLimited -> R.string.auth_error_rate_limited
        AuthMessage.BadRequest -> R.string.auth_error_bad_request
        AuthMessage.ServerError -> R.string.auth_error_server
        AuthMessage.Network -> R.string.auth_error_network
        AuthMessage.Unknown -> R.string.auth_error_unknown
    }
