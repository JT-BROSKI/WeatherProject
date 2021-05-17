package com.jtbroski.myapplication.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherService {
    @GET("onecall/timemachine?")
    Call<ApiInfoConditions> getOneCallHistoricalWeatherData(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("dt") String dt,
            @Query("units") String units,
            @Query("appid") String app_id);

    @GET("onecall?")
    Call<ApiInfoConditions> getOneCallWeatherData(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("units") String units,
            @Query("appid") String app_id);
}
