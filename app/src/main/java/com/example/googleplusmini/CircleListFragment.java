package com.example.googleplusmini;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.CircleFeed;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class CircleListFragment extends Fragment {
    public static final String CIRCLE_ID_EXTRA = "circle_id";
    public static final String CIRCLE_NAME_EXTRA = "circle_name";

    private String mAccessToken;
    private SharedPreferences mPrefs;
    private PlusDomains mPlusDomains;

    public static List<Circle> mCircles;
    public static  ArrayList<String> circleIds = new ArrayList<String>();

    ListView CircleList;

    public CircleListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_circle_list, container, false);

        CircleList = (ListView)view.findViewById(R.id.circlelist);

        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);
        mAccessToken = mPrefs.getString(LoginActivity.ACCESS_TOKEN, null);

        new GetCirclesTask(getActivity()).execute();
    }

    public class GetCirclesTask extends AsyncTask<Void, Void, List<Circle>> {
        Activity mActivity;

        GetCirclesTask(Activity activity) {
            this.mActivity = activity;
        }

        @Override
        protected List<Circle> doInBackground(Void... args) {
            try {
                GoogleCredential credential = new GoogleCredential().setAccessToken(mAccessToken);
                credential.refreshToken();
                mPlusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(),
                        credential).build();

                PlusDomains.Circles.List listCircles = mPlusDomains.circles().list("me");
                listCircles.setMaxResults(5L);
                CircleFeed circleFeed = listCircles.execute();
                mCircles = circleFeed.getItems();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
            return mCircles;
        }

        @Override
        protected void onPostExecute(List<Circle> circles) {
            if(circles != null) {
                final ArrayList<String> circleNames = new ArrayList<String>();

                for(Circle c : circles) {
                    circleIds.add(c.getId());
                    circleNames.add(c.getDisplayName());
                }

                CircleList.setAdapter(new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, circleNames));
                CircleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent(getActivity(), CircleActivity.class);
                        String circleid = circleIds.get(position);
                        String circleName = circleNames.get(position);
                        i.putExtra(CIRCLE_ID_EXTRA, circleid);
                        i.putExtra(CIRCLE_NAME_EXTRA, circleName);
                        getActivity().startActivity(i);
                    }
                });
            }
        }
    }
}