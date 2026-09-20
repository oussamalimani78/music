package com.limani.music.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.limani.music.R
import com.limani.music.data.repository.MusicRepositoryImpl
import com.limani.music.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rgTheme: RadioGroup = view.findViewById(R.id.rgTheme)
        val rbSystem: RadioButton = view.findViewById(R.id.rbThemeSystem)
        val rbLight: RadioButton = view.findViewById(R.id.rbThemeLight)
        val rbDark: RadioButton = view.findViewById(R.id.rbThemeDark)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.themeMode.collectLatest { mode ->
                    rgTheme.setOnCheckedChangeListener(null)
                    when (mode) {
                        MusicRepositoryImpl.MODE_NIGHT_NO -> rbLight.isChecked = true
                        MusicRepositoryImpl.MODE_NIGHT_YES -> rbDark.isChecked = true
                        else -> rbSystem.isChecked = true
                    }
                    setupRadioListener(rgTheme)
                }
            }
        }
    }

    private fun setupRadioListener(rgTheme: RadioGroup) {
        rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> MusicRepositoryImpl.MODE_NIGHT_NO
                R.id.rbThemeDark -> MusicRepositoryImpl.MODE_NIGHT_YES
                else -> MusicRepositoryImpl.MODE_NIGHT_FOLLOW_SYSTEM
            }
            viewModel.setThemeMode(mode)
            val nightMode = when (mode) {
                MusicRepositoryImpl.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                MusicRepositoryImpl.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }
}
