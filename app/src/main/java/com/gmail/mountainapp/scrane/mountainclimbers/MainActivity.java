package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends SignedInActivity {

    private Button playButton, levelSelectButton, timedButton, puzzleButton;
    private ImageView settingsButton;
    private TextView userNameText;
    private ImageView userProfilePicture;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        editor = preferences.edit();

        playButton = findViewById(R.id.mainPlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt(getString(R.string.MODE), Common.MODE_DEFAULT);

                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);

                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                Intent playGame = new Intent();

                int packPos = 0;
                int levelPos = 0;
                Levels.Pack pack;

                DataBaseHandler db = new DataBaseHandler(MainActivity.this);
                boolean goToTutorial = true;
                boolean stop = false;
                while (!stop) {
                    pack = Levels.packs[packPos];
                    if (goToTutorial && pack.getNumTutorials() > 0){
                        levelPos = 0;
                        while (db.isCompletedTutorial(db.getId(packPos, levelPos)) && levelPos < pack.getNumTutorials()){
                            levelPos++;
                        }
                        if (levelPos == pack.getNumTutorials()){
                            goToTutorial = false;
                            levelPos = 0;
                        } else {
                            stop = true;
                        }
                    } else if (goToTutorial && pack.getNumTutorials() == 0) {
                        goToTutorial = false;
                    } else {
                        levelPos = 0;
                        while(db.isCompleted(db.getId(packPos, levelPos)) && levelPos < pack.getLength()){
                            levelPos++;
                        }
                        if (levelPos == pack.getLength()){
                            goToTutorial = true;
                            levelPos = 0;
                            packPos++;
                        } else {
                            stop = true;
                        }
                    }
                    if (packPos == Levels.packs.length){
                        stop = true;
                        levelPos = 0;
                        packPos = 0;
                    }
                }
                editor.putInt(getString(R.string.LEVELPOS), levelPos);
                editor.putInt(getString(R.string.PACKPOS), packPos);
                startActivity(selectPack);
                startActivity(selectLevel);
                if (goToTutorial){
                    playGame.setClass(MainActivity.this, TutorialActivity.class);
                } else {
                    playGame.setClass(MainActivity.this, PlayGameActivity.class);
                }
                editor.apply();
                startActivity(playGame);
            }
        });

        levelSelectButton = findViewById(R.id.mainLevelSelectButton);
        levelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                editor.putInt(getString(R.string.MODE), Common.MODE_DEFAULT);
                editor.apply();
                startActivity(selectPack);
            }
        });

        timedButton = findViewById(R.id.mainTimedModeButton);
        timedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                editor.putInt(getString(R.string.MODE), Common.MODE_TIMED);
                editor.apply();
                startActivity(selectPack);
            }
        });

        puzzleButton = findViewById(R.id.mainPuzzleModeButton);
        puzzleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                editor.putInt(getString(R.string.MODE), Common.MODE_PUZZLE);
                editor.apply();
                startActivity(selectPack);
            }
        });

        settingsButton = findViewById(R.id.mainSettingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settings = new Intent();
                settings.setClass(MainActivity.this, SettingsActivity.class);
                startActivity(settings);
            }
        });

        userNameText = findViewById(R.id.userName);
        userProfilePicture = findViewById(R.id.userProfilePicture);

        userProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewProfile = new Intent();
                viewProfile.setClass(MainActivity.this, ActivityViewProfile.class);
                startActivity(viewProfile);
            }
        });
    }

    @Override
    protected void onAccountChanged(){
        if (account == null){
            userNameText.setText(getString(R.string.sign_in));
            userProfilePicture.setImageDrawable(getDrawable(R.drawable.nobody));
            return;
        }
        userNameText.setText(account.getGivenName());
        if (account.getPhotoUrl() == null){
            userProfilePicture.setImageDrawable(getDrawable(R.drawable.nobody));
        } else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), account.getPhotoUrl());
                userProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e){
                Log.d("MAIN", "Couldn't retrieve profile image from URI");
                e.printStackTrace();
                userProfilePicture.setImageDrawable(getDrawable(R.drawable.nobody));
            }
        }
    }
}
