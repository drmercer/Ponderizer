package net.danmercer.ponderizer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_SCRIPTURE_REFERENCE = "S_REF";
    private static final String KEY_CLICK_LISTENER = "CLICK_LISTENER";

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
                toast("FAB clicked");
            }
        });

        // Fill scriptures View
        ListView listView = (ListView) findViewById(R.id.scripturesList);
        SimpleAdapter adapter = getListAdapter();

        // - attach adapter to ListView
        listView.setAdapter(adapter);
    }

    private void toast(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    @NonNull
    private SimpleAdapter getListAdapter() {
        // - create mock list of references
        LinkedList<String> scriptureRefs = new LinkedList<>();
        scriptureRefs.add("James 1:5");
        scriptureRefs.add("1 John 3:18");

        // - fill an adapter with the list data
        List<HashMap<String, Object>> list = new ArrayList<>();
        for (final String s : scriptureRefs) { // For each scripture reference...
            // Create a map containing the scripture reference and its click listener
            list.add(new HashMap<String, Object>() {{
                put(KEY_SCRIPTURE_REFERENCE, s);
            }});
        }
        SimpleAdapter adapter = new ScriptureListAdapter(
                this, // Context
                list, // list of scripture references used to populate the ListView
                R.layout.list_item, // layout used for each item in ListView
                new String[]{KEY_SCRIPTURE_REFERENCE, KEY_SCRIPTURE_REFERENCE}, // keys to look for in the maps in list
                new int[]{R.id.listitem_text, R.id.listitem}); // ids of views to fill with values

        // Need to set a ViewBinder so that the list items will do something. This ViewBinder
        // attaches the OnClickListeners (found in the maps in list) to the list entries
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.listitem) {
                    view.setTag(data);
                    Log.d("MainActivity.getAdapter", "setTag on view " + view.toString() + " to " + data.toString());
                    view.setOnCreateContextMenuListener(MainActivity.this);
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("OnItemClickListen", "onItemClick called! " + MainActivity.this.getResources().getResourceName(v.getId()));
                            v.showContextMenu();
                        }
                    });
                    return true; // Tells the SimpleAdapter that this view is all taken care of.
                }
                return false;
            }
        });
        return adapter;
    }

    // This is called when a list item is clicked
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listitem) {
            final String scripture = (String) v.getTag();
            getMenuInflater().inflate(R.menu.scripture_context, menu); // Inflate menu from res.

            // Create menu click listener
            MenuItem.OnMenuItemClickListener l = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.context_view_scripture:
                            // TODO: go to scripture view
                            toast("Would go to scripture view: " + scripture);
                            return true;
                        case R.id.context_memorize_scripture:
                            // TODO: go to memorize view
                            toast("Would go to memorize view " + scripture);
                            return true;
                        case R.id.context_view_notes:
                            // TODO: go to notes view
                            toast("Would go to notes view " + scripture);
                            return true;
                    }
                    return false;
                }
            };

            // Attach listener to all items on the menu
            int size = menu.size();
            for (int i = 0; i < size; i++) {
                menu.getItem(i).setOnMenuItemClickListener(l);
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
            toast("<would open settings>");
            return true;
        } else if (id == R.id.action_test) {
            // TODO: START TEST ACTIVITY
            toast("<would open test activity>");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
