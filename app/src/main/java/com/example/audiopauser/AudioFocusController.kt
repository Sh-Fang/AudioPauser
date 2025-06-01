package com.example.audiopauser

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

class AudioFocusController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var originalVolume: Int = 0

    companion object {
        private const val TAG = "AudioFocusController"
    }

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(TAG, "Audio focus changed: $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 我们获得了音频焦点
                Log.d(TAG, "Gained audio focus - other apps should pause")
            }
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 失去音频焦点
                Log.d(TAG, "Lost audio focus")
            }
        }
    }

    fun pauseOtherApps(): Boolean {
        return try {
            // 记录当前音量
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // 创建音频焦点请求
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(focusChangeListener)
                    .build()

                val result = audioManager.requestAudioFocus(focusRequest!!)
                Log.d(TAG, "Audio focus request result: $result")
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                // 兼容旧版本
                @Suppress("DEPRECATION")
                val result = audioManager.requestAudioFocus(
                    focusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request audio focus", e)
            false
        }
    }

    fun releaseAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { request ->
                    audioManager.abandonAudioFocusRequest(request)
                    focusRequest = null
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(focusChangeListener)
            }
            Log.d(TAG, "Audio focus released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release audio focus", e)
        }
    }

}