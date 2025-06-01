package com.example.audiopauser.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiopauser.model.AudioFocusController
import com.example.audiopauser.service.CountdownService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val audioFocusController = AudioFocusController(context)

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    var hours = MutableStateFlow(0)
    var minutes = MutableStateFlow(0)
    var seconds = MutableStateFlow(10)

    fun startCountdown() {
        val totalSeconds = hours.value * 3600 + minutes.value * 60 + seconds.value
        if (totalSeconds > 0) {
            viewModelScope.launch {
                val gotFocus = audioFocusController.pauseOtherApps()
                val intent = Intent(context, CountdownService::class.java)
                intent.putExtra("duration", totalSeconds)
                context.startForegroundService(intent)
                _statusMessage.value = if (gotFocus) {
                    "⏳ 倒计时已开始，音频焦点已请求"
                } else {
                    "⏳ 倒计时已开始，但请求音频焦点失败"
                }
            }
        } else {
            _statusMessage.value = "⚠️ 请选择有效时间"
        }
    }

    fun releaseFocus() {
        audioFocusController.releaseAudioFocus()
    }
}
