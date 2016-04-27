package com.example.googleplusmini;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Person;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ProfileFragment extends Fragment {
    private SharedPreferences mPrefs;
    private String mAccessToken;
    private PlusDomains mPlusDomains;
    private com.google.api.services.plusDomains.model.Person mPerson;

    private ImageView mProfilePicImageView;
    private TextView mNameTextView;
    private TextView mOccupationTextView;
    private TextView mOrganizationTextView;
    private TextView mAboutMeTextView;

    private String mProfilePicURL;
    private String mName;
    private String mOccupation;
    private String mOrganization;
    private String mAboutMe;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getActivity().getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);
        mAccessToken = mPrefs.getString(LoginActivity.ACCESS_TOKEN, null);

        new GetProfileTask(getActivity()).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile, container, false);

        mProfilePicImageView = (ImageView) view.findViewById(R.id.propic_image_view);
        mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
        mOccupationTextView = (TextView) view.findViewById(R.id.occupation_text_view);
        mOrganizationTextView = (TextView) view.findViewById(R.id.organization_text_view);
        mAboutMeTextView = (TextView) view.findViewById(R.id.aboutme_text_view);

        return view;
    }


    public class GetProfileTask extends AsyncTask<Void, Void, com.google.api.services.plusDomains.model.Person> {
        Activity mActivity;

        GetProfileTask(Activity activity) {
            this.mActivity = activity;
        }

        @Override
        protected com.google.api.services.plusDomains.model.Person doInBackground(Void... args) {
            try {
                GoogleCredential credential = new GoogleCredential().setAccessToken(mAccessToken);
                credential.refreshToken();
                mPlusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(),
                        credential).build();
                mPerson = mPlusDomains.people().get("me").execute();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
            return mPerson;
        }

        @Override
        protected void onPostExecute(com.google.api.services.plusDomains.model.Person person) {
            if(person != null) {
                mProfilePicURL = mPerson.getImage().getUrl();
                mProfilePicURL = mProfilePicURL.substring(0, mProfilePicURL.length() - 2) + "1000";
                mName = formatName(mPerson.getName());
                mOccupation = mPerson.getOccupation();
                mOrganization = formatOrganization(mPerson.getOrganizations());
                mAboutMe = mPerson.getAboutMe();

                new LoadImageAsyncTask(getActivity(), mProfilePicImageView).execute(mProfilePicURL);
                mNameTextView.setText(mName);
                mOccupationTextView.setText(mOccupation);
                mOrganizationTextView.setText(mOrganization);
                mAboutMeTextView.setText(mAboutMe);
            } else {
                Toast.makeText(getActivity(), R.string.invalid_account, Toast.LENGTH_SHORT).show();
                mPrefs.edit().remove(LoginActivity.ACCESS_TOKEN).apply();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        }

        private String formatName(com.google.api.services.plusDomains.model.Person.Name name) {
            String formattedName = "";
            if(name.getHonorificPrefix() != null) {
                formattedName += name.getHonorificPrefix();
            }
            if(name.getGivenName() != null) {
                formattedName += " " + name.getGivenName();
            }
            if(name.getMiddleName() != null) {
                formattedName += " " + name.getMiddleName();
            }
            if(name.getFamilyName() != null) {
                formattedName += " " + name.getFamilyName();
            }
            if(name.getHonorificSuffix() != null) {
                formattedName += " " + name.getHonorificSuffix();
            }
            return formattedName;
        }

        private String formatOrganization(List<Person.Organizations> orgs) {
            String org = "";
            if(orgs != null) {
                org = orgs.get(0).getName();
            }
            return org;
        }
    }
}