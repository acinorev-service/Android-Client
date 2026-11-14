package ru.fet1sov.akinorev.push

import ru.fet1sov.akinorev.BuildConfig

object PushConfig {
    const val ChannelId = "acinorev_notifications"
    const val ExtraOpenNotifications = "ru.fet1sov.akinorev.extra.OPEN_NOTIFICATIONS"

    val isConfigured: Boolean
        get() = BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() &&
            BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FIREBASE_SENDER_ID.isNotBlank()
}
