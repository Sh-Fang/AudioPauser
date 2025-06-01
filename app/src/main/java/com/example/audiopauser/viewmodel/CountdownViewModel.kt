package com.example.audiopauser.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audiopauser.model.AudioFocusController
import com.example.audiopauser.service.CountdownService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext

    private val _statusMessage = MutableStateFlow("⏳ 等待中...")
    val statusMessage: StateFlow<String> = _statusMessage

    var hours = MutableStateFlow(0)
    var minutes = MutableStateFlow(0)
    var seconds = MutableStateFlow(10)

    private var service: CountdownService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val countdownBinder = binder as? CountdownService.CountdownBinder
            service = countdownBinder?.getService()
            val flow = countdownBinder?.getTimeFlow()

            // 开始监听倒计时
            flow?.let {
                viewModelScope.launch {
                    it.collect { sec ->
                        val h = sec / 3600
                        val m = (sec % 3600) / 60
                        val s = sec % 60
                        _statusMessage.value = String.format("⏳ 剩余时间：%02d:%02d:%02d", h, m, s)
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isBound = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCountdown() {
        val totalSec = hours.value * 3600 + minutes.value * 60 + seconds.value
        if (totalSec > 0) {
            val intent = Intent(context, CountdownService::class.java)
            intent.putExtra("duration", totalSec)
            ContextCompat.startForegroundService(context, intent)
            bindToService()
        } else {
            _statusMessage.value = "⚠️ 请选择有效时间"
        }
    }

    private fun bindToService() {
        if (!isBound) {
            val intent = Intent(context, CountdownService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }
}

