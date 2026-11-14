package ru.fet1sov.akinorev.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.library.LibraryRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.LibraryGame

data class LibraryGameUi(
    val id: String,
    val title: String,
    val iconPath: String,
    val genreSlug: String,
    val secondsPlayed: Long,
    val lastPlayedAtMillis: Long?,
)

data class GamesUiState(
    val isAuthPending: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val games: List<LibraryGameUi> = emptyList(),
    val totalSeconds: Long = 0,
    val isUnavailable: Boolean = false,
)

class GamesViewModel(
    private val authRepository: AuthRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = mutableUiState.asStateFlow()

    private var loaded = false

    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                val authorized = state is SessionState.Authorized
                mutableUiState.update {
                    it.copy(
                        isAuthPending = state is SessionState.Loading,
                        isAuthenticated = authorized,
                    )
                }
                when {
                    authorized && !loaded -> {
                        loaded = true
                        load()
                    }

                    state is SessionState.Guest -> {
                        loaded = false
                        mutableUiState.value = GamesUiState(isAuthPending = false)
                    }
                }
            }
        }

        viewModelScope.launch { authRepository.restoreSession() }
    }

    fun refresh() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing) return
        load()
    }

    fun retry() = refresh()

    private fun load() {
        if (!mutableUiState.value.isAuthenticated) return

        viewModelScope.launch {
            val hasGames = mutableUiState.value.games.isNotEmpty()
            mutableUiState.update {
                it.copy(
                    isLoading = !hasGames,
                    isRefreshing = hasGames,
                    isUnavailable = false,
                )
            }

            when (val result = libraryRepository.fetchLibrary()) {
                is AuthResult.Success -> {
                    val games = result.value.games.map(LibraryGame::toUi)
                    val totalSeconds = result.value.totals.secondsPlayed
                        .takeIf { it > 0 }
                        ?: games.sumOf { it.secondsPlayed }

                    mutableUiState.update {
                        it.copy(
                            games = games,
                            totalSeconds = totalSeconds,
                            isLoading = false,
                            isRefreshing = false,
                            isUnavailable = false,
                        )
                    }
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isUnavailable = it.games.isEmpty(),
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                GamesViewModel(
                    authRepository = application.container.authRepository,
                    libraryRepository = application.container.libraryRepository,
                )
            }
        }
    }
}

private fun LibraryGame.toUi(): LibraryGameUi = LibraryGameUi(
    id = id,
    title = title,
    iconPath = iconPath,
    genreSlug = genreSlug,
    secondsPlayed = secondsPlayed,
    lastPlayedAtMillis = lastPlayedAtMillis,
)
