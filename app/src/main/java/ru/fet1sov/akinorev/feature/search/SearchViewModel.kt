package ru.fet1sov.akinorev.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fet1sov.akinorev.AcinorevApplication
import ru.fet1sov.akinorev.data.remote.AuthResult
import ru.fet1sov.akinorev.data.search.SearchPageSize
import ru.fet1sov.akinorev.data.search.SearchRepository
import ru.fet1sov.akinorev.model.SearchGroup

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val groups: List<SearchGroup> = emptyList(),
    val total: Int = 0,
    val limit: Int = SearchPageSize,
    val hasError: Boolean = false,
    val emojis: Map<String, String> = emptyMap(),
) {
    val hasResults: Boolean
        get() = groups.isNotEmpty()

    val hasMore: Boolean
        get() = !isSearching && total > limit

    val isEmpty: Boolean
        get() = !isSearching && !hasError && query.isNotBlank() && groups.isEmpty()
}

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = mutableUiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        mutableUiState.update { it.copy(query = value) }
        scheduleSearch()
    }

    fun submit() {
        searchJob?.cancel()
        val query = mutableUiState.value.query.trim()
        if (query.length < MinQueryLength) return
        searchJob = viewModelScope.launch { search(query) }
    }

    fun loadMore() {
        if (!mutableUiState.value.hasMore) return
        mutableUiState.update { it.copy(limit = it.limit + SearchPageSize) }
        submit()
    }

    fun retry() = submit()

    fun clear() {
        searchJob?.cancel()
        searchJob = null
        mutableUiState.value = SearchUiState()
    }

    private fun scheduleSearch() {
        searchJob?.cancel()

        val query = mutableUiState.value.query.trim()
        if (query.length < MinQueryLength) {
            mutableUiState.update {
                it.copy(
                    isSearching = false,
                    groups = emptyList(),
                    total = 0,
                    hasError = false,
                    limit = SearchPageSize,
                )
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(DebounceMillis)
            search(query)
        }
    }

    private suspend fun search(query: String) {
        mutableUiState.update { it.copy(isSearching = true, hasError = false) }

        when (val result = searchRepository.search(query, mutableUiState.value.limit)) {
            is AuthResult.Success -> mutableUiState.update {
                it.copy(
                    isSearching = false,
                    groups = result.value.groups,
                    total = result.value.total,
                    hasError = false,
                    emojis = result.value.emojis,
                )
            }

            is AuthResult.Failure -> mutableUiState.update {
                it.copy(
                    isSearching = false,
                    groups = emptyList(),
                    total = 0,
                    hasError = true,
                    emojis = emptyMap(),
                )
            }
        }
    }

    companion object {
        private const val MinQueryLength = 1
        private const val DebounceMillis = 300L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AcinorevApplication
                SearchViewModel(searchRepository = application.container.searchRepository)
            }
        }
    }
}
