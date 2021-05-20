package com.jtbroski.myapplication.ui.search

import android.app.Application
import android.database.Cursor
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jtbroski.myapplication.Utils

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private var _refreshHomeFragment = MutableLiveData<Boolean>()
    val refreshHomeFragment: LiveData<Boolean>
        get() = _refreshHomeFragment

    private var _searchFilterAdapter: SearchFilterAdapter
    val searchFilterAdapter: SearchFilterAdapter
        get() = _searchFilterAdapter

    private var _showLoadingCircle = MutableLiveData(false)
    val showLoadingCircle: LiveData<Boolean>
        get() = _showLoadingCircle

    private lateinit var citiesFilteredCursor: Cursor

    val refreshHomeFragmentFromAdapter: LiveData<Boolean>

    init {
        _searchFilterAdapter =
            SearchFilterAdapter(getApplication<Application>().applicationContext, null, false)
        refreshHomeFragmentFromAdapter = _searchFilterAdapter.refreshHomeFragment
    }

    // Display the swipe refresh layout's loading circle in the home fragment
    fun displayLoadingCircle() {
        _showLoadingCircle.value = true
        _showLoadingCircle.postValue(false)
    }

    fun updateFilterCursor(text: String) {
        citiesFilteredCursor =
            if (text.length > 2) Utils.locationDbHelper.getCitiesFilteredCursor(text)
            else Utils.locationDbHelper.getCitiesFilteredCursor("")
        searchFilterAdapter.changeCursor(citiesFilteredCursor)
    }

    fun onEnterPressed(location: String) {
        // Check whether the geocoder can find a location matching the string within the EditText
        if (Utils.checkLocationValidity(location)) {
            searchFilterAdapter.closeCursor()
            _refreshHomeFragment.value = true
            _refreshHomeFragment.postValue(false)
            displayLoadingCircle()
        } else {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Unable to find any locations matching with $location", Toast.LENGTH_SHORT
            ).show()
        }
    }
}