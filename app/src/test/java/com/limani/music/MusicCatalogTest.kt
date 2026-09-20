package com.limani.music

import com.limani.music.data.catalog.MusicCatalog
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
}
