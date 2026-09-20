package com.limani.music.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.limani.music.data.catalog.MusicCatalog
import com.limani.music.data.model.Playlist
import com.limani.music.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface MusicRepository {
    val songsFlow: Flow<List<Song>>
    val favoriteSongsFlow: Flow<List<Song>>
    val playlistsFlow: Flow<List<Playlist>>
    val themeModeFlow: Flow<Int>

    fun toggleFavorite(songId: String)
    fun isFavorite(songId: String): Boolean
    fun createPlaylist(name: String): Playlist
    fun deletePlaylist(playlistId: String)
    fun addSongToPlaylist(playlistId: String, songId: String)
    fun removeSongFromPlaylist(playlistId: String, songId: String)
    fun setThemeMode(mode: Int)
}

class MusicRepositoryImpl(
    context: Context,
    private val catalogSongs: List<Song> = MusicCatalog.getSongs()
) : MusicRepository {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("app_music_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val favoritesSet = MutableStateFlow<Set<String>>(loadFavorites())
    private val playlistsState = MutableStateFlow<List<Playlist>>(loadPlaylists())
    private val themeModeState = MutableStateFlow<Int>(loadThemeMode())

    private val allSongsState = MutableStateFlow<List<Song>>(emptyList())

    override val songsFlow: Flow<List<Song>> = allSongsState.asStateFlow()

    override val favoriteSongsFlow: Flow<List<Song>> = songsFlow.map { songs ->
        songs.filter { it.isFavorite }
    }

    override val playlistsFlow: Flow<List<Playlist>> = playlistsState.asStateFlow()

    override val themeModeFlow: Flow<Int> = themeModeState.asStateFlow()

    init {
        updateSongsList()
    }

    private fun updateSongsList() {
        val currentFavs = favoritesSet.value
        allSongsState.value = catalogSongs.map { song ->
            song.copy(isFavorite = currentFavs.contains(song.id))
        }
    }

    override fun toggleFavorite(songId: String) {
        val current = favoritesSet.value.toMutableSet()
        if (current.contains(songId)) {
            current.remove(songId)
        } else {
            current.add(songId)
        }
        favoritesSet.value = current
        saveFavorites(current)
        updateSongsList()
    }

    override fun isFavorite(songId: String): Boolean {
        return favoritesSet.value.contains(songId)
    }

    override fun createPlaylist(name: String): Playlist {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            songIds = emptyList()
        )
        val updated = playlistsState.value + newPlaylist
        playlistsState.value = updated
        savePlaylists(updated)
        return newPlaylist
    }

    override fun deletePlaylist(playlistId: String) {
        val updated = playlistsState.value.filter { it.id != playlistId }
        playlistsState.value = updated
        savePlaylists(updated)
    }

    override fun addSongToPlaylist(playlistId: String, songId: String) {
        val updated = playlistsState.value.map { playlist ->
            if (playlist.id == playlistId && !playlist.songIds.contains(songId)) {
                playlist.copy(songIds = playlist.songIds + songId)
            } else {
                playlist
            }
        }
        playlistsState.value = updated
        savePlaylists(updated)
    }

    override fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val updated = playlistsState.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(songIds = playlist.songIds.filter { it != songId })
            } else {
                playlist
            }
        }
        playlistsState.value = updated
        savePlaylists(updated)
    }

    override fun setThemeMode(mode: Int) {
        themeModeState.value = mode
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    private fun loadFavorites(): Set<String> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveFavorites(favs: Set<String>) {
        val json = gson.toJson(favs)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    private fun loadPlaylists(): List<Playlist> {
        val json = prefs.getString(KEY_PLAYLISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<Playlist>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        prefs.edit().putString(KEY_PLAYLISTS, json).apply()
    }

    private fun loadThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, MODE_NIGHT_FOLLOW_SYSTEM)
    }

    companion object {
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_PLAYLISTS = "key_playlists"
        private const val KEY_THEME_MODE = "key_theme_mode"

        const val MODE_NIGHT_FOLLOW_SYSTEM = -1
        const val MODE_NIGHT_NO = 1
        const val MODE_NIGHT_YES = 2
    }
}
