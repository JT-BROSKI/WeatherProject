package com.jtbroski.myapplication;

import java.util.Date;

public class Weather {

    private int id;
    private Date date;
    private Date sunrise;
    private Date sunset;
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

    public Weather(int id, Date date, Date sunrise, Date sunset, String temperatureCurrent, String temperatureMax, String temperatureMin, String feelsLike,
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getSunrise() {
        return sunrise;
    }

    public Date getSunset() {
        return sunset;
    }

    public String getTemperatureCurrent() {
        return temperatureCurrent;
    }

    public void setTemperatureCurrent(String temperatureCurrent) {
        this.temperatureCurrent = temperatureCurrent;
    }

    public String getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(String temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public String getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(String temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public String getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(String feelsLike) {
        this.feelsLike = feelsLike;
    }

    public String getPrecipChance() {
        return precipChance;
    }

    public void setPrecipChance(String precipChance) {
        this.precipChance = precipChance;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindScale() {
        return windScale;
    }

    public void setWindScale(String windScale) {
        this.windScale = windScale;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
