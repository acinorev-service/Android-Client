package ru.fet1sov.akinorev.di

import android.content.Context
import ru.fet1sov.akinorev.BuildConfig
import ru.fet1sov.akinorev.data.auth.AuthRepository
import ru.fet1sov.akinorev.data.auth.DefaultAuthRepository
import ru.fet1sov.akinorev.data.auth.EncryptedSessionStorage
import ru.fet1sov.akinorev.data.auth.SessionManager
import ru.fet1sov.akinorev.data.catalog.CatalogRepository
import ru.fet1sov.akinorev.data.catalog.DefaultCatalogRepository
import ru.fet1sov.akinorev.data.emojis.ContentResolverEmojiFileSource
import ru.fet1sov.akinorev.data.emojis.DefaultEmojisRepository
import ru.fet1sov.akinorev.data.emojis.EmojiFileSource
import ru.fet1sov.akinorev.data.emojis.EmojisRepository
import ru.fet1sov.akinorev.data.library.DefaultLibraryRepository
import ru.fet1sov.akinorev.data.library.LibraryRepository
import ru.fet1sov.akinorev.data.navigation.InMemoryNavigationRepository
import ru.fet1sov.akinorev.data.navigation.NavigationRepository
import ru.fet1sov.akinorev.data.notifications.DefaultNotificationPreferencesRepository
import ru.fet1sov.akinorev.data.notifications.DefaultNotificationsRepository
import ru.fet1sov.akinorev.data.notifications.NotificationPreferencesRepository
import ru.fet1sov.akinorev.data.notifications.NotificationsRepository
import ru.fet1sov.akinorev.data.play.DefaultPlaySessionRepository
import ru.fet1sov.akinorev.data.play.PlaySessionRepository
import ru.fet1sov.akinorev.data.push.DefaultPushDeviceRepository
import ru.fet1sov.akinorev.data.push.FirebasePushTokenSource
import ru.fet1sov.akinorev.data.push.PushRegistrar
import ru.fet1sov.akinorev.data.push.PushTokenStore
import ru.fet1sov.akinorev.data.push.SharedPreferencesPushTokenStore
import ru.fet1sov.akinorev.data.realtime.DefaultRealtimeSocket
import ru.fet1sov.akinorev.data.realtime.RealtimeSocket
import ru.fet1sov.akinorev.data.remote.ApiClient
import ru.fet1sov.akinorev.data.remote.NetworkModule
import ru.fet1sov.akinorev.data.search.DefaultSearchRepository
import ru.fet1sov.akinorev.data.search.SearchRepository
import ru.fet1sov.akinorev.data.settings.AppSettingsRepository
import ru.fet1sov.akinorev.data.settings.SharedPreferencesAppSettingsRepository

interface AppContainer {
    val navigationRepository: NavigationRepository
    val authRepository: AuthRepository
    val notificationPreferencesRepository: NotificationPreferencesRepository
    val notificationsRepository: NotificationsRepository
    val emojisRepository: EmojisRepository
    val emojiFileSource: EmojiFileSource
    val libraryRepository: LibraryRepository
    val catalogRepository: CatalogRepository
    val playSessionRepository: PlaySessionRepository
    val searchRepository: SearchRepository
    val appSettingsRepository: AppSettingsRepository
    val realtimeSocket: RealtimeSocket
    val pushRegistrar: PushRegistrar
}

class DefaultAppContainer(
    context: Context,
) : AppContainer {
    private val applicationContext = context.applicationContext

    private val sessionManager by lazy {
        SessionManager(EncryptedSessionStorage(applicationContext))
    }

    private val networkModule by lazy {
        NetworkModule(
            baseUrl = BuildConfig.API_BASE_URL,
            cdnBaseUrl = BuildConfig.CDN_BASE_URL,
            sessionManager = sessionManager,
        )
    }

    private val pushTokenStore: PushTokenStore by lazy {
        SharedPreferencesPushTokenStore(applicationContext)
    }

    private val apiClient by lazy {
        ApiClient(api = networkModule.api, sessionManager = sessionManager)
    }

    override val navigationRepository: NavigationRepository by lazy {
        InMemoryNavigationRepository()
    }

    override val authRepository: AuthRepository by lazy {
        DefaultAuthRepository(
            api = networkModule.api,
            sessionManager = sessionManager,
            client = apiClient,
            onLogout = { runCatching { pushRegistrar.unregister() } },
        )
    }

    override val notificationPreferencesRepository: NotificationPreferencesRepository by lazy {
        DefaultNotificationPreferencesRepository(
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val notificationsRepository: NotificationsRepository by lazy {
        DefaultNotificationsRepository(
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val emojisRepository: EmojisRepository by lazy {
        DefaultEmojisRepository(
            api = networkModule.api,
            cdnApi = networkModule.cdnApi,
            client = apiClient,
        )
    }

    override val emojiFileSource: EmojiFileSource by lazy {
        ContentResolverEmojiFileSource(applicationContext)
    }

    override val libraryRepository: LibraryRepository by lazy {
        DefaultLibraryRepository(
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val catalogRepository: CatalogRepository by lazy {
        DefaultCatalogRepository(
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val playSessionRepository: PlaySessionRepository by lazy {
        DefaultPlaySessionRepository(
            socket = realtimeSocket,
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val searchRepository: SearchRepository by lazy {
        DefaultSearchRepository(
            api = networkModule.api,
            client = apiClient,
        )
    }

    override val appSettingsRepository: AppSettingsRepository by lazy {
        SharedPreferencesAppSettingsRepository(applicationContext)
    }

    override val realtimeSocket: RealtimeSocket by lazy {
        DefaultRealtimeSocket(
            sessionManager = sessionManager,
            authRepository = authRepository,
        )
    }

    override val pushRegistrar: PushRegistrar by lazy {
        PushRegistrar(
            store = pushTokenStore,
            tokenSource = FirebasePushTokenSource(),
            repository = DefaultPushDeviceRepository(
                api = networkModule.api,
                client = apiClient,
            ),
        )
    }
}
