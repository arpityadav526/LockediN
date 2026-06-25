package com.lockedin.feature.tools.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DictionaryUiState(
    val searchQuery: String = "",
    val result: DictionaryResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val dictionaryApiService: DictionaryApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchWord(word: String) {
        if (word.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, result = null) }

            try {
                val results = dictionaryApiService.lookupWord(word.trim().lowercase())
                val firstResult = results.firstOrNull()

                if (firstResult != null) {
                    val updatedRecent = (listOf(word.trim()) + _uiState.value.recentSearches)
                        .distinct()
                        .take(20)
                    _uiState.update {
                        it.copy(
                            result = firstResult,
                            isLoading = false,
                            recentSearches = updatedRecent
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No results found")
                    }
                }
            } catch (e: Exception) {
                val errorMsg = when {
                    e.message?.contains("Unable to resolve host") == true ->
                        "Dictionary requires an internet connection"
                    e.message?.contains("404") == true || e.message?.contains("Not Found") == true ->
                        "No results found for \"$word\""
                    else -> "Something went wrong. Please try again."
                }
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }
}
