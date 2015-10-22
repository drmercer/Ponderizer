package net.danmercer.ponderizer;

import android.app.Activity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dan on 10/20/2015.
 */
public class ScriptureListAdapter extends SimpleAdapter implements View.OnCreateContextMenuListener {
    private static final String KEY_SCRIPTURE = "SCRIPTURE";

    private static List<? extends Map<String, Scripture>> convertScriptureList(List<Scripture> scriptures) {
        // Fill a list of maps (which is what the SimpleAdapter superclass requires) with the data
        // in the scriptures list
        List<HashMap<String, Scripture>> list = new ArrayList<>();
        for (final Scripture s : scriptures) { // For each scripture...
            // Create a map containing the scripture, and put it in the list.
            list.add(new HashMap<String, Scripture>() {{
                put(KEY_SCRIPTURE, s);
            }});
        }
        return list;
    }

    // The Activity in which this Scripture list is being displayed.
    private final Activity activity;

    public ScriptureListAdapter(final Activity activity, List<Scripture> scriptures, int layoutRes, int rootViewId, int textViewId) {
        super(activity,
                convertScriptureList(scriptures), // The list of scriptures
                layoutRes, // The id of the layout to use
                new String[]{KEY_SCRIPTURE, KEY_SCRIPTURE}, // keys to look for in the maps in list
                new int[]{rootViewId, textViewId}); // ids of views to fill with values
        this.activity = activity;

        // Set up ViewBinder so that the list items will do something. This ViewBinder
        // attaches OnClickListeners to the list entries and populates their text views
        setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                // We only want to deal with Scriptures, so if data is not a scripture,
                // log an error and return false.
                if (!(data instanceof Scripture)) {
                    Log.e("ScriptureListAdapter",
                            "setViewValue was called with data that is not a Scripture!");
                    return false;
                }

                // Now that we know data is a Scripture, we can determine what "view" is:
                if (view.getId() == R.id.listitem) {
                    // View is the root view of the list item.
                    // Put Scripture as the tag of the  view:
                    view.setTag(data);
                    Log.d("MainActivity.getAdapter", "setTag on view " + view.toString() + " to " + data.toString());
                    // Set the activity as the OnCreateContextMenuListener:
                    view.setOnCreateContextMenuListener(activity);
                    // Set up the onClickListener:
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d("OnItemClickListen", "onItemClick called! " + activity.getResources().getResourceName(v.getId()));
                            v.showContextMenu();
                        }
                    });
                    // Return true to inform the Adapter that this view is all taken care of.
                    return true;

                } else if (view.getId() == R.id.listitem_text) {
                    // If the view is the listitem_text view
                    // Assert that the view is a TextView, just to be safe.
                    if (!(view instanceof TextView)) {
                        throw new IllegalStateException(
                                "View with id listitem_text is not a TextView!");
                    }

                    // Apply the scripture reference as the text of the TextView
                    ((TextView) view).setText(((Scripture) data).getReference());
                    // Return true to inform the Adapter that this view is all taken care of.
                    return true;

                }
                // If it reaches this point, it means view was neither "listitem" or
                // "listitem_text". This shouldn't ever happen.
                Log.e("ScriptureListAdapter", "something funky happened in the ViewBinder!");
                return false;
            }
        });
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true; // This is just here to make sure the list items are clickable
    }

    @Override
    public boolean isEnabled(int position) {
        return true; // This is just here to make sure the list items are clickable
    }

    // This is called when a list item is clicked (which creates a context menu for that list item)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listitem) {
            final String scripture = (String) v.getTag();
            activity.getMenuInflater().inflate(R.menu.scripture_context, menu); // Inflate menu from res.

            // Create menu click listener
            MenuItem.OnMenuItemClickListener l = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.context_view_scripture:
                            // TODO: go to scripture view
                            Toast.makeText(activity,
                                    "Would go to scripture view: " + scripture,
                                    Toast.LENGTH_LONG).show();
                            return true;
                        case R.id.context_memorize_scripture:
                            // TODO: go to memorize view
                            Toast.makeText(activity,
                                    "Would go to memorize view " + scripture,
                                    Toast.LENGTH_LONG).show();
                            return true;
                        case R.id.context_view_notes:
                            // TODO: go to notes view
                            Toast.makeText(activity,
                                    "Would go to notes view " + scripture,
                                    Toast.LENGTH_LONG).show();
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
}
