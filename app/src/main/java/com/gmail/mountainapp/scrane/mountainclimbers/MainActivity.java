package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

    private static int RC_SIGN_IN = 0;

    private Button playButton, levelSelectButton, timedButton, puzzleButton;
    private ImageView settingsButton;
    private TextView userNameText;
    private ImageView userProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.mainPlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.MODE = Common.MODE_DEFAULT;

                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);

                Intent selectLevel = new Intent();
                selectLevel.setClass(MainActivity.this, LevelSelectActivity.class);
                Intent playGame = new Intent();
                playGame.setClass(MainActivity.this, PlayGameActivity.class);

                int length = Levels.packs[Common.PACK_POS].getLength();

                DataBaseHandler db = new DataBaseHandler(MainActivity.this);
                while (db.isCompleted(db.getId(Common.PACK_POS, Common.LEVEL_POS)) && Common.PACK_POS < Levels.packs.length - 1) {
                    if (Common.LEVEL_POS == length - 1) {
                        Common.PACK_POS++;
                        length = Levels.packs[Common.PACK_POS].getLength();
                        Common.LEVEL_POS = 0;
                    } else {
                        Common.LEVEL_POS++;
                    }
                }

                startActivity(selectPack);
                startActivity(selectLevel);
                startActivity(playGame);
            }
        });

        levelSelectButton = findViewById(R.id.mainLevelSelectButton);
        levelSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_DEFAULT;
                startActivity(selectPack);
            }
        });

        timedButton = findViewById(R.id.mainTimedModeButton);
        timedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_TIMED;
                startActivity(selectPack);
            }
        });

        puzzleButton = findViewById(R.id.mainPuzzleModeButton);
        puzzleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectPack = new Intent();
                selectPack.setClass(MainActivity.this, PackSelectActivity.class);
                Common.MODE = Common.MODE_PUZZLE;
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
            userNameText.setText("Sign in");
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
