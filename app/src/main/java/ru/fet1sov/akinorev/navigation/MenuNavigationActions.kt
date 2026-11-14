package ru.fet1sov.akinorev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import ru.fet1sov.akinorev.feature.menu.MenuEntry
import ru.fet1sov.akinorev.model.CommunityDestination
import ru.fet1sov.akinorev.model.EmojiCreateDestination
import ru.fet1sov.akinorev.model.EmojisDestination
import ru.fet1sov.akinorev.model.GamesDestination
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.model.NotificationsDestination
import ru.fet1sov.akinorev.model.ProfileDestination
import ru.fet1sov.akinorev.model.SettingsDestination

class MenuNavigationActions(
    private val navController: NavController,
    private val navigationActions: NavigationActions,
) {
    fun open(entry: MenuEntry) {
        when (entry) {
            MenuEntry.Messages -> navigationActions.navigateToTopLevelDestination(MessagesDestination)
            MenuEntry.Games -> navigationActions.navigateToTopLevelDestination(GamesDestination)
            MenuEntry.Notifications -> navController.navigate(NotificationsDestination)
            MenuEntry.Community -> navController.navigate(CommunityDestination)
            MenuEntry.Emojis -> navController.navigate(EmojisDestination)
            MenuEntry.Settings -> navController.navigate(SettingsDestination)
        }
    }

    fun openProfile(username: String) {
        if (username.isBlank()) return
        navController.navigate(ProfileDestination(username))
    }

    fun openEmojiCreate() {
        navController.navigate(EmojiCreateDestination)
    }

    fun closeEmojiCreate() {
        navController.popBackStack()
    }
}

@Composable
fun rememberMenuNavigationActions(
    navController: NavController,
    navigationActions: NavigationActions,
): MenuNavigationActions = remember(navController, navigationActions) {
    MenuNavigationActions(navController, navigationActions)
}
