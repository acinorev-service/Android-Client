package ru.fet1sov.akinorev.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.awaitCancellation
import ru.fet1sov.akinorev.feature.navigation.NavigationViewModel
import ru.fet1sov.akinorev.feature.presence.PresenceViewModel
import ru.fet1sov.akinorev.feature.push.PushTokenViewModel
import ru.fet1sov.akinorev.model.HomeDestination
import ru.fet1sov.akinorev.model.NotificationsDestination
import ru.fet1sov.akinorev.push.PushConfig
import ru.fet1sov.akinorev.ui.navigation.NavigationScaffold

@Composable
fun AcinorevApp(
    modifier: Modifier = Modifier,
    startOnNotifications: Boolean = false,
    onStartNavigationConsumed: () -> Unit = {},
    viewModel: NavigationViewModel = viewModel(factory = NavigationViewModel.Factory),
    presenceViewModel: PresenceViewModel = viewModel(factory = PresenceViewModel.Factory),
    pushViewModel: PushTokenViewModel = viewModel(factory = PushTokenViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    LaunchedEffect(lifecycleOwner, presenceViewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            presenceViewModel.start()
            try {
                awaitCancellation()
            } finally {
                presenceViewModel.stop()
            }
        }
    }

    LaunchedEffect(pushViewModel) {
        pushViewModel.refreshToken()
    }

    LaunchedEffect(Unit) {
        if (!PushConfig.isConfigured || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return@LaunchedEffect
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val navController = rememberNavController()
    val navigationActions = rememberNavigationActions(navController)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedDestination = when (val entry = currentBackStackEntry) {
        null -> HomeDestination
        else -> entry.destination.toTopLevelDestination()
    }

    LaunchedEffect(startOnNotifications, navController) {
        if (!startOnNotifications) return@LaunchedEffect
        navController.navigate(NotificationsDestination) {
            launchSingleTop = true
        }
        onStartNavigationConsumed()
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
