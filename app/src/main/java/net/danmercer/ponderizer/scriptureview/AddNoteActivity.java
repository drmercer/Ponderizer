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

package net.danmercer.ponderizer.scriptureview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import net.danmercer.ponderizer.R;
import net.danmercer.ponderizer.Scripture;

import java.io.File;

public class AddNoteActivity extends AppCompatActivity {
    public static final int RESULT_DELETED = 4;

    private TextView mNoteEntry;
    private Intent mLaunchIntent;
    private boolean mExistingNote = false;
    private boolean mEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNoteEntry = (TextView) findViewById(R.id.note_edittext);

        mLaunchIntent = getIntent();
        String text = mLaunchIntent.getStringExtra(Note.EXTRA_NOTE_TEXT);
        mExistingNote = text != null;
        if (mExistingNote) {
            long time = mLaunchIntent.getLongExtra(Note.EXTRA_NOTE_TIME, 0);
            // Display the text of the note that was passed in the intent.
            mNoteEntry.setText(text);

            // Set the previous timestamp as the title
            setTitle(Note.format(time));

            // Disable editing
            setTextEditingEnabled(false);
        } else {
            // No note to view, so let the user add a new note
            mEdited = true;
        }
    }

    private void promptToConfirmEditing() {
        AlertDialog.Builder db = new AlertDialog.Builder(AddNoteActivity.this);
        DialogInterface.OnClickListener listener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case Dialog.BUTTON_POSITIVE:
                                setTextEditingEnabled(true);
                                setTitle(R.string.dialog_edit_note_title);
                                invalidateOptionsMenu();
                                return;
                            default:
                                mNoteEntry.clearFocus();
                                dialog.cancel();
                        }
                    }
                };
        db.setMessage(R.string.prompt_confirm_edit_note);
        db.setPositiveButton(R.string.yes, listener);
        db.setNegativeButton(R.string.no, listener);
        db.show();
    }

    private void promptToConfirmDiscard() {
        promptToConfirmResult(RESULT_CANCELED, R.string.prompt_discard_changes);
    }

    private void promptToConfirmDelete() {
        promptToConfirmResult(RESULT_DELETED, R.string.prompt_delete_note);
    }

    // Called by promptToConfirmDiscard() and promptToConfirmDelete()
    private void promptToConfirmResult(final int resultCode, @StringRes int promptId) {
        AlertDialog.Builder db = new AlertDialog.Builder(AddNoteActivity.this);
        DialogInterface.OnClickListener listener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case Dialog.BUTTON_POSITIVE:
                                setResult(resultCode);
                                finish(); // Finish without saving
                                return;
                            default:
                                return;
                        }
                    }
                };
        db.setMessage(promptId);
        db.setPositiveButton("Yes", listener);
        db.setNegativeButton("No", listener);
        db.show();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    private void setTextEditingEnabled(boolean enabled) {
        mNoteEntry.getBackground().setAlpha(enabled ? 255 : 0);
        mNoteEntry.setCursorVisible(enabled);
        mNoteEntry.setFocusable(enabled);
        mNoteEntry.setFocusableInTouchMode(enabled);
        if (enabled) {
            mNoteEntry.requestFocus();
            mEdited = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mExistingNote) {
            getMenuInflater().inflate(R.menu.menu_edit_note, menu);
        }
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mEdited && mExistingNote) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_discard_changes).setVisible(true);
        }
        menu.findItem(R.id.action_done).setVisible(mEdited);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.action_done:
                saveAndFinish();
                return true;

            case R.id.action_edit:
                promptToConfirmEditing();
                return true;

            case R.id.action_discard_changes:
                promptToConfirmDiscard();
                return true;

            case R.id.action_delete:
                promptToConfirmDelete();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveAndFinish();
    }

    private void saveAndFinish() {
        String text = mNoteEntry.getText().toString();

        if (text.isEmpty()) {
            // The user backspaced all the text, so delete the note.
            setResult(RESULT_DELETED);
            finish();
        } else if (mEdited) {
            long time = System.currentTimeMillis();
            if (mLaunchIntent.hasExtra(ScriptureIntent.EXTRA_SCRIPTURE)) {
                // If the launch intent has an EXTRA_SCRIPTURE extra, it means this activity was
                // launched from a widget, so write the new note directly to file.
                Scripture s = mLaunchIntent.getParcelableExtra(ScriptureIntent.EXTRA_SCRIPTURE);
                File noteFile = new File(getDir(Scripture.NOTES_DIR, Context.MODE_PRIVATE),
                        s.getFilename());
                Note.writeNoteToFile(time, text, noteFile);
            } else {
                // Otherwise, send the note via Intent back to the ScriptureViewActivity that
                // launched this activity
                mLaunchIntent.putExtra(Note.EXTRA_NOTE_TEXT, text);
                mLaunchIntent.putExtra(Note.EXTRA_NOTE_TIME, time);
            }

            Toast.makeText(this, R.string.toast_note_saved, Toast.LENGTH_SHORT).show();

            // Set activity result and finish
            setResult(RESULT_OK, mLaunchIntent);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}
