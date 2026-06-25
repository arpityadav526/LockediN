package com.lockedin.feature.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lockedin.MainActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.bootDataStore by preferencesDataStore(name = "app_prefs")

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        // Read lock state synchronously — this is intentional and acceptable at boot
        val isLocked = runBlocking {
            try {
                context.bootDataStore.data
                    .map { prefs -> prefs[booleanPreferencesKey("IS_LOCKED")] ?: true }
                    .first()
            } catch (e: Exception) {
                true // Default to locked if anything goes wrong
            }
        }

        if (isLocked) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("FROM_BOOT", true)
            }
            context.startActivity(launchIntent)
        }
    }
}
