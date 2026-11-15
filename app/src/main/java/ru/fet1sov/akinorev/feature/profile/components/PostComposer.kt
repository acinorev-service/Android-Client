package ru.fet1sov.akinorev.feature.profile.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.fet1sov.akinorev.R
import ru.fet1sov.akinorev.data.community.MaxPostImages
import ru.fet1sov.akinorev.feature.profile.ComposerImageUi
import ru.fet1sov.akinorev.ui.components.LocalImage
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun PostComposer(
    text: String,
    placeholder: String,
    images: List<ComposerImageUi>,
    onTextChange: (String) -> Unit,
    onImagesPicked: (List<String>) -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = navigationPalette()
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MaxPostImages),
    ) { uris ->
        if (uris.isNotEmpty()) onImagesPicked(uris.map(Uri::toString))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (images.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(images, key = { it.id }) { image ->
                    ComposerThumbnail(
                        image = image,
                        enabled = enabled,
                        onRemove = { onRemoveImage(image.id) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 110.dp),
            enabled = enabled,
            minLines = 3,
            maxLines = 8,
            placeholder = {
                Text(
                    text = placeholder,
                    color = palette.textMuted,
                    fontSize = 14.sp,
                )
            },
            textStyle = TextStyle(
                color = palette.textPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = palette.textMuted,
                unfocusedBorderColor = palette.barBorder,
                focusedContainerColor = palette.bar,
                unfocusedContainerColor = palette.bar,
                cursorColor = palette.textPrimary,
                focusedTextColor = palette.textPrimary,
                unfocusedTextColor = palette.textPrimary,
                disabledTextColor = palette.textMuted,
            ),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = {
                    if (images.size < MaxPostImages) {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }
                },
                enabled = enabled && images.size < MaxPostImages,
                shape = CircleShape,
                color = palette.barBorder.copy(alpha = 0.25f),
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        kind = AppIconKind.Plus,
                        tint = palette.textPrimary,
                        contentDescription = stringResource(R.string.community_posts_attach_image),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = stringResource(R.string.community_posts_markdown_hint),
                color = palette.textMuted,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun ComposerThumbnail(
    image: ComposerImageUi,
    enabled: Boolean,
    onRemove: () -> Unit,
) {
    val palette = navigationPalette()
    Box(modifier = Modifier.size(80.dp)) {
        if (image.uri != null) {
            LocalImage(
                uri = image.uri,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .background(palette.barBorder.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            RemoteImage(
                url = resolveMediaUrl(image.url.orEmpty()),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                contentScale = ContentScale.Crop,
            )
        }

        if (enabled) {
            Surface(
                onClick = onRemove,
                shape = CircleShape,
                color = palette.idleContent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
            ) {
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        kind = AppIconKind.Close,
                        tint = palette.bar,
                        contentDescription = stringResource(R.string.community_posts_remove_image),
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
    }
}
