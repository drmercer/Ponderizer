package net.danmercer.ponderizer;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import net.danmercer.ponderizer.scriptureview.Note;

import java.util.LinkedList;

public class ExportActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private static final int REQUEST_SHARE = 2;
    private RadioButton mDateOnlyButton, mDateAndTimeButton;
    private Scripture mScripture;
    private CheckBox mScripBox, mRefBox, mNotesBox, mNoteHeadersBox;
    private Button mExportButton;
    private Button mClipboardButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mScripture = getIntent().getParcelableExtra(Scripture.EXTRA_SCRIPTURE);
        boolean hasNotes = mScripture.hasNotes(this);

        mNoteHeadersBox = (CheckBox) findViewById(R.id.checkBox_noteHeaders);
        mNotesBox = (CheckBox) findViewById(R.id.checkBox_notes);
        mDateOnlyButton = (RadioButton) findViewById(R.id.radioButton1);
        mDateAndTimeButton = (RadioButton) findViewById(R.id.radioButton2);
        if (hasNotes) {
            // Set up "Include Note Headers" box to depend upon "Include Notes" checkbox.
            mNotesBox.setOnCheckedChangeListener(this);
            // Set up radio buttons to depend upon "Include Note Headers" checkbox.
            mNoteHeadersBox.setOnCheckedChangeListener(this);
            // Manually fire listener for initial setup.
            onCheckedChanged(mNoteHeadersBox, mNoteHeadersBox.isChecked());
        } else {
            // No notes, so disable everything from "Include Notes" down.
            mNotesBox.setChecked(false);
            mNotesBox.setEnabled(false);
            // This line tells the user why the checkboxes are disabled.
            mNotesBox.setText(mNotesBox.getText() + " " + getResources().getString(R.string.no_notes));
            mNoteHeadersBox.setChecked(false);
            mNoteHeadersBox.setEnabled(false);
            mDateAndTimeButton.setChecked(false);
            mDateAndTimeButton.setEnabled(false);
            mDateOnlyButton.setChecked(false);
            mDateOnlyButton.setEnabled(false);
        }

        // Attach other active checkboxes to listener as well
        mRefBox = (CheckBox) findViewById(R.id.checkBox_scriptureReference);
        mRefBox.setOnCheckedChangeListener(this);
        mScripBox = (CheckBox) findViewById(R.id.checkBox_scriptureText);
        mScripBox.setOnCheckedChangeListener(this);

        // Set up Export Buttonm
        mExportButton = (Button) findViewById(R.id.button_export);
        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                export();
            }
        });

        // Set up Copy to clipboard Button
        mClipboardButton = (Button) findViewById(R.id.button_clipboard);
        mClipboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.checkBox_notes:
                if (!isChecked) {// If "Notes" is unchecked, disable "Note headers"
                    mNoteHeadersBox.setChecked(false);
                }
                mNoteHeadersBox.setEnabled(isChecked);
                break;

            case R.id.checkBox_noteHeaders:
                mDateAndTimeButton.setEnabled(isChecked);
                mDateOnlyButton.setEnabled(isChecked);
                return; // Since it was only the Note Headers checkbox that changed, we can return from
                // here because the code below is irrelevant.
        }

        // We only want the Export and Clipboard buttons to be clickable if the user has at least
        // one checkbox selected.
        boolean atLeastOneChecked = mRefBox.isChecked()
                || mScripBox.isChecked()
                || mNotesBox.isChecked();
        mExportButton.setEnabled(atLeastOneChecked);
        mClipboardButton.setEnabled(atLeastOneChecked);
    }

    private void export() {
       String export = generateText();

        // Share string with intent
        Intent i = new Intent(Intent.ACTION_SEND);
        i.putExtra(Intent.EXTRA_TEXT, export);
        Intent chooser = Intent.createChooser(i, "Share via...");
        i.setType("text/plain");
        startActivityForResult(chooser, REQUEST_SHARE);
    }

    private void copyToClipboard() {
        String text = generateText();

        // Copy text to clipboard.
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Scripture", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Copied to clipboard.", Toast.LENGTH_SHORT).show();
    }

    // Calls generateText() // TODO: left off here. finish this comment
    private String generateText() {
        CheckBox cbScriptureText = (CheckBox) findViewById(R.id.checkBox_scriptureText);
        CheckBox cbScriptureRef = (CheckBox) findViewById(R.id.checkBox_scriptureReference);
        CheckBox cbNotes = (CheckBox) findViewById(R.id.checkBox_notes);
        CheckBox cbNoteHeaders = (CheckBox) findViewById(R.id.checkBox_noteHeaders);

        boolean text = cbScriptureText.isChecked();
        boolean ref = cbScriptureRef.isChecked();
        boolean notes = cbNotes.isChecked();
        boolean headers = cbNoteHeaders.isChecked();
        boolean timeInHeaders = mDateAndTimeButton.isChecked();

        return generateText(this, mScripture, text, ref, notes, headers, timeInHeaders);
    }

    private static String generateText(Context c, Scripture s, boolean text, boolean ref,
                                       boolean notes, boolean headers, boolean timeInHeaders) {
        StringBuilder sb = new StringBuilder();
        if (ref) {
            sb.append(s.getReference()).append("\n\n");
        }
        if (text) {
            sb.append(s.getBody()).append("\n\n");
        }
        if (notes) {
            LinkedList<Note> list = new LinkedList<>();
            Note.loadFromFile(s.getNotesFile(c), list);
            for (Note n : list) {
                if (headers) {
                    if (timeInHeaders) {
                        sb.append(n.getTimeString());
                    } else {
                        sb.append(n.getDateString());
                    }
                    sb.append(":\n\n");
                }
                sb.append(n.getText()).append("\n\n");
            }
        }
        return sb.toString().trim();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHARE) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
