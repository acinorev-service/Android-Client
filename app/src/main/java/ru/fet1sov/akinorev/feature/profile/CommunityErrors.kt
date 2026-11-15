package ru.fet1sov.akinorev.feature.profile

import ru.fet1sov.akinorev.data.remote.AuthError

internal fun AuthError.toProfileLoadError(): ProfileLoadError = when (this) {
    AuthError.Network -> ProfileLoadError.Network
    AuthError.Unexpected -> ProfileLoadError.Unknown
    is AuthError.Server -> when {
        status == 423 || code == "user_banned" -> ProfileLoadError.Banned
        status == 404 || code == "user_not_found" -> ProfileLoadError.NotFound
        status == 403 -> ProfileLoadError.Forbidden
        status == 429 -> ProfileLoadError.RateLimited
        status >= 500 -> ProfileLoadError.Server
        else -> ProfileLoadError.Unknown
    }
}

internal fun AuthError.toProfileActionError(): ProfileActionError = when (this) {
    AuthError.Network -> ProfileActionError.Network
    AuthError.Unexpected -> ProfileActionError.Generic
    is AuthError.Server -> when {
        status == 401 -> ProfileActionError.Unauthorized
        status == 403 -> ProfileActionError.Forbidden
        status == 404 -> ProfileActionError.NotFound
        status == 429 -> ProfileActionError.RateLimited
        status == 423 -> ProfileActionError.Banned
        else -> ProfileActionError.Generic
    }
}

internal fun AuthError.toProfileEditError(): ProfileEditError = when (this) {
    AuthError.Network -> ProfileEditError.Network
    AuthError.Unexpected -> ProfileEditError.Generic
    is AuthError.Server -> when {
        status == 401 -> ProfileEditError.Unauthorized
        status == 409 || code == "user_exists" -> ProfileEditError.UsernameTaken
        status == 429 || code == "username_change_limited" -> ProfileEditError.UsernameLimited
        status == 403 || code == "subscription_required" -> ProfileEditError.SubscriptionRequired
        status == 400 -> ProfileEditError.InvalidValue
        else -> ProfileEditError.Generic
    }
}

internal fun AuthError.toPostActionError(): PostActionError = when (this) {
    AuthError.Network -> PostActionError.Network
    AuthError.Unexpected -> PostActionError.Generic
    is AuthError.Server -> when {
        status == 400 -> PostActionError.BadRequest
        status == 401 -> PostActionError.Unauthorized
        status == 403 -> PostActionError.Forbidden
        status == 404 -> PostActionError.NotFound
        status == 429 -> PostActionError.RateLimited
        status == 423 -> PostActionError.Banned
        else -> PostActionError.Generic
    }
}
