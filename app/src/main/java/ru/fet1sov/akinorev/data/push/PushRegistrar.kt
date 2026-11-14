package ru.fet1sov.akinorev.data.push

import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.remote.AuthResult
import java.util.Locale

class PushRegistrar(
    private val store: PushTokenStore,
    private val tokenSource: PushTokenSource,
    private val repository: PushDeviceRepository,
) {
    suspend fun sync(token: String? = null): Boolean {
        val resolved = token ?: tokenSource.currentToken() ?: store.token() ?: return false
        store.save(resolved)

        return repository.register(
            token = resolved,
            appVersion = BuildConfig.VERSION_NAME,
            locale = Locale.getDefault().language,
        ) is AuthResult.Success
    }

    suspend fun unregister() {
        val token = store.token() ?: tokenSource.currentToken() ?: return
        repository.unregister(token)
        store.clear()
    }
}
