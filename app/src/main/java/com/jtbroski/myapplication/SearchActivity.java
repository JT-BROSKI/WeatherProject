package com.jtbroski.myapplication;

import android.annotation.SuppressLint;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SearchActivity extends AppCompatActivity {
    private SearchFilterAdapter searchFilterAdapter;
    private Cursor citiesFilteredCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Toolbar Back Arrow
        ImageButton backArrowButton = findViewById(R.id.btn_backArrow);
        backArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        backArrowButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(SearchActivity.this, R.color.black)
                                : ContextCompat.getColor(SearchActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(SearchActivity.this, R.color.transparent));
                        break;
                }
                return false;
            }
        });

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

                    // Check whether the geocoder can find a location matching the string within the EditText
                    if (Utils.checkLocationValidity(location)) {
                        Utils.refreshMainActivity();
                        onBackPressed();
                    } else {
                        Toast.makeText(SearchActivity.this, "Unable to find any locations matching with " + location, Toast.LENGTH_SHORT).show();
                    }
                }

                return false;
            }
        });
    }

    // Update the theme of the activity
    private void updateTheme(boolean isDark) {
        if (isDark) {
            setTheme(R.style.Theme_UI_Dark);
        } else {
            setTheme(R.style.Theme_UI_Light);
        }
    }
}