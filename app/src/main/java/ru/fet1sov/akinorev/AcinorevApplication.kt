package ru.fet1sov.akinorev

import android.app.Application
import ru.fet1sov.akinorev.di.AppContainer
import ru.fet1sov.akinorev.di.DefaultAppContainer

class AcinorevApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
