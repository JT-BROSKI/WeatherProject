package com.jtbroski.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import java.util.function.Consumer;

public class PreferenceDatabaseHelper extends SQLiteOpenHelper {

    private static final String PREFERRED_LOCATION_TABLE = "PREFERRED_LOCATION_TABLE";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";

    private static final String SETTINGS_TABLE = "SETTINGS_TABLE";
    private static final String COLUMN_SETTINGS = "SETTINGS";
    private static final String COLUMN_FLAG = "FLAG";

    private static final String IN_IMPERIAL = "IN_IMPERIAL";
    private static final String DARK_THEME = "DARK_THEME";

    private Context context;

    public PreferenceDatabaseHelper(@Nullable Context context) {
        super(context, "preferences.db", null, 1);
        this.context = context;
        initializeDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create preferred location table
        String createPreferredLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + PREFERRED_LOCATION_TABLE + " (" + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createPreferredLocationTableStatement);

        // Create settings table
        String createSettingsTableStatment = "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + " (" + COLUMN_SETTINGS + " TEXT, " + COLUMN_FLAG + " BOOL)";
        db.execSQL(createSettingsTableStatment);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void getCurrentLocation() {   // TODO Potential refactor this to correctly ask and handle location permission access/deny results
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createAlertMessageNoGps();
        } else {
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // ask permissions here using below code
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            Consumer<Location> myConsumer = new PreferenceDatabaseHelper.MyConsumer(context);
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), myConsumer);
        }
    }

    public boolean getImperialFlag() {
        SQLiteDatabase db = getReadableDatabase();
        String queryString = "SELECT " +  COLUMN_FLAG + " FROM " + SETTINGS_TABLE + " WHERE " + COLUMN_SETTINGS + " LIKE '" + IN_IMPERIAL + "'";
        Cursor cursor = db.rawQuery(queryString, null);
        cursor.moveToFirst();

        boolean flag = cursor.getInt(0) == 1;

        db.close();
        cursor.close();

        return flag;
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
        } else {
            insert = db.insert(PREFERRED_LOCATION_TABLE, null, cv);
        }

        db.close();
        return insert == 1;
    }

    public void updateImperialFlag(boolean flag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_SETTINGS, IN_IMPERIAL);
        cv.put(COLUMN_FLAG, flag);
        db.update(SETTINGS_TABLE, cv, COLUMN_SETTINGS + " = ?", new String[] {IN_IMPERIAL});

        db.close();
    }

    private void createAlertMessageNoGps() {
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

    private boolean hasSettings() {
        String queryString = "SELECT * FROM " + SETTINGS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }

    // Initializes the database and if applicable add the data to the tables
    private void initializeDatabase() {
        if (!hasPreferredLocation()) {
            getCurrentLocation();
        }

        if (!hasSettings()) {
            setSettingsToDefaults();
        }
    }

    // Set the settings to defaults (use imperial units and light theme)
    private void setSettingsToDefaults() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Add the units settings to the table
        ContentValues unitsCv = new ContentValues();
        unitsCv.put(COLUMN_SETTINGS, IN_IMPERIAL);
        unitsCv.put(COLUMN_FLAG, true);
        db.insert(SETTINGS_TABLE, null, unitsCv);

        // Add the theme settings to the table
        ContentValues themeCv = new ContentValues();
        themeCv.put(COLUMN_SETTINGS, DARK_THEME);
        themeCv.put(COLUMN_FLAG, false);
        db.insert(SETTINGS_TABLE, null, themeCv);

        db.close();
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
            ((MainActivity) context).callWeatherApi(location);

            if (!Utils.getInstance(context).preferenceDbHelper.updatePreferredLocation(location)) {
                Toast.makeText(context, "Unable to save preferred location.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public Consumer<Location> andThen(Consumer<? super Location> after) {
            return null;
        }
    }
}
