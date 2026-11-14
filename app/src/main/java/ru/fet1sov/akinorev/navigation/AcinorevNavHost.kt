package ru.fet1sov.akinorev.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.fet1sov.akinorev.feature.create.CreateScreen
import ru.fet1sov.akinorev.feature.games.GamesScreen
import ru.fet1sov.akinorev.feature.home.HomeScreen
import ru.fet1sov.akinorev.feature.menu.MenuScreen
import ru.fet1sov.akinorev.feature.messages.MessagesScreen
import ru.fet1sov.akinorev.model.CreateDestination
import ru.fet1sov.akinorev.model.GamesDestination
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.model.MenuDestination
import ru.fet1sov.akinorev.model.MessagesDestination

@Composable
fun AcinorevNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(tween(220)) + slideInVertically(tween(280)) { it / 10 }
        },
        exitTransition = {
            fadeOut(tween(140))
        },
        popEnterTransition = {
            fadeIn(tween(220))
        },
        popExitTransition = {
            fadeOut(tween(140))
        },
    ) {
        composable<HomeDestination> {
            HomeScreen()
        }
        composable<MessagesDestination> {
            MessagesScreen()
        }
        composable<CreateDestination> {
            CreateScreen()
        }
        composable<MenuDestination> {
            MenuScreen()
        }
        composable<GamesDestination> {
            GamesScreen()
        }
    }
}
