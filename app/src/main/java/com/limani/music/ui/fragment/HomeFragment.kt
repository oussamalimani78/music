package com.limani.music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.limani.music.R
import com.limani.music.data.model.Song
import com.limani.music.ui.MainViewModel
import com.limani.music.ui.adapter.SongAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHomeSongs: RecyclerView = view.findViewById(R.id.rvHomeSongs)
        val emptyStateLayout: View = view.findViewById(R.id.emptyStateLayout)
        val txtSongCountHeader: TextView = view.findViewById(R.id.txtSongCountHeader)
        val btnQuickPlay: FloatingActionButton = view.findViewById(R.id.btnQuickPlay)

        val emptyIcon: ImageView = emptyStateLayout.findViewById(R.id.imgEmptyIcon)
        val emptyTitle: TextView = emptyStateLayout.findViewById(R.id.txtEmptyTitle)
        val emptyDesc: TextView = emptyStateLayout.findViewById(R.id.txtEmptyDescription)

        emptyIcon.setImageResource(R.drawable.ic_empty_music)
        emptyTitle.text = getString(R.string.empty_catalog_title)
        emptyDesc.text = getString(R.string.empty_catalog_desc)

        songAdapter = SongAdapter(
            onSongClick = { song -> viewModel.playSong(song) },
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onMoreClick = { song -> showSongOptions(song) }
        )

        rvHomeSongs.layoutManager = LinearLayoutManager(requireContext())
        rvHomeSongs.adapter = songAdapter

        btnQuickPlay.setOnClickListener {
            val songs = viewModel.allSongs.value
            if (songs.isNotEmpty()) {
                viewModel.playSong(songs.first(), songs)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allSongs.collectLatest { songs ->
                    songAdapter.submitList(songs)
                    val count = songs.size
                    txtSongCountHeader.text = if (count == 1) "1 song available" else "$count songs available"

                    if (songs.isEmpty()) {
                        rvHomeSongs.visibility = View.GONE
                        emptyStateLayout.visibility = View.VISIBLE
                        btnQuickPlay.isEnabled = false
                    } else {
                        rvHomeSongs.visibility = View.VISIBLE
                        emptyStateLayout.visibility = View.GONE
                        btnQuickPlay.isEnabled = true
                    }
                }
            }
        }
    }

    private fun showSongOptions(song: Song) {
        val playlists = viewModel.playlists.value
        if (playlists.isEmpty()) {
            viewModel.createPlaylist("Favorites Mix")
        }
        val currentPlaylists = viewModel.playlists.value
        if (currentPlaylists.isNotEmpty()) {
            val options = currentPlaylists.map { it.name }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_to_playlist))
                .setItems(options) { _, which ->
                    val playlist = currentPlaylists[which]
                    viewModel.addSongToPlaylist(playlist.id, song.id)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}
