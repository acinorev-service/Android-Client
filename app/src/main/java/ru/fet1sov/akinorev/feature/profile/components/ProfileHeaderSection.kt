package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.feature.profile.ProfileUiState
import ru.fet1sov.akinorev.ui.components.CommunityRichText
import ru.fet1sov.akinorev.ui.components.LocalImage
import ru.fet1sov.akinorev.ui.components.ProfileName
import ru.fet1sov.akinorev.ui.components.UserAvatar
import ru.fet1sov.akinorev.ui.format.rememberCompactCount
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun ProfileHeaderSection(
    state: ProfileUiState,
    onAvatarClick: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenEmojiPicker: () -> Unit,
    onEditUsernameChange: (String) -> Unit,
    onEditBioChange: (String) -> Unit,
    onEditNameColorChange: (String) -> Unit,
    onEditNameAnimationChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: return
    val editing = state.editing
    val palette = navigationPalette()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(156.dp)
                .clip(CircleShape)
                .clickable(enabled = editing.isActive, onClick = onAvatarClick),
        ) {
            if (editing.isActive && editing.avatarUri != null) {
                LocalImage(
                    uri = editing.avatarUri,
                    contentDescription = profile.username,
                    modifier = Modifier.size(156.dp),
                    shape = CircleShape,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            } else {
                UserAvatar(
                    avatar = profile.avatarPath,
                    userId = profile.id,
                    username = profile.username,
                    size = 156.dp,
                    isSubscribed = profile.isSubscribed,
                    shape = CircleShape,
                )
            }
        }

        if (editing.isActive) {
            Surface(
                onClick = onAvatarClick,
                shape = RoundedCornerShape(50),
                color = Color.Transparent,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.community_profile_edit_upload_avatar),
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (editing.isActive) {
            OutlinedTextField(
                value = editing.username,
                onValueChange = onEditUsernameChange,
                modifier = Modifier.width(250.dp),
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.community_profile_edit_username),
                        color = palette.textMuted,
                        fontSize = 14.sp,
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.textMuted,
                    unfocusedBorderColor = palette.barBorder,
                    focusedContainerColor = palette.bar,
                    unfocusedContainerColor = palette.bar,
                    cursorColor = palette.textPrimary,
                    focusedTextColor = palette.textPrimary,
                    unfocusedTextColor = palette.textPrimary,
                ),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ProfileName(
                    username = profile.username,
                    nameColor = if (profile.isSubscribed) profile.nameColor else "",
                    nameAnimation = if (profile.isSubscribed) profile.nameAnimation else "",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }

        if (editing.isActive) {
            Spacer(modifier = Modifier.height(10.dp))
            NameStyleEditor(
                username = editing.username,
                color = editing.nameColor,
                animation = editing.nameAnimation,
                subscribed = profile.isSubscribed,
                onColorChange = onEditNameColorChange,
                onAnimationChange = onEditNameAnimationChange,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            if (profile.badges.isNotEmpty()) {
                ProfileBadges(
                    badges = profile.badges,
                    modifier = Modifier.padding(top = 6.dp),
                    size = 26.dp,
                )
            }

            if (state.showPresence) {
                ProfilePresence(
                    isOnline = state.isOnline,
                    isSelf = state.isSelf,
                    isBlocked = state.isBlocked,
                    device = state.presenceDevice,
                    lastSeenDevice = state.lastSeenDevice,
                    lastSeenMillis = state.lastSeenMillis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        if (editing.isActive) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.community_profile_edit_bio),
                color = palette.textMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                OutlinedTextField(
                    value = editing.bio,
                    onValueChange = onEditBioChange,
                    modifier = Modifier.weight(1f),
                    minLines = 2,
                    maxLines = 3,
                    textStyle = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.community_profile_edit_bio),
                            color = palette.textMuted,
                            fontSize = 14.sp,
                        )
                    },
                    supportingText = {
                        Text(
                            text = stringResource(
                                R.string.community_profile_edit_bio_counter,
                                editing.bio.codePointCount(0, editing.bio.length),
                                BioMaxLength,
                            ),
                            color = palette.textMuted,
                            fontSize = 11.sp,
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.textMuted,
                        unfocusedBorderColor = palette.barBorder,
                        focusedContainerColor = palette.bar,
                        unfocusedContainerColor = palette.bar,
                        cursorColor = palette.textPrimary,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                    ),
                )

                Surface(
                    onClick = onOpenEmojiPicker,
                    shape = CircleShape,
                    color = palette.barBorder.copy(alpha = 0.25f),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppIcon(
                            kind = AppIconKind.Emojis,
                            tint = palette.textPrimary,
                            contentDescription = stringResource(R.string.emojis_title),
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        } else if (profile.bio.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            ) {
                CommunityRichText(
                    text = profile.bio,
                    emojiUrls = state.emojiUrls,
                    style = TextStyle(
                        color = palette.textPrimary,
                        fontSize = 20.sp,
                        lineHeight = 28.sp,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    emojiSize = 22.sp,
                )
            }
        }

        val editError = editing.error
        if (editing.isActive && editError != null) {
            Text(
                text = editErrorText(editError),
                color = EditErrorColor,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (editing.isActive) {
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onCancelEdit,
                    enabled = !editing.isSaving,
                    shape = RoundedCornerShape(50),
                    color = palette.bar,
                ) {
                    Text(
                        text = stringResource(R.string.community_profile_edit_cancel),
                        color = palette.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Surface(
                    onClick = onSaveProfile,
                    enabled = !editing.isSaving,
                    shape = RoundedCornerShape(50),
                    color = palette.idleContent,
                ) {
                    Text(
                        text = stringResource(
                            if (editing.isSaving) {
                                R.string.community_profile_edit_saving
                            } else {
                                R.string.community_profile_edit_save
                            },
                        ),
                        color = palette.bar,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    )
                }
            }
        } else if (state.isSelf) {
            Surface(
                onClick = onEditProfile,
                shape = CircleShape,
                color = Color.Transparent,
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        kind = AppIconKind.Appearance,
                        tint = palette.textPrimary,
                        contentDescription = stringResource(R.string.community_profile_edit),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileFollowers(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = rememberCompactCount(count),
            color = palette.textPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            lineHeight = 42.sp,
        )
        Text(
            text = stringResource(R.string.community_profile_followers).uppercase(),
            color = palette.textMuted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun editErrorText(error: ru.fet1sov.akinorev.feature.profile.ProfileEditError): String = when (error) {
    ru.fet1sov.akinorev.feature.profile.ProfileEditError.Network,
    ru.fet1sov.akinorev.feature.profile.ProfileEditError.Unauthorized,
    ru.fet1sov.akinorev.feature.profile.ProfileEditError.Generic,
    -> stringResource(R.string.community_profile_edit_error_generic)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.SubscriptionRequired,
    -> stringResource(R.string.community_profile_edit_error_subscription_required)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.UsernameTaken ->
        stringResource(R.string.community_profile_edit_error_username_taken)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.UsernameLimited ->
        stringResource(R.string.community_profile_edit_error_username_limited)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.BioTooLong ->
        stringResource(R.string.community_profile_edit_error_bio_too_long)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.UsernameTooShort ->
        stringResource(R.string.community_profile_edit_error_username_too_short)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.UsernameTooLong ->
        stringResource(R.string.community_profile_edit_error_username_too_long)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.InvalidValue ->
        stringResource(R.string.community_profile_edit_error_invalid_value)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.AvatarUnsupported ->
        stringResource(R.string.community_profile_edit_error_avatar_unsupported)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.AvatarTooLarge ->
        stringResource(R.string.community_profile_edit_error_avatar_too_large)

    ru.fet1sov.akinorev.feature.profile.ProfileEditError.AvatarUnreadable ->
        stringResource(R.string.community_profile_edit_error_avatar_unreadable)
}

private const val BioMaxLength = 30
private val EditErrorColor = Color(0xFFE5484D)
