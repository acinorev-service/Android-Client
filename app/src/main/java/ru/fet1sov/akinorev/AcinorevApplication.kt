package ru.fet1sov.akinorev

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.fet1sov.akinorev.di.AppContainer
import ru.fet1sov.akinorev.di.DefaultAppContainer
import ru.fet1sov.akinorev.push.PushInitializer

class AcinorevApplication : Application() {
    lateinit var container: AppContainer
        private set

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        PushInitializer.initialize(this)
    }
}
