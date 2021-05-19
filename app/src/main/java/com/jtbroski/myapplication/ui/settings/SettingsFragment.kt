package com.jtbroski.myapplication.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.databinding.FragmentSettingsBinding
import com.jtbroski.myapplication.ui.main.MainActivity


class SettingsFragment : Fragment() {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(requireActivity()).get(SettingsViewModel::class.java)
        binding.settingsViewModel = viewModel

        viewModel.updateOriginalUnitsFlag()

        // Units Radio Group
        binding.rgUnits.setOnCheckedChangeListener { _, checkedId ->
            run {
                val checkedRadioButton = binding.rgUnits.findViewById<RadioButton>(checkedId)
                val isImperial = checkedRadioButton.text.toString() == getString(R.string.imperial)
                Utils.preferenceDbHelper.updateImperialFlag(isImperial)
                viewModel.onUnitsGroupChange(isImperial)
            }
        }

        // Theme Radio Group
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            run {
                val checkedRadioButton = binding.rgTheme.findViewById<RadioButton>(checkedId)
                val isDark = checkedRadioButton.text.toString() == getString(R.string.dark)
                Utils.preferenceDbHelper.updateDarkThemeFlag(isDark)
                viewModel.onThemeGroupChange(isDark)
            }
        }

        (requireActivity() as MainActivity).invalidateOptionsMenu()

        return binding.root
    }
}