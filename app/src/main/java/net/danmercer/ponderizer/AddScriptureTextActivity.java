package net.danmercer.ponderizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class AddScriptureTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        // Set up ActionBar with support API
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get text fields
        EditText title = (EditText) findViewById(R.id.scripture_title);
        EditText body = (EditText) findViewById(R.id.scripture_text);

        // Get the text that was shared with us from the start intent
        Intent startIntent = getIntent();
        String bodyString = startIntent.getStringExtra(Intent.EXTRA_TEXT);
        if (bodyString == null) {
            bodyString = startIntent.getStringExtra(Intent.EXTRA_TITLE);
        } else {
            String titleString = startIntent.getStringExtra(Intent.EXTRA_TITLE);
            if (title != null) {
                title.setText(titleString);
            }
        }
        body.setText(cleanupText(bodyString));
    }

    /**
     * Gets rid of any words in the text that contain forward slashes (i.e. URLs)
     */
    private String cleanupText(String text) {
        String[] tokens = text.split("\\s"); // Split into word-like tokens
        for (String token : tokens) {
            // If the token has any forward or backward slashes, remove it
            if (token.contains("/")) {
                text = text.replace(token, "");
            }
        }
        return text.trim(); // Trim any extra whitespace
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_scripture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            // TODO: record scripture in device memory
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
