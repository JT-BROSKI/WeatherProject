package com.jtbroski.myapplication;

import java.util.Calendar;

public class Weather {

    private final Calendar date;
    private final Calendar sunrise;
    private final Calendar sunset;
    private final String temperatureCurrent;
    private final String temperatureMax;
    private final String temperatureMin;
    private final String precipChance;
    private final String windSpeed;
    private final String windDirection;
    private final String windScale;
    private final String icon;

    public Weather(Calendar date, Calendar sunrise, Calendar sunset, String temperatureCurrent, String temperatureMax, String temperatureMin,
                   String precipChance, String windSpeed, String windDirection, String windScale, String icon) {
        this.date = date;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.temperatureCurrent = temperatureCurrent;
        this.temperatureMax = temperatureMax;
        this.temperatureMin = temperatureMin;
        this.precipChance = precipChance;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.windScale = windScale;
        this.icon = icon;
    }

    public Calendar getDate() {
        return date;
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

    public String getPrecipChance() {
        return precipChance;
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

    public String getIcon() {
        return icon;
    }
}
