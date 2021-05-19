package com.jtbroski.myapplication.ui.alert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.WeatherAlert
import com.jtbroski.myapplication.databinding.FragmentWeatherAlertBinding
import com.jtbroski.myapplication.ui.main.MainActivity

class WeatherAlertFragment : Fragment() {
    private lateinit var binding: FragmentWeatherAlertBinding

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

        (requireActivity() as MainActivity).invalidateOptionsMenu()

        return binding.root
    }
}