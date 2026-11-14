package ru.fet1sov.akinorev.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.fet1sov.akinorev.feature.navigation.NavigationViewModel
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.ui.navigation.NavigationScaffold

@Composable
fun AcinorevApp(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = viewModel(factory = NavigationViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navigationActions = rememberNavigationActions(navController)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = when (val entry = currentBackStackEntry) {
        null -> HomeDestination
        else -> entry.destination.toTopLevelDestination()
    }

    NavigationScaffold(
        uiState = uiState,
        selectedDestination = selectedDestination,
        onDestinationSelected = navigationActions::navigateToTopLevelDestination,
        modifier = modifier,
    ) {
        AcinorevNavHost(navController = navController)
    }
}
