package com.limani.music.ui.fragment

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.limani.music.R
import com.limani.music.data.model.Song
import com.limani.music.ui.MainViewModel
import com.limani.music.ui.adapter.PlaylistAdapter
import com.limani.music.ui.adapter.SongAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var songAdapter: SongAdapter
    private lateinit var playlistAdapter: PlaylistAdapter

    private var currentTab = TAB_ALL_SONGS

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroup: ChipGroup = view.findViewById(R.id.chipGroupLibrary)
        val chipAllSongs: Chip = view.findViewById(R.id.chipAllSongs)
        val chipFavorites: Chip = view.findViewById(R.id.chipFavorites)
        val chipPlaylists: Chip = view.findViewById(R.id.chipPlaylists)

        val rvSongs: RecyclerView = view.findViewById(R.id.rvLibrarySongs)
        val rvPlaylists: RecyclerView = view.findViewById(R.id.rvLibraryPlaylists)
        val emptyStateLayout: View = view.findViewById(R.id.emptyStateLayout)
        val fabCreatePlaylist: FloatingActionButton = view.findViewById(R.id.fabCreatePlaylist)

        val emptyIcon: ImageView = emptyStateLayout.findViewById(R.id.imgEmptyIcon)
        val emptyTitle: TextView = emptyStateLayout.findViewById(R.id.txtEmptyTitle)
        val emptyDesc: TextView = emptyStateLayout.findViewById(R.id.txtEmptyDescription)

        songAdapter = SongAdapter(
            onSongClick = { song -> viewModel.playSong(song) },
            onFavoriteClick = { song -> viewModel.toggleFavorite(song.id) },
            onMoreClick = { song -> showSongOptions(song) }
        )

        playlistAdapter = PlaylistAdapter(
            onPlaylistClick = { playlist -> showPlaylistSongs(playlist.name, playlist.songIds) },
            onDeleteClick = { playlist -> viewModel.deletePlaylist(playlist.id) }
        )

        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        rvSongs.adapter = songAdapter

        rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        rvPlaylists.adapter = playlistAdapter

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.contains(chipAllSongs.id)) {
                currentTab = TAB_ALL_SONGS
            } else if (checkedIds.contains(chipFavorites.id)) {
                currentTab = TAB_FAVORITES
            } else if (checkedIds.contains(chipPlaylists.id)) {
                currentTab = TAB_PLAYLISTS
            }
            updateTabContent(rvSongs, rvPlaylists, emptyStateLayout, emptyIcon, emptyTitle, emptyDesc, fabCreatePlaylist)
        }

        fabCreatePlaylist.setOnClickListener { showCreatePlaylistDialog() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allSongs.collectLatest {
                        if (currentTab == TAB_ALL_SONGS) {
                            updateTabContent(rvSongs, rvPlaylists, emptyStateLayout, emptyIcon, emptyTitle, emptyDesc, fabCreatePlaylist)
                        }
                    }
                }
                launch {
                    viewModel.favoriteSongs.collectLatest {
                        if (currentTab == TAB_FAVORITES) {
                            updateTabContent(rvSongs, rvPlaylists, emptyStateLayout, emptyIcon, emptyTitle, emptyDesc, fabCreatePlaylist)
                        }
                    }
                }
                launch {
                    viewModel.playlists.collectLatest {
                        if (currentTab == TAB_PLAYLISTS) {
                            updateTabContent(rvSongs, rvPlaylists, emptyStateLayout, emptyIcon, emptyTitle, emptyDesc, fabCreatePlaylist)
                        }
                    }
                }
            }
        }
    }

    private fun updateTabContent(
        rvSongs: RecyclerView,
        rvPlaylists: RecyclerView,
        emptyLayout: View,
        emptyIcon: ImageView,
        emptyTitle: TextView,
        emptyDesc: TextView,
        fab: FloatingActionButton
    ) {
        when (currentTab) {
            TAB_ALL_SONGS -> {
                fab.visibility = View.GONE
                rvPlaylists.visibility = View.GONE
                val songs = viewModel.allSongs.value
                songAdapter.submitList(songs)
                if (songs.isEmpty()) {
                    rvSongs.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    emptyIcon.setImageResource(R.drawable.ic_empty_music)
                    emptyTitle.text = getString(R.string.empty_catalog_title)
                    emptyDesc.text = getString(R.string.empty_catalog_desc)
                } else {
                    rvSongs.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE
                }
            }
            TAB_FAVORITES -> {
                fab.visibility = View.GONE
                rvPlaylists.visibility = View.GONE
                val favs = viewModel.favoriteSongs.value
                songAdapter.submitList(favs)
                if (favs.isEmpty()) {
                    rvSongs.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    emptyIcon.setImageResource(R.drawable.ic_heart_outline)
                    emptyTitle.text = getString(R.string.empty_favorites_title)
                    emptyDesc.text = getString(R.string.empty_favorites_desc)
                } else {
                    rvSongs.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE
                }
            }
            TAB_PLAYLISTS -> {
                rvSongs.visibility = View.GONE
                fab.visibility = View.VISIBLE
                val playlists = viewModel.playlists.value
                playlistAdapter.submitList(playlists)
                if (playlists.isEmpty()) {
                    rvPlaylists.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                    emptyIcon.setImageResource(R.drawable.ic_queue_music)
                    emptyTitle.text = getString(R.string.empty_playlists_title)
                    emptyDesc.text = getString(R.string.empty_playlists_desc)
                } else {
                    rvPlaylists.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun showCreatePlaylistDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            hint = getString(R.string.playlist_name_hint)
        }
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_playlist))
            .setView(input)
            .setPositiveButton(getString(R.string.create)) { _, _ ->
                val name = input.text.toString()
                if (name.isNotBlank()) {
                    viewModel.createPlaylist(name)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showPlaylistSongs(playlistName: String, songIds: List<String>) {
        val allSongs = viewModel.allSongs.value
        val playlistSongs = allSongs.filter { songIds.contains(it.id) }
        val songTitles = if (playlistSongs.isNotEmpty()) {
            playlistSongs.map { "${it.title} - ${it.artist}" }.toTypedArray()
        } else {
            arrayOf("No songs in this playlist yet")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(playlistName)
            .setItems(songTitles) { _, which ->
                if (playlistSongs.isNotEmpty()) {
                    viewModel.playSong(playlistSongs[which], playlistSongs)
                }
            }
            .setPositiveButton("Play All") { _, _ ->
                if (playlistSongs.isNotEmpty()) {
                    viewModel.playPlaylist(playlistSongs, 0)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showSongOptions(song: Song) {
        val playlists = viewModel.playlists.value
        if (playlists.isEmpty()) {
            showCreatePlaylistDialog()
            return
        }
        val options = playlists.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_to_playlist))
            .setItems(options) { _, which ->
                viewModel.addSongToPlaylist(playlists[which].id, song.id)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    companion object {
        private const val TAB_ALL_SONGS = 0
        private const val TAB_FAVORITES = 1
        private const val TAB_PLAYLISTS = 2
    }
}
