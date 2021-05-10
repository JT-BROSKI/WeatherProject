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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jtbroski.myapplication.WeatherAlertActivity.ALERT_DATA_ID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final String OPEN_WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private final Integer OPEN_WEATHER_API_KEY = R.string.open_weather_map_key;

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

    private DrawerLayout drawerLayout;

    private FavoriteLocationListAdapter favoriteLocationListAdapter;
    private RecentLocationListAdapter recentLocationListAdapter;

    private DailyConditionsRecViewAdapter dailyConditionsRecViewAdapter;
    private RecyclerView dailyConditionsRecView;

    private HourlyConditionsRecViewAdapter hourlyConditionsRecViewAdapter;
    private RecyclerView hourlyConditionsRecView;
    private List<ApiInfoHourlyConditions> fullDayHourlyConditions;
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
        NonScrollableListView favoriteLocationListView = findViewById(R.id.list_favorites);
        Cursor favoriteLocationCursor = Utils.preferenceDbHelper.getFavoriteLocations();
        favoriteLocationListAdapter = new FavoriteLocationListAdapter(this, favoriteLocationCursor, false);
        favoriteLocationListAdapter.changeCursor(favoriteLocationCursor);
        favoriteLocationListView.setAdapter(favoriteLocationListAdapter);

        // Populate recent locations list in the navigation drawer
        NonScrollableListView recentLocationListView = findViewById(R.id.list_recent);
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
        fullDayHourlyConditions = new ArrayList<>();
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

        String measurement = isImperial ? "imperial" : "metric";
        getHistoricalWeather(location, measurement);
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
        if (requestCode == PreferenceDatabaseHelper.REQUEST_LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Utils.preferenceDbHelper.requestCurrentLocation(this);
            } else {
                Toast.makeText(this, "My location permissions denied. Go to system settings to update location sharing permissions for My Location functionality.", Toast.LENGTH_LONG).show();
            }
        } else {
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

    // Get the forecasted weather conditions
    private void getForecastedWeather(Location location, String measurement) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenWeatherService openWeatherService = retrofit.create(OpenWeatherService.class);
        Call<ApiInfoConditions> call = openWeatherService.getOneCallWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                measurement,
                getApplicationContext().getResources().getString(OPEN_WEATHER_API_KEY));
        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    ApiInfoConditions forecast = response.body();

                    Utils.timeZone = forecast.getTimezone();
                    populateFinalFullDayHourlyConditionsJsonArray(forecast.getHourly());
                    Utils.updateLastQueriedLocation(forecast);

                    updateCurrentConditions(forecast);
                    updateCurrentLocation(forecast);
                    updateDailyConditions(forecast.getDaily());
                    updateHourlyConditions(forecast.getHourly());
                    updateWeatherAlerts(forecast.getAlerts());

                    Utils.preferenceDbHelper.updatePreferredLocation(Utils.lastQueriedLocation);

                    resetScrollViews();

                    // Hide the splash image when the data has been loaded
                    if (splash.getVisibility() != View.GONE) {
                        splash.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to get forecast weather data through retrofit.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get the past hourly conditions based on the current day's midnight
    private void getHistoricalWeather(Location location, String measurement) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenWeatherService openWeatherService = retrofit.create(OpenWeatherService.class);
        Call<ApiInfoConditions> call = openWeatherService.getOneCallHistoricalWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                Utils.getCurrentDayMidnight(),
                measurement,
                getApplicationContext().getResources().getString(OPEN_WEATHER_API_KEY));
        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiInfoHourlyConditions> hourlyConditions = response.body().getHourly();

                    if (hourlyConditions != null) {
                        populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions);
                    }

                    getHistoricalWeatherBackup(location, measurement);
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to get historical weather data through retrofit.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Get the past hourly conditions based on an hour before the current day's midnight just in case the normal historical request missed a few hours
    private void getHistoricalWeatherBackup(Location location, String measurement) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OPEN_WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenWeatherService openWeatherService = retrofit.create(OpenWeatherService.class);
        Call<ApiInfoConditions> call = openWeatherService.getOneCallHistoricalWeatherData(
                Double.toString(location.getLatitude()),
                Double.toString(location.getLongitude()),
                Utils.getPreviousThreeHours(),
                measurement,
                getApplicationContext().getResources().getString(OPEN_WEATHER_API_KEY));

        call.enqueue(new Callback<ApiInfoConditions>() {
            @Override
            public void onResponse(Call<ApiInfoConditions> call, Response<ApiInfoConditions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiInfoHourlyConditions> hourlyConditions = response.body().getHourly();

                    if (hourlyConditions != null) {
                        populateInitialFullDayHourlyConditionsJsonArray(hourlyConditions);
                    }

                    getForecastedWeather(location, measurement);
                }
            }

            @Override
            public void onFailure(Call<ApiInfoConditions> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to get historical weather backup data through retrofit.", Toast.LENGTH_SHORT).show();
            }
        });
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

    // Populates an array list with the current and future hourly conditions
    private void populateFinalFullDayHourlyConditionsJsonArray(List<ApiInfoHourlyConditions> hourlyConditions) {
        int currentMidnightTime = Integer.parseInt(Utils.getCurrentDayMidnight());

        for (ApiInfoHourlyConditions hourlyCondition : hourlyConditions) {
            int time = hourlyCondition.getDt();

            if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                fullDayHourlyConditions.add(hourlyCondition);
                hoursRecorded.add(time);
            }
        }
        dailyConditionsRecViewAdapter.sortFullDayHourConditions(fullDayHourlyConditions, currentMidnightTime);
    }

    // Populates an array list with the past hourly conditions within the current day
    private void populateInitialFullDayHourlyConditionsJsonArray(List<ApiInfoHourlyConditions> hourlyConditions) {
        int currentMidnightTime = Integer.parseInt(Utils.getCurrentDayMidnight());

        for (ApiInfoHourlyConditions hourlyCondition : hourlyConditions) {
            int time = hourlyCondition.getDt();

            if (time >= currentMidnightTime && !hoursRecorded.contains(time)) {
                fullDayHourlyConditions.add(hourlyCondition);
                hoursRecorded.add(time);
            }
        }
    }

    // Updates the data within the Current Conditions Material Card View
    private void updateCurrentConditions(ApiInfoConditions forecast) {
        String icon;
        String temp;
        String tempHighLow;
        String description;
        String feelsLike;
        String precipitation;
        String humidity;
        String wind;

        ApiInfoCurrentConditions currentConditions = forecast.getCurrent();

        // Parse current date and set current time zone
        Utils.currentDate = Utils.convertUnixTimeToLocalCalendarDate(currentConditions.getDt() * 1000L);
        Utils.setTimeZone(Utils.currentDate);

        // Parse icon data
        icon = currentConditions.getWeather().get(0).getIcon();

        // Parse current temperature
        temp = String.valueOf((int) Math.round(currentConditions.getTemp()));

        // Parse high and low temperature for today
        ApiInfoTemp today = forecast.getDaily().get(0).getTemp();
        String high = (int) Math.round(today.getMax()) + "\u00B0";
        String low = (int) Math.round(today.getMin()) + "\u00B0";
        tempHighLow = high + " | " + low;

        // Parse current weather description
        description = currentConditions.getWeather().get(0).getDescription().toUpperCase();

        // Parse current feels like temperature
        feelsLike = "Feels like " + (int) Math.round(currentConditions.getFeelsLike()) + "\u00B0";

        // Parse current precipitation chance
        double precipitationAverage;
        if (forecast.getMinutely() != null) {
            List<ApiInfoMinutelyConditions> minutelyConditions = forecast.getMinutely();

            // Calculate the precipitation chance based on the conditions within the next 30 minutes
            int chance = 0;
            for (int i = 0; i < 30; i++) {
                double precipitationChance = minutelyConditions.get(i).getPrecipitation();
                if (precipitationChance > 0)
                    chance++;
            }
            precipitationAverage = chance / 30.0 * 100;
        } else {
            precipitationAverage = forecast.getHourly().get(0).getPop() * 100;
        }
        precipitation = (int) Math.round(precipitationAverage) + "%";

        // Parse current humidity
        humidity = currentConditions.getHumidity() + "%";

        // Parse wind data
        double windSpeed = (int) Math.round(currentConditions.getWindSpeed());
        String windDirection = Utils.convertWindDirection(currentConditions.getWindDeg());
        String units = isImperial ? "mph" : "kph";
        wind = windSpeed + " " + units + " " + windDirection;

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
        txtPrecipitation.setText(precipitation);
        txtHumidity.setText(humidity);
        txtWind.setText(wind);
    }

    // Updates the location string within the App Bar
    private void updateCurrentLocation(ApiInfoConditions forecast) {
        try {
            double latitude = forecast.getLat();
            double longitude = forecast.getLon();
            updateCurrentLocationOnWeatherMap(latitude, longitude);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addressList = Utils.locationName == null ? geocoder.getFromLocation(latitude, longitude, 1) :
                    geocoder.getFromLocationName(Utils.locationName, 1);

            txtCurrentLocation.setText(Utils.parseAddressName(addressList.get(0)));
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Failed to parse current location data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentLocationOnWeatherMap(double latitude, double longitude) {
        if (map != null) {
            LatLng location = new LatLng(latitude, longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mapZoom));
        }
    }


    // Updates the data within the Daily Conditions Material Card View
    private void updateDailyConditions(List<ApiInfoDailyConditions> dailyConditions) {
        ArrayList<Weather> dailyWeather = new ArrayList<>();

        boolean hasPrecipitation = false;

        for (ApiInfoDailyConditions dailyCondition : dailyConditions) {
            // Parse current date
            Calendar date = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getDt() * 1000L);

            // Parse sunrise and sunset date
            Calendar sunrise = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getSunrise() * 1000L);
            Calendar sunset = Utils.convertUnixTimeToLocalCalendarDate(dailyCondition.getSunset() * 1000L);

            // Parse high and low temperature
            ApiInfoTemp temperature = dailyCondition.getTemp();
            String temperatureMax = String.valueOf((int) Math.round(temperature.getMax()));
            String temperatureMin = String.valueOf((int) Math.round(temperature.getMin()));

            // Parse precipitation chance
            String precipitationChance = String.valueOf((int) (dailyCondition.getPop() * 100));
            if (!precipitationChance.equals("0")) {
                hasPrecipitation = true;
            }

            // Parse wind data
            String windSpeed = String.valueOf((int) Math.round(dailyCondition.getWindSpeed()));
            String windDirection = Utils.convertWindDirection(dailyCondition.getWindDeg());
            String windScale = isImperial ? "mph" : "kph";

            // Parse icon data
            String icon = Utils.createWeatherIconUrl(dailyCondition.getWeather().get(0).getIcon());

            // Create new weather object and add to the array list
            Weather weather = new Weather(date, sunrise, sunset, "", temperatureMax, temperatureMin,
                    precipitationChance, windSpeed, windDirection, windScale, icon);
            dailyWeather.add(weather);
        }

        dailyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
        dailyConditionsRecViewAdapter.setDailyWeather(dailyWeather);
        dailyConditionsRecView.post(() -> dailyConditionsRecView.smoothScrollToPosition(0));
    }

    // Updates the data within the Hourly Conditions Material Card View
    private void updateHourlyConditions(List<ApiInfoHourlyConditions> hourlyConditions) {
        ArrayList<Weather> hourlyWeather = new ArrayList<>();

        boolean hasPrecipitation = false;

        for (ApiInfoHourlyConditions hourlyCondition : hourlyConditions) {
            // Parse current date
            Calendar date = Utils.convertUnixTimeToLocalCalendarDate(hourlyCondition.getDt() * 1000L);

            // Parse current temperature
            String temperatureCurrent = String.valueOf((int) Math.round(hourlyCondition.getTemp()));

            // Parse precipitation chance
            String precipitationChance = String.valueOf((int) (hourlyCondition.getPop() * 100));
            if (!precipitationChance.equals("0")) {
                hasPrecipitation = true;
            }

            // Parse wind data
            String windSpeed = String.valueOf((int) Math.round(hourlyCondition.getWindSpeed()));
            String windDirection = Utils.convertWindDirection(hourlyCondition.getWindDeg());
            String windScale = isImperial ? "mph" : "kph";

            // Parse icon data
            String icon = Utils.createWeatherIconUrl(hourlyCondition.getWeather().get(0).getIcon());

            // Create new weather object and add to the array list
            Weather weather = new Weather(date, null, null, temperatureCurrent, "", "",
                    precipitationChance, windSpeed, windDirection, windScale, icon);
            hourlyWeather.add(weather);
        }

        hourlyConditionsRecViewAdapter.setShowPrecipitation(hasPrecipitation);
        hourlyConditionsRecViewAdapter.setHourlyWeather(hourlyWeather);
        hourlyConditionsRecView.post(() -> hourlyConditionsRecView.smoothScrollToPosition(0));
    }

    // Update the weather alerts notification
    private void updateWeatherAlerts(List<ApiInfoAlerts> alerts) {
        if (alerts != null) {
            ApiInfoAlerts firstAlert = alerts.get(0);
            txtWeatherAlert.setText(firstAlert.getEvent());

            weatherAlerts = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mma z EEE, MMM d, yyyy", Locale.US);
            for (ApiInfoAlerts alert : alerts) {
                String sender = alert.getSenderName();
                String title = alert.getEvent();

                Calendar startDate = Utils.convertUnixTimeToLocalCalendarDate(alert.getStart() * 1000L);
                String startString = dateFormat.format(startDate.getTime());

                Calendar endDate = Utils.convertUnixTimeToLocalCalendarDate(alert.getEnd() * 1000L);
                String endString = dateFormat.format(endDate.getTime());

                String description = alert.getDescription();

                weatherAlerts.add(new WeatherAlert(sender, title, startString, endString, description));
            }

            weatherAlertLayout.setVisibility(View.VISIBLE);
            resetScrollViews();
        } else {
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