package ru.fet1sov.akinorev.di

import android.content.Context
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.EncryptedSessionStorage
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.navigation.InMemoryNavigationRepository
import ru.fet1sov.akinorev.data.navigation.NavigationRepository
import ru.fet1sov.akinorev.data.remote.NetworkModule

interface AppContainer {
    val navigationRepository: NavigationRepository
    val authRepository: AuthRepository
}

class DefaultAppContainer(
    context: Context,
) : AppContainer {
    private val applicationContext = context.applicationContext

    private val sessionManager by lazy {
        SessionManager(EncryptedSessionStorage(applicationContext))
    }

    private val networkModule by lazy {
        NetworkModule(baseUrl = BuildConfig.API_BASE_URL, sessionManager = sessionManager)
    }

    override val navigationRepository: NavigationRepository by lazy {
        InMemoryNavigationRepository()
    }

    override val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(api = networkModule.api, sessionManager = sessionManager)
    }
}
