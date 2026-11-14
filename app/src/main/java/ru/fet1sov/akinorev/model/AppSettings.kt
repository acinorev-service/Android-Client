package ru.fet1sov.akinorev.model

enum class AppTheme {
    System,
    Light,
    Dark,
    ;

    fun isDark(systemInDarkTheme: Boolean): Boolean = when (this) {
        System -> systemInDarkTheme
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromStorage(value: String?): AppTheme = entries.firstOrNull { it.name == value } ?: System
    }
}

enum class AppLanguage(val tag: String) {
    System(""),
    English("en"),
    Russian("ru"),
    ;

    companion object {
        val supported: List<AppLanguage> = listOf(English, Russian)

        fun fromTag(tag: String?): AppLanguage = entries.firstOrNull { it.tag == tag } ?: System

        fun fromStorage(value: String?): AppLanguage = entries.firstOrNull { it.name == value } ?: System
    }
}

data class AppSettings(
    val theme: AppTheme = AppTheme.System,
    val language: AppLanguage = AppLanguage.System,
    val animationsEnabled: Boolean = true,
)
