package com.gmail.mountainapp.scrane.mountainclimbers;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

public class ActivityViewProfile extends SignedInActivity {
    public static final int RC_ACHIEVEMENT_UI = 1;
    public static final int RC_BACKUP = 2;
    public static final int RC_RESTORE = 3;

    public static int[] packCompletedAchievementIDs = new int[] {
            R.string.achievement_getting_started,
            R.string.achievement_teamwork,
            R.string.achievement_this_is_easy,
            R.string.achievement_hard_worker,
            R.string.achievement_01101001,
            R.string.achievement_awoooo,
            R.string.achievement_the_big_one
    };

    private TextView acheivementText, userInfoText, restoreText, backupText;
    private Button signOutButton;
    private AchievementsClient achievementsClient;
    private SharedPreferences.Editor preferences;
    private SnapshotsClient snapshotsClient;

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
                if (achievementsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, "Could not connect to Google Play Games", Toast.LENGTH_LONG).show();
                    return;
                }
                acheivementText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        achievementsClient.getAchievementsIntent()
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

        restoreText = findViewById(R.id.restoreBackup);
        restoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account == null || !shouldSignIn) {
                    Toast.makeText(ActivityViewProfile.this, "You must sign in save your progress", Toast.LENGTH_LONG).show();
                    return;
                }
                if (snapshotsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, "Could not connect to Google Drive", Toast.LENGTH_LONG).show();
                    return;
                }
                Task<Intent> task = snapshotsClient.getSelectSnapshotIntent("Select backup", true, true, 10);
                task.addOnCompleteListener(new OnCompleteListener<Intent>() {
                    @Override
                    public void onComplete(@NonNull Task<Intent> task) {
                        Intent intent = task.getResult();
                        if (intent == null){
                            Toast.makeText(ActivityViewProfile.this, "An error occurred", Toast.LENGTH_LONG).show();
                        } else {
                            startActivityForResult(intent, RC_RESTORE);
                        }
                    }
                });
            }
        });

        backupText = findViewById(R.id.backUpText);
        backupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account == null || !shouldSignIn) {
                    Toast.makeText(ActivityViewProfile.this, "You must sign in save your progress", Toast.LENGTH_LONG).show();
                    return;
                }
                if (snapshotsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, "Could not connect to Google Drive", Toast.LENGTH_LONG).show();
                    return;
                } else {

                }
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

    private Task<SnapshotMetadata> writeSnapshot(Snapshot snapshot,
                                                 byte[] data, Bitmap coverImage, String desc) {

        // Set the data payload for the snapshot
        snapshot.getSnapshotContents().writeBytes(data);

        // Create the change operation
        SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                .setCoverImage(coverImage)
                .setDescription(desc)
                .build();

        SnapshotsClient snapshotsClient =
                Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));

        // Commit the operation
        return snapshotsClient.commitAndClose(snapshot, metadataChange);
    }

    Task<byte[]> loadSnapshot() {
        // Display a progress dialog
        // ...

        // Get the SnapshotsClient from the signed in account.
        SnapshotsClient snapshotsClient =
                Games.getSnapshotsClient(this, GoogleSignIn.getLastSignedInAccount(this));

        // In the case of a conflict, the most recently modified version of this snapshot will be used.
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        // Open the saved game using its name.
        return snapshotsClient.open(sharedPreferences.getString(getString(R.string.SAVED_GAME_ID), "temp"), true, conflictResolutionPolicy)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("PROFILE", "Error while opening Snapshot.", e);
                    }
                }).continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, byte[]>() {
                    @Override
                    public byte[] then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {
                        Snapshot snapshot = task.getResult().getData();

                        // Opening the snapshot was a success and any conflicts have been resolved.
                        try {
                            // Extract the raw data from the snapshot.
                            return snapshot.getSnapshotContents().readFully();
                        } catch (IOException e) {
                            Log.e("PROFILE", "Error while reading Snapshot.", e);
                        }

                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        // Dismiss progress dialog and reflect the changes in the UI when complete.
                        // ...
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
        achievementsClient = Games.getAchievementsClient(this, account);
        achievementsClient.setSteps(getString(R.string.achievement_unstoppable), db.howManyCompleted());
        achievementsClient.setSteps(getString(R.string.achievement_perfect_score), db.howManyPerfect());
        achievementsClient.setSteps(getString(R.string.achievement_speed_demon), db.howManyInUnder10Seconds());
        achievementsClient.setSteps(getString(R.string.achievement_learn_the_ropes), db.howManyTutorialCompletedInPack(0));
        for (int i = 0; i < packCompletedAchievementIDs.length; i++){
            achievementsClient.setSteps(getString(packCompletedAchievementIDs[i]), db.howManyCompletedInPack(i));
        }
        achievementsClient.setSteps(getString(R.string.achievement_master), db.howManyLevelsCompleted());
        db.close();
        snapshotsClient = Games.getSnapshotsClient(this, account);
    }

    @Override
    protected void onPause(){
        super.onPause();
        preferences.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (intent == null){
            return;
        }
        if (requestCode == RC_RESTORE) {
            Log.d("PROFILE", "Got result from 'Restore'");
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                // Load a snapshot.
                SnapshotMetadata snapshotMetadata =
                        intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                String saveName = snapshotMetadata.getUniqueName();
                preferences.putString(getString(R.string.SAVED_GAME_ID), saveName);
                // Load the game data from the Snapshot
                // ...
            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                // Create a new snapshot named with a unique string
                String unique = new BigInteger(281, new Random()).toString(13);
                String saveName = "snapshotTemp-" + unique;
                preferences.putString(getString(R.string.SAVED_GAME_ID), saveName);
                // Create the new snapshot
                // ...
            }
        } else if (requestCode == RC_BACKUP){
            Log.d("PROFILE", "Got result from 'Backup'");
        } else if (requestCode == RC_ACHIEVEMENT_UI) {
            Log.d("PROFILE", "Got result from 'Achievement'");
        }
    }
}
