package ru.fet1sov.akinorev.push

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import ru.fet1sov.akinorev.NavigationActivity
import ru.fet1sov.akinorev.R

object NotificationPresenter {
    fun show(context: Context, message: RemoteMessage) {
        if (!canPostNotifications(context)) return

        val data = message.data
        val title = message.notification?.title
            ?: data["title"]?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.app_name)
        val body = message.notification?.body
            ?: data["body"].orEmpty()
        val tag = data["tag"]?.takeIf { it.isNotBlank() }
            ?: data["notification_id"]?.takeIf { it.isNotBlank() }

        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId(tag),
            Intent(context, NavigationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(PushConfig.ExtraOpenNotifications, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, PushConfig.ChannelId)
            .setSmallIcon(R.drawable.ic_push_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(tag, notificationId(tag), notification)
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun notificationId(tag: String?): Int =
        tag?.hashCode() ?: (System.currentTimeMillis() and Int.MAX_VALUE.toLong()).toInt()
}
