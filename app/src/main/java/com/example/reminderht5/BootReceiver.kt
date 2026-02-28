package com.example.reminderht5

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val task = TaskPrefs.loadTask(context)
        if (task.enabled) {
            AlarmScheduler.scheduleBase(context, task)
        }
    }
}
