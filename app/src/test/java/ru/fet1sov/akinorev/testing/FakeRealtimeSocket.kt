package ru.fet1sov.akinorev.testing

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonElement
import ru.fet1sov.akinorev.data.realtime.RealtimeEvent
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket

class FakeRealtimeSocket : RealtimeSocket {
    val connected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = connected

    private val mutableEvents = MutableSharedFlow<RealtimeEvent>(extraBufferCapacity = 32)
    override val events: SharedFlow<RealtimeEvent> = mutableEvents

    val sent = mutableListOf<String>()

    var connectCalls = 0
        private set
    var disconnectCalls = 0
        private set

    override fun connect() {
        connectCalls += 1
        connected.value = true
    }

    override fun disconnect() {
        disconnectCalls += 1
        connected.value = false
    }

    override fun send(message: String): Boolean {
        sent += message
        return connected.value
    }

    suspend fun emit(type: String, payload: JsonElement? = null) {
        mutableEvents.emit(RealtimeEvent(type = type, payload = payload))
    }
}
