package ru.fet1sov.akinorev.ui.system

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import ru.fet1sov.akinorev.model.AppLanguage
import java.util.Locale

object AppLocale {
    fun wrap(context: Context, language: AppLanguage): Context {
        if (language.tag.isBlank()) return context

        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
