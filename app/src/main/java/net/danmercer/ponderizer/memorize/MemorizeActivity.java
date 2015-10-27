package net.danmercer.ponderizer.memorize;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

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
        mScripture = intent.getParcelableExtra(Scripture.EXTRA_SCRIPTURE);
        if (mScripture == null) {
            // If no Scripture was put into the intent, abort the Activity and report an error.
            Log.e("MemorizeActivity", "MemorizeActivity was launched without a Scripture!");
            finish();
        }
        mText = mScripture.getBody();

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
            int part2 = (wordsToMaskPerTen % 2 == 1)? part1 + 1 : part1;
            Log.d("mathy", "wordsToMaskPerTen: " + wordsToMaskPerTen);
            Log.d("mathy", "part 1: " + part1);
            Log.d("mathy", "part 2: " + part2);

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
