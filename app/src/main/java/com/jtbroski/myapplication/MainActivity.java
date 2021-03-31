package com.jtbroski.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView txtCurrentLocation;

    private ImageView imgCurrentConditionsImage;

    private TextView txtCurrentTemperature;
    private TextView txtCurrentTemperatureScale;
    private TextView txtCurrentTemperatureHighLow;
    private TextView txtCurrentConditionsDescription;
    private TextView txtFeelsLike;
    private TextView txtPrecipitation;
    private TextView txtHumidity;
    private TextView txtWind;

    private RequestQueue queue;

    private DailyConditionsRecViewAdapter dailyConditionsRecViewAdapter;
    private RecyclerView dailyConditionsRecView;

    private HourlyConditionsRecViewAdapter hourlyConditionsRecViewAdapter;
    private RecyclerView hourlyConditionsRecView;
    private JSONArray fullyDayHourlyConditions;
    private ArrayList<Integer> hoursRecorded;

    private SwipeRefreshLayout swipeRefreshLayout;

    private Geocoder geocoder;
    private ArrayList<Address> addressList;

    private boolean isImperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        this.deleteDatabase("preferences.db");  // temporary database reset until full database has been created
        Utils.getInstance(this);
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        geocoder = new Geocoder(this);
        addressList = new ArrayList<>();

        // Toolbar Image Buttons
        ImageButton searchButton = findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        });
        searchButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(MainActivity.this, R.color.black)
                                                                                         : ContextCompat.getColor(MainActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
                        break;
                }
                return false;
            }
        });

        ImageButton myLocationButton = findViewById(R.id.btn_myLocation);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.locationName = null;
                Utils.preferenceDbHelper.getCurrentLocation();
            }
        });
        myLocationButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(MainActivity.this, R.color.black)
                                : ContextCompat.getColor(MainActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
                        break;
                }
                return false;
            }
        });

        ImageButton settingsButton = findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
        settingsButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(MainActivity.this, R.color.black)
                                : ContextCompat.getColor(MainActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.transparent));
                        break;
                }
                return false;
            }
        });

        // Current Conditions Material Card View
        txtCurrentLocation = findViewById(R.id.current_location);
        imgCurrentConditionsImage = findViewById(R.id.current_conditions_image);
        txtCurrentTemperature = findViewById(R.id.current_temperature);
        txtCurrentTemperatureScale = findViewById(R.id.temperature_scale);
        txtCurrentTemperatureHighLow = findViewById(R.id.current_temperature_high_low);
        txtCurrentConditionsDescription = findViewById(R.id.current_conditions_description);
        txtFeelsLike = findViewById(R.id.feels_like);
        txtPrecipitation = findViewById(R.id.precipitation_value);
        txtHumidity = findViewById(R.id.humidity_value);
        txtWind = findViewById(R.id.wind_data);

        // Hourly Conditions Material Card View
        hourlyConditionsRecViewAdapter = new HourlyConditionsRecViewAdapter(this);
        hourlyConditionsRecView = findViewById(R.id.hourly_conditions_recycler_view);
        hourlyConditionsRecView.setAdapter(hourlyConditionsRecViewAdapter);
        hourlyConditionsRecView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hourlyConditionsRecView.setFocusable(false);    // this is set to false so that it does not mess with the scroll view's scroll position upon updating its data

        // Daily Conditions Material Card View
        dailyConditionsRecViewAdapter = new DailyConditionsRecViewAdapter(this);
        dailyConditionsRecView = findViewById(R.id.daily_conditions_recycler_view);
        dailyConditionsRecView.setAdapter(dailyConditionsRecViewAdapter);
        dailyConditionsRecView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dailyConditionsRecView.setFocusable(false);     // this is set to false so that it does not mess with the scroll view's scroll position upon updating its data

        // Pull Down Swipe Layout
        swipeRefreshLayout = findViewById(R.id.pullDownRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callWeatherApi(Utils.lastQueriedLocation);
            }
        });

        queue = Volley.newRequestQueue(this);

        Location preferredLocation = Utils.preferenceDbHelper.getPreferredLocation();
        if (preferredLocation != null) {
            callWeatherApi(preferredLocation);
        }
    }

    // Calls the OpenWeather API and updates all weather conditions
    public void callWeatherApi(Location location) {
        fullyDayHourlyConditions = new JSONArray();
        hoursRecorded = new ArrayList<>();
        isImperial = Utils.preferenceDbHelper.getImperialFlag();

        final String API_KEY = "&appid=" + getApiKey();
        final String END_POINT = " https://api.openweathermap.org/data";
        final String VERSION = "2.5";
        final String TEMP_MEASUREMENT = "&units=" + (isImperial ? "imperial" : "metric");
        String coordinates = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        String currentMidnight = Utils.getCurrentDayMidnight();

        queue.add(constructHistoricalStringRequest(currentMidnight, END_POINT, VERSION, coordinates, TEMP_MEASUREMENT, API_KEY));
    }

    // Creates a Location object based on the string parameter passed in, then executes the callWeatherApi(Location location) function
    public void callWeatherApi(String location) {
        addressList.clear();

        try {
            addressList = (ArrayList<Address>) geocoder.getFromLocationName(location, 10);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Unable to get any locations matching with " + location, Toast.LENGTH_SHORT).show();
        }

        if (addressList.size() > 0) {
            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(addressList.get(0).getLatitude());
            newLocation.setLongitude(addressList.get(0).getLongitude());
            callWeatherApi(newLocation);    // TODO potentially implement preferred location saving here
        } else {
            Toast.makeText(this, "Unable to get any geocoded location data for " + location, Toast.LENGTH_SHORT).show();
        }
    }

    public void resetScrollView() {
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setFocusable(false);     // this is necessary so that the scroll view contents don't turn dim upon programmatically scrolling
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    // Construct the API string request for the current and future weather conditions
    private StringRequest constructForecastStringRequest(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String ONE_CALL = "onecall?";
        String urlCurrent = endPoint + "/" + version + "/" + ONE_CALL + coordinates + measurement + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlCurrent,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }

                            JSONObject result = new JSONObject(response);
                            JSONObject currentConditions = result.getJSONObject("current");
                            JSONArray hourlyConditions = result.getJSONArray("hourly");
                            JSONArray dailyConditions = result.getJSONArray("daily");

                            parseCurrentTimeZone(result);
                            populateFinalFullDayHourlyConditionsJsonArray(hourlyConditions, currentMidnight);
                            Utils.updateLastQueriedLocation(result);

                            boolean minutelyAvailable = true;
                            JSONArray precipConditions = null;
                            try {
                                precipConditions = result.getJSONArray("minutely");
                            } catch (JSONException e) {
                                precipConditions = hourlyConditions;
                                minutelyAvailable = false;
                            }

                            updateCurrentConditions(currentConditions, precipConditions, dailyConditions, minutelyAvailable);
                            updateCurrentLocation(result);
                            updateDailyConditions(dailyConditions);
                            updateHourlyConditions(hourlyConditions);

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Failed to parse current weather data.", Toast.LENGTH_SHORT).show();
                        } finally {
                            resetScrollView();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                    }
                });

        return stringRequest;
    }

    // Construct the string request for past hourly conditions
    private StringRequest constructHistoricalStringRequest(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String HISTORICAL_ONE_CALL = "onecall/timemachine?";
        String time = "&dt=" + currentMidnight;
        String urlHistorical = endPoint + "/" + version + "/" + HISTORICAL_ONE_CALL + coordinates + time + measurement + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlHistorical,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject result = new JSONObject(response);
                            JSONArray hourlyConditions = result.getJSONArray("hourly");
                            populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions, currentMidnight);

                            queue.add(constructHistoricalStringRequestBackup(currentMidnight, endPoint, version, coordinates, measurement, apiKey));
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to parse historic weather data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                    }
                });

        return stringRequest;
    }

    // Construct the backup string request for past hourly conditions just in case the normal historical string request missed a few hours
    private StringRequest constructHistoricalStringRequestBackup(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String HISTORICAL_ONE_CALL = "onecall/timemachine?";
        String timeThreeHours = "&dt=" + Utils.getPreviousThreeHours();
        String urlPreviousThreeHours = endPoint + "/" + version + "/" + HISTORICAL_ONE_CALL + coordinates + timeThreeHours + measurement + apiKey;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlPreviousThreeHours,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject result = new JSONObject(response);
                            JSONArray hourlyConditions = result.getJSONArray("hourly");
                            populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions, currentMidnight);

                            queue.add(constructForecastStringRequest(currentMidnight, endPoint, version, coordinates, measurement, apiKey));
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to parse weather data three hours in the past.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                    }
                });

        return stringRequest;
    }

    // Reads and return the Open Weather Map API key from OpenWeatherMap-Info.xml within assets
    private String getApiKey() {
        String apiKey = "";
        String fileName = "OpenWeatherMap-Info.xml";

        InputStream inputStream = null;
        try {
            inputStream = getAssets().open(fileName);
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = xmlPullParserFactory.newPullParser();
            xmlPullParser.setInput(inputStream, null);

            int event = xmlPullParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = xmlPullParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("apiKey")) {
                            apiKey = xmlPullParser.nextText();
                        }
                        break;
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Unable to the API key.", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to close input stream.", Toast.LENGTH_SHORT).show();
            }
        }

        return apiKey;
    }

    // Parse the current time zone of the weather data
    private void parseCurrentTimeZone(JSONObject data) {
        try {
            Utils.timeZone = data.getString("timezone");
        } catch (Exception e) {
            Toast.makeText(this, "Unable to parse the current time zone.", Toast.LENGTH_SHORT).show();
        }
    }

    // Populates a JSON array with the current and future hourly conditions
    private void populateFinalFullDayHourlyConditionsJsonArray(JSONArray hourlyConditions, String currentMidnight) {
        int currentMidnightTime = Integer.parseInt(currentMidnight);

        try {
            for (int i = 0; i < hourlyConditions.length(); i++) {
                JSONObject hourlyCondition = hourlyConditions.getJSONObject(i);
                int time = hourlyCondition.getInt("dt");

                fullyDayHourlyConditions.put(hourlyCondition);
                hoursRecorded.add(time);
            }
            dailyConditionsRecViewAdapter.sortFullDayHourConditions(fullyDayHourlyConditions, currentMidnightTime);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to construct final fully day hourly conditions json array.", Toast.LENGTH_SHORT).show();
        }
    }

    // Populates a JSON array with the past hourly conditions within the current day
    private void populateInitialFullDayHourlyConditionsJsonArray(JSONArray hourlyConditions, String currentMidnight) {
        int currentMidnightTime = Integer.parseInt(currentMidnight);

        try {
            for (int i = 0; i < hourlyConditions.length(); i++) {
                JSONObject hourlyCondition = hourlyConditions.getJSONObject(i);
                int time = hourlyCondition.getInt("dt");

                if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                    fullyDayHourlyConditions.put(hourlyCondition);
                    hoursRecorded.add(time);
                }
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to construct initial fully day hourly conditions json array.", Toast.LENGTH_SHORT).show();
        }
    }

    // Updates the data within the Current Conditions Material Card View
    private void updateCurrentConditions(JSONObject currentConditions, JSONArray precipConditions, JSONArray dailyConditions, boolean minutelyAvailable) {
        String icon;
        String temp;
        String tempHighLow;
        String description;
        String feelsLike;
        String precip;
        String humidity;
        String wind;

        // Parse current date and set current time zone
        try {
            Utils.currentDate = Utils.convertUnixTimeToLocalCalendarDate(currentConditions.getInt("dt") * 1000L);
            Utils.setTimeZone(Utils.currentDate);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to parse date.", Toast.LENGTH_SHORT).show();
        }

        // Parse icon data
        try {
            icon = currentConditions.getJSONArray("weather").getJSONObject(0).getString("icon");
        } catch (Exception e) {
            icon = "";
        }

        // Parse current temperature
        try {
            temp = currentConditions.getString("temp");
            temp = Utils.roundStringNumberValue(temp);
        } catch (Exception e) {
            temp = "N/A";
        }

        // Parse high and low temperature for today
        try {
            JSONObject today = dailyConditions.getJSONObject(0).getJSONObject("temp");
            String high = Utils.roundStringNumberValue(today.getString("max")) + "\u00B0";
            String low = Utils.roundStringNumberValue(today.getString("min")) + "\u00B0";

            tempHighLow = high + " | " + low;
        } catch (Exception e) {
            tempHighLow = "N/A";
        }

        // Parse current weather description
        try {
            description = currentConditions.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase();
        } catch (Exception e) {
            description = "N/A";
        }

        // Parse current feels like temperature
        try {
            feelsLike = currentConditions.getString("feels_like");
            feelsLike = "Feels like " + Utils.roundStringNumberValue(feelsLike) + "\u00B0";
        } catch (Exception e) {
            feelsLike = "N/A";
        }

        // Parse current precipitation chance
        int precipChance;
        try {
            if (minutelyAvailable) {
                int chance = 0;
                for (int i = 0; i < 30; i++) {
                    double precipitation = precipConditions.getJSONObject(i).getDouble("precipitation");
                    if (precipitation > 0)
                        chance++;
                }
                precipChance = (int) Math.round(chance / 30.0 * 100);
            } else {
                precipChance = (int) (precipConditions.getJSONObject(0).getDouble("pop") * 100);
            }
            precip = String.valueOf(precipChance);
            precip = Utils.roundStringNumberValue(precip) + "%";
        } catch (Exception e) {
            precip = "N/A";
        }

        // Parse current humidity
        try {
            humidity = currentConditions.getString("humidity") + "%";
        } catch (Exception e) {
            humidity = "N/A";
        }

        // Parse wind data
        try {
            String windSpeed = Utils.roundStringNumberValue(currentConditions.getString("wind_speed"));
            String windDirection = Utils.convertWindDirection(currentConditions.getString("wind_deg"));
            String units = isImperial ? "mph" : "kph";

            wind = windSpeed + " " + units + " " + windDirection;
        } catch (Exception e) {
            wind = "N/A";
        }

        // Load weather icon image
        if (!icon.isEmpty())
            Glide.with(this)
                    .asBitmap()
                    .load(Utils.createWeatherIconUrl(icon))
                    .into(imgCurrentConditionsImage);

        // Load current conditions
        txtCurrentTemperature.setText(temp);
        txtCurrentTemperatureScale.setText(isImperial ? "F" : "C");
        txtCurrentTemperatureHighLow.setText(tempHighLow);
        txtCurrentConditionsDescription.setText(description);
        txtFeelsLike.setText(feelsLike);
        txtPrecipitation.setText(precip);
        txtHumidity.setText(humidity);
        txtWind.setText(wind);
    }

    // Updates the location string within the App Bar
    private void updateCurrentLocation(JSONObject result) {
        try {
            double latitude = result.getDouble("lat");
            double longitude = result.getDouble("lon");

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = Utils.locationName == null ? geocoder.getFromLocation(latitude, longitude, 1) :
                                                                     geocoder.getFromLocationName(Utils.locationName, 1);

            Address address = addressList.get(0);
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

            String currentLocation = city + ", " + admin;
            txtCurrentLocation.setText(currentLocation);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to parse location data", Toast.LENGTH_SHORT).show();
        }
    }

    // Updates the data within the Daily Conditions Material Card View
    private void updateDailyConditions(JSONArray dailyConditions) {
        ArrayList<Weather> dailyWeather = new ArrayList<>();

        boolean hasPrecipitation = false;

        try {
            for (int i = 0; i < dailyConditions.length(); i++) {

                JSONObject dailyCondition = dailyConditions.getJSONObject(i);

                // Parse current date
                Calendar date = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getInt("dt") * 1000L);

                // Parse sunrise and sunset date
                Calendar sunrise = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getInt("sunrise") * 1000L);
                Calendar sunset = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getInt("sunset") * 1000L);

                // Parse high and low temperature
                JSONObject temperature = dailyCondition.getJSONObject("temp");
                String temperatureMax = Utils.roundStringNumberValue(temperature.getString("max"));
                String temperatureMin = Utils.roundStringNumberValue(temperature.getString("min"));

                // Parse precipitation chance
                int precipChance = (int) (dailyCondition.getDouble("pop") * 100);
                String precipChanceString = String.valueOf(precipChance);
                if (!precipChanceString.equals("0")) {
                    hasPrecipitation = true;
                }

                // Parse wind data
                String windSpeed = Utils.roundStringNumberValue(dailyCondition.getString("wind_speed"));
                String windDirection = Utils.convertWindDirection(dailyCondition.getString("wind_deg"));
                String windScale = isImperial ? "mph" : "kph";

                // Parse icon data
                String icon = Utils.createWeatherIconUrl(dailyCondition.getJSONArray("weather").getJSONObject(0).getString("icon"));

                // Create new weather object and add to the array list
                Weather weather = new Weather(i, date, sunrise, sunset, "", temperatureMax, temperatureMin, "",
                        precipChanceString, "", windSpeed, windDirection, windScale, "", icon);
                dailyWeather.add(weather);
            }

            dailyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
            dailyConditionsRecViewAdapter.setDailyWeather(dailyWeather);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse daily conditions.", Toast.LENGTH_SHORT).show();
        }
    }

    // Updates the data within the Hour Conditions Material Card View
    private void updateHourlyConditions(JSONArray hourlyConditions) {
        ArrayList<Weather> hourlyWeather = new ArrayList<>();

        boolean hasPrecipitation = false;

        try {
            for (int i = 0; i < hourlyConditions.length(); i++) {

                JSONObject hourlyCondition = hourlyConditions.getJSONObject(i);

                // Parse current date
                Calendar date = Utils.convertUnixTimeToLocalCalendarDate(hourlyCondition.getInt("dt") * 1000L);

                // Parse current temperature
                String temperatureCurrent = Utils.roundStringNumberValue(hourlyCondition.getString("temp"));

                // Parse precipitation chance
                int precipChance = (int) (hourlyCondition.getDouble("pop") * 100);
                String precipChanceString = String.valueOf(precipChance);
                if (!precipChanceString.equals("0")) {
                    hasPrecipitation = true;
                }

                // Parse wind data
                String windSpeed = Utils.roundStringNumberValue(hourlyCondition.getString("wind_speed"));
                String windDirection = Utils.convertWindDirection(hourlyCondition.getString("wind_deg"));
                String windScale = isImperial ? "mph" : "kph";

                // Parse icon data
                String icon = Utils.createWeatherIconUrl(hourlyCondition.getJSONArray("weather").getJSONObject(0).getString("icon"));

                // Create new weather object and add to the array list
                Weather weather = new Weather(i, date, null, null, temperatureCurrent, "", "", "",
                        precipChanceString, "", windSpeed, windDirection, windScale, "", icon);
                hourlyWeather.add(weather);
            }

            hourlyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
            hourlyConditionsRecViewAdapter.setHourlyWeather(hourlyWeather);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse hourly conditions.", Toast.LENGTH_SHORT).show();
        }
    }

    // Update the theme of the activity
    private void updateTheme(boolean isDark) {
        if (isDark) {
            setTheme(R.style.Theme_UI_Dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setTheme(R.style.Theme_UI_Light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}