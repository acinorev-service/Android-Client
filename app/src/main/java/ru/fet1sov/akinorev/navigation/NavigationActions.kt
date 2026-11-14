package ru.fet1sov.akinorev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import ru.fet1sov.akinorev.model.MenuDestination
import ru.fet1sov.akinorev.model.TopLevelDestination

class NavigationActions(
    private val navController: NavController,
) {
    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = destination != MenuDestination
        }
    }
}

@Composable
fun rememberNavigationActions(navController: NavController): NavigationActions =
    remember(navController) {
        NavigationActions(navController)
    }
