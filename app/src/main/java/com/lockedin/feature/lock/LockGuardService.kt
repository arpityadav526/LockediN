package com.lockedin.feature.lock

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lockedin.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val android.content.Context.guardDataStore by preferencesDataStore(name = "app_prefs")

class LockGuardService : AccessibilityService() {

    override fun onServiceConnected() {
        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        } ?: return
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return

        // Don't redirect if we're already in LockediN
        if (packageName == "com.lockedin") return

        // Allow system UI packages (needed for some device interactions)
        val systemPackages = setOf(
            "com.android.systemui",
            "com.android.incallui",
            "com.android.phone",
            "com.android.server.telecom"
        )
        if (packageName in systemPackages) return

        val isLocked = runBlocking {
            try {
                applicationContext.guardDataStore.data
                    .map { prefs -> prefs[booleanPreferencesKey("IS_LOCKED")] ?: true }
                    .first()
            } catch (e: Exception) {
                true
            }
        }

        if (isLocked) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {
        // Required override — no-op
    }
}
