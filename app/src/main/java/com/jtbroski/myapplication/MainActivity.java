package com.jtbroski.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    private SearchViewModel searchViewModel;
    private SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        this.deleteDatabase(PreferenceDatabaseHelper.DB_NAME);  // Kept for debugging purposes
//        this.deleteDatabase(LocationDatabaseHelper.DB_NAME);    // Kept for debugging purposes
        Utils.initialize(this);
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        searchViewModel.getRefreshHomeFragment().observe(this, refreshHomeFragment -> {
            if (refreshHomeFragment)
                callWeatherApi(Utils.lastQueriedLocation);
        });
        searchViewModel.getRefreshHomeFragmentFromAdapter().observe(this, refreshHomeFragment -> {
            if (refreshHomeFragment)
                callWeatherApi(Utils.lastQueriedLocation);
        });

        settingsViewModel.getRbDarkIsChecked().observe(this, isDark -> updateTheme(isDark));
        settingsViewModel.getRefreshHomeFragment().observe(this, refreshHomeFragment -> {
            if (refreshHomeFragment)
                callWeatherApi(Utils.lastQueriedLocation);
        });
    }

    @Override
    public void onBackPressed() {
        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (!fragment.isVisible())
                    continue;

                if (fragment.isVisible() && fragment instanceof SettingsFragment)
                    settingsViewModel.checkForChange();
            }
        }

        super.onBackPressed();
    }

    // Calls the OpenWeather API in the home fragment and updates all weather conditions
    public void callWeatherApi(Location location) {
        homeViewModel.callWeatherApi(location);
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

    // Updates the list views within the home fragment's navigation drawer by update their cursors
    public void updateNavigationListViews() {
        homeViewModel.updateDrawerCursors();
    }

    // Updates the theme of the activity
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