package com.jtbroski.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;

public class WeatherAlertActivity extends AppCompatActivity {
    public final static String ALERT_DATA_ID = "ALERT_DATA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_alert);

        // Toolbar Back Arrow
        ImageButton backArrowButton = findViewById(R.id.btn_backArrow);
        backArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        backArrowButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(WeatherAlertActivity.this, R.color.black)
                                : ContextCompat.getColor(WeatherAlertActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(WeatherAlertActivity.this, R.color.transparent));
                        break;
                }
                return false;
            }
        });

        Bundle bundle = getIntent().getExtras();
        ArrayList<WeatherAlert> weatherAlerts = bundle.getParcelableArrayList(ALERT_DATA_ID);

        WeatherAlertRecViewAdapter weatherAlertRecViewAdapter = new WeatherAlertRecViewAdapter(this, weatherAlerts);
        RecyclerView weatherAlertRecView = findViewById(R.id.weather_alert_recycler_view);
        weatherAlertRecView.setAdapter(weatherAlertRecViewAdapter);
        weatherAlertRecView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    // Update the theme of the activity
    private void updateTheme(boolean isDark) {
        if (isDark) {
            setTheme(R.style.Theme_UI_Dark);
        } else {
            setTheme(R.style.Theme_UI_Light);
        }
    }
}