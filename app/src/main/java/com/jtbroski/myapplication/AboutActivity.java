package com.jtbroski.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateTheme(Utils.preferenceDbHelper.getDarkThemeFlag());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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
                        v.setBackgroundColor(Utils.preferenceDbHelper.getDarkThemeFlag() ? ContextCompat.getColor(AboutActivity.this, R.color.black)
                                : ContextCompat.getColor(AboutActivity.this, R.color.purple_700));
                        break;

                    case MotionEvent.ACTION_UP:
                        v.setBackgroundColor(ContextCompat.getColor(AboutActivity.this, R.color.transparent));
                        break;
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