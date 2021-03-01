package com.jtbroski.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String PREFERRED_LOCATION_TABLE = "PREFERRED_LOCATION_TABLE";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "preferences.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + PREFERRED_LOCATION_TABLE + " (" + COLUMN_LATITUDE + " REAL, " + COLUMN_LONGITUDE + " REAL)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    private boolean hasPreferredLocation() {
        String queryString = "SELECT * FROM " + PREFERRED_LOCATION_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);

        boolean hasResults = cursor.moveToFirst();
        cursor.close();

        return hasResults;
    }
}
