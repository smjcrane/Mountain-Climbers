package com.gmail.mountainapp.scrane.mountainclimbers;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.BadParcelableException;
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
import java.util.Date;
import java.util.Random;

import static com.gmail.mountainapp.scrane.mountainclimbers.Common.packCompletedAchievementIDs;

public class ActivityViewProfile extends SignedInActivity {
    public static final int RC_ACHIEVEMENT_UI = 1;
    public static final int RC_BACKUP = 2;
    public static final int RC_RESTORE = 3;

    public static final int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

    private TextView acheivementText, userInfoText, restoreText, backupText;
    private Button signOutButton, doneButton;
    private AchievementsClient achievementsClient;
    private SharedPreferences.Editor preferences;
    private SnapshotsClient snapshotsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        preferences = sharedPreferences.edit();

        userInfoText = findViewById(R.id.userInfoText);

        doneButton = findViewById(R.id.profileDoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        acheivementText = findViewById(R.id.achievementText);
        acheivementText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (account == null || !shouldSignIn) {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.sign_in_for_achieve), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (achievementsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.error_connecting_gpg), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.sign_in_for_backup), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (snapshotsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.error_connecting_drive), Toast.LENGTH_SHORT).show();
                    return;
                }
                Task<Intent> task = snapshotsClient.getSelectSnapshotIntent(getString(R.string.select_backup), true, true, 10);
                task.addOnCompleteListener(new OnCompleteListener<Intent>() {
                    @Override
                    public void onComplete(@NonNull Task<Intent> task) {
                        Intent intent = task.getResult();
                        if (intent == null){
                            Toast.makeText(ActivityViewProfile.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
                        } else {
                            startActivityForResult(intent, RC_RESTORE);
                        }
                    }
                });
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
                    signInClient.revokeAccess();
                } else {
                    preferences.putBoolean(getString(R.string.SHOULD_SIGN_IN), true);
                    shouldSignIn = true;
                    signInWithActivity();
                }
            }
        });
    }

    private void backUpFromDatabase() {
        DataBaseHandler db = new DataBaseHandler(ActivityViewProfile.this);
        final byte[] bytes = db.getBytes(ActivityViewProfile.this);
        db.close();
        Date date = new Date();
        final String saveName = "Backup-" + date.getTime();
        Task<SnapshotsClient.DataOrConflict<Snapshot>> task = snapshotsClient.open(saveName, true, conflictResolutionPolicy);
        task.addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
            @Override
            public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                if (task.isSuccessful()){
                    Snapshot snapshot = task.getResult().getData();
                    snapshot.getSnapshotContents().writeBytes(bytes);
                    SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                            .setDescription(new Date().toString())
                            .build();
                    snapshotsClient.commitAndClose(snapshot, metadataChange);
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.backup_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.backup_fail), Toast.LENGTH_SHORT).show();
                    Log.d("PROFILE", task.getException() == null ? "null" : "except " + task.getException().toString());
                }

            }
        });
    }

    @Override
    protected void onAccountChanged(){
        if (account == null){
            userInfoText.setText(getString(R.string.not_signed_in));
            signOutButton.setText(getString(R.string.sign_in));
            return;
        }
        userInfoText.setText(account.getDisplayName());
        signOutButton.setText(getString(R.string.sign_out));
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
        if (requestCode == RC_RESTORE && resultCode == RESULT_OK) {
            Log.d("PROFILE", "Got result from 'Restore'");
            try {
                if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                    // Load a snapshot.
                    SnapshotMetadata snapshotMetadata =
                            intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA);
                    // Load the game data from the Snapshot
                    snapshotsClient.open(snapshotMetadata).addOnCompleteListener(new OnCompleteListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                        @Override
                        public void onComplete(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) {
                            if (task.isSuccessful()) {
                                SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
                                if (result.isConflict()) {
                                    Toast.makeText(ActivityViewProfile.this, getString(R.string.error_getting_backup), Toast.LENGTH_SHORT).show();
                                } else {
                                    try {
                                        byte[] bytes = result.getData().getSnapshotContents().readFully();
                                        DataBaseHandler db = new DataBaseHandler(ActivityViewProfile.this);
                                        db.mergeWithBytes(ActivityViewProfile.this, bytes);
                                        db.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(ActivityViewProfile.this, getString(R.string.error_reading_backup), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(ActivityViewProfile.this, getString(R.string.error_getting_backup), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                    backUpFromDatabase();
                }
            } catch (BadParcelableException e){
                e.printStackTrace();
                Toast.makeText(ActivityViewProfile.this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == RC_BACKUP){
            Log.d("PROFILE", "Got result from 'Backup'");
        } else if (requestCode == RC_ACHIEVEMENT_UI) {
            Log.d("PROFILE", "Got result from 'Achievement'");
        }
    }
}
