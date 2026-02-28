package com.example.reminderht5

import android.content.Context

object TaskPrefs {
    private const val PREF = "reminder_prefs"

    private const val KEY_ENABLED = "enabled"
    private const val KEY_ID = "task_id"
    private const val KEY_NAME = "name"

    private const val KEY_YEAR = "year"
    private const val KEY_MONTH = "month"
    private const val KEY_DAY = "day"
    private const val KEY_HOUR = "hour"
    private const val KEY_MIN = "minute"

    private const val KEY_REPEAT_VALUE = "repeat_value"
    private const val KEY_REPEAT_UNIT = "repeat_unit"

    private const val KEY_RING_COUNT = "ring_count"
    private const val KEY_RING_EVERY = "ring_every"

    private const val KEY_MUSIC_URI = "music_uri"

    fun saveTask(ctx: Context, t: ReminderTask) {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit()
            .putBoolean(KEY_ENABLED, t.enabled)
            .putInt(KEY_ID, t.taskId)
            .putString(KEY_NAME, t.name)

            .putInt(KEY_YEAR, t.year)
            .putInt(KEY_MONTH, t.month)
            .putInt(KEY_DAY, t.day)
            .putInt(KEY_HOUR, t.hour)
            .putInt(KEY_MIN, t.minute)

            .putInt(KEY_REPEAT_VALUE, t.repeatValue)
            .putString(KEY_REPEAT_UNIT, t.repeatUnit)

            .putInt(KEY_RING_COUNT, t.ringCount)
            .putInt(KEY_RING_EVERY, t.ringEveryMinutes)

            .putString(KEY_MUSIC_URI, t.musicUriStr)
            .apply()
    }

    fun loadTask(ctx: Context): ReminderTask {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

        return ReminderTask(
            taskId = sp.getInt(KEY_ID, 1),
            enabled = sp.getBoolean(KEY_ENABLED, false),

            name = sp.getString(KEY_NAME, "Nhiệm vụ") ?: "Nhiệm vụ",

            year = sp.getInt(KEY_YEAR, 2026),
            month = sp.getInt(KEY_MONTH, 1),
            day = sp.getInt(KEY_DAY, 1),
            hour = sp.getInt(KEY_HOUR, 7),
            minute = sp.getInt(KEY_MIN, 0),

            repeatValue = sp.getInt(KEY_REPEAT_VALUE, 1),
            repeatUnit = sp.getString(KEY_REPEAT_UNIT, "Tháng") ?: "Tháng",

            ringCount = sp.getInt(KEY_RING_COUNT, 10),
            ringEveryMinutes = sp.getInt(KEY_RING_EVERY, 1),

            musicUriStr = sp.getString(KEY_MUSIC_URI, "") ?: ""
        )
    }

    fun setEnabled(ctx: Context, enabled: Boolean) {
        val cur = loadTask(ctx).copy(enabled = enabled)
        saveTask(ctx, cur)
    }

    fun clear(ctx: Context) {
        val sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().clear().apply()

    }
}
