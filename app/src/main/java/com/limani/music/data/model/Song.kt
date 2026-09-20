package com.limani.music.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

/**
 * Data class representing a music track bundled inside the application resources.
 */
data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    @RawRes val rawResId: Int,
    @DrawableRes val coverResId: Int? = null,
    val isFavorite: Boolean = false
)
