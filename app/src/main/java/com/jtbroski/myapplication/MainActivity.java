package com.jtbroski.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView txtCurrentLocation;

    private ImageView imgCurrentConditionsImage;

    private TextView txtCurrentTemperature;
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

    private Geocoder geocoder;
    private ArrayList<Address> addressList;

    private SearchView searchView;
    private SearchFilterAdapter searchFilterAdapter;
    private Cursor citiesFilteredCursor;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //this.deleteDatabase("preferences.db");  // temporary database reset until full database has been created
        Utils.getInstance(this);

        geocoder = new Geocoder(this);
        addressList = new ArrayList<>();

        txtCurrentLocation = findViewById(R.id.current_location);
        imgCurrentConditionsImage = findViewById(R.id.current_conditions_image);
        txtCurrentTemperature = findViewById(R.id.current_temperature);
        txtCurrentTemperatureHighLow = findViewById(R.id.current_temperature_high_low);
        txtCurrentConditionsDescription = findViewById(R.id.current_conditions_description);
        txtFeelsLike = findViewById(R.id.feels_like);
        txtPrecipitation = findViewById(R.id.precipitation_value);
        txtHumidity = findViewById(R.id.humidity_value);
        txtWind = findViewById(R.id.wind_data);

        hourlyConditionsRecViewAdapter = new HourlyConditionsRecViewAdapter(this);
        hourlyConditionsRecView = findViewById(R.id.hourly_conditions_recycler_view);
        hourlyConditionsRecView.setAdapter(hourlyConditionsRecViewAdapter);
        hourlyConditionsRecView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dailyConditionsRecViewAdapter = new DailyConditionsRecViewAdapter(this);
        dailyConditionsRecView = findViewById(R.id.daily_conditions_recycler_view);
        dailyConditionsRecView.setAdapter(dailyConditionsRecViewAdapter);
        dailyConditionsRecView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        queue = Volley.newRequestQueue(this);

        Location preferredLocation = Preferences.getInstance(this).getPreferredLocation();
        if (preferredLocation != null) {
            callWeatherApi(preferredLocation);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return false;
            }
        };

        MenuItem searchMenuItem = menu.findItem(R.id.search_menu);
        searchMenuItem.setOnActionExpandListener(onActionExpandListener);

        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search by city or zip code");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callWeatherApi(query);
                collapseSearchView();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                citiesFilteredCursor = Utils.dbHelper.getCitiesFilteredCursor(newText);
                searchFilterAdapter.changeCursor(citiesFilteredCursor);

                return false;
            }
        });

        searchFilterAdapter = new SearchFilterAdapter(this, citiesFilteredCursor, false);
        searchView.setSuggestionsAdapter(searchFilterAdapter);

        return true;
    }

    // TODO Create menu item activities
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_menu:
                return true;

            case R.id.my_location_menu:
                Preferences.getInstance(this).getCurrentLocation(this);
                return true;

            case R.id.settings_menu:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Calls the OpenWeather API and updates all weather conditions
    public void callWeatherApi(Location location) {
        final String API_KEY = "&appid=4ae663188d74e9a952417c9234e8f511";
        final String END_POINT = " https://api.openweathermap.org/data";
        final String VERSION = "2.5";
        final String ONE_CALL = "onecall?";
        final String TEMP_MEASUREMENT = "&units=imperial";

        String coordinates = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();

        String url = END_POINT + "/" + VERSION + "/" + ONE_CALL + coordinates + TEMP_MEASUREMENT + API_KEY;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject result = new JSONObject(response);
                            JSONObject currentConditions = result.getJSONObject("current");
                            JSONArray hourlyConditions = result.getJSONArray("hourly");
                            JSONArray dailyConditions = result.getJSONArray("daily");

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
                            Toast.makeText(MainActivity.this, "Failed to parse weather data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "API call failed", Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(stringRequest);
    }

    // Creates a Location object based on the string parameter passed in, then executes the callWeatherApi(Location location) function
    public void callWeatherApi(String location) {
        addressList.clear();

        try {
            addressList = (ArrayList<Address>) geocoder.getFromLocationName(location, 10);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Unable to get any locations matching with " + location, Toast.LENGTH_SHORT).show();
        }

        // If more than one address was found,
        // then create a matrix cursor from the addresses and pass in the new cursor to the search filter adapter
        // else, then call the weather api with the new location
        if (addressList.size() > 1) {   // This is primarily to account for domestic and international zip codes, however Geocoder does not seem to be return international zip codes
            String[] columns = new String[] {"_id", "city", "country"};
            MatrixCursor matrixCursor = new MatrixCursor(columns);

            for (int i = 0; i < addressList.size(); i++) {
                Address address = addressList.get(i);
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
                }

                matrixCursor.addRow(new String[]{String.valueOf(i), city, admin});
            }
            searchFilterAdapter.changeCursor(citiesFilteredCursor);

        } else if (addressList.size() > 0) {
            Location newLocation = new Location(LocationManager.GPS_PROVIDER);
            newLocation.setLatitude(addressList.get(0).getLatitude());
            newLocation.setLongitude(addressList.get(0).getLongitude());
            callWeatherApi(newLocation);    // TODO potentially implement preferred location saving here
        }
    }

    // Collapse the search view and scroll to the top
    public void collapseSearchView() {
        searchView.setIconified(true);  // Clears the text within the search view
        searchView.setIconified(true);  // Collapse the search view and the keyboard

        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.scrollTo(0, 0);
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
            int epocTime = currentConditions.getInt("dt");
            Utils.currentDate = new Date(epocTime * 1000L);
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
        try {
            if (minutelyAvailable) {
                precip = precipConditions.getJSONObject(0).getString("precipitation");
            }
            else {
                precip = precipConditions.getJSONObject(0).getString("pop");
            }
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

            // TODO add options for metric vs imperial
            wind = windSpeed + " mph " + windDirection;
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
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);

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
                Date date = new Date(dailyCondition.getInt("dt") * 1000L);

                // Parse high and low temperature
                JSONObject temperature = dailyCondition.getJSONObject("temp");
                String temperatureMax = Utils.roundStringNumberValue(temperature.getString("max"));
                String temperatureMin = Utils.roundStringNumberValue(temperature.getString("min"));

                // Parse precipitation chance
                String precipChance = Utils.roundStringNumberValue(dailyCondition.getString("pop"));
                if (!precipChance.equals("0")) {
                    hasPrecipitation = true;
                }

                // TODO add options for metric vs imperial
                // Parse wind data
                String windSpeed = Utils.roundStringNumberValue(dailyCondition.getString("wind_speed"));
                String windDirection = Utils.convertWindDirection(dailyCondition.getString("wind_deg"));
                String windScale = "mph";

                // Parse icon data
                String icon = Utils.createWeatherIconUrl(dailyCondition.getJSONArray("weather").getJSONObject(0).getString("icon"));

                // Create new weather object and add to the array list
                Weather weather = new Weather(i, date, "", temperatureMax, temperatureMin, "",
                        precipChance, "", windSpeed, windDirection, windScale, "", icon);
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
                Date date = new Date(hourlyCondition.getInt("dt") * 1000L);

                // Parse current temperature
                String temperatureCurrent = Utils.roundStringNumberValue(hourlyCondition.getString("temp"));

                // Parse precipitation chance
                String precipChance = Utils.roundStringNumberValue(hourlyCondition.getString("pop"));
                if (!precipChance.equals("0")) {
                    hasPrecipitation = true;
                }

                // TODO add options for metric vs imperial
                // Parse wind data
                String windSpeed = Utils.roundStringNumberValue(hourlyCondition.getString("wind_speed"));
                String windDirection = Utils.convertWindDirection(hourlyCondition.getString("wind_deg"));
                String windScale = "mph";

                // Parse icon data
                String icon = Utils.createWeatherIconUrl(hourlyCondition.getJSONArray("weather").getJSONObject(0).getString("icon"));

                // Create new weather object and add to the array list
                Weather weather = new Weather(i, date, temperatureCurrent, "", "", "",
                        precipChance, "", windSpeed, windDirection, windScale, "", icon);
                hourlyWeather.add(weather);
            }

            hourlyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
            hourlyConditionsRecViewAdapter.setHourlyWeather(hourlyWeather);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse hourly conditions.", Toast.LENGTH_SHORT).show();
        }
    }
}