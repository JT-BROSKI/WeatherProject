package com.jtbroski.myapplication;

import android.content.Context;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class Utils {

    private static Utils instance;

    public static Date currentDate;
    public static DatabaseHelper dbHelper;

    // Mostly likely remove later unless database querying is significantly slower
    public static ArrayList<String> allCities;
    public static ArrayList<String> allCitiesA;
    public static ArrayList<String> allCitiesB;
    public static ArrayList<String> allCitiesC;
    public static ArrayList<String> allCitiesD;
    public static ArrayList<String> allCitiesE;
    public static ArrayList<String> allCitiesF;
    public static ArrayList<String> allCitiesG;
    public static ArrayList<String> allCitiesH;
    public static ArrayList<String> allCitiesI;
    public static ArrayList<String> allCitiesJ;
    public static ArrayList<String> allCitiesK;
    public static ArrayList<String> allCitiesL;
    public static ArrayList<String> allCitiesM;
    public static ArrayList<String> allCitiesN;
    public static ArrayList<String> allCitiesO;
    public static ArrayList<String> allCitiesP;
    public static ArrayList<String> allCitiesQ;
    public static ArrayList<String> allCitiesR;
    public static ArrayList<String> allCitiesS;
    public static ArrayList<String> allCitiesT;
    public static ArrayList<String> allCitiesU;
    public static ArrayList<String> allCitiesV;
    public static ArrayList<String> allCitiesW;
    public static ArrayList<String> allCitiesX;
    public static ArrayList<String> allCitiesY;
    public static ArrayList<String> allCitiesZ;

    private Utils(Context context) {
        dbHelper = new DatabaseHelper(context);
//        initializeCitiesList();
        populateAllCities(context);
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

    public static String roundStringNumberValue(String value) {
        int roundedValue =  (int)Math.round(Double.parseDouble(value));
        return String.valueOf(roundedValue);
    }

    public static boolean isCurrentDay(Date date) {
        String currentDay = formatDate(currentDate);
        String compareDay = formatDate(date);

        return currentDay.equals(compareDay);
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

    public static void setTimeZone(Date date) {
        calendar.setTime(date);
        hourFormat.setTimeZone(calendar.getTimeZone());
        dateFormat.setTimeZone(calendar.getTimeZone());
        weekDayFormat.setTimeZone(calendar.getTimeZone());
        weekDayDateFormat.setTimeZone(calendar.getTimeZone());
    }

    // Mostly likely remove later unless database querying is significantly slower
    private static ArrayList<String> getAlphabeticCityList(char firstLetter) {
        switch (firstLetter) {
            case 'A':
                return allCitiesA;

            case 'B':
                return allCitiesB;

            case 'C':
                return allCitiesC;

            case 'D':
                return allCitiesD;

            case 'E':
                return allCitiesE;

            case 'F':
                return allCitiesF;

            case 'G':
                return allCitiesG;

            case 'H':
                return allCitiesH;

            case 'I':
                return allCitiesI;

            case 'J':
                return allCitiesJ;

            case 'K':
                return allCitiesK;

            case 'L':
                return allCitiesL;

            case 'M':
                return allCitiesM;

            case 'N':
                return allCitiesN;

            case 'O':
                return allCitiesO;

            case 'P':
                return allCitiesP;

            case 'Q':
                return allCitiesQ;

            case 'R':
                return allCitiesR;

            case 'S':
                return allCitiesS;

            case 'T':
                return allCitiesT;

            case 'U':
                return allCitiesU;

            case 'V':
                return allCitiesV;

            case 'W':
                return allCitiesW;

            case 'X':
                return allCitiesX;

            case 'Y':
                return allCitiesY;

            case 'Z':
                return allCitiesZ;

            default:
                return null;
        }
    }

    // Mostly likely remove later unless database querying is significantly slower
    private void initializeCitiesList() {
        allCitiesA = new ArrayList<>();
        allCitiesB = new ArrayList<>();
        allCitiesC = new ArrayList<>();
        allCitiesD = new ArrayList<>();
        allCitiesE = new ArrayList<>();
        allCitiesF = new ArrayList<>();
        allCitiesG = new ArrayList<>();
        allCitiesH = new ArrayList<>();
        allCitiesI = new ArrayList<>();
        allCitiesJ = new ArrayList<>();
        allCitiesK = new ArrayList<>();
        allCitiesL = new ArrayList<>();
        allCitiesM = new ArrayList<>();
        allCitiesN = new ArrayList<>();
        allCitiesO = new ArrayList<>();
        allCitiesP = new ArrayList<>();
        allCitiesQ = new ArrayList<>();
        allCitiesR = new ArrayList<>();
        allCitiesS = new ArrayList<>();
        allCitiesT = new ArrayList<>();
        allCitiesU = new ArrayList<>();
        allCitiesV = new ArrayList<>();
        allCitiesW = new ArrayList<>();
        allCitiesX = new ArrayList<>();
        allCitiesY = new ArrayList<>();
        allCitiesZ = new ArrayList<>();
    }

    // Mostly likely remove later unless database querying is significantly slower
    private static void populateAllCities(Context context) {
        allCities = new ArrayList<>();

        InputStream inputStream = context.getResources().openRawResource(R.raw.countries_and_cities);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<String> cityList;
            // Parse the data into jsonobject to get original data in form of json.
            JSONObject countries = new JSONObject(byteArrayOutputStream.toString());
            Iterator interator = countries.keys();
            while (interator.hasNext()) {
                String country = interator.next().toString();
                JSONArray cities = countries.getJSONArray(country);

                for (int i = 0; i < cities.length(); i++) {
                    String city = cities.getString(i);
                    char firstLetter = city.charAt(0);

                    cityList = getAlphabeticCityList(firstLetter);

                    if (cityList != null) {
                        cityList.add(city + ", " + country);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Calendar calendar = Calendar.getInstance();
    private static DateFormat hourFormat = new SimpleDateFormat("h a");
    private static DateFormat dateFormat = new SimpleDateFormat("M/dd");
    private static DateFormat weekDayFormat = new SimpleDateFormat("EEE");
    private static DateFormat weekDayDateFormat = new SimpleDateFormat("EEE dd");
}
