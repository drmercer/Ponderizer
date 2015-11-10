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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.danmercer.ponderizer.NewMainActivity;
import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;
import net.danmercer.ponderizer.scriptureview.ScriptureIntent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MemorizeTestActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String WORD_SPLIT_REGEX = "[\\d\\s]+";
    public static final int COLOR_CORRECT = 0xFF11BB44;
    public static final int COLOR_INCORRECT = Color.RED;
    public static final String SPLIT_REGEX = "[\\s\\d]+";
    public static final String PLAYSTORE_URL = "https://play.google.com/store/apps/details?id=net.danmercer.ponderizer";
    private Scripture mScripture;
    private EditText mScriptureView;
    private Button[] mAnswerButtons;
    private String[] mWords;
    private Button mCorrectAnswer;
    private String mCorrectWord;
    private int mNextWordIndex = 0;
    private int mMissedWordCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get scripture from intent
        mScripture = getIntent().getParcelableExtra(ScriptureIntent.EXTRA_SCRIPTURE);
        if (mScripture == null) {
            Log.e("MemorizeTestActivity", "Started MemorizeTestActivity without a scripture.");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        // Split the scripture text into words
        String body = mScripture.body;
        mWords = body.split(SPLIT_REGEX);
        // Do all this to avoid empty strings in the mWords array.
        LinkedList<String> temp = new LinkedList<>(Arrays.asList(mWords));
        for (String word : mWords) {
            if (word == null || word.trim().isEmpty()) {
                // Filter any empty words out of the temp list
                temp.remove(word);
            }
        }
        int newWordCount = temp.size();
        // If some words were removed...
        if (newWordCount != mWords.length) {
            // Create a new array without the empty words.
            mWords = temp.toArray(new String[newWordCount]);
        }

        // Set up the instructions view with formatted text
        TextView instructions = (TextView) findViewById(R.id.text1);
        String instructionsText = getResources().getString(R.string.test_instructions_fmt, mScripture.getReference());
        instructions.setText(instructionsText);

        // Grab the repurposed EditText used to display the scripture as the user "types" it
        mScriptureView = (EditText) findViewById(R.id.editText);

        // Set up the answer buttons
        mAnswerButtons = new Button[4];
        mAnswerButtons[0] = (Button) findViewById(R.id.button1);
        mAnswerButtons[1] = (Button) findViewById(R.id.button2);
        mAnswerButtons[2] = (Button) findViewById(R.id.button3);
        mAnswerButtons[3] = (Button) findViewById(R.id.button4);

        // Use this activity as a click listener
        for (Button b : mAnswerButtons) {
            b.setOnClickListener(this);
        }

        // Set up the next question
        nextQuestion();
    }

    // Sets up the buttons with the next selection of four words.
    private void nextQuestion() {
        String[] nextChoices = new String[4];

        // Get the next word
        if (mNextWordIndex == mWords.length) {
            // The user finished the scripture, so get rid of the buttons
            for (Button b : mAnswerButtons) {
                b.setVisibility(View.GONE);
            }
            showResults();
            return;
        }
        mCorrectWord = mWords[mNextWordIndex++];
        nextChoices[0] = mCorrectWord; // next answer

        // fill nextChoices 1-3 with bogus answers
        Random random = new Random();
        for (int i = 1; i < 4; i++) {
            String word;
            do {
                word = mWords[random.nextInt(mWords.length)];
                // Loop because we don't want duplicate words in the array.
            } while (arrayContains(word, nextChoices));
            nextChoices[i] = word;
        }

        // Randomize array
        String temp;
        for (int i = nextChoices.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            temp = nextChoices[index];
            nextChoices[index] = nextChoices[i];
            nextChoices[i] = temp;
        }

        // Set up buttons with next choices
        for (int i = 0; i < 4; i++) {
            Button b = mAnswerButtons[i];
            b.setText(nextChoices[i]);
            if (nextChoices[i] == mCorrectWord) {
                mCorrectAnswer = b;
            }
        }

        // Increment next word index
    }

    private static boolean arrayContains(String word, String[] strings) {
        String wordLowerCase = word.toLowerCase();
        for (String s : strings) {
            if (s != null && wordLowerCase.equals(s.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    private void showResults() {
        final int percentageCorrect = (100 * (mWords.length - mMissedWordCount)) / mWords.length;
        boolean offerToMarkComplete = percentageCorrect >= 90
                && mScripture.getCategory() != NewMainActivity.Category.COMPLETED;

        // Create a dialog with the results
        AlertDialog.Builder db = new AlertDialog.Builder(this);
        String msg;
        if (percentageCorrect == 100) {
            // The user recited the scripture perfectly!
            db.setTitle(R.string.results_title_perfect);
            msg = getString(R.string.results_msg_perfect);
        } else {
            db.setTitle(R.string.results_title);
            msg = getString(R.string.results_msg_score_fmt, percentageCorrect);
        }
        if (offerToMarkComplete) { // If they did well enough...
            // Ask if they want to mark the scripture as completed.
            msg += " " + getString(R.string.results_mark_complete);
            db.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mScripture.changeCategory(MemorizeTestActivity.this,
                            NewMainActivity.Category.COMPLETED);
                }
            });
            db.setNegativeButton(R.string.no, null);

            // Also give the user the option to share their results
            db.setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Start an Activity chooser with a share Intent
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT,
                            getString(
                                    R.string.results_share_text_fmt,
                                    mScripture.getReference(),
                                    percentageCorrect) + "\n\n" + PLAYSTORE_URL);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                            getString(R.string.results_share_subject_fmt,
                                    mScripture.getReference(),
                                    percentageCorrect));
                    Intent chooser = Intent.createChooser(shareIntent, getString(R.string.choose_share));
                    startActivity(chooser);
                }
            });
        } else {
            msg += " " + getString(R.string.results_keep_trying);
            db.setPositiveButton(R.string.ok, null);
        }
        db.setMessage(msg);
        AlertDialog dialog = db.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        Log.d("OnClickListener", "" + v.getId());

        // Add word to string with proper format - red and strikethrough for incorrect words
        Editable text = mScriptureView.getText();
        text.append(" ");
        if (v != mCorrectAnswer) {
            // Count each incorrect word
            mMissedWordCount++;

            // Append the user's input to the text in the scripture view.
            String userWord = ((TextView) v).getText().toString();
            int oldLength = text.length();
            text.append(userWord);
            int newLength = text.length();
            // Add strikethrough
            StrikethroughSpan ss = new StrikethroughSpan();
            text.setSpan(ss, oldLength, newLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            // Add color
            ForegroundColorSpan cs = new ForegroundColorSpan(COLOR_INCORRECT);
            text.setSpan(cs, oldLength, newLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            // Append correct word without formatting
            text.append(' ');
            text.append(mCorrectWord);
        } else {
            // Grab the start and end indices of the new word in the TextView
            int oldLength = text.length();
            text.append(mCorrectWord);
            int newLength = text.length();
            // Add color to a Span between those indices
            ForegroundColorSpan cs = new ForegroundColorSpan(COLOR_CORRECT);
            text.setSpan(cs, oldLength, newLength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        mScriptureView.setText(text);

        nextQuestion();
    }
}
