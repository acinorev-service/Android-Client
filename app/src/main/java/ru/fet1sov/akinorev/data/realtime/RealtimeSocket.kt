package ru.fet1sov.akinorev.data.realtime

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface RealtimeSocket {
    val isConnected: StateFlow<Boolean>

    val events: SharedFlow<RealtimeEvent>

    fun connect()

    fun disconnect()
}
