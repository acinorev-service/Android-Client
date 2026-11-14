package ru.fet1sov.akinorev.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.R

object PushInitializer {
    @Volatile
    private var firebaseApp: FirebaseApp? = null

    fun initialize(context: Context) {
        createChannel(context)
        if (!PushConfig.isConfigured) return

        firebaseApp = runCatching {
            FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID)
                    .build(),
            )
        }.getOrNull()
    }

    fun app(): FirebaseApp? = firebaseApp

    private fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            PushConfig.ChannelId,
            context.getString(R.string.notifications_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notifications_channel_description)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }
}
