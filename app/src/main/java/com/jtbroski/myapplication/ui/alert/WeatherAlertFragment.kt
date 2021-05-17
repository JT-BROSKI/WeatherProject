package com.jtbroski.myapplication.ui.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.WeatherAlert
import com.jtbroski.myapplication.databinding.FragmentWeatherAlertBinding

class WeatherAlertFragment : Fragment() {
    private lateinit var binding: FragmentWeatherAlertBinding
    private val navController: NavController by lazy { findNavController() }

    companion object {
        const val ALERT_DATA_ID = "ALERT_DATA"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_weather_alert, container, false)
        binding.lifecycleOwner = this

        // Toolbar Back Arrow
        binding.btnBackArrow.setOnClickListener {
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

        val bundle = arguments
        if (bundle != null) {
            val weatherAlerts = bundle.getParcelableArrayList<WeatherAlert>(ALERT_DATA_ID)
            val weatherAlertRecViewAdapter =
                WeatherAlertRecViewAdapter(
                    requireActivity(),
                    weatherAlerts
                )
            binding.weatherAlertRecyclerView.adapter = weatherAlertRecViewAdapter
            binding.weatherAlertRecyclerView.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        }

        return binding.root
    }
}