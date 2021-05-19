package com.jtbroski.myapplication.ui.home

import android.app.Application
import android.database.Cursor
import android.location.Geocoder
import android.location.Location
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jtbroski.myapplication.*
import com.jtbroski.myapplication.retrofit.ApiInfoAlerts
import com.jtbroski.myapplication.retrofit.ApiInfoConditions
import com.jtbroski.myapplication.retrofit.ApiInfoDailyConditions
import com.jtbroski.myapplication.retrofit.ApiInfoHourlyConditions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private var weatherRepository: WeatherRepository

    private var fullDayHourlyConditions: ArrayList<ApiInfoHourlyConditions>
    private var hoursRecorded: ArrayList<Int>

    private var _closeCursors = MutableLiveData<Boolean>()
    val closeCursors: LiveData<Boolean>
        get() = _closeCursors

    private var _homeReturned = MutableLiveData<Boolean>()
    val homeReturned: LiveData<Boolean>
        get() = _homeReturned

    private var _resetScrollView = MutableLiveData(false)
    val resetScrollView: LiveData<Boolean>
        get() = _resetScrollView

    private var _showSplash = MutableLiveData(true)
    val showSplash: LiveData<Boolean>
        get() = _showSplash

    private var _stopSwipeLayoutRefresh = MutableLiveData(false)
    val stopSwipeLayoutRefresh: LiveData<Boolean>
        get() = _stopSwipeLayoutRefresh

    private var _currentLocation = MutableLiveData<String>()
    val currentLocation: LiveData<String>
        get() = _currentLocation

    // Weather Alerts
    private var _weatherAlert = MutableLiveData<String>()
    val weatherAlert: LiveData<String>
        get() = _weatherAlert

    private var _weatherAlertIsVisible = MutableLiveData(View.GONE)
    val weatherAlertIsVisible: LiveData<Int>
        get() = _weatherAlertIsVisible

    private var _weatherAlerts: ArrayList<WeatherAlert>
    val weatherAlerts: ArrayList<WeatherAlert>
        get() = _weatherAlerts

    // Current Conditions
    private var _currentConditionsIcon = MutableLiveData<String>()
    val currentConditionsIcon: LiveData<String>
        get() = _currentConditionsIcon

    private var _currentTemperature = MutableLiveData<String>()
    val currentTemperature: LiveData<String>
        get() = _currentTemperature

    private var _currentTemperatureScale = MutableLiveData<String>()
    val currentTemperatureScale: LiveData<String>
        get() = _currentTemperatureScale

    private var _currentTemperatureHighLow = MutableLiveData<String>()
    val currentTemperatureHighLow: LiveData<String>
        get() = _currentTemperatureHighLow

    private var _currentTemperatureDescription = MutableLiveData<String>()
    val currentTemperatureDescription: LiveData<String>
        get() = _currentTemperatureDescription

    private var _feelsLike = MutableLiveData<String>()
    val feelsLike: LiveData<String>
        get() = _feelsLike

    private var _currentPrecipitation = MutableLiveData<String>()
    val currentPrecipitation: LiveData<String>
        get() = _currentPrecipitation

    private var _currentHumidity = MutableLiveData<String>()
    val currentHumidity: LiveData<String>
        get() = _currentHumidity

    private var _currentWind = MutableLiveData<String>()
    val currentWind: LiveData<String>
        get() = _currentWind

    // Daily Conditions
    private var _dailyConditionsRecViewAdapter: DailyConditionsRecViewAdapter
    val dailyConditionsRecViewAdapter: DailyConditionsRecViewAdapter
        get() = _dailyConditionsRecViewAdapter

    // Hourly Conditions
    private var _hourlyConditionsRecViewAdapter: HourlyConditionsRecViewAdapter
    val hourlyConditionsRecViewAdapter: HourlyConditionsRecViewAdapter
        get() = _hourlyConditionsRecViewAdapter

    private var isImperial: Boolean = false

    var forecastedWeather: LiveData<ApiInfoConditions>
    private var historicalWeather: LiveData<List<ApiInfoHourlyConditions>>
    private var historicalWeatherBackup: LiveData<List<ApiInfoHourlyConditions>>

    init {
        hoursRecorded = ArrayList()
        fullDayHourlyConditions = ArrayList()
        _weatherAlerts = ArrayList()

        weatherRepository = WeatherRepository()
        forecastedWeather = weatherRepository.forecastedWeather
        historicalWeather = weatherRepository.historicalWeather
        historicalWeatherBackup = weatherRepository.historicalWeatherBackup

        _dailyConditionsRecViewAdapter =
            DailyConditionsRecViewAdapter(getApplication<Application>().applicationContext)
        _hourlyConditionsRecViewAdapter =
            HourlyConditionsRecViewAdapter(
                getApplication<Application>().applicationContext
            )

        val preferredLocation = Utils.preferenceDbHelper.preferredLocation
        if (preferredLocation != null) {
            callWeatherApi(preferredLocation)
        }
    }

    // Calls the OpenWeather API
    fun callWeatherApi(location: Location) {
        fullDayHourlyConditions.clear()
        hoursRecorded.clear()

        isImperial = Utils.preferenceDbHelper.imperialFlag
        val measurement = if (Utils.preferenceDbHelper.imperialFlag) "imperial" else "metric"

        // For some reason the forecastedWeather observer within the HomeFragment gets ran twice,
        // with the first run using the old weather data and the second run using the new weather data,
        // causing a weird bug with the daily conditions recycler view showing the first few conditions with the old weather data.
        // This visual bug is prevented by setting the observed life data to null before trying to populate it again.
        // The observer is still ran twice, however nothing happens due to null checks.
        weatherRepository.clearWeatherData()

        weatherRepository.callWeatherApi(
            location,
            measurement,
            getApplication<Application>().applicationContext.resources.getString(R.string.open_weather_map_key)
        )
    }

    fun closeCursors() {
        _closeCursors.value = true
    }

    // Updates the list views within the navigation drawer by update their cursors
    fun updateDrawerCursors() {
        _homeReturned.value = true
    }

    // Updates all weather conditions
    fun updateWeatherConditions() {
        _stopSwipeLayoutRefresh.value = true

        populateFullDayHourlyConditions()

        val forecast = forecastedWeather.value
        if (forecast != null) {
            Utils.timeZone = forecast.timezone
            Utils.updateLastQueriedLocation(forecast)

            updateCurrentConditions(forecast)
            updateCurrentLocation(forecast)
            updateDailyConditions(forecast.daily)
            updateHourlyConditions(forecast.hourly)
            updateWeatherAlerts(forecast.alerts)

            Utils.preferenceDbHelper.updatePreferredLocation(Utils.lastQueriedLocation)

            _resetScrollView.postValue(true)

            // Hide the splash image when the data has been loaded
            if (_showSplash.value == true) {
                _showSplash.value = false
            }
        }
    }

    // Populates an array list with the current and future hourly conditions
    private fun populateFullDayHourlyConditions() {
        val currentMidnightTime = Utils.getCurrentDayMidnight().toInt()

        val historicalHourly = historicalWeather.value
        if (historicalHourly != null)
            for (hourlyCondition in historicalHourly) {
                val time = hourlyCondition.dt
                if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                    fullDayHourlyConditions.add(hourlyCondition)
                    hoursRecorded.add(time)
                }
            }

        val historicalHourlyBackup = historicalWeatherBackup.value
        if (historicalHourlyBackup != null)
            for (hourlyCondition in historicalHourlyBackup) {
                val time = hourlyCondition.dt
                if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                    fullDayHourlyConditions.add(hourlyCondition)
                    hoursRecorded.add(time)
                }
            }

        val forecastedHourly = forecastedWeather.value?.hourly
        if (forecastedHourly != null)
            for (hourlyCondition in forecastedHourly) {
                val time = hourlyCondition.dt
                if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                    fullDayHourlyConditions.add(hourlyCondition)
                    hoursRecorded.add(time)
                }
            }

        if (fullDayHourlyConditions.size > 0)
            _dailyConditionsRecViewAdapter.sortFullDayHourConditions(
                fullDayHourlyConditions,
                currentMidnightTime
            )
    }

    // Updates the data within the Current Conditions Material Card View
    private fun updateCurrentConditions(forecast: ApiInfoConditions) {
        val currentConditions = forecast.current

        // Parse current date and set current time zone
        Utils.currentDate = Utils.convertUnixTimeToLocalCalendarDate(currentConditions.dt * 1000L)
        Utils.setTimeZone(Utils.currentDate)

        // Parse icon data
        _currentConditionsIcon.value = currentConditions.weather[0].icon

        // Parse current temperature
        _currentTemperature.value = currentConditions.temp.roundToInt().toString()
        _currentTemperatureScale.value = if (isImperial) "F" else "C"

        // Parse high and low temperature for today
        val today = forecast.daily[0].temp
        val high: String = today.max.roundToInt().toString() + "\u00B0"
        val low: String = today.min.roundToInt().toString() + "\u00B0"
        _currentTemperatureHighLow.value = "$high | $low"

        // Parse current weather description
        _currentTemperatureDescription.value =
            currentConditions.weather[0].description.uppercase(Locale.getDefault())

        // Parse current feels like temperature
        _feelsLike.value = "Feels like " + currentConditions.feelsLike.roundToInt() + "\u00B0"

        // Parse current precipitation chance
        val precipitationAverage: Double
        precipitationAverage = if (forecast.minutely != null) {
            val minutelyConditions = forecast.minutely

            // Calculate the precipitation chance based on the conditions within the next 30 minutes
            var chance = 0
            for (i in 0..29) {
                val precipitationChance = minutelyConditions[i].precipitation
                if (precipitationChance > 0) chance++
            }
            chance / 30.0 * 100
        } else {
            forecast.hourly[0].pop * 100
        }
        _currentPrecipitation.value = precipitationAverage.roundToInt().toString() + "%"

        // Parse current humidity
        _currentHumidity.value = currentConditions.humidity.toString() + "%"

        // Parse wind data
        val windSpeed = currentConditions.windSpeed.roundToInt()
        val windDirection = Utils.convertWindDirection(currentConditions.windDeg)
        val units = if (isImperial) "mph" else "kph"
        _currentWind.value = "$windSpeed $units $windDirection"
    }

    // Updates the location string within the App Bar
    private fun updateCurrentLocation(forecast: ApiInfoConditions) {
        try {
            val latitude = forecast.lat
            val longitude = forecast.lon

            val geocoder =
                Geocoder(getApplication<Application>().applicationContext, Locale.getDefault())
            val addressList =
                if (Utils.locationName == null) geocoder.getFromLocation(latitude, longitude, 1)
                else geocoder.getFromLocationName(Utils.locationName, 1)

            _currentLocation.setValue(Utils.parseAddressName(addressList[0]))
        } catch (e: Exception) {
            Toast.makeText(
                getApplication<Application>().applicationContext,
                "Failed to parse current location data",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Updates the data within the Daily Conditions Material Card View
    private fun updateDailyConditions(dailyConditions: List<ApiInfoDailyConditions>) {
        val dailyWeather = ArrayList<Weather>()

        var hasPrecipitation = false

        for (dailyCondition in dailyConditions) {
            // Parse current date
            val date = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.dt * 1000L)

            // Parse sunrise and sunset date
            val sunrise = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.sunrise * 1000L)
            val sunset = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.sunset * 1000L)

            // Parse high and low temperature
            val temperature = dailyCondition.temp
            val temperatureMax = temperature.max.roundToInt().toString()
            val temperatureMin = temperature.min.roundToInt().toString()

            // Parse precipitation chance
            val precipitationChance = (dailyCondition.pop * 100).roundToInt().toString()
            if (precipitationChance != "0")
                hasPrecipitation = true

            // Parse wind data
            val windSpeed = dailyCondition.windSpeed.roundToInt().toString()
            val windDirection = Utils.convertWindDirection(dailyCondition.windDeg)
            val windScale = if (isImperial) "mph" else "kph"

            // Parse icon data
            val icon = Utils.createWeatherIconUrl(dailyCondition.weather[0].icon)

            // Create new weather object and add to the array list
            val weather = Weather(
                date, sunrise, sunset, "", temperatureMax, temperatureMin,
                precipitationChance, windSpeed, windDirection, windScale, icon
            )
            dailyWeather.add(weather)
        }

        _dailyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation)
        _dailyConditionsRecViewAdapter.setDailyWeather(dailyWeather)
    }

    // Updates the data within the Hourly Conditions Material Card View
    private fun updateHourlyConditions(hourlyConditions: List<ApiInfoHourlyConditions>) {
        val hourlyWeather = ArrayList<Weather>()

        var hasPrecipitation = false

        for (hourlyCondition in hourlyConditions) {
            // Parse current date
            val date = Utils.convertUnixTimeToLocalCalendarDate(hourlyCondition.dt * 1000L)

            // Parse current temperature
            val temperatureCurrent = hourlyCondition.temp.roundToInt().toString()

            // Parse precipitation chance
            val precipitationChance = (hourlyCondition.pop * 100).roundToInt().toString()
            if (precipitationChance != "0")
                hasPrecipitation = true

            // Parse wind data
            val windSpeed = hourlyCondition.windSpeed.roundToInt().toString()
            val windDirection = Utils.convertWindDirection(hourlyCondition.windDeg)
            val windScale = if (isImperial) "mph" else "kph"

            // Parse icon data
            val icon = Utils.createWeatherIconUrl(hourlyCondition.weather[0].icon)

            // Create new weather object and add to the array list
            val weather = Weather(
                date, null, null, temperatureCurrent, "", "",
                precipitationChance, windSpeed, windDirection, windScale, icon
            )
            hourlyWeather.add(weather)
        }

        _hourlyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation)
        _hourlyConditionsRecViewAdapter.setHourlyWeather(hourlyWeather)
    }

    // Update the weather alerts notification
    private fun updateWeatherAlerts(alerts: List<ApiInfoAlerts>?) {
        if (alerts != null) {
            val firstAlert = alerts[0]
            _weatherAlert.value = firstAlert.event
            _weatherAlerts = ArrayList()
            val dateFormat = SimpleDateFormat("hh:mma z EEE, MMM d, yyyy", Locale.US)
            for (alert in alerts) {
                val sender = alert.senderName
                val title = alert.event
                val startDate = Utils.convertUnixTimeToLocalCalendarDate(alert.start * 1000L)
                val startString = dateFormat.format(startDate.time)
                val endDate = Utils.convertUnixTimeToLocalCalendarDate(alert.end * 1000L)
                val endString = dateFormat.format(endDate.time)
                val description = alert.description
                _weatherAlerts.add(WeatherAlert(sender, title, startString, endString, description))
            }
            _weatherAlertIsVisible.value = View.VISIBLE
            _resetScrollView.value = false
        } else {
            _weatherAlertIsVisible.value = View.GONE
        }
    }
}