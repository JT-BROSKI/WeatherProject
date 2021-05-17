package com.jtbroski.myapplication.ui.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.jtbroski.myapplication.LocationDatabaseHelper;
import com.jtbroski.myapplication.R;
import com.jtbroski.myapplication.Utils;

public class SearchFilterAdapter extends CursorAdapter {
    private final Context mContext;
    private Cursor cursor;

    public MutableLiveData<Boolean> refreshHomeFragment;

    public SearchFilterAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.mContext = context;
        this.cursor = c;

        refreshHomeFragment = new MutableLiveData<>(false);
    }

    public void closeCursor() {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        this.cursor = cursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_search_suggestion, parent, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtSearchSuggestion = view.findViewById(R.id.txt_Search_Suggestion);

        String city = cursor.getString(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_CITY));
        String country = cursor.getString(cursor.getColumnIndex(LocationDatabaseHelper.COLUMN_COUNTRY));
        String citySuggestion = city + ", " + country;
        txtSearchSuggestion.setText(citySuggestion);

        // For some reason list_item_search_suggestion will not use its designated attribute values for its background and text color
        // So we have to manually set it here
        if (Utils.preferenceDbHelper.getDarkThemeFlag()) {
            txtSearchSuggestion.setBackgroundColor(Color.parseColor(context.getResources().getString(R.color.dark_gray1)));
            txtSearchSuggestion.setTextColor(Color.parseColor(context.getResources().getString(R.color.light_gray1)));
        } else {
            txtSearchSuggestion.setBackgroundColor(Color.parseColor(context.getResources().getString(R.color.white)));
            txtSearchSuggestion.setTextColor(Color.parseColor(context.getResources().getString(R.color.black)));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) ((ViewGroup) v).getChildAt(0);
                String location = textView.getText().toString();
                closeCursor();

                // Ensure whether the geocoder can find a location matching the preloaded location string
                if (Utils.checkLocationValidity(location)) {
                    refreshHomeFragment.setValue(true);
                    refreshHomeFragment.postValue(false);   // Reset the refresh flag to avoid a circular navigation bug where if you try to navigate to the search fragment, it will navigate you back to the home fragment
                } else {
                    Toast.makeText(mContext, "Unable to find any locations matching with " + location, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
