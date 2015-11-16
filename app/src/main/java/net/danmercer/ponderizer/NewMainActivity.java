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

import java.io.File;
import java.util.Random;

public class NewMainActivity extends AppActivity {

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

        // Set up the App Bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Migrate scriptures from old file system
        File ipDir = Scripture.getDir(this, Category.IN_PROGRESS);
        File parentDir = ipDir.getParentFile();
        File oldDir = new File(parentDir, "app_present");
        if (oldDir.exists()) {
            File[] oldFiles = oldDir.listFiles();
            for (File f : oldFiles) {
                Log.d("NewMain", "old = " + f.getAbsolutePath());
                File newPath = new File(ipDir, f.getName());
                Log.d("NewMain", "new = " + newPath.getAbsolutePath());
                f.renameTo(newPath);
            }
        }

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

        // One in fifty times, show a SnackBar prompting the user to view the about page
        int rand = new Random().nextInt(50);
        if (rand == 33) {
            Snackbar s = Snackbar.make(findViewById(R.id.fab),
                    R.string.popup_like_this_app, Snackbar.LENGTH_LONG);
            s.setAction(R.string.popup_contribute, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(NewMainActivity.this, AboutActivity.class));
                }
            });
            s.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
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
