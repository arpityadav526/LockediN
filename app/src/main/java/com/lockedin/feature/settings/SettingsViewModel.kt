package com.lockedin.feature.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.core.utils.FileUtils
import com.lockedin.core.utils.SecurityUtils
import com.lockedin.data.preferences.AppPreferences
import com.lockedin.data.repository.ChatRepository
import com.lockedin.data.repository.FileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val isLocked: Boolean = false,
    val storageUsed: String = "0 B",
    val appVersion: String = "1.0",
    val aiApiKey: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val fileRepository: FileRepository,
    private val chatRepository: ChatRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appPreferences.isLocked.collect { locked ->
                _uiState.update { it.copy(isLocked = locked ?: false) }
            }
        }
        viewModelScope.launch {
            appPreferences.aiApiKey.collect { key ->
                _uiState.update { it.copy(aiApiKey = key ?: "") }
            }
        }
        updateStorageUsed()
    }

    fun lockDevice(activity: Activity) {
        viewModelScope.launch {
            appPreferences.setLocked(true)
        }
    }

    fun changePin(currentPin: String, newPin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val storedHash = appPreferences.pinHash.first() ?: ""
            if (SecurityUtils.verifyPin(currentPin, storedHash)) {
                val newHash = SecurityUtils.hashPin(newPin)
                appPreferences.setPinHash(newHash)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun deleteAllFiles() {
        viewModelScope.launch {
            fileRepository.deleteAllFiles()
            updateStorageUsed()
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun setAiApiKey(key: String) {
        viewModelScope.launch {
            appPreferences.setAiApiKey(key)
        }
    }

    fun openDeviceAdminSettings() {
        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun updateStorageUsed() {
        val homeworkDir = File(context.filesDir, "homework")
        val size = FileUtils.getDirectorySize(homeworkDir)
        _uiState.update { it.copy(storageUsed = FileUtils.formatFileSize(size)) }
    }
}
