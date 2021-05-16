package com.jtbroski.myapplication

import android.app.Application
import android.database.Cursor
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private var _refreshHomeFragment = MutableLiveData<Boolean>()
    val refreshHomeFragment: LiveData<Boolean>
        get() = _refreshHomeFragment

    private var _searchFilterAdapter: SearchFilterAdapter
    val searchFilterAdapter: SearchFilterAdapter
        get() = _searchFilterAdapter

    private lateinit var citiesFilteredCursor: Cursor

    val refreshHomeFragmentFromAdapter: LiveData<Boolean>

    init {
        _searchFilterAdapter =
            SearchFilterAdapter(getApplication<Application>().applicationContext, null, false)
        refreshHomeFragmentFromAdapter = _searchFilterAdapter.refreshHomeFragment
    }

    fun updateFilterCursor(text: CharSequence) {
        citiesFilteredCursor =
            if (text.length > 2) Utils.locationDbHelper.getCitiesFilteredCursor(text.toString())
            else Utils.locationDbHelper.getCitiesFilteredCursor("")
        searchFilterAdapter.changeCursor(citiesFilteredCursor)
    }

    fun onEnterPressed(location: String, actionId: Int) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            searchFilterAdapter.closeCursor()

            // Check whether the geocoder can find a location matching the string within the EditText
            if (Utils.checkLocationValidity(location)) {
                _refreshHomeFragment.value = true
            } else {
                Toast.makeText(
                    getApplication<Application>().applicationContext,
                    "Unable to find any locations matching with $location", Toast.LENGTH_SHORT
                ).show()
            }
        }
        false
    }
}