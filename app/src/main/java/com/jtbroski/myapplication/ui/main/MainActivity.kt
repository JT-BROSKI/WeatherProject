package com.jtbroski.myapplication.ui.main

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.jtbroski.myapplication.PreferenceDatabaseHelper
import com.jtbroski.myapplication.R
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.databinding.ActivityMainBinding
import com.jtbroski.myapplication.ui.about.AboutFragment
import com.jtbroski.myapplication.ui.alert.WeatherAlertFragment
import com.jtbroski.myapplication.ui.home.HomeFragment
import com.jtbroski.myapplication.ui.home.HomeViewModel
import com.jtbroski.myapplication.ui.search.SearchFragment
import com.jtbroski.myapplication.ui.search.SearchViewModel
import com.jtbroski.myapplication.ui.settings.SettingsFragment
import com.jtbroski.myapplication.ui.settings.SettingsViewModel
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private var isDrawerLocked = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val navController: NavController by lazy { (supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController }
    private val navHostFragment: NavHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment }

    // View Models
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
//        this.deleteDatabase(PreferenceDatabaseHelper.DB_NAME);  // Kept for debugging purposes
//        this.deleteDatabase(LocationDatabaseHelper.DB_NAME);    // Kept for debugging purposes
        Utils.initialize(this)
        updateTheme(Utils.preferenceDbHelper.darkThemeFlag)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        binding.mainViewModel = viewModel

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerToggle.syncState()
        binding.drawerLayout.addDrawerListener(drawerToggle)

        // Dynamically set navigation view width
        val params = binding.navView.layoutParams
        params.width = (resources.displayMetrics.widthPixels * 0.8).roundToInt()
        binding.navView.layoutParams = params

        setSupportActionBar(binding.toolbar)

        // If this is on startup, don't display the toolbar title as we will be in the home fragment
        // Else, if the settings fragment is being hosted, set the toolbar title to "Settings"
        if (viewModel.startUp) {
            supportActionBar?.setDisplayShowTitleEnabled(false)
        } else {
            val fragments = navHostFragment.childFragmentManager.fragments
            for (fragment in fragments)
                if (fragment is SettingsFragment)
                    supportActionBar?.setTitle(R.string.settings_title)
        }

        viewModel.closeDrawer.observe(this, { close -> if (close) binding.drawerLayout.closeDrawer(GravityCompat.START) })
        viewModel.locationSelected.observe(this, { selectedLocation ->
            callWeatherApi(selectedLocation)
        })

        homeViewModel.closeCursors.observe(this, { close ->
            if (close) viewModel.closeCursors()
        })
        homeViewModel.homeReturned.observe(this, { returned ->
            if (returned) viewModel.updateDrawerCursors()
        })

        searchViewModel.refreshHomeFragment.observe(this, { refreshHomeFragment ->
            if (refreshHomeFragment) callWeatherApi(Utils.lastQueriedLocation)
        })
        searchViewModel.refreshHomeFragmentFromAdapter.observe(this, { refreshHomeFragment ->
                if (refreshHomeFragment) callWeatherApi(Utils.lastQueriedLocation)
        })

        settingsViewModel.rbDarkIsChecked.observe(this, { isDark -> updateTheme(isDark) })
        settingsViewModel.refreshHomeFragment.observe(this, { refreshHomeFragment ->
            if (refreshHomeFragment) callWeatherApi(Utils.lastQueriedLocation)
        })
    }

    override fun onBackPressed() {
        val fragments = navHostFragment.childFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible && fragment is AboutFragment) {
                invalidateOptionsMenu()
            }

            if (fragment.isVisible && fragment is SearchFragment) {
                invalidateOptionsMenu()
            }

            if (fragment.isVisible && fragment is SettingsFragment)  {
                settingsViewModel.checkForChange()
                invalidateOptionsMenu()
            }

            if (fragment.isVisible && fragment is WeatherAlertFragment) {
                invalidateOptionsMenu()
            }
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        hideAllMenuItems(menu)
        val fragments = navHostFragment.childFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible && fragment is HomeFragment) {
                menu.findItem(R.id.search).isVisible = true
                menu.findItem(R.id.myLocation).isVisible = true
                menu.findItem(R.id.settings).isVisible = true
                enableNavigationDrawer()
                break
            }

            if (fragment.isVisible && fragment is SearchFragment) {
                menu.findItem(R.id.searchView).isVisible = true
                setUpSearchView(menu)
                disableNavigationDrawer()
                break
            }

            if (fragment.isVisible && fragment is SettingsFragment) {
                menu.findItem(R.id.about).isVisible = true
                disableNavigationDrawer()
                break
            }

            if (fragment.isVisible && fragment is WeatherAlertFragment) {
                disableNavigationDrawer()
                supportActionBar?.setDisplayShowTitleEnabled(true)
                supportActionBar?.setTitle(R.string.weather_alert_title)
                break
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->  {
                if (isDrawerLocked) {
                    // If the up button was hit in the settings fragment, check for changes
                    var isSettingsOrAlerts = false      // This boolean is to allow the settings title to disappear more seamlessly
                    val fragments = navHostFragment.childFragmentManager.fragments
                    for (fragment in fragments) {
                        if (fragment.isVisible && fragment is SettingsFragment) {
                            settingsViewModel.checkForChange()
                            isSettingsOrAlerts = true
                        }

                        if (fragment.isVisible && fragment is AboutFragment) {
                            supportActionBar?.setTitle(R.string.settings_title)
                        }

                        if (fragment.isVisible && fragment is WeatherAlertFragment) {
                            isSettingsOrAlerts = true
                        }
                    }

                    navController.popBackStack()
                    hideKeyboard()
                    invalidateOptionsMenu()

                    if (isSettingsOrAlerts)
                        supportActionBar?.setDisplayShowTitleEnabled(false)
                } else {
                    if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.open()
                    }
                }
            }
            R.id.search -> navController.navigate(R.id.action_homeFragment_to_searchFragment)
            R.id.myLocation -> {
                Utils.locationName = null
                Utils.preferenceDbHelper.getCurrentLocation(this)
            }
            R.id.settings -> {
                navController.navigate(R.id.action_homeFragment_to_settingsFragment)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                supportActionBar?.setTitle(R.string.settings_title)
            }
            R.id.about -> {
                navController.navigate(R.id.action_settingsFragment_to_aboutFragment)
                supportActionBar?.setDisplayShowTitleEnabled(true)
                supportActionBar?.setTitle(R.string.about_title)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PreferenceDatabaseHelper.REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utils.preferenceDbHelper.requestCurrentLocation(this)
            } else {
                Toast.makeText(
                    this,
                    "My location permissions denied. Go to system settings to update location sharing permissions for My Location functionality.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // Calls the OpenWeather API in the home fragment and updates all weather conditions
    fun callWeatherApi(location: Location?) {
        homeViewModel.callWeatherApi(location!!)
    }

    // Updates the list views within the home fragment's navigation drawer by update their cursors
    fun updateNavigationListViews() {
        homeViewModel.updateDrawerCursors()
    }

    // Disable the navigation drawer
    private fun disableNavigationDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle.isDrawerIndicatorEnabled = false
        drawerToggle.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
        isDrawerLocked = true
    }

    // Enable the navigation drawer
    private fun enableNavigationDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        if (!viewModel.startUp) {
            supportActionBar?.setDisplayShowHomeEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        } else { viewModel.startUp = false}
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.toolbarNavigationClickListener = null
        isDrawerLocked = false
    }

    // Hide the soft keyboard (mainly used if in the search fragment)
    private fun hideKeyboard() {
        val inputMethodManager = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val windowToken = findViewById<View>(android.R.id.content).windowToken
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    // Hide all the menu items
    private fun hideAllMenuItems(menu: Menu) {
        menu.findItem(R.id.search).isVisible = false
        menu.findItem(R.id.myLocation).isVisible = false
        menu.findItem(R.id.settings).isVisible = false
        menu.findItem(R.id.searchView).isVisible = false
        menu.findItem(R.id.about).isVisible = false
    }

    // Setup the listeners for the search view within the search fragment
    private fun setUpSearchView(menu: Menu) {
        val searchViewItem = menu.findItem(R.id.searchView)
        val searchView = searchViewItem.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_hint)
        searchView.isIconifiedByDefault = false
        searchView.requestFocus()                   // This has to be called after isIconifiedByDefault is set to false so that thew search view can gain text focus
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.setOnQueryTextListener( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchViewModel.onEnterPressed(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchViewModel.updateFilterCursor(newText)
                return true
            }
        })
    }

    // Updates the theme of the activity
    private fun updateTheme(isDark: Boolean) {
        if (isDark) {
            setTheme(R.style.Theme_UI_Dark)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            setTheme(R.style.Theme_UI_Light)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}