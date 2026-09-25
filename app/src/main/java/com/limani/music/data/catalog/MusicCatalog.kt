package com.limani.music.data.catalog

import android.content.Context
import com.limani.music.R
import com.limani.music.data.model.Song
import java.io.File

/**
 * Central catalog of music bundled inside the Android application.
 *
 * Automatically detects MP3 files in `app/src/main/res/raw/` and extracts song metadata
 * (artist and title) according to filename parsing rules.
 */
object MusicCatalog {

    /**
     * List of bundled songs packaged directly inside `app/src/main/res/raw/`.
     */
    val BUNDLED_SONGS: List<Song>
        get() = getSongs()

    /**
     * Helper method to retrieve all bundled songs dynamically from res/raw.
     */
    fun getSongs(context: Context? = null): List<Song> {
        val songsList = mutableListOf<Song>()
        val processedResIds = mutableSetOf<Int>()

        // 1. Scan filesystem directories for MP3 files (available in test/JVM environment)
        val rawDirs = listOf(
            File("app/src/main/res/raw"),
            File("src/main/res/raw"),
            File("res/raw")
        )

        for (dir in rawDirs) {
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles { _, name -> name.endsWith(".mp3", ignoreCase = true) }?.forEach { file ->
                    val filename = file.name
                    val baseName = filename.substringBeforeLast(".")
                    val resName = sanitizeResourceName(baseName)
                    val resId = getRawResourceId(resName, context)

                    if (!processedResIds.contains(resId)) {
                        if (resId != 0) {
                            processedResIds.add(resId)
                        }
                        val song = parseSongFromFilename(filename, resId)
                        songsList.add(song)
                    }
                }
            }
        }

        // 2. Reflection scan on R.raw fields (for compiled Android resources)
        val pkgName = context?.packageName ?: "com.limani.music"
        try {
            val rawClass = Class.forName("$pkgName.R\$raw")
            val fields = rawClass.fields
            for (field in fields) {
                val resName = field.name
                if (resName.startsWith(".") || resName == "gitkeep" || resName.startsWith("ic_")) continue
                val resId = field.getInt(null)
                if (resId != 0 && !processedResIds.contains(resId)) {
                    processedResIds.add(resId)
                    val song = parseSongFromFilename(resName, resId)
                    songsList.add(song)
                }
            }
        } catch (_: Throwable) {
            // R$raw class does not exist if res/raw contains no compiled raw resources
        }

        return songsList
    }

    /**
     * Attempts to resolve the raw resource ID for a given resource name.
     */
    private fun getRawResourceId(resName: String, context: Context?): Int {
        if (context != null) {
            val id = context.resources.getIdentifier(resName, "raw", context.packageName)
            if (id != 0) return id
        }
        val pkgName = context?.packageName ?: "com.limani.music"
        return try {
            val rawClass = Class.forName("$pkgName.R\$raw")
            val field = rawClass.getField(resName)
            field.getInt(null)
        } catch (_: Throwable) {
            0
        }
    }

    private fun sanitizeResourceName(name: String): String {
        return name.lowercase().replace(Regex("[^a-z0-9_]"), "_")
    }

    /**
     * Parses an MP3 filename or resource name into a Song object adhering to rules 1-8.
     *
     * Rules:
     * 1. artist - title.mp3 -> Artist = artist, Title = title
     * 2. title - artist.mp3 -> Follow established project convention (Artist - Title)
     * 3. Replace "_" with spaces.
     * 4. Remove .mp3 extension.
     * 5. Clean technical characters & Title Case.
     * 6. Unknown artist fallback: "Unknown Artist"
     * 7. Unknown title fallback: cleaned filename as title.
     * 8. Keep rawResId intact.
     */
    fun parseSongFromFilename(
        rawFilename: String,
        rawResId: Int,
        albumName: String = "Bundled Tracks"
    ): Song {
        // 1. Remove extension
        var name = rawFilename.replace(Regex("(?i)\\.mp3$"), "")

        // 2. Remove technical tags in brackets or parentheses e.g. [128kbps], (Official Video)
        name = name.replace(Regex("\\[.*?\\]|\\(.*?\\)"), "").trim()

        // 3. Remove leading track numbers e.g. "01 - ", "01. ", "01_"
        name = name.replace(Regex("^\\d{1,3}[\\._\\-\\s]+"), "").trim()

        // 4. Identify artist-title separator
        // Possible separators: " - ", "_-_", "___", " -- "
        val separatorRegex = Regex("\\s+-\\s+|_-_|___|\\s+--\\s+")

        val parts = if (separatorRegex.containsMatchIn(name)) {
            name.split(separatorRegex, limit = 2)
        } else if (name.contains("-")) {
            name.split("-", limit = 2)
        } else {
            listOf(name)
        }

        val rawArtist: String
        val rawTitle: String

        if (parts.size >= 2) {
            rawArtist = parts[0].trim()
            rawTitle = parts[1].trim()
        } else {
            rawArtist = ""
            rawTitle = parts[0].trim()
        }

        // 5. Replace underscores with spaces (Rule 3)
        val artistClean = rawArtist.replace("_", " ").trim()
        val titleClean = rawTitle.replace("_", " ").trim()

        // 6. Apply Rule 6 & Rule 7
        val finalArtist = if (artistClean.isBlank()) {
            "Unknown Artist"
        } else {
            capitalizeWords(artistClean)
        }

        val baseFilenameWithoutExt = rawFilename.replace(Regex("(?i)\\.mp3$"), "").replace("_", " ").trim()
        val finalTitle = if (titleClean.isBlank()) {
            capitalizeWords(baseFilenameWithoutExt.ifBlank { "Unknown Song" })
        } else {
            capitalizeWords(titleClean)
        }

        val songId = if (rawResId != 0) "raw_$rawResId" else "raw_${rawFilename.hashCode()}"

        return Song(
            id = songId,
            title = finalTitle,
            artist = finalArtist,
            album = albumName,
            durationMs = 0L,
            rawResId = rawResId,
            coverResId = R.drawable.ic_music_note
        )
    }

    private fun capitalizeWords(input: String): String {
        if (input.isBlank()) return input
        return input.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}
