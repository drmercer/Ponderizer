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

package net.danmercer.ponderizer.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;
import net.danmercer.ponderizer.scriptureview.ScriptureIntent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemorizeActivity extends AppCompatActivity {
    private Scripture mScripture;
    private boolean mFirstLetterVisible;
    private String mText;
    private TextView mScripView;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the Scripture to display
        Intent intent = getIntent();
        mScripture = intent.getParcelableExtra(ScriptureIntent.EXTRA_SCRIPTURE);
        if (mScripture == null) {
            // If no Scripture was put into the intent, abort the Activity and report an error.
            Log.e("MemorizeActivity", "MemorizeActivity was launched without a Scripture!");
            finish();
            return;
        }
        mText = mScripture.getBody();

        // Set activity title to show scripture reference
        setTitle(getTitle() + " " + mScripture.getReference());

        // Set up Spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "\"A___\" Show first letters",
                        "\"____\" Hide whole words"
                }));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFirstLetterVisible = position == 0;
                updateTextView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { /* Don't care! */ }
        });

        // Set up SeekBar
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setMax(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the scripture text view with the new setting.
                updateTextView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* Don't care! */ }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* Don't care! */ }
        });
        mScripView = (TextView) findViewById(R.id.scripture_text);

        // Set the seek bar to maximum, so that the user starts with the text masked.
        mSeekBar.setProgress(mSeekBar.getMax());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_memorize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_test:
                // Launch MemorizeTestActivity
                startActivity(new ScriptureIntent(this, MemorizeTestActivity.class, mScripture));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateTextView() {
        Pattern p;
        if (mFirstLetterVisible) {
            p = Pattern.compile("(?<=[A-z])[A-z']+");
        } else {
            p = Pattern.compile("[A-z']+");
        }
        Matcher m = p.matcher(mText);

        // Count all the words.
        int wordCount = 0;
        while (m.find()) {
            wordCount++;
        }
        m.reset();

        int seekBarProgress = mSeekBar.getProgress();
        if (seekBarProgress != 0) {
            // Convert String mText into a char array
            int textLength = mText.length();
            char[] text = new char[textLength];
            mText.getChars(0, textLength, text, 0);

            int wordsToMaskPerTen = seekBarProgress / 10;
            // Divide that number into two parts, a floored half and a ceiling'ed half. This way we
            // can more evenly distribute the masked words throughout the text.
            int part1 = wordsToMaskPerTen / 2;
            int part2 = (wordsToMaskPerTen % 2 == 1) ? part1 + 1 : part1;

            for (int i = 0; m.find(); i++) {
                boolean mask;
                if (i % 10 < 5) {
                    mask = i % 5 < part1;
                } else {
                    mask = i % 5 < part2;
                }
                if (mask) {
                    int startIdx = m.start();
                    int matchLength = m.end() - startIdx;
                    for (int j = 0; j < matchLength; j++) {
                        text[startIdx + j] = '_';
                    }
                }
            }

            String newText = new String(text);
            mScripView.setText(newText);
        } else {
            mScripView.setText(mText);
        }
    }

}
