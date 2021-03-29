package com.jtbroski.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    boolean isOriginallyImperial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean inImperial = Utils.preferenceDbHelper.getImperialFlag();
        boolean inDarkTheme = Utils.preferenceDbHelper.getDarkThemeFlag();
        updateSettingsTheme(inDarkTheme);
        isOriginallyImperial = inImperial;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Toolbar Back Arrow
        ImageButton backArrowButton = findViewById(R.id.btn_backArrow);
        backArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForChange();
                onBackPressed();
            }
        });

        // Units Radio Group
        RadioButton imperialRadioButton = findViewById(R.id.rb_imperial);
        imperialRadioButton.setChecked(inImperial);

        RadioButton metricRadioButton = findViewById(R.id.rb_metric);
        metricRadioButton.setChecked(!inImperial);

        RadioGroup unitsRadioGroup = findViewById(R.id.rg_units);
        unitsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = unitsRadioGroup.findViewById(checkedId);
                boolean isImperial = checkedRadioButton.getText().toString().equals(getString(R.string.imperial));
                Utils.preferenceDbHelper.updateImperialFlag(isImperial);
            }
        });

        // Theme Radio Group
        RadioButton lightRadioButton = findViewById(R.id.rb_light);
        lightRadioButton.setChecked(!inDarkTheme);

        RadioButton darkRadioButton = findViewById(R.id.rb_dark);
        darkRadioButton.setChecked(inDarkTheme);

        RadioGroup themeRadioGroup = findViewById(R.id.rg_theme);
        themeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = themeRadioGroup.findViewById(checkedId);
                boolean isDark = checkedRadioButton.getText().toString().equals(getString(R.string.dark));
                Utils.preferenceDbHelper.updateDarkThemeFlag(isDark);
                updateSettingsTheme(isDark);
            }
        });
    }

    private void checkForChange() {
        boolean inImperial = Utils.preferenceDbHelper.getImperialFlag();
        if (inImperial != isOriginallyImperial) {
            Utils.refreshMainActivity();
        }
    }

    private void updateSettingsTheme(boolean isDark) {
        if (isDark) {
            setTheme(R.style.Theme_UI_Dark);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            setTheme(R.style.Theme_UI_Light);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}