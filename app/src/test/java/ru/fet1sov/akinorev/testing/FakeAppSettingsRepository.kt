package ru.fet1sov.akinorev.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppSettings
import ru.fet1sov.akinorev.model.AppTheme

class FakeAppSettingsRepository(
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
