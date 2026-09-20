package com.limani.music.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limani.music.R
import com.limani.music.data.model.Song
import java.util.Locale

class SongAdapter(
    private val onSongClick: (Song) -> Unit,
    private val onFavoriteClick: (Song) -> Unit,
    private val onMoreClick: (Song) -> Unit
) : ListAdapter<Song, SongAdapter.SongViewHolder>(SongDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAlbumArt: ImageView = itemView.findViewById(R.id.imgAlbumArt)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtArtistAlbum: TextView = itemView.findViewById(R.id.txtArtistAlbum)
        private val txtDuration: TextView = itemView.findViewById(R.id.txtDuration)
        private val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        private val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)

        fun bind(song: Song) {
            txtTitle.text = song.title
            txtArtistAlbum.text = "${song.artist} • ${song.album}"
            txtDuration.text = formatDuration(song.durationMs)

            if (song.coverResId != null && song.coverResId != 0) {
                imgAlbumArt.setImageResource(song.coverResId)
                imgAlbumArt.clearColorFilter()
            } else {
                imgAlbumArt.setImageResource(R.drawable.ic_music_note)
            }

            if (song.isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_heart_filled)
                btnFavorite.setColorFilter(ContextCompat.getColor(itemView.context, R.color.accent_heart))
            } else {
                btnFavorite.setImageResource(R.drawable.ic_heart_outline)
                btnFavorite.clearColorFilter()
            }

            itemView.setOnClickListener { onSongClick(song) }
            btnFavorite.setOnClickListener { onFavoriteClick(song) }
            btnMore.setOnClickListener { onMoreClick(song) }
        }

        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
}
