package ru.fet1sov.akinorev.data.realtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.JwtToken
import ru.fet1sov.akinorev.data.auth.SessionManager
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DefaultRealtimeSocket(
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    apiBaseUrl: String = BuildConfig.API_BASE_URL,
    private val client: OkHttpClient = defaultClient(),
) : RealtimeSocket {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val endpoint = websocketEndpoint(apiBaseUrl)

    private val mutableConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = mutableConnected.asStateFlow()

    private val mutableEvents = MutableSharedFlow<RealtimeEvent>(
        replay = 0,
        extraBufferCapacity = EventBufferCapacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<RealtimeEvent> = mutableEvents.asSharedFlow()

    @Volatile
    private var started = false

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var reconnectJob: Job? = null
    private var attempts = 0

    override fun connect() {
        scope.launch {
            val shouldOpen = mutex.withLock {
                if (started) return@withLock false
                started = true
                attempts = 0
                true
            }
            if (shouldOpen) open()
        }
    }

    override fun disconnect() {
        scope.launch {
            val socket = mutex.withLock {
                started = false
                reconnectJob?.cancel()
                reconnectJob = null
                attempts = 0
                val current = webSocket
                webSocket = null
                current
            }
            mutableConnected.value = false
            socket?.close(NormalClosure, "client stopped")
        }
    }

    override fun send(message: String): Boolean {
        val socket = webSocket ?: return false
        return socket.send(message)
    }

    private suspend fun open() {
        val canOpen = mutex.withLock { started && webSocket == null }
        if (!canOpen) return

        ensureFreshToken()

        val token = sessionManager.accessToken()
        if (token.isNullOrBlank()) {
            scheduleReconnect()
            return
        }

        val request = Request.Builder()
            .url("$endpoint/ws?token=${URLEncoder.encode(token, "UTF-8")}")
            .header("User-Agent", UserAgent)
            .build()

        val socket = client.newWebSocket(request, SocketListener())

        val keepSocket = mutex.withLock {
            if (started) {
                webSocket = socket
                true
            } else {
                false
            }
        }
        if (!keepSocket) socket.close(NormalClosure, "client stopped")
    }

    private suspend fun ensureFreshToken() {
        val token = sessionManager.accessToken() ?: return
        if (!JwtToken.isExpiring(token)) return
        authRepository.refreshTokens()
    }

    private fun handleClosed(socket: WebSocket) {
        mutableConnected.value = false
        scope.launch {
            val shouldReconnect = mutex.withLock {
                if (webSocket === socket) webSocket = null
                started
            }
            if (shouldReconnect) scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            val delayMillis = mutex.withLock {
                val current = attempts
                attempts = current + 1
                reconnectDelay(current)
            }
            delay(delayMillis)
            if (mutex.withLock { started }) open()
        }
    }

    private inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            mutableConnected.value = true
            scope.launch {
                mutex.withLock { attempts = 0 }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val message = runCatching { Json.parseToJsonElement(text) }.getOrNull() as? JsonObject ?: return
            val type = message["type"]?.jsonPrimitive?.contentOrNull
                ?.takeIf { it.isNotBlank() } ?: return
            mutableEvents.tryEmit(
                RealtimeEvent(type = type, payload = message["payload"]),
            )
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(NormalClosure, null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            handleClosed(webSocket)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            handleClosed(webSocket)
        }
    }

    private companion object {
        const val NormalClosure = 1000
        const val EventBufferCapacity = 32
        const val UserAgent = "Akinorev-Android/${BuildConfig.VERSION_NAME} (Android)"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()
    }
}

internal fun websocketEndpoint(apiBaseUrl: String): String {
    val trimmed = apiBaseUrl.trim().trimEnd('/').removeSuffix("/api")
    val base = trimmed.ifBlank { apiBaseUrl.trim() }
    return when {
        base.startsWith("https://") -> "wss://" + base.removePrefix("https://")
        base.startsWith("http://") -> "ws://" + base.removePrefix("http://")
        base.startsWith("wss://") || base.startsWith("ws://") -> base
        else -> "ws://$base"
    }
}

internal fun reconnectDelay(
    attempt: Int,
    jitterMillis: Long = Random.nextLong(JitterMaxMillis),
): Long {
    val exponent = attempt.coerceIn(0, MaxExponent)
    val base = (BaseDelayMillis shl exponent).coerceAtMost(MaxDelayMillis)
    return base + jitterMillis.coerceAtLeast(0)
}

private const val BaseDelayMillis = 1_000L
private const val MaxDelayMillis = 30_000L
private const val MaxExponent = 5
private const val JitterMaxMillis = 1_000L
