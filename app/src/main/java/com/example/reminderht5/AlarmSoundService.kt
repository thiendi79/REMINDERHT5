package com.example.reminderht5

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AlarmSoundService : Service() {

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_PLAY

        when (action) {
            ACTION_STOP -> {
                stopNow()
                return START_NOT_STICKY
            }

            ACTION_PLAY -> {
                val title = intent?.getStringExtra(EXTRA_TITLE) ?: "REMINDER HT"
                val musicUriStr = intent?.getStringExtra(EXTRA_MUSIC_URI) ?: ""
                val soundUri = if (musicUriStr.isNotBlank()) Uri.parse(musicUriStr)
                else android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                    ?: android.provider.Settings.System.DEFAULT_RINGTONE_URI

                // Foreground notification (nhỏ) để service không bị kill
                startForeground(NOTI_ID, buildNotification(title))

                playSound(soundUri)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopNow()
        super.onDestroy()
    }

    private fun playSound(uri: Uri) {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null

        player = MediaPlayer().apply {
            setDataSource(this@AlarmSoundService, uri)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            prepare()
            start()
        }
    }

    private fun stopNow() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null

        stopForeground(true)
        stopSelf()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                "REMINDER HT",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(title: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingFlags()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText("Đang reo…")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    private fun pendingFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
    }

    companion object {
        private const val CHANNEL_ID = "reminder_ht_alarm_service"
        private const val NOTI_ID = 9001

        private const val ACTION_PLAY = "reminder_ht.action.PLAY"
        private const val ACTION_STOP = "reminder_ht.action.STOP"

        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_MUSIC_URI = "extra_music_uri"

        // ==== 2 HÀM MÀ AlarmReceiver.kt ĐANG GỌI ====

        fun playOnce(ctx: Context, title: String, musicUriStr: String) {
            val i = Intent(ctx, AlarmSoundService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MUSIC_URI, musicUriStr)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            } else {
                ctx.startService(i)
            }
        }

        fun stop(ctx: Context) {
            val i = Intent(ctx, AlarmSoundService::class.java).apply {
                action = ACTION_STOP
            }
            ctx.startService(i)
        }
    }
}
