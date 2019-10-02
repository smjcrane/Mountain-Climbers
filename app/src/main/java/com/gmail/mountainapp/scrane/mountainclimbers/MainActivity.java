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

public class MainActivity extends AppCompatActivity {

    private static int RC_SIGN_IN = 0;

    private Button playButton, levelSelectButton, timedButton, puzzleButton;
    private ImageView settingsButton;
    private GoogleSignInClient signInClient;
    private GoogleSignInAccount account;
    private GoogleSignInOptions signInOptions;
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

        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);
        account = GoogleSignIn.getLastSignedInAccount(this);

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

    private void signInSilently() {
        if (account !=null && account.getDisplayName() != null && GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            Log.d("MAIN", "Signed in as " + account.getDisplayName());
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            Task<GoogleSignInAccount> task = signInClient.silentSignIn();
            if (task.isSuccessful()){
                Log.d("MAIN", "task success");
                Log.d("MAIN", "" + task.getResult().getDisplayName());
            } else {
                task.addOnCompleteListener(
                        this,
                        new OnCompleteListener<GoogleSignInAccount>() {
                            @Override
                            public void onComplete( Task<GoogleSignInAccount> task) {
                                if (task.isSuccessful()) {
                                    // The signed in account is stored in the task's result.
                                    account = task.getResult();
                                    if (account!=null){
                                        Log.d("MAIN", "Silently signed in with name "+ account.getDisplayName());
                                    }
                                } else {
                                    Log.d("MAIN", "Silent sign in failed, trying with activity");
                                    Intent intent = signInClient.getSignInIntent();
                                    startActivityForResult(intent, RC_SIGN_IN);
                                }
                            }
                        });
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Log.d("MAIN", "Sign in was successful");
                // The signed in account is stored in the result.
                account = result.getSignInAccount();
                if (account!=null){
                    Log.d("MAIN", "Signed in with activity with name " + account.getDisplayName());
                }
            } else {
                Log.d("MAIN", "Sign in unsuccessful " + (result.getStatus().getStatusCode()) + " " + result.getStatus().getStatusMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //signInSilently();
        if (account == null){
            Log.d("MAIN", "account is null");
        } else {
            userNameText.setText(account.getDisplayName());
            if (account.getPhotoUrl() == null){
                userProfilePicture.setImageDrawable(getDrawable(R.drawable.nobody));
            } else {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), account.getPhotoUrl());
                    userProfilePicture.setImageBitmap(bitmap);
                } catch (IOException e){
                    Log.d("MAIN", "Idk");
                    e.printStackTrace();
                    userProfilePicture.setImageDrawable(getDrawable(R.drawable.nobody));
                }
            }

        }
    }
}
