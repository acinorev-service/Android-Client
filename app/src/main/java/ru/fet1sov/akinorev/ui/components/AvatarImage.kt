package ru.fet1sov.akinorev.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import java.nio.ByteBuffer
import kotlin.math.roundToInt

internal sealed interface AvatarImage {
    class Still(val bitmap: Bitmap) : AvatarImage

    class Animated(val drawable: AnimatedImageDrawable) : AvatarImage
}

internal fun decodeAvatarImage(bytes: ByteArray): AvatarImage? = runCatching {
    decodeImageSource(ImageDecoder.createSource(ByteBuffer.wrap(bytes)))
}.getOrNull()

internal fun decodeImageSource(source: ImageDecoder.Source): AvatarImage? = runCatching {
    val drawable = ImageDecoder.decodeDrawable(source) { decoder, info, _ ->
        val width = info.size.width
        val height = info.size.height
        val maxDimension = maxOf(width, height)
        if (maxDimension > MaxAvatarDimension) {
            val scale = MaxAvatarDimension.toFloat() / maxDimension
            decoder.setTargetSize(
                (width * scale).roundToInt().coerceAtLeast(1),
                (height * scale).roundToInt().coerceAtLeast(1),
            )
        }
    }

    when (drawable) {
        is AnimatedImageDrawable -> AvatarImage.Animated(drawable)
        is BitmapDrawable -> drawable.bitmap?.let { AvatarImage.Still(it) }
        else -> null
    }
}.getOrNull()

private const val MaxAvatarDimension = 512
