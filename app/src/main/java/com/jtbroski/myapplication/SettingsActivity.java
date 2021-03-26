package com.jtbroski.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private final String isInImperial = "isInImperial";
    private final String isInDarkTheme = "isInDarkTheme";
    private HashMap<String, Boolean> originalSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean inImperial = Utils.preferenceDbHelper.getImperialFlag();

        originalSettings = new HashMap<>();
        originalSettings.put(isInImperial, inImperial);

        RadioButton imperialRadioButton = findViewById(R.id.rb_imperial);
        imperialRadioButton.setChecked(inImperial);

        RadioButton metricRadioButton = findViewById(R.id.rb_metric);
        metricRadioButton.setChecked(!inImperial);

        RadioGroup unitsRadioGroup = findViewById(R.id.rg_units);
        unitsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = unitsRadioGroup.findViewById(checkedId);
                boolean isImperial = checkedRadioButton.getText().toString().equals("Imperial");
                Utils.preferenceDbHelper.updateImperialFlag(isImperial);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                checkForChange();
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkForChange() {
        boolean inImperial = Utils.preferenceDbHelper.getImperialFlag();
        if (inImperial != originalSettings.get(isInImperial)) {
            Utils.refreshMainActivity();
        }
    }
}