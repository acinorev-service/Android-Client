package ru.fet1sov.akinorev.data.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.model.TopLevelDestination

class InMemoryNavigationRepository(
    initialBadges: Map<TopLevelDestination, Int> = mapOf(MessagesDestination to 3),
) : NavigationRepository {
    private val badges = MutableStateFlow(initialBadges)

    override fun observeBadges(): Flow<Map<TopLevelDestination, Int>> = badges.asStateFlow()
}
