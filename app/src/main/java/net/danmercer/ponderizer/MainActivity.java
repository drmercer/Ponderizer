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
                Intent i = new Intent(MainActivity.this.getApplicationContext(), AddScriptureReferenceActivity.class);
                startActivity(i);
            }
        });

        // Fill scriptures View
        // Create mock scripture references if necessary
        File dir = getDir(CATEGORY_PRESENT, MODE_PRIVATE);
        if (dir.listFiles().length == 0) {
            Scripture mock1 = new Scripture("1 Nephi 2:15", "15 And my father dwelt in a tent.");
            mock1.writeToFile(dir);
            Scripture mock2 = new Scripture("Jacob 6:12", "12 O be wise; what can I say more?");
            mock2.writeToFile(dir);
        }
        LinkedList<Scripture> scriptureList = Scripture.loadScriptures(dir);
        if (!scriptureList.isEmpty()) {
            ListView listView = (ListView) findViewById(R.id.scripturesList);
            // - get scripture references
            ListAdapter adapter = getListAdapter(scriptureList);

            // - attach adapter to ListView
            listView.setAdapter(adapter);
        }
    }

    @NonNull
    private ListAdapter getListAdapter(LinkedList<Scripture> scriptureList) { // TODO: inline?
        // Construct adapter
        ListAdapter adapter = new ScriptureListAdapter(
                this, // Activity
                scriptureList, // List of scriptures used to populate the ListView.
                R.layout.list_item, // Layout used for each item in the ListView.
                R.id.listitem, // Root view of that layout.
                R.id.listitem_text); // Text view in that layout, to put scripture reference in.

        return adapter;
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
