package com.example.reminderht5

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmScheduler {

    const val EXTRA_ACTION = "extra_action"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_RING_INDEX = "extra_ring_index"

    const val ACTION_BASE = "ACTION_BASE"   // tới giờ hẹn chính
    const val ACTION_RING = "ACTION_RING"   // các lần nhắc lại (ring)
    const val ACTION_STOP = "ACTION_STOP"   // nút dừng từ notification

    private fun alarmManager(ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun cancelAll(ctx: Context, taskId: Int) {
        val am = alarmManager(ctx)

        // hủy base
        am.cancel(pendingBase(ctx, taskId))

        // hủy tối đa 60 ring (đủ cho demo)
        for (i in 1..60) {
            am.cancel(pendingRing(ctx, taskId, i))
        }
    }

    fun scheduleBase(ctx: Context, task: ReminderTask) {
        val triggerAt = computeFirstTriggerMillis(task)

        val pi = pendingBase(ctx, task.taskId)
        setExactCompat(ctx, triggerAt, pi)
    }

    fun scheduleNextBase(ctx: Context, task: ReminderTask) {
        val now = System.currentTimeMillis()
        val next = computeNextBaseMillis(now, task)
        setExactCompat(ctx, next, pendingBase(ctx, task.taskId))
    }

    fun scheduleRings(ctx: Context, task: ReminderTask) {
        // ringIndex bắt đầu từ 1..ringCount
        val base = System.currentTimeMillis()
        val every = (task.ringEveryMinutes.coerceAtLeast(1)) * 60_000L
        val max = task.ringCount.coerceIn(1, 60)

        for (i in 1..max) {
            val whenMillis = base + (i - 1) * every
            setExactCompat(ctx, whenMillis, pendingRing(ctx, task.taskId, i))
        }
    }

    // ---------------- PendingIntents ----------------

    private fun pendingBase(ctx: Context, taskId: Int): PendingIntent {
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_BASE)
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            ctx,
            10_000 + taskId,
            intent,
            pendingFlags()
        )
    }

    private fun pendingRing(ctx: Context, taskId: Int, ringIndex: Int): PendingIntent {
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_RING)
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_RING_INDEX, ringIndex)
        }
        return PendingIntent.getBroadcast(
            ctx,
            20_000 + taskId * 100 + ringIndex,
            intent,
            pendingFlags()
        )
    }

    fun pendingStop(ctx: Context, taskId: Int): PendingIntent {
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ACTION, ACTION_STOP)
            putExtra(EXTRA_TASK_ID, taskId)
        }
        return PendingIntent.getBroadcast(
            ctx,
            30_000 + taskId,
            intent,
            pendingFlags()
        )
    }

    private fun pendingFlags(): Int {
        val base = PendingIntent.FLAG_UPDATE_CURRENT
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            base or PendingIntent.FLAG_IMMUTABLE
        } else base
    }

    // ---------------- Alarm set exact ----------------

    private fun setExactCompat(ctx: Context, triggerAtMillis: Long, pi: PendingIntent) {
        val am = alarmManager(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            @Suppress("DEPRECATION")
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    // ---------------- Time calc ----------------

    private fun computeFirstTriggerMillis(task: ReminderTask): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, task.year)
            set(Calendar.MONTH, (task.month - 1).coerceIn(0, 11))
            set(Calendar.DAY_OF_MONTH, task.day.coerceIn(1, 31))
            set(Calendar.HOUR_OF_DAY, task.hour.coerceIn(0, 23))
            set(Calendar.MINUTE, task.minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // nếu chọn thời gian đã qua -> dời tới lần lặp tiếp theo
        val now = System.currentTimeMillis()
        var t = cal.timeInMillis
        if (t <= now) {
            t = computeNextBaseMillis(now, task)
        }
        return t
    }

    private fun computeNextBaseMillis(fromMillis: Long, task: ReminderTask): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = fromMillis
            // lấy "h:m" theo task
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, task.hour.coerceIn(0, 23))
            set(Calendar.MINUTE, task.minute.coerceIn(0, 59))
        }

        // nếu vừa set xong mà vẫn <= now thì cộng thêm chu kỳ
        if (cal.timeInMillis <= fromMillis) {
            addRepeat(cal, task.repeatValue, task.repeatUnit)
        } else {
            // vẫn phải đảm bảo đúng chu kỳ nếu user chọn lặp ngày/tháng...:
            // ở đây ta vẫn coi "lần kế" là lần cal hiện tại, không ép về ngày/tháng gốc.
        }
        return cal.timeInMillis
    }

    private fun addRepeat(cal: Calendar, value: Int, unit: String) {
        val v = value.coerceAtLeast(1)
        when (unit) {
            "Giờ" -> cal.add(Calendar.HOUR_OF_DAY, v)
            "Ngày" -> cal.add(Calendar.DAY_OF_YEAR, v)
            "Tuần" -> cal.add(Calendar.WEEK_OF_YEAR, v)
            "Tháng" -> cal.add(Calendar.MONTH, v)
            "Năm" -> cal.add(Calendar.YEAR, v)
            else -> cal.add(Calendar.MONTH, v)
        }
    }
}
