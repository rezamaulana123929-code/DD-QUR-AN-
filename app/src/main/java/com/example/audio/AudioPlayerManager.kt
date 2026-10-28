package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.model.AyatItem
import com.example.data.model.SurahDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioPlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val surahNumber: Int? = null,
    val surahName: String? = null,
    val verseNumber: Int? = null, // null if playing full surah
    val qariId: String = "05",
    val qariName: String = "",
    val error: String? = null,
    val hasActiveTrack: Boolean = false
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    // Playlist context for continuous verse playback
    private var currentPlaylist: List<AyatItem> = emptyList()
    private var currentPlaylistIndex: Int = -1
    private var currentQariId: String = "05"
    private var currentSurahDetail: SurahDetail? = null
    private var shouldStartOnPrepared: Boolean = true

    private fun getOrCreatePlayer(): MediaPlayer {
        mediaPlayer?.let { return it }
        val player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { mp ->
                if (shouldStartOnPrepared) {
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = true,
                        error = null
                    )
                    mp.start()
                } else {
                    _playbackState.value = _playbackState.value.copy(
                        isLoading = false,
                        isPlaying = false,
                        error = null
                    )
                }
            }
            setOnCompletionListener {
                onTrackFinished()
            }
            setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "MediaPlayer error: what=$what, extra=$extra")
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isLoading = false,
                    error = "Gagal memutar audio (Koneksi terputus atau format tidak didukung)"
                )
                true
            }
        }
        mediaPlayer = player
        return player
    }

    private fun getFolder(qariId: String): String = when (qariId) {
        "01" -> "Abdullah-Al-Juhany"
        "02" -> "Abdul-Muhsin-Al-Qasim"
        "03" -> "Abdurrahman-as-Sudais"
        "04" -> "Ibrahim-Al-Dossari"
        "05" -> "Misyari-Rasyid-Al-Afasi"
        "06" -> "Yasser-Al-Dosari"
        else -> "Misyari-Rasyid-Al-Afasi"
    }

    fun playVerse(
        surahNumber: Int,
        surahName: String,
        ayat: AyatItem,
        qariId: String,
        qariName: String,
        allAyat: List<AyatItem> = emptyList()
    ) {
        val audioUrl = ayat.audio?.get(qariId)
            ?: "https://cdn.equran.id/audio-partial/${getFolder(qariId)}/${String.format("%03d%03d", surahNumber, ayat.nomorAyat)}.mp3"

        currentPlaylist = allAyat
        currentPlaylistIndex = allAyat.indexOfFirst { it.nomorAyat == ayat.nomorAyat }
        currentQariId = qariId
        currentSurahDetail = null

        playUrl(
            url = audioUrl,
            surahNumber = surahNumber,
            surahName = surahName,
            verseNumber = ayat.nomorAyat,
            qariId = qariId,
            qariName = qariName,
            startImmediately = true
        )
    }

    fun playFullSurah(
        surah: SurahDetail,
        qariId: String,
        qariName: String
    ) {
        val audioUrl = surah.audioFull?.get(qariId)
            ?: "https://cdn.equran.id/audio-full/${getFolder(qariId)}/${String.format("%03d", surah.nomor)}.mp3"

        currentPlaylist = emptyList()
        currentPlaylistIndex = -1
        currentQariId = qariId
        currentSurahDetail = surah

        playUrl(
            url = audioUrl,
            surahNumber = surah.nomor,
            surahName = surah.namaLatin,
            verseNumber = null,
            qariId = qariId,
            qariName = qariName,
            startImmediately = true
        )
    }

    fun changeQari(newQariId: String, newQariName: String) {
        if (!_playbackState.value.hasActiveTrack) {
            currentQariId = newQariId
            _playbackState.value = _playbackState.value.copy(qariId = newQariId, qariName = newQariName)
            return
        }

        val wasPlaying = mediaPlayer?.isPlaying == true
        currentQariId = newQariId

        val surahNo = _playbackState.value.surahNumber ?: 1
        val surahName = _playbackState.value.surahName ?: ""
        val verseNo = _playbackState.value.verseNumber

        if (verseNo != null && currentPlaylist.isNotEmpty() && currentPlaylistIndex in currentPlaylist.indices) {
            val ayat = currentPlaylist[currentPlaylistIndex]
            val audioUrl = ayat.audio?.get(newQariId)
                ?: "https://cdn.equran.id/audio-partial/${getFolder(newQariId)}/${String.format("%03d%03d", surahNo, verseNo)}.mp3"

            playUrl(
                url = audioUrl,
                surahNumber = surahNo,
                surahName = surahName,
                verseNumber = verseNo,
                qariId = newQariId,
                qariName = newQariName,
                startImmediately = wasPlaying
            )
        } else if (currentSurahDetail != null) {
            val surah = currentSurahDetail!!
            val audioUrl = surah.audioFull?.get(newQariId)
                ?: "https://cdn.equran.id/audio-full/${getFolder(newQariId)}/${String.format("%03d", surah.nomor)}.mp3"

            playUrl(
                url = audioUrl,
                surahNumber = surah.nomor,
                surahName = surah.namaLatin,
                verseNumber = null,
                qariId = newQariId,
                qariName = newQariName,
                startImmediately = wasPlaying
            )
        }
    }

    private fun playUrl(
        url: String,
        surahNumber: Int,
        surahName: String,
        verseNumber: Int?,
        qariId: String,
        qariName: String,
        startImmediately: Boolean = true
    ) {
        try {
            val player = getOrCreatePlayer()
            player.reset()
            shouldStartOnPrepared = startImmediately

            _playbackState.value = AudioPlaybackState(
                isPlaying = false,
                isLoading = true,
                surahNumber = surahNumber,
                surahName = surahName,
                verseNumber = verseNumber,
                qariId = qariId,
                qariName = qariName,
                error = null,
                hasActiveTrack = true
            )

            player.setDataSource(url)
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error starting playback", e)
            _playbackState.value = _playbackState.value.copy(
                isPlaying = false,
                isLoading = false,
                error = "Gagal memuat audio: ${e.localizedMessage}"
            )
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying && _playbackState.value.hasActiveTrack) {
                it.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
        }
    }

    fun togglePlayPause() {
        if (_playbackState.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error stopping player", e)
        }
        _playbackState.value = AudioPlaybackState()
        currentPlaylist = emptyList()
        currentPlaylistIndex = -1
    }

    fun nextVerse() {
        if (currentPlaylist.isNotEmpty() && currentPlaylistIndex in 0 until (currentPlaylist.size - 1)) {
            val nextIndex = currentPlaylistIndex + 1
            val nextAyat = currentPlaylist[nextIndex]
            val surahNo = _playbackState.value.surahNumber ?: 1
            val surahName = _playbackState.value.surahName ?: ""
            val qariName = _playbackState.value.qariName
            playVerse(surahNo, surahName, nextAyat, currentQariId, qariName, currentPlaylist)
        }
    }

    fun previousVerse() {
        if (currentPlaylist.isNotEmpty() && currentPlaylistIndex > 0) {
            val prevIndex = currentPlaylistIndex - 1
            val prevAyat = currentPlaylist[prevIndex]
            val surahNo = _playbackState.value.surahNumber ?: 1
            val surahName = _playbackState.value.surahName ?: ""
            val qariName = _playbackState.value.qariName
            playVerse(surahNo, surahName, prevAyat, currentQariId, qariName, currentPlaylist)
        }
    }

    private fun onTrackFinished() {
        // If there's a next verse in the playlist, auto-advance!
        if (currentPlaylist.isNotEmpty() && currentPlaylistIndex in 0 until (currentPlaylist.size - 1)) {
            nextVerse()
        } else {
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error releasing player", e)
        }
    }
}
