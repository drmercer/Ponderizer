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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

public class MemorizeTestActivity extends AppCompatActivity {

    public static final String WORD_SPLIT_REGEX = "[\\d\\s]+";
    private Scripture mScripture;
    private EditText mTextInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorize_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get scripture from intent
        mScripture = getIntent().getParcelableExtra(Scripture.EXTRA_SCRIPTURE);
        if (mScripture == null) {
            Log.e("MemorizeTestActivity", "Started MemorizeTestActivity without a scripture.");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        TextView instructions = (TextView) findViewById(R.id.text1);
        instructions.setText(getResources().getString(R.string.enter_recitiation_fmt, mScripture.getReference()));

        mTextInput = (EditText) findViewById(R.id.editText);

        Button checkButton = (Button) findViewById(R.id.button_check_recitiation);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkTextAccuracy();
            }
        });
    }

    private void checkTextAccuracy() {
        String userInput = mTextInput.getText().toString();
        String scriptureText = mScripture.getBody();

        String[] userWords = userInput.split(WORD_SPLIT_REGEX);
        String[] scripWords = scriptureText.split(WORD_SPLIT_REGEX);

        // TODO: write diff comparison algorithm
    }

}
