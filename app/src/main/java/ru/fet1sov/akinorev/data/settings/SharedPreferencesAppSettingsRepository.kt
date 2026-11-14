package ru.fet1sov.akinorev.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.fet1sov.akinorev.model.AppLanguage
import ru.fet1sov.akinorev.model.AppSettings
import ru.fet1sov.akinorev.model.AppTheme

class SharedPreferencesAppSettingsRepository(
    context: Context,
) : AppSettingsRepository {
    private val preferences = context.applicationContext
        .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    private val mutableSettings = MutableStateFlow(read())
    override val settings: StateFlow<AppSettings> = mutableSettings.asStateFlow()

    override fun setTheme(theme: AppTheme) {
        preferences.edit().putString(ThemeKey, theme.name).apply()
        mutableSettings.update { it.copy(theme = theme) }
    }

    override fun setLanguage(language: AppLanguage) {
        preferences.edit().putString(LanguageKey, language.name).commit()
        mutableSettings.update { it.copy(language = language) }
    }

    override fun setAnimationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(AnimationsKey, enabled).apply()
        mutableSettings.update { it.copy(animationsEnabled = enabled) }
    }

    private fun read(): AppSettings = AppSettings(
        theme = AppTheme.fromStorage(preferences.getString(ThemeKey, null)),
        language = AppLanguage.fromStorage(preferences.getString(LanguageKey, null)),
        animationsEnabled = preferences.getBoolean(AnimationsKey, true),
    )

    companion object {
        const val PreferencesName = "app_settings"
        private const val ThemeKey = "app_theme"
        private const val LanguageKey = "app_language"
        private const val AnimationsKey = "app_animations"
    }
}
