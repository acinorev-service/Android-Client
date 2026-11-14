package ru.fet1sov.akinorev.feature.navigation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.fet1sov.akinorev.MainDispatcherRule
import ru.fet1sov.akinorev.data.navigation.NavigationRepository
import ru.fet1sov.akinorev.model.MessagesDestination
import ru.fet1sov.akinorev.model.TopLevelDestination
import ru.fet1sov.akinorev.model.topLevelDestinations

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(mainDispatcher)

    @Test
    fun uiState_exposesDestinationsAndBadges() = runTest(mainDispatcher) {
        val repository = FakeNavigationRepository(mapOf(MessagesDestination to 4))
        val viewModel = NavigationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(topLevelDestinations, state.destinations)
        assertEquals(mapOf(MessagesDestination to 4), state.badges)
    }

    @Test
    fun uiState_updatesWhenBadgesChange() = runTest(mainDispatcher) {
        val repository = FakeNavigationRepository(emptyMap())
        val viewModel = NavigationViewModel(repository)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
        assertEquals(emptyMap<TopLevelDestination, Int>(), viewModel.uiState.value.badges)

        repository.updateBadges(mapOf(MessagesDestination to 7))
        advanceUntilIdle()

        assertEquals(mapOf(MessagesDestination to 7), viewModel.uiState.value.badges)
    }

    private class FakeNavigationRepository(
        initialBadges: Map<TopLevelDestination, Int>,
    ) : NavigationRepository {
        private val badges = MutableStateFlow(initialBadges)

        override fun observeBadges(): Flow<Map<TopLevelDestination, Int>> = badges

        fun updateBadges(value: Map<TopLevelDestination, Int>) {
            badges.value = value
        }
    }
}
