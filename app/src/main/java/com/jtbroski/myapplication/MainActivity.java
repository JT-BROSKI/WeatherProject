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
//import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private ScrollView parent;

    private ImageView imgCurrentConditionsImage;

    private TextView txtCurrentTemperature;
    private TextView txtCurrentTemperatureHighLow;
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
        txtPrecipitation = findViewById(R.id.precipitation_value);
        txtHumidity = findViewById(R.id.humidity_value);
        txtWind = findViewById(R.id.wind_value);

//        currentConditionsActivity = new CurrentConditionsActivity();
//        currentConditionsActivity.setCurrentTemperature("100");

        queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = callWeatherApi("fake location");
        queue.add(stringRequest);

    }

    private StringRequest callWeatherApi(String location)
    {
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
                            updateCurrentConditions(currentConditions, minutelyConditions);

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "JSON Parsing Failed", Toast.LENGTH_SHORT).show();
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

    private void updateCurrentConditions(JSONObject currentConditions, JSONArray minutelyConditions)
    {
        String temp;
        String tempHighLow;
        String precip;
        String humidity;
        String wind;

        try {
            temp = currentConditions.getString("temp");
            temp = Utils.roundStringNumberValue(temp);
        } catch (Exception e) {
            temp = "N/A";
        }

        // TODO Compile all temperatures within the day and determine high and low
//        try {
//            tempHighLow = currentConditions.getString("temp");
//        } catch (Exception e) {
//            tempHighLow = "N/A";
//        }
        tempHighLow = "75 | 25";

        try {
            precip = minutelyConditions.getJSONObject(0).getString("precipitation") + "%";
        } catch (Exception e) {
            precip = "N/A";
        }

        try {
            humidity = currentConditions.getString("humidity") + "%";
        } catch (Exception e) {
            humidity = "N/A";
        }

        // TODO Determine whether wind_gust is a necessary piece of information
        // TODO Create wind direction conversion utility function
        try {
            wind = currentConditions.getString("wind_speed");
            wind = Utils.roundStringNumberValue(wind) + "(N)";
        } catch (Exception e) {
            wind = "N/A";
        }

        txtCurrentTemperature.setText(temp);
        txtCurrentTemperatureHighLow.setText(tempHighLow);
        txtPrecipitation.setText(precip);
        txtHumidity.setText(humidity);
        txtWind.setText(wind);

        // TODO Create proper utility function for determining weather icon
//        Glide.with(this)
//                .asBitmap()
//                .load("http://openweathermap.org/img/wn/01d@2x.png")
//                .into(imgCurrentConditionsImage);

    }
}