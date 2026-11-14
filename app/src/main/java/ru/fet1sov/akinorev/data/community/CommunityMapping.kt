package ru.fet1sov.akinorev.data.community

import ru.fet1sov.akinorev.data.remote.dto.EquippedBadgeDto
import ru.fet1sov.akinorev.data.remote.dto.PostAuthorDto
import ru.fet1sov.akinorev.data.remote.dto.PostCommentDto
import ru.fet1sov.akinorev.data.remote.dto.PostDto
import ru.fet1sov.akinorev.data.remote.dto.PostImageDto
import ru.fet1sov.akinorev.data.remote.dto.ProfileRelationDto
import ru.fet1sov.akinorev.data.remote.dto.UserDto
import ru.fet1sov.akinorev.model.CommunityPost
import ru.fet1sov.akinorev.model.CommunityProfile
import ru.fet1sov.akinorev.model.PostAuthor
import ru.fet1sov.akinorev.model.PostComment
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.ProfileBadge
import ru.fet1sov.akinorev.model.ProfileRelation
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

internal fun UserDto.toCommunityProfile(): CommunityProfile = CommunityProfile(
    id = id,
    username = username,
    bio = bio,
    avatarPath = avatar,
    createdAtMillis = parseCommunityTimestamp(createdAt),
    lastSeenMillis = parseCommunityTimestamp(lastSeen),
    lastSeenDevice = lastSeenDevice.orEmpty(),
    followersCount = followersCount,
    followingCount = followingCount,
    isBlocked = isBlocked,
    isSubscribed = isSubscribed,
    nameColor = nameColor,
    nameAnimation = nameAnimation,
    badges = equippedBadges.orEmpty().mapNotNull(EquippedBadgeDto::toDomain),
)

private fun EquippedBadgeDto.toDomain(): ProfileBadge? {
    val id = userItemId.takeIf { it.isNotBlank() } ?: itemId.takeIf { it.isNotBlank() } ?: return null
    return ProfileBadge(
        userItemId = id,
        itemId = itemId,
        order = equippedOrder,
        name = item?.name.orEmpty(),
        icon = item?.icon.orEmpty(),
        rarity = item?.rarity.orEmpty(),
    )
}

internal fun ProfileRelationDto.toDomain(): ProfileRelation = ProfileRelation(
    isSelf = isSelf,
    isFollowing = isFollowing,
    isBlocked = isBlocked,
    blockedMe = blockedMe,
)

internal fun PostDto.toDomain(): CommunityPost? {
    val postId = id.takeIf { it.isNotBlank() } ?: return null
    return CommunityPost(
        id = postId,
        author = author.toDomainAuthor(),
        content = content.orEmpty(),
        images = images.orEmpty().mapNotNull(PostImageDto::toDomain),
        views = views,
        likesCount = likesCount,
        likedByMe = likedByMe,
        commentsCount = commentsCount,
        commentsPreview = commentsPreview.orEmpty().mapNotNull(PostCommentDto::toDomain),
        repostsCount = repostsCount,
        repostedByMe = repostedByMe,
        isRepost = isRepost,
        repostOf = repostOf?.toDomain(),
        repostContent = repostContent.orEmpty(),
        repostImages = repostImages.orEmpty().mapNotNull(PostImageDto::toDomain),
        isPinned = isPinned,
        createdAtMillis = parseCommunityTimestamp(createdAt),
        updatedAtMillis = parseCommunityTimestamp(updatedAt),
    )
}

internal fun PostCommentDto.toDomain(): PostComment? {
    val commentId = id.takeIf { it.isNotBlank() } ?: return null
    return PostComment(
        id = commentId,
        postId = postId,
        parentId = parentId,
        depth = depth,
        content = content,
        author = author?.toDomainAuthor(),
        canDelete = canDelete,
        canEdit = canEdit,
        isEdited = isEdited,
        likesCount = likesCount,
        likedByMe = likedByMe,
        createdAtMillis = parseCommunityTimestamp(createdAt),
        updatedAtMillis = parseCommunityTimestamp(updatedAt),
        children = children.mapNotNull(PostCommentDto::toDomain),
    )
}

internal fun PostAuthorDto?.toDomainAuthor(): PostAuthor = PostAuthor(
    id = this?.id.orEmpty(),
    username = this?.username.orEmpty(),
    avatarPath = this?.avatar.orEmpty(),
    isSubscribed = this?.isSubscribed ?: false,
    nameColor = this?.nameColor.orEmpty(),
    nameAnimation = this?.nameAnimation.orEmpty(),
)

internal fun PostImageDto.toDomain(): PostImage? {
    val url = url.takeIf { it.isNotBlank() } ?: return null
    return PostImage(url = url, mime = mime.orEmpty(), size = size)
}

internal fun PostImage.toDto(): PostImageDto = PostImageDto(
    url = url,
    mime = mime.ifBlank { null },
    size = size,
)

internal fun parseCommunityTimestamp(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(value).toInstant().toEpochMilli() }.getOrNull()
        ?: runCatching { Instant.parse(value).toEpochMilli() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() }
            .getOrNull()
}
