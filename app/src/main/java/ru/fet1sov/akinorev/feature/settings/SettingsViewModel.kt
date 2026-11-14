package ru.fet1sov.akinorev.feature.settings

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
import ru.fet1sov.akinorev.data.notifications.NotificationPreferencesRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppTheme
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.model.NotificationPreferences
import ru.fet1sov.akinorev.ui.icons.AppIconKind

enum class SettingsTab(
    val labelRes: Int,
    val icon: AppIconKind,
) {
    Appearance(ru.fet1sov.akinorev.R.string.settings_tabs_appearance, AppIconKind.Appearance),
    Notifications(ru.fet1sov.akinorev.R.string.settings_tabs_notifications, AppIconKind.Notifications),
    Account(ru.fet1sov.akinorev.R.string.settings_tabs_account, AppIconKind.Account),
}

enum class SettingsNotificationMessage {
    LoadFailed,
    SaveFailed,
}

data class SettingsUiState(
    val activeTab: SettingsTab = SettingsTab.Appearance,
    val theme: AppTheme = AppTheme.System,
    val language: AppLanguage = AppLanguage.System,
    val animationsEnabled: Boolean = true,
    val isAuthPending: Boolean = true,
    val isAuthenticated: Boolean = false,
    val notifications: NotificationPreferences = NotificationPreferences.Default,
    val isNotificationsLoading: Boolean = false,
    val savingPreference: NotificationPreferenceKey? = null,
    val notificationMessage: SettingsNotificationMessage? = null,
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    private var notificationsLoaded = false

    init {
        val settings = appSettingsRepository.settings.value
        mutableUiState.update {
            it.copy(
                theme = settings.theme,
                language = settings.language,
                animationsEnabled = settings.animationsEnabled,
            )
        }

        viewModelScope.launch {
            appSettingsRepository.settings.collect { value ->
                mutableUiState.update {
                    it.copy(
                        theme = value.theme,
                        language = value.language,
                        animationsEnabled = value.animationsEnabled,
                    )
                }
            }
        }

        viewModelScope.launch {
            authRepository.session.collect { state ->
                mutableUiState.update {
                    it.copy(
                        isAuthPending = state is SessionState.Loading,
                        isAuthenticated = state is SessionState.Authorized,
                    )
                }
                if (state is SessionState.Authorized && !notificationsLoaded) {
                    loadNotificationPreferences()
                }
                if (state is SessionState.Guest) {
                    notificationsLoaded = false
                    mutableUiState.update {
                        it.copy(
                            notifications = NotificationPreferences.Default,
                            isNotificationsLoading = false,
                            notificationMessage = null,
                        )
                    }
                }
            }
        }

        viewModelScope.launch { authRepository.restoreSession() }
    }

    fun selectTab(tab: SettingsTab) {
        mutableUiState.update { it.copy(activeTab = tab) }
    }

    fun setTheme(theme: AppTheme) {
        appSettingsRepository.setTheme(theme)
    }

    fun setLanguage(language: AppLanguage) {
        appSettingsRepository.setLanguage(language)
        if (language.tag.isBlank()) return
        viewModelScope.launch { authRepository.updateLocale(language.tag) }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        appSettingsRepository.setAnimationsEnabled(enabled)
    }

    fun setNotificationPreference(key: NotificationPreferenceKey, enabled: Boolean) {
        val current = mutableUiState.value
        if (current.savingPreference == key || current.notifications[key] == enabled) return

        mutableUiState.update {
            it.copy(
                notifications = it.notifications.updated(key, enabled),
                savingPreference = key,
                notificationMessage = null,
            )
        }

        viewModelScope.launch {
            when (val result = notificationPreferencesRepository.updatePreference(key, enabled)) {
                is AuthResult.Success -> mutableUiState.update {
                    it.copy(
                        notifications = result.value,
                        savingPreference = null,
                    )
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        notifications = it.notifications.updated(key, !enabled),
                        savingPreference = null,
                        notificationMessage = SettingsNotificationMessage.SaveFailed,
                    )
                }
            }
        }
    }

    fun retryNotificationPreferences() {
        notificationsLoaded = false
        viewModelScope.launch { loadNotificationPreferences() }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    private suspend fun loadNotificationPreferences() {
        notificationsLoaded = true
        mutableUiState.update {
            it.copy(isNotificationsLoading = true, notificationMessage = null)
        }
        when (val result = notificationPreferencesRepository.fetchPreferences()) {
            is AuthResult.Success -> mutableUiState.update {
                it.copy(
                    notifications = result.value,
                    isNotificationsLoading = false,
                )
            }

            is AuthResult.Failure -> {
                notificationsLoaded = false
                mutableUiState.update {
                    it.copy(
                        isNotificationsLoading = false,
                        notificationMessage = SettingsNotificationMessage.LoadFailed,
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                SettingsViewModel(
                    authRepository = application.container.authRepository,
                    appSettingsRepository = application.container.appSettingsRepository,
                    notificationPreferencesRepository = application.container.notificationPreferencesRepository,
                )
            }
        }
    }
}
