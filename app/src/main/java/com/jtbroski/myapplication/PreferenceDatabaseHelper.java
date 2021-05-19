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

import com.jtbroski.myapplication.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PreferenceDatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "preferences.db";
    public static final int REQUEST_LOCATION_PERMISSION = 1;

    private static final String PREFERRED_LOCATION_TABLE = "PREFERRED_LOCATION_TABLE";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";

    private static final String SETTINGS_TABLE = "SETTINGS_TABLE";
    private static final String COLUMN_SETTINGS = "SETTINGS";
    private static final String COLUMN_FLAG = "FLAG";

    private static final String IN_IMPERIAL = "IN_IMPERIAL";
    private static final String DARK_THEME = "DARK_THEME";

    private static final String FAVORITE_LOCATION_TABLE = "FAVORITE_LOCATION_TABLE";
    private static final String RECENT_LOCATION_TABLE = "RECENT_LOCATION_TABLE";
    public static final String COLUMN_LOCATION = "LOCATION";

    public PreferenceDatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);
        initializeDatabase(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create preferred location table
        String createPreferredLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + PREFERRED_LOCATION_TABLE + " (" + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createPreferredLocationTableStatement);

        // Create settings table
        String createSettingsTableStatement = "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + " (" + COLUMN_SETTINGS + " TEXT, " + COLUMN_FLAG + " BOOL)";
        db.execSQL(createSettingsTableStatement);

        // Create favorite location table
        String createFavoriteLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + FAVORITE_LOCATION_TABLE + " (" + COLUMN_LOCATION + " TEXT, " + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createFavoriteLocationTableStatement);

        // Create recent location table
        String createRecentLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + RECENT_LOCATION_TABLE + " (" + COLUMN_LOCATION + " TEXT, " + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createRecentLocationTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteFavoriteLocation(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FAVORITE_LOCATION_TABLE, COLUMN_LOCATION + "=?", new String[]{name});
        db.close();
    }

    public void getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            createAlertMessageNoGps(context);
        } else {
            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // ask permissions here using below code
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                Consumer<Location> myConsumer = new PreferenceDatabaseHelper.MyConsumer(context);
                locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), myConsumer);
            }
        }
    }

    public boolean getDarkThemeFlag() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_FLAG + " FROM " + SETTINGS_TABLE + " WHERE " + COLUMN_SETTINGS + " LIKE '" + DARK_THEME + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        boolean flag = cursor.getInt(0) == 1;

        db.close();
        cursor.close();

        return flag;
    }

    public Location getFavoriteLocation(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + FAVORITE_LOCATION_TABLE + " WHERE " + COLUMN_LOCATION + " LIKE '%" + name + "%'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        Location favoriteLocation = new Location(LocationManager.GPS_PROVIDER);
        favoriteLocation.setLatitude(cursor.getDouble(1));
        favoriteLocation.setLongitude(cursor.getDouble(2));
        cursor.close();

        return favoriteLocation;
    }

    public Cursor getFavoriteLocations() {
        String[] columns = new String[]{"_id", "location"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + FAVORITE_LOCATION_TABLE;
        Cursor dbCursor = db.rawQuery(query, null);

        if (dbCursor.moveToFirst()) {
            int id = 1;
            do {
                matrixCursor.addRow(new String[]{String.valueOf(id), dbCursor.getString(0)});
                id++;
            } while (dbCursor.moveToNext());
        }
        db.close();
        dbCursor.close();

        return matrixCursor;
    }

    public boolean getImperialFlag() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_FLAG + " FROM " + SETTINGS_TABLE + " WHERE " + COLUMN_SETTINGS + " LIKE '" + IN_IMPERIAL + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        boolean flag = cursor.getInt(0) == 1;

        db.close();
        cursor.close();

        return flag;
    }

    public Location getPreferredLocation() {
        Location preferredLocation = null;

        if (hasPreferredLocation()) {
            String query = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.rawQuery(query, null);
            cursor.moveToFirst();

            preferredLocation = new Location(LocationManager.GPS_PROVIDER);
            preferredLocation.setLatitude(cursor.getDouble(0));
            preferredLocation.setLongitude(cursor.getDouble(1));

            cursor.close();
            db.close();
        }

        return preferredLocation;
    }

    public Location getRecentLocation(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + RECENT_LOCATION_TABLE + " WHERE " + COLUMN_LOCATION + " LIKE '%" + name + "%'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        Location recentLocation = new Location(LocationManager.GPS_PROVIDER);
        recentLocation.setLatitude(cursor.getDouble(1));
        recentLocation.setLongitude(cursor.getDouble(2));
        cursor.close();

        return recentLocation;
    }

    public Cursor getRecentLocations() {
        String[] columns = new String[]{"_id", "location"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + RECENT_LOCATION_TABLE;
        Cursor dbCursor = db.rawQuery(query, null);

        if (dbCursor.moveToFirst()) {
            int id = 1;
            do {
                matrixCursor.addRow(new String[]{String.valueOf(id), dbCursor.getString(0)});
                id++;
            } while (dbCursor.moveToNext());
        }
        db.close();
        dbCursor.close();

        return matrixCursor;
    }

    public void requestCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Consumer<Location> myConsumer = new PreferenceDatabaseHelper.MyConsumer(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getCurrentLocation(LocationManager.GPS_PROVIDER, null, context.getMainExecutor(), myConsumer);
        }
    }

    public boolean updatePreferredLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues preferredLocation = new ContentValues();

        preferredLocation.put(COLUMN_LATITUDE, location.getLatitude());
        preferredLocation.put(COLUMN_LONGITUDE, location.getLongitude());

        long insert;
        if (hasPreferredLocation()) {
            insert = db.update(PREFERRED_LOCATION_TABLE, preferredLocation, null, null);
        } else {
            insert = db.insert(PREFERRED_LOCATION_TABLE, null, preferredLocation);
        }

        db.close();
        return insert == 1;
    }

    public void updateImperialFlag(boolean flag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues imperialUnitsPreference = new ContentValues();

        imperialUnitsPreference.put(COLUMN_SETTINGS, IN_IMPERIAL);
        imperialUnitsPreference.put(COLUMN_FLAG, flag);
        db.update(SETTINGS_TABLE, imperialUnitsPreference, COLUMN_SETTINGS + " = ?", new String[]{IN_IMPERIAL});

        db.close();
    }

    public void updateDarkThemeFlag(boolean flag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues darkThemePreference = new ContentValues();

        darkThemePreference.put(COLUMN_SETTINGS, DARK_THEME);
        darkThemePreference.put(COLUMN_FLAG, flag);
        db.update(SETTINGS_TABLE, darkThemePreference, COLUMN_SETTINGS + " = ?", new String[]{DARK_THEME});

        db.close();
    }

    public void updateFavoriteLocations(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the entry from the recent locations table
        String recentQuery = "SELECT * FROM " + RECENT_LOCATION_TABLE + " WHERE " + COLUMN_LOCATION + " LIKE '%" + name + "%'";
        Cursor recentCursor = db.rawQuery(recentQuery, null);
        recentCursor.moveToFirst();

        double recentLocationLatitude = recentCursor.getDouble(1);
        double recentLocationLongitude = recentCursor.getDouble(2);

        db.delete(RECENT_LOCATION_TABLE, COLUMN_LOCATION + "=?", new String[]{name});
        recentCursor.close();

        // Add the a need entry to the favorite locations table
        ContentValues newFavoriteLocation = new ContentValues();
        newFavoriteLocation.put(COLUMN_LOCATION, name);
        newFavoriteLocation.put(COLUMN_LATITUDE, recentLocationLatitude);
        newFavoriteLocation.put(COLUMN_LONGITUDE, recentLocationLongitude);
        db.insert(FAVORITE_LOCATION_TABLE, null, newFavoriteLocation);

        db.close();
    }

    public void updateRecentLocations(String name, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + RECENT_LOCATION_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        ContentValues newRecentLocation = new ContentValues();
        newRecentLocation.put(COLUMN_LOCATION, name);
        newRecentLocation.put(COLUMN_LATITUDE, latitude);
        newRecentLocation.put(COLUMN_LONGITUDE, longitude);

        boolean hasResults = cursor.moveToFirst();
        if (hasResults) {
            if (checkForExistingLocation(db, name)) {       // If the table already contains a location with the same name, don't add the new location
                cursor.close();
                db.close();
                return;
            }
            else {
                // Get the original entries within the recent locations table
                ArrayList<ContentValues> recentLocations = new ArrayList<>();
                do {
                    ContentValues recentLocationValues = new ContentValues();
                    recentLocationValues.put(COLUMN_LOCATION, cursor.getString(0));
                    recentLocationValues.put(COLUMN_LATITUDE, cursor.getDouble(1));
                    recentLocationValues.put(COLUMN_LONGITUDE, cursor.getDouble(2));
                    recentLocations.add(recentLocationValues);
                } while (cursor.moveToNext());

                // If there are currently 5 recent locations, then delete the last one
                if (recentLocations.size() == 5) {
                    recentLocations.remove(recentLocations.size() - 1);
                }

                // Delete all the rows within the recent location table
                db.delete(RECENT_LOCATION_TABLE,null,null);

                // Insert the new recent location at the top of the table, then insert the rest of the original recent locations in the table
                db.insert(RECENT_LOCATION_TABLE, null, newRecentLocation);
                for (ContentValues recentLocation : recentLocations) {
                    db.insert(RECENT_LOCATION_TABLE, null, recentLocation);
                }
            }
        } else {
            db.insert(RECENT_LOCATION_TABLE, null, newRecentLocation);
        }

        cursor.close();
        db.close();
    }

    private boolean checkForExistingLocation(SQLiteDatabase db, String name) {
        String recentQuery = "SELECT * FROM " + RECENT_LOCATION_TABLE + " WHERE " + COLUMN_LOCATION + " LIKE '%" + name + "%'";
        Cursor recentCursor = db.rawQuery(recentQuery, null);
        boolean hasRecentResults = recentCursor.moveToFirst();
        recentCursor.close();

        String favoriteQuery = "SELECT * FROM " + FAVORITE_LOCATION_TABLE + " WHERE " + COLUMN_LOCATION + " LIKE '%" + name + "%'";
        Cursor favoriteCursor = db.rawQuery(favoriteQuery, null);
        boolean hasFavoriteResults = favoriteCursor.moveToFirst();
        favoriteCursor.close();

        return hasRecentResults || hasFavoriteResults;
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
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }

    private boolean hasSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + SETTINGS_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }

    // Initializes the database and if applicable add the data to the tables
    private void initializeDatabase(Context context) {
        if (!hasPreferredLocation()) {
            getCurrentLocation(context);
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
        themeCv.put(COLUMN_FLAG, true);
        db.insert(SETTINGS_TABLE, null, themeCv);

        db.close();
    }

    private class MyConsumer implements Consumer<Location> {
        private final Context context;

        public MyConsumer(Context context) {
            this.context = context;
        }

        @Override
        public void accept(Location location) {
            ((MainActivity) context).callWeatherApi(location);

            if (!Utils.preferenceDbHelper.updatePreferredLocation(location)) {
                Toast.makeText(context, "Unable to save preferred location.", Toast.LENGTH_SHORT).show();
            } else {
                Utils.addMyLocationToRecentLocations(context, location);
            }
        }

        @Override
        public Consumer<Location> andThen(Consumer<? super Location> after) {
            return null;
        }
    }
}
