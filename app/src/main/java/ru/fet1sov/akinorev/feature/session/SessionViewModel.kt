package ru.fet1sov.akinorev.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState
import ru.fet1sov.akinorev.ui.components.clearAvatarCache

class SessionViewModel(
    private val repository: AuthRepository,
    private val onSessionClosed: () -> Unit = ::clearAvatarCache,
) : ViewModel() {
    val state: StateFlow<SessionState> = repository.session

    init {
        viewModelScope.launch { repository.restoreSession() }
        viewModelScope.launch { observeSession() }
    }

    private suspend fun observeSession() {
        repository.session.collect { state ->
            if (state == SessionState.Guest) onSessionClosed()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                SessionViewModel(application.container.authRepository)
            }
        }
    }
}
