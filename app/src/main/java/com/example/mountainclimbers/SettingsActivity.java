package com.example.mountainclimbers;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFERENCES = "preferences";
    public static final String SPEED = "speed";
    public static final String LANDSCAPE_LOCKED = "landscape_locked";

    private SeekBar speedBar;
    private Switch landscapeLockSwitch;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private int speed;
    private boolean isLandscapeLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();

        speedBar = findViewById(R.id.settingSpeedSeekBar);
        speed = preferences.getInt(SPEED, 0);
        speedBar.setProgress(speed);
        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speed = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt(SPEED, speed);
            }
        });

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
