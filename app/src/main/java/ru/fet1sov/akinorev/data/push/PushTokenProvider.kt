package ru.fet1sov.akinorev.data.push

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.fet1sov.akinorev.push.PushConfig
import kotlin.coroutines.resume

interface PushTokenSource {
    suspend fun currentToken(): String?
}

class FirebasePushTokenSource : PushTokenSource {
    override suspend fun currentToken(): String? {
        if (!PushConfig.isConfigured) return null

        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(task.result)
                    } else {
                        continuation.resume(null)
                    }
                }
        }
    }
}
