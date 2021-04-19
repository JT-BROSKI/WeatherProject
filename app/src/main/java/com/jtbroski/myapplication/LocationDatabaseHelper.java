package com.jtbroski.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LocationDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "locations.db";
    private final File DB_FILE;

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

    public LocationDatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        DB_FILE = context.getDatabasePath(DB_NAME);

        populateDatabase(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Returns the cursor from the a raw query filtered based on the text parameter
    public Cursor getCitiesFilteredCursor(String text) {
        String[] columns = new String[]{"_id", "city", "country"};
        MatrixCursor matrixCursor = new MatrixCursor(columns);

        if (!text.isEmpty()) {
            char firstLetter = text.charAt(0);
            String table = getAlphabeticCityDatabase(firstLetter);

            if (table != null) {
                SQLiteDatabase db = this.getReadableDatabase();
                String query = "SELECT * FROM " + table + " WHERE " + COLUMN_CITY + " LIKE '" + text + "%'";
                Cursor dbCursor = db.rawQuery(query, null);

                if (dbCursor.moveToFirst()) {
                    int id = 1;
                    do {
                        matrixCursor.addRow(new String[]{String.valueOf(id), dbCursor.getString(0), dbCursor.getString(1)});
                        id++;
                    } while (dbCursor.moveToNext());
                }
                db.close();
                dbCursor.close();
            }
        }

        return matrixCursor;
    }

    // Copy the contents within the locations database in assets to the actual locations database used by the program
    private void copyDatabase(Context context) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = context.getAssets().open(DB_NAME);
            outputStream = new FileOutputStream(DB_FILE);

            int length;
            byte[] buffer = new byte[1024];
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Unable to copy locations database.", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Toast.makeText(context, "Failed to close input/output stream.", Toast.LENGTH_SHORT).show();
            }
        }
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

    // Populate the locations database, if applicable
    private void populateDatabase(Context context) {
        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + CITIES_A_TABLE + "'";
        Cursor dbCursor = db.rawQuery(query, null);

        if (!dbCursor.moveToFirst()) {
            copyDatabase(context);
        }
        db.close();
        dbCursor.close();
    }
}
