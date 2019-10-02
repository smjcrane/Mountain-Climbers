package com.gmail.mountainapp.scrane.mountainclimbers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class ActivityViewProfile extends SignedInActivity {
    public static final int RC_ACHIEVEMENT_UI = 1;
    public static int[] packCompletedAchievementIDs = new int[] {
            R.string.achievement_getting_started,
            R.string.achievement_teamwork,
            R.string.achievement_this_is_easy,
            R.string.achievement_hard_worker,
            R.string.achievement_01101001,
            R.string.achievement_awoooo,
            R.string.achievement_the_big_one
    };

    private TextView acheivementText, userInfoText, backupText;
    private Button signOutButton;
    private AchievementsClient client;
    private SharedPreferences.Editor preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        preferences = sharedPreferences.edit();

        userInfoText = findViewById(R.id.userInfo);

        acheivementText = findViewById(R.id.achievementText);
        acheivementText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account == null || !shouldSignIn) {
                    Toast.makeText(ActivityViewProfile.this, "You must sign in to view acheivements", Toast.LENGTH_LONG).show();
                    return;
                }
                if (client == null) {
                    Toast.makeText(ActivityViewProfile.this, "Could not connect to Google Play Games", Toast.LENGTH_LONG).show();
                    return;
                }
                acheivementText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        client.getAchievementsIntent()
                                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                                    @Override
                                    public void onSuccess(Intent intent) {
                                        startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                                    }
                                });
                    }
                });
            }
        });

        backupText = findViewById(R.id.backUpText);
        backupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ActivityViewProfile.this,
                        "Sorry this feature has not been implemented yet", Toast.LENGTH_LONG).show();
            }
        });

        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SIGN", "should i " + shouldSignIn);
                if (shouldSignIn){
                    shouldSignIn = false;
                    account = null;
                    onAccountChanged();
                    preferences.putBoolean(getString(R.string.SHOULD_SIGN_IN), false);
                    signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            account = null;
                            Log.d("PROFILE", "Signed out");
                        }
                    });
                } else {
                    preferences.putBoolean(getString(R.string.SHOULD_SIGN_IN), true);
                    shouldSignIn = true;
                    signInWithActivity();
                }
            }
        });
    }

    @Override
    protected void onAccountChanged(){
        if (account == null){
            userInfoText.setText("You are not signed in");
            signOutButton.setText("Sign in");
            return;
        }
        userInfoText.setText(account.getDisplayName());
        signOutButton.setText("Sign out");
        DataBaseHandler db = new DataBaseHandler(this);
        client = Games.getAchievementsClient(this, account);
        client.setSteps(getString(R.string.achievement_unstoppable), db.howManyCompleted());
        client.setSteps(getString(R.string.achievement_perfect_score), db.howManyPerfect());
        client.setSteps(getString(R.string.achievement_speed_demon), db.howManyInUnder10Seconds());
        client.setSteps(getString(R.string.achievement_learn_the_ropes), db.howManyTutorialCompletedInPack(0));
        for (int i = 0; i < packCompletedAchievementIDs.length; i++){
            client.setSteps(getString(packCompletedAchievementIDs[i]), db.howManyCompletedInPack(i));
        }
        client.setSteps(getString(R.string.achievement_master), db.howManyLevelsCompleted());
        db.close();
        //TODO: the secret settings achievement
    }

    @Override
    protected void onPause(){
        super.onPause();
        preferences.commit();
    }
}
