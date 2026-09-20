package com.limani.music

import com.limani.music.data.model.Playlist
import com.limani.music.data.model.Song
import com.limani.music.data.repository.MusicRepository
import com.limani.music.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

class FakeMusicRepository(
    initialSongs: List<Song> = emptyList()
) : MusicRepository {

    private val songsState = MutableStateFlow(initialSongs)
    private val playlistsState = MutableStateFlow<List<Playlist>>(emptyList())
    private val themeModeState = MutableStateFlow(-1)

    override val songsFlow: Flow<List<Song>> = songsState.asStateFlow()
    override val favoriteSongsFlow: Flow<List<Song>> = songsFlow.map { songs -> songs.filter { it.isFavorite } }
    override val playlistsFlow: Flow<List<Playlist>> = playlistsState.asStateFlow()
    override val themeModeFlow: Flow<Int> = themeModeState.asStateFlow()

    override fun toggleFavorite(songId: String) {
        songsState.value = songsState.value.map { song ->
            if (song.id == songId) {
                song.copy(isFavorite = !song.isFavorite)
            } else {
                song
            }
        }
    }

    override fun isFavorite(songId: String): Boolean {
        return songsState.value.find { it.id == songId }?.isFavorite == true
    }

    override fun createPlaylist(name: String): Playlist {
        val playlist = Playlist(UUID.randomUUID().toString(), name, emptyList())
        playlistsState.value = playlistsState.value + playlist
        return playlist
    }

    override fun deletePlaylist(playlistId: String) {
        playlistsState.value = playlistsState.value.filter { it.id != playlistId }
    }

    override fun addSongToPlaylist(playlistId: String, songId: String) {
        playlistsState.value = playlistsState.value.map { pl ->
            if (pl.id == playlistId && !pl.songIds.contains(songId)) {
                pl.copy(songIds = pl.songIds + songId)
            } else {
                pl
            }
        }
    }

    override fun removeSongFromPlaylist(playlistId: String, songId: String) {
        playlistsState.value = playlistsState.value.map { pl ->
            if (pl.id == playlistId) {
                pl.copy(songIds = pl.songIds.filter { it != songId })
            } else {
                pl
            }
        }
    }

    override fun setThemeMode(mode: Int) {
        themeModeState.value = mode
    }
}

class DummyPlayerController {
    val currentSong = MutableStateFlow<Song?>(null)
    val isPlaying = MutableStateFlow(false)
    val playbackPositionMs = MutableStateFlow(0L)
    val durationMs = MutableStateFlow(0L)
    val isShuffleEnabled = MutableStateFlow(false)
    val repeatMode = MutableStateFlow(0)
    val queue = MutableStateFlow<List<Song>>(emptyList())
}

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val sampleSongs = listOf(
        Song("1", "Acoustic Sun", "Artist One", "Album Alpha", 180000L, 0),
        Song("2", "Electric Night", "Artist Two", "Album Beta", 210000L, 0),
        Song("3", "Jazz Breeze", "Artist One", "Album Gamma", 240000L, 0)
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchFilteringByTitleArtistAlbum() = runTest {
        val repository = FakeMusicRepository(sampleSongs)
        val dummyPlayerController = com.limani.music.player.PlayerController(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
        val viewModel = MainViewModel(repository, dummyPlayerController)

        backgroundScope.launch { viewModel.allSongs.collect {} }
        backgroundScope.launch { viewModel.filteredSongs.collect {} }

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("Acoustic")
        testDispatcher.scheduler.advanceUntilIdle()

        var results = viewModel.filteredSongs.value
        assertEquals(1, results.size)
        assertEquals("Acoustic Sun", results[0].title)

        viewModel.setSearchQuery("Artist One")
        testDispatcher.scheduler.advanceUntilIdle()

        results = viewModel.filteredSongs.value
        assertEquals(2, results.size)
    }

    @Test
    fun testToggleFavoriteInRepositoryAndViewModel() = runTest {
        val repository = FakeMusicRepository(sampleSongs)
        val dummyPlayerController = com.limani.music.player.PlayerController(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
        val viewModel = MainViewModel(repository, dummyPlayerController)

        viewModel.toggleFavorite("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val favs = repository.favoriteSongsFlow.first()
        assertEquals(1, favs.size)
        assertEquals("1", favs[0].id)
        assertTrue(favs[0].isFavorite)

        viewModel.toggleFavorite("1")
        testDispatcher.scheduler.advanceUntilIdle()

        val favsAfter = repository.favoriteSongsFlow.first()
        assertTrue(favsAfter.isEmpty())
    }

    @Test
    fun testPlaylistCreationAndSongManagement() = runTest {
        val repository = FakeMusicRepository(sampleSongs)
        val dummyPlayerController = com.limani.music.player.PlayerController(
            androidx.test.core.app.ApplicationProvider.getApplicationContext()
        )
        val viewModel = MainViewModel(repository, dummyPlayerController)

        viewModel.createPlaylist("Chill List")
        testDispatcher.scheduler.advanceUntilIdle()

        val playlists = repository.playlistsFlow.first()
        assertEquals(1, playlists.size)
        assertEquals("Chill List", playlists[0].name)

        val playlistId = playlists[0].id
        viewModel.addSongToPlaylist(playlistId, "1")
        viewModel.addSongToPlaylist(playlistId, "2")
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedPlaylists = repository.playlistsFlow.first()
        assertEquals(2, updatedPlaylists[0].songIds.size)

        viewModel.removeSongFromPlaylist(playlistId, "1")
        testDispatcher.scheduler.advanceUntilIdle()

        val playlistsAfterRemove = repository.playlistsFlow.first()
        assertEquals(1, playlistsAfterRemove[0].songIds.size)
        assertFalse(playlistsAfterRemove[0].songIds.contains("1"))

        viewModel.deletePlaylist(playlistId)
        testDispatcher.scheduler.advanceUntilIdle()

        val finalPlaylists = repository.playlistsFlow.first()
        assertTrue(finalPlaylists.isEmpty())
    }
}
