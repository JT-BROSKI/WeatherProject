package com.jtbroski.myapplication.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Generated using jsonschema2pojo
public class ApiInfoMinutelyConditions {
    @SerializedName("dt")
    @Expose
    private Integer dt;
    @SerializedName("precipitation")
    @Expose
    private Double precipitation;

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }
}
