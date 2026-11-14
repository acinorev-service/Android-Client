package ru.fet1sov.akinorev.data.realtime

import kotlinx.serialization.json.JsonElement

data class RealtimeEvent(
    val type: String,
    val payload: JsonElement?,
)
