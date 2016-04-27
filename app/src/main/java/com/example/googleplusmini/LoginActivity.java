package com.example.googleplusmini;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;

public class LoginActivity extends FragmentActivity {

    public static final String ACCESS_TOKEN = "access_token";

    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private final static String ME_SCOPE
            = "https://www.googleapis.com/auth/plus.me";
    private final static String PROFILE_READ
            = "https://www.googleapis.com/auth/plus.profiles.read";
    private final static String CIRCLES_READ_SCOPE
            = "https://www.googleapis.com/auth/plus.circles.read";
    private final static String mScopes
            = "oauth2:" + ME_SCOPE + " " + PROFILE_READ + " " + CIRCLES_READ_SCOPE;

    private SharedPreferences mPrefs;
    private String mEmail;
    private String mAccessToken;

    private Button mSignInButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = this.getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);
        mAccessToken = mPrefs.getString(ACCESS_TOKEN, null);
        if(mAccessToken != null) {
            Intent i = new Intent(LoginActivity.this, PagerActivity.class);
            startActivity(i);
            finish();
        }

        setContentView(R.layout.activity_login);
        getActionBar().hide();

        mSignInButton = (Button)findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserAccount();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.choose_account_toast, Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            getUsername();
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                new GetUsernameTask(LoginActivity.this, mEmail, mScopes).execute();
            } else {
                Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void handleException(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            LoginActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }

    public class GetUsernameTask extends AsyncTask<Void, Void, Void> {
        Activity mActivity;
        String mScope;
        String mEmail;

        GetUsernameTask(Activity activity, String name, String scope) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = name;
        }

        @Override
        protected Void doInBackground(Void... args) {
            try {
                mAccessToken = fetchToken();
                if (mAccessToken != null) {
                    mPrefs.edit().putString(ACCESS_TOKEN, mAccessToken).apply();
                    Intent i = new Intent(LoginActivity.this, PagerActivity.class);
                    startActivity(i);
                    finish();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            return null;
        }

        protected String fetchToken() throws IOException {
            try {
                return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                handleException(userRecoverableException);
            } catch (GoogleAuthException fatalException) {
                System.out.println(fatalException.getMessage());
            }
            return null;
        }
    }

}