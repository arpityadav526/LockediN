package com.lockedin.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.core.utils.SecurityUtils
import com.lockedin.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalStudyMinutes: Int = 0,
    val pomodoroCycles: Int = 0,
    val pinHash: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appPreferences.totalStudyMinutes,
                appPreferences.pomodoroCycles,
                appPreferences.pinHash
            ) { minutes, cycles, hash ->
                HomeUiState(
                    totalStudyMinutes = minutes,
                    pomodoroCycles = cycles,
                    pinHash = hash ?: ""
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun verifyPin(inputPin: String): Boolean {
        return SecurityUtils.verifyPin(inputPin, _uiState.value.pinHash)
    }
}
