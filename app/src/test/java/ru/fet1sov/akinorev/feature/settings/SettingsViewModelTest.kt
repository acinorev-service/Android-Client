package ru.fet1sov.akinorev.feature.settings

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.auth.asDomain
import ru.fet1sov.akinorev.data.notifications.DefaultNotificationPreferencesRepository
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.dto.ProfilePayloadDto
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppSettings
import ru.fet1sov.akinorev.model.AppTheme
import ru.fet1sov.akinorev.model.NotificationPreferenceKey
import ru.fet1sov.akinorev.testing.ApiFixtures
import ru.fet1sov.akinorev.testing.FakeAcinorevApi
import ru.fet1sov.akinorev.testing.InMemorySessionStorage

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()
    private val api = FakeAcinorevApi()
    private val sessionManager = SessionManager(InMemorySessionStorage())
    private val authRepository = DefaultAuthRepository(api, sessionManager)
    private val settingsRepository = FakeAppSettingsRepository()
    private val notificationsRepository =
        DefaultNotificationPreferencesRepository(api, ApiClient(api, sessionManager))

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    private suspend fun authorize() {
        sessionManager.save(ApiFixtures.user.asDomain(), ApiFixtures.tokens.asDomain())
        api.onGetProfile = { ApiFixtures.success(ProfilePayloadDto(user = ApiFixtures.user)) }
    }

    private fun viewModel() = SettingsViewModel(
        authRepository = authRepository,
        appSettingsRepository = settingsRepository,
        notificationPreferencesRepository = notificationsRepository,
    )

    @Test
    fun authorizedSessionLoadsNotificationPreferences() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = {
            ApiFixtures.success(ApiFixtures.notificationPreferences(follow = false, broadcast = false))
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthPending)
        assertTrue(state.isAuthenticated)
        assertFalse(state.notifications[NotificationPreferenceKey.Follow])
        assertFalse(state.notifications[NotificationPreferenceKey.Broadcast])
        assertTrue(state.notifications[NotificationPreferenceKey.Like])
        assertFalse(state.isNotificationsLoading)
        assertNull(state.notificationMessage)
    }

    @Test
    fun preferenceChange_updatesServerAndState() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = { ApiFixtures.success(ApiFixtures.notificationPreferences()) }
        api.onUpdateNotificationPreferences = { request ->
            ApiFixtures.success(
                ApiFixtures.notificationPreferences(like = request.like ?: true),
            )
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.setNotificationPreference(NotificationPreferenceKey.Like, false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.notifications[NotificationPreferenceKey.Like])
        assertNull(state.savingPreference)
        assertNull(state.notificationMessage)
    }

    @Test
    fun preferenceChange_withServerErrorRevertsAndShowsMessage() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = { ApiFixtures.success(ApiFixtures.notificationPreferences()) }
        api.onUpdateNotificationPreferences = { ApiFixtures.error(500, "server_error") }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.setNotificationPreference(NotificationPreferenceKey.Follow, false)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notifications[NotificationPreferenceKey.Follow])
        assertNull(state.savingPreference)
        assertEquals(SettingsNotificationMessage.SaveFailed, state.notificationMessage)
    }

    @Test
    fun loadFailure_showsLoadMessage() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = { ApiFixtures.error(500, "server_error") }

        val viewModel = viewModel()
        advanceUntilIdle()

        assertEquals(SettingsNotificationMessage.LoadFailed, viewModel.uiState.value.notificationMessage)
        assertFalse(viewModel.uiState.value.isNotificationsLoading)
    }

    @Test
    fun settingsChanges_arePersistedInRepository() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = { ApiFixtures.success(ApiFixtures.notificationPreferences()) }
        var localeUpdates = 0
        api.onUpdateLocale = {
            localeUpdates += 1
            ApiFixtures.success(ru.fet1sov.akinorev.data.remote.dto.LocaleDto(locale = "ru"))
        }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.setTheme(AppTheme.Dark)
        viewModel.setLanguage(AppLanguage.Russian)
        viewModel.setAnimationsEnabled(false)
        advanceUntilIdle()

        val settings = settingsRepository.settings.value
        assertEquals(AppTheme.Dark, settings.theme)
        assertEquals(AppLanguage.Russian, settings.language)
        assertFalse(settings.animationsEnabled)
        assertEquals(1, localeUpdates)
        assertEquals(AppTheme.Dark, viewModel.uiState.value.theme)
    }

    @Test
    fun selectTab_changesActiveTab() = runTest(mainDispatcher) {
        authorize()
        api.onGetNotificationPreferences = { ApiFixtures.success(ApiFixtures.notificationPreferences()) }

        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.selectTab(SettingsTab.Account)

        assertEquals(SettingsTab.Account, viewModel.uiState.value.activeTab)
    }
}

private class FakeAppSettingsRepository(
    initial: AppSettings = AppSettings(),
) : AppSettingsRepository {
    private val mutableSettings = MutableStateFlow(initial)
    override val settings: StateFlow<AppSettings> = mutableSettings

    override fun setTheme(theme: AppTheme) {
        mutableSettings.update { it.copy(theme = theme) }
    }

    override fun setLanguage(language: AppLanguage) {
        mutableSettings.update { it.copy(language = language) }
    }

    override fun setAnimationsEnabled(enabled: Boolean) {
        mutableSettings.update { it.copy(animationsEnabled = enabled) }
    }
}
