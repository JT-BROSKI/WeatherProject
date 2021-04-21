package com.jtbroski.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;

public class FavoriteLocationListAdapter extends CursorAdapter {
    private final Context mContext;

    public FavoriteLocationListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_nav_location, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtLocation = view.findViewById(R.id.txt_location);
        txtLocation.setText(cursor.getString(cursor.getColumnIndex(PreferenceDatabaseHelper.COLUMN_LOCATION)));
        txtLocation.setOnClickListener(v -> {
            String favoriteLocationName = ((TextView) v).getText().toString();
            Utils.locationName = favoriteLocationName;
            ((MainActivity) mContext).callWeatherApi(Utils.preferenceDbHelper.getFavoriteLocation((favoriteLocationName)));
            ((MainActivity) mContext).closeDrawerLayout();
        });

        ToggleButton toggleButton = view.findViewById(R.id.btn_favorite);
        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_on));
        toggleButton.setOnClickListener(v -> {
            TextView txtLocation1 = (TextView) ((ViewGroup) v.getParent()).getChildAt(0);
            String location = txtLocation1.getText().toString();
            Utils.preferenceDbHelper.deleteFavoriteLocation(location);
            ((MainActivity) mContext).updateNavigationListViews();
        });
    }
}
