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

package net.danmercer.ponderizer.scriptureview;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.danmercer.ponderizer.ExportActivity;
import net.danmercer.ponderizer.NewMainActivity;
import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;
import net.danmercer.ponderizer.memorize.MemorizeActivity;
import net.danmercer.ponderizer.memorize.MemorizeTestActivity;

public class ScriptureViewActivity extends AppCompatActivity {
    public static final int NUM_OF_TABS = 2;
    public static final int IDX_SCRIPTURE_TAB = 0;
    public static final int IDX_NOTES_TAB = 1;

    public class ScriptureViewAdapter extends FragmentPagerAdapter {
        final NotesViewFragment notesFrag;
        final ScriptureTextFragment scriptureFrag;

        public ScriptureViewAdapter(FragmentManager fm) {
            super(fm);
            notesFrag = NotesViewFragment.createNew(scripture);
            scriptureFrag = ScriptureTextFragment.createNew(scripture);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case IDX_SCRIPTURE_TAB:
                    return scriptureFrag;
                case IDX_NOTES_TAB:
                    return notesFrag;
                default:
                    Log.e("ScriptureViewAdapter", "Unsupported page index: " + position);
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case IDX_SCRIPTURE_TAB:
                    return getResources().getString(R.string.tab_title_scripture);
                case IDX_NOTES_TAB:
                    return getResources().getString(R.string.tab_title_notes);
                default:
                    Log.e("ScriptureViewAdapter", "Unsupported page index: " + position);
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_OF_TABS;
        }
    }

    // The result code that this activity ends with iff the scripture has been deleted.
    public static final int RESULT_SCRIPTURE_DELETED = 2;
    // The scripture being shown in this activity
    Scripture scripture;

    private ViewPager pager;
    private ScriptureViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scripture_view);

        // Get the Scripture to display
        Intent intent = getIntent();
        scripture = intent.getParcelableExtra(ScriptureIntent.EXTRA_SCRIPTURE);
        if (scripture == null) {
            // If no Scripture was put into the intent, abort the Activity and report an error.
            Log.e("ScriptureViewActivity", "ScriptureViewActivity was launched without a Scripture!");
            finish();
            return;
        }

        // Display the scripture reference in the Activty title
        String reference = scripture.getReference();
        ActionBar ab = getSupportActionBar();
        ab.setTitle(reference);


        // Set up the page fragments (scripture view and notes)
        adapter = new ScriptureViewAdapter(getSupportFragmentManager());
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Set up tabs
        TabLayout tl = (TabLayout) findViewById(R.id.tabs);
        tl.setTabsFromPagerAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tl));
        tl.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Nothing doing!
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Nothing doing!
            }
        });

        // Set the result to RESULT_OK by default.
        setResult(RESULT_OK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_scripture_view, menu);
        inflater.inflate(R.menu.menu_memorize, menu);
        // Appends the "Memorize Test" button to this menu

        MenuItem markCompleted = menu.findItem(R.id.action_mark_completed);
        if (scripture.isCompleted()) {
            markCompleted.setTitle(R.string.menu_mark_in_progress);
        } else {
            markCompleted.setTitle(R.string.menu_mark_complete);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_delete:
                // Delete this scripture
                scripture.deleteWithConfirmation(this, new Runnable() {
                    @Override
                    public void run() {
                        setResult(RESULT_SCRIPTURE_DELETED);
                        finish();
                    }
                });
                return true;

            case R.id.action_add_note:
                adapter.notesFrag.launchAddNoteActivity();
                return true;

            case R.id.action_memorize:
                // Open the Memorize view
                startActivity(new ScriptureIntent(this, MemorizeActivity.class, scripture));
                return true;

            case R.id.action_start_test:
                // Launch MemorizeTestActivity
                startActivity(new ScriptureIntent(this, MemorizeTestActivity.class, scripture));
                return true;

            case R.id.action_mark_completed:
                // Mark the scripture as completed/incompleted - i.e. toggle the category
                if (!scripture.isCompleted()) {
                    scripture.changeCategory(this, NewMainActivity.Category.COMPLETED);
                    item.setTitle(R.string.menu_mark_in_progress);
                } else {
                    scripture.changeCategory(this, NewMainActivity.Category.IN_PROGRESS);
                    item.setTitle(R.string.menu_mark_complete);
                }
                finish();
                return true;

            case R.id.action_export:
                startActivity(new ScriptureIntent(this, ExportActivity.class, scripture));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
