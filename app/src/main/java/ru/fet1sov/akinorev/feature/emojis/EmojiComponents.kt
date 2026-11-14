package ru.fet1sov.akinorev.feature.emojis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.SkeletonBlock
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.NavigationPalette
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

internal val CrystalGreen = Color(0xFF0EA800)

@Composable
internal fun EmojiPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: AppIconKind? = null,
) {
    val palette = navigationPalette()

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.idleContent,
            contentColor = palette.bar,
            disabledContainerColor = palette.barBorder,
            disabledContentColor = palette.textMuted,
        ),
        modifier = modifier,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = palette.bar,
            )
        } else {
            if (icon != null) {
                AppIcon(
                    kind = icon,
                    tint = palette.bar,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
internal fun EmojiTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    helper: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    minHeight: Dp? = null,
    enabled: Boolean = true,
) {
    val palette = navigationPalette()
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (focused) palette.accent else palette.barBorder

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            color = palette.textMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            textStyle = TextStyle(
                color = palette.textPrimary,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            ),
            cursorBrush = SolidColor(palette.accent),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(palette.screen)
                        .border(1.dp, borderColor, shape)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .then(if (minHeight != null) Modifier.heightIn(min = minHeight) else Modifier),
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = palette.textMuted,
                            fontSize = 15.sp,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (helper != null) {
            helper()
        }
    }
}

@Composable
internal fun EmojiLockedBanner(modifier: Modifier = Modifier) {
    val palette = navigationPalette()
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CrystalGreen.copy(alpha = 0.06f))
            .border(BorderStroke(1.dp, CrystalGreen.copy(alpha = 0.4f)), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(CrystalGreen.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            AppIcon(
                kind = AppIconKind.Diamond,
                tint = CrystalGreen,
                modifier = Modifier.size(20.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.emojis_locked_title),
                color = palette.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.emojis_locked_description),
                color = palette.textMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
    }
}

@Composable
internal fun EmojiSkeletonCell(modifier: Modifier = Modifier) {
    val palette = navigationPalette()

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, palette.barBorder.copy(alpha = 0.4f)),
        modifier = modifier.aspectRatio(1f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SkeletonBlock(
                height = 44.dp,
                width = 44.dp,
                shape = RoundedCornerShape(10.dp),
            )
        }
    }
}

@Composable
private fun EmojiImagePlaceholder(palette: NavigationPalette) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(
            kind = AppIconKind.Emojis,
            tint = palette.textMuted,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun EmojiCell(
    emoji: EmojiUi,
    isDeleting: Boolean,
    onOpenOwner: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val shape = RoundedCornerShape(16.dp)
    val marker = remember(emoji.name) { ":${emoji.name}:" }
    val owner = emoji.ownerUsername
    val imageUrl = remember(emoji.url) { resolveMediaUrl(emoji.url) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(palette.barBorder.copy(alpha = 0.18f))
            .border(BorderStroke(1.dp, palette.barBorder.copy(alpha = 0.5f)), shape)
            .then(if (owner.isNotBlank()) Modifier.clickable(onClick = onOpenOwner) else Modifier),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RemoteImage(
                url = imageUrl,
                contentDescription = marker,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                placeholder = { EmojiImagePlaceholder(palette = palette) },
            )

            Text(
                text = marker.uppercase(),
                color = palette.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (owner.isNotBlank()) {
                Text(
                    text = "@$owner",
                    color = palette.textMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (emoji.isMine) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = palette.textPrimary,
                    )
                } else {
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = palette.textPrimary,
                        contentColor = palette.bar,
                    ) {
                        Box(
                            modifier = Modifier.size(26.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppIcon(
                                kind = AppIconKind.Trashbin,
                                tint = palette.bar,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
