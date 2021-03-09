package com.jtbroski.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchFilterAdapter extends CursorAdapter {
    private Context context;
    private SearchView searchView;

    public SearchFilterAdapter(Context context, Cursor c, boolean autoRequery, SearchView searchView) {
        super(context, c, autoRequery);

        this.context = context;
        this.searchView = searchView;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_search_suggestion, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtSearchSuggestion = view.findViewById(R.id.txt_Search_Suggestion);

        String city = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CITY));
        String country = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COUNTRY));
        String citySuggestion = city + ", " + country;
        txtSearchSuggestion.setText(citySuggestion);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
