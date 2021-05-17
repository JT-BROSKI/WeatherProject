package com.jtbroski.myapplication.retrofit;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Generated using jsonschema2pojo
public class ApiInfoConditions {
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lon")
    @Expose
    private Double lon;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("timezone_offset")
    @Expose
    private Integer timezoneOffset;
    @SerializedName("current")
    @Expose
    private ApiInfoCurrentConditions current;
    @SerializedName("minutely")
    @Expose
    private List<ApiInfoMinutelyConditions> minutely = null;
    @SerializedName("hourly")
    @Expose
    private List<ApiInfoHourlyConditions> hourly = null;
    @SerializedName("daily")
    @Expose
    private List<ApiInfoDailyConditions> daily = null;
    @SerializedName("alerts")
    @Expose
    private List<ApiInfoAlerts> alerts = null;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Integer getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(Integer timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public ApiInfoCurrentConditions getCurrent() {
        return current;
    }

    public void setCurrent(ApiInfoCurrentConditions current) {
        this.current = current;
    }

    public List<ApiInfoMinutelyConditions> getMinutely() {
        return minutely;
    }

    public void setMinutely(List<ApiInfoMinutelyConditions> minutely) {
        this.minutely = minutely;
    }

    public List<ApiInfoHourlyConditions> getHourly() {
        return hourly;
    }

    public void setHourly(List<ApiInfoHourlyConditions> hourly) {
        this.hourly = hourly;
    }

    public List<ApiInfoDailyConditions> getDaily() {
        return daily;
    }

    public void setDaily(List<ApiInfoDailyConditions> daily) {
        this.daily = daily;
    }

    public List<ApiInfoAlerts> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<ApiInfoAlerts> alerts) {
        this.alerts = alerts;
    }
}
