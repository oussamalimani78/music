package com.limani.music

import com.limani.music.data.catalog.MusicCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MusicCatalogTest {

    @Test
    fun testGetSongsReturnsNonNullList() {
        val songs = MusicCatalog.getSongs()
        assertNotNull(songs)
    }

    @Test
    fun testCatalogEmptyStateHandling() {
        val songs = MusicCatalog.getSongs()
        // Ensure catalog does not throw exception when iterating
        for (song in songs) {
            assertNotNull(song.id)
            assertNotNull(song.title)
            assertNotNull(song.artist)
        }
    }

    @Test
    fun testFilenameParsingChebBilalExample() {
        val song = MusicCatalog.parseSongFromFilename("cheb_bilal - ya_rayt.mp3", 101)
        assertEquals("Cheb Bilal", song.artist)
        assertEquals("Ya Rayt", song.title)
        assertEquals(101, song.rawResId)
    }

    @Test
    fun testFilenameParsingWithUnderscoresAndDash() {
        val song = MusicCatalog.parseSongFromFilename("cheb_bilal_-_ya_rayt.mp3", 102)
        assertEquals("Cheb Bilal", song.artist)
        assertEquals("Ya Rayt", song.title)
        assertEquals(102, song.rawResId)
    }

    @Test
    fun testFilenameParsingWithTripleUnderscores() {
        val song = MusicCatalog.parseSongFromFilename("cheb_bilal___ya_rayt.mp3", 103)
        assertEquals("Cheb Bilal", song.artist)
        assertEquals("Ya Rayt", song.title)
        assertEquals(103, song.rawResId)
    }

    @Test
    fun testFilenameParsingProjectConventionArtistTitle() {
        val song = MusicCatalog.parseSongFromFilename("title_here - artist_here.mp3", 104)
        assertEquals("Title Here", song.artist)
        assertEquals("Artist Here", song.title)
    }

    @Test
    fun testFilenameParsingNoArtistFallbackToUnknownArtist() {
        val song = MusicCatalog.parseSongFromFilename("just_a_song_title.mp3", 105)
        assertEquals("Unknown Artist", song.artist)
        assertEquals("Just A Song Title", song.title)
    }

    @Test
    fun testFilenameParsingTechnicalCharacterCleaning() {
        val song = MusicCatalog.parseSongFromFilename("01 - artist_name - track_title [320kbps] (Official Video).mp3", 106)
        assertEquals("Artist Name", song.artist)
        assertEquals("Track Title", song.title)
    }
}
