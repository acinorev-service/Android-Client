package ru.fet1sov.akinorev.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostTextTest {
    @Test
    fun localizedPostText_prefersRequestedLocale() {
        val content = mapOf("ru" to "Привет", "en" to "Hello")

        assertEquals("Привет", localizedPostText(content, "ru-RU"))
        assertEquals("Hello", localizedPostText(content, "en-US"))
    }

    @Test
    fun localizedPostText_fallsBackToAvailableLocale() {
        val content = mapOf("ru" to "Привет")

        assertEquals("Привет", localizedPostText(content, "de"))
        assertEquals("", localizedPostText(null, "ru"))
        assertEquals("", localizedPostText(emptyMap(), "en"))
    }

    @Test
    fun postLocaleTag_normalizesUnknownLocales() {
        assertEquals("ru", postLocaleTag("ru-RU"))
        assertEquals("en", postLocaleTag("de"))
        assertEquals("en", postLocaleTag(null))
    }

    @Test
    fun splitPostContent_groupsConsecutiveImages() {
        val images = listOf(PostImage("a"), PostImage("b"))
        val parts = splitPostContent("text [[img:0]][[img:1]] tail", images)

        assertEquals(3, parts.size)
        assertTrue(parts[0] is PostContentPart.Text)
        val imagePart = parts[1] as PostContentPart.Images
        assertEquals(2, imagePart.images.size)
        assertTrue(parts[2] is PostContentPart.Text)
    }

    @Test
    fun splitPostContent_appendsUnreferencedImages() {
        val images = listOf(PostImage("a"), PostImage("b"))
        val parts = splitPostContent("only text", images)

        assertEquals(2, parts.size)
        assertTrue(parts[0] is PostContentPart.Text)
        val imagePart = parts[1] as PostContentPart.Images
        assertEquals(2, imagePart.images.size)
    }

    @Test
    fun splitPostContent_dropsUnknownMarkers() {
        val images = listOf(PostImage("a"))
        val parts = splitPostContent("hello [[img:5]]", images)

        assertEquals(1, parts.size)
        assertTrue(parts[0] is PostContentPart.Text)
    }

    @Test
    fun rebuildImageMarkers_renumbersAndDropsExcessMarkers() {
        val content = mapOf(
            "ru" to "a [[img:0]] b [[img:1]] c [[img:2]]",
            "en" to "x [[img:9]]",
        )

        val rebuilt = rebuildImageMarkers(content, imageCount = 2)

        assertEquals("a [[img:0]] b [[img:1]] c ", rebuilt["ru"])
        assertEquals("x [[img:0]]", rebuilt["en"])
    }

    @Test
    fun appendImageMarkers_addsBlockOnNewLine() {
        assertEquals("text\n[[img:1]]\n[[img:2]]", appendImageMarkers("text", 1, 2))
        assertEquals("[[img:0]]", appendImageMarkers("", 0, 1))
        assertEquals("text\n[[img:0]]", appendImageMarkers("text\n", 0, 1))
    }

    @Test
    fun collectEmojiNames_findsMarkersWithBoundaries() {
        val names = collectEmojiNames("привет :pepe: и :HELLO_1:, а также a:b: и :no")

        assertEquals(listOf("pepe", "hello_1"), names)
    }

    @Test
    fun filledPostContent_trimsAndDropsEmptyLocales() {
        val filled = filledPostContent(mapOf("ru" to "  Привет  ", "en" to "   ", "de" to "Hallo"))

        assertEquals(mapOf("ru" to "Привет"), filled)
    }
}
