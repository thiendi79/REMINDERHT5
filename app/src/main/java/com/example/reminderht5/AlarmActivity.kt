package com.example.reminderht5

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class AlarmActivity : ComponentActivity() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var vibrating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // bật màn hình + hiện trên lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "BÁO THỨC"
        val musicUriStr = intent.getStringExtra(EXTRA_MUSIC_URI) ?: ""
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)

        startAlarmSound(musicUriStr)
        startVibrate()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AlarmUi(
                        title = title,
                        onPause = {
                            // TẠM DỪNG: chỉ tắt tiếng/rung hiện tại, KHÔNG huỷ lịch nhắc
                            stopEverything()
                            finish()
                        },
                        onStopForever = {
                            // DỪNG HẲN: tắt + huỷ lịch + disable task
                            stopEverything()
                            if (taskId > 0) {
                                try {
                                    TaskPrefs.setEnabled(this@AlarmActivity, false)
                                    AlarmScheduler.cancelAll(this@AlarmActivity, taskId)
                                } catch (_: Exception) {
                                }
                            }
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }

    private fun startAlarmSound(musicUriStr: String) {
        val uri = if (musicUriStr.isNotBlank()) {
            android.net.Uri.parse(musicUriStr)
        } else {
            android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                ?: android.provider.Settings.System.DEFAULT_RINGTONE_URI
        }

        try {
            player = MediaPlayer().apply {
                setDataSource(this@AlarmActivity, uri)
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
        } catch (_: Exception) {
            // nếu uri lỗi thì fallback sang chuông mặc định
            try {
                val fallback = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
                    ?: android.provider.Settings.System.DEFAULT_RINGTONE_URI
                player = MediaPlayer().apply {
                    setDataSource(this@AlarmActivity, fallback)
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
            } catch (_: Exception) {
            }
        }
    }

    private fun startVibrate() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator?.hasVibrator() != true) return

        vibrating = true
        val pattern = longArrayOf(0, 800, 400, 800, 400, 800) // rung-nghỉ lặp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopEverything() {
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null

        if (vibrating) {
            try {
                vibrator?.cancel()
            } catch (_: Exception) {
            }
            vibrating = false
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_MUSIC_URI = "extra_music_uri"
        private const val EXTRA_TASK_ID = "extra_task_id"

        fun start(context: Context, title: String, musicUriStr: String?, taskId: Int) {
            val i = Intent(context, AlarmActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MUSIC_URI, musicUriStr ?: "")
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startActivity(i)
        }
    }
}

@Composable
private fun AlarmUi(
    title: String,
    onPause: () -> Unit,
    onStopForever: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text(text = "Đang reo…", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(28.dp))

        // TẠM DỪNG (không huỷ lịch)
        Button(
            onClick = onPause,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("TẠM DỪNG")
        }

        Spacer(Modifier.height(12.dp))

        // DỪNG HẲN (huỷ lịch)
        Button(
            onClick = onStopForever,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("DỪNG HẲN")
        }
    }
}
