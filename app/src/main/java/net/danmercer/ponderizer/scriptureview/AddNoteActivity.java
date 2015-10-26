package net.danmercer.ponderizer.scriptureview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.danmercer.ponderizer.R;

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
                                setTitle("Edit Note");
                                invalidateOptionsMenu();
                                return;
                            default:
                                mNoteEntry.clearFocus();
                                dialog.cancel();
                        }
                    }
                };
        db.setMessage("Are you sure you want to edit this note?");
        db.setPositiveButton("Yes", listener);
        db.setNegativeButton("No", listener);
        db.show();
    }

    private void promptToConfirmDiscard() {
        promptToConfirmResult(RESULT_CANCELED, "Are you sure you want to discard your changes?");
    }

    private void promptToConfirmDelete() {
        promptToConfirmResult(RESULT_DELETED, "Are you sure you want to delete this note?");
    }

    // Called by promptToConfirmDiscard() and promptToConfirmDelete()
    private void promptToConfirmResult(final int resultCode, String prompt) {
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
        db.setMessage(prompt);
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
        } else {
            mLaunchIntent.putExtra(Note.EXTRA_NOTE_TEXT, text);
            mLaunchIntent.putExtra(Note.EXTRA_NOTE_TIME, System.currentTimeMillis());

            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();

            // Set activity result and finish, going back to the ScriptureViewActivity
            setResult(RESULT_OK, mLaunchIntent);
            finish();
        }
    }

}
