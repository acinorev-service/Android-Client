package ru.fet1sov.akinorev.feature.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.navigation.NavigationRepository

class NavigationViewModel(
    navigationRepository: NavigationRepository,
) : ViewModel() {
    val uiState: StateFlow<NavigationUiState> = navigationRepository.observeBadges()
        .map { badges -> NavigationUiState(badges = badges) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(UI_STATE_SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = NavigationUiState(),
        )

    companion object {
        private const val UI_STATE_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                NavigationViewModel(application.container.navigationRepository)
            }
        }
    }
}
