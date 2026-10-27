package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AdhanPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val title: String = "",
    val error: String? = null
)

class AdhanPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(AdhanPlaybackState())
    val playbackState: StateFlow<AdhanPlaybackState> = _playbackState.asStateFlow()

    // Authentic Makkah / Madinah Adhan CDN audio URL
    private val adhanUrl = "https://cdn.islamic.network/media/audio/adhan/1.mp3"

    fun testAdhanSound() {
        if (_playbackState.value.isPlaying) {
            stop()
            return
        }

        try {
            stop()
            _playbackState.value = AdhanPlaybackState(
                isPlaying = false,
                isLoading = true,
                title = "Memutar Uji Suara Adzan...",
                error = null
            )

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setOnPreparedListener { mp ->
                    _playbackState.value = AdhanPlaybackState(
                        isPlaying = true,
                        isLoading = false,
                        title = "Suara Adzan Merdu (Uji Coba)",
                        error = null
                    )
                    mp.start()
                }
                setOnCompletionListener {
                    stop()
                }
                setOnErrorListener { _, what, extra ->
                    Log.w("AdhanPlayer", "Streaming failed (what=$what, extra=$extra), playing system notification tone")
                    playSystemFallbackTone()
                    true
                }
            }
            mediaPlayer = player
            player.setDataSource(adhanUrl)
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("AdhanPlayer", "Failed to start streaming adhan", e)
            playSystemFallbackTone()
        }
    }

    private fun playSystemFallbackTone() {
        try {
            stop()
            val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val player = MediaPlayer.create(context, notificationUri)?.apply {
                setOnCompletionListener { stop() }
                start()
            }
            mediaPlayer = player
            _playbackState.value = AdhanPlaybackState(
                isPlaying = true,
                isLoading = false,
                title = "Nada Pengingat Standar",
                error = null
            )
        } catch (e: Exception) {
            _playbackState.value = AdhanPlaybackState(
                isPlaying = false,
                isLoading = false,
                title = "",
                error = "Tidak dapat memutar suara notifikasi: ${e.localizedMessage}"
            )
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e("AdhanPlayer", "Error releasing player", e)
        } finally {
            mediaPlayer = null
            _playbackState.value = AdhanPlaybackState()
        }
    }

    fun release() {
        stop()
    }
}
