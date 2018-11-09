package com.quickiepos.example;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class ParentMainActivity extends AppCompatActivity {

           /*----------------------------------------------------------------------
            |  Class: ParentMainActivity
            |
            |  Purpose: Creates a Tab View that will enable the user to move through 3 tabs (Fragments)
            |
            |  Note: ParentLocationTab: Tab that will handle all the location
            |        ParentHistoryTab: Tab that will show the user's all the previous consultation
            |        ParentProfileTab: Tab that will show the users's profile and enable the user to edit profile details
            |
            |
            *-------------------------------------------------------------------*/

    //Declare class variables
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);

        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    ParentLocationTab parentLocationTab = new ParentLocationTab();
                    return parentLocationTab;
                case 1:
                    ParentHistoryTab parentHistoryTab = new ParentHistoryTab();
                    return parentHistoryTab;
                case 2:
                    ParentProfileTab parentProfileTab = new ParentProfileTab();
                    return parentProfileTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent_icon activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_log_out) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ParentMainActivity.this, UserOptionsActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
