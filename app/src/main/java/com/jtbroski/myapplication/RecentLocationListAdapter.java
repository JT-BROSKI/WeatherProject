package com.jtbroski.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;

public class RecentLocationListAdapter extends CursorAdapter {
    private Cursor cursor;
    private HomeViewModel viewModel;

    public RecentLocationListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.cursor = c;
    }

    public void closeCursor() {
        if (cursor != null && !cursor.isClosed())
            cursor.close();
    }

    public void setViewModel(HomeViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
        this.cursor = cursor;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_nav_location, parent, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtLocation = view.findViewById(R.id.txt_location);
        txtLocation.setText(cursor.getString(cursor.getColumnIndex(PreferenceDatabaseHelper.COLUMN_LOCATION)));

        int textColor = Utils.preferenceDbHelper.getDarkThemeFlag()
                ? Color.parseColor(context.getResources().getString(R.color.light_gray2))
                : Color.parseColor(context.getResources().getString(R.color.light_gray3));
        txtLocation.setTextColor(textColor);

        txtLocation.setOnClickListener(v -> {
            closeCursor();
            String recentLocationName = ((TextView) v).getText().toString();
            Utils.locationName = recentLocationName;
            viewModel.callWeatherApi(Utils.preferenceDbHelper.getRecentLocation(recentLocationName));
            viewModel.updateDrawerCursors();
            viewModel.closeDrawer();
        });

        ToggleButton toggleButton = view.findViewById(R.id.btn_favorite);
        toggleButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_favorite_off));
        toggleButton.setOnClickListener(v -> {
            closeCursor();
            TextView txtLocation1 = (TextView) ((ViewGroup) v.getParent()).getChildAt(0);
            String location = txtLocation1.getText().toString();
            Utils.preferenceDbHelper.updateFavoriteLocations(location);
            viewModel.updateDrawerCursors();
        });
    }
}
