package com.hadiscare.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hadiscare.app.data.PreferencesManager
import com.hadiscare.app.service.HadiCareService

/**
 * Boot & Package Restart Receiver.
 * Ensures Hadi's Care auto-starts listening on boot if monitoring was previously toggled ON.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val preferencesManager = PreferencesManager(context)
            if (preferencesManager.isMonitoringActive()) {
                HadiCareService.start(context)
            }
        }
    }
}
