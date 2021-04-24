package com.jtbroski.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.jtbroski.myapplication.WeatherAlertActivity.ALERT_DATA_ID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ImageView splash;
    private ScrollView scrollView;

    private TextView txtCurrentLocation;
    private TextView txtWeatherAlert;

    private ImageView imgCurrentConditionsImage;

    private TextView txtCurrentTemperature;
    private TextView txtCurrentTemperatureScale;
    private TextView txtCurrentTemperatureHighLow;
    private TextView txtCurrentConditionsDescription;
    private TextView txtFeelsLike;
    private TextView txtPrecipitation;
    private TextView txtHumidity;
    private TextView txtWind;

    private GoogleMap map;
    private int mapZoom;
    private TileOverlay weatherTileOverlay;
    private ArrayList<Triplet<Integer, Integer, Integer>> weatherTiles;

    private RequestQueue queue;

    private DrawerLayout drawerLayout;

    private NonScrollableListView favoriteLocationListView;
    private NonScrollableListView recentLocationListView;
    private FavoriteLocationListAdapter favoriteLocationListAdapter;
    private RecentLocationListAdapter recentLocationListAdapter;

    private DailyConditionsRecViewAdapter dailyConditionsRecViewAdapter;
    private RecyclerView dailyConditionsRecView;

    private HourlyConditionsRecViewAdapter hourlyConditionsRecViewAdapter;
    private RecyclerView hourlyConditionsRecView;
    private JSONArray fullyDayHourlyConditions;
    private ArrayList<Integer> hoursRecorded;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ConstraintLayout weatherAlertLayout;
    private ArrayList<WeatherAlert> weatherAlerts;

    private boolean isImperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        this.deleteDatabase(PreferenceDatabaseHelper.DB_NAME);  // Kept for debugging purposes
//        this.deleteDatabase(LocationDatabaseHelper.DB_NAME);    // Kept for debugging purposes
        Utils.initialize(MainActivity.this);
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ensure the splash image is not displayed if it isn't a startup action
        splash = findViewById(R.id.splash);
        if (!Utils.startUp) {
            splash.setVisibility(View.GONE);
        }

        Toolbar toolbar = findViewById(R.id.tool_bar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);

        // Dynamically set navigation view width
        NavigationView navigationView = findViewById(R.id.nav_view);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        navigationView.setLayoutParams(params);

        // Populate favorite locations in the navigation drawer
        favoriteLocationListView = findViewById(R.id.list_favorites);
        Cursor favoriteLocationCursor = Utils.preferenceDbHelper.getFavoriteLocations();
        favoriteLocationListAdapter = new FavoriteLocationListAdapter(this, favoriteLocationCursor, false);
        favoriteLocationListAdapter.changeCursor(favoriteLocationCursor);
        favoriteLocationListView.setAdapter(favoriteLocationListAdapter);

        // Populate recent locations list in the navigation drawer
        recentLocationListView = findViewById(R.id.list_recent);
        Cursor recentLocationCursor = Utils.preferenceDbHelper.getRecentLocations();
        recentLocationListAdapter = new RecentLocationListAdapter(this, recentLocationCursor, false);
        recentLocationListAdapter.changeCursor(recentLocationCursor);
        recentLocationListView.setAdapter(recentLocationListAdapter);

        // Ensure the scrollview is scrolled to the top once all elements in the entire view have been loaded
        ConstraintLayout mainLayout = findViewById(R.id.main_layout);
        mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resetScrollViews();
                mainLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        // Appbar Info
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
                Utils.preferenceDbHelper.getCurrentLocation(MainActivity.this);
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

        txtCurrentLocation = findViewById(R.id.current_location);
        txtWeatherAlert = findViewById(R.id.alert_info);
        weatherAlertLayout = findViewById(R.id.alert_layout);
        weatherAlertLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, WeatherAlertActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ALERT_DATA_ID, weatherAlerts);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        scrollView = findViewById(R.id.scrollView);

        // Current Conditions Material Card View
        imgCurrentConditionsImage = findViewById(R.id.current_conditions_image);
        txtCurrentTemperature = findViewById(R.id.current_temperature);
        txtCurrentTemperatureScale = findViewById(R.id.temperature_scale);
        txtCurrentTemperatureHighLow = findViewById(R.id.current_temperature_high_low);
        txtCurrentConditionsDescription = findViewById(R.id.current_conditions_description);
        txtFeelsLike = findViewById(R.id.feels_like);
        txtPrecipitation = findViewById(R.id.precipitation_value);
        txtHumidity = findViewById(R.id.humidity_value);
        txtWind = findViewById(R.id.wind_data);

        // Weather Map Material Card View
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.weather_map_fragment);
        mapFragment.getMapAsync(this);

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
        swipeRefreshLayout.setOnRefreshListener(() -> callWeatherApi(Utils.lastQueriedLocation));

        queue = Volley.newRequestQueue(this);

        // If the program is starting up, then get the weather for the preferred location
        // Else, then this is being called due to a theme change and we want to get the weather for the last queried location
        if (Utils.startUp) {
            Utils.startUp = false;
            Location preferredLocation = Utils.preferenceDbHelper.getPreferredLocation();
            if (preferredLocation != null) {
                callWeatherApi(preferredLocation);
            }
        } else {
            callWeatherApi(Utils.lastQueriedLocation);
        }
    }

    // Calls the OpenWeather API and updates all weather conditions
    public void callWeatherApi(Location location) {
        fullyDayHourlyConditions = new JSONArray();
        hoursRecorded = new ArrayList<>();
        isImperial = Utils.preferenceDbHelper.getImperialFlag();

        // Clear the weather tiles when a new location is being request
        // This may not be necessary now that we have the correct weather map URL, the tile overlay options may already be checking if it contains certain tiles
        if (weatherTiles != null) {
            weatherTiles.clear();
        }

        // Update the weather tile overlay
        if (map != null && !Utils.startUp) {
            updateWeatherTileOverlay();
        }

        final String API_KEY = "appid=" + getResources().getString(R.string.open_weather_map_key);
        final String END_POINT = "https://api.openweathermap.org/data";
        final String VERSION = "2.5";
        final String TEMP_MEASUREMENT = "&units=" + (isImperial ? "imperial" : "metric");
        String coordinates = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        String currentMidnight = Utils.getCurrentDayMidnight(this);

        queue.add(constructHistoricalStringRequest(currentMidnight, END_POINT, VERSION, coordinates, TEMP_MEASUREMENT, API_KEY));
    }

    // Close the drawer layout
    public void closeDrawerLayout() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onDestroy() {
        Utils.removeContext();
        favoriteLocationListAdapter.closeCursor();
        recentLocationListAdapter.closeCursor();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setAllGesturesEnabled(false);
        map.setOnMapLoadedCallback(() -> resetScrollViews());    // Make sure the scroll view is scrolled to the top on the map finishes loading (this is generally only useful on startup_

        mapZoom = 8;
        weatherTiles = new ArrayList<>();

        updateWeatherTileOverlay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PreferenceDatabaseHelper.REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Utils.preferenceDbHelper.requestCurrentLocation(this);
                } else {
                    Toast.makeText(this, "My location permissions denied. Go to system settings to update location sharing permissions for My Location functionality.", Toast.LENGTH_LONG).show();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Reset the scroll view by scrolling to the top
    public void resetScrollViews() {
        // We need to use "post" here to ensure that a runnable is added to the thread queue
        // This ensures that this task will execute after previously queued task have properly executed (i.e weather tile loading)
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    // Update the list views within the navigation drawer by update their cursors
    public void updateNavigationListViews() {
        favoriteLocationListAdapter.changeCursor(Utils.preferenceDbHelper.getFavoriteLocations());
        recentLocationListAdapter.changeCursor(Utils.preferenceDbHelper.getRecentLocations());
    }

    // Construct the API string request for the current and future weather conditions
    private StringRequest constructForecastStringRequest(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String ONE_CALL = "onecall?";
        String urlCurrent = endPoint + "/" + version + "/" + ONE_CALL + coordinates + measurement + "&" + apiKey;

        return new StringRequest(Request.Method.GET, urlCurrent,
                response -> {
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
                        Utils.updateLastQueriedLocation(MainActivity.this, result);

                        boolean minutelyAvailable = true;
                        JSONArray precipConditions;
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
                        updateWeatherAlerts(result);

                        Utils.preferenceDbHelper.updatePreferredLocation(Utils.lastQueriedLocation);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to parse current weather data.", Toast.LENGTH_SHORT).show();
                    } finally {
                        resetScrollViews();

                        // Hide the splash image when the data has been loaded
                        if (splash.getVisibility() != View.GONE) {
                            splash.setVisibility(View.GONE);
                        }
                    }
                },
                error -> Toast.makeText(this, "Forecast API call failed", Toast.LENGTH_SHORT).show());
    }

    // Construct the string request for past hourly conditions
    private StringRequest constructHistoricalStringRequest(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String HISTORICAL_ONE_CALL = "onecall/timemachine?";
        String time = "&dt=" + currentMidnight;
        String urlHistorical = endPoint + "/" + version + "/" + HISTORICAL_ONE_CALL + coordinates + time + measurement + "&" + apiKey;

        return new StringRequest(Request.Method.GET, urlHistorical,
                response -> {
                    try {
                        JSONObject result = new JSONObject(response);
                        JSONArray hourlyConditions = result.getJSONArray("hourly");
                        populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions, currentMidnight);

                        queue.add(constructHistoricalStringRequestBackup(currentMidnight, endPoint, version, coordinates, measurement, apiKey));
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse historic weather data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Historical API call failed", Toast.LENGTH_SHORT).show());
    }

    // Construct the backup string request for past hourly conditions just in case the normal historical string request missed a few hours
    private StringRequest constructHistoricalStringRequestBackup(String currentMidnight, String endPoint, String version, String coordinates, String measurement, String apiKey) {
        final String HISTORICAL_ONE_CALL = "onecall/timemachine?";
        String timeThreeHours = "&dt=" + Utils.getPreviousThreeHours(this);
        String urlPreviousThreeHours = endPoint + "/" + version + "/" + HISTORICAL_ONE_CALL + coordinates + timeThreeHours + measurement + "&" + apiKey;

        return new StringRequest(Request.Method.GET, urlPreviousThreeHours,
                response -> {
                    try {
                        JSONObject result = new JSONObject(response);
                        JSONArray hourlyConditions = result.getJSONArray("hourly");
                        populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions, currentMidnight);

                        queue.add(constructForecastStringRequest(currentMidnight, endPoint, version, coordinates, measurement, apiKey));
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse weather data three hours in the past.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Historical Backup API call failed", Toast.LENGTH_SHORT).show());
    }

    // Create the tile provider for the weather maps
    private TileProvider createTileProvider() {
        return new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {
                String urlString = String.format(Locale.US, "https://tile.openweathermap.org/map/precipitation/%d/%d/%d.png?appid=%s",
                        zoom, x, y, getResources().getString(R.string.open_weather_map_key));

                URL url = null;
                if (zoom == mapZoom && isNewTile(x, y, zoom)) {
                    try {
                        url = new URL(urlString);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed the create the URL for Open Weather Maps precipitation layer.", Toast.LENGTH_SHORT).show();
                    }
                }
                return url;
            }
        };
    }

    // Check if the map tile values have already been requested before
    // NOTE: This may be unnecessary now that we have the correct weather map URL, the tile overlay options may already be checking if it contains certain tiles
    // If it is a new tile return true, else return false
    private boolean isNewTile(int x, int y, int zoom) {
        if (weatherTiles.size() == 0) {
            weatherTiles.add(new Triplet(x, y, zoom));
        } else {
            for (int i = 0; i < weatherTiles.size(); i++) {
                Triplet tile = weatherTiles.get(0);
                if ((int) tile.getFirst() == x && (int) tile.getSecond() == y && (int) tile.getThird() == zoom) {
                    return false;
                }
            }
            weatherTiles.add(new Triplet<>(x, y, zoom));
        }
        return true;
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
            Glide.with(getApplicationContext())
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
            updateCurrentLocationOnWeatherMap(latitude, longitude);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = Utils.locationName == null ? geocoder.getFromLocation(latitude, longitude, 1) :
                    geocoder.getFromLocationName(Utils.locationName, 1);

            txtCurrentLocation.setText(Utils.parseAddressName(addressList.get(0)));
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to parse location data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentLocationOnWeatherMap(double latitude, double longitude) {
        if (map != null) {
            LatLng location = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mapZoom));
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
                Weather weather = new Weather(date, sunrise, sunset, "", temperatureMax, temperatureMin,
                        precipChanceString, windSpeed, windDirection, windScale, icon);
                dailyWeather.add(weather);
            }

            dailyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
            dailyConditionsRecViewAdapter.setDailyWeather(dailyWeather);
            dailyConditionsRecView.post(() -> dailyConditionsRecView.smoothScrollToPosition(0));
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
                Weather weather = new Weather(date, null, null, temperatureCurrent, "", "",
                        precipChanceString, windSpeed, windDirection, windScale, icon);
                hourlyWeather.add(weather);
            }

            hourlyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
            hourlyConditionsRecViewAdapter.setHourlyWeather(hourlyWeather);
            hourlyConditionsRecView.post(() -> hourlyConditionsRecView.smoothScrollToPosition(0));
        } catch (Exception e) {
            Toast.makeText(this, "Failed to parse hourly conditions.", Toast.LENGTH_SHORT).show();
        }
    }

    // Update the weather alerts notification
    private void updateWeatherAlerts(JSONObject result) {
        try {
            JSONArray alerts = result.getJSONArray("alerts");

            JSONObject firstAlert = alerts.getJSONObject(0);
            txtWeatherAlert.setText(firstAlert.getString("event"));

            weatherAlerts = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma z EEE, MMM d, yyyy", Locale.US);
            for (int i = 0; i < alerts.length(); i++) {
                JSONObject alert = alerts.getJSONObject(i);

                String sender = alert.getString("sender_name");
                String title = alert.getString("event");

                Calendar startDate = Utils.convertUnixTimeToLocalCalendarDate(alert.getInt("start") * 1000L);
                String startString = dateFormat.format(startDate.getTime());

                Calendar endDate = Utils.convertUnixTimeToLocalCalendarDate(alert.getInt("end") * 1000L);
                String endString = dateFormat.format(endDate.getTime());

                String description = alert.getString("description");

                weatherAlerts.add(new WeatherAlert(sender, title, startString, endString, description));
            }

            weatherAlertLayout.setVisibility(View.VISIBLE);
            resetScrollViews();
        } catch (Exception e) {
            weatherAlertLayout.setVisibility(View.GONE);
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

    // Updates the weather tile overlay for our google maps
    private void updateWeatherTileOverlay() {
        if (weatherTileOverlay != null) {
            weatherTileOverlay.remove();
        }

        TileOverlayOptions tileOverlayOptions = new TileOverlayOptions().tileProvider(createTileProvider());
        tileOverlayOptions.transparency(0.5f);
        weatherTileOverlay = map.addTileOverlay(tileOverlayOptions);
    }
}