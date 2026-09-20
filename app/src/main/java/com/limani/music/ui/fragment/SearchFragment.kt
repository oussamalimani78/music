package com.limani.music.ui.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
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

class SearchFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var songAdapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etSearch: EditText = view.findViewById(R.id.etSearch)
        val btnClearSearch: ImageButton = view.findViewById(R.id.btnClearSearch)
        val rvSearchSongs: RecyclerView = view.findViewById(R.id.rvSearchSongs)
        val emptyStateLayout: View = view.findViewById(R.id.emptyStateLayout)

        val emptyIcon: ImageView = emptyStateLayout.findViewById(R.id.imgEmptyIcon)
        val emptyTitle: TextView = emptyStateLayout.findViewById(R.id.txtEmptyTitle)
        val emptyDesc: TextView = emptyStateLayout.findViewById(R.id.txtEmptyDescription)

        emptyIcon.setImageResource(R.drawable.ic_nav_search)
        emptyTitle.text = getString(R.string.no_search_results)
        emptyDesc.text = "Try searching for a different track title, artist, or album."

        songAdapter = SongAdapter(
            onSongClick = { song -> viewModel.playSong(song) },
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onMoreClick = { song -> showSongOptions(song) }
        )

        rvSearchSongs.layoutManager = LinearLayoutManager(requireContext())
        rvSearchSongs.adapter = songAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                viewModel.setSearchQuery(query)
                btnClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearch.setText("")
            viewModel.setSearchQuery("")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredSongs.collectLatest { songs ->
                    songAdapter.submitList(songs)
                    if (songs.isEmpty()) {
                        rvSearchSongs.visibility = View.GONE
                        emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        rvSearchSongs.visibility = View.VISIBLE
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
        } else {
            viewModel.createPlaylist("My Hits")
        }
    }
}
