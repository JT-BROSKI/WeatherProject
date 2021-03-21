package com.jtbroski.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class SearchFilterAdapter extends CursorAdapter {
    private Context context;

    public SearchFilterAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);

        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_search_suggestion, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtSearchSuggestion = view.findViewById(R.id.txt_Search_Suggestion);

        String city = cursor.getString(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_CITY));
        String country = cursor.getString(cursor.getColumnIndexOrThrow(LocationDatabaseHelper.COLUMN_COUNTRY));
        String citySuggestion = city + ", " + country;
        txtSearchSuggestion.setText(citySuggestion);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView)((ViewGroup)v).getChildAt(0);
                ((MainActivity)context).callWeatherApi(textView.getText().toString());
                ((MainActivity)context).collapseSearchView();
            }
        });
    }
}
