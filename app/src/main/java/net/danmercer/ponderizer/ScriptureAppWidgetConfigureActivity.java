package net.danmercer.ponderizer;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;

/**
 * The configuration screen for the {@link ScriptureAppWidget ScriptureAppWidget} AppWidget.
 */
public class ScriptureAppWidgetConfigureActivity extends Activity implements AdapterView.OnItemClickListener {

    // FIELDS:
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID; // Default value is an invalid ID
    private LinkedList<Scripture> mScriptureList;

    public ScriptureAppWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.scripture_app_widget_configure);
        ListView mListView = (ListView) findViewById(R.id.scripturesList);

        // Initialize ListView
        File dir = getDir(Scripture.CATEGORY_PRESENT, MODE_PRIVATE);
        mScriptureList = Scripture.loadScriptures(dir);
        if (!mScriptureList.isEmpty()) {
            ListView lv = (ListView) findViewById(R.id.scripturesList);
            lv.setAdapter(new ArrayAdapter<Scripture>(this, R.layout.list_item, R.id.listitem_text, mScriptureList));
            lv.setOnItemClickListener(this);
        } else {
            // Scriptures list is empty, so tell the user to add a scripture first.
            Toast.makeText(this,
                    "Please add a scripture from Gospel Library before placing a widget.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, AddScriptureInstructions.class));
            finish();
            return;
        }

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }

    // Called when an entry in the ListView is clicked.
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Scripture scripture = mScriptureList.get(position);

        String reference = scripture.getReference();
        String text = scripture.getBody();

        // Save widget data to preferences file
        ScriptureAppWidget.saveWidgetPrefs(this, mAppWidgetId, reference, text);

        // It is the responsibility of the configuration activity to update the app widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ScriptureAppWidget.updateAppWidget(this, appWidgetManager, mAppWidgetId);

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

}

