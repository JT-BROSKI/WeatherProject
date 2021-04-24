package com.jtbroski.myapplication;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    private static Utils instance;
    private static Context mContext;
    private static Geocoder geocoder;

    public static boolean startUp = true;
    public static Calendar currentDate;
    public static LocationDatabaseHelper locationDbHelper;
    public static PreferenceDatabaseHelper preferenceDbHelper;
    public static Location lastQueriedLocation;
    public static String locationName;      // Used as a reference for updating the current location based on what the geocoder finds
    public static String timeZone;

    private Utils(Context context) {
        updateContext(context);
        lastQueriedLocation = new Location("");
        locationName = null;
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new Utils(context);
        } else {
            updateContext(context);
        }
    }

    public static void addMyLocationToRecentLocations(Context context, Location currentLocation) {
        ArrayList<Address> addressList;
        try {
            addressList = (ArrayList<Address>) geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to geocode current location.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (addressList.size() > 0) {
            Address currentAddress = addressList.get(0);

            double latitude = currentAddress.getLatitude();
            double longitude = currentAddress.getLongitude();

            preferenceDbHelper.updateRecentLocations(parseAddressName(currentAddress), latitude, longitude);
            ((MainActivity) context).updateNavigationListViews();
        }
    }

    // Check whether the geocoder can find a location matching the string parameter
    // If the geocoder can find a location return true, else return false
    public static boolean checkLocationValidity(String location) {
        boolean isValid = false;
        ArrayList<Address> addressList;
        try {
            addressList = (ArrayList<Address>) geocoder.getFromLocationName(location, 10);
        } catch (Exception e) {
            return isValid;
        }

        if (addressList.size() > 0) {
            Utils.locationName = parseAddressName(addressList.get(0));

            double latitude = addressList.get(0).getLatitude();
            double longitude = addressList.get(0).getLongitude();

            Utils.lastQueriedLocation.setLatitude(latitude);
            Utils.lastQueriedLocation.setLongitude(longitude);

            preferenceDbHelper.updateRecentLocations(locationName, latitude, longitude);

            isValid = true;
        }

        return isValid;
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
        } else if (valueInt < 68) {
            return "(NE)";
        } else if (valueInt <= 121) {
            return "(E)";
        } else if (valueInt < 158) {
            return "(SE)";
        } else if (valueInt <= 202) {
            return "(S)";
        } else if (valueInt < 248) {
            return "(SW)";
        } else if (valueInt <= 292) {
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

    public static String getCurrentDayMidnight(Context context) {
        String time = "";

        LocalDateTime now = LocalDateTime.now();
        now = now.minusHours(now.getHour());
        now = now.minusMinutes(now.getMinute());
        now = now.minusSeconds(now.getSecond());
        now = now.minusNanos(now.getNano());

        String format = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        try {
            Calendar previousDayStart = Calendar.getInstance();
            previousDayStart.setTime(formatter.parse(now.toString()));
            time = String.valueOf(previousDayStart.getTimeInMillis() / 1000L);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to parse local date time for current midnight calendar object.", Toast.LENGTH_SHORT).show();
        }

        return time;
    }

    public static String getPreviousThreeHours(Context context) {
        String time = "";

        LocalDateTime now = LocalDateTime.now();
        now = now.minusHours(1);
        now = now.minusMinutes(now.getMinute());
        now = now.minusSeconds(now.getSecond());
        now = now.minusNanos(now.getNano());

        String format = "yyyy-MM-dd'T'HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.US);
        try {
            Calendar previousDayStart = Calendar.getInstance();
            previousDayStart.setTime(formatter.parse(now.toString()));
            time = String.valueOf(previousDayStart.getTimeInMillis() / 1000L);
        } catch (Exception e) {
            Toast.makeText(context, "Failed to parse local date time for previous three hour calendar object.", Toast.LENGTH_SHORT).show();
        }

        return time;
    }

    public static boolean isCurrentDay(Calendar date) {
        String currentDay = formatDate(currentDate);
        String compareDay = formatDate(date);

        return currentDay.equals(compareDay);
    }

    public static String parseAddressName(Address address) {
        String[] addressTokens = address.getAddressLine(0).split(",");

        String city;
        String admin;
        if (addressTokens.length == 4) {
            city = addressTokens[1].trim();
            admin = addressTokens[2].replace(address.getPostalCode(), "").trim();
        } else {
            city = address.getLocality();
            if (city == null) {
                city = address.getSubAdminArea();
            }

            admin = address.getAdminArea();
            if (admin == null) {
                admin = address.getCountryName();
            }

            if (city == null && admin != null) {
                city = address.getAdminArea();
                admin = address.getCountryName();
            }
        }

        return city + ", " + admin;
    }

    public static void refreshMainActivity() {
        ((MainActivity) mContext).recreate();
    }

    public static void removeContext() {
        mContext = null;
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

    public static void updateLastQueriedLocation(Context context, JSONObject data) {
        try {
            lastQueriedLocation.setLatitude(data.getDouble("lat"));
            lastQueriedLocation.setLongitude(data.getDouble("lon"));
        } catch (Exception e) {
            Toast.makeText(context, "Unable to update last queried location.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void updateContext(Context context) {
        geocoder = new Geocoder(context);
        locationDbHelper = new LocationDatabaseHelper(context);
        preferenceDbHelper = new PreferenceDatabaseHelper(context);
        mContext = context;
    }

    private static final DateFormat hourFormat = new SimpleDateFormat("h a", Locale.US);
    private static final DateFormat dateFormat = new SimpleDateFormat("M/dd", Locale.US);
    private static final DateFormat weekDayFormat = new SimpleDateFormat("EEE", Locale.US);
    private static final DateFormat weekDayDateFormat = new SimpleDateFormat("EEE dd", Locale.US);
}
