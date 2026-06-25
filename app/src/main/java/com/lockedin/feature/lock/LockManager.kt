package com.lockedin.feature.lock

import android.app.Activity
import android.view.WindowManager
import com.lockedin.data.preferences.AppPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor(
    private val appPreferences: AppPreferences
) {
    fun enterLockMode(activity: Activity) {
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            // startLockTask() fails silently on some devices without Device Owner
            // The Accessibility Service acts as fallback
            e.printStackTrace()
        }
        // Keep screen on while locked
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun exitLockMode(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            // May not be in lock task mode
            e.printStackTrace()
        }
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    suspend fun setLocked(isLocked: Boolean) {
        appPreferences.setLocked(isLocked)
    }
}
