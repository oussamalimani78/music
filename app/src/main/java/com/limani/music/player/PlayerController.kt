package com.limani.music.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.limani.music.R
import com.limani.music.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerController(private val context: Context) {

    private val exoPlayer: ExoPlayer by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(playerListener)
            }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private var originalQueue = listOf<Song>()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTicker()
            } else {
                stopProgressTicker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                val dur = exoPlayer.duration
                if (dur > 0) {
                    _durationMs.value = dur
                }
            } else if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
                _playbackPositionMs.value = _durationMs.value
                stopProgressTicker()
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            val currentIdx = exoPlayer.currentMediaItemIndex
            val queueList = _queue.value
            if (currentIdx in queueList.indices) {
                _currentSong.value = queueList[currentIdx]
                _durationMs.value = queueList[currentIdx].durationMs
                _playbackPositionMs.value = 0L
            }
        }
    }

    fun playSong(song: Song, newQueue: List<Song> = listOf(song)) {
        val index = newQueue.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            playQueue(newQueue, index)
        } else {
            playQueue(listOf(song) + newQueue, 0)
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        originalQueue = songs
        val activeQueue = if (_isShuffleEnabled.value) {
            val selected = songs.getOrNull(startIndex)
            val rest = songs.filter { selected == null || it.id != selected.id }.shuffled()
            if (selected != null) listOf(selected) + rest else rest
        } else {
            songs
        }

        _queue.value = activeQueue

        val mediaItems = activeQueue.map { createMediaItem(it) }
        val startIdx = if (_isShuffleEnabled.value && startIndex in songs.indices) 0 else startIndex.coerceIn(activeQueue.indices)

        exoPlayer.setMediaItems(mediaItems, startIdx, 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        _currentSong.value = activeQueue.getOrNull(startIdx)
        _durationMs.value = _currentSong.value?.durationMs ?: 0L
        _playbackPositionMs.value = 0L
    }

    fun togglePlayPause() {
        if (_currentSong.value == null && _queue.value.isNotEmpty()) {
            playQueue(_queue.value, 0)
            return
        }
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                exoPlayer.seekTo(0)
            }
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == Player.REPEAT_MODE_ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000L) {
            exoPlayer.seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else {
            exoPlayer.seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val newShuffle = !_isShuffleEnabled.value
        _isShuffleEnabled.value = newShuffle

        val current = _currentSong.value
        if (current != null && originalQueue.isNotEmpty()) {
            val newQueue = if (newShuffle) {
                listOf(current) + originalQueue.filter { it.id != current.id }.shuffled()
            } else {
                originalQueue
            }
            _queue.value = newQueue
            val mediaItems = newQueue.map { createMediaItem(it) }
            val currentPos = exoPlayer.currentPosition
            val isPlayingNow = exoPlayer.isPlaying
            val newIdx = newQueue.indexOfFirst { it.id == current.id }.coerceAtLeast(0)

            exoPlayer.setMediaItems(mediaItems, newIdx, currentPos)
            exoPlayer.prepare()
            if (isPlayingNow) {
                exoPlayer.play()
            }
        }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        exoPlayer.repeatMode = nextMode
    }

    private fun createMediaItem(song: Song): MediaItem {
        val uri = Uri.parse("android.resource://${context.packageName}/${song.rawResId}")
        val coverRes = song.coverResId.takeIf { it != null && it != 0 } ?: R.drawable.default_album_art
        val artworkUri = Uri.parse("android.resource://${context.packageName}/$coverRes")

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(artworkUri)
            .build()

        return MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(uri)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch {
            while (true) {
                val pos = exoPlayer.currentPosition
                if (pos >= 0) {
                    _playbackPositionMs.value = pos
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTicker()
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
