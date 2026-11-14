package ru.fet1sov.akinorev.testing

import ru.fet1sov.akinorev.data.emojis.EmojiFileResult
import ru.fet1sov.akinorev.data.emojis.EmojiFileSource
import ru.fet1sov.akinorev.data.emojis.EmojiUploadFile

class FakeEmojiFileSource : EmojiFileSource {
    var result: EmojiFileResult = EmojiFileResult.Ready(
        EmojiUploadFile(
            fileName = "pepe.png",
            mimeType = "image/png",
            bytes = byteArrayOf(1, 2, 3),
        ),
    )

    val requestedUris: MutableList<String> = mutableListOf()

    override suspend fun read(uri: String): EmojiFileResult {
        requestedUris += uri
        return result
    }
}
