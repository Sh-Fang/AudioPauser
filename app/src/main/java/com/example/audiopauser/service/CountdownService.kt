package com.example.audiopauser.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.audiopauser.R
import com.example.audiopauser.model.AudioFocusController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CountdownService : Service() {
    private val binder = CountdownBinder()

    private val _timeFlow = MutableStateFlow(0)
    val timeFlow: StateFlow<Int> = _timeFlow

    private var totalSeconds = 0
    private var isRunning = false
    private var job: Job? = null

    inner class CountdownBinder : Binder() {
        fun getService(): CountdownService = this@CountdownService
        fun getTimeFlow(): StateFlow<Int> = timeFlow
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        totalSeconds = intent?.getIntExtra("duration", 0) ?: 0
        startForegroundWithNotification()
        startCountdown()
        return START_STICKY
    }


    private fun startCountdown() {
        if (isRunning) return
        isRunning = true
        job = CoroutineScope(Dispatchers.Default).launch {
            var remaining = totalSeconds
            while (remaining >= 0) {
                _timeFlow.value = remaining
                updateNotification(remaining)
                delay(1000)
                remaining--
            }
            // Countdown finished
            stopForeground(STOP_FOREGROUND_REMOVE) // Remove the notification
            stopSelf() // Stop the service
        }
    }

    private fun updateNotification(remainingSeconds: Int) {
        val channelId = "countdown_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("倒计时运行中")
            .setContentText("剩余时间: $remainingSeconds 秒") // Update text with remaining time
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true) // Prevents the notification from making sound/vibrating on update
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            1,
            notification
        )
    }

    // Modify startForegroundWithNotification to call updateNotification initially
    private fun startForegroundWithNotification() {
        val channelId = "countdown_channel"
        val channelName = "倒计时服务"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(chan)
        }

        // Call updateNotification to create the initial notification
        updateNotification(totalSeconds)

        val initialNotification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("倒计时运行中")
            .setContentText("倒计时初始化...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(1, initialNotification)
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}
