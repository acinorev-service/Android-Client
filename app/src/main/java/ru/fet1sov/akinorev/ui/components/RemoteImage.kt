package ru.fet1sov.akinorev.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: (@Composable () -> Unit)? = null,
) {
    var image by remember(url) { mutableStateOf(RemoteImageLoader.cached(url)) }

    LaunchedEffect(url) {
        if (image == null) {
            image = if (url.isBlank()) null else RemoteImageLoader.load(url)
        }
    }

    val target = modifier.clip(shape)

    when (val current = image) {
        is AvatarImage.Still -> Image(
            bitmap = current.bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = target,
        )

        is AvatarImage.Animated -> DrawableImage(
            drawable = current.drawable,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = target,
        )

        null -> if (placeholder != null) {
            placeholder()
        } else {
            Box(modifier = target)
        }
    }
}
