package com.lockedin.feature.lock

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.admin.DeviceAdminReceiver as AndroidDeviceAdminReceiver

class DeviceAdminReceiver : AndroidDeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled — now startLockTask() works without user confirmation
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }

    companion object {
        fun getComponentName(context: Context): ComponentName =
            ComponentName(context, DeviceAdminReceiver::class.java)
    }
}
