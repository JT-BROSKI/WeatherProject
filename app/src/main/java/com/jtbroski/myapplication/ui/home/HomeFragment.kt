package com.jtbroski.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.databinding.FragmentHomeBinding
import com.jtbroski.myapplication.retrofit.ApiInfoConditions
import com.jtbroski.myapplication.ui.alert.WeatherAlertFragment
import java.net.URL
import java.util.*
import kotlin.math.roundToInt


class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    private lateinit var map: GoogleMap
    private var mapZoom: Int = 8
    private var weatherTileOverlay: TileOverlay? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this

        // Weather Map Material Card View
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.weather_map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // Navigation Drawer
        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toggle.syncState()
        binding.drawerLayout.addDrawerListener(toggle)

        viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        binding.homeViewModel = viewModel

        // Swipe Refresh Layout
        binding.pullDownRefresh.setOnRefreshListener { viewModel.callWeatherApi(Utils.lastQueriedLocation) }

        // Search Image Button
        binding.btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        binding.btnSearch.setOnTouchListener { v, event ->
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

        // My Location Image Button
        binding.btnMyLocation.setOnClickListener {
            Utils.locationName = null
            Utils.preferenceDbHelper.getCurrentLocation(requireActivity())
        }
        binding.btnMyLocation.setOnTouchListener { v, event ->
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

        // Settings Image Button
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
        }
        binding.btnSettings.setOnTouchListener { v, event ->
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

        // Alert Layout
        binding.alertLayout.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                WeatherAlertFragment.ALERT_DATA_ID,
                viewModel.weatherAlerts
            )
            findNavController().navigate(R.id.action_homeFragment_to_weatherAlertFragment, bundle)
        }

        // Dynamically set navigation view width
        val params = binding.navView.layoutParams
        params.width = (resources.displayMetrics.widthPixels * 0.8).roundToInt()
        binding.navView.layoutParams = params

        viewModel.updateDrawerCursors()

        setupRecyclerViews()
        setupObservers()

        return binding.root
    }

    override fun onDestroyView() {
        viewModel.closeCursors()
        super.onDestroyView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.setAllGesturesEnabled(false)

        updateWeatherTileOverlay()
        updateCurrentLocationOnWeatherMap(viewModel.forecastedWeather.value)    // We call this here just in case forecastedWeather has already been retrieved, but the map was not ready yet
    }

    // Create the tile provider for the weather maps
    private fun createTileProvider(): TileProvider {
        return object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL {
                val urlString = String.format(
                    Locale.US,
                    "https://tile.openweathermap.org/map/precipitation/%d/%d/%d.png?appid=%s",
                    zoom,
                    x,
                    y,
                    resources.getString(R.string.open_weather_map_key)
                )
                var url: URL? = null
                if (zoom == mapZoom) {
                    try {
                        url = URL(urlString)
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireActivity(),
                            "Failed the create the URL for Open Weather Maps precipitation layer.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                return url!!
            }
        }
    }

    // Load current condition's weather icon image
    private fun loadCurrentConditionsIcon() {
        Glide.with(requireActivity().applicationContext)
            .asBitmap()
            .load(Utils.createWeatherIconUrl(viewModel.currentConditionsIcon.value))
            .into(binding.currentConditionsImage)
    }

    // Reset the scroll view by scrolling to the top
    private fun resetScrollView() {
        // We need to use "post" here to ensure that a runnable is added to the thread queue
        // This ensures that this task will execute after previously queued task have properly executed (i.e weather tile loading)
        binding.scrollView.post { binding.scrollView.fullScroll(ScrollView.FOCUS_UP) }
    }

    private fun setupRecyclerViews() {
        binding.dailyConditionsRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.dailyConditionsRecyclerView.isFocusable =
            false     // this is set to false so that it does not mess with the scroll view's scroll position upon updating its data

        binding.hourlyConditionsRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyConditionsRecyclerView.isFocusable =
            false    // this is set to false so that it does not mess with the scroll view's scroll position upon updating its data
    }

    private fun setupObservers() {
        // Update Current Conditions Material Card
        binding.homeViewModel?.forecastedWeather?.observe(viewLifecycleOwner, {
            viewModel.updateWeatherConditions()
            updateCurrentLocationOnWeatherMap(viewModel.forecastedWeather.value)
        })
        binding.homeViewModel?.currentConditionsIcon?.observe(viewLifecycleOwner, {
            loadCurrentConditionsIcon()
        })

        binding.homeViewModel?.closeDrawer?.observe(viewLifecycleOwner, { close ->
            if (close) binding.drawerLayout.closeDrawer(GravityCompat.START)
        })
        binding.homeViewModel?.resetScrollView?.observe(viewLifecycleOwner, { refresh ->
            if (refresh) resetScrollView()
        })
        binding.homeViewModel?.showSplash?.observe(viewLifecycleOwner, { showSplash ->
            if (!showSplash) binding.splash.visibility = View.GONE
        })
        binding.homeViewModel?.stopSwipeLayoutRefresh?.observe(viewLifecycleOwner, { stopRefresh ->
            if (stopRefresh && binding.pullDownRefresh.isRefreshing) binding.pullDownRefresh.isRefreshing =
                false
        })
    }

    // Update where the google map fragment is zoomed into
    private fun updateCurrentLocationOnWeatherMap(forecast: ApiInfoConditions?) {
        if (forecast != null && this::map.isInitialized) {
            val latitude = forecast.lat
            val longitude = forecast.lon
            val location = LatLng(latitude, longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mapZoom.toFloat()))
        }
    }

    // Updates the weather tile overlay for our google maps
    private fun updateWeatherTileOverlay() {
        weatherTileOverlay?.remove()

        val tileOverlayOptions = TileOverlayOptions().tileProvider(createTileProvider())
        tileOverlayOptions.transparency(0.5f)
        weatherTileOverlay = map.addTileOverlay(tileOverlayOptions)
    }
}