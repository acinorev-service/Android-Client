package ru.fet1sov.akinorev.di

import ru.fet1sov.akinorev.data.navigation.InMemoryNavigationRepository
import ru.fet1sov.akinorev.data.navigation.NavigationRepository

interface AppContainer {
    val navigationRepository: NavigationRepository
}

class DefaultAppContainer : AppContainer {
    override val navigationRepository: NavigationRepository by lazy {
        InMemoryNavigationRepository()
    }
}
