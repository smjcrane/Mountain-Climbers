package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public abstract class SignedInActivity extends AppCompatActivity {

    public static int RC_SIGN_IN = 0;

    protected GoogleSignInClient signInClient;
    protected GoogleSignInAccount account;
    private GoogleSignInOptions signInOptions;
    protected boolean shouldSignIn;
    protected SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(getString(R.string.PREFERENCES), MODE_PRIVATE);
        shouldSignIn = sharedPreferences.getBoolean(getString(R.string.SHOULD_SIGN_IN), false);

        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        signInClient = GoogleSignIn.getClient(this, signInOptions);
    }

    protected void signInSilently() {
        if (account !=null && account.getDisplayName() != null && GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            onAccountChanged();
        } else {
            Task<GoogleSignInAccount> task = signInClient.silentSignIn();
            if (task.isSuccessful()){
                account = task.getResult();
                onAccountChanged();
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
                                        onAccountChanged();
                                    }
                                } else {
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
                account = result.getSignInAccount();
                onAccountChanged();
            } else {
                shouldSignIn = false;
                Log.d("SIGN-IN", "Sign in unsuccessful " + (result.getStatus().getStatusCode()) + " " + result.getStatus().getStatusMessage());
            }
        }
    }

    protected void onAccountChanged(){return;};

    @Override
    protected void onResume() {
        super.onResume();
        shouldSignIn = sharedPreferences.getBoolean(getString(R.string.SHOULD_SIGN_IN), false);
        if (shouldSignIn ){
            signInSilently();
        } else {
            account = null;
            onAccountChanged();
        }
    }
}
