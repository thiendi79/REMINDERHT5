package com.example.reminderht5
import com.example.reminderht5.ui.theme.ReminderHTTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.reminderht5.ui.theme.ReminderHTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReminderHTTheme {
                Surface {
                    ReminderHomeScreen()
                }
            }
        }
    }
}
