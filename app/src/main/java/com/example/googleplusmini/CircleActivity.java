package com.example.googleplusmini;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.PeopleFeed;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class CircleActivity extends ActionBarActivity {

    Bitmap bitmap;

    PlusDomains mPlusDomains;
    PlusDomains mPlusDomainsPeople;
    String mAccessToken;

    SharedPreferences mPrefs;

    public static final String EXTRA_ACCESS_TOKEN = "access_token";

    private final static String ME_SCOPE
            = "https://www.googleapis.com/auth/plus.me";
    private final static String PROFILE_READ
            = "https://www.googleapis.com/auth/plus.profiles.read";
    private final static String CIRCLES_READ_SCOPE
            = "https://www.googleapis.com/auth/plus.circles.read";
    private final static String mScopes
            = "oauth2:" + ME_SCOPE + " " + PROFILE_READ + " " + CIRCLES_READ_SCOPE;

    private String circleId;
    private String circleName;
    private ListView circleListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPrefs = this.getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);
        mAccessToken = mPrefs.getString(EXTRA_ACCESS_TOKEN, null);

        Intent i = getIntent();
        circleId = i.getStringExtra(CircleListFragment.CIRCLE_ID_EXTRA);
        circleName = i.getStringExtra(CircleListFragment.CIRCLE_NAME_EXTRA);
        getSupportActionBar().setTitle(circleName);

        circleListView = (ListView)findViewById(R.id.nameList);
        if(mAccessToken!=null) {
            new GetFriendProfileTask(this).execute();
        }
    }

    private class CustomArrayAdapter extends ArrayAdapter<String> {
        ArrayList<String> titleNames;
        ArrayList<String> singleRowImageUrls;
        Context context;

        public CustomArrayAdapter(Context c, ArrayList<String> displayNames,
                                  ArrayList<String> imageRowUrls) {
            super(c, R.layout.single_row,R.id.nameInSingleRow,displayNames);
            this.context=c;
            this.singleRowImageUrls=imageRowUrls;
            this.titleNames=displayNames;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater= (LayoutInflater) context.getSystemService(
                    context.LAYOUT_INFLATER_SERVICE);
            View row=inflater.inflate(R.layout.single_row,parent,false);

            TextView myDisplayName= (TextView) row.findViewById(R.id.nameInSingleRow);
            myDisplayName.setText(titleNames.get(position));
            MyTask asynctask=new MyTask(row);
            asynctask.execute(singleRowImageUrls.get(position).substring(0,singleRowImageUrls.get(position).length()-2)+"200");
            return row;
        }
    }


    public class MyTask extends AsyncTask<Object,String ,Bitmap>{
        View rowInMyTask;

        public  MyTask(View row) {
            this.rowInMyTask=row;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(Object... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL((String) args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                ImageView myImage= (ImageView) rowInMyTask.findViewById(R.id.imageViewinSingleRow);
                myImage.setImageBitmap(image);
            } else {
                Toast.makeText(CircleActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GetFriendProfileTask extends AsyncTask<Void, Void, ArrayList<Person>> {
        Activity mActivity;
        PlusDomains.People.ListByCircle listPeople;
        PeopleFeed peopleFeed;

        GetFriendProfileTask(Activity activity) {
            this.mActivity = activity;
        }

        @Override
        protected ArrayList<Person> doInBackground(Void... args) {
            try {
                GoogleCredential credential = new GoogleCredential().setAccessToken(mAccessToken);
                credential.refreshToken();
                mPlusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(),
                        credential).build();
                listPeople = mPlusDomains.people().listByCircle(circleId);
                listPeople.setMaxResults(100L);
                peopleFeed = listPeople.execute();
                System.out.println(peopleFeed.getItems().size());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }

            try {
                if(peopleFeed.getItems().size() > 0) {
                    ArrayList<Person> people = new ArrayList<Person>();
                    for(Person p : peopleFeed.getItems()) {
                        String id = p.getId();
                        Person mPerson = mPlusDomains.people().get(id).execute();
                        people.add(mPerson);
                    }
                    return people;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<Person>();
        }

        @Override
        protected void onPostExecute(ArrayList<Person> people) {
            PlusDomains mPlusDomainsPeople;
            if (people.size() > 0) {
                final ArrayList<String> personNames = new ArrayList<String>();
                final ArrayList<String> personOccupation = new ArrayList<String>();
                final ArrayList<String> personAboutme = new ArrayList<String>();
                final ArrayList<String> personPicture = new ArrayList<String>();
                final ArrayList<String> personOrganization = new ArrayList<String>();
                for(Person p :people) {
                    if(p.getDisplayName() != null) {
                        personNames.add(p.getDisplayName());
                    } else {
                        personNames.add("No information available");
                    }
                    if(p.getOccupation() != null) {
                        personOccupation.add(p.getOccupation());
                    } else {
                        personOccupation.add("No information available");
                    }
                    if(p.getAboutMe() != null) {
                        personAboutme.add(p.getAboutMe());
                    } else {
                        personAboutme.add("No information available");
                    }
                    if(p.getOrganizations() != null) {
                        personOrganization.add(p.getOrganizations().get(0).getName());
                    } else {
                        personOrganization.add("No information available");
                    }
                    if(p.getImage() != null) {
                        personPicture.add(p.getImage().getUrl());
                    } else {
                        personPicture.add("No photo available");
                    }
                }

                CustomArrayAdapter adapter = new CustomArrayAdapter(CircleActivity.this, personNames,
                        personPicture);
                circleListView.setAdapter(adapter);
                circleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(CircleActivity.this,FriendProfileActivity.class);
                        i.putExtra("circleName", circleName);
                        i.putExtra("profilePicUrl", personPicture.get(position));
                        i.putExtra("personOccupation",personOccupation.get(position));
                        i.putExtra("personName",personNames.get(position));
                        i.putExtra("personAboutme",personAboutme.get(position));
                        i.putExtra("personOrganization",personOrganization.get(position));
                        i.putExtra("access",mAccessToken);
                        startActivity(i);
                    }
                });
            } else
                System.out.println("unable to get names");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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
