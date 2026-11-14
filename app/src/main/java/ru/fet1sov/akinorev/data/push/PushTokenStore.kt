package ru.fet1sov.akinorev.data.push

import android.content.Context

interface PushTokenStore {
    fun token(): String?

    fun save(token: String)

    fun clear()
}

class SharedPreferencesPushTokenStore(
    context: Context,
) : PushTokenStore {
    private val preferences = context.applicationContext
        .getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)

    override fun token(): String? = preferences.getString(TokenKey, null)?.takeIf { it.isNotBlank() }

    override fun save(token: String) {
        preferences.edit().putString(TokenKey, token).apply()
    }

    override fun clear() {
        preferences.edit().remove(TokenKey).apply()
    }

    private companion object {
        const val PreferencesName = "push_state"
        const val TokenKey = "fcm_token"
    }
}
