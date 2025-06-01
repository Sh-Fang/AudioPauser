package com.example.audiopauser

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat

class CountdownService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = 0
    private var audioFocusController: AudioFocusController? = null

    override fun onCreate() {
        super.onCreate()
        audioFocusController = AudioFocusController(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        remainingSeconds = intent?.getIntExtra("duration", 0) ?: 0

        startForegroundNotification()
        startCountdown()

        return START_NOT_STICKY
    }

    private fun startCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                if (remainingSeconds > 0) {
                    updateNotification(remainingSeconds)
                    remainingSeconds--
                    handler.postDelayed(this, 1000)
                } else {
                    val success = audioFocusController?.pauseOtherApps() ?: false
                    audioFocusController?.releaseAudioFocus()
                    stopForeground(true)
                    stopSelf()
                }
            }
        })
    }

    private fun startForegroundNotification() {
        val channelId = "countdown_channel"
        val channelName = "Countdown Timer"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("倒计时进行中")
            .setContentText("音频将被暂停")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1, notification)
    }

    private fun updateNotification(secondsLeft: Int) {
        val hours = secondsLeft / 3600
        val minutes = (secondsLeft % 3600) / 60
        val seconds = secondsLeft % 60

        val text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val notification = NotificationCompat.Builder(this, "countdown_channel")
            .setContentTitle("倒计时中")
            .setContentText("剩余时间：$text")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
