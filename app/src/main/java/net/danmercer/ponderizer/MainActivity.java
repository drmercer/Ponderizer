package net.danmercer.ponderizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    public static final String CATEGORY_PRESENT = "present";
    public static final int REQUEST_VIEW_SCRIPTURE = 1;

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
                Intent i = new Intent(MainActivity.this.getApplicationContext(), AddScriptureInstructions.class);
                startActivity(i);
            }
        });

        // Fill scriptures View
        refreshScriptureList();
    }

    // Fills or refreshes the list of Scriptures
    private void refreshScriptureList() {
        File dir = getDir(CATEGORY_PRESENT, MODE_PRIVATE);
        final LinkedList<Scripture> scriptureList = Scripture.loadScriptures(dir);
        if (!scriptureList.isEmpty()) {
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<Scripture>(this, R.layout.list_item, R.id.listitem_text, scriptureList));
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Scripture scripture = scriptureList.get(position);
                    Intent launch = new Intent(MainActivity.this, ScriptureViewActivity.class);
                    // Scripture implements Parcelable, so it can be added directly to the intent:
                    launch.putExtra(Scripture.EXTRA_NAME, scripture);
                    startActivityForResult(launch, REQUEST_VIEW_SCRIPTURE);
                }
            });
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIEW_SCRIPTURE) {
            if (resultCode == ScriptureViewActivity.RESULT_SCRIPTURE_DELETED) {
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
