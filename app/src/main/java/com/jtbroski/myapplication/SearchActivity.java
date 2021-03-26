package com.jtbroski.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SearchActivity extends AppCompatActivity {
    private SearchFilterAdapter searchFilterAdapter;
    private Cursor citiesFilteredCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        searchFilterAdapter = new SearchFilterAdapter(this, citiesFilteredCursor, false);
        ListView searchFilterList = findViewById(R.id.search_filter_list);
        searchFilterList.setAdapter(searchFilterAdapter);

        EditText searchEditText = findViewById(R.id.search_txtView);
        searchEditText.requestFocus();
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                citiesFilteredCursor = s.length() > 2 ? Utils.locationDbHelper.getCitiesFilteredCursor(s.toString()) :
                                                        Utils.locationDbHelper.getCitiesFilteredCursor("");
                searchFilterAdapter.changeCursor(citiesFilteredCursor);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String location = textView.getText().toString();

                    Utils.locationName = location;
                    Utils.forwardToWeatherApiCall(location);
                    onBackPressed();
                }

                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}