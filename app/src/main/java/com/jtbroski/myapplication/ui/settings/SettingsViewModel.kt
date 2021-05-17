package com.jtbroski.myapplication.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jtbroski.myapplication.Utils

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private var _refreshHomeFragment = MutableLiveData<Boolean>()
    val refreshHomeFragment: LiveData<Boolean>
        get() = _refreshHomeFragment

    private var _rbImperialIsChecked = MutableLiveData<Boolean>()
    val rbImperialIsChecked: LiveData<Boolean>
        get() = _rbImperialIsChecked

    private var _rbMetricIsChecked = MutableLiveData<Boolean>()
    val rbMetricIsChecked: LiveData<Boolean>
        get() = _rbMetricIsChecked

    private var _rbLightIsChecked = MutableLiveData<Boolean>()
    val rbLightIsChecked: LiveData<Boolean>
        get() = _rbLightIsChecked

    private var _rbDarkIsChecked = MutableLiveData<Boolean>()
    val rbDarkIsChecked: LiveData<Boolean>
        get() = _rbDarkIsChecked

    private var isOriginallyImperial: Boolean = false

    init {
        val inImperial = Utils.preferenceDbHelper.imperialFlag
        val inDarkTheme = Utils.preferenceDbHelper.darkThemeFlag

        _rbImperialIsChecked.value = inImperial
        _rbMetricIsChecked.value = !inImperial
        _rbLightIsChecked.value = !inDarkTheme
        _rbDarkIsChecked.value = inDarkTheme
    }

    fun checkForChange() {
        val inImperial = Utils.preferenceDbHelper.imperialFlag
        if (inImperial != isOriginallyImperial) {
            _refreshHomeFragment.value = true
        }
    }

    fun onThemeGroupChange(inDarkTheme: Boolean) {
        _rbLightIsChecked.value = !inDarkTheme
        _rbDarkIsChecked.value = inDarkTheme
    }

    fun onUnitsGroupChange(inImperial: Boolean) {
        _rbImperialIsChecked.value = inImperial
        _rbMetricIsChecked.value = !inImperial
    }

    // Update the flag for determining the original units being used
    fun updateOriginalUnitsFlag() {
        isOriginallyImperial = Utils.preferenceDbHelper.imperialFlag
    }
}