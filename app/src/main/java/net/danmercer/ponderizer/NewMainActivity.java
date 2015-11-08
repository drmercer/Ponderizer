/*
 * Copyright 2015. Dan Mercer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.danmercer.ponderizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Random;

public class NewMainActivity extends AppCompatActivity {

    public enum Category {
        IN_PROGRESS,
        COMPLETED;
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        int margin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        mViewPager.setPageMargin(margin);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Set up "Add" floating action button.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch activity with instructions for adding scriptures
                Intent i = new Intent(NewMainActivity.this.getApplicationContext(), AddScriptureInstructions.class);
                startActivity(i);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        // One in fifty times, show a SnackBar asking for feature suggestions.
        int rand = new Random().nextInt(50);
        if (rand == 33) {
            Snackbar s = Snackbar.make(findViewById(R.id.fab),
                    R.string.feedback_could_improve, Snackbar.LENGTH_LONG);
            s.setAction(R.string.feedback_tell_me, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchFeedbackDialog();
                }
            });
            s.show();
        }
    }

    // Called by the SnackBar created in onStart() (up there ^) to launch a dialog to allow the
    // user to send a feedback email.
    private void launchFeedbackDialog() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setMessage(R.string.feedback_dialog_message);
        AlertDialog.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("onclick", "fired");
                launchFeedbackEmailIntent();
            }
        };
        db.setPositiveButton(R.string.feedback_dialog_confirm, l);
        db.setNegativeButton(R.string.feedback_dialog_cancel, null);
        db.show();
    }

    // Called by the feedback dialog to start an activity via an email intent.
    private void launchFeedbackEmailIntent() {
        String version = null;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Never going to happen.
            e.printStackTrace();
        }
        Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setType("text/plain");
        i.setData(Uri.parse("mailto:"));
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"danmercerdev@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT,
                "Feature Suggestion for Ponderizer (" + version + ")");
        i.putExtra(Intent.EXTRA_TEXT, "Please describe the feature you would like to see. Thanks " +
                "for supporting the Ponderizer app!\n\n");
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_insert_sm:
                ScriptureMasteryHelper.showDialog(this, new Runnable() {
                    @Override
                    public void run() {
                        refreshScriptureLists();
                    }
                });
                return true;
            
            case R.id.action_feedback:
                launchFeedbackDialog();
                return true;

            case R.id.action_help:
                Uri url = Uri.parse("https://github.com/drmercer/Ponderizer/wiki/User-Help-Pages");
                Intent help = new Intent(Intent.ACTION_VIEW, url);
                startActivity(help);
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshScriptureLists() {
        mSectionsPagerAdapter.refreshLists();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class MainPagerAdapter extends FragmentPagerAdapter {

        private ScriptureListFragment[] frags = new ScriptureListFragment[2];

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private Category getCategoryForPosition(int position) {
            if (position == 0) {
                return Category.IN_PROGRESS;
            } else if (position == 1) {
                return Category.COMPLETED;
            } else {
                Log.e("MainPagerAdapter", "getCategoryForPosition() called with invalid position");
                return null;
            }
        }

        @Override
        public Fragment getItem(int position) {
            if (frags[position] == null) {
                frags[position] = ScriptureListFragment.newInstance(getCategoryForPosition(position));
            }
            return frags[position];
        }

        @Override
        public int getCount() {
            // Show 2 total pages - "In Progress" and "Completed"
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Category cat = getCategoryForPosition(position);
            switch (cat) {
                case IN_PROGRESS:
                    return "In Progress";
                case COMPLETED:
                    return "Completed";
                default:
                    // Invalid position
                    return null;
            }
        }

        public void refreshLists() {
            for (ScriptureListFragment f : frags) {
                f.refreshScriptureList();
            }
        }
    }

}
