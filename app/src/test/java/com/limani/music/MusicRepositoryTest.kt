package com.limani.music

import androidx.test.core.app.ApplicationProvider
import com.limani.music.data.model.Song
import com.limani.music.data.repository.MusicRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MusicRepositoryTest {

    private val sampleCatalog = listOf(
        Song("s1", "Song One", "Artist A", "Album X", 180000L, 0),
        Song("s2", "Song Two", "Artist B", "Album Y", 200000L, 0)
    )

    @Test
    fun testFavoriteTogglingAndPersistence() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = MusicRepositoryImpl(context, sampleCatalog)

        assertFalse(repository.isFavorite("s1"))

        repository.toggleFavorite("s1")

        assertTrue(repository.isFavorite("s1"))
        val favs = repository.favoriteSongsFlow.first()
        assertEquals(1, favs.size)
        assertEquals("s1", favs[0].id)

        // Verify persistence with a new repository instance
        val repository2 = MusicRepositoryImpl(context, sampleCatalog)
        assertTrue(repository2.isFavorite("s1"))
    }

    @Test
    fun testPlaylistOperations() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = MusicRepositoryImpl(context, sampleCatalog)

        val created = repository.createPlaylist("Party Hits")
        assertEquals("Party Hits", created.name)

        repository.addSongToPlaylist(created.id, "s1")
        repository.addSongToPlaylist(created.id, "s2")

        val playlists = repository.playlistsFlow.first()
        assertEquals(1, playlists.size)
        assertEquals(2, playlists[0].songIds.size)

        repository.removeSongFromPlaylist(created.id, "s1")
        val playlistsAfterRemove = repository.playlistsFlow.first()
        assertEquals(1, playlistsAfterRemove[0].songIds.size)

        repository.deletePlaylist(created.id)
        val finalPlaylists = repository.playlistsFlow.first()
        assertTrue(finalPlaylists.isEmpty())
    }

    @Test
    fun testThemeModeSetting() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = MusicRepositoryImpl(context, sampleCatalog)

        repository.setThemeMode(MusicRepositoryImpl.MODE_NIGHT_YES)
        val mode = repository.themeModeFlow.first()
        assertEquals(MusicRepositoryImpl.MODE_NIGHT_YES, mode)
    }
}
