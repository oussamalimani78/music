package com.limani.music.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.limani.music.R
import com.limani.music.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class NowPlayingBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var isUserTrackingSeekBar = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_now_playing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

        val btnCollapse: ImageButton = view.findViewById(R.id.btnCollapse)
        val btnHeaderQueue: ImageButton = view.findViewById(R.id.btnHeaderQueue)
        val imgArtwork: ImageView = view.findViewById(R.id.imgNowPlayingArtwork)
        val txtTitle: TextView = view.findViewById(R.id.txtNowPlayingTitle)
        val txtArtist: TextView = view.findViewById(R.id.txtNowPlayingArtist)
        val btnFav: ImageButton = view.findViewById(R.id.btnNowPlayingFav)
        val seekBar: SeekBar = view.findViewById(R.id.seekBarProgress)
        val txtCurrentPos: TextView = view.findViewById(R.id.txtCurrentPosition)
        val txtTotalDur: TextView = view.findViewById(R.id.txtTotalDuration)

        val btnShuffle: ImageButton = view.findViewById(R.id.btnShuffle)
        val btnPrevious: ImageButton = view.findViewById(R.id.btnPrevious)
        val btnPlayPause: FloatingActionButton = view.findViewById(R.id.btnPlayPause)
        val btnNext: ImageButton = view.findViewById(R.id.btnNext)
        val btnRepeat: ImageButton = view.findViewById(R.id.btnRepeat)

        btnCollapse.setOnClickListener { dismiss() }

        btnHeaderQueue.setOnClickListener { showQueueDialog() }

        btnPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        btnPrevious.setOnClickListener { viewModel.skipToPrevious() }
        btnNext.setOnClickListener { viewModel.skipToNext() }
        btnShuffle.setOnClickListener { viewModel.toggleShuffle() }
        btnRepeat.setOnClickListener { viewModel.toggleRepeatMode() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    txtCurrentPos.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {
                isUserTrackingSeekBar = true
            }

            override fun onStopTrackingTouch(sb: SeekBar?) {
                isUserTrackingSeekBar = false
                sb?.let { viewModel.seekTo(it.progress.toLong()) }
            }
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentSong.collectLatest { song ->
                        if (song != null) {
                            txtTitle.text = song.title
                            txtArtist.text = "${song.artist} • ${song.album}"
                            if (song.coverResId != null && song.coverResId != 0) {
                                imgArtwork.setImageResource(song.coverResId)
                                imgArtwork.clearColorFilter()
                            } else {
                                imgArtwork.setImageResource(R.drawable.ic_music_note)
                            }

                            if (song.isFavorite) {
                                btnFav.setImageResource(R.drawable.ic_heart_filled)
                                btnFav.setColorFilter(ContextCompat.getColor(requireContext(), R.color.accent_heart))
                            } else {
                                btnFav.setImageResource(R.drawable.ic_heart_outline)
                                btnFav.clearColorFilter()
                            }

                            btnFav.setOnClickListener { viewModel.toggleFavorite(song.id) }
                        }
                    }
                }

                launch {
                    viewModel.isPlaying.collectLatest { playing ->
                        if (playing) {
                            btnPlayPause.setImageResource(R.drawable.ic_pause)
                        } else {
                            btnPlayPause.setImageResource(R.drawable.ic_play)
                        }
                    }
                }

                launch {
                    viewModel.durationMs.collectLatest { dur ->
                        seekBar.max = dur.toInt()
                        txtTotalDur.text = formatTime(dur)
                    }
                }

                launch {
                    viewModel.playbackPositionMs.collectLatest { pos ->
                        if (!isUserTrackingSeekBar) {
                            seekBar.progress = pos.toInt()
                            txtCurrentPos.text = formatTime(pos)
                        }
                    }
                }

                launch {
                    viewModel.isShuffleEnabled.collectLatest { shuffle ->
                        if (shuffle) {
                            btnShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary))
                        } else {
                            btnShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSurfaceVariant))
                        }
                    }
                }

                launch {
                    viewModel.repeatMode.collectLatest { mode ->
                        when (mode) {
                            Player.REPEAT_MODE_ONE -> {
                                btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                                btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary))
                            }
                            Player.REPEAT_MODE_ALL -> {
                                btnRepeat.setImageResource(R.drawable.ic_repeat)
                                btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary))
                            }
                            else -> {
                                btnRepeat.setImageResource(R.drawable.ic_repeat)
                                btnRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSurfaceVariant))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showQueueDialog() {
        val queue = viewModel.queue.value
        val items = if (queue.isNotEmpty()) {
            queue.map { "${it.title} - ${it.artist}" }.toTypedArray()
        } else {
            arrayOf("Queue is empty")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.queue_title))
            .setItems(items) { _, which ->
                if (queue.isNotEmpty()) {
                    viewModel.playPlaylist(queue, which)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}
