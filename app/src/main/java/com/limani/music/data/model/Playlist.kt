package com.limani.music.data.model

/**
 * Data class representing a user-created or custom playlist of songs.
 */
data class Playlist(
    val id: String,
    val name: String,
    val songIds: List<String> = emptyList()
)
