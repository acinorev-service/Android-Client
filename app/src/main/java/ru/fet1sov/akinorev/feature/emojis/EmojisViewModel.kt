package ru.fet1sov.akinorev.feature.emojis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.Emoji

data class EmojiUi(
    val id: String,
    val name: String,
    val title: String,
    val url: String,
    val ownerUsername: String,
    val isMine: Boolean,
)

data class EmojisUiState(
    val isAuthPending: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isSubscribed: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val deletingId: String? = null,
    val items: List<EmojiUi> = emptyList(),
    val hasMore: Boolean = false,
    val total: Int = 0,
    val isUnavailable: Boolean = false,
)

sealed interface EmojisEvent {
    data object Deleted : EmojisEvent

    data object DeleteFailed : EmojisEvent
}

class EmojisViewModel(
    private val authRepository: AuthRepository,
    private val emojisRepository: EmojisRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(EmojisUiState())
    val uiState: StateFlow<EmojisUiState> = mutableUiState.asStateFlow()

    private val mutableEvents = Channel<EmojisEvent>(Channel.BUFFERED)
    val events: Flow<EmojisEvent> = mutableEvents.receiveAsFlow()

    private var nextPage = FirstPage

    init {
        viewModelScope.launch {
            authRepository.session.collect { state ->
                val authorized = state as? SessionState.Authorized
                mutableUiState.update {
                    it.copy(
                        isAuthPending = state is SessionState.Loading,
                        isAuthenticated = authorized != null,
                        isSubscribed = authorized?.user?.isSubscribed == true,
                    )
                }
                if (state is SessionState.Guest) {
                    nextPage = FirstPage
                    mutableUiState.value = EmojisUiState(isAuthPending = false)
                }
            }
        }

        viewModelScope.launch { authRepository.restoreSession() }
    }

    fun refresh() {
        val state = mutableUiState.value
        if (!state.isAuthenticated || state.isLoading) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoading = true, isUnavailable = false) }
            when (val result = emojisRepository.fetchPage(page = FirstPage, limit = PageSize)) {
                is AuthResult.Success -> {
                    nextPage = result.value.page + 1
                    mutableUiState.update {
                        it.copy(
                            items = result.value.items.map(Emoji::toUi),
                            hasMore = result.value.page < result.value.totalPages,
                            total = result.value.total,
                            isLoading = false,
                            isUnavailable = false,
                        )
                    }
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(isLoading = false, isUnavailable = it.items.isEmpty())
                }
            }
        }
    }

    fun loadMore() {
        val state = mutableUiState.value
        if (!state.isAuthenticated || state.isLoading || state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(isLoadingMore = true) }
            when (val result = emojisRepository.fetchPage(page = nextPage, limit = PageSize)) {
                is AuthResult.Success -> {
                    nextPage = result.value.page + 1
                    mutableUiState.update { current ->
                        val known = current.items.mapTo(HashSet()) { it.id }
                        val next = result.value.items
                            .filter { it.id !in known }
                            .map(Emoji::toUi)
                        current.copy(
                            items = current.items + next,
                            hasMore = result.value.page < result.value.totalPages,
                            total = result.value.total,
                            isLoadingMore = false,
                        )
                    }
                }

                is AuthResult.Failure -> mutableUiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun delete(id: String) {
        val state = mutableUiState.value
        if (!state.isAuthenticated || state.deletingId != null) return

        viewModelScope.launch {
            mutableUiState.update { it.copy(deletingId = id) }
            when (emojisRepository.delete(id)) {
                is AuthResult.Success -> {
                    mutableUiState.update { current ->
                        current.copy(
                            items = current.items.filterNot { it.id == id },
                            total = (current.total - 1).coerceAtLeast(0),
                            deletingId = null,
                        )
                    }
                    mutableEvents.send(EmojisEvent.Deleted)
                }

                is AuthResult.Failure -> {
                    mutableUiState.update { it.copy(deletingId = null) }
                    mutableEvents.send(EmojisEvent.DeleteFailed)
                }
            }
        }
    }

    companion object {
        const val PageSize = 24
        private const val FirstPage = 1

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                EmojisViewModel(
                    authRepository = application.container.authRepository,
                    emojisRepository = application.container.emojisRepository,
                )
            }
        }
    }
}

private fun Emoji.toUi(): EmojiUi = EmojiUi(
    id = id,
    name = name,
    title = title,
    url = url,
    ownerUsername = ownerUsername,
    isMine = isMine,
)
