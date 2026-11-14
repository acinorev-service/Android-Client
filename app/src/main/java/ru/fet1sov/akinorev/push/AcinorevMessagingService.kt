package ru.fet1sov.akinorev.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication

class AcinorevMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val application = applicationContext as? AcinorevApplication ?: return
        application.applicationScope.launch {
            application.container.pushRegistrar.sync(token = token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        NotificationPresenter.show(this, message)
    }
}
