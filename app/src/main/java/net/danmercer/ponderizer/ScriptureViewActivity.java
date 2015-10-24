package net.danmercer.ponderizer;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ScriptureViewActivity extends AppCompatActivity {

    public static final int RESULT_SCRIPTURE_DELETED = 2;
    // The scripture being shown in this activity
    private Scripture scripture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scripture_view);

        // Get the Scripture to display
        Intent intent = getIntent();
        scripture = intent.getParcelableExtra(Scripture.EXTRA_NAME);
        if (scripture == null) {
            // If no Scripture was put into the intent, abort the Activity and report an error.
            Log.e("ScriptureViewActivity", "ScriptureViewActivity was launched without a Scripture!");
            finish();
        }

        // Display the scripture reference in the Activty title
        String reference = scripture.getReference();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(reference);
        } else {
            setTitle(reference);
        }

        // Display the scripture text in the activity.
        TextView textView = (TextView) findViewById(R.id.scripture_text);
        textView.setText(scripture.getBody());

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
                File presentDir = getDir(MainActivity.CATEGORY_PRESENT, MODE_PRIVATE);
                File[] list = presentDir.listFiles();
                for (File f : list) {
                    if (scripture.filename.equals(f.getName())) {
                        f.delete();
                        break;
                    }
                }
                // TODO: delete the scripture
                setResult(RESULT_SCRIPTURE_DELETED);
                finish();
                return true;
            case R.id.action_add_note:
                // Add a note to this scripture's notes list
                Toast.makeText(this, "Add Note to list", Toast.LENGTH_LONG).show();
                // TODO: add note
                return true;
            case R.id.action_memorize:
                // Open the Memorize view
                Toast.makeText(this, "Memorize Scripture", Toast.LENGTH_LONG).show();
                // TODO: open memorize view
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Open Settings", Toast.LENGTH_LONG).show();
                // Open Settings Activity
                // TODO: open settings activity
        }
        return super.onOptionsItemSelected(item);
    }
}
