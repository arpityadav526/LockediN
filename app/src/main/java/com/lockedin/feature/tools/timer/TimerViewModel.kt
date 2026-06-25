package com.lockedin.feature.tools.timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockedin.R
import com.lockedin.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class TimerState { IDLE, WORKING, SHORT_BREAK, LONG_BREAK }

data class TimerUiState(
    val state: TimerState = TimerState.IDLE,
    val timeRemainingMs: Long = 25 * 60 * 1000L,
    val totalTimeMs: Long = 25 * 60 * 1000L,
    val currentCycle: Int = 1,
    val totalCycles: Int = 4,
    val isRunning: Boolean = false
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    companion object {
        private const val WORK_DURATION_MS = 25 * 60 * 1000L
        private const val SHORT_BREAK_MS = 5 * 60 * 1000L
        private const val LONG_BREAK_MS = 15 * 60 * 1000L
        private const val CHANNEL_ID = "pomodoro_timer"
        private const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    fun startPause() {
        if (_uiState.value.isRunning) {
            pause()
        } else {
            start()
        }
    }

    private fun start() {
        val currentState = _uiState.value
        if (currentState.state == TimerState.IDLE) {
            _uiState.update {
                it.copy(
                    state = TimerState.WORKING,
                    timeRemainingMs = WORK_DURATION_MS,
                    totalTimeMs = WORK_DURATION_MS,
                    isRunning = true
                )
            }
        } else {
            _uiState.update { it.copy(isRunning = true) }
        }
        startCountdown()
    }

    private fun pause() {
        timerJob?.cancel()
        _uiState.update { it.copy(isRunning = false) }
    }

    fun reset() {
        timerJob?.cancel()
        _uiState.update {
            TimerUiState()
        }
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingMs > 0 && _uiState.value.isRunning) {
                delay(100L)
                _uiState.update { it.copy(timeRemainingMs = it.timeRemainingMs - 100L) }
            }

            if (_uiState.value.timeRemainingMs <= 0) {
                onSessionComplete()
            }
        }
    }

    private suspend fun onSessionComplete() {
        val currentState = _uiState.value

        sendNotification(
            when (currentState.state) {
                TimerState.WORKING -> "Focus session complete! Time for a break."
                TimerState.SHORT_BREAK -> "Break over! Time to focus."
                TimerState.LONG_BREAK -> "Long break over! Ready for another cycle?"
                TimerState.IDLE -> ""
            }
        )

        when (currentState.state) {
            TimerState.WORKING -> {
                // Track study time
                appPreferences.addStudyMinutes(25)

                if (currentState.currentCycle >= currentState.totalCycles) {
                    // Long break after 4 cycles
                    appPreferences.incrementPomodoroCycles()
                    _uiState.update {
                        it.copy(
                            state = TimerState.LONG_BREAK,
                            timeRemainingMs = LONG_BREAK_MS,
                            totalTimeMs = LONG_BREAK_MS,
                            isRunning = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            state = TimerState.SHORT_BREAK,
                            timeRemainingMs = SHORT_BREAK_MS,
                            totalTimeMs = SHORT_BREAK_MS,
                            isRunning = true
                        )
                    }
                }
                startCountdown()
            }
            TimerState.SHORT_BREAK -> {
                _uiState.update {
                    it.copy(
                        state = TimerState.WORKING,
                        timeRemainingMs = WORK_DURATION_MS,
                        totalTimeMs = WORK_DURATION_MS,
                        currentCycle = it.currentCycle + 1,
                        isRunning = true
                    )
                }
                startCountdown()
            }
            TimerState.LONG_BREAK -> {
                _uiState.update { TimerUiState() }
            }
            TimerState.IDLE -> {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Timer notifications for study sessions"
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(message: String) {
        if (message.isBlank()) return
        try {
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Pomodoro Timer")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            // Notification permission may not be granted — fail silently
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
