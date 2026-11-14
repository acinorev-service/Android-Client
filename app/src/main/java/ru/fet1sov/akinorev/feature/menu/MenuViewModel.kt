package ru.fet1sov.akinorev.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.SessionState

data class MenuUiState(
    val username: String = "",
    val email: String = "",
    val isLoggingOut: Boolean = false,
)

class MenuViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val loggingOut = MutableStateFlow(false)

    val uiState: StateFlow<MenuUiState> = combine(repository.session, loggingOut) { state, isLoggingOut ->
        val user = (state as? SessionState.Authorized)?.user
        MenuUiState(
            username = user?.username.orEmpty(),
            email = user?.email.orEmpty(),
            isLoggingOut = isLoggingOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(UI_STATE_SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = MenuUiState(),
    )

    fun logout() {
        if (loggingOut.value) return
        loggingOut.value = true
        viewModelScope.launch {
            repository.logout()
            loggingOut.value = false
        }
    }

    companion object {
        private const val UI_STATE_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                MenuViewModel(application.container.authRepository)
            }
        }
    }
}
