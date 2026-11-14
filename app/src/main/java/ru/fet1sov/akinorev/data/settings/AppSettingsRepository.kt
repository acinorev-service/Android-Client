package ru.fet1sov.akinorev.data.settings

import kotlinx.coroutines.flow.StateFlow
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppSettings
import ru.fet1sov.akinorev.model.AppTheme

interface AppSettingsRepository {
    val settings: StateFlow<AppSettings>

    fun setTheme(theme: AppTheme)

    fun setLanguage(language: AppLanguage)

    fun setAnimationsEnabled(enabled: Boolean)
}
