package ru.fet1sov.akinorev.ui.components

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocalImage(
    uri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp),
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val image by produceState<AvatarImage?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            if (uri.isBlank()) {
                null
            } else {
                decodeImageSource(ImageDecoder.createSource(context.contentResolver, Uri.parse(uri)))
            }
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

@Composable
internal fun DrawableImage(
    drawable: Drawable,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = contentScale.toImageViewScaleType()
                this.contentDescription = contentDescription
            }
        },
        update = { view ->
            if (view.drawable !== drawable) {
                view.setImageDrawable(drawable)
            }
            if (drawable is AnimatedImageDrawable && !drawable.isRunning) {
                drawable.start()
            }
        },
        modifier = modifier,
    )
}

private fun ContentScale.toImageViewScaleType(): ImageView.ScaleType = when (this) {
    ContentScale.Fit -> ImageView.ScaleType.FIT_CENTER
    ContentScale.FillBounds -> ImageView.ScaleType.FIT_XY
    else -> ImageView.ScaleType.CENTER_CROP
}
