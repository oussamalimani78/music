package com.limani.music

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.limani.music.data.repository.MusicRepositoryImpl
import com.limani.music.ui.MainViewModel
import com.limani.music.ui.MainViewModelFactory
import com.limani.music.ui.dialog.NowPlayingBottomSheetDialogFragment
import com.limani.music.ui.fragment.FavoritesFragment
import com.limani.music.ui.fragment.HomeFragment
import com.limani.music.ui.fragment.LibraryFragment
import com.limani.music.ui.fragment.SearchFragment
import com.limani.music.ui.fragment.SettingsFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            val app = application as MusicApplication
            return MainViewModelFactory(app.repository, app.playerController)
        }

    private val viewModel: MainViewModel by viewModels()

    private var activeFragmentTag = TAG_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), TAG_HOME)
        }

        setupBottomNavigation()
        setupMiniPlayer()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeMode.collectLatest { mode ->
                    val nightMode = when (mode) {
                        MusicRepositoryImpl.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                        MusicRepositoryImpl.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
                        AppCompatDelegate.setDefaultNightMode(nightMode)
                    }
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (activeFragmentTag != TAG_HOME) {
                        loadFragment(HomeFragment(), TAG_HOME)
                    }
                    true
                }
                R.id.navigation_library -> {
                    if (activeFragmentTag != TAG_LIBRARY) {
                        loadFragment(LibraryFragment(), TAG_LIBRARY)
                    }
                    true
                }
                R.id.navigation_search -> {
                    if (activeFragmentTag != TAG_SEARCH) {
                        loadFragment(SearchFragment(), TAG_SEARCH)
                    }
                    true
                }
                R.id.navigation_favorites -> {
                    if (activeFragmentTag != TAG_FAVORITES) {
                        loadFragment(FavoritesFragment(), TAG_FAVORITES)
                    }
                    true
                }
                R.id.navigation_settings -> {
                    if (activeFragmentTag != TAG_SETTINGS) {
                        loadFragment(SettingsFragment(), TAG_SETTINGS)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        activeFragmentTag = tag
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }

    private fun setupMiniPlayer() {
        val miniCard: MaterialCardView = findViewById(R.id.miniPlayerCard)
        val miniTitle: TextView = findViewById(R.id.miniSongTitle)
        val miniArtist: TextView = findViewById(R.id.miniSongArtist)
        val miniArtwork: ImageView = findViewById(R.id.miniAlbumArt)
        val miniPlayPauseBtn: ImageButton = findViewById(R.id.miniPlayPauseBtn)
        val miniNextBtn: ImageButton = findViewById(R.id.miniNextBtn)
        val miniProgress: LinearProgressIndicator = findViewById(R.id.miniProgress)

        miniPlayPauseBtn.setOnClickListener { viewModel.togglePlayPause() }
        miniNextBtn.setOnClickListener { viewModel.skipToNext() }

        miniCard.setOnClickListener {
            val dialog = NowPlayingBottomSheetDialogFragment()
            dialog.show(supportFragmentManager, "NowPlayingDialog")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentSong.collectLatest { song ->
                        if (song != null) {
                            miniCard.visibility = View.VISIBLE
                            miniTitle.text = song.title
                            miniArtist.text = song.artist
                            if (song.coverResId != null && song.coverResId != 0) {
                                miniArtwork.setImageResource(song.coverResId)
                                miniArtwork.clearColorFilter()
                            } else {
                                miniArtwork.setImageResource(R.drawable.ic_music_note)
                            }
                        } else {
                            miniCard.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.isPlaying.collectLatest { playing ->
                        if (playing) {
                            miniPlayPauseBtn.setImageResource(R.drawable.ic_pause)
                        } else {
                            miniPlayPauseBtn.setImageResource(R.drawable.ic_play)
                        }
                    }
                }

                launch {
                    viewModel.playbackPositionMs.collectLatest { pos ->
                        val duration = viewModel.durationMs.value
                        if (duration > 0) {
                            val progress = ((pos.toDouble() / duration.toDouble()) * 1000).toInt()
                            miniProgress.progress = progress.coerceIn(0, 1000)
                        } else {
                            miniProgress.progress = 0
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG_HOME = "home"
        private const val TAG_LIBRARY = "library"
        private const val TAG_SEARCH = "search"
        private const val TAG_FAVORITES = "favorites"
        private const val TAG_SETTINGS = "settings"
    }
}
