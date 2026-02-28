package com.example.reminderht5

import android.content.Context
import org.json.JSONObject
import java.io.File

object BackupManager {

    private const val FILE_NAME = "reminder_backup.json"

    fun backup(context: Context) {
        try {

            val task = TaskPrefs.loadTask(context)

            val json = JSONObject().apply {
                put("name", task.name)
                put("enabled", task.enabled)
                put("year", task.year)
                put("month", task.month)
                put("day", task.day)
                put("hour", task.hour)
                put("minute", task.minute)
                put("repeatValue", task.repeatValue)
                put("repeatUnit", task.repeatUnit)
                put("ringCount", task.ringCount)
                put("ringEveryMinutes", task.ringEveryMinutes)
                put("musicUriStr", task.musicUriStr)
            }

            val file = File(context.filesDir, FILE_NAME)
            file.writeText(json.toString())

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun restore(context: Context) {
        try {

            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) return

            val json = JSONObject(file.readText())

            val task = ReminderTask(
                taskId = 1,
                enabled = json.getBoolean("enabled"),
                name = json.getString("name"),
                year = json.getInt("year"),
                month = json.getInt("month"),
                day = json.getInt("day"),
                hour = json.getInt("hour"),
                minute = json.getInt("minute"),
                repeatValue = json.getInt("repeatValue"),
                repeatUnit = json.getString("repeatUnit"),
                ringCount = json.getInt("ringCount"),
                ringEveryMinutes = json.getInt("ringEveryMinutes"),
                musicUriStr = json.getString("musicUriStr")
            )

            TaskPrefs.saveTask(context, task)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
