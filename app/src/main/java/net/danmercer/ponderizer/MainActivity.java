package net.danmercer.ponderizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String CATEGORY_PRESENT = "present";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up ActionBar with support API
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up FAB demo (circle button with email icon) TODO: replace/remove this
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAddScripture();
            }
        });

        // Fill scriptures View
        File dir = getDir(CATEGORY_PRESENT, MODE_PRIVATE);
        final LinkedList<Scripture> scriptureList = Scripture.loadScriptures(dir);
        if (!scriptureList.isEmpty()) {
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<Scripture>(this, R.layout.list_item, R.id.listitem_text, scriptureList));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String scripture = scriptureList.get(position).toString();
                    Toast.makeText(MainActivity.this, scripture, Toast.LENGTH_SHORT).show();
                    // TODO: start scripture view activity
                }
            });
        } else {
            //TODO: set up empty list
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list_item, R.id.listitem_text, new String[]{"Tap to add a scripture"}));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    launchAddScripture();
                }
            });
        }
    }

    private void launchAddScripture() {
        Intent i = new Intent(MainActivity.this.getApplicationContext(), AddScriptureReferenceActivity.class);
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

        if (id == R.id.action_settings) {
            // TODO: Open Settings
            Toast.makeText(this, "<would open settings>", Toast.LENGTH_LONG);
            return true;
        } else if (id == R.id.action_test) {
            // TODO: START TEST ACTIVITY
            Toast.makeText(this, "<would open test activity>", Toast.LENGTH_LONG);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
