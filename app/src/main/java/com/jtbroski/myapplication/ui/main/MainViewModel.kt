package com.jtbroski.myapplication.ui.main

import android.app.Application
import android.database.Cursor
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jtbroski.myapplication.Utils
import com.jtbroski.myapplication.ui.FavoriteLocationListAdapter
import com.jtbroski.myapplication.ui.RecentLocationListAdapter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var _locationSelected = MutableLiveData<Location>()
    val locationSelected: LiveData<Location>
        get() = _locationSelected

    private var _closeDrawer = MutableLiveData(false)
    val closeDrawer: LiveData<Boolean>
        get() = _closeDrawer

    // Favorite Locations
    private lateinit var favoriteLocationListCursor: Cursor
    private var _favoriteLocationListAdapter: FavoriteLocationListAdapter
    val favoriteLocationListAdapter: FavoriteLocationListAdapter
        get() = _favoriteLocationListAdapter

    // Recent Locations
    private lateinit var recentLocationListCursor: Cursor
    private var _recentLocationListAdapter: RecentLocationListAdapter
    val recentLocationListAdapter: RecentLocationListAdapter
        get() = _recentLocationListAdapter

    var startUp = true

    init {
        _favoriteLocationListAdapter =
            FavoriteLocationListAdapter(
                getApplication<Application>().applicationContext,
                null,
                false
            )
        _favoriteLocationListAdapter.setViewModel(this)

        _recentLocationListAdapter =
            RecentLocationListAdapter(
                getApplication<Application>().applicationContext,
                null,
                false
            )
        _recentLocationListAdapter.setViewModel(this)
    }

    fun callWeatherApi(location: Location) {
        _locationSelected.value = location
    }

    fun closeCursors() {
        _favoriteLocationListAdapter.closeCursor()
        _recentLocationListAdapter.closeCursor()
    }

    fun closeDrawer() {
        _closeDrawer.value = true
    }

    // Updates the list views within the navigation drawer by update their cursors
    fun updateDrawerCursors() {
        favoriteLocationListCursor = Utils.preferenceDbHelper.favoriteLocations
        _favoriteLocationListAdapter.changeCursor(favoriteLocationListCursor)

        recentLocationListCursor = Utils.preferenceDbHelper.recentLocations
        _recentLocationListAdapter.changeCursor(recentLocationListCursor)
    }
}