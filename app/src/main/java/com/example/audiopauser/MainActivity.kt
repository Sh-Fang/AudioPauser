package com.example.audiopauser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.audiopauser.ui.screen.AudioPauserScreen
import com.example.audiopauser.ui.theme.AudioPauserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPauserTheme {
                AudioPauserScreen()
            }
        }
    }
}
