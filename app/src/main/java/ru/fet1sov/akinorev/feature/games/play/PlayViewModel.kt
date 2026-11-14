package ru.fet1sov.akinorev.feature.games.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.catalog.CatalogRepository
import ru.fet1sov.akinorev.data.play.PlaySessionRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.ui.components.apiOrigin
import java.security.SecureRandom

data class PlayUiState(
    val gameId: String,
    val fallbackTitle: String,
    val isLoading: Boolean = true,
    val isUnavailable: Boolean = false,
    val game: CatalogGame? = null,
    val players: Int = 0,
    val roomCode: String = "",
    val gameUrl: String = "",
    val inviteLink: String = "",
    val isFullscreen: Boolean = false,
    val isSessionActive: Boolean = false,
) {
    val title: String
        get() = game?.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
}

class PlayViewModel(
    private val gameId: String,
    private val fallbackTitle: String,
    private val catalogRepository: CatalogRepository,
    private val playSessionRepository: PlaySessionRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(
        PlayUiState(
            gameId = gameId,
            fallbackTitle = fallbackTitle,
            gameUrl = buildGameUrl(gameId, room = null),
        ),
    )
    val uiState: StateFlow<PlayUiState> = mutableUiState.asStateFlow()

    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private var startedSession = false

    init {
        observePlayers()
        observeSession()
        load()
        viewModelScope.launch { authRepository.restoreSession() }
    }

    fun retry() = load()

    fun toggleFullscreen() {
        mutableUiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun exitFullscreen() {
        mutableUiState.update { it.copy(isFullscreen = false) }
    }

    fun ensureRoom(): String {
        val current = mutableUiState.value.roomCode
        if (current.isNotEmpty()) return current

        val room = createRoomCode()
        mutableUiState.update {
            it.copy(
                roomCode = room,
                gameUrl = buildGameUrl(gameId, room),
                inviteLink = buildInviteLink(gameId, room),
            )
        }
        return room
    }

    private fun load() {
        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, isUnavailable = false) }

            when (val result = catalogRepository.fetchGame(gameId)) {
                is AuthResult.Success -> {
                    mutableUiState.update {
                        it.copy(
                            game = result.value,
                            isLoading = false,
                            isUnavailable = false,
                        )
                    }
                    playSessionRepository.watch(gameId)
                    val players = playSessionRepository.fetchPlayers(gameId)
                    mutableUiState.update { it.copy(players = players) }
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(isLoading = false, isUnavailable = true)
                }
            }
        }
    }

    private fun observePlayers() {
        viewModelScope.launch {
            playSessionRepository.players.collect { counts ->
                val count = counts[gameId] ?: return@collect
                mutableUiState.update { it.copy(players = count) }
            }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                if (state is SessionState.Authorized) {
                    startSessionIfNeeded()
                }
            }
        }
    }

    private fun startSessionIfNeeded() {
        if (startedSession || gameId.isBlank()) return
        startedSession = true
        playSessionRepository.start(gameId)
        mutableUiState.update { it.copy(isSessionActive = true) }
        startHeartbeat()
    }

    private fun startHeartbeat() {
        if (heartbeatJob?.isActive == true) return
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(HeartbeatIntervalMillis)
                playSessionRepository.heartbeat(gameId)
            }
        }
    }

    override fun onCleared() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        if (startedSession) {
            playSessionRepository.end(gameId)
            startedSession = false
        }
        playSessionRepository.unwatch(gameId)
        super.onCleared()
    }

    companion object {
        private const val HeartbeatIntervalMillis = 60_000L
        private const val RoomLength = 6
        private const val RoomAlphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun factory(gameId: String, title: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                PlayViewModel(
                    gameId = gameId,
                    fallbackTitle = title,
                    catalogRepository = application.container.catalogRepository,
                    playSessionRepository = application.container.playSessionRepository,
                    authRepository = application.container.authRepository,
                )
            }
        }
    }
}

internal fun buildGameUrl(gameId: String, room: String?): String {
    val origin = apiOrigin(BuildConfig.API_BASE_URL)
    val base = "$origin/game/$gameId"
    if (room.isNullOrBlank()) return base
    return "$base?room=$room&online=1"
}

internal fun buildInviteLink(gameId: String, room: String): String {
    val site = BuildConfig.SITE_URL.trimEnd('/')
    val base = "$site/play/$gameId"
    return if (room.isBlank()) base else "$base?room=$room"
}

internal fun createRoomCode(random: SecureRandom = SecureRandom()): String = buildString {
    repeat(6) {
        append("ABCDEFGHJKLMNPQRSTUVWXYZ23456789"[random.nextInt(32)])
    }
}
