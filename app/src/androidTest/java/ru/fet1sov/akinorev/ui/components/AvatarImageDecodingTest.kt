package ru.fet1sov.akinorev.ui.components

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarImageDecodingTest {
    private val assets = InstrumentationRegistry.getInstrumentation().context.assets

    @Test
    fun animatedGif_decodesToAnimatedDrawable() {
        val image = decodeAvatarImage(read("avatar-animated.gif"))

        assertNotNull(image)
        assertTrue(image is AvatarImage.Animated)

        val drawable = (image as AvatarImage.Animated).drawable
        InstrumentationRegistry.getInstrumentation().runOnMainSync { drawable.start() }
        assertTrue(drawable.isRunning)
    }

    @Test
    fun staticPng_decodesToStillImage() {
        val image = decodeAvatarImage(read("avatar-static.png"))

        assertTrue(image is AvatarImage.Still)
    }

    @Test
    fun brokenPayload_returnsNull() {
        assertNull(decodeAvatarImage(byteArrayOf(1, 2, 3, 4)))
    }

    @Test
    fun clearAvatarCache_dropsCachedImages() {
        val image = decodeAvatarImage(read("avatar-static.png"))
        assertNotNull(image)
        AvatarImageCache.put("cache-key", image!!)
        assertNotNull(AvatarImageCache.get("cache-key"))

        clearAvatarCache()

        assertNull(AvatarImageCache.get("cache-key"))
    }

    private fun read(name: String): ByteArray = assets.open(name).use { it.readBytes() }
}
