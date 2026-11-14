package ru.fet1sov.akinorev.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import ru.fet1sov.akinorev.model.CreateDestination
import ru.fet1sov.akinorev.model.GamesDestination
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.model.MenuDestination
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.model.TopLevelDestination

fun NavDestination.toTopLevelDestination(): TopLevelDestination? = when {
    hasRoute<HomeDestination>() -> HomeDestination
    hasRoute<MessagesDestination>() -> MessagesDestination
    hasRoute<CreateDestination>() -> CreateDestination
    hasRoute<MenuDestination>() -> MenuDestination
    hasRoute<GamesDestination>() -> GamesDestination
    else -> null
}
