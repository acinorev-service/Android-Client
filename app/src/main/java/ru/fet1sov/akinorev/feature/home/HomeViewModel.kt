package ru.fet1sov.akinorev.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.catalog.CatalogRepository
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.model.CatalogGame
import ru.fet1sov.akinorev.model.CatalogGenre

data class GameCardUi(
    val id: String,
    val title: String,
    val bannerPath: String,
    val iconPath: String,
    val genreSlug: String,
    val authorUsername: String,
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isUnavailable: Boolean = false,
    val games: List<GameCardUi> = emptyList(),
    val genres: List<String> = FallbackGenres,
    val selectedGenre: String = "",
    val isSearchOpen: Boolean = false,
) {
    val isFilteredEmpty: Boolean
        get() = !isLoading && !isUnavailable && games.isEmpty()
}

class HomeViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    private var allGames: List<CatalogGame> = emptyList()
    private var loaded = false

    init {
        load()
    }

    fun refresh() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing) return
        load(force = true)
    }

    fun retry() = refresh()

    fun openSearch() {
        mutableUiState.update { it.copy(isSearchOpen = true, selectedGenre = "") }
        applyFilters()
    }

    fun closeSearch() {
        mutableUiState.update { it.copy(isSearchOpen = false) }
        applyFilters()
    }

    fun toggleGenre(slug: String) {
        mutableUiState.update {
            it.copy(selectedGenre = if (it.selectedGenre == slug) "" else slug)
        }
        applyFilters()
    }

    fun clearFilters() {
        mutableUiState.update { it.copy(selectedGenre = "") }
        applyFilters()
    }

    private fun load(force: Boolean = false) {
        if (loaded && !force) return

        viewModelScope.launch {
            val hasGames = allGames.isNotEmpty()
            mutableUiState.update {
                it.copy(
                    isLoading = !hasGames,
                    isRefreshing = hasGames,
                    isUnavailable = false,
                )
            }

            coroutineScope {
                val gamesDeferred = async { catalogRepository.fetchGames() }
                val genresDeferred = async { catalogRepository.fetchGenres() }

                when (val gamesResult = gamesDeferred.await()) {
                    is AuthResult.Success -> {
                        allGames = gamesResult.value
                        loaded = true
                    }

                    is AuthResult.Failure -> {
                        mutableUiState.update {
                            it.copy(isUnavailable = allGames.isEmpty())
                        }
                    }
                }

                when (val genresResult = genresDeferred.await()) {
                    is AuthResult.Success -> applyGenres(genresResult.value)
                    is AuthResult.Failure -> applyGenres(emptyList())
                }
            }

            mutableUiState.update {
                it.copy(isLoading = false, isRefreshing = false)
            }
            applyFilters()
        }
    }

    private fun applyGenres(genres: List<CatalogGenre>) {
        val slugs = genres.map(CatalogGenre::slug).filter(String::isNotBlank)
        mutableUiState.update {
            it.copy(genres = slugs.ifEmpty { FallbackGenres })
        }
    }

    private fun applyFilters() {
        val genre = mutableUiState.value.selectedGenre

        val filtered = allGames.asSequence()
            .filter { genre.isEmpty() || it.genreSlug == genre }
            .map(CatalogGame::toCardUi)
            .toList()

        mutableUiState.update { it.copy(games = filtered) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                HomeViewModel(catalogRepository = application.container.catalogRepository)
            }
        }
    }
}

internal fun CatalogGame.toCardUi(): GameCardUi = GameCardUi(
    id = id,
    title = title,
    bannerPath = bannerPath,
    iconPath = iconPath,
    genreSlug = genreSlug,
    authorUsername = authorUsername,
)

internal val FallbackGenres = listOf("rpg", "shooter", "sandbox", "roleplay", "arcade", "racing", "horror")
