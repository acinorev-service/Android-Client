package ru.fet1sov.akinorev.data.navigation

import kotlinx.coroutines.flow.Flow
import ru.fet1sov.akinorev.model.TopLevelDestination

interface NavigationRepository {
    fun observeBadges(): Flow<Map<TopLevelDestination, Int>>
}
