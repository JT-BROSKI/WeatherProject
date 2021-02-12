package com.jtbroski.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private ScrollView parent;

    private ImageView imgCurrentConditionsImage;

    private TextView txtCurrentTemperature;
    private TextView txtCurrentTemperatureHighLow;
    private TextView txtCurrentConditionsDescription;
    private TextView txtFeelsLike;
    private TextView txtPrecipitation;
    private TextView txtHumidity;
    private TextView txtWind;

    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parent = findViewById(R.id.parent);

        imgCurrentConditionsImage = findViewById(R.id.current_conditions_image);
        txtCurrentTemperature = findViewById(R.id.current_temperature);
        txtCurrentTemperatureHighLow = findViewById(R.id.current_temperature_high_low);
        txtCurrentConditionsDescription = findViewById(R.id.current_conditions_description);
        txtFeelsLike = findViewById(R.id.feels_like);
        txtPrecipitation = findViewById(R.id.precipitation_value);
        txtHumidity = findViewById(R.id.humidity_value);
        txtWind = findViewById(R.id.wind_value);

//        currentConditionsActivity = new CurrentConditionsActivity();
//        currentConditionsActivity.setCurrentTemperature("100");

        queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = callWeatherApi("fake location");
        queue.add(stringRequest);

    }

    private StringRequest callWeatherApi(String location) {
        final String API_KEY = "&appid=4ae663188d74e9a952417c9234e8f511";
        final String END_POINT = " https://api.openweathermap.org/data";
        final String VERSION = "2.5";
        final String ONE_CALL = "onecall?";
        final String TEMP_MEASUREMENT = "&units=imperial";

        //TODO properly get location data
        String coordinates = "lat=35.4590&lon=-97.4057";

        String url = END_POINT + "/" + VERSION + "/" + ONE_CALL + coordinates + TEMP_MEASUREMENT + API_KEY;

        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject result = new JSONObject(response);
                            JSONObject currentConditions = result.getJSONObject("current");
                            JSONArray minutelyConditions = result.getJSONArray("minutely");
                            JSONArray dailyConditions = result.getJSONArray("daily");
                            updateCurrentConditions(currentConditions, minutelyConditions, dailyConditions);

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
    }

    private void updateCurrentConditions(JSONObject currentConditions, JSONArray minutelyConditions, JSONArray dailyConditions) {
        String icon;
        String temp;
        String tempHighLow;
        String description;
        String feelsLike;
        String precip;
        String humidity;
        String wind;

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
            precip = minutelyConditions.getJSONObject(0).getString("precipitation") + "%";
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
}