package com.limani.music.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.limani.music.R
import com.limani.music.data.model.Playlist

class PlaylistAdapter(
    private val onPlaylistClick: (Playlist) -> Unit,
    private val onDeleteClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtPlaylistName: TextView = itemView.findViewById(R.id.txtPlaylistName)
        private val txtSongCount: TextView = itemView.findViewById(R.id.txtSongCount)
        private val btnDeletePlaylist: ImageButton = itemView.findViewById(R.id.btnDeletePlaylist)

        fun bind(playlist: Playlist) {
            txtPlaylistName.text = playlist.name
            val count = playlist.songIds.size
            txtSongCount.text = if (count == 1) "1 song" else "$count songs"

            itemView.setOnClickListener { onPlaylistClick(playlist) }
            btnDeletePlaylist.setOnClickListener { onDeleteClick(playlist) }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}
