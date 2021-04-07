package com.jtbroski.myapplication;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherAlert implements Parcelable {

    private String sender;
    private String event;
    private String startDate;
    private String endDate;
    private String description;

    public WeatherAlert(String sender, String event, String startDate, String endDate, String description) {
        this.sender = sender;
        this.event = event;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    protected WeatherAlert(Parcel in) {
        sender = in.readString();
        event = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        description = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
        dest.writeString(event);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WeatherAlert> CREATOR = new Creator<WeatherAlert>() {
        @Override
        public WeatherAlert createFromParcel(Parcel in) {
            return new WeatherAlert(in);
        }

        @Override
        public WeatherAlert[] newArray(int size) {
            return new WeatherAlert[size];
        }
    };

    public String getSender() {
        return sender;
    }

    public String getEvent() {
        return event;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }
}
