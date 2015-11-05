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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AddScriptureTextActivity extends AppCompatActivity {

    private EditText titleTextView;
    private EditText bodyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        // Set up ActionBar with support API
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Get text fields
        titleTextView = (EditText) findViewById(R.id.scripture_title);
        bodyTextView = (EditText) findViewById(R.id.scripture_text);

        // Get the text that was shared with us from the start intent
        Intent startIntent = getIntent();
        String bodyString = startIntent.getStringExtra(Intent.EXTRA_TEXT);
        if (bodyString == null) {
            bodyString = startIntent.getStringExtra(Intent.EXTRA_TITLE);
        } else {
            String titleString = startIntent.getStringExtra(Intent.EXTRA_TITLE);
            if (titleTextView != null) {
                titleTextView.setText(titleString);
            }
        }
        bodyTextView.setText(cleanupText(bodyString));
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
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_done) {
            Log.d("AddScriptureText", "Done button pressed");
            // Construct a new Scripture object using the text provided by the user.
            String reference = titleTextView.getText().toString();
            String body = bodyTextView.getText().toString();

            // Make sure the user entered something in both fields.
            if (reference.isEmpty()) {
                Toast.makeText(this,
                        "Please enter a scripture reference for the title",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            if (body.isEmpty()) {
                Toast.makeText(this,
                        "Please enter a scripture passage",
                        Toast.LENGTH_LONG).show();
                return false;
            }

            new Scripture(reference, body, NewMainActivity.Category.IN_PROGRESS)
                    .writeToFile(getDir(NewMainActivity.Category.IN_PROGRESS.name(), MODE_PRIVATE));
            Toast.makeText(this, "Added " + reference, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
