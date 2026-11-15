package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.profile.PostActionError
import ru.fet1sov.akinorev.feature.profile.ProfileActionError
import ru.fet1sov.akinorev.feature.profile.ProfileEditError
import ru.fet1sov.akinorev.feature.profile.ProfileLoadError

@Composable
fun profileLoadErrorText(error: ProfileLoadError): String = when (error) {
    ProfileLoadError.Network -> stringResource(R.string.community_error_network)
    ProfileLoadError.NotFound -> stringResource(R.string.community_profile_not_found_title)
    ProfileLoadError.Banned -> stringResource(R.string.community_error_banned)
    ProfileLoadError.Forbidden -> stringResource(R.string.community_error_forbidden)
    ProfileLoadError.RateLimited -> stringResource(R.string.community_error_rate_limited)
    ProfileLoadError.Server -> stringResource(R.string.community_error_server)
    ProfileLoadError.Unknown -> stringResource(R.string.community_error_generic)
}

@Composable
fun profileActionErrorText(error: ProfileActionError): String = when (error) {
    ProfileActionError.Network -> stringResource(R.string.community_error_network)
    ProfileActionError.Unauthorized -> stringResource(R.string.community_error_unauthorized)
    ProfileActionError.Forbidden -> stringResource(R.string.community_error_forbidden)
    ProfileActionError.NotFound -> stringResource(R.string.community_error_not_found)
    ProfileActionError.RateLimited -> stringResource(R.string.community_error_rate_limited)
    ProfileActionError.Banned -> stringResource(R.string.community_error_banned)
    ProfileActionError.Generic -> stringResource(R.string.community_error_generic)
}

@Composable
fun postActionErrorText(error: PostActionError): String = when (error) {
    PostActionError.Network -> stringResource(R.string.community_error_network)
    PostActionError.Unauthorized -> stringResource(R.string.community_error_unauthorized)
    PostActionError.Forbidden -> stringResource(R.string.community_error_forbidden)
    PostActionError.NotFound -> stringResource(R.string.community_error_not_found)
    PostActionError.RateLimited -> stringResource(R.string.community_error_rate_limited)
    PostActionError.Banned -> stringResource(R.string.community_error_banned)
    PostActionError.BadRequest -> stringResource(R.string.community_error_bad_request)
    PostActionError.ImageUnsupported -> stringResource(R.string.community_error_image_unsupported)
    PostActionError.ImageTooLarge -> stringResource(R.string.community_error_image_too_large)
    PostActionError.ImageUnreadable -> stringResource(R.string.community_error_image_unreadable)
    PostActionError.Generic -> stringResource(R.string.community_error_generic)
}

@Composable
fun profileEditErrorText(error: ProfileEditError): String = when (error) {
    ProfileEditError.Network -> stringResource(R.string.community_error_network)
    ProfileEditError.Unauthorized -> stringResource(R.string.community_error_unauthorized)
    ProfileEditError.UsernameTaken -> stringResource(R.string.community_profile_edit_error_username_taken)
    ProfileEditError.UsernameLimited -> stringResource(R.string.community_profile_edit_error_username_limited)
    ProfileEditError.SubscriptionRequired -> stringResource(R.string.community_profile_edit_error_subscription_required)
    ProfileEditError.BioTooLong -> stringResource(R.string.community_profile_edit_error_bio_too_long)
    ProfileEditError.UsernameTooShort -> stringResource(R.string.community_profile_edit_error_username_too_short)
    ProfileEditError.UsernameTooLong -> stringResource(R.string.community_profile_edit_error_username_too_long)
    ProfileEditError.InvalidValue -> stringResource(R.string.community_profile_edit_error_invalid_value)
    ProfileEditError.AvatarUnsupported -> stringResource(R.string.community_profile_edit_error_avatar_unsupported)
    ProfileEditError.AvatarTooLarge -> stringResource(R.string.community_profile_edit_error_avatar_too_large)
    ProfileEditError.AvatarUnreadable -> stringResource(R.string.community_profile_edit_error_avatar_unreadable)
    ProfileEditError.Generic -> stringResource(R.string.community_profile_edit_error_generic)
}
