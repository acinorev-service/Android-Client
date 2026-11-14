package ru.fet1sov.akinorev.feature.navigation

import ru.fet1sov.akinorev.model.TopLevelDestination
import ru.fet1sov.akinorev.model.topLevelDestinations

data class NavigationUiState(
    val destinations: List<TopLevelDestination> = topLevelDestinations,
    val badges: Map<TopLevelDestination, Int> = emptyMap(),
)
