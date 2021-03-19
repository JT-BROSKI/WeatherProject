package com.jtbroski.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String PREFERRED_LOCATION_TABLE = "PREFERRED_LOCATION_TABLE";
    private static final String COLUMN_LATITUDE = "LATITUDE";
    private static final String COLUMN_LONGITUDE = "LONGITUDE";

    private static final String CITIES_A_TABLE = "CITIES_A_TABLE";
    private static final String CITIES_B_TABLE = "CITIES_B_TABLE";
    private static final String CITIES_C_TABLE = "CITIES_C_TABLE";
    private static final String CITIES_D_TABLE = "CITIES_D_TABLE";
    private static final String CITIES_E_TABLE = "CITIES_E_TABLE";
    private static final String CITIES_F_TABLE = "CITIES_F_TABLE";
    private static final String CITIES_G_TABLE = "CITIES_G_TABLE";
    private static final String CITIES_H_TABLE = "CITIES_H_TABLE";
    private static final String CITIES_I_TABLE = "CITIES_I_TABLE";
    private static final String CITIES_J_TABLE = "CITIES_J_TABLE";
    private static final String CITIES_K_TABLE = "CITIES_K_TABLE";
    private static final String CITIES_L_TABLE = "CITIES_L_TABLE";
    private static final String CITIES_M_TABLE = "CITIES_M_TABLE";
    private static final String CITIES_N_TABLE = "CITIES_N_TABLE";
    private static final String CITIES_O_TABLE = "CITIES_O_TABLE";
    private static final String CITIES_P_TABLE = "CITIES_P_TABLE";
    private static final String CITIES_Q_TABLE = "CITIES_Q_TABLE";
    private static final String CITIES_R_TABLE = "CITIES_R_TABLE";
    private static final String CITIES_S_TABLE = "CITIES_S_TABLE";
    private static final String CITIES_T_TABLE = "CITIES_T_TABLE";
    private static final String CITIES_U_TABLE = "CITIES_U_TABLE";
    private static final String CITIES_V_TABLE = "CITIES_V_TABLE";
    private static final String CITIES_W_TABLE = "CITIES_W_TABLE";
    private static final String CITIES_X_TABLE = "CITIES_X_TABLE";
    private static final String CITIES_Y_TABLE = "CITIES_Y_TABLE";
    private static final String CITIES_Z_TABLE = "CITIES_Z_TABLE";
    public static final String COLUMN_CITY = "CITY";
    public static final String COLUMN_COUNTRY = "COUNTRY";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "preferences.db", null, 1);
        populateAllCities(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create preferred location table
        String createPreferredLocationTableStatement = "CREATE TABLE IF NOT EXISTS " + PREFERRED_LOCATION_TABLE + " (" + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";
        db.execSQL(createPreferredLocationTableStatement);

        String createCities_A_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_A_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_A_TableStatement);

        String createCities_B_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_B_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_B_TableStatement);

        String createCities_C_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_C_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_C_TableStatement);

        String createCities_D_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_D_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_D_TableStatement);

        String createCities_E_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_E_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_E_TableStatement);

        String createCities_F_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_F_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_F_TableStatement);

        String createCities_G_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_G_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_G_TableStatement);

        String createCities_H_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_H_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_H_TableStatement);

        String createCities_I_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_I_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_I_TableStatement);

        String createCities_J_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_J_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_J_TableStatement);

        String createCities_K_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_K_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_K_TableStatement);

        String createCities_L_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_L_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_L_TableStatement);

        String createCities_M_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_M_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_M_TableStatement);

        String createCities_N_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_N_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_N_TableStatement);

        String createCities_O_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_O_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_O_TableStatement);

        String createCities_P_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_P_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_P_TableStatement);

        String createCities_Q_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_Q_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_Q_TableStatement);

        String createCities_R_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_R_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_R_TableStatement);

        String createCities_S_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_S_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_S_TableStatement);

        String createCities_T_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_T_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_T_TableStatement);

        String createCities_U_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_U_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_U_TableStatement);

        String createCities_V_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_V_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " TEXT)";
        db.execSQL(createCities_V_TableStatement);

        String createCities_W_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_W_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " REAL)";
        db.execSQL(createCities_W_TableStatement);

        String createCities_X_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_X_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " REAL)";
        db.execSQL(createCities_X_TableStatement);

        String createCities_Y_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_Y_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " REAL)";
        db.execSQL(createCities_Y_TableStatement);

        String createCities_Z_TableStatement = "CREATE TABLE IF NOT EXISTS " + CITIES_Z_TABLE + " (" + COLUMN_CITY + " TEXT, " + COLUMN_COUNTRY + " REAL)";
        db.execSQL(createCities_Z_TableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Returns the cursor from the a raw query filtered based on the text parameter
    public Cursor getCitiesFilteredCursor(String text) {
        String[] columns = new String[] {"_id", "city", "country"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        if (!text.isEmpty()) {
            char firstLetter = text.charAt(0);
            String table = getAlphabeticCityDatabase(firstLetter);

            if (table != null) {
                SQLiteDatabase db = this.getWritableDatabase();
                String query = "SELECT * FROM " + table + " WHERE " + COLUMN_CITY + " LIKE " + "'" + text + "%'";
                Cursor dbCursor = db.rawQuery(query, null);

                if (dbCursor.moveToFirst()) {
                    int id = 1;
                    do {
                        matrixCursor.addRow(new String[]{String.valueOf(id), dbCursor.getString(1), dbCursor.getString(2)});
                        id++;
                    } while (dbCursor.moveToNext());
                }
                db.close();
            }
        }

        return matrixCursor;
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

    private static String getAlphabeticCityDatabase(char firstLetter) {
        switch (Character.toUpperCase(firstLetter)) {
            case 'A':
                return CITIES_A_TABLE;

            case 'B':
                return CITIES_B_TABLE;

            case 'C':
                return CITIES_C_TABLE;

            case 'D':
                return CITIES_D_TABLE;

            case 'E':
                return CITIES_E_TABLE;

            case 'F':
                return CITIES_F_TABLE;

            case 'G':
                return CITIES_G_TABLE;

            case 'H':
                return CITIES_H_TABLE;

            case 'I':
                return CITIES_I_TABLE;

            case 'J':
                return CITIES_J_TABLE;

            case 'K':
                return CITIES_K_TABLE;

            case 'L':
                return CITIES_L_TABLE;

            case 'M':
                return CITIES_M_TABLE;

            case 'N':
                return CITIES_N_TABLE;

            case 'O':
                return CITIES_O_TABLE;

            case 'P':
                return CITIES_P_TABLE;

            case 'Q':
                return CITIES_Q_TABLE;

            case 'R':
                return CITIES_R_TABLE;

            case 'S':
                return CITIES_S_TABLE;

            case 'T':
                return CITIES_T_TABLE;

            case 'U':
                return CITIES_U_TABLE;

            case 'V':
                return CITIES_V_TABLE;

            case 'W':
                return CITIES_W_TABLE;

            case 'X':
                return CITIES_X_TABLE;

            case 'Y':
                return CITIES_Y_TABLE;

            case 'Z':
                return CITIES_Z_TABLE;

            default:
                return null;
        }
    }

    // Check to see if the cities database is populated. We only check CITIES_A_TABLE.
    private boolean hasCities() {
        String queryString = "SELECT * FROM " + CITIES_A_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();
        db.close();

        return hasResults;
    }

    private boolean hasPreferredLocation() {
        String queryString = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }

    private void populateAllCities(Context context) {
        if (!hasCities()) {

            // Populate non-US cities
            InputStream inputStream = context.getResources().openRawResource(R.raw.countries_and_cities);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int ctr;
            try {
                ctr = inputStream.read();
                while (ctr != -1) {
                    byteArrayOutputStream.write(ctr);
                    ctr = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SQLiteDatabase db = this.getWritableDatabase();
            try {
                String table;
                // Parse the data into jsonobject to get original data in form of json.
                JSONObject countries = new JSONObject(byteArrayOutputStream.toString());
                Iterator interator = countries.keys();
                while (interator.hasNext()) {
                    String country = interator.next().toString();
                    JSONArray cities = countries.getJSONArray(country);

                    for (int i = 0; i < cities.length(); i++) {
                        String city = cities.getString(i);
                        char firstLetter = city.charAt(0);

                        table = getAlphabeticCityDatabase(firstLetter);

                        long insert = -1;   // TODO remember to comment out to save for debugging later
                        if (table != null) {
                            ContentValues cv = new ContentValues();
                            cv.put(COLUMN_CITY, city);
                            cv.put(COLUMN_COUNTRY, country);

                            insert = db.insert(table, null, cv);
                        }

                        // TODO remember to comment out to save for debugging later
                        if (insert == -1) {
                            Toast.makeText(context, "Unable to add " + city + ", " + country + " to table " + table, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Populate US cities
            inputStream = context.getResources().openRawResource(R.raw.us_states_and_cities);
            byteArrayOutputStream = new ByteArrayOutputStream();

            try {
                ctr = inputStream.read();
                while (ctr != -1) {
                    byteArrayOutputStream.write(ctr);
                    ctr = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String table;
                // Parse the data into jsonobject to get original data in form of json.
                JSONObject states = new JSONObject(byteArrayOutputStream.toString());
                Iterator interator = states.keys();
                while (interator.hasNext()) {
                    String state = interator.next().toString();
                    JSONArray cities = states.getJSONArray(state);

                    for (int i = 0; i < cities.length(); i++) {
                        String city = cities.getString(i);
                        char firstLetter = city.charAt(0);

                        table = getAlphabeticCityDatabase(firstLetter);

                        long insert = -1;   // TODO remember to comment out to save for debugging later
                        if (table != null) {
                            ContentValues cv = new ContentValues();
                            cv.put(COLUMN_CITY, city + ", " + state);
                            cv.put(COLUMN_COUNTRY, "United States");

                            insert = db.insert(table, null, cv);
                        }

                        // TODO remember to comment out to save for debugging later
                        if (insert == -1) {
                            Toast.makeText(context, "Unable to add " + city + ", " + state + " to table " + table, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                db.close();
            }
        }
    }
}
