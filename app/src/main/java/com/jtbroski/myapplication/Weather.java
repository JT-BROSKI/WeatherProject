package com.jtbroski.myapplication;

import java.util.Calendar;
import java.util.Date;

public class Weather {

    private int id;
    private Calendar date;
    private Calendar sunrise;
    private Calendar sunset;
    private String temperatureCurrent;
    private String temperatureMax;
    private String temperatureMin;
    private String feelsLike;
    private String precipChance;
    private String humidity;
    private String windSpeed;
    private String windDirection;
    private String windScale;
    private String description;
    private String icon;

    public Weather(int id, Calendar date, Calendar sunrise, Calendar sunset, String temperatureCurrent, String temperatureMax, String temperatureMin, String feelsLike,
                   String precipChance, String humidity, String windSpeed, String windDirection, String windScale, String description, String icon) {
        this.id = id;
        this.date = date;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.temperatureCurrent = temperatureCurrent;
        this.temperatureMax = temperatureMax;
        this.temperatureMin = temperatureMin;
        this.feelsLike = feelsLike;
        this.precipChance = precipChance;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.windScale = windScale;
        this.description = description;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Calendar getSunrise() {
        return sunrise;
    }

    public Calendar getSunset() {
        return sunset;
    }

    public String getTemperatureCurrent() {
        return temperatureCurrent;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public String getPrecipChance() {
        return precipChance;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getWindScale() {
        return windScale;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }
}
