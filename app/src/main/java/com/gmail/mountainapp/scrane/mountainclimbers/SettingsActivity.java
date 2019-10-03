package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;


public class SettingsActivity extends SignedInActivity {

    public static final int SPEED_TORTOISE = 1;
    public static final int SPEED_HARE = 2;
    public static final int SPEED_LIGHTNING = 3;
    public static final int SPEED_INFINITY = Integer.MAX_VALUE;

    public static final int CLIMBER_CIRCLE = 0;
    public static final int CLIMBER_PEG = 1;
    public static final int CLIMBER_HOLLOW = 2;

    private Switch landscapeLockSwitch;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ImageView imageTortoise, imageHare, imageLightning, imageInfinity;
    private ImageView imageCircle, imagePeg, imageHollow;

    private Button doneButton;

    private int speed;
    private boolean isLandscapeLocked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();

        imageTortoise = findViewById(R.id.speedImageTortoise);
        imageHare = findViewById(R.id.speedImageHare);
        imageLightning = findViewById(R.id.speedImageLightning);
        imageInfinity = findViewById(R.id.speedImageInfinity);

        imageCircle = findViewById(R.id.climberImageCircle);
        imagePeg = findViewById(R.id.climberImagePeg);
        imageHollow = findViewById(R.id.climberImageHollow);

        final Drawable selected = getDrawable(R.drawable.rectangle_border);

        landscapeLockSwitch = findViewById(R.id.settingsLandscapeSwitch);
        isLandscapeLocked = preferences.getBoolean(getString(R.string.LANDSCAPE_LOCKED), true);
        landscapeLockSwitch.setChecked(isLandscapeLocked);
        landscapeLockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(getString(R.string.LANDSCAPE_LOCKED), isChecked);
            }
        });

        imageTortoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SETTINGS", "Clicked on tortoise");
                imageTortoise.setBackground(selected);
                imageHare.setBackground(null);
                imageLightning.setBackground(null);
                imageInfinity.setBackground(null);
                editor.putInt(getString(R.string.SPEED), SPEED_TORTOISE);
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
                editor.putInt(getString(R.string.SPEED), SPEED_HARE);
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
                editor.putInt(getString(R.string.SPEED), SPEED_LIGHTNING);
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
                editor.putInt(getString(R.string.SPEED), SPEED_INFINITY);
            }
        });

        speed = preferences.getInt(getString(R.string.SPEED), 1);
        Log.d("SETTINGS", "The speed is "+speed);
        switch (speed){
            case SPEED_TORTOISE:
                imageTortoise.callOnClick();
                break;
            case SPEED_HARE:
                imageHare.callOnClick();
                break;
            case SPEED_LIGHTNING:
                imageLightning.callOnClick();
                break;
            case SPEED_INFINITY:
                imageLightning.callOnClick();
                break;
        }

        imageCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCircle.setBackground(selected);
                imagePeg.setBackground(null);
                imageHollow.setBackground(null);
                editor.putInt(getString(R.string.CLIMBER_APPEARANCE), CLIMBER_CIRCLE);
            }
        });

        imagePeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCircle.setBackground(null);
                imagePeg.setBackground(selected);
                imageHollow.setBackground(null);
                editor.putInt(getString(R.string.CLIMBER_APPEARANCE), CLIMBER_PEG);
            }
        });

        imageHollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCircle.setBackground(null);
                imagePeg.setBackground(null);
                imageHollow.setBackground(selected);
                editor.putInt(getString(R.string.CLIMBER_APPEARANCE), CLIMBER_HOLLOW);
            }
        });

        int climberAppearance = preferences.getInt(getString(R.string.CLIMBER_APPEARANCE), 0);
        switch (climberAppearance){
            case CLIMBER_CIRCLE:
                imageCircle.callOnClick();
                break;
            case CLIMBER_PEG:
                imagePeg.callOnClick();
                break;
            case CLIMBER_HOLLOW:
                imageHollow.callOnClick();
                break;
        }

        doneButton = findViewById(R.id.settingsDoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        editor.commit();
    }

    @Override
    protected void onAccountChanged(){
        if (!shouldSignIn || account == null){
            return;
        }
        AchievementsClient achievementsClient = Games.getAchievementsClient(this, account);
        achievementsClient.unlock(getString(R.string.achievement_tinker));
    }
}
