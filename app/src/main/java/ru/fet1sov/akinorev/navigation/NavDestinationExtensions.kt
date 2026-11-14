package ru.fet1sov.akinorev.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import ru.fet1sov.akinorev.model.CommunityDestination
import ru.fet1sov.akinorev.model.CreateDestination
import ru.fet1sov.akinorev.model.EmojiCreateDestination
import ru.fet1sov.akinorev.model.EmojisDestination
import ru.fet1sov.akinorev.model.GamesDestination
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.model.MenuDestination
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.model.NotificationsDestination
import ru.fet1sov.akinorev.model.PlayDestination
import ru.fet1sov.akinorev.model.ProfileDestination
import ru.fet1sov.akinorev.model.SettingsDestination
import ru.fet1sov.akinorev.model.TopLevelDestination

fun NavDestination.toTopLevelDestination(): TopLevelDestination? = when {
    hasRoute<HomeDestination>() -> HomeDestination
    hasRoute<MessagesDestination>() -> MessagesDestination
    hasRoute<CreateDestination>() -> CreateDestination
    hasRoute<MenuDestination>() -> MenuDestination
    hasRoute<GamesDestination>() -> GamesDestination
    hasRoute<PlayDestination>() -> null
    hasRoute<NotificationsDestination>() -> MenuDestination
    hasRoute<CommunityDestination>() -> MenuDestination
    hasRoute<EmojisDestination>() -> MenuDestination
    hasRoute<EmojiCreateDestination>() -> MenuDestination
    hasRoute<SettingsDestination>() -> MenuDestination
    hasRoute<ProfileDestination>() -> MenuDestination
    else -> null
}
