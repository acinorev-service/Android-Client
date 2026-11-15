package ru.fet1sov.akinorev.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.fet1sov.akinorev.model.PostContentPart
import ru.fet1sov.akinorev.model.PostImage
import ru.fet1sov.akinorev.model.splitPostContent
import ru.fet1sov.akinorev.ui.components.CommunityRichText
import ru.fet1sov.akinorev.ui.components.RemoteImage
import ru.fet1sov.akinorev.ui.components.resolveMediaUrl
import ru.fet1sov.akinorev.ui.icons.AppIcon
import ru.fet1sov.akinorev.ui.icons.AppIconKind
import ru.fet1sov.akinorev.ui.navigation.navigationPalette

@Composable
fun PostContent(
    source: String,
    images: List<PostImage>,
    emojiUrls: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val palette = navigationPalette()
    val parts = remember(source, images) { splitPostContent(source, images) }
    if (parts.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        parts.forEach { part ->
            when (part) {
                is PostContentPart.Text -> {
                    CommunityRichText(
                        text = part.value,
                        emojiUrls = emojiUrls,
                        style = androidx.compose.ui.text.TextStyle(
                            color = palette.textPrimary,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        emojiSize = 17.sp,
                    )
                }

                is PostContentPart.Images -> PostImageCarousel(images = part.images)
            }
        }
    }
}

@Composable
private fun PostImageCarousel(images: List<PostImage>) {
    if (images.isEmpty()) return
    var viewerUrl by remember { mutableStateOf<String?>(null) }

    if (images.size == 1) {
        val image = images.first()
        RemoteImage(
            url = resolveMediaUrl(image.url),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clickable { viewerUrl = image.url },
            shape = RoundedCornerShape(12.dp),
        )
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(images) { _, image ->
                RemoteImage(
                    url = resolveMediaUrl(image.url),
                    contentDescription = null,
                    modifier = Modifier
                        .size(220.dp)
                        .clickable { viewerUrl = image.url },
                    shape = RoundedCornerShape(12.dp),
                )
            }
        }
    }

    val current = viewerUrl
    if (current != null) {
        PostImageViewer(url = current, onDismiss = { viewerUrl = null })
    }
}

@Composable
private fun PostImageViewer(
    url: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            RemoteImage(
                url = resolveMediaUrl(url),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                contentScale = ContentScale.Fit,
            )
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        kind = AppIconKind.Close,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
