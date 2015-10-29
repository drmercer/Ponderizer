package net.danmercer.ponderizer.scriptureview;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.danmercer.ponderizer.ExportActivity;
import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;
import net.danmercer.ponderizer.memorize.MemorizeActivity;

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
                    return "Scripture";
                case IDX_NOTES_TAB:
                    return "Notes";
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

    // The result code that this activity ends with iff the scripture has been deleted. This tells
    // the MainActivity (which starts this one) that it should refresh its scripture list
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
        scripture = intent.getParcelableExtra(Scripture.EXTRA_SCRIPTURE);
        if (scripture == null) {
            // If no Scripture was put into the intent, abort the Activity and report an error.
            Log.e("ScriptureViewActivity", "ScriptureViewActivity was launched without a Scripture!");
            finish();
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
        getMenuInflater().inflate(R.menu.menu_scripture_view, menu);
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
                Intent i = new Intent(this, MemorizeActivity.class);
                i.putExtra(Scripture.EXTRA_SCRIPTURE, scripture);
                startActivity(i);
                return true;

            case R.id.action_export:
                Intent intent = new Intent(this, ExportActivity.class);
                intent.putExtra(Scripture.EXTRA_SCRIPTURE, scripture);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
