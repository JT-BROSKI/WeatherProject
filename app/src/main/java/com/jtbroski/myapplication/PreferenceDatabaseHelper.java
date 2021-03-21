package com.jtbroski.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class PreferenceDatabaseHelper extends SQLiteOpenHelper {

    private static final String PREFERRED_LOCATION_TABLE = "PREFERRED_LOCATION_TABLE";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";

    public PreferenceDatabaseHelper(@Nullable Context context) {
        super(context, "preferences.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create preferred location table
        String createPreferredLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + PREFERRED_LOCATION_TABLE + " (" + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createPreferredLocationTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

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

            Consumer<Location> myConsumer = new PreferenceDatabaseHelper.MyConsumer(context);
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), myConsumer);
        }
    }

    public Location getPreferredLocation() {
        Location preferredLocation = null;

        if (hasPreferredLocation()) {
            String queryString = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(queryString, null);
            cursor.moveToFirst();

            preferredLocation = new Location(LocationManager.GPS_PROVIDER);
            preferredLocation.setLatitude(cursor.getDouble(0));
            preferredLocation.setLongitude(cursor.getDouble(1));

            cursor.close();
            db.close();
        }

        return preferredLocation;
    }

    public boolean updatePreferredLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_LATITUDE, location.getLatitude());
        cv.put(COLUMN_LONGITUDE, location.getLongitude());

        long insert;
        if (hasPreferredLocation()) {
            insert = db.update(PREFERRED_LOCATION_TABLE, cv, null, null);
        }
        else {
            insert = db.insert(PREFERRED_LOCATION_TABLE, null, cv);
        }

        db.close();
        return insert == 1;
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

    private boolean hasPreferredLocation() {
        String queryString = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }

    private class MyConsumer implements Consumer<Location> {

        public Location location;
        private Context context;

        public MyConsumer(Context context) {
            this.context = context;
        }

        public Location getLocation() {
            return location;
        }

        @Override
        public void accept(Location location) {
            this.location = location;
            ((MainActivity)context).callWeatherApi(location);

            if(!Utils.getInstance(context).preferenceDbHelper.updatePreferredLocation(location)) {
                Toast.makeText(context, "Unable to save preferred location.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public Consumer<Location> andThen(Consumer<? super Location> after) {
            return null;
        }
    }
}
