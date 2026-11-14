package ru.fet1sov.akinorev.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import ru.fet1sov.akinorev.feature.community.CommunityScreen
import ru.fet1sov.akinorev.feature.create.CreateScreen
import ru.fet1sov.akinorev.feature.emojis.EmojiCreateScreen
import ru.fet1sov.akinorev.feature.emojis.EmojisScreen
import ru.fet1sov.akinorev.feature.games.GamesScreen
import ru.fet1sov.akinorev.feature.games.play.PlayScreen
import ru.fet1sov.akinorev.feature.home.HomeScreen
import ru.fet1sov.akinorev.feature.menu.MenuScreen
import ru.fet1sov.akinorev.feature.messages.MessagesScreen
import ru.fet1sov.akinorev.feature.notifications.NotificationsScreen
import ru.fet1sov.akinorev.feature.profile.ProfileScreen
import ru.fet1sov.akinorev.feature.settings.SettingsScreen
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
import ru.fet1sov.akinorev.ui.system.LocalUiAnimationsEnabled

@Composable
fun AcinorevNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val animationsEnabled = LocalUiAnimationsEnabled.current
    val navigationActions = rememberNavigationActions(navController)
    val menuActions = rememberMenuNavigationActions(
        navController = navController,
        navigationActions = navigationActions,
    )

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
        enterTransition = {
            if (animationsEnabled) {
                fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 10 }
            } else {
                EnterTransition.None
            }
        },
        exitTransition = {
            if (animationsEnabled) fadeOut(tween(140)) else ExitTransition.None
        },
        popEnterTransition = {
            if (animationsEnabled) fadeIn(tween(220)) else EnterTransition.None
        },
        popExitTransition = {
            if (animationsEnabled) fadeOut(tween(140)) else ExitTransition.None
        },
    ) {
        composable<HomeDestination> {
            HomeScreen(
                onPlay = { gameId, title ->
                    navController.navigate(PlayDestination(gameId = gameId, title = title))
                },
            )
        }
        composable<MessagesDestination> {
            MessagesScreen()
        }
        composable<CreateDestination> {
            CreateScreen()
        }
        composable<MenuDestination> {
            MenuScreen(
                onOpenEntry = menuActions::open,
                onOpenProfile = menuActions::openProfile,
            )
        }
        composable<GamesDestination> {
            GamesScreen(
                onPlay = { gameId, title ->
                    navController.navigate(PlayDestination(gameId = gameId, title = title))
                },
                onOpenCatalog = {
                    navigationActions.navigateToTopLevelDestination(HomeDestination)
                },
            )
        }
        composable<PlayDestination> { entry ->
            val destination = entry.toRoute<PlayDestination>()
            PlayScreen(
                gameId = destination.gameId,
                title = destination.title,
                onBack = { navController.popBackStack() },
            )
        }
        composable<NotificationsDestination> {
            NotificationsScreen(onOpenProfile = menuActions::openProfile)
        }
        composable<CommunityDestination> {
            CommunityScreen()
        }
        composable<EmojisDestination> {
            EmojisScreen(
                onOpenCreate = menuActions::openEmojiCreate,
                onOpenProfile = menuActions::openProfile,
            )
        }
        composable<EmojiCreateDestination> {
            EmojiCreateScreen(onClose = menuActions::closeEmojiCreate)
        }
        composable<SettingsDestination> {
            SettingsScreen()
        }
        composable<ProfileDestination> { entry ->
            val destination = entry.toRoute<ProfileDestination>()
            ProfileScreen(username = destination.username)
        }
    }
}
