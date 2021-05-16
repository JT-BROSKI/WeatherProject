package com.jtbroski.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.jtbroski.myapplication.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)
        binding.lifecycleOwner = this

        // Toolbar Back Arrow
        binding.btnBackArrow.setOnClickListener {
            findNavController().navigate(R.id.action_aboutFragment_to_settingsFragment)
        }
        binding.btnBackArrow.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.setBackgroundColor(
                    if (Utils.preferenceDbHelper.darkThemeFlag) ContextCompat.getColor(requireActivity(), R.color.black)
                    else ContextCompat.getColor(requireActivity(), R.color.purple_700)
                )
                MotionEvent.ACTION_UP -> v.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.transparent))
            }
            false
        }

        return binding.root
    }
}