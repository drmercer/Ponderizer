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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.danmercer.ponderizer.memorize.MemorizeActivity;
import net.danmercer.ponderizer.scriptureview.ScriptureViewActivity;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_VIEW_SCRIPTURE = 1;
    private LinkedList<Scripture> mScriptureList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up ActionBar via the support API
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up "Add" floating action button.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch activity with instructions for adding scriptures
                Intent i = new Intent(MainActivity.this.getApplicationContext(), AddScriptureInstructions.class);
                startActivity(i);
            }
        });

        // Fill scriptures View
        refreshScriptureList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int rand = new Random().nextInt(50);
        if (rand == 33) {
            Snackbar s = Snackbar.make(findViewById(R.id.fab), "Could this app be improved?", Snackbar.LENGTH_LONG);
            s.setAction("Tell me how!", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchFeedbackDialog();
                }
            });
            s.show();
        }
    }

    private void launchFeedbackDialog() {
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setMessage("Want to suggest a feature? Tap \"OK\" to write me an email.");
        AlertDialog.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("onclick", "fired");
                launchFeedbackEmailIntent();
            }
        };
        db.setPositiveButton("OK", l);
        db.setNegativeButton("Nah", null);
        db.show();
    }

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
    protected void onResume() {
        super.onResume();
        refreshScriptureList();
    }

    // Fills or refreshes the list of Scriptures
    private void refreshScriptureList() {
        mScriptureList = Scripture.loadScriptures(this, NewMainActivity.Category.IN_PROGRESS);
        if (!mScriptureList.isEmpty()) {
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<Scripture>(this, R.layout.list_item, R.id.listitem_text, mScriptureList));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Scripture scripture = mScriptureList.get(position);
                    startActivityForResult(scripture.getViewIntent(MainActivity.this), REQUEST_VIEW_SCRIPTURE);
                }
            });
            registerForContextMenu(lv);
        } else {
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.listitem_text, new String[]{"Tap to add a scripture"}));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Launch the AddScriptureInstructions activity
                    Intent i = new Intent(MainActivity.this.getApplicationContext(), AddScriptureInstructions.class);
                    startActivity(i);
                }
            });
            unregisterForContextMenu(lv);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_scripture, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position; // Get index of item that was long-clicked
        Scripture s = mScriptureList.get(pos);

        switch (item.getItemId()) {

            case R.id.action_delete:
                // Delete this scripture
                s.deleteWithConfirmation(this, new Runnable() {
                    @Override
                    public void run() {
                        // Refresh mScripturesList
                        refreshScriptureList();
                    }
                });
                return true;

            case R.id.action_add_note:
                startActivity(s.getAddNoteIntent(this));
                return true;

            case R.id.action_view:
                startActivityForResult(s.getViewIntent(this), REQUEST_VIEW_SCRIPTURE);
                return true;

            case R.id.action_memorize:
                startActivity(s.getMemorizeIntent(this));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIEW_SCRIPTURE) {
            // The user just got back from viewing a scripture.
            if (resultCode == ScriptureViewActivity.RESULT_SCRIPTURE_DELETED) {
                // The scripture was deleted, so refresh the list.
                refreshScriptureList();
            }
        }
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
            case R.id.action_feedback:
                launchFeedbackDialog();
                return true;

            case R.id.action_help:
                Uri url = Uri.parse("https://github.com/drmercer/Ponderizer/wiki/User-Help-Pages");
                Intent help = new Intent(Intent.ACTION_VIEW, url);
                startActivity(help);
        }

        return super.onOptionsItemSelected(item);
    }
}
