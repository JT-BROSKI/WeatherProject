package com.jtbroski.myapplication;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Utils {

    private static Utils instance;
    private static Context mContext;

    public static Calendar currentDate;
    public static LocationDatabaseHelper locationDbHelper;
    public static PreferenceDatabaseHelper preferenceDbHelper;
    public static Location lastQueriedLocation;
    public static String locationName;      // Used as a reference for updating the current location based on what the geocoder finds
    public static String timeZone;

    private Utils(Context context) {
        locationDbHelper = new LocationDatabaseHelper(context);
        preferenceDbHelper = new PreferenceDatabaseHelper(context);
        lastQueriedLocation = new Location("");
        locationName = null;
    }

    public static Utils getInstance(Context context) {
        if (instance == null) {
            instance = new Utils(context);
            mContext = context;
        }

        return instance;
    }

    public static Calendar convertUnixTimeToLocalCalendarDate(long unixTime) {
        Calendar localDate = new GregorianCalendar(TimeZone.getTimeZone(timeZone));
        localDate.setTimeInMillis(unixTime);

        return localDate;
    }

    public static String convertWindDirection(String value) {
        int valueInt = Integer.parseInt(value);

        if (valueInt <= 22 || valueInt >= 338) {
            return "(N)";
        } else if (valueInt > 22 && valueInt < 68) {
            return "(NE)";
        } else if (valueInt >= 68 && valueInt <= 121) {
            return "(E)";
        } else if (valueInt > 121 && valueInt < 158) {
            return "(SE)";
        } else if (valueInt >= 158 && valueInt <= 202) {
            return "(S)";
        } else if (valueInt > 202 && valueInt < 248) {
            return "(SW)";
        } else if (valueInt >= 248 && valueInt <= 292) {
            return "(W)";
        } else {
            return "(NW)";
        }
    }

    public static String createWeatherIconUrl(String value) {
        final String prefix = "https://openweathermap.org/img/wn/";
        final String suffix = "@2x.png";

        return prefix + value + suffix;
    }

    public static String formatHour(Calendar date) {
        return hourFormat.format(date.getTime());
    }

    public static String formatDate(Calendar date) {
        return dateFormat.format(date.getTime());
    }

    public static String formatDayDailyCondition(Calendar date) {
        return weekDayDateFormat.format(date.getTime());
    }

    public static String formatDayHourlyCondition(Calendar day) {
        return weekDayFormat.format(day.getTime());
    }

    public static void forwardToWeatherApiCall(String query) {
        ((MainActivity) mContext).callWeatherApi(query);
    }

    public static String getCurrentDayMidnight() {
        String time = "";

        LocalDateTime now = LocalDateTime.now();
        now = now.minusHours(now.getHour());
        now = now.minusMinutes(now.getMinute());
        now = now.minusSeconds(now.getSecond());
        now = now.minusNanos(now.getNano());

        String format = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            Calendar previousDayStart = Calendar.getInstance();
            previousDayStart.setTime(formatter.parse(now.toString()));
            time = String.valueOf(previousDayStart.getTimeInMillis() / 1000L);
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to parse local date time for current midnight calendar object.", Toast.LENGTH_SHORT).show();
        }

        return time;
    }

    public static String getPreviousThreeHours() {
        String time = "";

        LocalDateTime now = LocalDateTime.now();
        now = now.minusHours(1);
        now = now.minusMinutes(now.getMinute());
        now = now.minusSeconds(now.getSecond());
        now = now.minusNanos(now.getNano());

        String format = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            Calendar previousDayStart = Calendar.getInstance();
            previousDayStart.setTime(formatter.parse(now.toString()));
            time = String.valueOf(previousDayStart.getTimeInMillis() / 1000L);
        } catch (Exception e) {
            Toast.makeText(mContext, "Failed to parse local date time for previous three hour calendar object.", Toast.LENGTH_SHORT).show();
        }

        return time;
    }

    public static boolean isCurrentDay(Calendar date) {
        String currentDay = formatDate(currentDate);
        String compareDay = formatDate(date);

        return currentDay.equals(compareDay);
    }

    public static void refreshMainActivity() {
        ((MainActivity) mContext).callWeatherApi(lastQueriedLocation);
    }

    public static String roundStringNumberValue(String value) {
        int roundedValue = (int) Math.round(Double.parseDouble(value));
        return String.valueOf(roundedValue);
    }

    public static void setTimeZone(Calendar date) {
        hourFormat.setTimeZone(date.getTimeZone());
        dateFormat.setTimeZone(date.getTimeZone());
        weekDayFormat.setTimeZone(date.getTimeZone());
        weekDayDateFormat.setTimeZone(date.getTimeZone());
    }

    public static void updateLastQueriedLocation(JSONObject data) {
        try {
            lastQueriedLocation.setLatitude(data.getDouble("lat"));
            lastQueriedLocation.setLongitude(data.getDouble("lon"));
        } catch (Exception e) {
            Toast.makeText(mContext, "Unable to update last queried location.", Toast.LENGTH_SHORT).show();
        }
    }

    private static DateFormat hourFormat = new SimpleDateFormat("h a");
    private static DateFormat dateFormat = new SimpleDateFormat("M/dd");
    private static DateFormat weekDayFormat = new SimpleDateFormat("EEE");
    private static DateFormat weekDayDateFormat = new SimpleDateFormat("EEE dd");
}
