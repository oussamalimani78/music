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
import com.limani.music.R
import com.limani.music.data.model.Song
import com.limani.music.ui.MainViewModel
import com.limani.music.ui.adapter.SongAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvFavorites: RecyclerView = view.findViewById(R.id.rvFavorites)
        val emptyStateLayout: View = view.findViewById(R.id.emptyStateLayout)

        val emptyIcon: ImageView = emptyStateLayout.findViewById(R.id.imgEmptyIcon)
        val emptyTitle: TextView = emptyStateLayout.findViewById(R.id.txtEmptyTitle)
        val emptyDesc: TextView = emptyStateLayout.findViewById(R.id.txtEmptyDescription)

        emptyIcon.setImageResource(R.drawable.ic_heart_outline)
        emptyTitle.text = getString(R.string.empty_favorites_title)
        emptyDesc.text = getString(R.string.empty_favorites_desc)

        songAdapter = SongAdapter(
            onSongClick = { song -> viewModel.playSong(song) },
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onMoreClick = { song -> showSongOptions(song) }
        )

        rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        rvFavorites.adapter = songAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteSongs.collectLatest { favs ->
                    songAdapter.submitList(favs)
                    if (favs.isEmpty()) {
                        rvFavorites.visibility = View.GONE
                        emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        rvFavorites.visibility = View.VISIBLE
                        emptyStateLayout.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showSongOptions(song: Song) {
        val playlists = viewModel.playlists.value
        if (playlists.isNotEmpty()) {
            val options = playlists.map { it.name }.toTypedArray()
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_to_playlist))
                .setItems(options) { _, which ->
                    viewModel.addSongToPlaylist(playlists[which].id, song.id)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }
}
