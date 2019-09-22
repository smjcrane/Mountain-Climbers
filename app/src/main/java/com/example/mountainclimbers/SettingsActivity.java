package com.example.mountainclimbers;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFERENCES = "preferences";
    public static final String SPEED = "speed";
    public static final String LANDSCAPE_LOCKED = "landscape_locked";
    
    private Switch landscapeLockSwitch;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ImageView imageTortoise, imageHare, imageLightning, imageInfinity;

    private int speed;
    private boolean isLandscapeLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();

        imageTortoise = findViewById(R.id.speedImageTortoise);
        imageHare = findViewById(R.id.speedImageHare);
        imageLightning = findViewById(R.id.speedImageLightning);
        imageInfinity = findViewById(R.id.speedImageInfinity);

        final Drawable selected = getDrawable(R.drawable.rectangle_border);

        imageTortoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SETTINGS", "Clicked on tortoise");
                imageTortoise.setBackground(selected);
                imageHare.setBackground(null);
                imageLightning.setBackground(null);
                imageInfinity.setBackground(null);
                editor.putInt(SPEED, 1);
            }
        });

        imageHare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SETTINGS", "Clicked on hare");
                imageHare.setBackground(selected);
                imageTortoise.setBackground(null);
                imageLightning.setBackground(null);
                imageInfinity.setBackground(null);
                editor.putInt(SPEED, 2);
            }
        });

        imageLightning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SETTINGS", "Clicked on lightning");
                imageLightning.setBackground(selected);
                imageTortoise.setBackground(null);
                imageHare.setBackground(null);
                imageInfinity.setBackground(null);
                editor.putInt(SPEED, 3);
            }
        });

        imageInfinity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SETTINGS", "Clicked on infinity");
                imageLightning.setBackground(null);
                imageTortoise.setBackground(null);
                imageHare.setBackground(null);
                imageInfinity.setBackground(selected);
                editor.putInt(SPEED, Integer.MAX_VALUE);
            }
        });

        speed = preferences.getInt(SPEED, 1);
        Log.d("SETTINGS", "The speed is "+speed);
        switch (speed){
            case 1:
                imageTortoise.callOnClick();
                break;
            case 2:
                imageHare.callOnClick();
                break;
            case 3:
                imageLightning.callOnClick();
                break;
            case Integer.MAX_VALUE:
                imageLightning.callOnClick();
                break;
        }

        landscapeLockSwitch = findViewById(R.id.settingsLandscapeSwitch);
        isLandscapeLocked = preferences.getBoolean(LANDSCAPE_LOCKED, true);
        landscapeLockSwitch.setChecked(isLandscapeLocked);
        landscapeLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(LANDSCAPE_LOCKED, isChecked);
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        editor.commit();
    }
}
