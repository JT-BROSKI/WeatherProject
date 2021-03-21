package com.jtbroski.myapplication;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    private static Utils instance;

    public static Date currentDate;
    public static LocationDatabaseHelper locationDbHelper;
    public static PreferenceDatabaseHelper preferenceDbHelper;

    private Utils(Context context) {
        locationDbHelper = new LocationDatabaseHelper(context);
        preferenceDbHelper = new PreferenceDatabaseHelper(context);
    }

    public static Utils getInstance(Context content) {
        if (instance == null) {
            instance = new Utils(content);
        }

        return instance;
    }

    public static String convertWindDirection(String value) {
        int valueInt = Integer.parseInt(value);

        if (valueInt <= 22 || valueInt >= 338)
        {
            return "(N)";
        }
        else if (valueInt > 22 && valueInt < 68)
        {
            return "(NE)";
        }
        else if (valueInt >= 68 && valueInt <= 121)
        {
            return "(E)";
        }
        else if (valueInt > 121 && valueInt < 158)
        {
            return "(SE)";
        }
        else if (valueInt >= 158 && valueInt <= 202)
        {
            return "(S)";
        }
        else if (valueInt > 202 && valueInt < 248)
        {
            return "(SW)";
        }
        else if (valueInt >= 248 && valueInt <= 292)
        {
            return "(W)";
        }
        else
        {
            return "(NW)";
        }
    }

    public static String createWeatherIconUrl(String value) {
        final String prefix = "https://openweathermap.org/img/wn/";
        final String suffix = "@2x.png";

        return prefix + value + suffix;
    }

    public static String formatHour(Date date) {
        return hourFormat.format(date);
    }

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static String formatDayDailyCondition(Date date) {
        return weekDayDateFormat.format(date);
    }

    public static String formatDayHourlyCondition(Date day) {
        return weekDayFormat.format(day);
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
            Date previousDayStart = formatter.parse(now.toString());
            time = String.valueOf(previousDayStart.getTime() / 1000L);
        } catch (Exception e) {

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
            Date previousDayStart = formatter.parse(now.toString());
            time = String.valueOf(previousDayStart.getTime() / 1000L);
        } catch (Exception e) {

        }

        return time;
    }

    public static boolean isCurrentDay(Date date) {
        String currentDay = formatDate(currentDate);
        String compareDay = formatDate(date);

        return currentDay.equals(compareDay);
    }

    public static String roundStringNumberValue(String value) {
        int roundedValue =  (int)Math.round(Double.parseDouble(value));
        return String.valueOf(roundedValue);
    }

    public static void setTimeZone(Date date) {
        calendar.setTime(date);
        hourFormat.setTimeZone(calendar.getTimeZone());
        dateFormat.setTimeZone(calendar.getTimeZone());
        weekDayFormat.setTimeZone(calendar.getTimeZone());
        weekDayDateFormat.setTimeZone(calendar.getTimeZone());
    }

    private static Calendar calendar = Calendar.getInstance();
    private static DateFormat hourFormat = new SimpleDateFormat("h a");
    private static DateFormat dateFormat = new SimpleDateFormat("M/dd");
    private static DateFormat weekDayFormat = new SimpleDateFormat("EEE");
    private static DateFormat weekDayDateFormat = new SimpleDateFormat("EEE dd");
}
