package com.jtbroski.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.jtbroski.myapplication.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding
    private val navController: NavController by lazy { findNavController() }

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

        // Toolbar Back Arrow
        binding.btnBackArrow.setOnClickListener {
            viewModel.checkForChange()
            navController.popBackStack()
        }
        binding.btnBackArrow.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    if (Utils.preferenceDbHelper.darkThemeFlag) ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                    else ContextCompat.getColor(requireActivity(), R.color.purple_700)
                )
                MotionEvent.ACTION_UP -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.transparent
                    )
                )
            }
            false
        }

        // About Image Button
        binding.btnAbout.setOnClickListener {
            navController.navigate(R.id.action_settingsFragment_to_aboutFragment)
        }
        binding.btnAbout.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    if (Utils.preferenceDbHelper.darkThemeFlag) ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                    else ContextCompat.getColor(requireActivity(), R.color.purple_700)
                )
                MotionEvent.ACTION_UP -> v.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.transparent
                    )
                )
            }
            false
        }

        return binding.root
    }
}