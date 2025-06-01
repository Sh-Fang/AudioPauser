package com.example.audiopauser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.NumberPicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.audiopauser.ui.theme.AudioPauserTheme
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AudioPauserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioPauserScreen()
                }
            }
        }
    }

    @Composable
    fun AudioPauserScreen(context: Context = LocalContext.current) {
        var hours by remember { mutableIntStateOf(0) }
        var minutes by remember { mutableIntStateOf(0) }
        var seconds by remember { mutableIntStateOf(10) }
        var statusMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("倒计时设置", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPickerView(value = hours, range = 0..23) { hours = it }
                Text(" : ")
                NumberPickerView(value = minutes, range = 0..59) { minutes = it }
                Text(" : ")
                NumberPickerView(value = seconds, range = 0..59) { seconds = it }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val totalSeconds = hours * 3600 + minutes * 60 + seconds
                    if (totalSeconds > 0) {
                        val intent = Intent(context, CountdownService::class.java)
                        intent.putExtra("duration", totalSeconds)
                        ContextCompat.startForegroundService(context, intent)
                        statusMessage = "⏳ 倒计时已开始，将在后台运行"
                    } else {
                        statusMessage = "⚠️ 请选择有效时间"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("开始倒计时")
            }

            if (statusMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(statusMessage)
            }
        }
    }

    @Composable
    fun NumberPickerView(
        value: Int,
        range: IntRange,
        onValueChange: (Int) -> Unit
    ) {
        AndroidView(
            factory = { context ->
                NumberPicker(context).apply {
                    minValue = range.first
                    maxValue = range.last
                    wrapSelectorWheel = true
                    this.value = value
                    setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }
                }
            },
            update = { picker ->
                if (picker.value != value) {
                    picker.value = value
                }
            },
            modifier = Modifier.size(width = 80.dp, height = 120.dp)
        )
    }
}
