package com.example.googleplusmini;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class PagerActivity extends FragmentActivity implements ActionBar.TabListener {
    private static final int PROFILE_TAB_POS = 0;
    private static final int CIRCLE_TAB_POS = 1;

    private ActionBar actionbar;
    private ViewPager viewpager;

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = this.getSharedPreferences(
                "com.example.googleplusmini", Context.MODE_PRIVATE);

        viewpager = new ViewPager(this);
        viewpager.setId(R.id.viewpager);
        setContentView(viewpager);
        viewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Fragment fragment=null;
                if(position == PROFILE_TAB_POS) {
                    fragment = new ProfileFragment();
                } else if(position == CIRCLE_TAB_POS) {
                    fragment = new CircleListFragment();
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });

        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                actionbar.setSelectedNavigationItem(arg0);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionbar.setDisplayShowHomeEnabled(false);
        //actionbar.setDisplayShowTitleEnabled(false);
        ActionBar.Tab profileTab=actionbar.newTab();
        ActionBar.Tab circlesTab=actionbar.newTab();
        actionbar.setTitle(R.string.app_name);
        circlesTab.setText("Circles");
        circlesTab.setTabListener(this);
        profileTab.setText("Profile");
        profileTab.setTabListener(this);
        actionbar.addTab(profileTab);
        actionbar.addTab(circlesTab);
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

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewpager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }
}