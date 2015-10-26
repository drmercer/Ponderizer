package net.danmercer.ponderizer.scriptureview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.danmercer.ponderizer.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class AddNoteActivity extends AppCompatActivity {

    private File mFile;
    private TextView mNoteEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFile = (File) getIntent().getSerializableExtra(NotesViewFragment.Note.EXTRA_NOTE_FILE);
        mNoteEntry = (TextView) findViewById(R.id.note_edittext);
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
            // Save the note to the file
            long timestamp = System.currentTimeMillis();
            String note = mNoteEntry.getText().toString();
            try {
                // Make sure the file exists.
                boolean append = !mFile.createNewFile();

                // Open a FileWriter in append mode
                FileWriter fw = new FileWriter(mFile, append);

                // Write Note to file
                new NotesViewFragment.Note(timestamp, note).writeToFile(fw);

                // Flush and close the file
                fw.flush();
                fw.close();
            } catch (IOException e) {
                Log.e("AddNoteActivity", "Couldn't save Note properly", e);
            }

            // Set activity result and finish, going back to the ScriptureViewActivity
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
