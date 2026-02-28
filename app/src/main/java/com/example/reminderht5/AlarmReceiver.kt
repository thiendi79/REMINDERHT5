package com.example.reminderht5

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra(AlarmScheduler.EXTRA_ACTION) ?: return
        val taskId = intent.getIntExtra(AlarmScheduler.EXTRA_TASK_ID, 1)
        val ringIndex = intent.getIntExtra(AlarmScheduler.EXTRA_RING_INDEX, 0)

        val task = TaskPrefs.loadTask(context)

        when (action) {
            AlarmScheduler.ACTION_STOP -> {
                Log.d("REMINDER_HT", "STOP pressed")
                AlarmSoundService.stop(context)
                AlarmScheduler.cancelAll(context, taskId)
                TaskPrefs.setEnabled(context, false)
            }

            AlarmScheduler.ACTION_BASE -> {
                if (!task.enabled || task.taskId != taskId) return

                Log.d("REMINDER_HT", "BASE fired music='${task.musicUriStr}'")

                // phát 1 lần ngay lúc hẹn
                AlarmSoundService.playOnce(context, task.name, task.musicUriStr)

                // lên lịch chu kỳ tiếp theo
                AlarmScheduler.scheduleNextBase(context, task)

                // lên lịch các lần nhắc lại (10 lần/1 phút...)
                AlarmScheduler.scheduleRings(context, task)
            }

            AlarmScheduler.ACTION_RING -> {
                if (!task.enabled || task.taskId != taskId) return

                Log.d("REMINDER_HT", "RING #$ringIndex fired")
                AlarmSoundService.playOnce(context, "Nhắc lại: ${task.name}", task.musicUriStr)
            }
        }
    }
}
