package com.jtbroski.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class Preferences {

    private static final String PREFERRED_LOCATION_KEY = "preferred_location";

    private static Preferences instance;
    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.R)
    private Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences("preference_db", Context.MODE_PRIVATE);
//        sharedPreferences.edit().clear().commit();

        if (getPreferredLocation() == null) {
            getCurrentLocation(context);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Preferences getInstance(Context context) {
        if (instance == null) {
            instance = new Preferences(context);
        }

        return instance;
    }

    public Location getPreferredLocation() {
        Gson gson = new Gson();
        Type type = new TypeToken<Location>() {}.getType();
        Location preferredLocation = gson.fromJson(sharedPreferences.getString(PREFERRED_LOCATION_KEY, null), type);
        return preferredLocation;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void getCurrentLocation(Context context) {   // TODO Potential refactor this to correctly ask and handle location permission access/deny results
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createAlertMessageNoGps(context);
        } else {
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // ask permissions here using below code
                ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            Consumer<Location> myConsumer = new MyConsumer(context);
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), myConsumer);
        }
    }

    private void createAlertMessageNoGps(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("GPS location is disabled, would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void savePreferredLocation(Location location) {
        Gson gson = new Gson();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(PREFERRED_LOCATION_KEY);
        editor.putString(PREFERRED_LOCATION_KEY, gson.toJson(location));
        editor.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class MyConsumer implements Consumer<Location>  {

        public Location location;
        private Context context;

        public MyConsumer(Context context) {
            this.context = context;
        }

        public Location getLocation() {
            return location;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        public void accept(Location location) {
            this.location = location;
            ((MainActivity)context).callWeatherApi(location);
            Preferences.getInstance(context).savePreferredLocation(location);
        }

        @Override
        public Consumer<Location> andThen(Consumer<? super Location> after) {
            return null;
        }
    }
}
