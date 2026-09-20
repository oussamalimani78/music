package com.limani.music.data.catalog

import com.limani.music.data.model.Song

/**
 * Central catalog of music bundled inside the Android application.
 *
 * HOW TO ADD BUNDLED SONGS:
 * 1. Place your audio files (e.g., .mp3, .wav, .m4a) in the resource folder:
 *    `app/src/main/res/raw/` (e.g. `app/src/main/res/raw/sample_song.mp3`).
 * 2. Add an entry to the `BUNDLED_SONGS` list below with the corresponding resource ID `R.raw.<filename>`
 *    and song metadata (title, artist, album, duration, optional artwork).
 *
 * Example:
 * ```
 * Song(
 *     id = "song_1",
 *     title = "Midnight Dreams",
 *     artist = "Acoustic Vibe",
 *     album = "Nightfall",
 *     durationMs = 215000L,
 *     rawResId = R.raw.midnight_dreams,
 *     coverResId = R.drawable.ic_album_placeholder
 * )
 * ```
 */
object MusicCatalog {

    /**
     * List of bundled songs packaged directly inside `res/raw/`.
     * If no audio files have been added yet, leave this list empty.
     * The app will handle the empty state gracefully without crashing.
     */
    val BUNDLED_SONGS: List<Song> = listOf(
        // Add your bundled songs here.
    )

    /**
     * Helper method to retrieve all bundled songs.
     */
    fun getSongs(): List<Song> {
        return BUNDLED_SONGS
    }
}
