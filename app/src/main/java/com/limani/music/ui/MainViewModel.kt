package com.limani.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.limani.music.data.model.Playlist
import com.limani.music.data.model.Song
import com.limani.music.data.repository.MusicRepository
import com.limani.music.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModel() {

    val allSongs: StateFlow<List<Song>> = repository.songsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlistsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val themeMode: StateFlow<Int> = repository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, MusicRepositoryImpl_MODE_NIGHT_FOLLOW_SYSTEM)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, searchQuery) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            val q = query.trim().lowercase()
            songs.filter { song ->
                song.title.lowercase().contains(q) ||
                        song.artist.lowercase().contains(q) ||
                        song.album.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerController.playbackPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val isShuffleEnabled: StateFlow<Boolean> = playerController.isShuffleEnabled
    val repeatMode: StateFlow<Int> = playerController.repeatMode
    val queue: StateFlow<List<Song>> = playerController.queue

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(songId: String) {
        repository.toggleFavorite(songId)
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val targetQueue = if (queue.isNotEmpty()) queue else allSongs.value
        playerController.playSong(song, targetQueue)
    }

    fun playPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playerController.playQueue(songs, startIndex)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun skipToNext() {
        playerController.skipToNext()
    }

    fun skipToPrevious() {
        playerController.skipToPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeatMode() {
        playerController.toggleRepeatMode()
    }

    fun createPlaylist(name: String) {
        if (name.isNotBlank()) {
            repository.createPlaylist(name.trim())
        }
    }

    fun deletePlaylist(playlistId: String) {
        repository.deletePlaylist(playlistId)
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        repository.addSongToPlaylist(playlistId, songId)
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        repository.removeSongFromPlaylist(playlistId, songId)
    }

    fun setThemeMode(mode: Int) {
        repository.setThemeMode(mode)
    }

    companion object {
        const val MusicRepositoryImpl_MODE_NIGHT_FOLLOW_SYSTEM = -1
    }
}

class MainViewModelFactory(
    private val repository: MusicRepository,
    private val playerController: PlayerController
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, playerController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: androidx.lifecycle.viewmodel.CreationExtras
    ): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, playerController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
