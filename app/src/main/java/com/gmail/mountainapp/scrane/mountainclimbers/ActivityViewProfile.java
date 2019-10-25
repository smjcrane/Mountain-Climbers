package com.gmail.mountainapp.scrane.mountainclimbers;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.Date;

import static com.gmail.mountainapp.scrane.mountainclimbers.Common.packCompletedAchievementIDs;

public class ActivityViewProfile extends DriveActivity {
    public static final int RC_ACHIEVEMENT_UI = 6;
    public static String FILE_ID = "FILE_ID";

    private TextView achievementText, userInfoText, restoreText, backupText;
    private Button signOutButton, doneButton;
    private AchievementsClient achievementsClient;
    private GamesClient gamesClient;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor preferences;
    String fileID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        sharedPreferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        preferences = sharedPreferences.edit();

        userInfoText = findViewById(R.id.userInfoText);

        doneButton = findViewById(R.id.profileDoneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        achievementText = findViewById(R.id.achievementText);
        achievementText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signedIn == false){
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.sign_in_for_achieve), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (achievementsClient == null) {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.error_connecting_gpg), Toast.LENGTH_SHORT).show();
                    return;
                }
                achievementText.setOnClickListener(new View.OnClickListener() {
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

        backupText = findViewById(R.id.backup);
        backupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signedIn){
                    backUpFromDatabase();
                } else {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.sign_in_for_backup), Toast.LENGTH_SHORT).show();
                }
            }
        });

        restoreText = findViewById(R.id.restore);
        restoreText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signedIn){
                    restoreFromBackup();
                } else {
                    Toast.makeText(ActivityViewProfile.this, getString(R.string.sign_in_for_backup), Toast.LENGTH_SHORT).show();
                }
            }
        });

        signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signOutButton.getText().equals(getString(R.string.sign_in))){
                    requestSignIn();
                } else {
                    signInClient.signOut();
                    signInClient.revokeAccess();
                    signedIn = false;
                    userInfoText.setText(getString(R.string.not_signed_in));
                    signOutButton.setText(getString(R.string.sign_in));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99){
            Log.d("PROFILE", "Permission?" + (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED));
        }
    }

    private void restoreFromBackup(){
        if (mDriveServiceHelper == null){
            Toast.makeText(this, getString(R.string.sign_in_for_backup), Toast.LENGTH_SHORT).show();
        }
        mDriveServiceHelper.queryFiles().addOnCompleteListener(new OnCompleteListener<FileList>() {
            @Override
            public void onComplete(@NonNull Task<FileList> task) {
                if (task.isSuccessful()){
                    FileList files = task.getResult();
                    File file = files.getFiles().get(0);
                    Log.d("PROFILE", "Got a file");
                    fileID = file.getId();
                    mDriveServiceHelper.readFile(fileID, ActivityViewProfile.this).addOnCompleteListener(new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()){
                                Log.d("PROFILE", "saving backup to temp "+task.getResult());
                                DataBaseHandler db = new DataBaseHandler(ActivityViewProfile.this);
                                db.mergeWithBackup(ActivityViewProfile.this);
                            } else {
                                task.getException().printStackTrace();
                            }

                        }
                    });
                } else {
                    task.getException().printStackTrace();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void saveToNewFile(){
        final DataBaseHandler db = new DataBaseHandler(ActivityViewProfile.this);
        mDriveServiceHelper.createFile().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                Log.d("PROFILE", "created");
                preferences.putString(FILE_ID, task.getResult());
                preferences.apply();
                mDriveServiceHelper.saveFile(task.getResult(), "MountainClimbersBackup",
                        db.getBytes(ActivityViewProfile.this)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("PROFILE", "Saved");
                        db.close();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void saveToExistingFile(String fileId){
        final DataBaseHandler db = new DataBaseHandler(ActivityViewProfile.this);
        mDriveServiceHelper.saveFile(fileId, "MountainClimbersBackup",
                db.getBytes(ActivityViewProfile.this)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("PROFILE", "Saved");
                db.close();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void backUpFromDatabase() {
        if (mDriveServiceHelper == null){
            Toast.makeText(this, getString(R.string.sign_in_for_backup), Toast.LENGTH_SHORT).show();
            return;
        }
        final String fileId = sharedPreferences.getString(FILE_ID, null);
        if (fileId == null){
            mDriveServiceHelper.queryFiles().addOnCompleteListener(new OnCompleteListener<FileList>() {
                @Override
                public void onComplete(@NonNull Task<FileList> task) {
                    if (task.isSuccessful()){
                        if (task.getResult().getFiles().size() == 0){
                            saveToNewFile();
                        } else {
                            String newfileId = task.getResult().getFiles().get(0).getId();
                            saveToExistingFile(newfileId);
                        }
                    }
                }
            });

        } else {
            mDriveServiceHelper.queryFiles().addOnCompleteListener(new OnCompleteListener<FileList>() {
                @Override
                public void onComplete(@NonNull Task<FileList> task) {
                    if (task.isSuccessful()){
                        boolean exists = false;
                        for (File f: task.getResult().getFiles()){
                            if (f.getId() == fileId){
                                exists = true;
                            }
                        }
                        if (!exists){
                            saveToNewFile();
                        } else {
                            saveToExistingFile(fileId);
                        }
                    } else {
                        task.getException().printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onSignIn(GoogleSignInAccount account) {
        super.onSignIn(account);
        if (account == null){
            userInfoText.setText(getString(R.string.not_signed_in));
            signOutButton.setText(getString(R.string.sign_in));
            return;
        }
        userInfoText.setText(account.getDisplayName());
        signOutButton.setText(getString(R.string.sign_out));
        gamesClient = Games.getGamesClient(this, account);
        gamesClient.setViewForPopups(findViewById(R.id.container_pop_up));
        signOutButton.setText(getString(R.string.sign_out));
        DataBaseHandler db = new DataBaseHandler(this);
        achievementsClient = Games.getAchievementsClient(this, account);
        achievementsClient.setSteps(getString(R.string.achievement_perfect_10), db.howManyPerfect());
        achievementsClient.setSteps(getString(R.string.achievement_perfect_100), db.howManyPerfect());
        achievementsClient.setSteps(getString(R.string.achievement_quick_10), db.howManyInUnder10Seconds());
        achievementsClient.setSteps(getString(R.string.achievement_quick_100), db.howManyInUnder10Seconds());
        achievementsClient.setSteps(getString(R.string.achievement_learning_the_ropes), db.howManyTutorialCompletedInPack(0));
        for (int i = 0; i < packCompletedAchievementIDs.length; i++){
            achievementsClient.setSteps(getString(packCompletedAchievementIDs[i]), db.howManyCompletedInPack(i));
        }
        if (db.getAchievementProgress(Common.ACHIEVEMENT_CUSTOMISE) > 0){
            achievementsClient.unlock(getString(R.string.achievement_customise));
        }
        Date date = new Date();
        int days = (int) (date.getTime() / (1000 * 24 * 60 * 60));
        achievementsClient.setSteps(getString(R.string.achievement_addicted), db.getDailyStreak(days));
        db.close();
    }

    @Override
    protected void onPause(){
        super.onPause();
        preferences.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_ACHIEVEMENT_UI) {
            Log.d("PROFILE", "Got result from 'Achievement'");
        }
    }
}
