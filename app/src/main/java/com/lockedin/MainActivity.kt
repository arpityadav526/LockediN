package com.lockedin

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.lockedin.core.components.PinSetupScreen
import com.lockedin.core.theme.LockediNTheme
import com.lockedin.core.utils.SecurityUtils
import com.lockedin.data.preferences.AppPreferences
import com.lockedin.feature.lock.LockManager
import com.lockedin.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prevent screenshots / screen recordings
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Enter kiosk mode immediately
        lockManager.enterLockMode(this)

        setContent {
            LockediNTheme {
                var isPinSet by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    isPinSet = appPreferences.isPinSet.first() ?: false
                }

                when (isPinSet) {
                    null -> {
                        // Loading state — show nothing while checking
                    }
                    false -> {
                        // First launch — show PIN setup
                        PinSetupScreen(
                            onPinSet = { pin ->
                                lifecycleScope.launch {
                                    val hash = SecurityUtils.hashPin(pin)
                                    appPreferences.setPinHash(hash)
                                    appPreferences.setLocked(true)
                                    isPinSet = true
                                }
                            }
                        )
                    }
                    true -> {
                        // Normal operation
                        AppNavigation(
                            lockManager = lockManager,
                            onUnlock = {
                                lockManager.exitLockMode(this@MainActivity)
                                lifecycleScope.launch {
                                    appPreferences.setLocked(false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-enter lock mode if we somehow got here while still locked
        lifecycleScope.launch {
            if (appPreferences.isLocked.first() == true) {
                lockManager.enterLockMode(this@MainActivity)
            }
        }
    }

    // Block hardware back button while locked
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Intentionally do nothing — back button is disabled in kiosk mode
    }
}
