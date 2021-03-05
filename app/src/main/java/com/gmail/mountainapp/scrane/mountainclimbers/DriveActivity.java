package com.gmail.mountainapp.scrane.mountainclimbers;

/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;


public abstract class DriveActivity extends AppCompatActivity {
    private static final String TAG = "DRIVE";

    private static final int REQUEST_CODE_SIGN_IN = 1;

    protected DriveServiceHelper mDriveServiceHelper;
    protected GoogleSignInClient signInClient;
    protected GamesClient gamesClient;

    protected boolean signedIn;
    protected boolean waitingForResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        signedIn = false;
        waitingForResult = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE).getBoolean(getString(R.string.SHOULD_SIGN_IN), false) && !signedIn){
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                            .requestScopes(new Scope(Scopes.GAMES_LITE))
                            .build();
            signInClient = GoogleSignIn.getClient(this, signInOptions);

            Task<GoogleSignInAccount> signIn = signInClient.silentSignIn();
            if (signIn.isSuccessful()) {
                // There's immediate result available.
                GoogleSignInAccount signInAccount = signIn.getResult();
                onSignIn(signInAccount);
            } else {
                onSignIn(null);
            }
            //requestSignIn();
        } else {
            onSignIn(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                } else {
                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.SHOULD_SIGN_IN), false);
                    editor.apply();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    void requestSignIn() {
        if (waitingForResult){
            return;
        }
        Log.d(TAG, "Requesting sign-in");
        waitingForResult = true;

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestScopes(new Scope(Scopes.GAMES_LITE))
                        .build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(signInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private Task<GoogleSignInAccount> handleSignInResult(Intent result) {
        Log.d("DRIVE", "Got a sign in result");
        return GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleAccount) {
                        onSignIn(googleAccount);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to sign in.", e);
                        signedIn = false;
                    }
                });

    }

    protected void onSignIn(GoogleSignInAccount googleAccount){
        waitingForResult = false;
        if (googleAccount == null) {
            Log.d(TAG, "Signed out");
            signedIn = false;
            return;
        }
        Log.d(TAG, "Signed in as " + googleAccount.getEmail());
        signedIn = true;

        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        DriveActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(googleAccount.getAccount());
        Drive googleDriveService =
                new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(getString(R.string.app_name))
                        .build();

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
    }
}