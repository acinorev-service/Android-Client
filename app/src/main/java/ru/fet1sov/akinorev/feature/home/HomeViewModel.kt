package ru.fet1sov.akinorev.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
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
import ru.fet1sov.akinorev.model.PlatformMobile

data class GameCardUi(
    val id: String,
    val title: String,
    val bannerPath: String,
    val iconPath: String,
    val genreSlug: String,
    val genreTitle: String,
    val authorUsername: String,
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isUnavailable: Boolean = false,
    val games: List<GameCardUi> = emptyList(),
    val genres: List<String> = FallbackGenres,
    val selectedGenre: String = "",
    val isSearchOpen: Boolean = false,
    val hasMore: Boolean = false,
) {
    val isFilteredEmpty: Boolean
        get() = !isLoading && !isUnavailable && games.isEmpty()
}

class HomeViewModel(
    private val catalogRepository: CatalogRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    private val loadedGames = mutableListOf<CatalogGame>()
    private var offset = 0
    private var total = 0
    private var loadJob: Job? = null
    private var genresLoaded = false

    init {
        loadGenres()
        loadPage(reset = true)
    }

    fun refresh() {
        val state = mutableUiState.value
        if (state.isLoading || state.isRefreshing) return
        loadPage(reset = true, force = true, keepContent = true)
    }

    fun retry() = refresh()

    fun loadMore() {
        val state = mutableUiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        loadPage(reset = false)
    }

    fun openSearch() {
        val hadGenre = mutableUiState.value.selectedGenre.isNotEmpty()
        mutableUiState.update { it.copy(isSearchOpen = true, selectedGenre = "") }
        if (hadGenre) {
            loadPage(reset = true, force = true)
        }
    }

    fun closeSearch() {
        mutableUiState.update { it.copy(isSearchOpen = false) }
    }

    fun toggleGenre(slug: String) {
        val next = if (mutableUiState.value.selectedGenre == slug) "" else slug
        if (next == mutableUiState.value.selectedGenre) return
        mutableUiState.update { it.copy(selectedGenre = next) }
        loadPage(reset = true, force = true)
    }

    fun clearFilters() {
        if (mutableUiState.value.selectedGenre.isEmpty()) return
        mutableUiState.update { it.copy(selectedGenre = "") }
        loadPage(reset = true, force = true)
    }

    private fun loadGenres() {
        if (genresLoaded) return
        viewModelScope.launch {
            when (val result = catalogRepository.fetchGenres()) {
                is AuthResult.Success -> {
                    val slugs = result.value.map(CatalogGenre::slug).filter(String::isNotBlank)
                    mutableUiState.update {
                        it.copy(genres = slugs.ifEmpty { FallbackGenres })
                    }
                    genresLoaded = true
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(genres = FallbackGenres)
                }
            }
        }
    }

    private fun loadPage(
        reset: Boolean,
        force: Boolean = false,
        keepContent: Boolean = false,
    ) {
        if (loadJob?.isActive == true && !force) return
        loadJob?.cancel()

        if (reset) {
            offset = 0
            total = 0
            if (keepContent && loadedGames.isNotEmpty()) {
                mutableUiState.update {
                    it.copy(
                        isRefreshing = true,
                        isLoadingMore = false,
                        isUnavailable = false,
                    )
                }
            } else {
                loadedGames.clear()
                mutableUiState.update {
                    it.copy(
                        isLoading = true,
                        isRefreshing = false,
                        isLoadingMore = false,
                        isUnavailable = false,
                        games = emptyList(),
                        hasMore = false,
                    )
                }
            }
        } else {
            mutableUiState.update { it.copy(isLoadingMore = true) }
        }

        val genre = mutableUiState.value.selectedGenre
        loadJob = viewModelScope.launch {
            when (val result = catalogRepository.fetchGames(
                limit = PageSize,
                offset = offset,
                genre = genre,
                platform = PlatformMobile,
            )) {
                is AuthResult.Success -> {
                    val page = result.value
                    if (reset) {
                        loadedGames.clear()
                        loadedGames += page.games
                        offset = page.games.size
                    } else {
                        val fresh = page.games.filter { game -> loadedGames.none { it.id == game.id } }
                        loadedGames += fresh
                        offset += page.games.size
                    }
                    total = maxOf(page.total, loadedGames.size)
                    val hasMore = page.games.isNotEmpty() && loadedGames.size < total

                    mutableUiState.update {
                        it.copy(
                            games = loadedGames.map(CatalogGame::toCardUi),
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            isUnavailable = false,
                            hasMore = hasMore,
                        )
                    }
                }

                is AuthResult.Failure -> mutableUiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        isUnavailable = loadedGames.isEmpty(),
                    )
                }
            }
        }
    }

    companion object {
        const val PageSize = 6

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
    genreTitle = genreTitle,
    authorUsername = authorUsername,
)

internal val FallbackGenres = listOf("rpg", "shooter", "sandbox", "roleplay", "arcade", "racing", "horror")
