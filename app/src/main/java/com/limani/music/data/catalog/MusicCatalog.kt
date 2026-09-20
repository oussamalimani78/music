package com.limani.music.data.catalog

import com.limani.music.R
import com.limani.music.data.model.Song

/**
 * Central catalog of music bundled inside the Android application.
 *
 * HOW TO ADD BUNDLED SONGS:
 * 1. Place your audio file (e.g. `my_song.mp3`) in the resource directory:
 *    `app/src/main/res/raw/my_song.mp3`
 *    Note: File names must contain only lowercase letters, digits, and underscores (e.g., `my_song.mp3`).
 *
 * 2. Add a `Song(...)` entry to the `BUNDLED_SONGS` list below using the template.
 *
 * TEMPLATE / EXAMPLE ENTRY:
 * ```
 * Song(
 *     id = "unique_song_id_1",                // Unique ID string for the track
 *     title = "Song Title",                   // Song Title
 *     artist = "Artist Name",                 // Artist Name
 *     album = "Album Name",                   // Album Name
 *     durationMs = 215000L,                   // Duration in milliseconds (e.g. 3m 35s = 215000L)
 *     rawResId = R.raw.my_song,               // Raw resource ID corresponding to app/src/main/res/raw/my_song.mp3
 *     coverResId = R.drawable.ic_music_note   // Optional artwork drawable resource ID (or null)
 * )
 * ```
 */
object MusicCatalog {

    /**
     * List of bundled songs packaged directly inside `app/src/main/res/raw/`.
     * Add your `Song(...)` entries to this list.
     * When empty, the app handles the empty state gracefully without crashing.
     */
    val BUNDLED_SONGS: List<Song> = listOf(
        /*
        Song(
            id = "example_song_1",
            title = "Example Song Title",
            artist = "Example Artist",
            album = "Example Album",
            durationMs = 180000L,
            rawResId = R.raw.example_audio,
            coverResId = R.drawable.ic_music_note
        )
        */
    )

    /**
     * Helper method to retrieve all bundled songs.
     */
    fun getSongs(): List<Song> {
        return BUNDLED_SONGS
    }
}
