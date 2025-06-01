package com.example.audiopauser.ui.screen

import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audiopauser.ui.component.NumberPickerView
import com.example.audiopauser.viewmodel.CountdownViewModel

@Composable
fun AudioPauserScreen() {
    val viewModel: CountdownViewModel = viewModel()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val hours by viewModel.hours.collectAsState()
    val minutes by viewModel.minutes.collectAsState()
    val seconds by viewModel.seconds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("设置倒计时", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NumberPickerView(value = hours, range = 0..23) {
                viewModel.hours.value = it
            }
            Text(" : ")
            NumberPickerView(value = minutes, range = 0..59) {
                viewModel.minutes.value = it
            }
            Text(" : ")
            NumberPickerView(value = seconds, range = 0..59) {
                viewModel.seconds.value = it
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.startCountdown() },
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


