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
import ru.fet1sov.akinorev.model.UserProfileSummary
import ru.fet1sov.akinorev.model.toProfileSummary
import ru.fet1sov.akinorev.ui.format.formatSubscriptionExpiry

data class MenuUiState(
    val isAuthPending: Boolean = true,
    val profile: UserProfileSummary? = null,
    val isLoggingOut: Boolean = false,
    val entries: List<MenuEntry> = MenuEntry.entries,
) {
    val subscriptionExpiry: String
        get() = formatSubscriptionExpiry(profile?.subscriptionExpiresAt)
}

class MenuViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val loggingOut = MutableStateFlow(false)

    val uiState: StateFlow<MenuUiState> = combine(repository.session, loggingOut) { state, isLoggingOut ->
        MenuUiState(
            isAuthPending = state is SessionState.Loading,
            profile = (state as? SessionState.Authorized)?.user?.toProfileSummary(),
            isLoggingOut = isLoggingOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(UI_STATE_SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = MenuUiState(),
    )

    init {
        viewModelScope.launch { repository.restoreSession() }
    }

    fun refreshProfile() {
        viewModelScope.launch { repository.fetchProfile() }
    }

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
