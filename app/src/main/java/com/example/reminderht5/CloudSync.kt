package com.example.reminderht5

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore

object CloudSync {

    private fun docPath(uid: String): String = "users/$uid/reminder/task"

    fun uploadTask(
        ctx: Context,
        uid: String,
        onDone: (ok: Boolean, msg: String) -> Unit
    ) {
        val t = TaskPrefs.loadTask(ctx)

        val data = hashMapOf(
            "taskId" to t.taskId,
            "enabled" to t.enabled,
            "name" to t.name,
            "year" to t.year,
            "month" to t.month,
            "day" to t.day,
            "hour" to t.hour,
            "minute" to t.minute,
            "repeatValue" to t.repeatValue,
            "repeatUnit" to t.repeatUnit,
            "ringCount" to t.ringCount,
            "ringEveryMinutes" to t.ringEveryMinutes,
            "musicUriStr" to t.musicUriStr,
            "updatedAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .document(docPath(uid))
            .set(data)
            .addOnSuccessListener { onDone(true, "Đã tải lên cloud") }
            .addOnFailureListener { e -> onDone(false, e.message ?: "Lỗi upload") }
    }

    fun downloadTask(
        ctx: Context,
        uid: String,
        onDone: (ok: Boolean, msg: String) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .document(docPath(uid))
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    onDone(false, "Cloud chưa có dữ liệu")
                    return@addOnSuccessListener
                }

                fun intOf(key: String, def: Int): Int =
                    (snap.getLong(key)?.toInt()) ?: def

                val t = ReminderTask(
                    taskId = intOf("taskId", 1),
                    enabled = snap.getBoolean("enabled") ?: false,
                    name = snap.getString("name") ?: "Nhiệm vụ",
                    year = intOf("year", 2026),
                    month = intOf("month", 1),
                    day = intOf("day", 1),
                    hour = intOf("hour", 7),
                    minute = intOf("minute", 0),
                    repeatValue = intOf("repeatValue", 1),
                    repeatUnit = snap.getString("repeatUnit") ?: "Tháng",
                    ringCount = intOf("ringCount", 10),
                    ringEveryMinutes = intOf("ringEveryMinutes", 1),
                    musicUriStr = snap.getString("musicUriStr") ?: ""
                )

                TaskPrefs.saveTask(ctx, t)
                TaskPrefs.setEnabled(ctx, t.enabled)

                // Nếu task bật thì schedule lại base
                if (t.enabled) {
                    AlarmScheduler.scheduleBase(ctx, t)
                } else {
                    AlarmScheduler.cancelAll(ctx, t.taskId)
                }

                onDone(true, "Đã tải về từ cloud")
            }
            .addOnFailureListener { e ->
                onDone(false, e.message ?: "Lỗi download")
            }
    }
}