package com.jtbroski.myapplication;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jtbroski.myapplication.retrofit.ApiInfoConditions;
import com.jtbroski.myapplication.retrofit.ApiInfoHourlyConditions;
import com.jtbroski.myapplication.retrofit.OpenWeatherService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherRepository {
    private final String OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";

    private OpenWeatherService openWeatherService;
    private MutableLiveData<ApiInfoConditions> forecastedWeather;
    private MutableLiveData<List<ApiInfoHourlyConditions>> historicalWeather;
    private MutableLiveData<List<ApiInfoHourlyConditions>> historicalWeatherBackup;

    public WeatherRepository() {
        forecastedWeather = new MutableLiveData<>();
        historicalWeather = new MutableLiveData<>();
        historicalWeatherBackup = new MutableLiveData<>();

        openWeatherService = new Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenWeatherService.class);
    }

    public void callWeatherApi(Location location, String measurement, String apiKey) {
        getHistoricalWeather(location, measurement, apiKey);
    }

    public void clearWeatherData() {
        historicalWeather.setValue(null);
        historicalWeatherBackup.setValue(null);
        forecastedWeather.setValue(null);
    }

    public LiveData<ApiInfoConditions> getForecastedWeather() {
        return forecastedWeather;
    }

    public LiveData<List<ApiInfoHourlyConditions>> getHistoricalWeather() {
        return historicalWeather;
    }

    public LiveData<List<ApiInfoHourlyConditions>> getHistoricalWeatherBackup() {
        return historicalWeatherBackup;
    }

    private void getForecastedWeather(Location location, String measurement, String apiKey) {
        Call<ApiInfoConditions> call = openWeatherService.getOneCallWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                measurement,
                apiKey);

        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    forecastedWeather.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                forecastedWeather.postValue(null);
            }
        });
    }

    private void getHistoricalWeather(Location location, String measurement, String apiKey) {
        Call<ApiInfoConditions> call = openWeatherService.getOneCallHistoricalWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                Utils.getCurrentDayMidnight(),
                measurement,
                apiKey);

        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiInfoHourlyConditions> hourlyConditions = response.body().getHourly();
                    if (hourlyConditions != null) {
                        historicalWeather.postValue(hourlyConditions);
                    }

                    getHistoricalWeatherBackup(location, measurement, apiKey);
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                historicalWeather.postValue(null);
            }
        });
    }

    private void getHistoricalWeatherBackup(Location location, String measurement, String apiKey) {
        Call<ApiInfoConditions> call = openWeatherService.getOneCallHistoricalWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                Utils.getPreviousThreeHours(),
                measurement,
                apiKey);

        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiInfoHourlyConditions> hourlyConditions = response.body().getHourly();
                    if (hourlyConditions != null) {
                        historicalWeatherBackup.postValue(hourlyConditions);
                    }

                    getForecastedWeather(location, measurement, apiKey);
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                historicalWeatherBackup.postValue(null);
            }
        });
    }
}
