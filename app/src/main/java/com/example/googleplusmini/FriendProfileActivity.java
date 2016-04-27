package com.example.googleplusmini;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class FriendProfileActivity extends ActionBarActivity {

    private SharedPreferences mPrefs;

    private String circleName;
    private String personName;
    private String personOccupation;
    private String personAboutme;
    private String personOrganization;
    private String personPictureUrl;

    private ImageView propic;
    private Bitmap bitmap;
    private TextView displayName;
    private TextView occupation;
    private TextView aboutMe;
    private TextView organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        mPrefs = this.getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);

        circleName=getIntent().getStringExtra("circleName");
        personName=getIntent().getStringExtra("personName");
        personAboutme=getIntent().getStringExtra("personAboutme");
        personPictureUrl=getIntent().getStringExtra("profilePicUrl");
        personPictureUrl=personPictureUrl.substring(0, personPictureUrl.length() - 2) + "1000";
        personOccupation=getIntent().getStringExtra("personOccupation");
        personOrganization=getIntent().getStringExtra("personOrganization");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(circleName);

        propic=(ImageView)findViewById(R.id.propic_image_view);
        displayName=(TextView)findViewById(R.id.name_text_view);
        occupation=(TextView)findViewById(R.id.occupation_text_view);
        aboutMe=(TextView)findViewById(R.id.aboutme_text_view);
        organization=(TextView)findViewById(R.id.organization_text_view);

        displayName.setText(personName);
        occupation.setText(personOccupation);
        aboutMe.setText(personAboutme);
        organization.setText(personOrganization);
        new LoadImageAsyncTask(this, propic).execute(personPictureUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_friend_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(item.getItemId()) {
            case R.id.gmail:
                Intent email = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "", null));
                try {
                    startActivity(Intent.createChooser(email, "Send mail..."));
                    Log.i("Finished sending email...", "");
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(this,
                            "There is no email client installed.", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_item_sign_out:
                mPrefs.edit().remove(LoginActivity.ACCESS_TOKEN).apply();
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
